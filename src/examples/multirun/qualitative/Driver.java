package multirun.qualitative;

import gov.nasa.jpf.symbc.Debug;

public class Driver {
	
	static int a(int x) {
		return 2 * x;
	}

	static int b(int x) {
		return 2 * x + 1;
	}

	public void selfCompose(int k, Problem prob){
		int N = 2 * k;
		int l[] = new int[k];
		int h[] = new int[N];
		int i, j;
		// init array. Can be safely removed
		for (i = 0; i < k; i++) {
			l[i] = Debug.makeSymbolicInteger("MUL_SYM_L_" + i);
		}
		for (i = 0; i < N; i++) {
			h[i] = Debug.makeSymbolicInteger("MUL_SYM_H_" + i);
		}
		// assume
		for (i = 0; i < k - 1; i++) {
			Debug.assume(prob.observe(h[a(i)], l[i]) != prob.observe(h[b(i)], l[i]));
			for (j = i + 1; j < k; j = j + 1){
				Debug.assume(prob.observe(h[a(j)], l[i]) == prob.observe(h[b(j)], l[i]));
			}
		}
		// assert
		i = k - 1;
		assert (prob.observe(h[a(i)], l[i]) == prob.observe(h[b(i)], l[i]));
	}
}
