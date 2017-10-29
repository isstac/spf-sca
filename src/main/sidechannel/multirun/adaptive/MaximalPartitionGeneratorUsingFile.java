package sidechannel.multirun.adaptive;

import gov.nasa.jpf.Config;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import sidechannel.util.SymbolicVariableCollector;
import sidechannel.util.smt.BitVectorUtils;
import sidechannel.util.smt.LinearIntegerUtils;
import sidechannel.util.smt.SmtLib2Utils;
import sidechannel.util.smt.Z3MaxSmtExecutor;

/**
 *
 * @author Quoc-Sang Phan <sang.phan@sv.cmu.edu>
 *
 */
public class MaximalPartitionGeneratorUsingFile<Cost> extends PartitionGenerator<Cost> {

	private Z3MaxSmtExecutor<Cost> z3;
	
	
	public MaximalPartitionGeneratorUsingFile(Config conf, AbstractPartitionVisitor<Cost> visitor, boolean linear) {
		super(conf,visitor,linear);
		z3 = new Z3MaxSmtExecutor<Cost>(conf,LINEAR,false);
	}
	
	@Override
	public void computePartitions(HashMap<Cost, HashSet<String>> obsrv, SymbolicVariableCollector collector){
		SmtLib2Utils<Cost> utils = null;
		if (LINEAR) {
			utils = new LinearIntegerUtils<Cost>(conf, collector);
		} else {
			utils = new BitVectorUtils<Cost>(conf, collector);
		}
		utils.generateMaxSmt(obsrv, false, true, true);
		map = utils.getPathConditionsToCost();
		z3.run(false, true, true, map);
		Integer[] lowInput = z3.getLowInput();
		Set<Cost> allTheCosts = z3.getCosts();
		visitor.visit(lowInput, allTheCosts);
	}
}
