package sidechannel.multirun.adaptive.greedy;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import gov.nasa.jpf.Config;
import sidechannel.common.Common;
import sidechannel.tree.InputNode;
import sidechannel.tree.Node;
import sidechannel.tree.SequenceNode;
import sidechannel.util.Environment;
import sidechannel.util.SymbolicVariableCollector;
import sidechannel.util.smt.BitVectorUtils;
import sidechannel.util.smt.LinearIntegerUtils;
import sidechannel.util.smt.SmtLib2Utils;
import sidechannel.util.smt.Z3MaxSmtExecutor;

/**
*
* @author Quoc-Sang Phan <sang.phan@sv.cmu.edu>
*
*/
public class SequenceGenerator{
	
	private StringBuilder hardConstraints;
	private StringBuilder softAssertions;
	private StringBuilder lowInputs;
	private StringBuilder observables;
	private String engine;
	private Config conf;
	private boolean LINEAR = false;
	private HashMap<String, Long> map;
	private Set<String> highVars = new HashSet<String>();
	private int id = 0;
	private SmtLib2Utils<Long> utils;
	private int numOfRuns;
	
	
	public SequenceGenerator(Config config, HashMap<Long, HashSet<String>> obsrv, SymbolicVariableCollector collector){
		conf = config;
		String theory = config.getProperty("SMT.theory","bitvector");
		LINEAR = theory.equals("linear");
		init(obsrv, collector);
		String path = Environment.find("z3");
		if (path == null){
			assert false;
		}
		numOfRuns = Integer.parseInt(conf.getProperty("multirun.num_run","1"));
		conf.setProperty("z3", path);
	}
	
	private void init(HashMap<Long, HashSet<String>> obsrv, SymbolicVariableCollector collector){
		utils = LINEAR? new LinearIntegerUtils<Long>(conf, collector)
				      : new BitVectorUtils<Long>(conf, collector);
		engine = utils.configSmtEngine();
		// hardConstraints = utils.groupPathConditionsUsingQuantifiers(obsrv);
		hardConstraints = utils.groupPathConditionsWithParameters(obsrv);
		softAssertions = utils.makeSoftAssertionsForMaxSmt();
		lowInputs = utils.getValuesOfLowInput();
		observables = utils.getValuesOfObservables();
		map = utils.getPathConditionsToCost();
	}
	
	private void generateMaxSmt(StringBuilder assumptions) {
		StringBuilder sb = new StringBuilder();
		sb.append(engine);
		sb.append(hardConstraints);
		if(assumptions != null){
			sb.append("\n");
			sb.append(assumptions);
			sb.append("\n");
		}
		sb.append(softAssertions);
		sb.append("\n(check-sat)\n");
		sb.append(lowInputs);
		sb.append(observables);
		String fileName = conf.getProperty("sidechannel.smt2","build/tmp/outputZ3bitvec.smt2");
		SmtLib2Utils.outputToFile(fileName, sb.toString());
	}
	
	public ArrayList<Node> generateSequence(Node parent){
		ArrayList<Node> children = new ArrayList<Node>();
		StringBuilder assumptions = null;
		if(parent.id > 0){
			assumptions = loadAssumptions(parent.id);
		}
		conf.setProperty("sidechannel.smt2","build/tmp/node" + parent.id + ".smt2");
		generateMaxSmt(assumptions);
				
		Z3MaxSmtExecutor<Long> z3 = new Z3MaxSmtExecutor<Long>(conf, LINEAR, false);
		if(!z3.run(true, true, true, null)){
			return null;
		}
		
		Integer[] lowInput = z3.getLowInput();
		InputNode dummy = new InputNode(parent.depth, ++id, parent);
		dummy.value = lowInput[0].toString();
		children.add(dummy);
		
		ArrayList<String> disjunctions = z3.getDisjunctions();
		
		if(disjunctions.size() <= 1){
			dummy.setDeadEnd();
			return children;
		}
		
		ArrayList<String> instances = null;
		
		boolean createAssumption = parent.depth < numOfRuns - 1;
		if(createAssumption){
			instances = instantiate(disjunctions, lowInput);
		}

		for (int i = 0; i < disjunctions.size(); ++i){
			SequenceNode child = new SequenceNode(dummy.depth + 1, ++id, dummy);
			// TODO: this needs to be fixed
			// For now, all the examples are not array, so I only take the first element
			// child.value = Integer.toString(lowInput[0]);
			child.value = map.get(disjunctions.get(i)).toString();
			children.add(child);
			if(createAssumption){
				StringBuilder asm = createAssumptions(instances.get(i));
				if(parent.id > 0){
					asm.insert(0,assumptions + "\n");
				}
				writeAssumptions(child.id, asm);
			}
		}
		return children;
	}
	
	ArrayList<String> instantiate(ArrayList<String> disjunctions, Integer[] lowInput){
		ArrayList<String> instances = new ArrayList<String>();
		for(int i = 0; i < disjunctions.size(); ++i){
			String line = disjunctions.get(i);
			line = line.substring(2, line.length() - 2);
			String[] params = line.split("\\s");
			StringBuilder sb = new StringBuilder();
			for(String para : params){
				if (para.charAt(0) == 'l') {
					int value = lowInput[Common.indexOf(para)];
					if(LINEAR) {
						para = Integer.toString(value);
					} else {
						para = "#x" + String.format("%08X", value & 0xFFFFFFFF);
					}
				}
				
				if (para.charAt(0) == 'h') {
					int pos = para.indexOf(Common.SEPARATOR);
					if(pos > 0){
						para = para.substring(0, pos);
					}
					highVars.add(para);
				}
				sb.append(para + " ");
			}
			instances.add(sb.toString());
		}
		return instances;
	}

	private StringBuilder createAssumptions(String function){
		StringBuilder sb = new StringBuilder();
		for(int i = 1; i <= map.size(); ++i ){
			sb.append("(assert ");
			sb.append(renameFunction(function, i));
			sb.append(" )\n");
		}
		return sb;
	}
	
	private String renameFunction(String function, int index){
		StringBuilder sb = new StringBuilder("( ");
		String[] tokens = function.split("\\s");
		for(String token : tokens){
			if(token.charAt(0) == 'h'){
				token += Common.SEPARATOR + index;
			}
			sb.append(token + " ");
		}
		sb.append(")");
		return sb.toString();
	}
	
	
	private void writeAssumptions(int id, StringBuilder sb){
		String fileName = conf.getProperty("sidechannel.tmpDir","build/tmp") + "/assumption" + id + ".txt";
		Writer writer = null;

		try {
			writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(fileName), "utf-8"));
			writer.write(sb.toString());
		} catch (IOException ex) {
			// report
		} finally {
			try {
				writer.close();
			} catch (Exception ex) {
			}
		}
	}
	
	private StringBuilder loadAssumptions(int id){
		StringBuilder asm = null;
		String fileName = conf.getProperty("sidechannel.tmpDir","build/tmp") + "/assumption" + id + ".txt";
		try{
			asm = new StringBuilder();
			FileInputStream fstream = new FileInputStream(fileName);
			BufferedReader br = new BufferedReader(new InputStreamReader(
					fstream));

			String line = br.readLine();
			while (line != null) {
				asm.append(line + "\n");				
				line = br.readLine();
			}
			br.close();
		} catch(Exception e){
			e.printStackTrace();
		}
		
		return asm;
	}
}
