import java.io.*;
import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;
import java.util.TreeMap;

public class HuffmanEncoding {
    private String pathName;  // holds the location of the file path to be compressed
    private String compressedPathName;  // holds the location of the compressed file
    private String decompressedPathName;  // holds the location of the decompressed file
    private TreeMap<Character, Integer> frequencyMap = new TreeMap<>();  // creates a new map of characters and their frequencies
    private Comparator<BinaryTree<PriorityTreeData>> treeCompare= new TreeComparator();  // allows for binary trees to be compared based on character frequency
    private PriorityQueue<BinaryTree<PriorityTreeData>> priorityQueue= new PriorityQueue<>(treeCompare);  // creates a new priority queue that sorts based on the frequencies of characters
    BinaryTree<PriorityTreeData> tree;  // initializes a new tree
    BufferedReader input;  // initializes the file reader
    BufferedBitWriter bitOutput;  // initializes a bit writer

    /**
     * Constructs the Huffman Coding application
     *
     * @param pathName the name of the file to be compressed
     * @param compressedPathName  the name of the file that holds the compressed text
     * @param decompressedPathName the name of the file that holds the decompressed text
     * 
     */
    public HuffmanEncoding(String pathName, String compressedPathName, String decompressedPathName){
        this.pathName = pathName;
        this.compressedPathName=compressedPathName;
        this.decompressedPathName=decompressedPathName;
    }

    /**
     * Compresses the file into the file in pathName into the file in compressedPathName using a map
     */
    public void compress(){

        createQueue();  // creates a priority queue

        try {
            tree = createTree();  // creates a new frequency tree
            // System.out.println(tree);  // This prints out the tree of the characters and their frequencies
        }
        catch(NoSuchElementException e) {
            System.err.println("Cannot compress an empty file.");  // prints an exception for an empty file
            return;
        }

        TreeMap<Character,String> bitMap = findCodeWords();
        // System.out.println(bitMap); // This prints out the map of characters to bits

        try {
            input = new BufferedReader(new FileReader(pathName));  // utilizes BufferedReader to read from pathName
            bitOutput = new BufferedBitWriter(compressedPathName);  // utilizes BufferedBitWriter to write bits to compressedPathName
        }

        catch (FileNotFoundException e){
            System.err.println("Make sure the input file exists. \n" + e.getMessage());  // prints an exception if the file does not have a valid name
            return;
        }

        try {
            int inputInt = input.read(); // Read next character's integer representation
            while (inputInt != -1) {  // while there are still characters to read
                char c = (char)inputInt;  // converts the integer version of the input to a character
                String characterPath= bitMap.get(c);  // gets the code word version of each character
                for (int i = 0; i<characterPath.length(); i++) {  // iterates through the code word
                    if (characterPath.charAt(i) == '0') {  // if the code word holds a 0, use a False in the output
                        bitOutput.writeBit(Boolean.FALSE);
                    } else {  // if the code word holds a 1, use a True in the output
                        bitOutput.writeBit(Boolean.TRUE);
                    }
                }
                inputInt = input.read(); // Read next character's integer representation
            }

            input.close(); // closes the file reader that reads pathName
            bitOutput.close();  // closes the file writer that writes the compressed file

        } catch(IOException e) {
            System.err.println("IO exception.\n" + e.getMessage());  // prints an IO exception if it can't be read
        }

    }

    /**
     * Decompresses the file in compressedPathName into the file in decompressedPathName by parsing through the bits
     */
    public void decompress() {
        BufferedWriter output;
        BufferedBitReader bitInput;
        try {
            bitInput = new BufferedBitReader(compressedPathName);  // creates a new BufferedBitReader that reads compressedPathName
            output = new BufferedWriter(new FileWriter(decompressedPathName));  // creates a new BufferedWriter that writes in decompressedPathName
        }
        catch (IOException e) {
            System.err.println("Make sure the compressed file exists.\n" + e.getMessage());  // makes sure it can open the reader and writer
            return;
        }

        try { // try to read the file
            BinaryTree<PriorityTreeData> secondTree=tree;  // keeps track of the beginning node in the tree

            try { //checks if the tree exists
                while (bitInput.hasNext()) {  // checks to see if the file has been completely read
                    boolean bit = bitInput.readBit();  // reads the next bit in the file

                    if (bit == Boolean.FALSE) { // if the bit is a '0'
                        if (tree.hasLeft()) {
                            tree = tree.getLeft(); // go to the left if it has a left node
                        }
                    }

                    else { // if the bit is a '1'
                        if (tree.hasRight()) {
                            tree = tree.getRight(); // go to the right if it has a right node
                        }
                    }

                    if (tree.isLeaf()) {  // if it reaches the leaf
                        output.write(tree.getData().getCharacter()); // write the character in decompressedPathName
                        tree = secondTree;  // resets the tree to the starting node
                    }
                }

            } catch (NullPointerException e) {  // if the tree doesn't exist, give a warning that the file is empty
                System.err.println("Cannot decompress an empty file.");
            }

            bitInput.close(); // closes the reader that read compressedPathName
            output.close(); // closes the writer that wrote in decompressedPathName
        }

        catch (IOException e){
            System.err.println("IO exception.\n"); // return an IO exception if the file cannot be read
        }
    }

    /**
     * Creates the priority queue for the code by first creating a frequency tree from the text and parsing through that
     */
    private void createQueue() {
        try {
            input = new BufferedReader(new FileReader(pathName));  // utilizes BufferedReader to read from pathName
        }
        catch (FileNotFoundException e) {
            System.err.println("Make sure the file has a valid name.\n" + e.getMessage());  // returns an exception if it cannot open the file in pathName
            return;
        }

        try {
            int cInt = input.read(); // Read next character's integer representation
            while (cInt != -1) {  // while there are still characters to be read
                char c = (char) cInt;  // sets c to the current character

                if (frequencyMap.containsKey(c)) {  // if the map contains the current character
                    frequencyMap.put(c, frequencyMap.get(c) + 1);  // increment frequency
                }
                else {  // if not
                    frequencyMap.put(c, 1);  // initialize the character in the mapwith a frequency of 1
                }

                cInt = input.read(); // Read next character's integer representation
            }

            //System.out.println(frequencyTree);

            for (Character c : frequencyMap.keySet()) {  // for all characters in the frequency map
                PriorityTreeData treeData = new PriorityTreeData(c, frequencyMap.get(c));  // initializes a new class to hold characters and frequencies
                BinaryTree tree = new BinaryTree(treeData);  // create a new tree of PriorityTreeData elements
                priorityQueue.add(tree);  // add it to the priority queue
            }
            //System.out.println(priorityQueue);  // prints out the queue for debugging

            input.close();  // closes the reader
        }

        catch (IOException e) {
            System.err.println("IO exception.\n" + e.getMessage());  // prints an exception for an IO error
        }
    }

    /**
     * Creates the final frequency tree recursively by removing the two least frequent trees from the priority queue
     * @return returns the final frequency tree
     */
    private BinaryTree<PriorityTreeData> createTree(){
        if(priorityQueue.size()==1){  // if there is only one unique tree
            return priorityQueue.remove();  // remove from the queue and return the tree
        }
        BinaryTree<PriorityTreeData> T1 = priorityQueue.remove();  // removes a tree from the queue
        BinaryTree<PriorityTreeData> T2 = priorityQueue.remove();  // removes another tree from the queue

        PriorityTreeData r= new PriorityTreeData('r',T1.getData().getFrequency()+T2.getData().getFrequency());  // creates a new node with their combined frequencies
        BinaryTree<PriorityTreeData> T = new BinaryTree<>(r, T1, T2);  // creates a tree with that node
        priorityQueue.add(T);  // adds the tree to the priority queue

        return createTree();  // recurses until there is only one tree in the priorityQueue
    }

    /**
     * Creates the bit code words for each character, based on frequency. If there is only one character, default to "0".
     * @return returns a map of the characters in tree to their respective code word
     */
    private TreeMap<Character,String> findCodeWords() {
        TreeMap<Character, String> codeWordMap= new TreeMap<>();  // creates a new map for holding the code words for each character
        if(tree.size()==1) {  // if there is only one character
            codeWordMap.put(tree.getData().getCharacter(), "0");  // its code word will be "0"
        }
        if (tree!=null && tree.size() > 1) {  // otherwise
            findCodeWordsHelper(tree, "", codeWordMap);  // calls the helper method to find code words for all characters
        }

        return codeWordMap;
    }

    /**
     * Recursively creates the code words for each character with recursion and by keeping track of what has been visited already
     * @param codeTree the tree that we are recursing through to find the code words
     * @param path holds the path taken so far to reach a node in the tree
     * @param codeWordMap returns a map of the characters to their respective code word
     */
    private void findCodeWordsHelper(BinaryTree<PriorityTreeData> codeTree, String path, TreeMap<Character, String> codeWordMap) {
        String pathSoFar = path;  // establishes the path that the string is currently taking

        if(!codeTree.hasLeft() && !codeTree.hasRight()) {  // if the tree does not have children
            codeWordMap.put(codeTree.data.getCharacter(), path);  // put the code word in the tree
        }

        if (codeTree.hasLeft()) {  // if the tree has a child on the left
            path += "0";  // adds a "0" to the path
            findCodeWordsHelper(codeTree.getLeft(), path, codeWordMap);  // recursion
            path = pathSoFar;  // resets the path
        }

        if (codeTree.hasRight()) {  // if the tree has a child on the right
            path += "1";  // adds a "1" to the path
            findCodeWordsHelper(codeTree.getRight(), path, codeWordMap);  // recursion
        }
    }

    public static void main(String[] args) {
        // checks a random test
        HuffmanEncoding test= new HuffmanEncoding("inputs/test.txt", "test_compressed.txt", "test_decompressed.txt");
        test.compress();
        test.decompress();

        // checks a single character
        HuffmanEncoding test1= new HuffmanEncoding("inputs/test1.txt", "test1_compressed.txt", "test1_decompressed.txt");
        test1.compress();
        test1.decompress();

        // checks multiple of the same character
        HuffmanEncoding test2= new HuffmanEncoding("inputs/test2.txt", "test2_compressed.txt", "test2_decompressed.txt");
        test2.compress();
        test2.decompress();

        // checks an empty file
        HuffmanEncoding test3= new HuffmanEncoding("inputs/test3.txt", "test3_compressed.txt", "test3_decompressed.txt");
        test3.compress();
        test3.decompress();

        // compresses and decompresses the constitution
        HuffmanEncoding constitution= new HuffmanEncoding("inputs/USConstitution.txt", "USConstitution_compressed.txt", "USConstitution_decompressed.txt");
        constitution.compress();
        constitution.decompress();

        //compresses War and Peace
        HuffmanEncoding warAndPeace= new HuffmanEncoding("inputs/WarAndPeace.txt", "WarAndPeace_compressed.txt", "WarAndPeace_decompressed.txt");
        warAndPeace.compress();
        warAndPeace.decompress();
    }

}
