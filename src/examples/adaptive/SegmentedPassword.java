package adaptive;

import multirun.adaptive.SymbolicAdaptiveExample;

public class SegmentedPassword extends SymbolicAdaptiveExample {

	static int count = 0;
	
	static{
		init("src/examples/adaptive/SegmentedPassword.jpf");
	}
	
	protected boolean check(int[] secret, int[] input) {
		assert(SIZE_HIGH == SIZE_LOW);
		count = 0;
		for (int i = 0; i < SIZE_HIGH; i++){
			if (secret[i] != input[i]){	
				return false;
			}
			count++;
		}
		return true;
	}

	public static void main(String[] args) {
		SegmentedPassword test = new SegmentedPassword();
		test.adaptiveAttack(args);
	}
	
	@Override
	public int cost(int[] h, int[] l) {
		check(h,l);
		return count;
	}

}
