import java.util.ArrayList;
import java.util.List;

/**
 * A point quadtree: stores an element at a 2D position,
 * with children at the subdivided quadrants
 *
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Spring 2015
 * @author CBK, Spring 2016, explicit rectangle
 * @author CBK, Fall 2016, generic with Point2D interface
 * @author Lindsey Kim
 * @author Kenneth Wu
 *
 */
public class PointQuadtree<E extends Point2D> {
    private E point;							// the point anchoring this node
    private int x1, y1;							// upper-left corner of the region
    private int x2, y2;							// lower-right corner of the region
    private PointQuadtree<E> c1, c2, c3, c4;	// children

    /**
     * Initializes a leaf quadtree, holding the point in the rectangle
     */
    public PointQuadtree(E point, int x1, int y1, int x2, int y2) {
        this.point = point;
        this.x1 = x1; this.y1 = y1; this.x2 = x2; this.y2 = y2;
    }

    // Getters

    public E getPoint() {
        return point;
    }

    public int getX1() {
        return x1;
    }

    public int getY1() {
        return y1;
    }

    public int getX2() {
        return x2;
    }

    public int getY2() {
        return y2;
    }

    /**
     * Returns the child (if any) at the given quadrant, 1-4
     * @param quadrant	1 through 4
     */
    public PointQuadtree<E> getChild(int quadrant) {
        if (quadrant==1) return c1;
        if (quadrant==2) return c2;
        if (quadrant==3) return c3;
        if (quadrant==4) return c4;
        return null;
    }

    /**
     * Returns whether or not there is a child at the given quadrant, 1-4
     * @param quadrant	1 through 4
     */
    public boolean hasChild(int quadrant) {
        return (quadrant==1 && c1!=null) || (quadrant==2 && c2!=null) || (quadrant==3 && c3!=null) || (quadrant==4 && c4!=null);
    }

    /**
     * Inserts the point into the tree
     */
    public void insert(E p2) {
        if(x1 <= (int) p2.getX() && p2.getX()<(int) point.getX()) {  // Determines if it is the on the left hand side
            if (y1 <= (int) p2.getY() && (int)p2.getY() < (int) point.getY()) {  // Determines if it is the in quadrant 2
                if (hasChild(2)) {
                    getChild(2).insert(p2);  // recurses through the second child until it finds the child that doesn't have a child
                } else {
                    c2 = new PointQuadtree(p2, x1, y1, (int) point.getX(), (int) point.getY());  // sets the second child to be a new quadtree at the point
                }
            }
            else if((int)point.getY()<=(int)p2.getY() && (int)p2.getY()<=y2){  // Determines if it is the in quadrant 3
                if (hasChild(3)) {
                    getChild(3).insert(p2);  // recurses through the third child until it finds the child that doesn't have a child
                } else {
                    c3 = new PointQuadtree(p2, x1, (int) point.getY(), (int) point.getX(), y2);  // sets the third child to be a new quadtree at the point
                }
            }
        }

        else if ((int) point.getX() <= (int)p2.getX() && (int) p2.getX()<= x2) {  // Determines if it is the on the right hand side
            if (y1 <= (int)p2.getY() && (int) p2.getY() < (int) point.getY()) {  // Determines if it is the in quadrant 1
                if (hasChild(1)) {
                    getChild(1).insert(p2);  // recurses through the first child until it finds the child that doesn't have a child
                } else {
                    c1 = new PointQuadtree(p2, (int)point.getX(), y1, x2, (int)point.getY());  // sets the first child to be a new quadtree at the point
                }
            }
            else if((int)point.getY()<=(int)p2.getY() && (int)p2.getY()<=y2){  // Determines if it is the in quadrant 4
                if (hasChild(4)) {
                    getChild(4).insert(p2);  // recurses through the fourth child until it finds the child that doesn't have a child
                } else {
                    c4 = new PointQuadtree(p2, (int)point.getX(), (int) point.getY(), x2, y2);  // sets the fourth child to be a new quadtree at the point
                }
            }

        }

    }

    /**
     * Finds the number of points in the quadtree (including its descendants)
     */
    public int size() {
        // TODO: YOUR CODE HERE -- compute directly, using only numbers not lists (i.e., don't just call allPoints() and return its size)
        int num = 1;  // accounts for the initial point

        // adds to num every time there is a child
        if(hasChild(1)) num+=c1.size();
        if(hasChild(2)) num+=c2.size();
        if(hasChild(3)) num+=c3.size();
        if(hasChild(4)) num+=c4.size();

        return num;
    }

    /**
     * Builds a list of all the points in the quadtree (including its descendants)
     */
    public List<E> allPoints() {
        ArrayList<E> all = new ArrayList<E>();  // stores all the points
        addToAllPoints(all);  // helper method that finds the list of all points
        return all;

    }


    /**
     * Uses the quadtree to find all points within the circle
     * @param cx	circle center x
     * @param cy  	circle center y
     * @param cr  	circle radius
     * @return    	the points in the circle (and the qt's rectangle)
     */
    public List<E> findInCircle(double cx, double cy, double cr) {
        // TODO: YOUR CODE HERE -- efficiency matters!
        ArrayList<E> hits= new ArrayList<>();  // stores all the points that it found
        findList(hits, cx, cy, cr);  // helper method that finds the list of hits
        return hits;

    }

    /**
     * Recursively finds all the points in the tree and stores it in a given list
     * @param allPoints	the list of all Points
     */
    // TODO: YOUR CODE HERE for any helper methods
    private void addToAllPoints(ArrayList<E> allPoints) {
        allPoints.add(point);  // adds each point to the list

        // recurses through each child to find each point
        if (hasChild(1)) c1.addToAllPoints(allPoints);
        if (hasChild(2)) c2.addToAllPoints(allPoints);
        if (hasChild(3)) c3.addToAllPoints(allPoints);
        if (hasChild(4)) c4.addToAllPoints(allPoints);
    }

    /**
     * Recursively finds all the points that are inside the circle and stores it in a given list
     * @param allHits the list of all the hits
     * @param cx	circle center x
     * @param cy  	circle center y
     * @param cr  	circle radius
     */

    private void findList(ArrayList<E> allHits, double cx, double cy, double cr) {
        if(Geometry.circleIntersectsRectangle(cx, cy, cr, x1, y1, x2, y2)) {  // only checks the points with quadrants that intersect the circle
            if(Geometry.pointInCircle(point.getX(), point.getY(), cx,cy,cr)) {  // only adds the points with quadrants that intersect the circle
                allHits.add(point);  // adds the point to the list
            }
            // recurses through each child to check if each child is within the circle
            if (hasChild(1)){
                c1.findList(allHits,cx,cy,cr);
            }
            if(hasChild(2)) {
                c2.findList(allHits,cx,cy,cr);
            }
            if (hasChild(3)) {
                c3.findList(allHits,cx,cy,cr);
            }
            if(hasChild(4)) {
                c4.findList(allHits,cx,cy,cr);
            }

        }
    }

    /**
     * Returns a string representation of the tree
     */
    public String toString(){
        return toStringHelper("");
    }

    /**
     * Recursively constructs a String representation of the tree from this node,
     * starting with the given indentation and indenting further going down the tree
     **/
    public String toStringHelper(String indent) {
        String res = indent + point + "\n";
        if (hasChild(1)) res += c1.toStringHelper(indent+"  ");
        if (hasChild(2)) res += c2.toStringHelper(indent+"  ");
        if (hasChild(3)) res += c3.toStringHelper(indent+"  ");
        if (hasChild(4)) res += c4.toStringHelper(indent+"  ");
        return res;
    }




}


