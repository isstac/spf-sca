package sidechannel;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class SendTextClient {
	
	public static void main(String[] args) throws IOException {

		String server = "127.0.0.1";
		int port = 22;

		Socket s = new Socket(server, port);

	    PrintWriter out = new PrintWriter(s.getOutputStream(), true);
	    String text = "Hello World!";
	    out.print(text + "\r\n");  // send the response to client

	    out.close();
	    s.close();
	  }
}
