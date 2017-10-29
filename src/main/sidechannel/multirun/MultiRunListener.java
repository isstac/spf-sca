package sidechannel.multirun;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.search.Search;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import sidechannel.ConcreteSideChannelListener;
import sidechannel.cost.approximate.ApproximateCost;
import sidechannel.cost.approximate.MultipleRunCost;
import sidechannel.util.Environment;
import sidechannel.util.smt.BitVectorUtils;
import sidechannel.util.smt.LinearIntegerUtils;
import sidechannel.util.smt.SmtLib2Utils;
import sidechannel.util.smt.Z3MaxSmtExecutor;

/**
 * A listener for non-adaptive multiple-run timing channel attack
 *
 * @author Quoc-Sang Phan <sang.phan@sv.cmu.edu>
 *
 */
public class MultiRunListener extends ConcreteSideChannelListener<ArrayList<Long>>  {
	
	protected boolean greedy = false;
	
	private long startTime;

	public MultiRunListener(Config config){
		super(config);
		startTime = System.nanoTime();
		
		// set bit length if using bit vector
		String bit_length = conf.getProperty("bit_length");
		if(bit_length != null){
			BitVectorUtils.bitLength = Integer.parseInt(bit_length);
		}		
		
		mode = SMT_LIB2_MODE;
		
		String secureMethod = null;
		
		if(sideChannel != ApproximateCost.CUSTOMIZED){
			secureMethod = conf.getProperty("sidechannel.secure_method");
		}

		costModel = new MultipleRunCost(sideChannel, secureMethod);
		
		greedy = conf.getProperty("greedy","false").equals("true");		
	}
	
	public void searchFinished(Search search) {
		String theory = conf.getProperty("SMT.theory","bitvector");
		SmtLib2Utils<ArrayList<Long>> utils = null;
		boolean linear = theory.equals("linear");
		if(linear){
			utils = new LinearIntegerUtils<ArrayList<Long>>(conf, collector);
		}else{
			utils = new BitVectorUtils<ArrayList<Long>>(conf, collector);
		}
		utils.generateMaxSmt(obsrv, true, greedy, false);
		
		if (DEBUG) {
			printCosts();
		}
		
		System.out.println(">>>>> There are " + numOfPCs 
				+ " path conditions and " + obsrv.size() + " observables");
		
        System.out.println(">>>>> SPF time is " + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime) + " ms");
		
		if(!greedy){
			// find z3
			String path = Environment.find("z3");
			if (path == null){
				return;
			}
			conf.setProperty("z3", path);
			
			long t1 = System.nanoTime();
			
			Z3MaxSmtExecutor<?> z3 = new Z3MaxSmtExecutor<>(conf, linear, false);
			z3.run(true, false, false, null);
			
	        long t = System.nanoTime() - t1;
	        System.out.println(">>>>> Z3 time is " + TimeUnit.NANOSECONDS.toMillis(t) + " ms");
		}
	}
	
	@Override
	protected void printCost(ArrayList<Long> cost){
		System.out.print(">>>>> Sequence of cost is <");
		int j;
		for(j = 0; j < cost.size() - 1; j++){
			System.out.print(cost.get(j) + ",");
		}
		System.out.print(cost.get(j));
		System.out.println(">");
	}
}
