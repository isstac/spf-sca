package sidechannel.multirun.adaptive.tree;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import sidechannel.common.Common;
import sidechannel.multirun.adaptive.AbstractAdaptiveQuantifier;
import sidechannel.tree.DotTree;
import sidechannel.tree.InputNode;
import sidechannel.tree.Node;
import sidechannel.tree.OutputNode;

/**
 *
 * @author Quoc-Sang Phan <sang.phan@sv.cmu.edu>
 *
 */
public class AttackTreeBuilder extends AbstractAdaptiveQuantifier {
	
	// the queue to use in breadth-first search
	private static Queue<Node> queue = new LinkedList<Node>();
	
	// a global unique ID for all the nodes in the attack tree
	public static int id = 0;
	public static int numOfLeaves = 0;
	public static int numOfWinningNodes = 0;
	public static int numOfDeadEnds = 0;
	
	private static int depth = 0;
	private static int numOfSelected = 0;
	
	private static boolean buildTree = true;
	
	private static DotTree dotTree = null;
	
	// for entropy
	private static Node selected = null;
	private static double maxEntropy = 0;
	private static Map<Integer,Integer> selectedNodes;
	private static double D = 0;
	private static double Entropy = 0;
	
	// when to delete garbage of model counting
	public static int MC_TIMER;
	// when to delete garbage of tree
	public static int TREE_TIMER;
	
	// timer for garbage collection
	public static int timer1 = 0;
	public static int timer2 = 0;
	public static boolean garbageCollection = false;
	
	public static void start(Config config, String[] args) {

		conf = config;
		
		MC_TIMER = Integer.parseInt(conf.getProperty("modelcounting.timer","30"));
		TREE_TIMER = Integer.parseInt(conf.getProperty("tree.timer","100"));

		setupEnvironment();
		
		buildTree = config.getProperty("visualize","true").equals("true");
		
		if(buildTree){
			dotTree = new DotTree();
		}

		String tree = config.getProperty("attack.tree","full");
		if(tree.equals("entropy")){
			createAttackTreeUsingEntropy();
		}
		else{
			createAttackTree();
		}
		printStatistic();
	}
	
	private static void printStatistic(){
		System.out.println("\n>>>>> Number of runs analyzed is " + depth/2);
		System.out.println(">>>>> There are total " + (id + 1) + " nodes.");
		if(selectedNodes != null){
			System.out.println(">>>>> " + selectedNodes.size() + " nodes are selected.");
		} else{
			System.out.println(">>>>> " + numOfSelected + " nodes are selected.");
		}
		System.out.println(">>>>> There are " + numOfWinningNodes + " nodes where the adversary successfully reveals the secret.");
		System.out.println(">>>>> There are " + numOfDeadEnds + " nodes where the adversary can't make a progress.");
		System.out.println(">>>>> There are " + numOfLeaves + " observables.");
		System.out.println(">>>>> Channel Capacity is " + Math.log(numOfLeaves)/Math.log(2) + " bits");
	}
	
	private static double computeEntropy(ArrayList<Node> nodes){
		// all the nodes are siblings of the same parent
		OutputNode grandparent = (OutputNode) nodes.get(0).parent.parent;
		double domain = 0;
		if(grandparent.size == 0){
			// compute domain from root node
			for(Node node : nodes){
				domain += ((OutputNode)node).size;
			}
			D = domain;
		} else {
			domain = grandparent.size;
		}
		double leakage = 0;
		for(Node node : nodes){
			leakage += ((OutputNode)node).size * (Math.log(domain) - Math.log(((OutputNode)node).size));
		}
		leakage = leakage / (Math.log(2) * domain);
		return leakage;
	}
	
	private static void addLeakage(OutputNode n){
		long size = n.size;
		Entropy += size * (Math.log(D) - Math.log(size));
	}
	
	private static void createAttackTreeUsingEntropy() {
		selectedNodes = new HashMap<Integer,Integer>();
		Node root = new OutputNode(0,0,null); // root of the attack tree
		root.value = "root";
		if(buildTree){
			dotTree.addNode(root);
		}
		queue.add(root); 
		// breadth-first search
		while(!queue.isEmpty()){
			Node head = queue.remove();
			collectGarbage(head);
			depth = head.depth;
			// ignore child of unselected nodes
			if(head.isOutput()){
				if(	selected != null && !head.parent.isSelected()){
					continue;
				}
			}
			
			if(head.getDepth() >= 2 * numOfRuns){
				// reach leaf node
				addLeakage((OutputNode)head);
				++numOfLeaves;
				continue;
			}
			
			if(head.isWinningNode()){
				addLeakage((OutputNode)head);
				++numOfLeaves;
				continue;
			}
			
			if(head.isDeadEnd()){
				continue;
			}
			
			ArrayList<Node> children = getChildNodes(head);
			if(children != null) {
				for(Node child : children){
					if(buildTree){
						dotTree.addNode(child);
					}
					queue.add(child);
					// add child node to the parent node in a tree model?
				}
			}
			
			if(head.isInput()){
				double leakage = computeEntropy(children);
				if(selected == null || head.parent.id != selected.parent.id || leakage > maxEntropy){
					((InputNode)head).selected = true;
					if(selected != null && selected.parent.id == head.parent.id){
						((InputNode)selected).selected = false;
					}
					selected = head;
					maxEntropy = leakage;
					selectedNodes.put(selected.parent.id,selected.id);
				}
			}
		}
		Entropy = Entropy / (Math.log(2) * D);
		System.out.println("\n>>>>> Leakage in Shannon entropy is " + Entropy);
		if(buildTree){
			dotTree.colorSelectedNodes(selectedNodes);
			dotTree.printTreeToFile(conf);
		}
	}
	
	/**
	 * 
	 */
	private static void createAttackTree() {
		Node root = new OutputNode(0,0,null); // root of the attack tree
		root.value = "root";
		if(buildTree){
			dotTree.addNode(root);
		}
		queue.add(root); 
		// breadth-first search
		while(!queue.isEmpty()){
			Node head = queue.remove();
			collectGarbage(head);
			depth = head.depth;
			if(head.isWinningNode() || head.isDeadEnd()){
				++numOfLeaves;
				continue;
			}
			
			if(head.getDepth() >= 2 * numOfRuns){
				// reach leaf node
				++numOfLeaves;
				continue;
			}
			ArrayList<Node> children = getChildNodes(head);
			if(children != null) {
				for(Node child : children){
					if(buildTree){
						dotTree.addNode(child);
					}
					queue.add(child);
					// add child node to the parent node in a tree model?
				}
			}
			if(head.isInput()){
				++numOfSelected;
			}
		}
		if(buildTree){
			dotTree.printTreeToFile(conf);
		}
	}
	
	private static ArrayList<Node> getChildNodes(Node parent){
		String tmpDir = conf.getProperty("sidechannel.tmpDir","build/tmp");
		conf.setProperty("sidechannel.smt2", tmpDir + "/maxSMT.node" + parent.id
				+ ".smt2");
		if(parent.isInput()){
			return getOutputNodes(parent);
		}
		// parent is an output node
		return getInputNodes(parent);
	}
	
	private static ArrayList<Node> getOutputNodes(Node parent){
		assert(parent.isInput()); // make sure that parent is an input node
		String target_args = "defend," + parent.parent.id + "," + parent.id;
		conf.setProperty("target.args", target_args);
		JPF jpf = new JPF(conf);
		GeneratingOutputListener defendListener = new GeneratingOutputListener(conf, parent);
		jpf.addListener(defendListener);
		try {
			jpf.run();
			return defendListener.getOutputNodes();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private static ArrayList<Node> getInputNodes(Node parent){
		assert(!parent.isInput()); // make sure that parent is an output node
		String target_args = "attack," + parent.id;
		conf.setProperty("target.args", target_args);
		JPF jpf = new JPF(conf);
		GeneratingInputListener attackListener = new GeneratingInputListener(conf, parent);
		jpf.addListener(attackListener);
		try {
			jpf.run();
			return attackListener.getInputNodes();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	// Too many small files will kill the file system of the OS
	// We need to delete constraint and input files
	// This would also speed up the procedure
	private static void collectGarbage(Node current) {
		++ timer2;
		if(timer2 < TREE_TIMER){
			return;
		}
		int maxInput = 0, maxOutput = 0;
		if (current.isInput()) {
			maxInput = current.id;
			maxOutput = current.parent.id;
		} else {
			maxInput = current.parent.id;
			maxOutput = current.parent.parent.id;
		}
		File folder = new File(conf.getProperty("sidechannel.tmpDir","build/tmp"));
		File[] listOfFiles = folder.listFiles();
		String fileName;
		int start, end, index;
		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				// System.out.println("File " + listOfFiles[i].getName());
				fileName = listOfFiles[i].getName();
				end = fileName.indexOf(".txt");
				start = fileName.indexOf(Common.CONSTRAINT_PREFIX);
				if(start >= 0){
					start += Common.CONSTRAINT_PREFIX.length();
					index = Integer.parseInt(fileName.substring(start,end));
					if(index < maxOutput){
						// System.out.println("Delete " + fileName);
						listOfFiles[i].delete();
						/*
						try {
							FileUtils.forceDelete(listOfFiles[i]);
						} catch (IOException e) {
							e.printStackTrace();
						}
						//*/
					}
				} else{
					start = fileName.indexOf(Common.INPUT_PREFIX);
					if(start >= 0){
						start += Common.INPUT_PREFIX.length();
						index = Integer.parseInt(fileName.substring(start,end));
						if(index < maxInput){
							// System.out.println("Delete " + fileName);
							listOfFiles[i].delete();
							/*
							try {
								FileUtils.forceDelete(listOfFiles[i]);
							} catch (IOException e) {
								e.printStackTrace();
							}
							//*/
						}
					}
				}
			}
		}
		// reset timer
		timer2 = 0;
	}
}
