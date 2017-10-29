package multirun;

import gov.nasa.jpf.symbc.Debug;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import sidechannel.multirun.Observable;

public class SimplifiedRSA {

	static int run;

	static boolean DEBUG = true;

	static long count = 0;

	static final int MAX_HIGH, MIN_HIGH, MODULO;

	static final String MAX_KEY = new String("sidechannel.max_high");
	static final String MIN_KEY = new String ("sidechannel.min_high");
	static final String MODULO_KEY = new String ("symbolic.max_int");

	static boolean foundMax = false;
	static boolean foundMin = false;
	static boolean foundModulo = false;

	static boolean greedy = true;
	
	static {
		// Hack for test: initialize the bounds on the secret from .jpf file
		String line = null;
		int max = Integer.MAX_VALUE, min = Integer.MIN_VALUE;
		int modulo = Integer.MAX_VALUE;
		
		try {
			
			FileInputStream fstream = new FileInputStream(
					"src/examples/multirun/SimplifiedRSA.jpf");
			BufferedReader br = new BufferedReader(new InputStreamReader(
					fstream));

			line = br.readLine();

			while (line != null) {

				if (!foundMax) {
					if (line.contains(MAX_KEY) && line.trim().charAt(0) != '#') {
						String value = line.split("=")[1].trim();
						max = Integer.parseInt(value);
						foundMax = true;
					}
				}

				if (!foundMin) {
					if (line.contains(MIN_KEY) && line.trim().charAt(0) != '#') {
						String value = line.split("=")[1].trim();
						min = Integer.parseInt(value);
						foundMin = true;
					}
				}

				if (!foundModulo) {
					if (line.contains(MODULO_KEY) && line.trim().charAt(0) != '#') {
						String value = line.split("=")[1].trim();
						modulo = Integer.parseInt(value);
						foundModulo = true;
					}
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
		} finally {
			MAX_HIGH = max;
			MIN_HIGH = min;
			MODULO = modulo;
			System.out.println(">>>>> MAX_HIGH is " + MAX_HIGH + " and MIN_HIGH is " + MIN_HIGH);
			System.out.println(">>>>> MODULO is " + MODULO);
		}
	}

	static int modPowFastKocherReduction(int num, int e, int m) { 
		// computes num^e mod m

		int s = 1;
		int y = num;
		int res = 0;
		count = 0;

		Debug.assume(e <= MAX_HIGH && e >= MIN_HIGH);
		
		int bound = (int) (Math.log(MAX_HIGH + 1) / Math.log(2));
		
        int j=0;
		while (e > 0) {
			if (e % 2 == 1) {
				// res = (s * y) % m;
				// reduction:
				int tmp = s * y;
				if (tmp > m) {
					tmp = tmp - m;
					count++;
					// System.out.println("reduction");
				}
				res = tmp % m;
				count++;
			} else {
				res = s;
				count++;
			}
			s = (res * res) % m; // squaring the base
			e /= 2;
			count++;
			j++;
			
			if(j==bound) break;
		}
		System.out.println("Cost is: " + count);
		return res;
	}

	public static void testGreedy() {

		int h;
		int[] l = new int[run];

		int[] encrypt = new int[run];

		int i;

		if (run > 1) {
			// read the low inputs from previous run

			String strLine = "";

			// Read File Line By Line
			try {
				FileInputStream fstream = new FileInputStream(
						"build/tmp/input.txt");
				BufferedReader br = new BufferedReader(new InputStreamReader(
						fstream));

				for (int j = 0; j < run - 1; j++) {

					strLine = br.readLine();
					if (strLine == null) {
						System.out.println(">>>>> Error reading inputs");
					} else {
						System.out.println(">>>>> Input in run " + (j + 1)
								+ " is " + strLine);
						l[j] = Integer.parseInt(strLine.trim());
					}
				}

				br.close();
			} catch (NumberFormatException e) {
				System.out.println(">>>>> This is not number >>>>>"
						+ strLine.trim() + "<<<<<");
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		h = Debug.makeSymbolicInteger("h");

		// format lx_y: x is the index of the run, y is the index of the element
		// since the input is not an array, the index is always 0
		// this fits with the parser in the GreedyQuantifier

		// l[0] = 13;
		l[run - 1] = Debug.makeSymbolicInteger("l" + (run - 1) + "_0");
		// l[0] = 2;

		// self-composition
		for (i = 0; i < run; i++) {
			encrypt[i] = modPowFastKocherReduction(l[i], h, MODULO);
			Observable.add(count);
		}

	}

	public static void main(String[] args) {
		
		if (args.length < 1) {
			System.out.println(">>>>> Need number of runs");
			System.exit(0);
		}
		
		run = Integer.parseInt(args[0]); // number of runs

		if (DEBUG) {
			System.out.println(">>>>> Number of run is " + run);
		}
		if (greedy) {
			testGreedy();
		} else {
			testFull();
		}
	}

	public static void testFull() {
		int h;
		int[] l = new int[run];

		int[] encrypt = new int[run];

		int i;

		h = Debug.makeSymbolicInteger("h");
		for (i = 0; i < run; i++) {
			l[i] = Debug.makeSymbolicInteger("l" + i + "_0");
		}

		// self-composition
		for (i = 0; i < run; i++) {
			encrypt[i] = modPowFastKocherReduction(l[i], h, MODULO);
			Observable.add(count);
		}
	}
}
