package multirun.qualitative;

import gov.nasa.jpf.symbc.Debug;

public class PasswordCheck implements Problem{
	
	static int MAX;

	int check(int H, int L) {
		int O;
		if (H == L)
			O = 1;
		else
			O = 0;
		return O;
	}

	public static void main(String[] args) {
		int run = Integer.parseInt(args[0]);
		MAX = Integer.parseInt(args[1]);
		PasswordCheck checker = new PasswordCheck();
		Driver driver = new Driver();
		driver.selfCompose(run, checker);
	}

	@Override
	public int observe(int h, int l) {
		Debug.assume(h >= 0 && l >= 0 && h < MAX);
		return check(h,l);
	}
}
