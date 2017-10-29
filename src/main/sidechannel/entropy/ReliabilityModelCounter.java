package sidechannel.entropy;

import gov.nasa.jpf.Config;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import sidechannel.util.ModelCounter;
import sidechannel.util.SymbolicVariableCollector;

/**
 *
 * @author Quoc-Sang Phan <sang.phan@sv.cmu.edu>
 *
 */
public class ReliabilityModelCounter<Cost> extends AbstractModelCounter <Cost,String>{
	
	private final int LATTE = 0;
	private final int BARVINOK = 1;
	
	private int modelCounter = LATTE;

	public ReliabilityModelCounter(Config conf, SymbolicVariableCollector collector){
		super(conf,collector);
		if(conf.getProperty("symbolic.counter","latte").trim().equals("barvinok")){
			modelCounter = BARVINOK;
		}
	}
	
	@Override
	public void countAll(HashMap<Cost, HashSet<String>> obsrv, AbstractCounterVisitor<Cost,String> visitor) {
		
		Iterator<Map.Entry<Cost, HashSet<String>>> it = obsrv.entrySet()
				.iterator();
		ModelCounter counter = new ModelCounter(conf,collector);
						
		while (it.hasNext()) {
			Map.Entry<Cost, HashSet<String>> pair = (Map.Entry<Cost, HashSet<String>>) it
					.next();
			
			Cost cost = pair.getKey();
			HashSet<String> paths = pair.getValue();
			
			BigInteger block;
			if(modelCounter == BARVINOK){
				block = counter.countBarvinok(paths);
			} else{
				// model counter is Latte
				block = counter.count(paths);
			}
			
			visitor.visit(block, cost, paths);
		}
		
		// ConfigUtils.cleanReliabilityTmpDir(conf);
	}

}
