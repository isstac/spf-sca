package sidechannel;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import gov.nasa.jpf.symbc.Debug;
import sidechannel.multirun.Observable;

public class PasswordChecker {
	
	static final int SIZE;
	static long count = 0;

	static boolean check(byte[] secret, byte[] input) {
		count = 0;
		for (int i = 0; i < SIZE; i++){
			if (secret[i] != input[i]){	
				return false;
			}
			count++;
		}
		return true;
	}
	
	static {
		// Hack for test: initialize the bounds on the secret from .jpf file
		String line = null;
		int inputSize = 1;
		try {
			
			FileInputStream fstream = new FileInputStream(
					"src/examples/sidechannel/PasswordChecker.jpf");
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
			SIZE = inputSize;
		}
	}
	
	public static void test(){
		byte[] h = new byte[SIZE];
		byte[] l = new byte[SIZE];

		byte j;
		for (j = 0; j < SIZE; j++) {
			h[j] = Debug.makeSymbolicByte("h" + j);
			// Debug.assume(h[j] >= MIN_HIGH && h[j] <= MAX_HIGH);
		}

		for (j = 0; j < SIZE; j++) {
			l[j] = Debug.makeSymbolicByte("l" + j);
			// Debug.assume(l[j] >= MIN_HIGH && l[j] <= MAX_HIGH);
			// l[j] = j;
		}
		check(h, l);
		Observable.add(count);
	}
	
	public static void main(String[] args) throws Exception {
		test();
	}
}
