package sidechannel;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class SimpleWriter {
	public static void main(String args[]) {
		try (Writer writer = new BufferedWriter(new OutputStreamWriter(
				// new FileOutputStream("filename.txt"), "utf-8"))) {
				new FileOutputStream("filename.txt")))) {
			writer.write("something");
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
