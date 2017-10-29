package sidechannel.cost.approximate.monitor;

import gov.nasa.jpf.jvm.bytecode.JVMInvokeInstruction;
import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;

/**
*
* @author Quoc-Sang Phan <sang.phan@sv.cmu.edu>
*
*/
public class SingleRunPathMonitor extends PathMonitor {

	public SingleRunPathMonitor(int sideChannel) {
		super(sideChannel);
	}
	
	public SingleRunPathMonitor(int sideChannel, String secureMethod) {
		super(sideChannel);
		this.secureMethod = secureMethod;
	}

	@Override
	public void instructionExecuted(VM vm, ThreadInfo currentThread, Instruction executedInstruction) {

		// executedInstruction is either INVOKESTATIC or INVOKEVIRTUAL
		if (secureMethod != null && executedInstruction instanceof JVMInvokeInstruction) {
			/*
			 * this is a naive computation of the cost in multiple-run analysis
			 * we assume that there is only one "secure" method to be analyzed,
			 * defined in "sidechannel.secure_method", there is no other method
			 * interfering from when it is invoked until it returns. Therefore,
			 * we can restart the step, and when it returns, the cost is saved
			 * in the step.
			 */
			JVMInvokeInstruction ins = (JVMInvokeInstruction) executedInstruction;
			MethodInfo mi = ins.getInvokedMethod();
			ClassInfo ci = mi.getClassInfo();
			if (null != ci) {
				if (mi.getBaseName().equals(secureMethod)) {
					// reset the current step
					currentCost = 0;
					return;
				}
			}
		}
		
		currentCost = monitor.updateCost(currentCost, currentThread, executedInstruction);
	}
}
