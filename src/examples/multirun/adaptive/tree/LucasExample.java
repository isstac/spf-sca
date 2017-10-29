package multirun.adaptive.tree;

import gov.nasa.jpf.symbc.Debug;
import multirun.adaptive.FullExample;
import sidechannel.multirun.Observable;

/**
* 
* @author Quoc-Sang Phan <sang.phan@sv.cmu.edu>
* 
* Lucas' version of Binary Search
*/
public class LucasExample extends FullExample {
	public static int foo(int h, int l) {
		Debug.assume(h >= 1 && h <= 3);
		if(h == l){
			return 0;
		}
		if(h < l) {
			return 1;
		}
		return 2;
	}
	
	public static void main(String args[]){
		SIZE_HIGH = 1;
		SIZE_LOW = 1;
		int[] h = initSecretInput(args);
		int[] l = initPublicInput(args);
		long cost = foo(h[0], l[0]);		
		Observable.add(cost);
	}
}
