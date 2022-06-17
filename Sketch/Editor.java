import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

/**
 * Client-server graphical editor
 *
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Fall 2012; loosely based on CS 5 code by Tom Cormen
 * @author CBK, winter 2014, overall structure substantially revised
 * @author Travis Peters, Dartmouth CS 10, Winter 2015; remove EditorCommunicatorStandalone (use echo server for testing)
 * @author CBK, spring 2016 and Fall 2016, restructured Shape and some of the GUI
 * @author Lindsey Kim
 */

public class Editor extends JFrame {
	private static String serverIP = "localhost";            // IP address of sketch server
	// "localhost" for your own machine;
	// or ask a friend for their IP address

	private static final int width = 800, height = 800;        // canvas size

	// Current settings on GUI
	public enum Mode {
		DRAW, MOVE, RECOLOR, DELETE
	}

	private Mode mode = Mode.DRAW;                // drawing/moving/recoloring/deleting objects
	private String shapeType = "ellipse";        // type of object to add
	private Color color = Color.black;            // current drawing color

	// Drawing state
	// these are remnants of my implementation; take them as possible suggestions or ignore them
	private Shape curr = null;                    // current shape (if any) being drawn
	private Sketch sketch;                        // holds and handles all the completed objects
	private int movingId = -1;                    // current shape id (if any; else -1) being moved
	private Point drawFrom = null;                // where the drawing started
	private Point moveFrom = null;                // where object is as it's being dragged
	private Point moveTo;
	// Communication
	private EditorCommunicator comm;            // communication with the sketch server
	private String serverRequest = "";          // message to be sent to the server

	public Editor() {
		super("Graphical Editor");

		sketch = new Sketch();

		// Connect to server
		comm = new EditorCommunicator(serverIP, this);
		comm.start();

		// Helpers to create the canvas and GUI (buttons, etc.)
		JComponent canvas = setupCanvas();
		JComponent gui = setupGUI();

		// Put the buttons and canvas together into the window
		Container cp = this.getContentPane();
		cp.setLayout(new BorderLayout());
		cp.add(canvas, BorderLayout.CENTER);
		cp.add(gui, BorderLayout.NORTH);

		// Usual initialization
		setLocationRelativeTo(null);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.pack();
		this.setVisible(true);
	}

	/**
	 * Creates a component to draw into
	 */
	private JComponent setupCanvas() {
		JComponent canvas = new JComponent() {
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				drawSketch(g);
			}
		};

		canvas.setPreferredSize(new Dimension(width, height));

		canvas.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent event) {
				handlePress(event.getPoint());
			}

			public void mouseReleased(MouseEvent event) {
				handleRelease();
			}
		});

		canvas.addMouseMotionListener(new MouseAdapter() {
			public void mouseDragged(MouseEvent event) {
				handleDrag(event.getPoint());
			}
		});

		return canvas;
	}

	/**
	 * Creates a panel with all the buttons
	 */
	private JComponent setupGUI() {
		// Select type of shape
		String[] shapes = {"ellipse", "rectangle", "segment"};
		JComboBox<String> shapeB = new JComboBox<String>(shapes);
		shapeB.addActionListener(e -> shapeType = (String) ((JComboBox<String>) e.getSource()).getSelectedItem());

		// Select drawing/recoloring color
		// Following Oracle example
		JButton chooseColorB = new JButton("choose color");
		JColorChooser colorChooser = new JColorChooser();
		JLabel colorL = new JLabel();
		colorL.setBackground(Color.black);
		colorL.setOpaque(true);
		colorL.setBorder(BorderFactory.createLineBorder(Color.black));
		colorL.setPreferredSize(new Dimension(25, 25));
		JDialog colorDialog = JColorChooser.createDialog(chooseColorB,
				"Pick a Color",
				true,  //modal
				colorChooser,
				e -> {
					color = colorChooser.getColor();
					colorL.setBackground(color);
				},  // OK button
				null); // no CANCEL button handler
		chooseColorB.addActionListener(e -> colorDialog.setVisible(true));

		// Mode: draw, move, recolor, or delete
		JRadioButton drawB = new JRadioButton("draw");
		drawB.addActionListener(e -> mode = Mode.DRAW);
		drawB.setSelected(true);
		JRadioButton moveB = new JRadioButton("move");
		moveB.addActionListener(e -> mode = Mode.MOVE);
		JRadioButton recolorB = new JRadioButton("recolor");
		recolorB.addActionListener(e -> mode = Mode.RECOLOR);
		JRadioButton deleteB = new JRadioButton("delete");
		deleteB.addActionListener(e -> mode = Mode.DELETE);
		ButtonGroup modes = new ButtonGroup(); // make them act as radios -- only one selected
		modes.add(drawB);
		modes.add(moveB);
		modes.add(recolorB);
		modes.add(deleteB);
		JPanel modesP = new JPanel(new GridLayout(1, 0)); // group them on the GUI
		modesP.add(drawB);
		modesP.add(moveB);
		modesP.add(recolorB);
		modesP.add(deleteB);

		// Put all the stuff into a panel
		JComponent gui = new JPanel();
		gui.setLayout(new FlowLayout());
		gui.add(shapeB);
		gui.add(chooseColorB);
		gui.add(colorL);
		gui.add(modesP);
		return gui;
	}

	/**
	 * Getter for the sketch instance variable
	 */
	public Sketch getSketch() {
		return sketch;
	}

	/**
	 * Draws all the shapes in the sketch,
	 * along with the object currently being drawn in this editor (not yet part of the sketch)
	 */
	public void drawSketch(Graphics g) {
		// TODO: YOUR CODE HERE
		for (Integer i : sketch.getIdSketchMap().navigableKeySet()) {  // iterates from the bottom to the top
			sketch.getIdSketchMap().get(i).draw(g);  // draws each shape
		}
		if(curr!=null) {
			curr.draw(g);
		}
	}

	// Helpers for event handlers

	/**
	 * Helper method for press at point
	 * In drawing mode, start a new object;
	 * in moving mode, (request to) start dragging if clicked in a shape;
	 * in recoloring mode, (request to) change clicked shape's color
	 * in deleting mode, (request to) delete clicked shape
	 */
	private void handlePress(Point p) {
		// TODO: YOUR CODE HERE
		if (mode == Mode.DRAW) {
			serverRequest += "draw";  // add draw to the message if we are in draw mode
			// TODO: tell communicator to tell server about the ellipse
			drawFrom = p;
			if (shapeType.equals("ellipse")) {  // if the shape is an ellipse
				curr = new Ellipse((int) p.getX(), (int) p.getY(), color);  // sets curr to a new ellipse
			}
			if (shapeType.equals("rectangle")) {  // if the shape is a rectangle
				curr = new Rectangle((int) p.getX(), (int) p.getY(), color);  // sets curr to a new rectangle
			}
			if (shapeType.equals("segment")) {  // if the shape is a segment
				curr = new Segment((int) p.getX(), (int) p.getY(), color); // sets the current to a new segment
			}
		} else { // if it is not in draw mode
			curr = sketch.getShape(p); // find the selected shape in the sketch, otherwise return null
			movingId = sketch.identifyShape(curr); // find the id of that shape if it exists (if not, return -1)
			if (curr != null) { // ensures the selected shape exists
				if (curr.contains((int) p.getX(), (int) p.getY())) { // if clicked within the shape
					// In move mode, move the shape if clicked in it
					if (mode == Mode.MOVE) {
						serverRequest += "move"; // add move to the message
						moveFrom = p;  //sets the point we are moving from to this point
					}

					// In recoloring mode, change the shape's color if clicked in it
					if (mode == Mode.RECOLOR) {
						serverRequest += "recolor";  // add recolor to the message if we are in recolor mode
						curr.setColor(color);  // changes the color of the shape
						serverRequest += " " + color.getRGB();  // add the color to the message if we are in
					}

					// In deleting mode, delete the shape if clicked in it
					if (mode == Mode.DELETE) {
						serverRequest += "delete";  // add delete to the message if we are in delete mode
					}
				}
			}
		}
		repaint();
		serverRequest += " " + movingId;  // add the id of the object we just looked at (if none, it will be -1 and curr will be null)
	}

	/**
	 * Helper method for drag to new point
	 * In drawing mode, update the other corner of the object;
	 * in moving mode, (request to) drag the object
	 */
	private void handleDrag(Point p) {
		// TODO: YOUR CODE HERE
		if (mode == Mode.DRAW) {  // if in draw mode
			if (shapeType.equals("rectangle")) {  // if it is a rectangle
				((Rectangle) curr).setCorners((int) drawFrom.getX(), (int) drawFrom.getY(), (int) p.getX(), (int) p.getY()); // set the corners
				repaint();
			}
			if (shapeType.equals("ellipse")) {  // if it is an eclipse
				((Ellipse) curr).setCorners((int) drawFrom.getX(), (int) drawFrom.getY(), (int) p.getX(), (int) p.getY()); //set the corners
			}
			if (shapeType.equals("segment")) {  // if it is a segment
				((Segment) curr).setEnd((int) p.getX(), (int) p.getY()); // set the end (what we are dragging to)
			}
		}

		// In moving mode, shift the object and keep track of where the mouse is
		if (mode == Mode.MOVE && curr != null) {  // ensures we aren't moving a nonexistent object
			moveTo = p;  // assign the point we move to to the current mouse location
		}
		repaint();

	}

	/**
	 * Helper method for release
	 * In drawing mode, pass the add new object request on to the server;
	 * in moving mode, release it
	 */
	private void handleRelease() {
		// TODO: YOUR CODE HERE
		if (mode == Mode.DRAW) { // in draw mode
			serverRequest += " " + curr.toString();  // adds the information for the shape to the message
			repaint();
		}

		if (mode == Mode.MOVE && moveFrom != null  && moveTo!=null) {  // if in move mode and there is a starting point and ending point
			serverRequest += " " + (int) (moveTo.getX() - moveFrom.getX()) + " " + (int) (moveTo.getY() - moveFrom.getY());  // adds dx and dy to the message
		}

		if (curr != null) {  // if there is a valid shape being looked at
			comm.send(serverRequest);  // sent the request to the server
		}

		// reset parameters for the next object
		moveFrom = null;
		moveTo = null;
		movingId = -1;
		serverRequest = "";
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new Editor();
			}
		});
	}
}
