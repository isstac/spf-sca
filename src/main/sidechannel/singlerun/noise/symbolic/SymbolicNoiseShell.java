package sidechannel.singlerun.noise.symbolic;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.JPFShell;
import sidechannel.CompositeListener;
import sidechannel.singlerun.noise.ProfileSettingsBuilder;

/**
 * @author Quoc-Sang Phan
 *
 */
public class SymbolicNoiseShell implements JPFShell {
	
	private Config conf;

	public SymbolicNoiseShell(Config config) {
		this.conf = config;
	}

	@Override
	public void start(String[] args) {
		JPF jpf = new JPF(conf);

		ProfileSettingsBuilder psb = new ProfileSettingsBuilder(conf);
		SymbolicNoiseQuantifier snq = new SymbolicNoiseQuantifier(conf);
		// the order is important: first build profile settings, 
		// then quantify leakage
		CompositeListener listener = new CompositeListener(psb, snq);
		jpf.addListener(listener);
		try {
			jpf.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
