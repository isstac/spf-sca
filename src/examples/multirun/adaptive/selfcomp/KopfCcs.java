package multirun.adaptive.selfcomp;

import gov.nasa.jpf.symbc.Debug;
import multirun.adaptive.AbstractSelfCompExample;

/**
 *
 * @author Quoc-Sang Phan <sang.phan@sv.cmu.edu>
 *
 */
public class KopfCcs extends AbstractSelfCompExample{

	/*
	 * An alternative version of foo
	 */
	public int foo1(int h, int l) {
		Debug.assume(h > 0 && h < 6);
		int cost = 0;
		if (l < 5) {
			if (h == 1)
				cost = 1;
			else {
				if (h == 2)
					cost = 2;
				else
					cost = 345;
			}
		} else if (l < 10) {
			if (h == 1)
				cost = 1;
			else {
				if (h == 5)
					cost = 5;
				else
					cost = 234;
			}
		} else {
			if (h < 4)
				cost = 123;
			else
				cost = 45;
		}
		return cost;
	}
	
	@Override
	public int foo(int h, int l) {
		Debug.assume(h > 0 && h < 6);
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
		KopfCcs ex = new KopfCcs();
		ex.run(args);
	}
}
