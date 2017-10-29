package sidechannel;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.PropertyListenerAdapter;
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import sidechannel.cost.approximate.ApproximateCost;
import sidechannel.util.SymbolicVariableCollector;

/**
 *
 * @author Quoc-Sang Phan <sang.phan@sv.cmu.edu>
 *
 */
public abstract class AbstractSideChannelListener <Cost,SymbolicPath> extends PropertyListenerAdapter{
	
	protected Config conf;
	
	protected int numOfPCs = 0;
	
	protected HashMap<Cost, HashSet<SymbolicPath>> obsrv = new HashMap<Cost, HashSet<SymbolicPath>>();
	
	protected SymbolicVariableCollector collector;
	
	protected boolean DEBUG = false;
	
	protected int inputSize1;
	
	protected boolean verbose = true;
	
	protected int sideChannel = ApproximateCost.CUSTOMIZED;
	
	protected ApproximateCost<Cost> costModel;
	
	public AbstractSideChannelListener (Config config){
		conf = config;
		
		/*
		verbose = conf.getProperty("sidechannel.verbose","false").trim().equals("true");
		if(!verbose){
			jpf.getReporter().getPublishers().clear();
		}
		//*/

		HashSet<String> setOfSymVar = new HashSet<String>();
		collector = new SymbolicVariableCollector(setOfSymVar);	
				
		String type = conf.getProperty("sidechannel","customized");
		switch(type){
		case "customized":
			sideChannel = ApproximateCost.CUSTOMIZED;
			break;
		case "timing":
			sideChannel = ApproximateCost.TIMING;
			break;
		case "memory":
			sideChannel = ApproximateCost.MEMORY;
			break;
		case "file":
			sideChannel = ApproximateCost.FILE;
			break;
		case "socket":
			sideChannel = ApproximateCost.SOCKET;
		default:
			assert false;
		}
		
		DEBUG = conf.getProperty("sidechannel.debug","false").equals("true");
	}
	
	@Override
	public void instructionExecuted(VM vm, ThreadInfo currentThread,
			Instruction nextInstruction, Instruction executedInstruction) {

		PathCondition pc;

		SystemState ss = vm.getSystemState();
		if (!ss.isIgnored()) {
			
			costModel.instructionExecuted(vm, currentThread, executedInstruction);
			
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

						ChoiceGenerator<?> cg = vm
								.getLastChoiceGeneratorOfType(PCChoiceGenerator.class);
						if (cg != null) {
							pc = ((PCChoiceGenerator) cg).getCurrentPC();
							if (pc != null) {
								processPC(pc,ss);
							}
						}
					}
				}
			}
		}
	}
	
	@Override
	public void choiceGeneratorAdvanced(VM vm, ChoiceGenerator<?> currentCG) {
		costModel.choiceGeneratorAdvanced();
	}

	@Override
	public void stateBacktracked(Search search) {
		costModel.stateBacktracked();
	}
	
	protected void processPC(PathCondition pc, SystemState ss ){
		pc.solve();

		collector.collectVariables(pc);
		SymbolicPath currentPC = format(pc);
		numOfPCs++;
		Cost currentCost = costModel.getCurrentCost(ss);
		HashSet<SymbolicPath> data = obsrv.get(currentCost);
		if (data == null) {
			data = new HashSet<SymbolicPath>();
			data.add(currentPC);
			obsrv.put(currentCost, data);
			// reset current cost
		}else{
			data.add(currentPC);
		}
	}
	
	/*
	 * do nothing
	 */
	protected abstract SymbolicPath format(PathCondition pc);
	
	public Set<String> getListOfVariables(){
		return collector.getListOfVariables();
	}
}
