package sidechannel.cost.approximate;

import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;
import sidechannel.cost.Cost;
import sidechannel.cost.approximate.monitor.PathMonitor;

/**
*
* Approximate cost model by monitoring the symbolic virtual machine
*
* @author Quoc-Sang Phan <sang.phan@sv.cmu.edu>
*
*/
public abstract class ApproximateCost <Unit> implements Cost<Unit> {
	
	public static final int UNDEFINED = -1;
	public static final int CUSTOMIZED = 0;
	public static final int TIMING = 1;
	public static final int MEMORY = 2;
	public static final int FILE = 3;
	public static final int SOCKET = 4;
	
	protected int sideChannel = UNDEFINED;
	protected PathMonitor monitor = null;
	
	public void instructionExecuted(VM vm, ThreadInfo currentThread,
			Instruction executedInstruction){
		if(monitor != null){
			monitor.instructionExecuted(vm, currentThread, executedInstruction);
		}
	}

	public void choiceGeneratorAdvanced() {
		if(monitor != null){
			monitor.choiceGeneratorAdvanced();
		}
	}

	public void stateBacktracked() {
		if(monitor != null){
			monitor.stateBacktracked();
		}
	}
}
