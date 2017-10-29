package sidechannel.multirun.adaptive.selfcomp;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import sidechannel.cost.abstraction.Singleton;
import sidechannel.multirun.adaptive.AbstractAdaptiveQuantifier;

/**
 *
 * @author Quoc-Sang Phan <sang.phan@sv.cmu.edu>
 *
 */
public class FullAdaptiveQuantifier extends AbstractAdaptiveQuantifier  {

	public static void start(Config config, String[] args) {
		conf = config;
		setupEnvironment();
		normalizeCosts();
		run();
	}
	
	public static void normalizeCosts(){
		String interval = conf.getProperty("cost.interval");
		if(interval == null){
			return;
		}
		conf.setProperty("target.args", "1");
		JPF jpf = new JPF(conf);
		NormalizeCostListener listener = new NormalizeCostListener(conf);
		jpf.addListener(listener);
		try {
			jpf.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void run(){
		// set back number of run
		conf.setProperty("target.args", null);
		JPF jpf = new JPF(conf);
		FullAdaptiveListener listener = new FullAdaptiveListener(conf);
		jpf.addListener(listener);
		try {
			jpf.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
