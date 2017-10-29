package multirun.adaptive;

import gov.nasa.jpf.symbc.Debug;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import sidechannel.common.Common;
import sidechannel.multirun.Observable;

/**
*
* @author Quoc-Sang Phan <sang.phan@sv.cmu.edu>
*
*/
public abstract class SymbolicAdaptiveExample {

	protected static int RUN = 1;
	protected static int SIZE_HIGH = 1;
	protected static int SIZE_LOW = 1;
	protected static boolean customized = true;
	
	public abstract int cost(int[] h, int[] l);
	
	protected static void init(String fileName){
		String line = null;
		int inputSize = 1;
		int numOfRuns = 1;
		try {
			
			FileInputStream fstream = new FileInputStream(fileName);
			BufferedReader br = new BufferedReader(new InputStreamReader(
					fstream));

			line = br.readLine();
			
			String value;

			while (line != null) {

				if (line.contains("sidechannel.high_input_size") && line.trim().charAt(0) != '#') {
					value = line.split("=")[1].trim();
					inputSize = Integer.parseInt(value);
				}
				
				if (line.contains("multirun.num_run") && line.trim().charAt(0) != '#') {
					value = line.split("=")[1].trim();
					numOfRuns = Integer.parseInt(value);
				}
				
				line = br.readLine();
			}
			br.close();
		} catch (Exception e) {
			System.out.println("Error is in >>>>>" + line + "<<<<<");
			e.printStackTrace();
		} finally {
			SIZE_HIGH = inputSize;
			SIZE_LOW = inputSize;
			RUN = numOfRuns;
			System.out.println(">>>>> Secret size is " + SIZE_HIGH);
			System.out.println(">>>>> Number of run is " + RUN);
		}
	}

	public void adaptiveAttack(String[] args) {
		if(args.length > 0){
			RUN = Integer.parseInt(args[0]);
		}
		int[] h = makeSymbolicArray("h", SIZE_HIGH);
		StringBuilder sb = new StringBuilder();
		for (int i = 1; i <= RUN; i++) {
			String label = "l" + i + sb.toString(); 
			int[] l = makeSymbolicArray(label, SIZE_LOW);
			int cost = cost(h, l);
			if(customized){
				Observable.add(cost);
			} else {
				cost = (int) Observable.getCost();
			}
			sb.append(Common.ADAPTIVE_SEPARATOR + cost);
		}
	}
	
	int[] makeSymbolicArray(String label, int size){
		int[] array = new int[size];
		for(int i = 0; i < size; ++i){
			array[i] = Debug.makeSymbolicInteger(label + Common.SEPARATOR + i);
		}
		return array;
	}
}
