package sidechannel.cost;

import gov.nasa.jpf.vm.SystemState;

/**
*
* A generic cost model
*
* @author Quoc-Sang Phan <sang.phan@sv.cmu.edu>
*
*/
public interface Cost <Unit> {
	
	public Unit getCurrentCost(SystemState ss);
}
