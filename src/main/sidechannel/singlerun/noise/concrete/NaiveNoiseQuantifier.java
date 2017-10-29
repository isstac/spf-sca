package sidechannel.singlerun.noise.concrete;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import sidechannel.CompositeListener;
import sidechannel.singlerun.SideChannelQuantifier;
import sidechannel.singlerun.noise.ProfileSettingsBuilder;

/**
*
* @author Quoc-Sang Phan <sang.phan@sv.cmu.edu>
*
*/
public class NaiveNoiseQuantifier {
	
	static Config conf;
	// static double domain = 0;
	static double domainH = 0;

	public static void start(Config config, String[] args) {
		conf = config;
		quantify();
	}
	
	public static void quantify(){

		double entropy = getEntropyObs();
		//*/
		int MIN = Integer.parseInt(conf.getProperty("symbolic.min_int", String.valueOf(Integer.MIN_VALUE)));
		int MAX = Integer.parseInt(conf.getProperty("symbolic.max_int", String.valueOf(Integer.MAX_VALUE)));
		
		String strMinHigh = conf.getProperty("sidechannel.min_high");
		String strMaxHigh = conf.getProperty("sidechannel.max_high");
		
		int min_high = (strMinHigh == null) ? MIN : Integer.parseInt(strMinHigh);
		int max_high = (strMaxHigh == null) ? MAX : Integer.parseInt(strMaxHigh);
		
		double entropyNoise = 0;
		for(int i = min_high; i <= max_high; ++i){
			System.out.println("=================== H = " + i + " ===================");
			entropyNoise += getEntropyNoise(i);
		}
		
		System.out.println("\n\n>>>>> Entropy of observable is " + entropy + " bits");
		System.out.println(">>>>> Entropy of noise is " + (entropyNoise / domainH) + " bits");
		
		entropy = entropy - (entropyNoise / domainH);
		
		System.out.println(">>>>> The leakage is " + entropy + " bits");
		//*/
		// System.out.println(">>>>> The leakage 2 is " + getEntropyNoise(2) + " bits");
	}
	
	public static double getEntropyObs(){
		double entropy = 0;
		conf.setProperty("target.args", null);
		JPF jpf = new JPF(conf); 
		
		ProfileSettingsBuilder psb = new ProfileSettingsBuilder(conf);
		SideChannelQuantifier scq = new SideChannelQuantifier(conf);
		// the order is important: first build profile settings, then quantify leakage
		CompositeListener listener = new CompositeListener(psb, scq);	
		jpf.addListener(listener);
		
		try {
			jpf.run();
			entropy = scq.getLeakage();
			domainH = scq.getDomainH();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return entropy;
	}
	
	public static double getEntropyNoise(int i) {
		double entropy = 0;
		SideChannelQuantifier listener = null;
		conf.setProperty("target.args", Integer.toString(i));
		JPF jpf = new JPF(conf);
		listener = new SideChannelQuantifier(conf);
		jpf.addListener(listener);
		try {
			jpf.run();
			entropy = listener.getLeakage();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return entropy;
	}
}
