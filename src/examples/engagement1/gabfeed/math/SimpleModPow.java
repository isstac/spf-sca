package engagement1.gabfeed.math;

import java.math.BigInteger;
import java.util.Random;

import gov.nasa.jpf.symbc.Debug;

public class SimpleModPow {
	
	public static long modPowSimple(long base, long exponent,
			long modulus) {
		long s = 1;
		int width = bitLength(exponent);
		int i = 0;

		while (i < width) {		
			Debug.assume(width < 3);
			
			s = (s * s ) % modulus;
			if (testBit(exponent, width - i - 1)) {
				s =  (s * base) % modulus;
			}
			++i;
		}
		return s;
	}
	
	public static long modPowNoise(long base, long exponent, long modulus) {
		//noise1 between 0..1, noise2 between 0..1
        long s = 1;
        final int width = bitLength(exponent);
        int i = 0;
        boolean noise1,noise2;
        while (i < width) {
            //final Random randomNumberGeneratorInstance = new Random();
            //while (i < width && randomNumberGeneratorInstance.nextDouble() < 0.5) {
                //while (i < width && randomNumberGeneratorInstance.nextDouble() < 0.5) {
        	while (i < width && (noise1=Debug.makeSymbolicBoolean("noise1")) ==false) {
        		while (i < width && (noise2=Debug.makeSymbolicBoolean("noise2")) ==false) {
                    s = (s*s) % modulus;
                    if (testBit(exponent, width - i - 1)) {
                        s = (s * base) % modulus;
                    }
                    ++i;
                }
            }
        }
        return s;
    }
	
	/*
	 * test bit at position i of h
	 */
	public static boolean testBit(long h, int i){
		return ((h & (1<< i)) != 0);
	}
	
	/*
	 * compute bit length of h
	 */
	public static int bitLength(long h){
		return SimpleDriver.BIT_LEN;//(int) Math.ceil(log2(h < 0 ? -h : h+1));
	}
	
	public static double log2(long h){
		return Math.log(h) / Math.log(2);
	}
}
