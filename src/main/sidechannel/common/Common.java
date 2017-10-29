package sidechannel.common;

/**
*
* @author Quoc-Sang Phan <sang.phan@sv.cmu.edu>
*
*/
public class Common {

	// $ was chosen, because it is not used in Mathematica
	// public static final String SEPARATOR = "$";
	public static final String SEPARATOR = "_";
	public static final String ADAPTIVE_SEPARATOR = "$";
	
	public static final String CONSTRAINT_PREFIX = "constraints";
	public static final String INPUT_PREFIX = "input";
	
	public static int indexOf(String lowInput){
		int pos = lowInput.indexOf(SEPARATOR);
		return Integer.parseInt(lowInput.substring(pos + 1));
	}
}
