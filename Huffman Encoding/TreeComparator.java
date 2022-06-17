import java.util.Comparator;

public class TreeComparator implements Comparator<BinaryTree<PriorityTreeData>> {
    // compares the frequency of characters in two different trees
    public int compare(BinaryTree<PriorityTreeData> o1, BinaryTree<PriorityTreeData> o2) {
        if (o1.data.getFrequency() < o2.data.getFrequency()) {
            return -1;  // returns -1 if less frequent
        } else if (o1.data.getFrequency() == o2.data.getFrequency()) {
            return 0;  // returns 0 if equal frequencies
        } else {
            return 1;  // returns 1 if more frequent
        }
    }
}
