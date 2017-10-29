package sidechannel.choice;

import gov.nasa.jpf.vm.choice.IntIntervalGenerator;

/**
 *
 * @author Quoc-Sang Phan <sang.phan@sv.cmu.edu>
 *
 */
public class CostChoiceGenerator extends IntIntervalGenerator {

	// store the cost, i.e. the observable, of the secure method
	protected Long cost;

	// Always make only one choice
	@SuppressWarnings("deprecation")
	public CostChoiceGenerator(Long n) {
		super(0, 0);
		cost = n;
	}
	
	public Long getCost(){
		return cost;
	}
}
