package sidechannel.cost.approximate.monitor;

import java.io.OutputStream;

import gov.nasa.jpf.symbc.bytecode.INVOKEVIRTUAL;
import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.IntegerFieldInfo;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.ThreadInfo;

/**
*
* @author Quoc-Sang Phan <sang.phan@sv.cmu.edu>
*
*/
public abstract class OutputStreamMonitor extends BytecodeMonitor {

	@Override
	public long updateCost(long currentCost, ThreadInfo currentThread, Instruction executedInstruction) {
		if (executedInstruction instanceof INVOKEVIRTUAL) {

			INVOKEVIRTUAL instruction = (INVOKEVIRTUAL) executedInstruction;
			MethodInfo methodInfo = instruction.getInvokedMethod();
			String className = methodInfo.getClassName();
			String methodName = methodInfo.getName();

			ClassInfo classInfo = methodInfo.getClassInfo();
			IntegerFieldInfo fieldInfo = (IntegerFieldInfo) classInfo
					.getDeclaredInstanceField("source");
			ElementInfo ei = instruction.getThisElementInfo(currentThread);

			try {
				Class<?> cls = Class.forName(className);

				// TCP or FileStream
				if (OutputStream.class.isAssignableFrom(cls)
						&& methodName.equals("write")) {

					int source = ei.getIntField(fieldInfo);
					if (source == getSource()) {
						currentCost++;
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		return currentCost;
	}
	
	abstract int getSource();

}
