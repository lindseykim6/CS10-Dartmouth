import java.io.*;
import java.net.*;

/**
 * Handles communication from the server (via "in") for ChatClient, printing to System.out

 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Fall 2012; revised Winter 2014 to separate out ChatClientCommunicator
*/

public class ChatClientCommunicator extends Thread {
	private Socket sock;			// the underlying socket for communication
	private ChatClient client;		// for which this is handling communication
	private BufferedReader in;		// from server
	private PrintWriter out;		// to server

	public ChatClientCommunicator(Socket sock, ChatClient client) throws IOException {
		this.sock = sock;
		this.client = client;
		in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
		out = new PrintWriter(sock.getOutputStream(), true);
	}

	public void send(String msg) {
		//called when have keyboard input
		this.out.println(msg);
	}
	
	public void run() {
		// Get lines from server; print to console
		try {
			String line;
			while ((line = in.readLine()) != null) {	
				System.out.println(line);
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			client.hangUp();
			System.out.println("server hung up");
		}

		// Clean up
		try {
			out.close();
			in.close();
			sock.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}	
}
