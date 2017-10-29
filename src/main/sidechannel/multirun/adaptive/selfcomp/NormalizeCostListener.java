package sidechannel.multirun.adaptive.selfcomp;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.search.Search;
import sidechannel.cost.abstraction.IntervalAbstraction;
import sidechannel.singlerun.SingleRunListener;

public class NormalizeCostListener extends SingleRunListener {

	public NormalizeCostListener(Config config) {
		super(config);
	}

	public void searchFinished(Search search) {
		super.searchFinished(search);
		if (done) {
			return;
		}
		String interval = conf.getProperty("cost.interval");
		if (interval != null) {
			int num = Integer.parseInt(interval);
			IntervalAbstraction<String> abs = new IntervalAbstraction<String>();
			obsrv = abs.normalize(obsrv, num);
			abstraction.setAbstraction(abs);
		}
	}
}
