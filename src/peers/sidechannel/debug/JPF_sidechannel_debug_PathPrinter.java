package sidechannel.debug;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.annotation.MJI;
import gov.nasa.jpf.symbc.numeric.PCChoiceGenerator;
import gov.nasa.jpf.symbc.numeric.PathCondition;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.NativePeer;
import gov.nasa.jpf.vm.VM;

import java.util.HashSet;

import sidechannel.util.ModelCounter;
import sidechannel.util.PathConditionUtils;
import sidechannel.util.SymbolicVariableCollector;

/**
 *
 * @author Quoc-Sang Phan <sang.phan@sv.cmu.edu>
 *
 */
public class JPF_sidechannel_debug_PathPrinter extends NativePeer {
	
	static SymbolicVariableCollector collector = null;

	@MJI
	public static void printPathSize____V(MJIEnv env, int objRef){
		VM vm = env.getVM();
		ChoiceGenerator<?> cg = vm.getChoiceGenerator();
		PathCondition pc = null;

		if (!(cg instanceof PCChoiceGenerator)) {
			ChoiceGenerator<?> prev_cg = cg.getPreviousChoiceGenerator();
			while (!((prev_cg == null) || (prev_cg instanceof PCChoiceGenerator))) {
				prev_cg = prev_cg.getPreviousChoiceGenerator();
			}
			cg = prev_cg;
		}

		if ((cg instanceof PCChoiceGenerator) && cg != null) {
			pc = ((PCChoiceGenerator) cg).getCurrentPC();
		}
		
		if(pc == null){
			System.out.println("PC is null");
			return;
		}
		
		HashSet<String> setOfSymVar = new HashSet<String>();
		collector = new SymbolicVariableCollector(setOfSymVar);
		String strPC = PathConditionUtils.cleanExpr(pc.header.toString());
		collector.collectVariables(pc);
		Config conf = vm.getConfig();
		ModelCounter counter = new ModelCounter(conf,collector);
		System.out.println("\n=====");
		System.out.println(strPC);
		System.out.println(">>>>> The number of model is: "
				+ counter.countSinglePath(strPC).toString());
		System.out.println("=====\n");
	}
}
