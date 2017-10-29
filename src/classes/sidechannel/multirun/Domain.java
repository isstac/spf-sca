package sidechannel.multirun;

import java.util.HashMap;
import java.util.Set;
import java.util.Vector;

import modelcounting.domain.Constraint;

/**
*
* @author Quoc-Sang Phan <sang.phan@sv.cmu.edu>
*
*/
public class Domain {
	
	public static HashMap<String,Vector<Set<Constraint>>> domains = null;
		
	native public static void shrinkAfterAssumption();
	
	native public static void setMinMax(int var, int min, int max);
	
}
