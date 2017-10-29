package sidechannel.cost.approximate.monitor;

import gov.nasa.jpf.symbc.bytecode.INVOKESTATIC;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.ThreadInfo;

public class InstructionCounter extends BytecodeMonitor {

	@Override
	public long updateCost(long currentCost, ThreadInfo currentThread, Instruction executedInstruction) {
		++currentCost;

		// for Thread.sleep
		if (executedInstruction instanceof INVOKESTATIC) {
			INVOKESTATIC instruction = (INVOKESTATIC) executedInstruction;
			MethodInfo methodInfo = instruction.getInvokedMethod();
			String className = methodInfo.getClassName();
			String methodName = methodInfo.getName();
			if(className.equals("java.lang.Thread") &&
					methodName.equals("sleep")){
				System.out.println(instruction.toString() + " ");	
				Object[] values = instruction.getArgumentValues(currentThread);
				long value = (long)values[0];
				currentCost += value * 10000;
			}
		}		
		return currentCost;
	}

}
