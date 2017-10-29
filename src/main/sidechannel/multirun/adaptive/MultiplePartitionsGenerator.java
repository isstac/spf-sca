package sidechannel.multirun.adaptive;

import gov.nasa.jpf.Config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import maximal.satisfiable.subsets.marco.Marco;
import sidechannel.common.Common;
import sidechannel.util.ConfigUtils;
import sidechannel.util.SymbolicVariableCollector;
import sidechannel.util.smt.BitVectorUtils;
import sidechannel.util.smt.LinearIntegerUtils;
import sidechannel.util.smt.SmtLib2Utils;

import com.microsoft.z3.BitVecNum;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.IntNum;
import com.microsoft.z3.Model;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Status;

/**
 * Enumerate a set of partitions as Maximal Satisfiable Subsets
 * This class does not generate all partitions, and thus
 * the attack tree created from it is not complete.
 *
 * @author Quoc-Sang Phan <sang.phan@sv.cmu.edu>
 *
 */
public class MultiplePartitionsGenerator<Cost> extends PartitionGenerator<Cost> {

	protected Context ctx = null;
	protected Solver solver = null;
	protected boolean DEBUG = false;
	protected int inputSize = 1;
	protected Expr[] lowInputExpr;
	protected boolean LINEAR = false;
	protected BoolExpr hardConstraints;
	protected BoolExpr[] C;
	protected boolean oneShot = true;

	public MultiplePartitionsGenerator(Config config, AbstractPartitionVisitor<Cost> visitor, boolean linear) {
		super(config, visitor, linear);
		HashMap<String, String> cfg = new HashMap<String, String>();
		cfg.put("model", "true");
		ctx = new Context(cfg);
		solver = ctx.mkSolver();
		
		inputSize = ConfigUtils.getLowInputSize(config);
		lowInputExpr = new Expr[inputSize];
	}
	
	public void setVerbose(){
		DEBUG = true;
	}

	protected void getLowInputExpr(SymbolicVariableCollector collector) {
		int count = 0;
		for (String var : collector.getListOfVariables()) {
			if (var.charAt(0) == 'l') {
				lowInputExpr[Common.indexOf(var)] = LINEAR ? ctx.mkIntConst(var)
						                                   : ctx.mkBVConst(var, BitVectorUtils.bitLength);
				++count;
			}
		}
		if(DEBUG){
			System.out.println(">>>>> Low input size is " + inputSize);
			System.out.println(">>>>> Number of variables created is " + count);
		}
		assert (count == inputSize);
	}

	protected void getDisjunctions() {
		int size = map.size();
		C = new BoolExpr[size];
		for (int i = 0; i < size; i++) {
			C[i] = ctx.mkBoolConst(SmtLib2Utils.OBSERVABLE_PREFIX + (i + 1));
		}
	}
	
	protected String generateSMTLIB2String(SmtLib2Utils<Cost> utils, HashMap<Cost, HashSet<String>> obsrv){
		return utils.groupPathConditionsWithTheSameCost(obsrv).toString();
	}
	
	protected void init(HashMap<Cost, HashSet<String>> obsrv, SymbolicVariableCollector collector){
		SmtLib2Utils<Cost> utils;
		utils = LINEAR? new LinearIntegerUtils<Cost>(conf, collector)
				      : new BitVectorUtils<Cost>(conf, collector);
		String smtlib2string = generateSMTLIB2String(utils,obsrv);
		if(DEBUG) {
			System.out.println(smtlib2string);
		}
		map = utils.getPathConditionsToCost();
		
		hardConstraints = ctx.parseSMTLIB2String(smtlib2string, null, null, null, null);
		// TODO: parse low inputs
		getLowInputExpr(collector);
		
		// TODO: parse high constraint
		getDisjunctions();
		
		oneShot = false;
	}

	@Override
	public void computePartitions(HashMap<Cost, HashSet<String>> obsrv, SymbolicVariableCollector collector){
		assert(oneShot);
		init(obsrv,collector);
		solver.add(hardConstraints);
		Marco marco = new Marco(ctx,hardConstraints);
		computePartitions(marco);
//		ctx.close();
	}
	
	protected void computePartitions(Marco marco){
		ArrayList<Set<Integer>>allMSS = marco.run(C);
		for(Set<Integer> MSS : allMSS){
			solver.push();
			MSS.forEach(i -> solver.add(C[i]));
			if(solver.check() != Status.SATISFIABLE){
				// this means Marco is implemented incorrectly
				assert false;
			}
			else{
				Expr[] values = new Expr[inputSize];
				HashSet<Cost> allTheCosts = new HashSet<Cost>();
				Model m = solver.getModel();
				Integer[] lowInput = getLowInput(m, values);
				MSS.forEach(i ->
					allTheCosts.add(map.get(SmtLib2Utils.OBSERVABLE_PREFIX + (i + 1)))
				);
				// do something with the partition, abstractly
				visitor.visit(lowInput, allTheCosts);
			}
			solver.pop();
		}
	}
	
	/* 
	 * Get the value of low input
	 */
	protected Integer[] getLowInput(Model m, Expr[] values){
		Integer[] lowInput = new Integer[inputSize];
		for(int i = 0; i < lowInputExpr.length; ++i){
			values[i] = m.evaluate(lowInputExpr[i], false);
			lowInput[i] = LINEAR? ((IntNum)values[i]).getInt()
					            : ((BitVecNum)values[i]).getBigInteger().intValueExact();
			if(DEBUG){
				System.out.println(lowInputExpr[i].toString() + " is " + values[i].toString());
			}
		}
		return lowInput;
	}
}
