package sidechannel.cost.approximate;

import java.util.ArrayList;

import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.SystemState;
import sidechannel.choice.CostChoiceGenerator;
import sidechannel.cost.approximate.monitor.SingleRunPathMonitor;

/**
*
* @author Quoc-Sang Phan <sang.phan@sv.cmu.edu>
*
*/
public class SingleRunCost extends ApproximateCost<Long> {
	
	public SingleRunCost(){
		this.sideChannel = ApproximateCost.CUSTOMIZED;
	}
	
	public SingleRunCost(int sideChannel, String secureMethod){
		this.sideChannel = sideChannel;
		if(sideChannel != ApproximateCost.CUSTOMIZED){
			monitor = new SingleRunPathMonitor(sideChannel,secureMethod);
		}
	}
	
	@Override
	public Long getCurrentCost(SystemState ss) {
		long cost = -1;
		if(sideChannel == ApproximateCost.CUSTOMIZED){
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
			if (costs.size() > 1) {
				// something is wrong here,
				// because this listener is only for 1 run
				assert false;
			}
			cost = costs.get(0);
		} else {
			assert (monitor != null);
			cost = monitor.getCurrentCost();
		}
		return cost;
	}

}
