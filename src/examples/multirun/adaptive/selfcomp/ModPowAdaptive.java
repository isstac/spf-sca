package multirun.adaptive.selfcomp;

import gov.nasa.jpf.symbc.Debug;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import multirun.adaptive.AbstractSelfCompExample;

/**
 *
 * @author Quoc-Sang Phan <sang.phan@sv.cmu.edu>
 *
 */
public class ModPowAdaptive extends AbstractSelfCompExample{

	static long count = 0;

	static final int MAX_HIGH, MIN_HIGH, MODULO;

	static final String MAX_KEY = new String("sidechannel.max_high");
	static final String MIN_KEY = new String ("sidechannel.min_high");
	static final String MODULO_KEY = new String ("symbolic.max_int");

	static boolean foundMax = false;
	static boolean foundMin = false;
	static boolean foundModulo = false;
		
	static {
		// Hack for test: initialize the bounds on the secret from .jpf file
		String line = null;
		int max = Integer.MAX_VALUE, min = Integer.MIN_VALUE;
		int modulo = Integer.MAX_VALUE;

		try {
			
			FileInputStream fstream = new FileInputStream(
					"src/examples/multirun/adaptive/selfcomp/ModPowAdaptive.jpf");
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

	public int modPowFastKocherReduction(int num, int e, int m) { 
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
		// System.out.println("Cost is " + count);
		return res;
	}

	public static void main(String[] args) {
		ModPowAdaptive ex = new ModPowAdaptive();
		ex.run(args);
	}

	@Override
	public int foo(int h, int l) {
		modPowFastKocherReduction(l,h,MODULO);
		int cost = (int) count;
		return cost;
	}
}