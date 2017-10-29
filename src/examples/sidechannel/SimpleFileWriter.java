package sidechannel;

import java.io.FileOutputStream;

public class SimpleFileWriter {

	public static void main(String args[]) {

		byte dataToWrite[] = { 1, 2, 3, 4 };
		FileOutputStream out;
		try {
			out = new FileOutputStream("the-file-name");
			out.write(dataToWrite);
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
