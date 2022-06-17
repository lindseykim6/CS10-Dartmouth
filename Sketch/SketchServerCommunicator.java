import java.io.*;
import java.net.Socket;
import java.util.Map;

/**
 * Handles communication between the server and one client, for SketchServer
 *
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Fall 2012; revised Winter 2014 to separate SketchServerCommunicator
 * @author Lindsey Kim
 */
public class SketchServerCommunicator extends Thread {
	private Socket sock;                    // to talk with client
	private BufferedReader in;                // from client
	private PrintWriter out;                // to client
	private SketchServer server;            // handling communication for

	public SketchServerCommunicator(Socket sock, SketchServer server) {
		this.sock = sock;
		this.server = server;
	}

	/**
	 * Sends a message to the client
	 * @param msg
	 */
	public void send(String msg) {
		out.println(msg);
	}

	/**
	 * Keeps listening for and handling (your code) messages from the client
	 */
	public void run() {
		try {
			System.out.println("someone connected");

			// Communication channel
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			out = new PrintWriter(sock.getOutputStream(), true);

			// Tell the client the current state of the world
			// TODO: YOUR CODE HERE
			for (Integer i : server.getSketch().getIdSketchMap().keySet()) {  // go through the shapes in the sketch map
				//gives the information to all the clients
				out.println("draw " + i + " " + server.getSketch().getIdSketchMap().get(i).toString() + "\n");
			}

			// Keep getting and handling messages from the client
			// TODO: YOUR CODE HERE
			String line; // each line given by the client
			Message message = new Message();  // creates a new message in order to analyze the line
			while ((line = in.readLine()) != null) {
				// look through the message and update ids and shapes in it as well as save the updated sketch
				server.updateIDandShapes(line, message);
				server.broadcast(message.toString());  // broadcast the new message's to string
			}

			// Clean up -- note that also remove self from server's list so it doesn't broadcast here
			server.removeCommunicator(this);
			out.close();
			in.close();
			sock.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
