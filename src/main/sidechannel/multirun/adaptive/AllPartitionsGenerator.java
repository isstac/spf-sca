package sidechannel.multirun.adaptive;

import gov.nasa.jpf.Config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import maximal.satisfiable.subsets.marco.Marco;
import sidechannel.common.Common;
import sidechannel.util.SymbolicVariableCollector;
import sidechannel.util.smt.BitVectorUtils;
import sidechannel.util.smt.SmtLib2Utils;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.BoolSort;
import com.microsoft.z3.Expr;
import com.microsoft.z3.FuncDecl;
import com.microsoft.z3.Model;
import com.microsoft.z3.Sort;
import com.microsoft.z3.Status;

/**
 * Enumerate all partitions as Maximal Satisfiable Subsets
 *
 * @author Quoc-Sang Phan <sang.phan@sv.cmu.edu>
 *
 */
public class AllPartitionsGenerator<Cost> extends MultiplePartitionsGenerator<Cost> {

	protected FuncDecl[] functions = null;
	protected HashMap<Integer, Cost> indexOfCost = new HashMap<Integer, Cost>();
	protected HashMap<Integer, String> indexOfParameters = new HashMap<Integer, String>();
	
	public AllPartitionsGenerator(Config config, AbstractPartitionVisitor<Cost> visitor, boolean linear) {
		super(config, visitor, linear);
	}

	@Override
	protected void getDisjunctions() {
		int size = map.size();
		C = new BoolExpr[size];
		functions = new FuncDecl[size];
		
		Iterator<Map.Entry<String,Cost>> iterator = map.entrySet().iterator();
		while(iterator.hasNext()){
			Map.Entry<String, Cost> pair = (Map.Entry<String, Cost>) iterator.next();
			Cost cost = pair.getValue();
			String functionAndParameters = pair.getKey();
			int pos = functionAndParameters.indexOf("l");
			String parameters = functionAndParameters.substring(pos, functionAndParameters.length() - 2);
			if(DEBUG){
				System.out.println(">>>>> Parameters is >>" + parameters + "<<");
				System.out.println(">>>>> Function with arguments is " + functionAndParameters);
			}
			// the form is: ( function param1 param2 ... paramN )
			String[] token = functionAndParameters.split(" ");
			
			String function = token[1];
			// index of OBS starts from 1
			int funcIndex = Integer.parseInt(function.substring(SmtLib2Utils.OBSERVABLE_PREFIX.length())) - 1;
			Sort argSort = LINEAR? ctx.mkIntSort() : ctx.mkBitVecSort(BitVectorUtils.bitLength);
			ArrayList<Sort> argSorts = new ArrayList<Sort>();
			ArrayList<Expr> args = new ArrayList<Expr>();
			BoolSort BS = ctx.getBoolSort();
			// parse all arguments
			if(DEBUG){
				System.out.println(">>>>> Function is " + token[1]);
			}
			for(int i = 2; i < token.length - 1; ++i){
				if(DEBUG){
					System.out.println(">>>>> Argument is " + token[i]);
				}
				argSorts.add(argSort);
				String arg = token[i];
				args.add(lowInputExpr[Common.indexOf(arg)]);
			}
			functions[funcIndex] = ctx.mkFuncDecl(function, argSorts.toArray(new Sort[] {}), BS);
			C[funcIndex] = (BoolExpr)functions[funcIndex].apply(args.toArray(new Expr[]{}));
			indexOfCost.put(funcIndex, cost);
			indexOfParameters.put(funcIndex,parameters);
		}
	}
	
	@Override
	protected String generateSMTLIB2String(SmtLib2Utils<Cost> utils, HashMap<Cost, HashSet<String>> obsrv){
		return utils.groupPathConditionsUsingQuantifiers(obsrv).toString();
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
	
	@Override
	protected void computePartitions(Marco marco){
		ArrayList<Set<Integer>>allMSS = marco.run(C);
		for(Set<Integer> MSS : allMSS){
			solver.push();
			MSS.forEach(i -> solver.add(C[i]));
			// use blocking clause to generate all partitions
			boolean firstTime = true;
			HashSet<Cost> allTheCosts = new HashSet<Cost>();
			while(solver.check() == Status.SATISFIABLE){
				Expr[] values = new Expr[inputSize];
				Model m = solver.getModel();
				Integer[] lowInput = getLowInput(m, values);
				if(firstTime){
					MSS.forEach(i -> allTheCosts.add(indexOfCost.get(i)));
					firstTime = false;
				}
				visitor.visit(lowInput, allTheCosts);
				//TODO: block the current input
				ArrayList<BoolExpr> negations = new ArrayList<BoolExpr>();
				for(int i : MSS){
					String vars = indexOfParameters.get(i);
					String[] token = vars.split(" ");
					ArrayList<Expr> params = new ArrayList<Expr>();
					for(String var : token){
						params.add(values[Common.indexOf(var)]);
					}
					// BoolExpr expr = ctx.mkNot((BoolExpr) functions[i].apply(params.toArray(values)));
					BoolExpr expr = ctx.mkNot((BoolExpr) functions[i].apply(params.toArray(new Expr[]{})));
					negations.add(expr);
				}
				BoolExpr blockingClause = ctx.mkOr(negations.toArray(new BoolExpr[]{}));
				solver.add(blockingClause);
			}
			solver.pop();
		}
	}
}
