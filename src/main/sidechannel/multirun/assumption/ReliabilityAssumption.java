package sidechannel.multirun.assumption;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import modelcounting.domain.Problem;

import com.google.common.cache.LoadingCache;

/**
 *
 * @author Quoc-Sang Phan <sang.phan@sv.cmu.edu>
 *
 */
public class ReliabilityAssumption {
	
	public static String buildConstraint(LoadingCache<Problem, Set<Problem>> omegaCache){
		assert(omegaCache != null);
		Collection<Set<Problem>> collections = omegaCache.asMap().values();
		// TODO: I think my analysis will be wrong if collections has more than 1 element
		// but I'm not sure
		// assert(collections.size() == 1);
		HashMap<List<String>,ArrayList<String>> map = new HashMap<List<String>,ArrayList<String>>();
		// Since the collection has size 1, this loop looks stupid
		for (Set<Problem> problems : collections) {
			// At this point we have a list of constraint:
			for (Problem problem : problems) {
				if (!problem.isFalse()) {
					//TODO: build constraint
					String constraint = problem.getConstraints().toString();
					List<String> key = problem.getVarList().asList();
					ArrayList<String> value = map.get(key);
					if (value == null) {
						value = new ArrayList<String>();
						value.add(constraint);
						map.put(key, value);
						// reset current cost
					}else{
						value.add(constraint);
					}
				}
			}
		}
		StringBuilder sb = new StringBuilder();
		for(ArrayList<String> disjunct : map.values()){
			for(String conjunct: disjunct){
				sb.append(conjunct + " #OR# ");
			}
			sb.delete(sb.length() - 6, sb.length());
			sb.append(" #AND# ");
		}
		sb.delete(sb.length() - 7, sb.length());
		return sb.toString();
	}
	
	
	public static void printOmegaCache(LoadingCache<Problem, Set<Problem>> omegaCache) {
		if (omegaCache == null) {
			return;
		}
		System.out.println("=================== OMEGA CACHE =================");
		for (Set<Problem> problems : omegaCache.asMap().values()) {
			for (Problem problem : problems) {
				if (!problem.isFalse()) {
					System.out.println(problem);
				}
			}
		}
		System.out.println("=================================================");
	}
}
