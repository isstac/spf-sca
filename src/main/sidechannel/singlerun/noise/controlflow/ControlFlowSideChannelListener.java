package sidechannel.singlerun.noise.controlflow;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.search.Search;
import sidechannel.cost.approximate.SingleRunCost;
import sidechannel.singlerun.SingleRunListener;
import sidechannel.util.ModelCounter;
import sidechannel.util.PathConditionUtils;

/**
*
* @author Quoc-Sang Phan <sang.phan@sv.cmu.edu>
*
*/
public class ControlFlowSideChannelListener extends SingleRunListener {

	private final int LATTE = 0;
	private final int BARVINOK = 1;
	
	private int modelCounter = LATTE;
	
	private double domain = 1;
	
	public ControlFlowSideChannelListener(Config config) {
		super(config);
		String secureMethod = conf.getProperty("sidechannel.secure_method");
		costModel = new SingleRunCost(sideChannel, secureMethod);
		if(conf.getProperty("symbolic.counter","latte").trim().equals("barvinok")){
			modelCounter = BARVINOK;
		}
		mode = LATTE_MODE;
	}

	@Override
	public void searchFinished(Search search) {

		super.searchFinished(search);
		if(done){
			return;
		}
		
		computeDomains();
		
		double leakage = 0;
		
		// compute leakage
		Iterator<Map.Entry<Long, HashSet<String>>> it = obsrv.entrySet()
				.iterator();
		
		ModelCounter counter1 = new ModelCounter(conf,collector);
		ModelCounter counter2 = new ModelCounter(conf,collector.projectH());
						
		while (it.hasNext()) {
			Map.Entry<Long, HashSet<String>> pair = (Map.Entry<Long, HashSet<String>>) it
					.next();
			
			HashSet<String> paths = pair.getValue();
			// This listener is for Control Flow Side Channel, where program has only 1 path condition for each observable
			assert (paths.size() == 1);
			for (String pc : paths) {
		        String project = PathConditionUtils.project(pc);
		        BigInteger block;
				BigInteger projectH;
				
				if(modelCounter == BARVINOK){
					block = counter1.countSinglePathBarvinok(pc);
					projectH = counter2.countSinglePathBarvinok(project);
				} else{
					// model counter is Latte
					block = counter1.countSinglePath(pc);
					projectH = counter2.countSinglePath(project);
				}
				leakage -= (block.doubleValue() / domain) * (Math.log(projectH.doubleValue() /domain) / Math.log(2));
			} 
		}
		System.out.println("\n>>>>> Leakage is " + leakage + " bits\n\n");
	}
	
	private void computeDomains(){
		int MIN = Integer.parseInt(conf.getProperty("symbolic.min_int", String.valueOf(Integer.MIN_VALUE)));
		int MAX = Integer.parseInt(conf.getProperty("symbolic.max_int", String.valueOf(Integer.MAX_VALUE)));
		
		String strMinHigh = conf.getProperty("sidechannel.min_high");
		String strMaxHigh = conf.getProperty("sidechannel.max_high");
		
		int min_high = (strMinHigh == null) ? MIN : Integer.parseInt(strMinHigh);
		int max_high = (strMaxHigh == null) ? MAX : Integer.parseInt(strMaxHigh);
				
		// if each variable has domain D, then n variables has domain D^n
		// domain = Math.pow(MAX - MIN + 1, collector.getListOfVariables().size()); // domain of the input
		
		//TODO: read domain from the problem settings file
		for(String var : collector.getListOfVariables()){
			if(var.charAt(0) == 'h'){
				domain *= max_high - min_high + 1;
			} else{
				domain *= MAX - MIN + 1;
			}
		}
	}
}
