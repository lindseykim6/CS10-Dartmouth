import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 * Webcam-based drawing
 * Scaffold for PS-1, Dartmouth CS 10, Fall 2016
 *
 *
 * @author Chris Bailey-Kellogg, Spring 2015 (based on a different webcam app from previous terms)
 * @author Kenneth Wu
 * @author Lindsey Kim
 *
 * 'p' switches to painting
 * 'r' recolors the image
 * 'w' turns on the webcam
 * 'c' clears the painting
 * 'o' saves the recolored image
 * 's' saves the painting
 * 'k' pauses and unpauses the painting
 * 'n' resets the painting
 * '0' selects the first brush
 * '1' selects the second brush
 * '2' selects both brushes
 * 'B' selects the current brush to blue
 * 'Y' selects the current brush to yellow
 * 'R' selects the current brush to red
 * 'G' selects the current brush to green
 * 'L' selects the current brush to black
 *
 */

public class CamPaintEC extends Webcam {
    private char displayMode = 'r';   // what to display: 'w': live webcam, 'r': recolored image, 'p': painting
    private RegionFinderEC finder;  // handles the finding for the first target color
    private RegionFinderEC finderTwo;  // handles the finding for the second target color
    private Color targetColor = Color.BLACK;  // default target color for first brush is black
    private Color targetColorTwo = Color.WHITE; // default target color for second brush is orange
    private Color paintColor = Color.blue;    // the color to put into the painting from the first "brush"
    private Color paintColorTwo = Color.RED;    // the color to put into the painting from the second "brush"
    private BufferedImage painting;  // the resulting masterpiece
    private int brushSelector = 0;  // determines which brush is used
    private boolean pause = Boolean.FALSE; // determines whether or not to pause the painting
    private boolean resetPainting=Boolean.FALSE;  // determines whether or not to reset the painting

    /**
     * Initializes the region finder and the drawing
     */
    public CamPaintEC() {
        finder = new RegionFinderEC();
        finderTwo= new RegionFinderEC();
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
        if (displayMode=='w') {  // opens up the webcam
            g.drawImage(image, 0, 0, null);
        }
        else if (displayMode=='r') { // opens up the recolored image
            if(brushSelector==0) {
                g.drawImage(finder.getRecoloredImage(), 0, 0, null);
            }
            else if (brushSelector==1){
                g.drawImage(finderTwo.getRecoloredImage(), 0, 0, null);
            }
            else if (brushSelector==2){
                g.drawImage(finder.getRecoloredImage(), 0, 0, null);
                g.drawImage(finderTwo.getRecoloredImage(), 0, 0, null);
            }
        }
        else if (displayMode=='p') {  // opens up the painting
            if (!resetPainting) {
                g.drawImage(painting, 0, 0, null);
            } else {
                clearPainting();
                restartState();
            }
        }

    }

    /**
     * Restarts the painting
     */
    private void restartState() {
        resetPainting=Boolean.FALSE;
    }

    /**
     * Webcam method, here finding regions and updating painting.
     */
    @Override
    public void processImage() {
        if(!pause) {
            if (image != null && targetColor != null) {  // checks to see if the image or targetColor is null
                // processes the first brush if it should be used
                if (brushSelector == 0 || brushSelector == 2) {
                    finder.setImage(image);
                    finder.findRegions(targetColor);  // finds all regions of targetColor with the first brush

                    if (displayMode == 'r') {
                        finder.recolorImage();  // recolors the image if the displayMode is for a recolored image
                    }
                    else if (displayMode == 'p') { // paints the largest region if the displayMode is for a painting
                        ArrayList<Point> largestRegion = finder.largestRegion();
                        for (int i = 0; i < largestRegion.size(); i++) {
                            int currentX = (int) largestRegion.get(i).getX();
                            int currentY = (int) largestRegion.get(i).getY();
                            painting.setRGB(currentX, currentY, paintColor.getRGB());  // updates the painting with the first brush
                        }
                    }
                }

                // processes the first brush if it should be used
                if (brushSelector == 1 || brushSelector == 2) {
                    processSecondImage();
                }
            }
        }

    }

    /**
     * Used to process the second brush
     */
    private void processSecondImage(){
        finderTwo.setImage(image);
        finderTwo.findRegions(targetColorTwo);  // finds all regions of targetColor with the second brush
        if (displayMode == 'r') {
            finderTwo.recolorImage(); // recolors the image if the displayMode is for a recolored image
        } else if (displayMode == 'p') { // paints the largest region if the displayMode is for a painting
            ArrayList<Point> largestRegion = finderTwo.largestRegion();
            for (int i = 0; i < largestRegion.size(); i++) {
                int currentX = (int) largestRegion.get(i).getX();
                int currentY = (int) largestRegion.get(i).getY();
                painting.setRGB(currentX, currentY, paintColorTwo.getRGB());  // updates the painting with the second brush
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
                if (brushSelector==0) {
                    targetColor = new Color(image.getRGB(x, y));  // sets the first brush to find the new targetColor
                }
                if (brushSelector==1) {
                    targetColorTwo=new Color(image.getRGB(x,y)); // sets the second brush to find the new targetColor
                }
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
        } else if (k == ' ') { // pauses and unpauses the painting
            pause = !pause;
        } else if (k == 'n') { // resets the painting
            resetPainting=Boolean.TRUE;
        } else if (k == '0') { // selects the first brush
            brushSelector=0;
        } else if (k == '1') { // selects the second brush
            brushSelector = 1;
        }else if (k == '2') { // selects both brushes
            brushSelector=2;
        } else if (k == 'B') { // sets the brush to blue, depending on what brush is selected
            if (brushSelector==0) {
                paintColor = Color.blue;
            }
            else if (brushSelector==1) {
                paintColorTwo = Color.blue;
            }
        } else if (k == 'Y') { // sets the brush to yellow, depending on what brush is selected
            if (brushSelector==0) {
                paintColor = Color.yellow;
            }
            else if (brushSelector==1) {
                paintColorTwo = Color.yellow;
            }
        } else if (k == 'R') { // sets the brush to red, depending on what brush is selected
            System.out.println(k);
            if (brushSelector==0) {
                paintColor = Color.red;
            }
            else if (brushSelector==1) {
                paintColorTwo = Color.red;
            }
        } else if (k=='G') { // sets the brush to green, depending on what brush is selected
            if (brushSelector==0) {
                paintColor = Color.green;
            }
            else if (brushSelector==1) {
                paintColorTwo = Color.green;
            }
        } else if(k=='L') { // sets the brush to black, depending on what brush is selected
            if (brushSelector==0) {
                paintColor = Color.black;
            }
            else if (brushSelector==1) {
                paintColorTwo = Color.black;
            }
        } else {  // handles unexpected keys
            System.out.println("unexpected key " + k);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new CamPaintEC();
            }
        });
    }
}
