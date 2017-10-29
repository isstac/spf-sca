package sidechannel.multirun.adaptive;

import java.util.HashMap;
import java.util.HashSet;

import gov.nasa.jpf.Config;
import sidechannel.util.SymbolicVariableCollector;

public abstract class PartitionGenerator <Cost> {
	
	protected Config conf;
	protected AbstractPartitionVisitor<Cost> visitor;
	protected HashMap<String, Cost> map;
	protected boolean LINEAR = false;
	
	public PartitionGenerator(Config config, AbstractPartitionVisitor<Cost> visitor, boolean linear){
		this.conf = config;
		this.visitor = visitor;
		LINEAR = linear;
	}
	
	public abstract void computePartitions(HashMap<Cost, HashSet<String>> obsrv, SymbolicVariableCollector collector);
}
