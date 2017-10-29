package sidechannel;

public class TestMemory {

	public void test(int x) {
		int[] a = null;
		if (x > 10) {
			a = new int[20];
		}
		else {
			a = new int[200];
		}
		System.out.println("Array size is " + a.length);
	}
	
	public static void main (String[] args) {
		TestMemory ex = new TestMemory();
		ex.test(1);
	}
}
