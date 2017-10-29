package sidechannel.util;

/**
*
* @author Quoc-Sang Phan <sang.phan@sv.cmu.edu>
*
*/
public class SymbolicDomain {
	
	public int min;
	public int max;
	
	public SymbolicDomain(int min, int max){
		if(min > max){
			//TODO: better message handling
			assert false;
		}
		this.min = min;
		this.max = max;
	}
}
