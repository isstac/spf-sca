package sidechannel.multirun.adaptive;

import gov.nasa.jpf.Config;

import java.io.File;

import org.apache.commons.io.FileUtils;

import sidechannel.util.Environment;
import sidechannel.util.smt.BitVectorUtils;

/**
 *
 * @author Quoc-Sang Phan <sang.phan@sv.cmu.edu>
 *
 */
public class AbstractAdaptiveQuantifier {
	
	protected static Config conf;
	public static int numOfRuns;

	protected static void setupEnvironment() {
		numOfRuns = Integer.parseInt(conf.getProperty("multirun.num_run","1"));
		// clean tmp file
		try {
			File dir1 = new File(conf.getProperty("sidechannel.tmpDir","build/tmp"));
			if (!dir1.exists()) {
				dir1.mkdirs();
			} else{
				FileUtils
				.cleanDirectory(dir1);
			}
			String tmpDir2 = conf.getProperty("symbolic.reliability.tmpDir","build/tmp/mc");
			assert (tmpDir2 != null);
			File dir2 = new File(tmpDir2);
			if (!dir2.exists()) {
				dir2.mkdirs();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// find z3
		String z3 = Environment.find("z3");
		if (z3 == null) {
			return;
		}
		conf.setProperty("z3", z3);

		String bit_length = conf.getProperty("bit_length");
		if (bit_length != null) {
			BitVectorUtils.bitLength = Integer.parseInt(bit_length);
		}
		
		/*
		// hack: minimize bit length to reduce the formula
		int maxInt = Integer.parseInt(conf.getProperty("symbolic.max_int"));
		BitVectorUtils.bitLength = (int) (Math.log(maxInt) / Math.log(2)) + 10;
		//*/
	}
}
