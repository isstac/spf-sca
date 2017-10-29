package multirun.adaptive.minimax;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

import multirun.adaptive.MiniMaxExample;
import sidechannel.multirun.Observable;

public class CRIMESimple extends MiniMaxExample{
	
	static {
		// Hack for test: initialize the bounds on the secret from .jpf file
		String line = null;
		int inputSize = 1;
		try {
			
			FileInputStream fstream = new FileInputStream(
					"src/examples/multirun/adaptive/minimax/CRIMESimple.jpf");
			BufferedReader br = new BufferedReader(new InputStreamReader(
					fstream));

			line = br.readLine();

			while (line != null) {

				if (line.contains("sidechannel.high_input_size") && line.trim().charAt(0) != '#') {
					String value = line.split("=")[1].trim();
					inputSize = Integer.parseInt(value);
				}
				line = br.readLine();
			}
			br.close();
		} catch (Exception e) {
			System.out.println("Error is in >>>>>" + line + "<<<<<");
			e.printStackTrace();
		} finally {
			SIZE_HIGH = inputSize;
			SIZE_LOW = inputSize;
		}
	}

	public static void main(String args[]) throws Exception{
		int[] h1 = initSecretInput(args);
		int[] l1 = initPublicInput(args);
		
		byte[] h = new byte[h1.length];
		byte[] l = new byte[l1.length];
		
		for(int i = 0; i < h1.length; ++i){
			h[i] = (byte) h1[i];
		}
		
		for(int i = 0; i < l1.length; ++i){
			l[i] = (byte) l1[i];
		}
		
		//*
		// byte[] cookie = { 'A', 'A', 'A', 'A', 'A', 'A', 'A', 'A', 'A', 'A', 'A', 'A', 'A', 'A', 'A'};
		byte[] cookie = { 's', 'e', 's', 's', 'i', 'o', 'n', 'k', 'e', 'y', ':'};

		byte[] h_cookie = Arrays.copyOf(h, h.length + cookie.length);
		byte[] l_cookie = Arrays.copyOf(l, l.length + cookie.length);
		System.arraycopy(cookie, 0, h_cookie, h.length, cookie.length);
		System.arraycopy(cookie, 0, l_cookie, l.length, cookie.length);

		final byte[] all = Arrays.copyOf(h_cookie, h_cookie.length
				+ l_cookie.length);
		System.arraycopy(l_cookie, 0, all, h_cookie.length, l_cookie.length);
		//*/
		
		final byte[] compressed = LZ77T.compress(all);
		Observable.add(compressed.length);
	}
}
