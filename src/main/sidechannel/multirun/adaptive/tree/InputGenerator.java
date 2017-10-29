package sidechannel.multirun.adaptive.tree;

import gov.nasa.jpf.Config;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import sidechannel.common.Common;
import sidechannel.multirun.adaptive.AbstractPartitionVisitor;
import sidechannel.multirun.adaptive.AllPartitionsGenerator;
import sidechannel.multirun.adaptive.MaximalPartitionGeneratorUsingFile;
import sidechannel.multirun.adaptive.MultiplePartitionsGenerator;
import sidechannel.multirun.adaptive.PartitionGenerator;
import sidechannel.tree.InputNode;
import sidechannel.tree.Node;
import sidechannel.util.SymbolicVariableCollector;

/**
*
* @author Quoc-Sang Phan <sang.phan@sv.cmu.edu>
*
*/
public class InputGenerator implements AbstractPartitionVisitor<Long>{

	private Config conf;
	private PartitionGenerator<Long> pg;
	private Node parent;
	private ArrayList<Node> children = new ArrayList<Node>();
	private SymbolicVariableCollector collector;
	private boolean DEBUG = false;
	
	public InputGenerator(Config config, Node parent){
		conf = config;
		String tree = config.getProperty("attack.tree","full");
		String theory = config.getProperty("SMT.theory","bitvector");
		boolean linear = theory.equals("linear");
		switch(tree){
		case "greedy":
			pg = new MaximalPartitionGeneratorUsingFile<Long>(config, this, linear);
			break;
		case "multi":
			pg = new MultiplePartitionsGenerator<Long>(config, this, linear);
		    break;
		case "entropy":
			pg = new AllPartitionsGenerator<Long>(config, this, linear);
			break;    
		case "full":
			pg = new AllPartitionsGenerator<Long>(config, this, linear);
			break;
		default:
			// wrong tree
			assert false;
		}
		
		this.parent = parent;
	}
	
	public void generateInput(HashMap<Long, HashSet<String>> obsrv, SymbolicVariableCollector collector){
		this.collector = collector;
		pg.computePartitions(obsrv, collector);
	}
	
	@Override
	public void visit(Integer[] lowInput, Set<Long> allTheCosts) {
		int id = ++AttackTreeBuilder.id;
		InputNode child = new InputNode(parent.depth + 1,id,parent);
		children.add(child);
		StringBuilder sb = new StringBuilder();
		// write the input into file
		for (String var : collector.getListOfVariables()) {
			if (var.charAt(0) == 'l') {
				int index = Common.indexOf(var);
				// convert hexadecimal number begin with #x
				sb.append(var + ":" + lowInput[index]+ "\n");
			}
		}
		sb.delete(sb.length() - 1, sb.length());
		child.value = sb.toString();
		
		// pruning
		if(allTheCosts.size() <= 1){
			child.setDeadEnd();
			++AttackTreeBuilder.numOfDeadEnds;
			return;
		}
		
		if(DEBUG){
			System.out.println("--------------------------------------------");
			System.out.println(sb.toString());
			for(Long cost : allTheCosts){
				System.out.println("The cost is " + cost);
			}
			System.out.println("--------------------------------------------\n");
		}
		
		String fileName = conf.getProperty("sidechannel.tmpDir","build/tmp") 
				+ "/" + Common.INPUT_PREFIX + id + ".txt";
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
	
	public ArrayList<Node> getInputNodes(){
		return children;
	}
}
