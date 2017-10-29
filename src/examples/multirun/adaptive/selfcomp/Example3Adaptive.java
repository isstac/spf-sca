package multirun.adaptive.selfcomp;

import gov.nasa.jpf.symbc.Debug;
import multirun.adaptive.AbstractSelfCompExample;

/**
 *
 * @author Quoc-Sang Phan <sang.phan@sv.cmu.edu>
 *
 */
public class Example3Adaptive extends AbstractSelfCompExample{
	
	@Override
	public int foo(int h, int l)
	{
		int cost;
		Debug.assume(l < 3);
		if (l == 0) {
			if (h < 2)
				cost = 1;
			else
				cost = 2;
		} else if (l == 1) {
			if (h < 3)
				cost = 3;
			else
				cost = 4;
		} else {// l==2
			if (h < 4)
				cost = 5;
			else
				cost = 6;
		}
		return cost;
	}
	
	public static void main(String[] args) {
		Example3Adaptive ex = new Example3Adaptive();
		ex.run(args);
	}
	
}
