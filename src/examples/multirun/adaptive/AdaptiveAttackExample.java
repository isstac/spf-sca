package multirun.adaptive;

import gov.nasa.jpf.symbc.Debug;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Vector;

import sidechannel.common.Common;

/**
 *
 * @author Quoc-Sang Phan <sang.phan@sv.cmu.edu>
 *
 */
public class AdaptiveAttackExample {
	
	protected static int SIZE_HIGH;
	protected static int SIZE_LOW;

	protected static void init(String fileName){
		String line = null;
		int inputSize = 1;
		try {
			
			FileInputStream fstream = new FileInputStream(fileName);
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
	
	protected static int[] initSecretInput(int id){

		int[] h = new int[SIZE_HIGH];
		for (int j = 0; j < SIZE_HIGH; j++) {
			h[j] = Debug.makeSymbolicInteger("h" + j);
		}
		// if this is not the first run, load domain from file 
		if(id > 0){
			String fileName = "build/tmp/constraints" + id + ".txt";
			System.out.println(">>>>> Load domain from " + fileName);
			String line = null;
			try {

				FileInputStream fstream = new FileInputStream(fileName);
				BufferedReader br = new BufferedReader(new InputStreamReader(
						fstream));

				line = br.readLine();
				boolean varDomain = false;
				
				while (line != null && !line.isEmpty()) {
					boolean PC = true;
					String[] conjuncts = line.split(" #AND# ");
					for(String conjunct : conjuncts){
						boolean c = false;
						String[] disjuncts = conjunct.split(" #OR# ");
						for(String disjunct : disjuncts){
							// example string: "[+1*h1<=+10, -1*h1=-8, +1*h0=+3]";
							// TODO: replace this manual parsing by regular expression
							String[] array1 = disjunct.substring(1, disjunct.length() -1).replaceAll(" ","").split(",");
							boolean d = true;
							for(String s1 : array1){
								// System.out.println("s1 is :" + s1);
								boolean isLEQ = s1.indexOf("<=") > 0;
								String[] array2 = s1.split("<=|=");
								String linearPolynomial = array2[0];
								Vector<Character> signs = new Vector<Character>();
								// add a dummy element, since the term start from 0
								signs.add('+');
								int i;
								for(i = 0; i < linearPolynomial.length(); i++){
									char sign = linearPolynomial.charAt(i);
									if(sign == '+' || sign == '-'){
										signs.add(sign);
									}
								}
								String[] array3 = array2[0].split("[+-]");
								int polynomial = 0;
								for(i = 1; i < array3.length; i++){
									String term = array3[i];
									String[] array4 = term.split("\\*");
									// all high variables have the form h1
									int coeff = Integer.parseInt(array4[0]);
									int index = Integer.parseInt(array4[1].substring(1));
									if(signs.get(i) == '+'){
										polynomial += coeff * h[index];
									} else {
										assert(signs.get(i) == '-');
										polynomial -= coeff * h[index];
									}
								}
								int val = Integer.parseInt(array2[1]);
								if(isLEQ){
									d = d && (polynomial <= val);
								} else {
									// is LE
									d = d && (polynomial == val);
								}
							}
							c = c || d;
						}
						PC = PC && c;
					}
					
					varDomain = varDomain || PC;
					line = br.readLine();
				}
				
				Debug.assume(varDomain);
				// TODO: why there is warning that there is no stream???
				br.close();
			} catch (Exception e) {
				System.out.println("Error is in >>>>>" + line + "<<<<<");
				e.printStackTrace();
			} 
		}
		return h;
	}
	
	protected static int[] initPublicInput(int id, boolean defend){
		int[] l = new int[SIZE_LOW];
		for (int j = 0; j < SIZE_LOW; j++) {
			// l[j] = Debug.makeSymbolicInteger("l_" + j);
			l[j] = Debug.makeSymbolicInteger("l" + Common.SEPARATOR + j);
		}
		if (defend) {
			String fileName = "build/tmp/input" + id + ".txt";
			System.out.println(">>>>> Load public inputs from " + fileName);
			String line = null;
			try {
				// file contains multiple lines of the form, e.g.: 
				// l_0:1
				// l_1:3
				// ...
				FileInputStream fstream = new FileInputStream(fileName);
				BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

				line = br.readLine();

				while (line != null && !line.isEmpty()) {
					String[] input = line.split(":");
					int index = Integer.parseInt(input[0].substring(input[0].indexOf(Common.SEPARATOR)+1));
					int val = Integer.parseInt(input[1]);
					l[index] = val;
					line = br.readLine();
				}
				// TODO: why there is warning that there is no stream???
				br.close();
			} catch (Exception e) {
				System.out.println("Error is in >>>>>" + line + "<<<<<");
				e.printStackTrace();
			}
		}
		return l;
	}
}
