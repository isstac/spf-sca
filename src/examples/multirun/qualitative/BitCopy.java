package multirun.qualitative;

import gov.nasa.jpf.symbc.Debug;

public class BitCopy implements Problem {

	static int SIZE;

	public int copy(int H, int L) {
		int O;
		if (L > SIZE)
			L = SIZE;
		L = 1 << L - 1;
		O = H & L;
		return O;
	}

	@Override
	public int observe(int h, int l) {
		Debug.assume(h >= 0 && l >= 0);
		return copy(h,l);
	}

	public static void main(String[] args) {
		int run = Integer.parseInt(args[0]);
		// SIZE = Integer.parseInt(args[1]);
		SIZE = 4;
		BitCopy obj = new BitCopy();
		Driver driver = new Driver();
		driver.selfCompose(run, obj);
	}

}
