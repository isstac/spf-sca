package sidechannel.multirun.adaptive.minimax;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.search.Search;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

import modelcounting.domain.Problem;
import sidechannel.multirun.assumption.ReliabilityAssumption;
import sidechannel.singlerun.SingleRunListener;
import sidechannel.util.ModelCounter;

import com.google.common.cache.LoadingCache;

/**
 *
 * @author Quoc-Sang Phan <sang.phan@sv.cmu.edu>
 *
 */
public class DefenderListener extends SingleRunListener {
		
	public DefenderListener(Config config) {
		super(config);
		mode = LATTE_MODE;
	}
	
	public void searchFinished(Search search) {

		super.searchFinished(search);
		if(done){
			return;
		}
		defenderSelectsOutput();
	}
	
	private void defenderSelectsOutput(){		
		// select only the output with largest partition
		long maxPartition = 0;
		String maxConstraint = null;
		ModelCounter counter = new ModelCounter(conf,collector);
		for (Long cost : obsrv.keySet()) {
			HashSet<String> setOfPC = obsrv.get(cost);
			
			// TODO: count for each PC			
			// long partition = counter.count(setOfPC);
			long partition = 0;
			System.out.println("\n\n\n>>>>> The cost is " + cost);
			StringBuilder sb = new StringBuilder();
			for(String pc : setOfPC){
				long count = counter.countSinglePathResetCache(pc).longValue();
				partition += count;
				if(count > 0){
					LoadingCache<Problem, Set<Problem>> omegaCache = counter.getOmegaCache();
					sb.append(ReliabilityAssumption.buildConstraint(omegaCache) + "\n");
					if(DEBUG){
						System.out.println("\n\n" + pc);
						ReliabilityAssumption.printOmegaCache(omegaCache);
					}
				}
			}
						
			
			System.out.println(">>>>> The number of model is: "
					+ partition);
			if(partition <= maxPartition){
				// TODO: should we ignore if 
				// the new partition is just equal to max?
				continue;
			}
			maxPartition = partition;
			maxConstraint = sb.toString();
		}
		System.out.println("\n==== Constraints to add to the next run ====");
		System.out.println(maxConstraint);
		System.out.println("\n============================================");
		writeConstraintsToFile(maxConstraint);
	}
	
	private void writeConstraintsToFile(String str){
		String target_args = conf.getProperty("target.args");
		int pos = target_args.indexOf(',');
		int run = Integer.parseInt(target_args.substring(0, pos));
		String fileName = "build/tmp/constraints" + run + ".txt";
		Writer writer = null;

		try {
			writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(fileName), "utf-8"));
			writer.write(str);
		} catch (IOException ex) {
			// report
		} finally {
			try {
				writer.close();
			} catch (Exception ex) {
			}
		}
	}
}
