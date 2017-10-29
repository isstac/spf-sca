package sidechannel.multirun.adaptive.tree;

import java.util.ArrayList;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.search.Search;
import sidechannel.cost.abstraction.IntervalAbstraction;
import sidechannel.singlerun.SingleRunListener;
import sidechannel.tree.Node;

/**
* Generating inputs using Maximal Satisfiable Subsets
*
* @author Quoc-Sang Phan <sang.phan@sv.cmu.edu>
*
*/
public class GeneratingInputListener extends SingleRunListener{
	
	private InputGenerator generator;
	private int parent = 0;
	
	public GeneratingInputListener(Config config, Node parent) {
		super(config);
		generator = new InputGenerator(config, parent);
		this.parent = parent.id;
	}
	
	public void searchFinished(Search search) {
		super.searchFinished(search);
		if(done){
			return;
		}
		// if root node, then start normalizing the cost
		if(parent == 0){
			String interval = conf.getProperty("cost.interval");
			if(interval != null){
				int num = Integer.parseInt(interval);
				IntervalAbstraction<String> abs = new IntervalAbstraction<String>();
				obsrv = abs.normalize(obsrv,num);
				abstraction.setAbstraction(abs);
			}
		}
		generator.generateInput(obsrv, collector);
	}
	
	public ArrayList<Node> getInputNodes(){
		return generator.getInputNodes();
	}
}
