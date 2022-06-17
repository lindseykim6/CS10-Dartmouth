import java.net.*;
import java.util.*;
import java.io.*;

/**
 * A server to handle sketches: getting requests from the clients,
 * updating the overall state, and passing them on to the clients
 *
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Fall 2012; revised Winter 2014 to separate SketchServerCommunicator
 * @author Lindsey Kim
 */
public class SketchServer {
	private ServerSocket listen;						// for accepting connections
	private ArrayList<SketchServerCommunicator> comms;	// all the connections with clients
	private Sketch sketch;								// the state of the world

	public SketchServer(ServerSocket listen) {
		this.listen = listen;
		sketch = new Sketch();
		comms = new ArrayList<SketchServerCommunicator>();
	}

	public Sketch getSketch() {
		return sketch;
	}

	/**
	 * The usual loop of accepting connections and firing off new threads to handle them
	 */
	public void getConnections() throws IOException {
		System.out.println("server ready for connections");
		while (true) {
			SketchServerCommunicator comm = new SketchServerCommunicator(listen.accept(), this);
			comm.setDaemon(true);
			comm.start();
			addCommunicator(comm);
		}
	}

	/**
	 * Adds the communicator to the list of current communicators
	 */
	public synchronized void addCommunicator(SketchServerCommunicator comm) {
		comms.add(comm);
	}

	/**
	 * Removes the communicator from the list of current communicators
	 */
	public synchronized void removeCommunicator(SketchServerCommunicator comm) {
		comms.remove(comm);
	}

	/**
	 * Sends the message from the one communicator to all (including the originator)
	 */
	public synchronized void broadcast(String msg) {
		for (SketchServerCommunicator comm : comms) {
			comm.send(msg);
		}
	}

	/**
	 * Look through the message and update ids and shapes in it as well as save the updated sketch
	 * @param line the message given
	 * @param message the message object we will be using to analyze the message
	 */
	public void updateIDandShapes(String line, Message message){
		message.parseMessage(line);  // parses through the line
		if(message.getModes().equals("draw")) {  // if it is draw mode (aka a new shape)
			message.setMovingID(sketch.incrementID());  // increment the original id and set the id to this new differentiated one
			if(message.getShape().equals("rectangle")) {  // if it is a rectangle
				sketch.newShape(message.getMovingID(), (new Rectangle(message.getX1(),message.getY1(),
						message.getX2(), message.getY2(), message.getColor())));  // add a rectangle to the sketch with the new id
			} else if(message.getShape().equals("ellipse")) {// if it is a ellipse
				sketch.newShape(message.getMovingID(), (new Ellipse(message.getX1(),message.getY1(),
						message.getX2(), message.getY2(), message.getColor())));  // add an ellipse to the sketch with the new id
			} else  if(message.getShape().equals("segment")) {  // if it is a segment
				sketch.newShape(message.getMovingID(), (new Segment(message.getX1(),message.getY1(),
						message.getX2(), message.getY2(), message.getColor())));  // add a segment to the sketch with the new id
			}
            System.out.println(message.getShape() + " added");
		}

		if(message.getModes().equals("recolor")) {  // if it is recolor mode
			Shape s= sketch.getShapeKey(message.getMovingID()); // gets the shape with the id we need
			s.setColor(message.getColor());  // sets the shape color to the new one
			sketch.newShape(message.getMovingID(), s); // add the shape with the new color to the sketch
            System.out.println("New Color of " + message.getShape() + " is  " + message.getColor());
		}

		if(message.getModes().equals("move")) {  // if it is move mode
			Shape s= sketch.getShapeKey(message.getMovingID());  // gets the shape with the id we need
			s.moveBy(message.getChangeX(), message.getChangeY());  // move it by the dx and dy we determined
			sketch.newShape(message.getMovingID(), s);  // adds the moved shape to the sketch
            System.out.println("Moved by " + message.getChangeX() + ", " + message.getChangeY());
		}

		if(message.getModes().equals("delete")) {  // if it is in delete mode
			sketch.removeShape(message.getMovingID());  // remove the shape with the id we need
            System.out.println("Deleted a " + message.getShape());
		}
	}

	public static void main(String[] args) throws Exception {
		new SketchServer(new ServerSocket(4242)).getConnections();
	}
}
