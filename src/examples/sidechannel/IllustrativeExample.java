package sidechannel;

public class IllustrativeExample {
	
	public void foo(int H) {
		if (H < 3) {
			System.out.println("This block take one instruction");
		} else {
			System.out.print("This block take");
			System.out.println("instructions");
		}
	}
	
	public static void main (String args[]){
		IllustrativeExample ie = new IllustrativeExample();
		ie.foo(3);
	}
}
