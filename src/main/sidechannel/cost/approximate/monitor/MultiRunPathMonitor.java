package sidechannel.cost.approximate.monitor;

import gov.nasa.jpf.jvm.bytecode.JVMInvokeInstruction;
import gov.nasa.jpf.jvm.bytecode.JVMReturnInstruction;
import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;
import sidechannel.choice.CostChoiceGenerator;

/**
*
* @author Quoc-Sang Phan <sang.phan@sv.cmu.edu>
*
*/
public class MultiRunPathMonitor extends PathMonitor {

	public MultiRunPathMonitor(int sideChannel, String secureMethod) {
		super(sideChannel);
		this.secureMethod = secureMethod;
	}

	@Override
	public void instructionExecuted(VM vm, ThreadInfo currentThread, Instruction executedInstruction) {

		currentCost = monitor.updateCost(currentCost, currentThread, executedInstruction);

		// executedInstruction is either INVOKESTATIC or INVOKEVIRTUAL
		if (executedInstruction instanceof JVMInvokeInstruction) {
			/*
			 * this is a naive computation of the cost in multiple-run
			 * analysis we assume that there is only one "secure" method to
			 * be analyzed, defined in "sidechannel.secure_method", there is no
			 * other method interfering from when it is invoked until it
			 * returns. Therefore, we can restart the step, and when it
			 * returns, the cost is saved in the step.
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
			
		// TODO: add temporary costs, or final cost if run = num_run
		if (executedInstruction instanceof JVMReturnInstruction) {
			
			MethodInfo mi = executedInstruction.getMethodInfo();
			ClassInfo ci = mi.getClassInfo();
			if (null != ci) {
				
				if (mi.getBaseName().equals(secureMethod)) {
					// the secure method returns, save the cost as follows:
					// create a new MultiRunChoiceGenerator.
					// this is just to store the cost
					// regarding the "secure" method.
					CostChoiceGenerator cg = new CostChoiceGenerator(
							currentCost);
					// Does not actually make any choice
					vm.getSystemState().setNextChoiceGenerator(cg);
					// nothing to do as there are no choices.
					return;
				}
			}
		}
	}

}
