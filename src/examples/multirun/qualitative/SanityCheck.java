package multirun.qualitative;

public class SanityCheck implements Problem {
	
	int func(int H, int L) {
		int O;
		if (H < 16)
			  O = L + H;
		else 
			  O = L;
		//System.out.println("out "+O);
		return O;
	}
	
	public static void main(String[] args){
		int run = Integer.parseInt(args[0]);
		SanityCheck checker = new SanityCheck();
		Driver driver = new Driver();
		driver.selfCompose(run, checker);
	}
	

	@Override
	public int observe(int h, int l) {
		return func(h,l);
	}
}