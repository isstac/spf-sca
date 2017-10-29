package sidechannel.multirun;

import gov.nasa.jpf.annotation.MJI;
import gov.nasa.jpf.symbc.numeric.IntegerExpression;
import gov.nasa.jpf.symbc.numeric.PCChoiceGenerator;
import gov.nasa.jpf.symbc.numeric.PathCondition;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.NativePeer;
import gov.nasa.jpf.vm.SystemState;
import gov.nasa.jpf.vm.VM;
import sidechannel.choice.DomainChoiceGenerator;
import sidechannel.common.GlobalVariables;
import sidechannel.util.PathConditionUtils;
import sidechannel.util.SymbolicDomain;

/**
*
* @author Quoc-Sang Phan <sang.phan@sv.cmu.edu>
*
*/
public class JPF_sidechannel_multirun_Domain extends NativePeer {

	@MJI
	public static void shrinkAfterAssumption____V(MJIEnv env, int objRef){
		
		VM vm = env.getVM();
		SystemState ss = vm.getSystemState();
		ChoiceGenerator<?> cg = vm.getChoiceGenerator();
		PathCondition pc = null;

		// search for PCChoiceGenerator
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
		
		PathCondition assumptions = null;
		/*
		// reset cg to search for domain
		cg = vm.getChoiceGenerator();
		// search for DomainChoiceGenerator
		if (!(cg instanceof DomainChoiceGenerator)) {
			ChoiceGenerator<?> prev_cg = cg.getPreviousChoiceGenerator();
			while (!((prev_cg == null) || (prev_cg instanceof DomainChoiceGenerator))) {
				prev_cg = prev_cg.getPreviousChoiceGenerator();
			}
			cg = prev_cg;
		}

		if ((cg instanceof DomainChoiceGenerator) && cg != null) {
			assumptions = ((DomainChoiceGenerator) cg).getPathCondition();
		}
		
		if(assumptions == null){
			assumptions = new PathCondition();
		}
		//*/
		assumptions = new PathCondition();
		PathConditionUtils.appendHead(assumptions, pc.header);
		
		DomainChoiceGenerator dcg = new DomainChoiceGenerator(assumptions);
		// System.out.println(">>>>> Add assumption " + assumptions.toString());
		// Does not actually make any choice
		ss.setNextChoiceGenerator(dcg);
		// nothing to do as there are no choices.
	}
	
	@MJI
	public static void setMinMax__III__V(MJIEnv env, int objRef, int var, int min, int max){
		Object [] attrs = env.getArgAttributes();
		IntegerExpression sym_arg = (IntegerExpression)attrs[0];
		if (sym_arg !=null){
			GlobalVariables.domains.put(attrs[0].toString(), new SymbolicDomain(min,max));
		}
	}
}
