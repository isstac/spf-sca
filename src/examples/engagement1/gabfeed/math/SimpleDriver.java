package engagement1.gabfeed.math;

import gov.nasa.jpf.symbc.Debug;

public class SimpleDriver {
    public static int BIT_LEN=3; // size of exponent, i.e. secret
	public static void main(String[] args){	
		long modulus = 1717;//Debug.makeSymbolicLong("modulus");
		long base = Debug.makeSymbolicLong("base"); 
		long exponent = Debug.makeSymbolicLong("exponent"); 
		
		
		//System.out.println("simple "+SimpleModPow.modPowSimple(base, exponent, modulus));
		System.out.println("noisy "+SimpleModPow.modPowNoise(base, exponent, modulus));
	}
}
