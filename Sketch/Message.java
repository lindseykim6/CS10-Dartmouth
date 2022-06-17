import java.awt.*;

/**
 * @author Lindsey Kim
 * Creates a message object to parse through a message
 */

public class Message {
    private String modes;
    private int movingID;
    private Color color;
    private int x1;
    private int x2;
    private int y1;
    private int y2;
    private int changeX;
    private int changeY;
    private String shape;
    private int colorRGB;

    /**
     * Reads through the message and determines what information it gives
     * @param line the message
     */
    public void parseMessage(String line){
        String[] tokens = line.split(" ");  // split the message by a space
        if(tokens[0].equals("draw")){ // if in draw mode (first token
            modes=tokens[0];  // set mode to the first token
            movingID = Integer.parseInt(tokens[1]); // set moving id to the second token
            shape=tokens[2]; // shape is third token
            x1=Integer.parseInt(tokens[3]); // x1 is fourth token
            y1=Integer.parseInt(tokens[4]); // y1 is the fifth token
            x2=Integer.parseInt(tokens[5]); // x2 is the sixth token
            y2=Integer.parseInt(tokens[6]); // y2 is the seventh token
            colorRGB=Integer.parseInt(tokens[7]); // sets the rgb value of the 8th token
            color=new Color(Integer.parseInt(tokens[7])); // sets the color value of the 8th token
        }
        else if(tokens[0].equals("move")){  // if in move mode
            modes=tokens[0];  // sets the first token to be mode
            movingID = Integer.parseInt(tokens[1]); // sets the second token to be moving ID
            changeX=Integer.parseInt(tokens[2]); // sets the change in x to be the third token
            changeY=Integer.parseInt(tokens[3]);  // sets the change in y to be the fourth token
        }

        else if(tokens[0].equals("recolor")){  // if in recolor mode
            modes=tokens[0];  // sets the first token to be mode
            colorRGB=Integer.parseInt(tokens[1]); // sets the rgb of the second token
            color=new Color(Integer.parseInt(tokens[1]));  // sets the color of the second token
            movingID = Integer.parseInt(tokens[2]); // sets the moving id to be the third token
        }

        else if(tokens[0].equals("delete")){  // if in delete mode
            modes=tokens[0];  // set the first token to be mode
            movingID=Integer.parseInt(tokens[1]); // set the second token to be the movingID
        }
    }

    /**
     * @return color
     */
    public Color getColor() {
        return color;
    }

    /**
     * @return change in x
     */
    public int getChangeX() {
        return changeX;
    }

    /**
     * @return movingID
     */
    public int getMovingID() {
        return movingID;
    }

    /**
     * @return change in Y
     */
    public int getChangeY() {
        return changeY;
    }

    /**
     * @return x1
     */
    public int getX1() {
        return x1;
    }

    /**
     * @return x2
     */

    public int getX2() {
        return x2;
    }

    /**
     * @return y1
     */
    public int getY1() {
        return y1;
    }

    /**
     * @return y2
     */
    public int getY2() {
        return y2;
    }

    /**
     * @return mode
     */
    public String getModes() {
        return modes;
    }

    /**
     * @return shape
     */
    public String getShape() {
        return shape;
    }

    /**
     * Sets moving ID
     * @param movingID the new id
     */
    public void setMovingID(int movingID) {
        this.movingID = movingID;
    }

    /**
     * @return an updated message
     */
    @Override
    public String toString() {
        // returns an updated message in draw mode
        if(modes.equals("draw")) {
            return modes + " " + movingID+ " " + shape +" " + x1 + " " + y1 + " " + x2 + " " + y2 + " " + colorRGB;
        }
        // returns an updated message in move mode
        else if (modes.equals("move")) {
            return modes + " " + movingID+ " " + changeX +" "+ changeY;
        }
        // returns an updated message in recolor mode
        else if (modes.equals("recolor")) {
            return modes + " " + colorRGB + " " + movingID;
        }
        // returns an updated message in deleted mode
        else {
            return modes + " " + movingID;
        }
    }
}
