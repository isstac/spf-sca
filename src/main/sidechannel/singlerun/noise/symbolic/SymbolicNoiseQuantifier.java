package sidechannel.singlerun.noise.symbolic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.antlr.runtime.RecognitionException;

import com.google.common.collect.Sets;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.symbc.numeric.PathCondition;
import gov.nasa.jpf.vm.SystemState;
import modelcounting.domain.Problem;
import modelcounting.domain.ProblemSetting;
import sidechannel.ConcreteSideChannelListener;
import sidechannel.cost.abstraction.IntervalAbstraction;
import sidechannel.cost.approximate.SingleRunCost;
import sidechannel.entropy.AbstractModelCounter;
import sidechannel.entropy.EntropyCalculator;
import sidechannel.entropy.ReliabilityModelCounter;
import sidechannel.util.BarvinokUtils;
import sidechannel.util.Environment;
import sidechannel.util.ModelCounter;
import sidechannel.util.Pair;
import sidechannel.util.PathConditionUtils;

/**
*
* @author Quoc-Sang Phan <sang.phan@sv.cmu.edu>
*
*/
public class SymbolicNoiseQuantifier extends ConcreteSideChannelListener<Long>  {
	
	private double entropyO = 0;
	private double entropyNoise = 0;
	private double entropyH = 0;
	private double domainH = 0;
	private double domain = 0;
	private ArrayList<String> high = new ArrayList<String>();
	private ArrayList<String> lowAndNoise = new ArrayList<String>();
	private boolean sampling = false;
	HashSet<String> pcs;
	Problem p = null;
	
	public SymbolicNoiseQuantifier(Config config) {
		super(config);
		costModel = new SingleRunCost(sideChannel,null);
		mode = LATTE_MODE;
		setSamplingMode();
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
		computeEntropyOfObservable();
		computeEntropyNoise();
		entropyH = Math.log(domainH) / Math.log(2);
		if(sampling){
			double newDomainH = reviseDomain();
			entropyH *= newDomainH / domainH;
		}
		System.out.println("\n\n>>>>> Entropy of observable is : " + entropyO);
		System.out.println(">>>>> Entropy of secret is : " + entropyH);
		System.out.println(">>>>> Join Entropy of O and h is : " + entropyNoise);
		System.out.println(">>>>> Entropy of noise is : " + (entropyNoise - entropyH) + " bits");
		System.out.println(">>>>> Leakage is " + (entropyO - entropyNoise + entropyH) + " bits\n");
	}
	
	private double reviseDomain(){
		if (p == null) {
			p = divideSetOfVariablesFromDomain();
		}
		Problem d = null;
		String tmpDir = conf.getProperty("symbolic.reliability.tmpDir");
		String target = conf.getProperty("target");
		String upFile = tmpDir + "/projected." + target + ".up";
		String content = ModelCounter.createProjectedUserProfileString(conf, collector);
		Environment.write(upFile, content);
		try {
			ProblemSetting problemSettings = ProblemSetting.loadFromFile(upFile);
			d = problemSettings.getDomain().asProblem();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		String relation = "C";
		String prefix = BarvinokUtils.createCountingPrefix(relation, high);
		StringBuilder sb = new StringBuilder();
		for(String pc : pcs){
			Problem spfProblem = BarvinokUtils.pathConditionToProblem(pc);
			if(sb.length() == 0){
				sb.append(BarvinokUtils.toBarvinok(spfProblem.addProblem(d)));
			} else{
				sb.append(" or " + BarvinokUtils.toBarvinok(spfProblem.addProblem(d)));
			}
		}
		String barvinokInput = BarvinokUtils.createCountingQuery(relation, prefix, sb.toString());
		
		// System.out.println("\n\n=====Barvinok input for revise domain is\n\n" + sb.toString() + "\n\n=====");
		
		String line = BarvinokUtils.executeBarvinok(conf, barvinokInput);
		String[] tokens = line.split(" ");
		double newDomainH = Double.parseDouble(tokens[1]);
		System.out.println(">>>>> Revise domain is " + newDomainH);
		return newDomainH;
	}
	
	private void computeEntropyOfObservable() {
		EntropyCalculator<Long, String> cal = null;
		cal = new EntropyCalculator<Long, String>(conf, collector);
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
		if (p == null) 
			p = divideSetOfVariablesFromDomain();		
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
			for(Pair elem : pairs){
				entropyNoise -= elem.domain * (elem.size / domain) * (Math.log(elem.size/domain) / Math.log(2));
			}
		}
	}
	
	@Override
	protected void processPC(PathCondition pc, SystemState ss ){
		if(sampling){
			pcs.add(PathConditionUtils.project(
					PathConditionUtils.cleanExpr(pc.header.toString())
			));
		}
		super.processPC(pc, ss);
	}
	
	public void setSamplingMode(){
		sampling = true;
		pcs = new HashSet<String>();
	}
}
