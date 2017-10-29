package sidechannel.multirun.adaptive.selfcomp;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.search.Search;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import sidechannel.multirun.MultiRunListener;
import sidechannel.multirun.adaptive.AbstractPartitionVisitor;
import sidechannel.multirun.adaptive.AllPartitionsGenerator;
import sidechannel.multirun.adaptive.MaximalPartitionGeneratorUsingFile;
import sidechannel.multirun.adaptive.MultiplePartitionsGenerator;
import sidechannel.multirun.adaptive.PartitionGenerator;

/**
 *
 * @author Quoc-Sang Phan <sang.phan@sv.cmu.edu>
 *
 */
public class SelfCompositionListener extends MultiRunListener implements AbstractPartitionVisitor<ArrayList<Long>> {
	
	protected int run = 0; // 0 is an invalid run id
	protected String tmpDir = null;
	
	protected long t;
	
	private PartitionGenerator<ArrayList<Long>> pg;
	private StringBuilder sb = new StringBuilder();
	
	private boolean linear = false;
	
	private boolean writeTime = false;
	
	protected int numOfPartitions = 0;
	
	public SelfCompositionListener(Config config){
		super(config);
		
		t = System.nanoTime();
		
		mode = SMT_LIB2_MODE;
		
		tmpDir = conf.getProperty("sidechannel.tmpDir");
		
		String tree = config.getProperty("attack.tree","full");
		String theory = config.getProperty("SMT.theory","bitvector");
		linear = theory.equals("linear");
		switch(tree){
		case "greedy":
			pg = new MaximalPartitionGeneratorUsingFile<ArrayList<Long>>(config, this, linear);
			break;
		case "multi":
			pg = new MultiplePartitionsGenerator<ArrayList<Long>>(config, this, linear);
		    break;  
		case "full":
			pg = new AllPartitionsGenerator<ArrayList<Long>>(config, this, linear);
			break;
		default:
			// wrong tree
			assert false;
		}
		
		String target_args = conf.getProperty("target.args");
		// the first argument should be the run
		int pos = target_args.indexOf(',');
		run = Integer.parseInt(pos == -1 ? target_args : target_args.substring(0,pos));
	}
	
	/*
	 * Number of partitions to be refined, i.e. exclude the leaves
	 */
	public int getNumOfPartitions(){
		return numOfPartitions;
	}
	
	protected String createInputLine(Integer[] lowInput, Set<ArrayList<Long>> allTheCosts ){
		String prevInput = null;
		if(run > 1){
			prevInput = conf.getProperty("target.args");
			String token[] = prevInput.split("@");
			prevInput = token[1] + "#";
		}
		
		StringBuilder sb = new StringBuilder();
		for (ArrayList<Long> costs : allTheCosts) {
			int i;
			for (i = 0; i < costs.size() - 1; i++){
				sb.append(costs.get(i)+ ":");
			}
			sb.append(costs.get(i)+ "@");
			
			if(prevInput != null){
				sb.append(prevInput);
			}
			for (i = 0; i < lowInput.length - 1; i++){
				sb.append(lowInput[i] + ":");
			}
			sb.append(lowInput[i] + "\n");
		}

		return sb.toString();
	}
	
	@Override
	public void searchFinished(Search search) {
		
		if (DEBUG) {
			printCosts();	
		}
		System.out.println("\n>>>>> There are " + numOfPCs 
				+ " path conditions and " + obsrv.size() + " observables \n");
		
		if(numOfPCs == 0){
			return;
		}
		
		// write the time
		long tmp = System.nanoTime();
		long t1 = tmp - t;
		// System.out.println("Time SPF is " + TimeUnit.NANOSECONDS.toMillis(t1) + " miliseconds");
		t = tmp;
		computePartitions();
		if(writeTime){
			long t2 = System.nanoTime() - t;
			writeTime(TimeUnit.NANOSECONDS.toMillis(t1) + "#" + TimeUnit.NANOSECONDS.toMillis(t2) + "\n");
		}
	}
	
	protected void computePartitions(){
		pg.computePartitions(obsrv, collector);
		
		// System.out.println(">>>>> There are " + allTheCosts.size() + " observables in " + run + "th run");
		String inputFileName = tmpDir + "/run" + run + ".txt" ;
		try {
			File file = new File(inputFileName);
			if (!file.exists()) {
				file.createNewFile();
			}
			Files.write(Paths.get(inputFileName), sb.toString().getBytes(),
					StandardOpenOption.APPEND);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	protected void writeTime(String str) {
		try {
			String inputFileName = tmpDir + "/time.txt";
			File file = new File(inputFileName);
			if (!file.exists()) {
				file.createNewFile();
			}
			Files.write(Paths.get(inputFileName), str.getBytes(), StandardOpenOption.APPEND);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void visit(Integer[] lowInput, Set<ArrayList<Long>> allTheCosts) {
		int size = allTheCosts.size();
		++AdaptiveSelfCompQuantifier.numOfSelectedNodes;
		AdaptiveSelfCompQuantifier.numOfNodes += (1+size);
		if(size <= 1){
			++AdaptiveSelfCompQuantifier.numOfLeaves;
			return;
		}
		numOfPartitions += size;
		if(run == AdaptiveSelfCompQuantifier.numOfRuns){
			AdaptiveSelfCompQuantifier.numOfLeaves += size;
		}
		System.out.println("--------------------------------------------");
		for(ArrayList<Long> cost: allTheCosts){
			printCost(cost);
		}
		System.out.println("--------------------------------------------\n");
		String result = createInputLine(lowInput,allTheCosts);	
		sb.append(result);
	}
}
