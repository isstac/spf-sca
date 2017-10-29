package sidechannel;

import java.math.BigInteger;

public class TestBigInteger {

	public static BigInteger test(byte[] message) {
		
		BigInteger m = new BigInteger(message);
		BigInteger result = m.modPow(new BigInteger(new byte[]{1,2,3}), new BigInteger(new byte[]{4,5,6}));
		// Debug.printPC("");
		return result;
	}
	
	public static void main (String[] args){
		byte[] message = new byte[3];

		System.out.println("Encrypted message is " + test(message).toString());
	}
}
