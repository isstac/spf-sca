package sidechannel.common;

import java.util.Hashtable;

import sidechannel.util.SymbolicDomain;

/**
*
* @author Quoc-Sang Phan <sang.phan@sv.cmu.edu>
*
*/
public class GlobalVariables {

	public static Hashtable<String, SymbolicDomain> domains = new Hashtable<String, SymbolicDomain>();
}
