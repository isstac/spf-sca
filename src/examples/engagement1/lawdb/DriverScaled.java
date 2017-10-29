package engagement1.lawdb;

import gov.nasa.jpf.symbc.Debug;

public class DriverScaled {

	public static void main(String[] args){
		BTree tree = new BTree(10);
		CheckRestrictedID checker = new CheckRestrictedID();

    int h = Debug.makeSymbolicInteger("h");

    int min = Integer.parseInt(args[0]);
    int max = Integer.parseInt(args[1]);
    int numberOfKeys = Integer.parseInt(args[2]);

		int stepSize = (max - min) / numberOfKeys;

    for(int key = min; key <= max; key += stepSize) {
//      Debug.assume(h != key);
      if(key == h) {
        return;
      }
      tree.add(key, null, false);
    }
    tree.add(h, null, false);
    checker.add(h);

		UDPServerHandler handler = new UDPServerHandler(tree,checker);
		int key = Debug.makeSymbolicInteger("key");
		handler.channelRead0(8,key,50,100);
		int noise = Debug.makeSymbolicInteger("noise");

		if(noise > 50){
			// do something to waste cycle
			int count = 0;
			for(int i = 0; i < 100; ++i){
				++count;
			}
		}
	}
}
