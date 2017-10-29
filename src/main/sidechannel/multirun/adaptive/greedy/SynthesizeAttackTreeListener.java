package sidechannel.multirun.adaptive.greedy;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.search.Search;
import sidechannel.singlerun.SingleRunListener;
import sidechannel.tree.DotTree;
import sidechannel.tree.Node;
import sidechannel.tree.SequenceNode;

/**
* This listener collects all the symbolic paths,
* then builds the whole attack tree
*
* @author Quoc-Sang Phan <sang.phan@sv.cmu.edu>
*
*/
public class SynthesizeAttackTreeListener extends SingleRunListener{
	
	protected int numOfRuns;
	private int depth = 0;
	private int numOfNodes = 0;
	public int numOfSelectedNodes = 0;
	public int numOfLeaves = 0;
	private Queue<Node> queue = new LinkedList<Node>();
	private static boolean buildTree = true;
	private static DotTree dotTree = null;
	
	public SynthesizeAttackTreeListener(Config config) {
		super(config);
		mode = SMT_LIB2_MODE;
		numOfRuns = Integer.parseInt(conf.getProperty("multirun.num_run","1"));
		buildTree = config.getProperty("visualize","true").equals("true");
		if(buildTree){
			dotTree = new DotTree();
		}
	}
	
	public void searchFinished(Search search) {

		super.searchFinished(search);
		if(done){
			return;
		}
		
		synthesizeAttackTree();
		printStats();
		
	}
	
	private void printStats(){
		System.out.println("\n>>>>> Number of runs analyzed is " + depth);
		System.out.println(">>>>> There are " + numOfNodes + " nodes");
		System.out.println(">>>>> There are " + numOfSelectedNodes + " selected nodes");
		System.out.println(">>>>> Number of observables is " + numOfLeaves);
		System.out.println(">>>>> Channel Capacity is " + Math.log(numOfLeaves));
	}
	
	private void synthesizeAttackTree(){
		SequenceNode root = new SequenceNode(0,0,null); // root of the attack tree
		root.value = "root";
		if(buildTree){
			dotTree.addNode(root);
		}
		queue.add(root); 
		SequenceGenerator generator = new SequenceGenerator(conf, obsrv, collector);
		// breadth-first search
		while(!queue.isEmpty()){
			Node head = queue.remove();
			depth = head.depth;

			if(head.getDepth() >= numOfRuns){
				// reach leaf node
				++numOfLeaves;
				continue;
			}
			
			ArrayList<Node> children = generator.generateSequence(head);
			if (children != null) {
				numOfNodes += children.size();
				Node input = children.get(0);
				if (buildTree) {
					dotTree.addNode(input);
				}
				
				if(input.isDeadEnd()){
					++numOfLeaves;
					continue;
				}
				
				++numOfSelectedNodes;
				
				for (int i = 1; i < children.size(); ++i) {
					Node child = children.get(i);
					if (buildTree) {
						dotTree.addNode(child);
					}
					queue.add(child);
				}
			}
		}
		if(buildTree){
			dotTree.printTreeToFile(conf);
		}

	}
}
