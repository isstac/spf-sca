package engagement1.lawdb;

import gov.nasa.jpf.symbc.Debug;
import multirun.adaptive.SymbolicAdaptiveExample;

/**
 *
 * @author Quoc-Sang Phan <sang.phan@sv.cmu.edu>
 *
 */
public class AdaptiveAttack extends SymbolicAdaptiveExample {

    static{
    	init("src/examples/engagement1/lawdb/adaptiveLawDB.jpf");
    	SIZE_LOW = 2;
    	customized = false;
    }
	
    public static void main(String[] args) {
    	AdaptiveAttack test = new AdaptiveAttack();
		test.adaptiveAttack(args);
	}
    
	@Override
	public int cost(int[] h, int[] l) {
		BTree tree = new BTree(10);
		CheckRestrictedID checker = new CheckRestrictedID();
	
		// create two concrete unrestricted ids
		int id1 = 64, id2 = 85;
		tree.add(id1, null, false);
		tree.add(id2, null, false);
		
		// create one symbolic restricted id
		Debug.assume(h[0]!=id1 && h[0]!=id2);
		tree.add(h[0], null, false);
		checker.add(h[0]);
		
		
		UDPServerHandler handler = new UDPServerHandler(tree,checker);
		int key = Debug.makeSymbolicInteger("key");
		handler.channelRead0(8,key,l[0],l[1]);
		return 0;
	}

}
