import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

/**
 * EXTRA CREDIT: Driver for interacting with a quadtree:
 * inserting points, viewing the tree, and finding points near a mouse press with labels
 * 
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Spring 2015
 * @author CBK, Spring 2016, updated for dots
 * @author CBK, Fall 2016, generics, dots, extended testing
 * @author Lindsey Kim
 * @author Kenneth Wu
 */
public class DotTreeGUIEC extends DrawingGUI {
	private static final int width=800, height=600;		// size of the universe
	private static final int dotRadius = 5;				// to draw dot, so it's visible
	private static final Color[] rainbow = {Color.RED, Color.ORANGE, Color.YELLOW, Color.GREEN, Color.BLUE, Color.MAGENTA};
			// to color different levels differently
	private char mode = 'a';						// 'a': adding; 'q': querying with the mouse
	private int mouseX, mouseY;						// current mouse location, when querying
	private int firstClick = 1;                    // keeps track of when the first dot is placed
	private PointQuadtree<Dot> tree = null;		// holds the dots
	private int mouseRadius = 100;					// circle around mouse location, for querying
	private boolean trackMouse = false;				// if true, then print out where the mouse is as it moves
	private List<Dot> found = null;					// who was found near mouse, when querying
	private int myX=65;
	private ArrayList<Dot> stringList=new ArrayList<>(); //the list of all the dots in order entered

	public DotTreeGUIEC() {
		super("dot tree", width, height);
	}

	/**
	 * DrawingGUI method, here keeping track of the location and redrawing to show it
	 */
	@Override
	public void handleMouseMotion(int x, int y) {

		if (mode == 'q') {
			mouseX = x; mouseY = y;
			repaint();
		}
		if (trackMouse) {
			System.out.println("@ ("+x+","+y+")");
		}
	}

	/**
	 * DrawingGUI method, here either adding a new point or querying near the mouse
	 */
	@Override
	public void handleMousePress(int x, int y) {
		if (mode == 'a') {
			// Add a new dot at the point
			firstClick--;

			if(firstClick==0){  // ensures that the first dot is placed on the first click
				Dot initialDot=new Dot(x, y);
				tree = new PointQuadtree<Dot>(initialDot, 0, 0, 800, 600);
				stringList.add(initialDot);  // adds the first dot to the stringList
			}
			else {  // if, it is not the fist click, it inserts in into the tree
				Dot newDot = new Dot(x, y);
				tree.insert(newDot);
				stringList.add(newDot);  // adds each dot to the stringList

			}
		}
		else if (mode == 'q') {
			// Set "found" to what tree says is near the mouse press
			found = tree.findInCircle(mouseX, mouseY, mouseRadius);
		}
		else {
			System.out.println("clicked at ("+x+","+y+")");
		}
		repaint();
	}

	/**
	 * DrawingGUI method, here toggling the mode between 'a' and 'q'
	 * and increasing/decresing mouseRadius via +/-
	 */
	@Override
	public void handleKeyPress(char key) {
		if (key=='a' || key=='q') mode = key;
		else if (key=='+') {
			mouseRadius += 10;
		}
		else if (key=='-') {
			mouseRadius -= 10;
			if (mouseRadius < 0) mouseRadius=0;
		}
		else if (key=='m') {
			trackMouse = !trackMouse;
		}

		repaint();
	}
	
	/**
	 * DrawingGUI method, here drawing the quadtree
	 * and if in query mode, the mouse location and any found dots
	 */
	@Override
	public void draw(Graphics g) {
		if (tree != null) drawTree(g, tree, 0);
		if (mode == 'q') {
			g.setColor(Color.BLACK);
			g.drawOval(mouseX-mouseRadius, mouseY-mouseRadius, 2*mouseRadius, 2*mouseRadius);			
			if (found != null) {
				g.setColor(Color.BLACK);
				for (Dot d : found) {
					g.fillOval((int)d.getX()-dotRadius, (int)d.getY()-dotRadius, 2*dotRadius, 2*dotRadius);
				}
			}
		}

		// Adds labels for each of the points on the PointQuadTree by parsing through stringList
		if(tree!=null) {
			for (int i = 0; i < stringList.size(); i++) {
				g.setColor(Color.BLACK);
				g.drawString(String.valueOf((char)(myX + i)), (int) stringList.get(i).getX() + 20, (int) stringList.get(i).getY());
			}
		}

	}

	/**
	 * Draws the dot tree
	 * @param g		the graphics object for drawing
	 * @param tree	a dot tree (not necessarily root)
	 * @param level	how far down from the root qt is (0 for root, 1 for its children, etc.)
	 */
	public void drawTree(Graphics g, PointQuadtree<Dot> tree, int level) {
		// Set the color for this level
		g.setColor(rainbow[level % rainbow.length]);
		// Draw this node's dot and lines through it

		// TODO: YOUR CODE HERE
		// Draws the first point in the tree and it's lines
		Dot p = tree.getPoint();
		g.fillOval((int)p.getX()-dotRadius, (int)p.getY()-dotRadius, 2*dotRadius, 2*dotRadius);
		g.drawLine(tree.getX1(), (int) p.getY(), tree.getX2(), (int) p.getY());
		g.drawLine((int) p.getX(), tree.getY1(), (int)p.getX(), tree.getY2());



		// TODO: YOUR CODE HERE
		// Recurse with children
		if(tree.hasChild(1)) {
			PointQuadtree<Dot> c1 = tree.getChild(1);
			drawTree(g, c1, level+1);  // draws child 1 with a different color
		}
		if(tree.hasChild(2)){
			PointQuadtree<Dot> c2 = tree.getChild(2);
			drawTree(g, c2, level+1);  // draws child 2 with a different color

		}
		if(tree.hasChild(3)) {
			PointQuadtree<Dot> c3 = tree.getChild(3);
			drawTree(g, c3, level+1);  // draws child 3 with a different color
		}
		if(tree.hasChild(4)) {
			PointQuadtree<Dot> c4 = tree.getChild(4);
			drawTree(g, c4, level+1);   // draws child 4 with a different color
		}

	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new DotTreeGUIEC();
			}
		});
	}
}
