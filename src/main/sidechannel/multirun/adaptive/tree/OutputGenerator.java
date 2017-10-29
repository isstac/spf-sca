package sidechannel.multirun.adaptive.tree;

import gov.nasa.jpf.Config;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import modelcounting.domain.Problem;
import sidechannel.common.Common;
import sidechannel.multirun.assumption.ReliabilityAssumption;
import sidechannel.tree.Node;
import sidechannel.tree.OutputNode;
import sidechannel.util.ConfigUtils;
import sidechannel.util.ModelCounter;
import sidechannel.util.SymbolicVariableCollector;

import com.google.common.cache.LoadingCache;

/**
*
* @author Quoc-Sang Phan <sang.phan@sv.cmu.edu>
*
*/
public class OutputGenerator {
	
	private Config conf;
	private Node parent;
	private ArrayList<Node> children = new ArrayList<Node>();
	
	private boolean DEBUG = false;
	
	public OutputGenerator(Config config, Node parent){
		conf = config;
		this.parent = parent;
	}
	
	public void generateOutput(HashMap<Long, HashSet<String>> obsrv, SymbolicVariableCollector collector){		
		// select only the output with largest blockSize
		ModelCounter counter = new ModelCounter(conf,collector);
		if(conf.getProperty("symbolic.reliability.problemSettings")== null){
			counter.createUserProfile(collector);
		}
		for (Long cost : obsrv.keySet()) {
			HashSet<String> setOfPC = obsrv.get(cost);
			// TODO: count for each PC			
			// long blockSize = counter.count(setOfPC);
			long blockSize = 0;
			System.out.println("\n\n\n>>>>> The cost is " + cost);
			StringBuilder sb = new StringBuilder();
			// TODO: do we really need to count?
			// because we only need to create assumptions from Omega
			// Does the count give additional information?
			for(String pc : setOfPC){
				long count = counter.countSinglePathResetCache(pc).longValue();
				++AttackTreeBuilder.timer1;
				blockSize += count;
				if(count > 0){
					LoadingCache<Problem, Set<Problem>> omegaCache = counter.getOmegaCache();
					sb.append(ReliabilityAssumption.buildConstraint(omegaCache) + "\n");
					if(DEBUG){
						System.out.println("\n\n" + pc);
						ReliabilityAssumption.printOmegaCache(omegaCache);
					}
				}
			}
			
			// garbage collection kicks in
			if(AttackTreeBuilder.timer1 > AttackTreeBuilder.MC_TIMER){
				ConfigUtils.cleanReliabilityTmpDir(conf);
				// reset timer
				AttackTreeBuilder.timer1 = 0;
			}
			
			int index = ++AttackTreeBuilder.id;
			OutputNode child = new OutputNode(parent.depth + 1,index,parent);
			// TODO: need to add the case where the blockSize doesn't increase
			// this is also a leaf node
			//*
			if(blockSize == 1){
				child.setLeafNode();
				++AttackTreeBuilder.numOfWinningNodes;
			}
			//*/
			child.value = Long.toString(cost);
			child.size = blockSize;
			/*
			if(parent.parent.id != 0 && blockSize >= parent.parent.size){
				child.setDeadEnd();
			}
			//*/
			children.add(child);
			writeConstraintsToFile(sb.toString(),index);			
			System.out.println(">>>>> The number of model is: "
					+ blockSize);
		}
	}
	
	public ArrayList<Node> getOutputNodes(){
		return children;
	}
	
	private void writeConstraintsToFile(String str, int id){
		String fileName =  conf.getProperty("sidechannel.tmpDir","build/tmp") 
				+ "/" + Common.CONSTRAINT_PREFIX + id + ".txt";
		Writer writer = null;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(fileName), "utf-8"));
			writer.write(str);
		} catch (IOException ex) {
			// report
		} finally {
			try {
				writer.close();
			} catch (Exception ex) {
			}
		}
	}
}
