package engagement1.gabfeed.math;

import java.math.BigInteger;

import gov.nasa.jpf.symbc.Debug;

public class Driver {

    public static int LENGTH = 1;
    
    public static void testModPow(){
        String modp1536 = "FFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B80DC1CD129024E088A67CC74020BBEA63B139B22514A08798E3404DDEF9519B3CD3A431B302B0A6DF25F14374FE1356D6D51C245E485B576625E7EC6F44C42E9A637ED6B0BFF5CB6F406B7EDEE386BFB5A899FA5AE9F24117C4B1FE649286651ECE45B3DC2007CB8A163BF0598DA48361C55D39A69163FA8FD24CF5F83655D23DCA3AD961C62F356208552BB9ED529077096966D670C354E4ABC9804F1746C08CA237327FFFFFFFFFFFFFFFF";
        BigInteger modulus = new BigInteger(modp1536, 16);  
        // BigInteger modulus = new BigInteger("1717", 10);
        // BigInteger base = new BigInteger("846", 10);//makeSymbolicBigInteger("base", LENGTH); 
        String basestr = "FFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B80DC1CD129024E088A67CC74020BBEA63B139B22514A08798E3404DDEF9519B3CD3A431B302B0A6DF25F14374FE1356D6D51C245E485B576625E7EC6F44C42E9A637ED6B0BFF5CB6F406B7EDEE386BFB5A899FA5AE9F24117C4B1FE649286651ECE45B3DC2007CB8A163BF0598DA48361C55D39A69163FA8FD24CF5F83655D23DCA3AD961C62F356208552BB9ED529077096966D670C354E4ABC9804F1746C08CA237327FFFFFFFFFFFFFFFA";
        BigInteger base = new BigInteger(basestr, 16);
        BigInteger exponent = Debug.makeSymbolicBigInteger("h", LENGTH); 
        ModPow.modPowNoNoise(base, exponent, modulus);
        System.out.println(Debug.getPC_prefix_notation());
    }
    
    public static void main(String[] args){
        LENGTH = Integer.parseInt(args[0]);
        // testModPow();
        // testModMul();
        // testLessThan();
        testLessThanInteger();

    }
    
    
    public static void testModMul(){
        String modp1536 = "FFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B80DC1CD129024E088A67CC74020BBEA63B139B22514A08798E3404DDEF9519B3CD3A431B302B0A6DF25F14374FE1356D6D51C245E485B576625E7EC6F44C42E9A637ED6B0BFF5CB6F406B7EDEE386BFB5A899FA5AE9F24117C4B1FE649286651ECE45B3DC2007CB8A163BF0598DA48361C55D39A69163FA8FD24CF5F83655D23DCA3AD961C62F356208552BB9ED529077096966D670C354E4ABC9804F1746C08CA237327FFFFFFFFFFFFFFFF";
        BigInteger modulus = new BigInteger(modp1536, 16);  
        BigInteger x = Debug.makeSymbolicBigInteger("x", LENGTH); 
        BigInteger y = Debug.makeSymbolicBigInteger("y", LENGTH); 
        OptimizedMultiplier.fastMultiply(x, y).mod(modulus);
    }
    
	public static void testLessThan() {
		BigInteger x = Debug.makeSymbolicBigInteger("x", 1);
		BigInteger y = Debug.makeSymbolicBigInteger("y", 1);

		int r = x.compareTo(y);

		if (r > 0) {
			System.out.println("greater");
		} else {
			System.out.println("lessoreq");
		}
		return;
	}


    public static void testLessThanInteger(){
      
      int x = Debug.makeSymbolicInteger("x");
      int y = Debug.makeSymbolicInteger("y");

      if (x == y) {
        System.out.println("greater");
              return;
      }
      else{
        System.out.println("lessoreq"); 
        return;
        //  if (x < y) {
        //   System.out.println("greater");
        // }
        //   else{
        //   System.out.println("lessoreq"); 
        // }
      }

    }
    
    public static void test(){
        // String modp1536 = "FFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B80DC1CD129024E088A67CC74020BBEA63B139B22514A08798E3404DDEF9519B3CD3A431B302B0A6DF25F14374FE1356D6D51C245E485B576625E7EC6F44C42E9A637ED6B0BFF5CB6F406B7EDEE386BFB5A899FA5AE9F24117C4B1FE649286651ECE45B3DC2007CB8A163BF0598DA48361C55D39A69163FA8FD24CF5F83655D23DCA3AD961C62F356208552BB9ED529077096966D670C354E4ABC9804F1746C08CA237327FFFFFFFFFFFFFFFF";
        // BigInteger modulus = new BigInteger(modp1536, 16);   
        BigInteger modulus = new BigInteger("1717", 10);
        BigInteger x = new BigInteger("846", 10);//makeSymbolicBigInteger("base", LENGTH); 
        // String basestr = "FFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B80DC1CD129024E088A67CC74020BBEA63B139B22514A08798E3404DDEF9519B3CD3A431B302B0A6DF25F14374FE1356D6D51C245E485B576625E7EC6F44C42E9A637ED6B0BFF5CB6F406B7EDEE386BFB5A899FA5AE9F24117C4B1FE649286651ECE45B3DC2007CB8A163BF0598DA48361C55D39A69163FA8FD24CF5F83655D23DCA3AD961C62F356208552BB9ED529077096966D670C354E4ABC9804F1746C08CA237327FFFFFFFFFFFFFFFA";
        // BigInteger x = new BigInteger(basestr, 16);
        BigInteger y = Debug.makeSymbolicBigInteger("exponent", LENGTH); 
        
        MontgomeryReducer red = new MontgomeryReducer(modulus);
        BigInteger xm = red.convertIn(x);
        System.out.println(">>>>> After convertIn(x)");
        BigInteger zm;
        // BigInteger z;
        zm = red.multiply(xm, red.convertIn(y));
        System.out.println(">>>>> After red.multiply(xm, red.convertIn(y)");
        red.convertOut(zm);
        //  z = x.multiply(y).mod(modulus);
        /*
        if (!red.convertOut(zm).equals(z))
            throw new AssertionError("Self-check failed");
        System.out.printf("%d%s%d mod %d%n", x, oper.equals("times") ? " * " : "^", y, mod);
        //*/
        // System.out.println("= " + z);
    }
    
}
