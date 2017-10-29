package sidechannel.cost.approximate;

import java.util.ArrayList;

import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.SystemState;
import sidechannel.choice.CostChoiceGenerator;
import sidechannel.cost.approximate.monitor.MultiRunPathMonitor;

/**
*
* @author Quoc-Sang Phan <sang.phan@sv.cmu.edu>
*
*/
public class MultipleRunCost extends ApproximateCost <ArrayList<Long>>{

	public MultipleRunCost(int sideChannel, String secureMethod){
		if(secureMethod != null){
			monitor = new MultiRunPathMonitor(sideChannel, secureMethod);
		}
	}
	
	@Override
	public ArrayList<Long> getCurrentCost(SystemState ss) {
		ChoiceGenerator<?>[] cgs = ss.getChoiceGenerators();
		ArrayList<Long> costs = new ArrayList<Long>();
		ChoiceGenerator<?> cg = null;
		// explore the choice generator chain - unique for a given path.
		for (int i = 0; i < cgs.length; i++) {
			cg = cgs[i];
			if ((cg instanceof CostChoiceGenerator)) {
				costs.add(((CostChoiceGenerator) cg).getCost());
			}
		}
		return costs;
	}

}
