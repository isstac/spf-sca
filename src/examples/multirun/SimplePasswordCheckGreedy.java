package multirun;

/**
*
* @author Quoc-Sang Phan <sang.phan@sv.cmu.edu>
*
*/
public class SimplePasswordCheckGreedy extends AbstractNonAdaptiveExample{

	
	static int count = 0;
	
	static {
		init("src/examples/multirun/SimplePasswordCheckGreedy.jpf");
	}
	
	protected boolean check(byte[] secret, byte[] input) {
		count = 0;
		for (int i = 0; i < SIZE; i++){
			if (secret[i] != input[i]){	
				return false;
			}
			count++;
		}
		return true;
	}

	public static void main(String[] args) {

		if (args.length < 1) {
			System.out.println("Need number of runs");
			System.exit(0);
		}
		run = Integer.parseInt(args[0]); // number of runs
		SimplePasswordCheckGreedy tester = new SimplePasswordCheckGreedy();

		if (DEBUG) {
			System.out.println("Number of run is " + run);
		}
		if (greedy) {
			tester.testGreedy();
		} else {
			tester.testFull();
		}
	}

	@Override
	public int foo(byte[] h, byte[] l) {
		check(h,l);
		return count;
	}
}
