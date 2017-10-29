package sidechannel;

import java.io.PrintWriter;

public class FormatFileWriter {
	
	public static void main(String args[]) {
		
		PrintWriter writer;
		try {
			writer = new PrintWriter("the-file-name.txt", "UTF-8");
			writer.println("The first line");
			writer.println("The second line");
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
