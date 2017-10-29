package sidechannel;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.symbc.numeric.PathCondition;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import sidechannel.util.PathConditionUtils;

/**
 *
 * @author Quoc-Sang Phan <sang.phan@sv.cmu.edu>
 *
 */
public class ConcreteSideChannelListener<Cost> extends AbstractSideChannelListener<Cost,String> {

	public static final int LATTE_MODE = 1; // infix
	public static final int SMT_LIB2_MODE = 2; // prefix
	
	protected int mode = 0;
	
	public ConcreteSideChannelListener(Config config) {
		super(config);
	}
	
	@Override
	protected String format(PathCondition pc){
		String str = PathConditionUtils.toStringFormat(pc, mode);
		/*
		if(DEBUG){
			System.out.println(str);
		}
		//*/
		return str;
	}
	
	protected void printCosts(){
		Iterator<Map.Entry<Cost, HashSet<String>>> it = obsrv
				.entrySet().iterator();

		while (it.hasNext()) {
			Map.Entry<Cost, HashSet<String>> pair = (Map.Entry<Cost, HashSet<String>>) it
					.next();
			Cost cost = pair.getKey();
			HashSet<String> paths = pair.getValue();

			printCost(cost);

			for (String pc : paths) {
				System.out.println(pc);
			}
			System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		}
		System.out.println("");
	}
	
	protected void printCost(Cost cost){}
}
