package adaptive;

import multirun.adaptive.SymbolicAdaptiveExample;

/**
*
* @author Quoc-Sang Phan <sang.phan@sv.cmu.edu>
*
*/
public class BinarySearch extends SymbolicAdaptiveExample {

	static{
		init("src/examples/adaptive/BinarySearch.jpf");
	}
	
	// binary search
	public static int foo(int h, int l) {
		if(h >= l){
			return 1;
		}
		return 2;
	}
	
	public static void main(String[] args) {
		BinarySearch test = new BinarySearch();
		test.adaptiveAttack(args);
	}
	
	@Override
	public int cost(int[] h, int[] l) {
		return foo(h[0], l[0]);
	}

}
