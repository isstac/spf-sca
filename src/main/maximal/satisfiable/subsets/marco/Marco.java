package maximal.satisfiable.subsets.marco;

import java.util.ArrayList;
import java.util.Set;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Solver;

/**
 * the algorithm is named after Venetian explorer Marco Polo
 *
 * @author Quoc-Sang Phan <sang.phan@sv.cmu.edu>
 *
 */
public class Marco {

	protected Context ctx = null;
	protected Solver s = null;
	
	public Marco(Context ctx, BoolExpr... hardConstraints){
		this.ctx = ctx;
		s = ctx.mkSolver();
		if(hardConstraints != null){
			s.add(hardConstraints);
		}
	}
	
	public ArrayList<Set<Integer>> run(BoolExpr[] C){
		MapSolver MAP = new MapSolver(C);
		SubsetSolver solver = new SubsetSolver(s,C);
		ArrayList<Set<Integer>> allMSS = new ArrayList<Set<Integer>>();
		while (MAP.isSatisfiable()){
			BoolExpr[] seed = MAP.nextSeed();
			if(solver.isSatisfiable(seed)){
				Set<Integer> MSS = solver.grow(seed);
				// yield MSS
				allMSS.add(MSS);
				
				if(MSS.size() == C.length){
					// no other MSS can be found
					break;
				}
				
				MAP.blockDown(MSS);
			}
			else{
				Set<Integer> MUS = solver.shrink(seed);
				// yield MUS: we ignore MUS
				MAP.blockUp(MUS);
			}
		}
		return allMSS;
	}
	
	public static void printAllMSSs(BoolExpr[] C, ArrayList<Set<Integer>> allMSS){
		System.out.println("The set of all Maximal Satisfiable Subsets are:");
		System.out.println("-------------------------");
		for(Set<Integer> MSS : allMSS){
			for(int i : MSS){
				System.out.println(C[i]);
			}
			System.out.println("-------------------------");
		}
	}
}
