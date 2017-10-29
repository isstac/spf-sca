package adaptive;

import multirun.adaptive.SymbolicAdaptiveExample;

public class Password  extends SymbolicAdaptiveExample {
	
	static{
		init("src/examples/adaptive/Password.jpf");
	}
	
	// binary search
	public static int check(int h, int l) {
		if(h != l){
			return 0;
		}
		return 1;
	}
	
	public static void main(String[] args) {
		Password test = new Password();
		test.adaptiveAttack(args);
	}
	
	@Override
	public int cost(int[] h, int[] l) {
		return check(h[0], l[0]);
	}

}
