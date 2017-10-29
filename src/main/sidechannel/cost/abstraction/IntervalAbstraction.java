package sidechannel.cost.abstraction;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

/**
*
* @author Quoc-Sang Phan <sang.phan@sv.cmu.edu>
*
*/
public class IntervalAbstraction <SymbolicPath> {

	private int numOfIntervals = 10;
	private Long minCost = Long.MAX_VALUE;
	private Long maxCost = Long.MIN_VALUE;
	private Long interval;
	private boolean initialized = false;
	
	public IntervalAbstraction(){}
	
	public IntervalAbstraction(HashMap<Long, HashSet<SymbolicPath>>obsrv, int numOfIntervals){
		initialize(obsrv,numOfIntervals);
	}
	
	private void initialize(HashMap<Long, HashSet<SymbolicPath>>obsrv, int numOfIntervals){
		this.numOfIntervals = numOfIntervals;
		// TODO this part may not be necessary if the listener tracks min and max values of the cost
		// However, this tracking might be costly for listeners that doesn't need normalization?
		// For now, let find the min and max here
		for(Long cost : obsrv.keySet()){
			if(cost < minCost){
				minCost = cost;
			}
			if(cost > maxCost){
				maxCost = cost;
			}
		}
		System.out.println(String.format("Max is %d and min is %d", maxCost, minCost));
		// end search
		
		interval = (maxCost - minCost ) / numOfIntervals;
		initialized = true;
	}
	
	public HashMap<Long, HashSet<SymbolicPath>> normalize(HashMap<Long, HashSet<SymbolicPath>>obsrv, int numOfIntervals){
		initialize(obsrv,numOfIntervals);
		HashMap<Long, HashSet<SymbolicPath>> result = new HashMap<Long, HashSet<SymbolicPath>>();
		for(Entry<Long, HashSet<SymbolicPath>> entry : obsrv.entrySet()){
			long key = entry.getKey();
			long index = normalize(key);
			HashSet<SymbolicPath> paths = result.get(index);
			if (paths == null) {
				paths = new HashSet<SymbolicPath>();
				paths.addAll(entry.getValue());
				result.put(index, paths);
			} else {
				paths.addAll(entry.getValue());
			}
		}
		return result;
	}
	
	public Long normalize(long cost){
		if(!initialized){
			return cost;
		}
		return (cost == maxCost) ? (numOfIntervals - 1) : (cost - minCost) / interval;
	}

}
