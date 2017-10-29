package sidechannel.singlerun;

import java.util.HashSet;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.symbc.numeric.PathCondition;
import gov.nasa.jpf.vm.SystemState;
import sidechannel.ConcreteSideChannelListener;
import sidechannel.cost.abstraction.Singleton;
import sidechannel.cost.approximate.ApproximateCost;
import sidechannel.cost.approximate.SingleRunCost;

/**
 *
 * @author Quoc-Sang Phan <sang.phan@sv.cmu.edu>
 *
 */
public class SingleRunListener extends ConcreteSideChannelListener<Long> {
		
	protected boolean done = false;
	protected Singleton abstraction = null;
	
	public SingleRunListener(Config config){
		super(config);
		mode = SMT_LIB2_MODE;
		
		if(sideChannel != ApproximateCost.CUSTOMIZED){
			String secureMethod = conf.getProperty("sidechannel.secure_method");
			costModel = new SingleRunCost(sideChannel, secureMethod);
		} else{
			costModel = new SingleRunCost();
		}
		
		String interval = conf.getProperty("cost.interval");
		if(interval != null){
			abstraction = Singleton.getInstance();
		}
	}

	@Override
	public void searchFinished(Search search) {

		System.out.println("\n>>>>> Before abstraction, there are " + numOfPCs 
				+ " path conditions and " + obsrv.size() + " observables \n");
		if(numOfPCs <= 1){
			// numOfPCs can't be 0
			// if it is 1, then we reach a fixed point
			done = true;
			return;
		}
		
		/*
		String interval = conf.getProperty("cost.interval");
		if(interval != null){
			int num = Integer.parseInt(interval);
			IntervalAbstraction<String> abs = new IntervalAbstraction<String>();
			obsrv = abs.normalize(obsrv,num);
		}	
		//*/	
	}
	
	@Override
	protected void processPC(PathCondition pc, SystemState ss ){
		pc.solve();

		collector.collectVariables(pc);
		String currentPC = format(pc);
		numOfPCs++;
		Long currentCost = costModel.getCurrentCost(ss);
		
		// normalize the cost
		if(abstraction != null){
			currentCost = abstraction.normalize(currentCost);
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
	
	@Override
	protected void printCost(Long cost) {
		System.out.println("Cost is " + cost);
	}
	
}
