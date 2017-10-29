package sidechannel.multirun.adaptive.tree;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.search.Search;

import java.util.ArrayList;

import sidechannel.singlerun.SingleRunListener;
import sidechannel.tree.Node;

/**
 * Generating outputs
 *
 * @author Quoc-Sang Phan <sang.phan@sv.cmu.edu>
 *
 */
public class GeneratingOutputListener extends SingleRunListener {

	OutputGenerator generator;

	public GeneratingOutputListener(Config config, Node parent) {
		super(config);
		mode = LATTE_MODE;
		generator = new OutputGenerator(config, parent);
	}

	public ArrayList<Node> getOutputNodes() {
		return generator.getOutputNodes();
	}

	public void searchFinished(Search search) {

		super.searchFinished(search);
		if (done) {
			return;
		}
		if (DEBUG) {
			printCosts();
		}
		generator.generateOutput(obsrv, collector);
	}

}
