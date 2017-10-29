package sidechannel.choice;

import gov.nasa.jpf.symbc.numeric.PathCondition;
import gov.nasa.jpf.vm.choice.IntIntervalGenerator;

/**
 *
 * @author Quoc-Sang Phan <sang.phan@sv.cmu.edu>
 *
 */
public class DomainChoiceGenerator extends IntIntervalGenerator {

	PathCondition assumptions;

	// Always make only one choice
	@SuppressWarnings("deprecation")
	public DomainChoiceGenerator(PathCondition PC) {
		super(0, 0);
		assumptions = PC;
	}

	// returns the PC constraints for the current choice
	public PathCondition getPathCondition() {
		return assumptions;
	}
}
