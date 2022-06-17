import java.net.*;
import java.util.*;
import java.io.*;

/**
 * Multithreaded chat server
 * Clients connect on port 4242 and give their name
 * Then the server accepts input and broadcasts it to the other clients
 * Connect by running ChatClient.java
 * 
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Fall 2012; revised Winter 2014 to separate out ChatServerCommunicator
 */

public class ChatServer {
	private ServerSocket listen;						// for accepting connections
	private ArrayList<ChatServerCommunicator> comms;	// all the connections with clients

	public ChatServer(ServerSocket listen) {
		this.listen = listen;
		comms = new ArrayList<ChatServerCommunicator>();
	}

	/**
	 * The usual loop of accepting connections and firing off new threads to handle them
	 */
	public void getConnections() throws IOException {
		while (true) {
			//listen.accept in next line blocks until new connection
			ChatServerCommunicator comm = new ChatServerCommunicator(listen.accept(), this);
			comm.setDaemon(true);
			comm.start();
			addCommunicator(comm);
		}
	}

	/**
	 * Adds the handler to the list of current client handlers
	 */
	public synchronized void addCommunicator(ChatServerCommunicator comm) {
		comms.add(comm);
	}

	/**
	 * Removes the handler from the list of current client handlers
	 */
	public synchronized void removeCommunicator(ChatServerCommunicator comm) {
		comms.remove(comm);
	}

	/**
	 * Sends the message from the one client handler to all the others (but not echoing back to the originator)
	 */
	public synchronized void broadcast(ChatServerCommunicator from, String msg) {
		for (ChatServerCommunicator c : comms) {
			if (c != from) {
				c.send(msg);
			}
		}
	}

	public static void main(String[] args) throws Exception {
		System.out.println("waiting for connections");
		new ChatServer(new ServerSocket(4242)).getConnections();
	}
}
