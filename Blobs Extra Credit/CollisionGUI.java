import java.awt.*;

import javax.swing.*;

import java.util.List;
import java.util.ArrayList;

/**
 * Using a quadtree for collision detection
 * 
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Spring 2015
 * @author CBK, Spring 2016, updated for blobs
 * @author CBK, Fall 2016, using generic PointQuadtree
 * @author Lindsey Kim
 * @author Kenneth Wu
 */
public class CollisionGUI extends DrawingGUI {
	private static final int width=800, height=600;		// size of the universe

	private List<Blob> blobs;						// all the blobs
	private List<Blob> colliders;;					// the blobs who collided at this step
	private char blobType = 'b';						// what type of blob to create
	private char collisionHandler = 'c';				// when there's a collision, 'c'olor them, or 'd'estroy them
	private int delay = 100;							// timer control

	public CollisionGUI() {
		super("super collider", width, height);

		blobs = new ArrayList<Blob>();

		// Timer drives the animation.
		startTimer();
	}

	/**
	 * Adds a blob of the current blobType at the location
	 */
	private void add(int x, int y) {
		if (blobType=='b') {
			blobs.add(new Bouncer(x,y,width,height));   // adds a bouncer
		}
		else if (blobType=='w') {
			blobs.add(new Wanderer(x,y));    // adds a wanderer
		}
		else {
			System.err.println("Unknown blob type "+blobType);
		}
	}

	/**
	 * DrawingGUI method, here creating a new blob
	 */
	public void handleMousePress(int x, int y) {
		add(x,y);    //  adds the blob to (x,y)
		repaint();
	}

	/**
	 * DrawingGUI method
	 */
	public void handleKeyPress(char k) {
		if (k == 'f') { // faster
			if (delay>1) delay /= 2;
			setTimerDelay(delay);
			System.out.println("delay:"+delay);
		}
		else if (k == 's') { // slower
			delay *= 2;
			setTimerDelay(delay);
			System.out.println("delay:"+delay);
		}
		else if (k == 'r') { // add some new blobs at random positions
			for (int i=0; i<10; i++) {
				add((int)(width*Math.random()), (int)(height*Math.random()));
				repaint();
			}			
		}
		else if (k == 'c' || k == 'd') { // control how collisions are handled
			collisionHandler = k;
			System.out.println("collision: "+k);
		}
		else if(k=='0') {    // test case 0
			test0();
		}
		else if(k=='1') {   // test case 1
			test1();
		}
		else { // set the type for new blobs
			blobType = k;			
		}
	}

	/**
	 * DrawingGUI method, here drawing all the blobs and then re-drawing the colliders in red
	 */
	public void draw(Graphics g) {
		// TODO: YOUR CODE HERE
		// Ask all the blobs to draw themselves.
		for(Blob blob: blobs) {
			g.setColor(Color.BLACK);
			blob.draw(g);
		}

		// Ask the colliders to draw themselves in red.
		if (blobs.size() > 0) {
			findColliders();
			if (collisionHandler=='c') {
				for (Blob collider: colliders) {
					g.setColor(Color.RED);
					collider.draw(g);
				}
			}
		}
	}

	/**
	 * Sets colliders to include all blobs in contact with another blob
	 */
	private void findColliders() {
		// TODO: YOUR CODE HERE
		colliders=new ArrayList<>();  // initializes the colliders (if the colliders should stay permanently red, this needs to be initialized in the beginning)

		// Create the tree by grabbing all points in blobs
		Blob firstBlob=blobs.get(0);
		PointQuadtree<Blob> tree= new PointQuadtree<>(firstBlob, 0,0, width, height);
		for(int i=1; i< blobs.size(); i++){
			tree.insert(blobs.get(i));
		}

		// For each blob, see if anybody else collided with it
		for(Blob blob: tree.allPoints()) {
			List<Blob> allColliders = tree.findInCircle(blob.getX(), blob.getY(), 2 * blob.getR());
			allColliders.remove(blob);  // removes itself from the collision list
			colliders.addAll(allColliders);   // adds each blob's collisions to colliders
		}

	}

	/**
	 * DrawingGUI method, here moving all the blobs and checking for collisions
	 */
	public void handleTimer() {
		// Ask all the blobs to move themselves.
		for (Blob blob : blobs) {
			blob.step();
		}

		// Check for collisions
		if (blobs.size() > 0) {
			findColliders();
			if (collisionHandler=='d') {
				blobs.removeAll(colliders);
				colliders = null;
			}
		}
		// Now update the drawing
		repaint();
	}

	/**
	 * Tests two bouncers that will eventually collide along the x axis
	 */
	private void test0(){
		Bouncer bouncer= new Bouncer(100,400, width, height);
		bouncer.setVelocity(3,0);  // sets the velocity to be going side to side
		blobs.add(bouncer);
		Bouncer bouncer2= new Bouncer(500,400, width, height);
		bouncer2.setVelocity(-3,0);  // sets the velocity to be going side to side
		blobs.add(bouncer2);
	}

	/**
	 * Tests two bouncers that will never collide and bounce vertically up and down
	 */
	private void test1(){
		Bouncer bouncer= new Bouncer(490,200, width, height);
		bouncer.setVelocity(0,10);  // sets the velocity to be going up and down
		blobs.add(bouncer);
		Bouncer bouncer2= new Bouncer(500,200, width, height);
		bouncer2.setVelocity(0,-10);  // sets the velocity to be going up and down
		blobs.add(bouncer2);
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new CollisionGUI();
			}
		});
	}
}
