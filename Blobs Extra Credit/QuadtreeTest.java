import java.util.List;

/**
 * Hard-coded tests for point quadtrees, Dartmouth CS 10, Fall 2017
 *
 * @author Chris Bailey-Kellogg, Fall 2017, extracted from other code, augmented
 * @author CBK, Winter 2021, minor improvements
 * @author Lindsey Kim
 * @author Kenneth Wu
 */
public class QuadtreeTest {
    /**
     * Is the tree of the expected size, both from size() and from allPoints()?
     *
     * @param tree
     * @param size
     * @return
     */
    private static int testSize(PointQuadtree<Dot> tree, int size) {
        int errs = 0;

        if (tree.size() != size) {
            errs++;
            System.err.println("wrong size: got " + tree.size() + " but expected " + size);
        }
        List<Dot> points = tree.allPoints();
        if (points.size() != size) {
            errs++;
            System.err.println("wrong points size: got " + points.size() + " but expected " + size);
        }

        return errs;
    }

    /**
     * A simple testing procedure, making sure actual is expected, and printing a message if not
     *
     * @param x                       query x coordinate
     * @param y                       query y coordinate
     * @param r                       query circle radius
     * @param expectedCircleRectangle how many times Geometry.circleIntersectsRectangle is expected to be called
     * @param expectedInCircle        how many times Geometry.pointInCircle is expected to be called
     * @param expectedHits            how many points are expected to be found
     * @return 0 if passed; 1 if failed
     */
    private static int testFind(PointQuadtree<Dot> tree, int x, int y, int r, int expectedCircleRectangle, int expectedInCircle, int expectedHits) {
        Geometry.resetNumInCircleTests();
        Geometry.resetNumCircleRectangleTests();
        int errs = 0;
        int num = tree.findInCircle(x, y, r).size();
        String which = "find near (" + x + "," + y + ") with radius " + r;
        if (Geometry.getNumCircleRectangleTests() != expectedCircleRectangle) {
            errs++;
            System.err.println(which + ": wrong # circle-rectangle, got " + Geometry.getNumCircleRectangleTests() + " but expected " + expectedCircleRectangle);
        }
        if (Geometry.getNumInCircleTests() != expectedInCircle) {
            errs++;
            System.err.println(which + ": wrong # in circle, got " + Geometry.getNumInCircleTests() + " but expected " + expectedInCircle);
        }
        if (num != expectedHits) {
            errs++;
            System.err.println(which + ": wrong # hits, got " + num + " but expected " + expectedHits);
        }
        return errs;
    }


    /**
     * test tree 0 -- first three points from figure in handout
     * hardcoded point locations for 800x600
     */
    private static void test0() {
        PointQuadtree<Dot> tree = new PointQuadtree<Dot>(new Dot(300, 400, "A"), 0, 0, 800, 600); // start with A
        tree.insert(new Dot(150, 450, "B"));
        tree.insert(new Dot(250, 550, "C"));
        int bad = 0;
        bad += testSize(tree, 3);
        bad += testFind(tree, 0, 0, 900, 3, 3, 3);        // rect for all; circle for all; find all
        bad += testFind(tree, 300, 400, 10, 3, 2, 1);        // rect for all; circle for A,B; find A
        bad += testFind(tree, 150, 450, 10, 3, 3, 1);        // rect for all; circle for all; find B
        bad += testFind(tree, 250, 550, 10, 3, 3, 1);        // rect for all; circle for all; find C
        bad += testFind(tree, 150, 450, 150, 3, 3, 2);    // rect for all; circle for all; find B, C
        bad += testFind(tree, 140, 440, 10, 3, 2, 0);        // rect for all; circle for A,B; find none
        bad += testFind(tree, 750, 550, 10, 2, 1, 0);        // rect for A,B; circle for A; find none
        if (bad == 0) System.out.println("test 0 passed!");
        else System.out.println("test 0 failed!");
        System.out.println(tree);

    }

    /**
     * test tree 1 -- figure in handout
     * hardcoded point locations for 800x600
     */
    private static void test1() {
        PointQuadtree<Dot> tree = new PointQuadtree<Dot>(new Dot(300, 400, "A"), 0, 0, 800, 600); // start with A
        tree.insert(new Dot(150, 450, "B"));
        tree.insert(new Dot(250, 550, "C"));
        tree.insert(new Dot(450, 200, "D"));
        tree.insert(new Dot(200, 250, "E"));
        tree.insert(new Dot(350, 175, "F"));
        tree.insert(new Dot(500, 125, "G"));
        tree.insert(new Dot(475, 250, "H"));
        tree.insert(new Dot(525, 225, "I"));
        tree.insert(new Dot(490, 215, "J"));
        tree.insert(new Dot(700, 550, "K"));
        tree.insert(new Dot(310, 410, "L"));
        int bad = 0;
        bad += testSize(tree, 12);
        bad += testFind(tree, 150,450,10,6,3,1); 	// rect for A [D] [E] [B [C]] [K]; circle for A, B, C; find B
        bad += testFind(tree, 500,125,10,8,3,1);		// rect for A [D [G F H]] [E] [B] [K]; circle for A, D, G; find G
        bad += testFind(tree, 300,400,15,10,6,2);	// rect for A [D [G F H]] [E] [B [C]] [K [L]]; circle for A,D,E,B,K,L; find A,L
        bad += testFind(tree, 495,225,50,10,6,3);	// rect for A [D [G F H [I [J]]]] [E] [B] [K]; circle for A,D,G,H,I,J; find H,I,J
        bad += testFind(tree, 0,0,900,12,12,12);		// rect for all; circle for all; find all
        if (bad==0) System.out.println("test 1 passed!");
        else System.out.println("test 1 failed!");
        System.out.println(tree);
    }

    private static void test2() {
        PointQuadtree<Dot> tree = new PointQuadtree<Dot>(new Dot(400, 300, "A"), 0, 0, 800, 600);
        tree.insert(new Dot(200, 200, "B"));
        tree.insert(new Dot(550, 150, "C"));
        tree.insert(new Dot(650, 500, "D"));
        tree.insert(new Dot(300, 250, "E"));
        tree.insert(new Dot(250, 225, "F"));
        tree.insert(new Dot(300, 100, "G"));
        tree.insert(new Dot(500, 500, "H"));
        tree.insert(new Dot(650, 450, "I"));
        int bad = 0;
        if(tree.size()!=9) {
            bad+=1;
        }
        bad += testFind(tree, 0, 0, 900, 9, 9, 9);
        bad += testFind(tree, 400, 300, 50, 9, 5, 1);
        bad += testFind(tree, 0, 0, 350, 7, 5, 3);
        if (bad==0) System.out.println("test 2 passed!");
        else System.out.println("test 2 failed!");
        System.out.println(tree);


    }

    // TODO: YOUR CODE HERE -- additional test case(s)

    public static void main(String[] args) {
        test0();
        test1();
        test2();
        // TODO: YOUR CODE HERE -- call additional test case(s)
    }
}
