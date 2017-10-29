package adaptive;

import multirun.adaptive.SymbolicAdaptiveExample;

public class ArrayEquals extends SymbolicAdaptiveExample{

static int count = 0;
	
	static{
		init("src/examples/adaptive/ArrayEquals.jpf");
	}
	
	public static boolean equals(int[] a, int[] a2) {
		count = 0;
        if (a==a2)
            return true;
        if (a==null || a2==null)
            return false;

        int length = a.length;
        if (a2.length != length)
            return false;

        for (int i=0; i<length; i++){
            if (a[i] != a2[i]){
                return false;
            }
            ++count;
        }

        return true;
    }

	public static void main(String[] args) {
		ArrayEquals test = new ArrayEquals();
		test.adaptiveAttack(args);
	}
	
	@Override
	public int cost(int[] h, int[] l) {
		ArrayEquals.equals(h,l);
		return count;
	}
}
