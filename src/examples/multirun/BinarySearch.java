package multirun;

import gov.nasa.jpf.symbc.Debug;

public class BinarySearch extends AbstractNonAdaptiveExample{

	public int search(byte h, byte l) {
		Debug.assume(h >= 1 && h <= 10);
		if(h > l){
			return 0;
		}
		return 1;
	}
	
	@Override
	public int foo(byte[] h, byte[] l) {
		return search(h[0], l[0]);
	}

	public static void main(String args[]){
		SIZE = 1;
		if (args.length < 1) {
			System.out.println("Need number of runs");
			System.exit(0);
		}
		run = Integer.parseInt(args[0]); // number of runs
		BinarySearch tester = new BinarySearch();

		if (DEBUG) {
			System.out.println("Number of run is " + run);
		}
		if (greedy) {
			tester.testGreedy();
		} else {
			tester.testFull();
		}
	}
}
