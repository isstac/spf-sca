package sidechannel;

import gov.nasa.jpf.symbc.Debug;

// taken from the BouncyCastle implementation

public class Salsa20 {

	/**
	 * Salsa20 function
	 *
	 * @param input
	 *          input data
	 */
	public static boolean salsaCore(int rounds, int[] input, int[] x) {
		if (input.length != 16) {
			throw new IllegalArgumentException();
		}
		if (x.length != 16) {
			throw new IllegalArgumentException();
		}
		if (rounds % 2 != 0) {
			throw new IllegalArgumentException("Number of rounds must be even");
		}

		int x00 = input[0];
		int x01 = input[1];
		int x02 = input[2];
		int x03 = input[3];
		int x04 = input[4];
		int x05 = input[5];
		int x06 = input[6];
		int x07 = input[7];
		int x08 = input[8];
		int x09 = input[9];
		int x10 = input[10];
		int x11 = input[11];
		int x12 = input[12];
		int x13 = input[13];
		int x14 = input[14];
		int x15 = input[15];

		for (int i = rounds; i > 0; i -= 2) {
			x04 ^= rotl(x00 + x12, 7);
			x08 ^= rotl(x04 + x00, 9);
			x12 ^= rotl(x08 + x04, 13);
			x00 ^= rotl(x12 + x08, 18);
			x09 ^= rotl(x05 + x01, 7);
			x13 ^= rotl(x09 + x05, 9);
			x01 ^= rotl(x13 + x09, 13);
			x05 ^= rotl(x01 + x13, 18);
			x14 ^= rotl(x10 + x06, 7);
			x02 ^= rotl(x14 + x10, 9);
			x06 ^= rotl(x02 + x14, 13);
			x10 ^= rotl(x06 + x02, 18);
			x03 ^= rotl(x15 + x11, 7);
			x07 ^= rotl(x03 + x15, 9);
			x11 ^= rotl(x07 + x03, 13);
			x15 ^= rotl(x11 + x07, 18);

			x01 ^= rotl(x00 + x03, 7);
			x02 ^= rotl(x01 + x00, 9);
			x03 ^= rotl(x02 + x01, 13);
			x00 ^= rotl(x03 + x02, 18);
			x06 ^= rotl(x05 + x04, 7);
			x07 ^= rotl(x06 + x05, 9);
			x04 ^= rotl(x07 + x06, 13);
			x05 ^= rotl(x04 + x07, 18);
			x11 ^= rotl(x10 + x09, 7);
			x08 ^= rotl(x11 + x10, 9);
			x09 ^= rotl(x08 + x11, 13);
			x10 ^= rotl(x09 + x08, 18);
			x12 ^= rotl(x15 + x14, 7);
			x13 ^= rotl(x12 + x15, 9);
			x14 ^= rotl(x13 + x12, 13);
			x15 ^= rotl(x14 + x13, 18);
		}

		x[0] = x00 + input[0];
		x[1] = x01 + input[1];
		x[2] = x02 + input[2];
		x[3] = x03 + input[3];
		x[4] = x04 + input[4];
		x[5] = x05 + input[5];
		x[6] = x06 + input[6];
		x[7] = x07 + input[7];
		x[8] = x08 + input[8];
		x[9] = x09 + input[9];
		x[10] = x10 + input[10];
		x[11] = x11 + input[11];
		x[12] = x12 + input[12];
		x[13] = x13 + input[13];
		x[14] = x14 + input[14];
		x[15] = x15 + input[15];
		
		
		//MAB added a simple assertion here
		if (x[6] > x[5]) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Rotate left
	 *
	 * @param x
	 *          value to rotate
	 * @param y
	 *          amount to rotate x
	 *
	 * @return rotated x
	 */
	protected static int rotl(int x, int y) {
		return (x << y) | (x >>> -y);
	}
	
	public static void main(String[] args) {
		int[] input = new int[16];
		int[] output = new int[16];
		
		for (int i = 0; i < input.length; i++) {
			input[i] = Debug.makeSymbolicInteger("PWD"+i);
		}
		
		salsaCore(2, input, output);
	}
}
