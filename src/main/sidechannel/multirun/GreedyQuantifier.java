package sidechannel.multirun;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.TimeUnit;

import sidechannel.util.Environment;
import sidechannel.util.smt.Z3MaxSmtExecutor;

/**
 * A multi-run quantifier using greedy strategy
 *
 * @author Quoc-Sang Phan <sang.phan@sv.cmu.edu>
 *
 */
public class GreedyQuantifier {

	public static Config conf;
	// static int inputSize;
	static boolean verbose;
	
	public static void start(Config config, String[] args){
				
		conf = config;
		
		// delete input file
		String input = conf.getProperty("greedy.input","build/tmp/input.txt");
		File file = new File(input);
		try {
			Files.deleteIfExists(file.toPath());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// verbose = conf.getProperty("sidechannel.verbose","false").trim().equals("true");
		verbose = true;
		
		// find z3
		String z3 = Environment.find("z3");
		if (z3 == null){
			return;
		}
		conf.setProperty("z3", z3);
			    
		quantify();
	}
	
	private static void synthesizeSingleRunAttack(int k) {

		System.out.println("\n\n>>>>>>>>>>>>>>> Result for run " + k + " <<<<<<<<<<<<<<<\n");
		long t = 0;
        long t1 = System.nanoTime();
		String args = conf.getProperty("multirun.args");

		String target_args = Integer.toString(k);
		if (args != null) {
			target_args = target_args + "," + args;
		}
		
		conf.setProperty("target.args", target_args);
		
		JPF jpf = new JPF(conf);
		
		try {
			jpf.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
		t = System.nanoTime()-t1;
        System.out.println(">>>>> SPF time of run " + k +  " is " + TimeUnit.NANOSECONDS.toMillis(t) + " ms");
		
        String theory = conf.getProperty("SMT.theory","bitvector");
        boolean linear = theory.equals("linear");
        
		// call z3
        t1 = System.nanoTime();
        Z3MaxSmtExecutor<?> z3 = new Z3MaxSmtExecutor<Long>(conf,linear,true);
        z3.run(false, true, false, null);
        t = System.nanoTime()-t1;
        Integer[] lowInput = z3.getLowInput();
        System.out.println(">>>>> Z3 time of run " + k +  " is " + TimeUnit.NANOSECONDS.toMillis(t) + " ms");
		
        try {
			StringBuilder sb = new StringBuilder();
			for (int j = 0; j < lowInput.length; j++) {
				sb.append(lowInput[j] + " ");
			}
			sb.append("\n");

			String inputFile = conf.getProperty("greedy.input","build/tmp/input.txt");
			File file = new File(inputFile);
			if (!file.exists()) {
				file.createNewFile();
			}

			Files.write(Paths.get(inputFile), sb.toString().getBytes(),
					StandardOpenOption.APPEND);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void quantify(){
		String num_run = conf.getProperty("multirun.num_run");
		if (num_run == null) {
			System.out.println(">>>>> Configuration error: number of run is required for multi-run analysis");
			return;
		}
		
		int k;
		try{
			k = Integer.parseInt(num_run);
		}
		catch(NumberFormatException e){
			System.out.println(">>>>> Configuration error: number of run is incorrect");
			e.printStackTrace();
			return;
		}
		
		assert (k > 0);
						
		// synthesize the attack step by step until the k-th execution
		for (int i = 1; i <= k; i++) {
            synthesizeSingleRunAttack(i);
		}
	}
}
