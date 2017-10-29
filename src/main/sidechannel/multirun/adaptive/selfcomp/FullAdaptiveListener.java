package sidechannel.multirun.adaptive.selfcomp;

import java.util.ArrayList;
import java.util.HashSet;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.symbc.numeric.PathCondition;
import gov.nasa.jpf.vm.SystemState;
import sidechannel.cost.abstraction.Singleton;
import sidechannel.multirun.MultiRunListener;

/**
 *
 * @author Quoc-Sang Phan <sang.phan@sv.cmu.edu>
 *
 */
public class FullAdaptiveListener extends MultiRunListener {

	protected Singleton abstraction = null;
	
	public FullAdaptiveListener(Config config) {
		super(config);
		String interval = conf.getProperty("cost.interval");
		if(interval != null){
			abstraction = Singleton.getInstance();
		}
	}
	
	@Override
	protected void processPC(PathCondition pc, SystemState ss ){
		pc.solve();

		collector.collectVariables(pc);
		String currentPC = format(pc);
		numOfPCs++;
		ArrayList<Long> currentCost = costModel.getCurrentCost(ss);
		
		// normalize the cost
		if(abstraction != null){
			currentCost = normalize(currentCost);
		}
		
		HashSet<String> data = obsrv.get(currentCost);
		if (data == null) {
			data = new HashSet<String>();
			data.add(currentPC);
			obsrv.put(currentCost, data);
			// reset current cost
		}else{
			data.add(currentPC);
		}
	}
	
	protected ArrayList<Long> normalize(ArrayList<Long> currentCost){
		ArrayList<Long> result = new ArrayList<Long>();
		for(Long cost : currentCost){
			result.add(abstraction.normalize(cost));
		}
		return result;
	}

}
