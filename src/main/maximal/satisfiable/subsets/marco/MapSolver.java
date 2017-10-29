package maximal.satisfiable.subsets.marco;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Model;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Status;

/**
 *
 * @author Quoc-Sang Phan <sang.phan@sv.cmu.edu>
 *
 */
public class MapSolver {

	int n = 0; // number of soft constraints
	
	/*
	 * The Map has an internal SAT solver
	 * Here we use Z3 for our convenience, 
	 * since it already has the Java API,
	 * but maybe MathSAT is better?
	 */
	protected Context ctx = null;
	protected Solver solver = null; 
	
	// the set of soft constraints
	BoolExpr[] C = null;
	
	// the set of boolean variable
	BoolExpr[] x = null;
	
	// HashMap<Integer,Integer> map = null;
	
	public MapSolver(BoolExpr[] Constraints){
		C = Constraints;
		n = Constraints.length;
		
		HashMap<String, String> cfg = new HashMap<String, String>();
		cfg.put("model", "true");
		ctx = new Context(cfg);
		solver = ctx.mkSolver();
		
		// initialize the variables
		x = new BoolExpr[n];
		// map = new HashMap<Integer,Integer>();
		for(int i = 0; i < n; i++){
			x[i] = ctx.mkBoolConst("x"+i);
			// map.put(x[i].getId(), i);
		}
	}
	
	boolean isSatisfiable(){
		return solver.check() == Status.SATISFIABLE;
	}
	
	BoolExpr[] nextSeed(){
		if(solver.check() == Status.UNSATISFIABLE)
			return null;
		// if map is empty, return the set of all constraints
		if(solver.getNumAssertions() == 0){
			//TODO: do we need to return a copy,
			// or we can simply return C
			BoolExpr[] copy = new BoolExpr[n];
			for(int i = 0; i < n; ++i){
				copy[i] = C[i];
			}
			return copy;
		}
		
		Model m = solver.getModel();
		ArrayList<BoolExpr> list = new ArrayList<BoolExpr>();
		for(int i = 0; i < n; ++i){
			// correct with definition, but doesn't work
			/*
			if(m.evaluate(x[i], false).isTrue()){
				list.add(C[i]);
			}
			//*/
			if(m.evaluate(x[i], false).isFalse()){
				continue;
			}
			// the default is to add all
			list.add(C[i]);
		}
		return list.toArray(new BoolExpr[1]);
	}
	
	public void blockUp(Set<Integer> MUS) {
		ArrayList<BoolExpr> result = new ArrayList<BoolExpr>();
		for (int i : MUS) {
			result.add(ctx.mkNot(x[i]));
		}
		BoolExpr block = ctx.mkOr(result.toArray(new BoolExpr[1]));
		solver.add(block);
	}
	
	public void blockDown(Set<Integer> MSS){
		// create the complement of the MSS
		ArrayList<BoolExpr> complement = new ArrayList<BoolExpr>();
		for(int i = 0; i < n; i++){
			if(!MSS.contains(i)){
				complement.add(x[i]);
			}
		}
		BoolExpr block = ctx.mkOr(complement.toArray(new BoolExpr[1]));
		solver.add(block);
	}
}
