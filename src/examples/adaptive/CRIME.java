package adaptive;

import java.util.Arrays;

import multirun.adaptive.SymbolicAdaptiveExample;
import multirun.adaptive.tree.LZ77T;

/**
*
* @author Quoc-Sang Phan <sang.phan@sv.cmu.edu>
*
*/
public class CRIME extends SymbolicAdaptiveExample{

	static{
		init("src/examples/adaptive/CRIME.jpf");
	}
	
	public static void main(String[] args) {
		CRIME test = new CRIME();
		test.adaptiveAttack(args);
	}
	
	@Override
	public int cost(int[] h1, int[] l1) {
		byte[] h = new byte[h1.length];
		byte[] l = new byte[l1.length];
		
		for(int i = 0; i < h1.length; ++i){
			h[i] = (byte) h1[i];
		}
		
		for(int i = 0; i < l1.length; ++i){
			l[i] = (byte) l1[i];
		}
		
		// byte[] cookie = { 'A', 'A', 'A', 'A', 'A', 'A', 'A', 'A', 'A', 'A', 'A', 'A', 'A', 'A', 'A'};
		byte[] cookie = { 's', 'e', 's', 's', 'i', 'o', 'n', 'k', 'e', 'y', ':'};

		byte[] h_cookie = Arrays.copyOf(h, h.length + cookie.length);
		byte[] l_cookie = Arrays.copyOf(l, l.length + cookie.length);
		System.arraycopy(cookie, 0, h_cookie, h.length, cookie.length);
		System.arraycopy(cookie, 0, l_cookie, l.length, cookie.length);

		final byte[] all = Arrays.copyOf(h_cookie, h_cookie.length
				+ l_cookie.length);
		System.arraycopy(l_cookie, 0, all, h_cookie.length, l_cookie.length);

		try{
			final byte[] compressed = LZ77T.compress(all);
			return compressed.length;
		} catch(Exception e){
			e.printStackTrace();
		}
		return -1;
	}

}
