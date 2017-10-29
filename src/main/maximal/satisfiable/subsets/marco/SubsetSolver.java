package maximal.satisfiable.subsets.marco;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Status;

/**
 * the algorithm is named after Venetian explorer Marco Polo
 *
 * @author Quoc-Sang Phan <sang.phan@sv.cmu.edu>
 *
 */
public class SubsetSolver {
	/*
	 * This is an SMT solver
	 */
	protected Solver solver = null; 
	
	// the set of soft constraints
	BoolExpr[] C = null;
	HashMap<Integer,Integer> map = null;
	
	public SubsetSolver(Solver solver, BoolExpr[] softConstraints){
		// the solver has some hard constraints
		this.solver = solver;
		C = softConstraints;
		map = new HashMap<Integer, Integer>();
		for (int i = 0; i < C.length; i++) {
			map.put(C[i].getId(), i);
		}
	}
	
	boolean isSatisfiable(BoolExpr[] seed){
		if(seed == null){
			System.out.println("Seed is null");
			return true;
		}
		solver.push();
		solver.add(seed);
		boolean result = solver.check() == Status.SATISFIABLE;
		// solver.pop();
		return result;
	}
	
	public Set<Integer> grow(BoolExpr[] seed){
		// seed has not been popped
		int depth = 1;
		Set<Integer> seedSet = new HashSet<Integer>();
		for(BoolExpr c : seed){
			seedSet.add(map.get(c.getId()));
		}
		for(int i = 0; i < C.length; ++i){
			if(!seedSet.contains(i)){
				solver.push();
				++depth;
				solver.add(C[i]);
				if(solver.check() == Status.SATISFIABLE){
					seedSet.add(i);
				}
				else{
					solver.pop();
					--depth;
				}
			}
		}
		// pop the seed
		solver.pop(depth);
		// at this point, seed grows into a Maximal Satisfiable Subset
		return seedSet;
	}
	
	public Set<Integer> shrink(BoolExpr[] seed){
		// seed has not been popped, pop it
		solver.pop();
		// convert seed to array list for convenience
		HashSet<BoolExpr> current = new HashSet<BoolExpr>();
		for(BoolExpr c : seed){
			current.add(c);
		}
	
		for(BoolExpr c : seed){
			if(current.size() <= 1){
				// seed is already minimal, no need to shrink
				break;
			}
			if(!current.contains(c)){
				continue;
			}
			// try removing c
			current.remove(c);
			BoolExpr[] newSeed = current.toArray(new BoolExpr[1]);
			
			/* Nikolaj's use of Unsat Core, doesn't seem to work
			if(solver.check(newSeed) == Status.UNSATISFIABLE){
				Expr[] core = solver.getUnsatCore();
				current = new ArrayList<BoolExpr>();
				for(Expr c1 : core){
					current.add((BoolExpr)c1);
				}
			}
			else{
				current.add(c);
			}
			//*/
			
			// solver.check(seed) shows some red annoying warnings
			// so we use push and pop to get rid of the warnings
			solver.push();
			solver.add(newSeed);
			if(solver.check() == Status.UNSATISFIABLE){
				// do nothing
			} else{
				// add it back, since it is not part of MUS
				current.add(c);
			}
			solver.pop();
		}
		// at this point seedList is a Minimal Unsatisfiable Subset
		Set<Integer> seedSet = new HashSet<Integer>();
		for(BoolExpr c : current){
			seedSet.add(map.get(c.getId()));
		}
		return seedSet;
	}
}
