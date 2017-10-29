package multirun.adaptive.minimax;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import multirun.adaptive.MiniMaxExample;
import sidechannel.multirun.Observable;

/**
 *
 * @author Quoc-Sang Phan <sang.phan@sv.cmu.edu>
 *
 */
public class SimplePasswordCheck extends MiniMaxExample{

	static int count = 0;
	
	static {
		// Hack for test: initialize the bounds on the secret from .jpf file
		String line = null;
		int inputSize = 1;
		try {
			
			FileInputStream fstream = new FileInputStream(
					"src/examples/multirun/adaptive/minimax/SimplePasswordCheck.jpf");
			BufferedReader br = new BufferedReader(new InputStreamReader(
					fstream));

			line = br.readLine();

			while (line != null) {

				if (line.contains("sidechannel.high_input_size") && line.trim().charAt(0) != '#') {
					String value = line.split("=")[1].trim();
					inputSize = Integer.parseInt(value);
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
		}
	}

	static boolean check(int[] secret, int[] input) {
		count = 0;
		for (int i = 0; i < SIZE_HIGH; i++){
			if (secret[i] != input[i]){	
				return false;
			}
			count++;
		}
		return true;
	}
	
	public static void main (String args[]){
		
		int[] h = initSecretInput(args);
		int[] l = initPublicInput(args);
		
		long cost = 0;
		
		check(h, l);
		cost = count;
		// Debug.printPC("\n\n>>>>> PC is\n");
		
		Observable.add(cost);
	}
}
