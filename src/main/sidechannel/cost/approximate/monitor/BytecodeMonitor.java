package sidechannel.cost.approximate.monitor;

import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.ThreadInfo;

/**
*
* Monitor a single byte code instruction
*
* @author Quoc-Sang Phan <sang.phan@sv.cmu.edu>
*
*/
public abstract class BytecodeMonitor {

	/*
	 * update the cost after the instruction is executed
	 */
	public abstract long updateCost (long currentCost, ThreadInfo currentThread, Instruction executedInstruction);
}
