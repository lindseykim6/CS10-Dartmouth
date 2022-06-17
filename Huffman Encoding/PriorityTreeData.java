public class PriorityTreeData {
    private char c;  // holds the character
    private int frequency;  // holds the number of times that the character has appeared

    // initialize instance variables
    public PriorityTreeData(char c, int frequency) {
        this.c = c;
        this.frequency = frequency;
    }

    // returns the character
    public char getCharacter() {
        return c;
    }

    // returns the frequency
    public int getFrequency() {
        return frequency;
    }

    // changes the character
    public void setCharacter(char newC) {
        c = newC;
    }

    // changes the frequency
    public void setFrequency(int newFrequency) {
        frequency = newFrequency;
    }

    // changes the print statement to include the character and its frequency
    @Override
    public String toString() {
        return "c=" + c + ", frequency=" + frequency + '}';
    }
}