package sidechannel.multirun.adaptive.minimax;

import java.io.File;

import org.apache.commons.io.FileUtils;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.JPFListener;
import sidechannel.multirun.adaptive.AbstractAdaptiveQuantifier;

/**
 *
 * @author Quoc-Sang Phan <sang.phan@sv.cmu.edu>
 *
 */
public class AdaptiveAttackQuantifier extends AbstractAdaptiveQuantifier{

	public static void start(Config config, String[] args) {

		conf = config;

		setupEnvironment();

		adaptiveAttack();

	}

	private static void adaptiveAttack() {

		for (int i = 1; i <= numOfRuns; i++) {
			// set up parameters
			conf.setProperty("sidechannel.smt2", "build/tmp/maxSMT.run" + i
					+ ".smt2");
			conf.setProperty("target.args", i + ",attack");
			// attack and defense
			JPF jpf = new JPF(conf);
			AdversaryListener attackListener = new AdversaryListener(conf);
			jpf.addListener(attackListener);
			try {
				System.out.println("============================ START RUN " + i + ""
						+ " ============================");
				jpf.run();
				if(attackListener.reachedFixedPoint()){
					return;
				}
				jpf.removeListener(attackListener);
				conf.setProperty("target.args", i + ",defend");
				jpf = new JPF(conf);
				JPFListener defendListener = new DefenderListener(conf);
				// JPFListener defendListener = new DefenderListenerWithMathematica(conf, jpf);
				jpf.addListener(defendListener);
				jpf.run();
				FileUtils.cleanDirectory(new File(conf
						.getProperty("symbolic.reliability.tmpDir")));
				System.out.println("============================= END RUN " + i + ""
						+ " =============================");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
