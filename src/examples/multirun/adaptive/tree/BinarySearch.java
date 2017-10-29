package multirun.adaptive.tree;

import multirun.adaptive.FullExample;
import sidechannel.multirun.Observable;

/**
 *
 * @author Quoc-Sang Phan <sang.phan@sv.cmu.edu>
 *
 */
public class BinarySearch extends FullExample{

	public static int foo(int h, int l) {
		if(h >= l){
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
