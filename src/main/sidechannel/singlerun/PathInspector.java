package sidechannel.singlerun;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.search.Search;
import sidechannel.util.Environment;
import sidechannel.util.smt.BitVectorUtils;
import sidechannel.util.smt.LinearIntegerUtils;
import sidechannel.util.smt.SmtLib2Utils;
import sidechannel.util.smt.Z3MaxSmtExecutor;

/**
 *
 * @author Quoc-Sang Phan <sang.phan@sv.cmu.edu>
 *
 */
public class PathInspector extends SingleRunListener {

	public PathInspector(Config config) {
		super(config);
	}

	public void searchFinished(Search search) {
		
		super.searchFinished(search);
		if(done){
			return;
		}
		
		String theory = conf.getProperty("SMT.theory","bitvector");
		SmtLib2Utils<Long> utils = null;
		boolean linear = theory.equals("linear");
		if(linear){
			utils = new LinearIntegerUtils<Long>(conf, collector);
		}else{
			utils = new BitVectorUtils<Long>(conf, collector);
		}
		
		if(false){
			System.out.println(utils.groupPathConditionsUsingQuantifiers(obsrv).toString());
			return;
		}
		
		utils.generateMaxSmt(obsrv, true, true, true);
		// Map<String,Long> map = utils.getPathConditionsToCost();
		
		if (DEBUG) {
			printCosts();
		}
		
		System.out.println(">>>>> There are " + numOfPCs 
				+ " path conditions and " + obsrv.size() + " observables");
		String path = Environment.find("z3");
		if (path == null){
			return;
		}
		conf.setProperty("z3", path);
				
		Z3MaxSmtExecutor<Long> z3 = new Z3MaxSmtExecutor<Long>(conf, linear, false);
		z3.run(true, true, true, null);
		
	}
}
