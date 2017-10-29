package multirun.adaptive.tree;

import multirun.adaptive.FullExample;
import sidechannel.multirun.Observable;

public class SegmentedPasswordCheck extends FullExample {

static int count = 0;
	
	static {
		init("src/examples/multirun/adaptive/tree/SegmentedPasswordCheck.jpf");
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
