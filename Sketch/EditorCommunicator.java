import java.io.*;
import java.net.Socket;

/**
 * Handles communication to/from the server for the editor
 *
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Fall 2012
 * @author Chris Bailey-Kellogg; overall structure substantially revised Winter 2014
 * @author Travis Peters, Dartmouth CS 10, Winter 2015; remove EditorCommunicatorStandalone (use echo server for testing)
 * @author Lindsey Kim
 */
public class EditorCommunicator extends Thread {
    private PrintWriter out;        // to server
    private BufferedReader in;        // from server
    protected Editor editor;        // handling communication for
    private Message message;

    /**
     * Establishes connection and in/out pair
     */
    public EditorCommunicator(String serverIP, Editor editor) {
        this.editor = editor;
        System.out.println("connecting to " + serverIP + "...");
        try {
            Socket sock = new Socket(serverIP, 4242);
            out = new PrintWriter(sock.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            System.out.println("...connected");
        } catch (IOException e) {
            System.err.println("couldn't connect");
            System.exit(-1);
        }
    }

    /**
     * Sends message to the server
     */
    public void send(String msg) {
        out.println(msg);
        editor.repaint();
    }

    /**
     * Keeps listening for and handling (your code) messages from the server
     */
    public void run() {
        try {
            // Handle messages
            // TODO: YOUR CODE HERE
            String line;
            while ((line = in.readLine()) != null) {
                handleLine(line);  // read through the message received from the server and update the client's sketch
                editor.repaint();  // repaint the client
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            System.out.println("server hung up");
        }
    }

    // Handle server
    // TODO: YOUR CODE HERE
    /**
     * Handles messages given by the client
     */
    private void handleLine(String line) {
        if (!line.equals("")) {  // makes sure the line isn't and empty string (in the beginning)
            message = new Message();  // creates a new message
            message.parseMessage(line);  // analyzes the message into separate variables
            if (message.getModes().equals("draw")) {  // if the message is in draw mode
                if (message.getShape().equals("rectangle")) {  // if it is a rectangle
                    editor.getSketch().newShape(message.getMovingID(), new Rectangle(message.getX1(), message.getY1(),
                            message.getX2(), message.getY2(), message.getColor()));  // add it with it's id to the sketch
                } else if (message.getShape().equals("ellipse")) { // if it is an ellipse
                    editor.getSketch().newShape(message.getMovingID(), new Ellipse(message.getX1(), message.getY1(),
                            message.getX2(), message.getY2(), message.getColor()));  // add the shape with it's id to the sketch
                } else if (message.getShape().equals("segment")) { // if it is a segment
                    editor.getSketch().newShape(message.getMovingID(), new Segment(message.getX1(), message.getY1(),
                            message.getX2(), message.getY2(), message.getColor())); // add the shape with it's id to the sketch
                }
            } else if (message.getModes().equals("recolor")) { // if the message is in recolor mode
                int id = message.getMovingID(); // find the id of the shape to be recolored
                Shape s = editor.getSketch().getIdSketchMap().get(id); // get the shape with the id
                s.setColor(message.getColor()); // set the color of the shape
                editor.getSketch().newShape(id, s); // update the map
            }

            if (message.getModes().equals("move")) {  // if the message is in move mode
                int id = message.getMovingID(); // find the id of the shape to be recolored
                Shape s = editor.getSketch().getIdSketchMap().get(id); // get the shape with the id
                s.moveBy(message.getChangeX(), message.getChangeY()); // move the shape by the given amount
                editor.getSketch().newShape(id, s); // update the map
            }

            if (message.getModes().equals("delete")) { // if the message is in delete mode
                editor.getSketch().getIdSketchMap().remove(message.getMovingID()); // remove the shape from the map
            }
        }
    }
}
