package sidechannel.multirun.adaptive.minimax;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.search.Search;
import sidechannel.singlerun.SingleRunListener;
import sidechannel.util.smt.BitVectorUtils;
import sidechannel.util.smt.LinearIntegerUtils;
import sidechannel.util.smt.SmtLib2Utils;
import sidechannel.util.smt.Z3MaxSmtExecutor;

/**
 *
 * @author Quoc-Sang Phan <sang.phan@sv.cmu.edu>
 *
 */
public class AdversaryListener extends SingleRunListener  {
		
	private final boolean LINEAR = false;
	
	private boolean fixedPoint = false;
	
	public AdversaryListener(Config config) {
		super(config);
		mode = SMT_LIB2_MODE;
	}
	
	public boolean reachedFixedPoint(){
		return fixedPoint;
	}
	
	public void searchFinished(Search search) {

		super.searchFinished(search);
		if(done){
			return;
		}
		adverserySelectsLowInput();
	}
	
	/*
	 * The adversary selects a low input using Max-SMT
	 * and returns a set of outputs wrt this low input
	 */
	private void adverserySelectsLowInput() {
		SmtLib2Utils<Long> utils = null;
		if (LINEAR) {
			utils = new LinearIntegerUtils<Long>(conf, collector);
		} else {
			utils = new BitVectorUtils<Long>(conf, collector);
		}

		utils.generateMaxSmt(obsrv, false, true, false);
		StringBuilder sbLowInputs = new StringBuilder();
		Z3MaxSmtExecutor<?> z3 = new Z3MaxSmtExecutor<Long>(conf, LINEAR, false);
		z3.run(false, true, false, null);
		Map<String, Integer> model = z3.getModel();
		Iterator<Map.Entry<String, Integer>> it = model.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, Integer> pair = (Map.Entry<String, Integer>) it.next();
			sbLowInputs.append(pair.getKey() + ":" + pair.getValue() + "\n");
		}
		sbLowInputs.delete(sbLowInputs.length() - 1, sbLowInputs.length());
		// output z3 result
		// System.out.println("\n>>> End Z3 output\n");
		String args = conf.getProperty("target.args");
		int i = Integer.parseInt(args.split(",")[0]);
		String fileName = "build/tmp/input" + i + ".txt";
		Writer writer = null;

		try {
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), "utf-8"));
			writer.write(sbLowInputs.toString());
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
