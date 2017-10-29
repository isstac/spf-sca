package sidechannel;

import java.io.DataOutputStream;
import java.io.OutputStream;
import java.net.Socket;

public class SimpleClient {
	public static void main(String args[]) throws Exception {
		String server = "127.0.0.1";
		int port = 22;
		double value = 128;

		Socket s = new Socket(server, port);
		OutputStream os = s.getOutputStream();
		DataOutputStream dos = new DataOutputStream(os);
		dos.writeDouble(value);

		s.close();
	}
}
