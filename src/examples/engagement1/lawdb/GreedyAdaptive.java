package engagement1.lawdb;

import gov.nasa.jpf.symbc.Debug;
import multirun.adaptive.FullExample;

public class GreedyAdaptive extends FullExample {

	public static void main(String args[]){
		SIZE_HIGH = 1;
		SIZE_LOW = 2;
		int[] h = initSecretInput(args);
		int[] l = initPublicInput(args);
		foo(h, l);		
	}
	
	public static void foo(int[] h, int[] l) {
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
	}
}
