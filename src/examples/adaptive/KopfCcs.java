package adaptive;

import multirun.adaptive.SymbolicAdaptiveExample;

/**
*
* @author Quoc-Sang Phan <sang.phan@sv.cmu.edu>
*
*/
public class KopfCcs extends SymbolicAdaptiveExample {

	static{
		init("src/examples/adaptive/KopfCcs.jpf");
	}
	
	// Kopf's example in CCS
	static int foo(int h, int l) {
		int cost = 0;
		if (l < 5) {
			if (h == 1)
				cost = 1;
			else {
				if (h == 2)
					cost = 2;
				else
					cost = 3;
			}
		} else if (l < 10) {
			if (h == 1)
				cost = 1;
			else {
				if (h == 5)
					cost = 2;
				else
					cost = 4;
			}
		} else {
			if (h < 4)
				cost = 3;
			else
				cost = 4;
		}
		return cost;
	}

	public static void main(String[] args) {
		KopfCcs test = new KopfCcs();
		test.adaptiveAttack(args);
	}

	@Override
	public int cost(int[] h, int[] l) {
		return foo(h[0],l[0]);
	}
}
