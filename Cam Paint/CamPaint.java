import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 * Webcam-based drawing
 * Scaffold for PS-1, Dartmouth CS 10, Fall 2016
 *
 * @author Chris Bailey-Kellogg, Spring 2015 (based on a different webcam app from previous terms)
 * @author Kenneth Wu
 * @author Lindsey Kim
 */
public class CamPaint extends Webcam {
    private char displayMode = 'r';            // what to display: 'w': live webcam, 'r': recolored image, 'p': painting
    private RegionFinder finder;            // handles the finding
    private Color targetColor = Color.BLACK;            // color of regions of interest (set by mouse press)
    private Color paintColor = Color.blue;    // the color to put into the painting from the "brush"
    private BufferedImage painting; // the resulting masterpiece
    /**
     * Initializes the region finder and the drawing
     */
    public CamPaint() {
        finder = new RegionFinder();
        clearPainting();

    }

    /**
     * Resets the painting to a blank image
     */
    protected void clearPainting() {
        painting = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    }

    /**
     * DrawingGUI method, here drawing one of live webcam, recolored image, or painting,
     * depending on display variable ('w', 'r', or 'p')
     */
    @Override
    public void draw(Graphics g) {
        if (displayMode=='w') { // opens up the webcam
            g.drawImage(image, 0, 0, null);
        }
        else if (displayMode=='r') {  // opens up the recolored image
            g.drawImage(finder.getRecoloredImage(), 0, 0, null);
        }
        else if (displayMode=='p') {  // opens up the painting
            g.drawImage(painting, 0,0, null);
        }

    }

    /**
     * Webcam method, here finding regions and updating painting.
     */
    @Override
    public void processImage() {
        if (image!=null && targetColor!=null ) {  // checks to see if the image or targetColor is null
            finder.setImage(image);
            finder.findRegions(targetColor);  // finds all regions of targetColor with the brush

            if (displayMode == 'r') {
                finder.recolorImage();  // recolors the image if the displayMode is for a recolored image
            }
            else if (displayMode == 'p') {  // paints the largest region if the displayMode is for a painting
                ArrayList<Point> largestRegion = finder.largestRegion();
                for (int i = 0; i < largestRegion.size(); i++) {
                    int currentX = (int) largestRegion.get(i).getX();
                    int currentY = (int) largestRegion.get(i).getY();
                    painting.setRGB(currentX, currentY, paintColor.getRGB());  // updates the painting
                }
            }
        }

    }

    /**
     * Overrides the DrawingGUI method to set targetColor.
     */
    @Override
    public void handleMousePress(int x, int y) {
        if (image != null) { // to be safe, make sure webcam is grabbing an image
            if(displayMode=='r') {  //checks to see if it is in the recolored image mode
                targetColor = new Color(image.getRGB(x, y));  // sets the brush to find the new targetColor
            }
        }
    }


    /**
     * DrawingGUI method, here doing various drawing commands
     */
    @Override
    public void handleKeyPress(char k) {
        if (k == 'p' || k == 'r' || k == 'w') { // display: painting, recolored image, or webcam
            displayMode = k;
        } else if (k == 'c') { // clear
            clearPainting();
        } else if (k == 'o') { // save the recolored image
            saveImage(finder.getRecoloredImage(), "pictures/recolored.png", "png");
        } else if (k == 's') { // save the painting
            saveImage(painting, "pictures/painting.png", "png");
        } else {
            System.out.println("unexpected key " + k);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new CamPaint();
            }
        });
    }
}
