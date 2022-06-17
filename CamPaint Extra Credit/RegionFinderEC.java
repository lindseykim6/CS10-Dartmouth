import java.awt.*;
import java.awt.image.*;
import java.util.*;

/**
 * Region growing algorithm: finds and holds regions in an image.
 * Each region is a list of contiguous points with colors similar to a target color.
 * Scaffold for PS-1, Dartmouth CS 10, Fall 2016
 * 
 * @author Chris Bailey-Kellogg, Winter 2014 (based on a very different structure from Fall 2012)
 * @author Travis W. Peters, Dartmouth CS 10, Updated Winter 2015
 * @author CBK, Spring 2015, updated for RegionsTest.CamPaint
 * @author Kenneth Wu
 * @author Lindsey Kim
 */

public class RegionFinderEC {
	private static final int maxColorDiff = 70;                // how similar a pixel color must be to the target color, to belong to a region
	// suitable value for maxColorDiff depends on your implementation of colorMatch() and how much difference in color you want to allow
	private static final int minRegion = 50;                // how many points in a region to be worth considering

	private BufferedImage image;                            // the image in which to find regions
	private BufferedImage recoloredImage;                   // the image with identified regions recolored

	private ArrayList<ArrayList<Point>> regions = new ArrayList<>();            // a region is a list of points so the identified regions
	// are in a list of lists of point

	private int r = 1; // how many neighbors around pixel to check

	public RegionFinderEC() {
		this.image = null;
	}

	public RegionFinderEC(BufferedImage image) {
		this.image = image;
	}

	public void setImage(BufferedImage image) {
		this.image = image;
	}

	public BufferedImage getImage() { return image;}

	public BufferedImage getRecoloredImage() {
		return recoloredImage;
	}


	/**
	 * Sets regions to the flood fill regions in the image, similar enough to the targetColor.
	 */
	public void findRegions(Color targetColor) {
		// visited keeps track of what pixels have been visited
		BufferedImage visited = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
		ArrayList<Point> toVisit = new ArrayList<Point>();  // keeps track of what pixels need to be visited
		for (int y = 0; y < image.getHeight(); y++) { // loops through all the pixels
			for (int x = 0; x < image.getWidth(); x++) {
				Color currentColor = new Color(image.getRGB(x, y)); // grabs the color of the pixel
				if (visited.getRGB(x, y) == 0 && colorMatch(targetColor, currentColor)) { //checks to see if it matches and is unvisited
					ArrayList<Point> newRegion = new ArrayList<Point>(); // a single region in the image
					toVisit.add(new Point(x, y));  // adds it to be visited
					while (toVisit.size() != 0) {  // visits all the points
						Point currentPoint = (Point) toVisit.get(toVisit.size() - 1);  //grabs the next point to be visited
						// and adds it to the region
						int currentPointX = (int) currentPoint.getX();
						int currentPointY = (int) currentPoint.getY();
						toVisit.remove(toVisit.size() - 1); // removes the last point from to visit
						newRegion.add(currentPoint);
						visited.setRGB(currentPointX, currentPointY, 1);  // marks the point as visited
						// checks the point's neighbors
						for (int ny = Math.max(0, currentPointY - r); ny < Math.min(image.getHeight(), currentPointY + 1 + r);
							 ny++) {
							for (int nx = Math.max(0, currentPointX - r); nx < Math.min(image.getWidth(), currentPointX + 1 + r); nx++) {
								Color neighborColor = new Color(image.getRGB(nx, ny));
								if (colorMatch(targetColor, neighborColor) && visited.getRGB(nx, ny) == 0) {
									Point newPoint = new Point(nx, ny); // if it matches and is not visited, add it to be visited
									toVisit.add(newPoint);
								}
							}
						}
					}
					if (newRegion.size() >= minRegion) {
						regions.add(newRegion); //only adds regions of at least minRegion size
					}
				}
			}
		}
	}

	/**
	 * Tests whether the two colors are "similar enough" (your definition, subject to the maxColorDiff threshold,
	 * which you can vary)
	 **/
	private static boolean colorMatch(Color c1, Color c2) {
		if (Math.abs(c1.getRed() - c2.getRed()) <= maxColorDiff) {
			if (Math.abs(c1.getBlue() - c2.getBlue()) <= maxColorDiff) {
				if (Math.abs(c1.getGreen() - c2.getGreen()) <= maxColorDiff) {
					return Boolean.TRUE;
				} else {
					return Boolean.FALSE;
				}
			} else {
				return Boolean.FALSE;
			}
		} else {
			return Boolean.FALSE;
		}
	}

	/**
	 * Returns the largest region detected (if any region has been detected)
	 */
	public ArrayList<Point> largestRegion() {
		ArrayList<Point> biggest = new ArrayList<Point>();
		for (int i = 0; i < regions.size(); i++) {
			if (regions.get(i).size() > biggest.size()) {
				biggest = regions.get(i);
			}
		}
		return biggest;
	}

	/**
	 * Sets recoloredImage to be a copy of image,
	 * but with each region a uniform random color,
	 * so we can see where they are
	 */
	public void recolorImage() {
		// First copy the original
		recoloredImage = new BufferedImage(image.getColorModel(), image.copyData(null), image.getColorModel().isAlphaPremultiplied(), null);
		// TODO: YOUR CODE HERE
		if (regions.size() > 0) {
			for (int region = 0; region < regions.size(); region++) {
				int randomRed = (int) (Math.random() * 255);
				int randomGreen = (int) (Math.random() * 255);
				int randomBlue = (int) (Math.random() * 255);
				Color regionColor = new Color(randomRed, randomGreen, randomBlue); //sets regionColor to be a random color
				for (int point = 0; point < regions.get(region).size(); point++) {
					int currentX = (int) regions.get(region).get(point).getX();
					int currentY = (int) regions.get(region).get(point).getY();
					recoloredImage.setRGB(currentX, currentY, regionColor.getRGB());  // alters recoloredImage to have random colors
				}
			}
		}
		regions = new ArrayList<>();  // initializes a new regions everytime the method is called
	}


}
