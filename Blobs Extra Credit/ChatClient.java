import java.io.*;
import java.net.*;
import java.util.Scanner;

/**
 * Client to connect to ChatServer
 * Start the server first
 *
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Fall 2012; revised Winter 2014 to separate out ChatClientCommunicator
 */
public class ChatClient {
	private Scanner console;					// input from the user
	private ChatClientCommunicator comm;		// communication with the server
	private boolean hungup = false;				// has the server hung up on us?

	public ChatClient(Socket sock) throws IOException {		
		// For reading lines from the console
		console = new Scanner(System.in);

		// Fire off a new thread to handle incoming messages from server
		comm = new ChatClientCommunicator(sock, this);
		comm.setDaemon(true);
		comm.start();

		// Greeting; name request and response
		System.out.println("Please enter your name");
		String name = console.nextLine(); //blocks until keyboard input
		comm.send(name);
	}

	/**
	 * Get console input and send it to server; 
	 * stop & clean up when server has hung up (noted by hungup)
	 */
	public void handleUser() throws IOException {
		while (!hungup) {
			//console.nextLine() blocks until text is entered
			comm.send(console.nextLine());
		}
	}

	/**
	 * Notes that the server has hung up (so handleUser loop will terminate)
	 */
	public void hangUp() {
		hungup = true;
	}
	
	public static void main(String[] args) throws IOException {
		new ChatClient(new Socket("localhost", 4242)).handleUser();
	}
}
