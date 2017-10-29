package sidechannel.cost.approximate.monitor;

import java.util.Stack;

import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;
import sidechannel.cost.approximate.ApproximateCost;

/**
*
* Monitor a symbolic path
*
* @author Quoc-Sang Phan <sang.phan@sv.cmu.edu>
*
*/
public abstract class PathMonitor
{

	protected String secureMethod = null;
	
	protected long currentCost = 0;
	protected Stack<Long> steps = new Stack<Long>();
	protected BytecodeMonitor monitor;
	
	public PathMonitor(int sideChannel){
		switch(sideChannel){
		case ApproximateCost.TIMING:
			// approximate elapsed time with the number of instruction
			monitor = new InstructionCounter();
			break;
		case ApproximateCost.MEMORY:
			monitor = new MemoryMonitor();
			break;
		case ApproximateCost.FILE:
			monitor = new FileMonitor();
			break;
		case ApproximateCost.SOCKET:
			monitor = new SocketMonitor();
			break;
		default:
			// unsupported type of side channel
			assert false;
		}
	}
	
	public abstract void instructionExecuted(VM vm, ThreadInfo currentThread,
			Instruction executedInstruction);
	
	public Long getCurrentCost(){
		return currentCost;
	}
	
	public void choiceGeneratorAdvanced() {
		steps.push(currentCost);
	}

	public void stateBacktracked() {
		currentCost = steps.pop();
	}
}
