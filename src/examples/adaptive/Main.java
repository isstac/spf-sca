/**
* @author corina pasareanu corina.pasareanu@sv.cmu.edu
*
*/

package adaptive;

import gov.nasa.jpf.symbc.Debug;
import sidechannel.multirun.Observable;

public class Main {

	static int f1(int high,int low) {
		if(high==low)
			return 1;
		else
			return 2;
	}
	static int f2(int high,int low) {
		if(high>=low)
			return 1;
		else
			return 2;
	}
	
	static int fKopf(int h, int l) {
		int cost = 0;
		if (l < 5) {
			if (h == 1)
				cost = 1;
			else {
				if (h == 2)
					cost = 2;
				else
					cost = 3;
			}
		} else if (l < 10) {
			if (h == 1)
				cost = 1;
			else {
				if (h == 5)
					cost = 2;
				else
					cost = 4;
			}
		} else {
			if (h < 4)
				cost = 3;
			else
				cost = 4;
		}
		return cost;
	}
	static int k=2;
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int H=Debug.makeSymbolicInteger("h");
		int L;
		String cost="";
		for(int i=1;i<=k;i++) {
			L=Debug.makeSymbolicInteger("l"+cost+"_0");
			int tmp = fKopf(H,L);
			cost=cost+""+tmp;
			Observable.add(tmp);
			//System.out.println("cost at "+i+" is "+cost);
		}
		System.out.println("cost "+cost);
		// System.out.println("(define-fun PC () Bool "+Debug.getPC_prefix_notation()+")");	
	}

}
