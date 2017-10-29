package sidechannel;

import gov.nasa.jpf.symbc.Debug;

/**
 * Lucas's password program
 *
 * @author Quoc-Sang Phan <sang.phan@sv.cmu.edu>
 *
 */
public class SecurePasswordChecker {

	public static int SIZE = 4; // a credit card with 4-digit pin
	int[] pin;

	public SecurePasswordChecker() {
		pin = new int[SIZE];
		for (int i = 0; i < SIZE; i++) {
			pin[i] = Debug.makeSymbolicInteger("PWD" + i);
		}
	}

	public boolean check(int[] input) {
		boolean matched = true;
		int i = 0;

		// dummy variable
		int j = 0;

		while (i < SIZE) {
			if (pin[i] != input[i])
				matched = false;
			//
			else {
				j = 0;
				++j;
			}
			i++;
		}
		return matched;
	}
	
	public boolean check2(int[] input) {
		boolean matched = true;
		int i = 0;

		// dummy variable
		int j = 0;

		while (i < SIZE) {
			if (pin[i] != input[i])
				matched = false;
			//
			else {
				// dummy instructions to waste time, and make the paths equal
				//j = 0;
				//++j;
			}
			i++;
		}
		return matched;
	}

	public static void main(String args[]) {
		SecurePasswordChecker checker = new SecurePasswordChecker();
		int[] input = { 1, 2, 3, 4 };
		checker.check(input);
	}
}
