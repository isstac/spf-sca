package sidechannel.singlerun;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.search.Search;
import sidechannel.ConcreteSideChannelListener;
import sidechannel.cost.abstraction.IntervalAbstraction;
import sidechannel.cost.approximate.SingleRunCost;
import sidechannel.entropy.AbstractModelCounter;
import sidechannel.entropy.EntropyCalculator;
import sidechannel.entropy.ReliabilityModelCounter;

/**
 * Listener for quantify side-channel leaks
 *
 * @author Quoc-Sang Phan <sang.phan@sv.cmu.edu>
 *
 */
public class SideChannelQuantifier extends ConcreteSideChannelListener<Long> {

	private boolean entropy = false;
	private double leakage = 0;
	private double domainH = 0;

	public SideChannelQuantifier(Config config) {
		super(config);
		init();
	}
	
	private void init(){
		mode = LATTE_MODE;
		entropy =  conf.getProperty("sidechannel.entropy","true").trim().equals("true");
		String secureMethod = conf.getProperty("sidechannel.secure_method");
		costModel = new SingleRunCost(sideChannel,secureMethod);
	}

	@Override
	public void searchFinished(Search search) {

		if(obsrv.size() == 0) {
			// there is no symbolic path, no leaks
			System.out.println("This program satisfies non-interference");
			return;
		}
		
		if(DEBUG){
			printCosts();
		}
		
		String interval = conf.getProperty("cost.interval");
		if(interval != null){
			int num = Integer.parseInt(interval);
			IntervalAbstraction<String> abs = new IntervalAbstraction<String>();
			obsrv = abs.normalize(obsrv,num);
		}

		System.out.println("Cardinality of the set : " + obsrv.size());
		if(entropy){
			EntropyCalculator<Long,String> cal = new EntropyCalculator<Long,String>(conf,collector);
			AbstractModelCounter<Long,String> mc = new ReliabilityModelCounter<Long>(conf,collector);
			domainH = cal.getDomainH();
			mc.countAll(obsrv, cal);
			leakage = cal.getLeakage();
			System.out.println("Shannon leakage is : " + leakage);
		}
		System.out.println("Channel capacity is : " + Math.log(obsrv.size()) / Math.log(2));
	}
	
	public double getLeakage(){
		return leakage;
	}
	
	public double getDomainH(){
		return domainH;
	}
	
	/*
	@Override
	protected void printCost(Long cost){
		System.out.println(">>>>> Cost is " + cost);
	}
	//*/
	
	//*
	@Override
	protected void printCosts(){
		obsrv.forEach((K,V) ->{
			System.out.println(">>>>> Cost is " + K);
		});
	}
	//*/
}
