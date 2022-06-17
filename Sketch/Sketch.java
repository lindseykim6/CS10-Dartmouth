import java.awt.*;
import java.util.TreeMap;
/**
 * @author Lindsey Kim
 * Creates a sketch object
 */

public class Sketch {
    private int id; // the id of the shape
    private TreeMap<Integer, Shape> idSketchMap;  // the map of ids to shape

    /**
     * Constructs a sketch
     */
    public Sketch() {
        id = 0;  //initialize the id to 0 initially
        idSketchMap = new TreeMap<>(); // initialize the tree map
    }

    /**
     * @return the id sketch map
     */
    public TreeMap<Integer, Shape> getIdSketchMap() { // returns the id shape map
        return idSketchMap;
    }

    /**
     * @return incremented id (by one)
     */
    public int incrementID() {  // increments the id and returns it
        id = id + 1;
        return id;
    }

    /**
     * @param s the shape we want to add
     * @param shapeID the id of the shape we want to aadd
     * puts a new shape with given shape id into the map
     */
    public synchronized void newShape(int shapeID, Shape s) {  // puts a new shape with a given shape id into the map
        idSketchMap.put(shapeID, s);
    }

    /**
     * @param shapeID the id of the shape we want to delete
     * removes a shpae with a given shape id
     */
    public synchronized void removeShape(int shapeID) {  // removes the shape with the given id
        idSketchMap.remove(shapeID);
    }

    /**
     * @param mousePress the point pressed
     * gets the shape that is pressed
     */
    public synchronized Shape getShape(Point mousePress) {  // gets the shape that is pressed on
        for (Integer i : idSketchMap.descendingKeySet()) {  // parses through the sketch map in top-to-bottom order
            if (idSketchMap.get(i).contains((int) mousePress.getX(), (int) mousePress.getY())) {  // if they find it
                return idSketchMap.get(i);  // return it
            }
        }
        return null;  // return null if not found
    }

    /**
     * @param key the id we look for
     * gets the shape with a certain key
     */
    public synchronized Shape getShapeKey(int key) {  //gets the shape with a certain key
        if (idSketchMap.containsKey(key)) {  // checks if it exists
            return idSketchMap.get(key);  //  returns the shape
        }
        return null;
    }

    /**
     * @param shape the shape we look for
     * parses through the shapes to find the one we are looking for
     */
    public synchronized int identifyShape(Shape shape) {
        for (Integer i : idSketchMap.keySet()) {  // parses through the shapes to find the one we are looking for
            if (idSketchMap.get(i).equals(shape)) {  // if found
                return i;  // return it
            }
        }
        return -1;  // otherwise return -1
    }
}


