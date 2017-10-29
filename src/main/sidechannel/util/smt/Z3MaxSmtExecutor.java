package sidechannel.util.smt;

import gov.nasa.jpf.Config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import sidechannel.common.Common;
import sidechannel.util.ConfigUtils;

/**
 * Execute Z3 on the Max-SMT file, parse results
 *
 * @author Quoc-Sang Phan <sang.phan@sv.cmu.edu>
 *
 */
public class Z3MaxSmtExecutor <Cost> {
	
	private Config conf;
	private Map<String,Cost> map;
	private boolean LINEAR;
	private static final String SYMBOL = "objectives";
	private Integer[] lowInput;
	private Set<Cost> allTheCosts;
	private ArrayList<String> disjunctions;
	private int count = 0;
	private boolean verbose = false;
	private StringBuilder sbOut = null;
	private Map<String,Integer> model;
	
	public Z3MaxSmtExecutor(Config config, boolean linear, boolean verbose){
		conf = config;
		LINEAR = linear;
		this.verbose = verbose;
		if(verbose){
			sbOut = new StringBuilder("\n===== MAX-SMT OUTPUT =====\n");
		}
	}

	public int getCount(){
		return count;
	}
	
	public Set<Cost> getCosts(){
		return allTheCosts;
	}
	
	public ArrayList<String> getDisjunctions(){
		return disjunctions;
	}
	
	public Integer[] getLowInput(){
		return lowInput;
	}
	
	public Map<String,Integer> getModel(){
		return model;
	}
	
	public boolean run(boolean getCount, boolean getInput, boolean getOutput, Map<String,Cost> map){
		if(getInput){
			int inputSize = ConfigUtils.getLowInputSize(conf);
			lowInput = new Integer[inputSize];
			model = new HashMap<String,Integer>();
		}
		if(getOutput){
			if(map != null){
				this.map = map;
				allTheCosts = new HashSet<Cost>();
			} else{
				disjunctions = new ArrayList<String>();
			}
		}
		try {
			String line;
			String z3 = conf.getProperty("z3");
			String maxSMTfileName = conf.getProperty("sidechannel.smt2","build/tmp/outputZ3bitvec.smt2");
			System.out.println(">>>>> Solving " + maxSMTfileName);
			Process p = Runtime.getRuntime().exec(
					z3 + " " + maxSMTfileName);
			BufferedReader input = new BufferedReader(
					new InputStreamReader(p.getInputStream()));
			while ((line = input.readLine()) != null) {
				
				if(line.contains("unsat")){
					System.out.println(">>>>> Formula is UNSAT");
					return false;
				}
				
				if(verbose){
					sbOut.append(line + "\n");
				}
				
				if(getCount){
					if(searchForCount(input,line)){
						getCount = false;
						continue;
					}
				}
				
				int tmp = line.indexOf("((");
				if (tmp != -1) {
					line = line.substring(2, line.length() - 2);
					String token[] = line.split("\\s");
					if(getInput){
						if(searchForInput(line,token)){
							continue;
						}
					}
					if(getOutput){
						if(map != null){
							if(searchForOutput(line,token)){
								continue;
							}
						} else {
							if(searchForFunction(line,token)){
								continue;
							}
						}
					}
				}
			}	
			if(verbose){
				sbOut.append("===========================");
				System.out.println(sbOut.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}
	
	private boolean searchForCount(BufferedReader input, String line) throws IOException{
		if(line.indexOf(SYMBOL) != -1){
			line = input.readLine();
			if(verbose){
				sbOut.append(line + "\n");
			}
			count = Integer.parseInt(line.substring(3,line.length()-1));
			System.out.println(">>>>> Minimum number of unsat clauses: " + count);
			return true;
		}
		return false;
	}
	
	/*
	 * pass the token as an argument so as not to split again
	 */
	private boolean searchForInput(String line, String[] token){
		if(line.charAt(0) == 'l'){
			int index = Common.indexOf(token[0]);
			if(LINEAR){
				// convert decimal number
				lowInput[index] = Integer.parseInt(token[1]);
			}
			else{
				// convert hexadecimal number begin with #x
				lowInput[index] = Integer.parseInt(
						token[1].substring(2), 16);
			}
			model.put(token[0], lowInput[index]);
			return true;
		}
		return false;
	}
	
	/*
	 * pass the token as an argument so as not to split again
	 */
	private boolean searchForOutput(String line, String[] token){
		if (line.contains(SmtLib2Utils.OBSERVABLE_PREFIX) && token[token.length-1].equals("true")){
			// the PC is true, so get its corresponding cost
			Cost cost = map.get(token[0]);
			allTheCosts.add(cost);
			return true;
		}
		return false;
	}
	
	/*
	 * pass the token as an argument so as not to split again
	 */
	private boolean searchForFunction(String line, String[] token){
		if (line.contains(SmtLib2Utils.OBSERVABLE_PREFIX) && token[token.length-1].equals("true")){
			// the PC is true, so get its corresponding cost
			int beginIndex = line.indexOf("(");
			int endIndex = line.indexOf(")");
			line = line.substring(beginIndex, endIndex + 1);
			disjunctions.add(line);
			return true;
		}
		return false;
	}
}
