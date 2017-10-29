package sidechannel.singlerun.noise.symbolic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.search.Search;
import modelcounting.domain.Problem;
import sidechannel.ConcreteSideChannelListener;
import sidechannel.cost.abstraction.IntervalAbstraction;
import sidechannel.cost.approximate.SingleRunCost;
import sidechannel.entropy.AbstractModelCounter;
import sidechannel.entropy.EntropyCalculator;
import sidechannel.entropy.ReliabilityModelCounter;
import sidechannel.util.BarvinokUtils;
import sidechannel.util.Pair;

/**
*
* @author Quoc-Sang Phan <sang.phan@sv.cmu.edu>
*
*/
public class MinEntropyQuantifier extends ConcreteSideChannelListener<Long>  {
	
	private double entropyO = 0;
	// private double entropyNoise = 0;
	private double entropyH = 0;
	private double domainH = 0;
	private double domain = 0;

	private double maxProb = 0;
	private double leakage = 0;
	private ArrayList<String> high = new ArrayList<String>();
	private ArrayList<String> lowAndNoise = new ArrayList<String>();
	
	public MinEntropyQuantifier(Config config) {
		super(config);
		costModel = new SingleRunCost(sideChannel,null);
		mode = LATTE_MODE;
	}
	
	@Override
	public void searchFinished(Search search) {

		if(obsrv.size() == 0) {
			// there is no symbolic path, no leaks
			System.out.println("This program satisfies non-interference");
			return;
		} else{
			System.out.println(">>>>> There are " + numOfPCs 
					+ " paths and " + obsrv.size() + " observables");
		}
		
		String interval = conf.getProperty("cost.interval");
		if(interval != null){
			int num = Integer.parseInt(interval);
			IntervalAbstraction<String> abs = new IntervalAbstraction<String>();
			obsrv = abs.normalize(obsrv,num);
		}
		
		System.out.println(">>>>> Channel capacity is : " + Math.log(obsrv.size())/Math.log(2));
		computeEntropyOfObservable();
		computeEntropyNoise();
		entropyH = Math.log(domainH) / Math.log(2);
		// System.out.println("\n\n>>>>> Entropy of observable is : " + entropyO);
		System.out.println(">>>>> Entropy of secret is : " + entropyH);
		System.out.println(">>>>> Min-entropy leakage is " + leakage + " bits\n");
	}
	
	private void computeEntropyOfObservable() {
		EntropyCalculator<Long, String> cal = new EntropyCalculator<Long, String>(conf, collector);
		AbstractModelCounter<Long, String> mc = new ReliabilityModelCounter<Long>(conf, collector);
		mc.countAll(obsrv, cal);
		domainH = cal.getDomainH();
		domain = cal.getDomain();
		entropyO = cal.getLeakage();
	}
	
	private Problem divideSetOfVariablesFromDomain(){
		Set<Problem> domain = Sets.newHashSet(BarvinokUtils.getDomain(conf).asProblem());
		assert(domain.size() == 1);
		Problem p = domain.iterator().next();
		List<String> vars = p.getVarList().asList();
		for(String var : vars){
			if(var.charAt(0) == 'h'){
				// high variables start with 'h'
				high.add(var);
			} else{
				lowAndNoise.add(var);
			}
		}
		return p;
	}
	
	private void computeEntropyNoise(){
		Problem p = divideSetOfVariablesFromDomain();		
		String relation = "C";
		String prefix = BarvinokUtils.createProjectionPrefix(relation, high, lowAndNoise);
		
		Iterator<Map.Entry<Long, HashSet<String>>> it = obsrv.entrySet()
				.iterator();
		while (it.hasNext()) {
			Map.Entry<Long, HashSet<String>> pair = (Map.Entry<Long, HashSet<String>>) it
					.next();
			//TODO: change this
			StringBuilder sb = new StringBuilder();
			for(String path : pair.getValue()){
				if(DEBUG){
					System.out.println("\n\n>>>>> Original path: \n" + path);	
				}
				Problem spfProblem = BarvinokUtils.pathConditionToProblem(path);
				if(sb.length() == 0){
					sb.append(BarvinokUtils.toBarvinok(spfProblem.addProblem(p)));
				} else{
					sb.append(" or " + BarvinokUtils.toBarvinok(spfProblem.addProblem(p)));
				}
			}
			String barvinokInput = BarvinokUtils.createCountingQuery(relation, prefix, sb.toString());
			String line = BarvinokUtils.executeBarvinok(conf, barvinokInput);
			// parse(line);
			if(DEBUG){
				System.out.println("\n>>>>> After parse:\n" + barvinokInput);
				System.out.println("\n>>>>> Projection is " + line);
				System.out.println("===============================================");
			}
			ArrayList<Pair> pairs = BarvinokUtils.parseProjection(conf, high, line);
			double max = 0;
			System.out.println("\n\n");
			for(Pair elem : pairs){
				double tmp = elem.size / domain;
				if(max < tmp) max = tmp;
			}
			maxProb += max;
		}
		leakage = Math.log(maxProb * domainH)/Math.log(2);
	}
}
