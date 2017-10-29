package challenge1;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

import gov.nasa.jpf.symbc.Debug;
import sidechannel.multirun.Observable;

public class CRIMEMultirun
{
	static int run;
	
	static int SIZE;
	
	static int SIZE_LOW = 5;

	static boolean DEBUG = false;
	
	static boolean greedy = true;
	
	static {
		// Hack for test: initialize the bounds on the secret from .jpf file
		String line = null;
		
		try {

			FileInputStream fstream = new FileInputStream(
					"src/examples/challenge1/Challenge1Multirun.jpf");
			BufferedReader br = new BufferedReader(new InputStreamReader(
					fstream));

			line = br.readLine();

			while (line != null) {

				if (line.contains("sidechannel.high_input_size") && line.trim().charAt(0) != '#') {
					String value = line.split("=")[1].trim();
					SIZE = Integer.parseInt(value);
				}
				
				if (line.contains("greedy") && line.trim().charAt(0) != '#') {
					String value = line.split("=")[1].trim();
					if (value.equals("false")){
						greedy = false;
					}
				}
				
				line = br.readLine();
			}
			br.close();
		} catch (Exception e) {
			System.out.println("Error is in >>>>>" + line + "<<<<<");
			e.printStackTrace();
		}
	}

	public static void testFull() throws Exception {
		byte[] h = new byte[SIZE];
		byte[][] l = new byte[run][SIZE];

		int i, j;
		for (j = 0; j < SIZE; j++) {
			h[j] = Debug.makeSymbolicByte("h" + j);
		}

		for (i = 0; i < run; i++) {
			for (j = 0; j < SIZE; j++) {
				l[i][j] = Debug.makeSymbolicByte("l" + i + "_" + j);
			}
		}

		byte[] cookie = {'c','o','o','k','i','e'};
		
		System.out.print("Cost ");
		for (i = 0; i < run; i++) {
			byte[] h_cookie=Arrays.copyOf(h, h.length + cookie.length);
			byte[] l_cookie=Arrays.copyOf(l[i], l[i].length + cookie.length);
			System.arraycopy(cookie, 0, h_cookie, h.length, cookie.length);
			System.arraycopy(cookie, 0, l_cookie, l[i].length, cookie.length);
					
			final byte[] all = Arrays.copyOf(h_cookie, h_cookie.length + l_cookie.length);
	        System.arraycopy(l_cookie, 0, all, h_cookie.length, l_cookie.length);
	        
	        final byte[] compressed = LZ77T.compress(all);
			Observable.add(compressed.length);
	        
			System.out.print(compressed.length + " ");
		
		}
		System.out.print("\n");
	}
	
	/*
	 * run > 0
	 */
	public static void testGreedy() throws Exception{

		byte[] h = new byte[SIZE];
		int[] len = new int[run];
		byte[][] l = new byte[run][SIZE_LOW];

		int i;

		byte[] cookie = {'c','o','o','k','i','e'};
		
		if (run > 1) {
			// read the low inputs from previous run

			String strLine;

			// Read File Line By Line
			System.out.println();
			try {
				FileInputStream fstream = new FileInputStream("build/tmp/input.txt");
				BufferedReader br = new BufferedReader(new InputStreamReader(
						fstream));

				for (int j = 0; j < run - 1; j++) {

					strLine = br.readLine();
					if (strLine == null) {
						System.out.println(">>>>> Error reading inputs");
					} else {
						System.out.println(">>>>> Input in run " + (j + 1)
								+ " is " + strLine);
						String nums[] = strLine.split("\\s");
						for (i = 0; i < SIZE_LOW; i++) {
							l[j][i] = Byte.parseByte(nums[i]);
						}
					}
				}

				br.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		for (i = 0; i < SIZE; i++) {

			h[i] = Debug.makeSymbolicByte("h" + i);

		}
		
		for (i = 0; i < SIZE_LOW; i++) {

			l[run - 1][i] = Debug.makeSymbolicByte("l" + run + "_" + i);
		}

		// self-composition
		for (i = 0; i < run; i++) {
			// len[i] = compress(h, l[i]);			
			byte[] h_cookie=Arrays.copyOf(h, h.length + cookie.length);
			byte[] l_cookie=Arrays.copyOf(l[i], l[i].length + cookie.length);
			System.arraycopy(cookie, 0, h_cookie, h.length, cookie.length);
			System.arraycopy(cookie, 0, l_cookie, l[i].length, cookie.length);
					
			final byte[] all = Arrays.copyOf(h_cookie, h_cookie.length + l_cookie.length);
	        System.arraycopy(l_cookie, 0, all, h_cookie.length, l_cookie.length);
	        
	        final byte[] compressed = LZ77T.compress(all);
	        len[i] = compressed.length;
			Observable.add(compressed.length);
		}
		
		// print out the cost
		if (DEBUG){
			 System.out.print("cost ");
			for (i = 0; i < run; i++) {
				System.out.print(len[i] + " ");
			}
			System.out.print("\n");
		}
	}

	public static void main(String[] args) throws Exception {

		if (args.length < 1) {
			System.out.println("Need number of runs");
			System.exit(0);
		}
		run = Integer.parseInt(args[0]); // number of runs

		if (DEBUG) {
			System.out.println("Number of run is " + run);
		}
		if (greedy) {
			testGreedy();
		} else {
			testFull();
		}
	}
}
