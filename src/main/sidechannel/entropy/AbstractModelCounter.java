package sidechannel.entropy;

import gov.nasa.jpf.Config;

import java.util.HashMap;
import java.util.HashSet;

import sidechannel.util.SymbolicVariableCollector;

/**
 *
 * @author Quoc-Sang Phan <sang.phan@sv.cmu.edu>
 *
 */
public abstract class AbstractModelCounter <Cost,SymbolicPath> {
	
	protected Config conf;
	protected SymbolicVariableCollector collector;
	
	public AbstractModelCounter(Config conf, SymbolicVariableCollector collector){
		this.conf = conf;
		this.collector = collector;
	}

	public abstract void countAll(HashMap<Cost, HashSet<SymbolicPath>> obsrv, AbstractCounterVisitor<Cost,SymbolicPath> visitor);
	
}
