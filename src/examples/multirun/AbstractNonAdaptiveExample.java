package multirun;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import gov.nasa.jpf.symbc.Debug;
import sidechannel.multirun.Observable;

/**
 *
 * @author Quoc-Sang Phan <sang.phan@sv.cmu.edu>
 *
 */
public abstract class AbstractNonAdaptiveExample {

	protected static int SIZE;

	protected static boolean greedy = true;

	protected static boolean DEBUG = false;

	protected static int run;
	
	public abstract int foo(byte[] h, byte[] l);

	protected static void init(String fileName) {
		// Hack for test: initialize the bounds on the secret from .jpf file
		String line = null;

		try {

			FileInputStream fstream = new FileInputStream(fileName);
			BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

			line = br.readLine();

			while (line != null) {

				if (line.contains("sidechannel.high_input_size") && line.trim().charAt(0) != '#') {
					String value = line.split("=")[1].trim();
					SIZE = Integer.parseInt(value);
				}

				if (line.contains("greedy") && line.trim().charAt(0) != '#') {
					String value = line.split("=")[1].trim();
					if (value.equals("false")) {
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
	
	/*
	 * driver to test the program SIZE is the size of the array,
	 * and (run + 1) is the number of executions
	 */
	public void testFull() {

		byte[] h = new byte[SIZE];
		int[] result = new int[run];
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

		for (i = 0; i < run; i++) {
			result[i] = foo(h, l[i]);
			Observable.add(result[i]);
		}
	}

	/*
	 * run > 0
	 */
	public void testGreedy() {

		byte[] h = new byte[SIZE];
		int[] result = new int[run];
		byte[][] l = new byte[run][SIZE];

		int i;

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
						for (i = 0; i < SIZE; i++) {
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

			l[run - 1][i] = Debug.makeSymbolicByte("l" + run + "_" + i);
		}

		// self-composition
		for (i = 0; i < run; i++) {
			result[i] = foo(h, l[i]);
			Observable.add(result[i]);
		}
	}
}
