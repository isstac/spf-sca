package sidechannel.cost.approximate.monitor;

import gov.nasa.jpf.symbc.bytecode.MULTIANEWARRAY;
import gov.nasa.jpf.symbc.bytecode.NEW;
import gov.nasa.jpf.symbc.bytecode.NEWARRAY;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.ThreadInfo;

/**
*
* @author Quoc-Sang Phan <sang.phan@sv.cmu.edu>
*
*/
public class MemoryMonitor extends BytecodeMonitor{

	@Override
	public long updateCost(long currentCost, ThreadInfo currentThread, Instruction executedInstruction) {
		boolean isNew = false;
		int objRef = -1;

		if (executedInstruction instanceof NEW) {
			isNew = true;
			objRef = ((NEW) executedInstruction).getNewObjectRef();
		}

		if (executedInstruction instanceof NEWARRAY
				|| executedInstruction instanceof MULTIANEWARRAY) {

			isNew = true;
			StackFrame frame = currentThread.getModifiableTopFrame();
			objRef = frame.peek();
		}

		if (isNew && (objRef > 0)) {
			ElementInfo ei = currentThread.getHeap().get(objRef);
			int alloc = ei.getHeapSize();
			currentCost += alloc;
		}
		return currentCost;
	}

}
