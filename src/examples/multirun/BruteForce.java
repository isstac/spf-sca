/**
* @author corina pasareanu corina.pasareanu@sv.cmu.edu
*
*/

package multirun;



public class BruteForce {
	        static int MAX_HIGH= 15;
			static int MIN_HIGH=1;

			static int modPowFastKocherReduction(int num, int e, int m){

					int s = 1;
					int y = num;
					int res = 0;
					int count = 0;

					// __CPROVER_assume(e <= MAX_HIGH && e >= MIN_HIGH);
                    
					while (e > 0) {
					//for (int j = 0; j < 5; j++){  //4 i.e. the number of bits+1
						if (e % 2 == 1) {
							// res = (s * y) % m;
							// reduction:
							int tmp = s * y;
							if (tmp > m) {
								tmp = tmp - m;
								count++;
							}
							res = tmp % m;
							count++;
						} else {
							res = s;
							count++;
						}
						s = (res * res) % m; // squaring the base
						e /= 2;
						count++;
						//if(e==0)break;
					}
					//System.out.println("j "+j+" e "+e);
					assert (e == 0);
					// return res;
			  return count;
			}
	public static void main(String[] args) {
		for (int l=MIN_HIGH;l<=MAX_HIGH;l++) {
		//int l = 5; //cost 6
			for (int h=MIN_HIGH;h<=MAX_HIGH;h++)
				System.out.println("low "+l+" high "+h+" cost "+modPowFastKocherReduction(l, h, 33));
			System.out.println("***");	
		}
	
	}
}
