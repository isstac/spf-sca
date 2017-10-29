package sidechannel;

/**
 * Electronic Purse example from Rybalchenko's oakland paper
 *
 * @author Quoc-Sang Phan <sang.phan@sv.cmu.edu>
 *
 */
public class ElectronicPurse {

	public void test(int high, int low) {
		
		// int lo = 0;
		
		while (high >= low) {
			high = high - low;
			
			// we don't need this since we observe the time
			// lo = lo + 1;
		}
	}
	
	public static void main (String args[]){
		ElectronicPurse ep = new ElectronicPurse();
		// in the paper, the author only consider when l is 5
		ep.test(25, 5);
	}
}
