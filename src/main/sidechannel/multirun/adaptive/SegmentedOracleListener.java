package sidechannel.multirun.adaptive;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.jvm.bytecode.JVMReturnInstruction;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.symbc.numeric.PCChoiceGenerator;
import gov.nasa.jpf.symbc.numeric.PathCondition;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.SystemState;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import sidechannel.choice.DomainChoiceGenerator;
import sidechannel.multirun.MultiRunListener;
import sidechannel.util.ModelCounter;
import sidechannel.util.PathConditionUtils;

/**
 *
 * @author Quoc-Sang Phan <sang.phan@sv.cmu.edu>
 *
 */
public class SegmentedOracleListener extends MultiRunListener {
	
	public static class Results implements Serializable {

		private static final long serialVersionUID = -3191799071427038671L;

		public int channelCapacity;
		public long pathLength;

		public double leakage;

		Results() {
			leakage = 0;
		}

		@Override
		public String toString() {

			StringBuilder b = new StringBuilder();
			b.append("Shannon leakage is: " + leakage).append(
					System.lineSeparator());
			b.append("Channel capacity is: ")
					.append(Math.log(channelCapacity) / Math.log(2))
					.append(System.lineSeparator());
			return b.toString();
		}
	}

	private final Results results = new Results();

	// the observable in multiple-run, which is a tuple of costs
	// TODO: can it be merged with the obsrv of the super class?
	public HashMap<ArrayList<Long>, ArrayList<String>> oracleObsrv = new HashMap<ArrayList<Long>, ArrayList<String>>();
	
	HashMap<String,Long> table;
	
	ModelCounter counter;

	public SegmentedOracleListener(Config config) {
		super(config);
		
		table = new HashMap<String,Long>();
		
		mode = LATTE_MODE;
		
		DEBUG = true;
	}

	public void searchFinished(Search search) {

		System.out.println("\n>>>>> There are " + numOfPCs 
				+ " path conditions and " + oracleObsrv.size() + " observables \n");
		
		if(oracleObsrv.size() == 0) {
			// there is no symbolic path, no leaks
			System.out.println("This program satisfies non-interference");
			return;
		}
		
		counter = new ModelCounter(conf,collector);

		results.channelCapacity = oracleObsrv.size();
		results.leakage = computeEntropiesOfObservables(); 
				//computeEntropyOfObservables();
		
		if (DEBUG) {
			System.out.println(results.toString());
		}
	}

	@Override
	public void instructionExecuted(VM vm, ThreadInfo currentThread,
			Instruction nextInstruction, Instruction executedInstruction) {

		PathCondition pc =  null;

		SystemState ss = vm.getSystemState();
		if (!ss.isIgnored()) {
			
			// TODO: add temporary costs, or final cost if run = num_run
			if (executedInstruction instanceof JVMReturnInstruction) {
								
				MethodInfo mi = executedInstruction.getMethodInfo();
				ClassInfo ci = mi.getClassInfo();
				if (null != ci) {
					
					// String className = ci.getName();
					String methodName = mi.getName();
					// if (className.equals(conf.getProperty("target")) &&
					// methodName.equals("main")) {
					if (methodName.equals("main")) {

						// get current PC
						String currentPC = null;
						ChoiceGenerator<?> cg = vm
								.getLastChoiceGeneratorOfType(PCChoiceGenerator.class);
						if (cg != null) {
							pc = ((PCChoiceGenerator) cg).getCurrentPC();
							if (pc != null) {
								pc.solve();

								currentPC = PathConditionUtils
										.cleanExpr(pc.header.toString());
								collector.collectVariables(pc);
							}
						}

						// add PC to the list
						if (currentPC != null) {
							
							numOfPCs++;
							ChoiceGenerator<?>[] cgs = ss.getChoiceGenerators();
							ArrayList<Long> currentCost = costModel.getCurrentCost(ss);
							PathCondition assumptions = getAssumptions(cgs);
							String domainStr = null;
							if(assumptions!=null){
								domainStr = PathConditionUtils
											.cleanExpr(assumptions.header.toString());
							}
							ArrayList<String> data = new ArrayList<String>();
							data.add(currentPC);
							data.add(domainStr);
							oracleObsrv.put(currentCost, data);
						}
					}
				}
			}
		}
	}
	
	// compute multiple-entropies
	protected double computeEntropiesOfObservables() {
		double leakage = 0;
		
		Iterator<Map.Entry<ArrayList<Long>, ArrayList<String>>> it = oracleObsrv.entrySet()
				.iterator();
		
		int count = 0;
			
		int run = 0;
		int SIZE = 0; // SIZE of the oracle

		String[] target_args = conf.getProperty("target.args").split(",");
		run = Integer.parseInt(target_args[0]);
		SIZE = Integer.parseInt(target_args[1]);
		
		int MIN = Integer.parseInt(conf.getProperty("symbolic.min_int", String.valueOf(Integer.MIN_VALUE)));
		int MAX = Integer.parseInt(conf.getProperty("symbolic.max_int", String.valueOf(Integer.MAX_VALUE)));
		
		// if each variable has domain D, then n variables has domain D^n
		double defaultDomain = Math.pow(MAX - MIN + 1, collector.getListOfVariables().size()); // domain of the input

		double prob = 0;
		
		while (it.hasNext()) {
			Map.Entry<ArrayList<Long>, ArrayList<String>> pair = (Map.Entry<ArrayList<Long>, ArrayList<String>>) it.next();
			ArrayList<String> pcAndDomain = pair.getValue();

			String PC = pcAndDomain.get(0);
			String domainString = pcAndDomain.get(1);
			
			Long block = countModel(PC);
			double domain;
			
			if(domainString != null) 
				domain = countModel(domainString);
			else
				domain = defaultDomain;
			
			double p = block / domain;
			
			leakage -= p * Math.log(p);
			
			ArrayList<Long> costs = pair.getKey();

			boolean isWorstCase = false;
			if(costs.size() == run && costs.get(run - 1) == SIZE){
				isWorstCase = true;
			}
			
			if(!isWorstCase && !verbose){
				continue;
			}

			System.out.println("\n=====");
			System.out.println(pcAndDomain.get(0));
			System.out.print("Sequence of cost is: ");
			for (long cost : costs) {
				System.out.print(cost + " ");
			}
			System.out.println();
			System.out.println("The block " + count + " is " + block);
			if(domainString != null){
				System.out.println("The assumption is: " + domainString);
			}
			System.out.println("The domain with assumption is: " + domain);
			System.out.println("The block probability " + count + " is " + p);
			
			if(isWorstCase) {
				prob += p;
			}
			System.out.println("=====\n");
			count++;

		}
		System.out.println("The overall probability is: " + prob);
		
		leakage = leakage / Math.log(2);
		cleanDirectory();
		
		return leakage;
	}
	
	private long countModel(String PC){
		Long result = table.get(PC);
		if (result != null){
			return result;
		}
		BigInteger points = counter.countSinglePath(PC);
		result = Long.parseLong(points.toString());
		table.put(PC, result);
		return result;
	}

	private PathCondition getAssumptions(ChoiceGenerator<?>[] cgs) {
		// A method sequence is a vector of strings
		PathCondition pc = null;
		ChoiceGenerator<?> cg = null;
		// explore the choice generator chain - unique for a given path.
		for (int i = 0; i < cgs.length; i++) {
			cg = cgs[i];
			if ((cg instanceof DomainChoiceGenerator)) {
				if(pc == null){
					pc = new PathCondition();
				}
				DomainChoiceGenerator dcg = (DomainChoiceGenerator) cg;
				PathConditionUtils.appendHead(pc, dcg.getPathCondition().header);
			}
		}
		return pc;
	}
	
	private void cleanDirectory(){
		// clean up the directory
		String tmpDir = conf.getProperty("symbolic.reliability.tmpDir");
		try {
			FileUtils.cleanDirectory(new File(tmpDir));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
