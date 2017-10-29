package sidechannel.montgomery;

import gov.nasa.jpf.symbc.Debug;

import java.io.IOException;
import java.math.BigInteger;

/**
 *
 * @author Quoc-Sang Phan <sang.phan@sv.cmu.edu>
 *
 */
public class ModMul {
	public static void main(String[] args) throws IOException {
		BigInteger x = makeSymbolicBigInteger("high", 3); 
		// BigInteger y = makeSymbolicBigInteger("low", 3); 
		BigInteger y = new BigInteger("7", 10);
		BigInteger modulus = new BigInteger("1717", 10);
		
		// Do computation
		MontgomeryReducer red = new MontgomeryReducer(modulus);
		BigInteger xm = red.convertIn(x);
		BigInteger zm = red.multiply(xm, red.convertIn(y));
		red.convertOut(zm);
	}
	
	public static BigInteger makeSymbolicBigInteger(String name, int length){
		byte[] val = new byte[length];
		for(int i = 0; i < length; i++){
			val[i] = Debug.makeSymbolicByte(name + i);
		}
		BigInteger integer = new BigInteger(val);
		return integer;
	}
}
