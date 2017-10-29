package multirun;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import gov.nasa.jpf.symbc.Debug;
import sidechannel.multirun.Observable;

public class Example2 {

	static int run;
	static boolean greedy = true;
	
	static {
		// Hack for test: initialize the bounds on the secret from .jpf file
		String line = null;

		try {
			
			FileInputStream fstream = new FileInputStream(
					"src/examples/multirun/Example2.jpf");
			BufferedReader br = new BufferedReader(new InputStreamReader(
					fstream));

			line = br.readLine();

			while (line != null) {

				if (line.contains("greedy") && line.trim().charAt(0) != '#') {
					String value = line.split("=")[1].trim();
					if (value.equals("false")){
						greedy = false;
						break;
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
	
	public static int foo(int h, int l)

	{
		int cost;
		if (h > 0)

		{
			if (l < 0) {
				cost = 1;
			} else {
				cost = 2;
			}
		} else {
			if (l < 5)
				cost = 1;
			else
				cost = 4;
		}
		return cost;
	}
	
	public static void main(String[] args) {
		
		if (args.length < 1) {
			System.out.println("Need number of runs");
			System.exit(0);
		}
		
		run = Integer.parseInt(args[0]); // number of runs

		if (greedy) {
			testGreedy();
		} else {
			testFull();
		}
	}

	public static void testGreedy() {

		int h;
		int[] l = new int[run];

		int[] obs = new int[run];

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
			obs[i] = foo(h, l[i]);
			Observable.add(obs[i]);
		}

	}
	
	public static void testFull() {
		int h;
		int[] l = new int[run];

		int[] obs = new int[run];

		int i;

		h = Debug.makeSymbolicInteger("h");
		for (i = 0; i < run; i++) {
			l[i] = Debug.makeSymbolicInteger("l" + i + "_0");
		}

		// self-composition
		for (i = 0; i < run; i++) {
			obs[i] = foo(h, l[i]);
			Observable.add(obs[i]);
		}
	}
}









