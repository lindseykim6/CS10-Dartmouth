import java.util.*;
/**
 *@author Lindsey Kim
 *@author Kenneth Wu
 *
 * Creates a library of methods to create Bacon's game with a graph
 */


public class Bacon<V, E> {

    /**
     * calls breadth first search to find the shortest paths to a vertex
     * @param g the graph to search from
     * @param source the center of the graph to build a shortestPathTree to
     * @return a shortest tree graph holding the shortest paths to the source
     */

    public static <V, E> Graph<V, E> bfs(Graph<V, E> g, V source) {
        Graph<V, E> shortestPathTree = new AdjacencyMapGraph<>();
        Set<V> visited = new HashSet<V>(); //Set to track which vertices have already been visited
        Queue<V> queue = new LinkedList<V>(); //queue to implement BFS
        queue.add(source); //enqueue start vertex
        visited.add(source); //add start to visited Set
        while (!queue.isEmpty()) { //loop until no more vertices
            V u = queue.remove(); //dequeue
            for (V v : g.outNeighbors(u)) { //loop over out neighbors
                if (!visited.contains(v)) { //if neighbor not visited, then neighbor is discovered from this vertex
                    visited.add(v); //add neighbor to visited Set
                    queue.add(v); //enqueue neighbor
                    if (!shortestPathTree.hasVertex(u)) shortestPathTree.insertVertex(u);  // adds the starting actor to the tree if it hasn't been added already
                    if (!shortestPathTree.hasVertex(v)) shortestPathTree.insertVertex(v);  // adds the target actor to the tree if it hasn't been added already
                    shortestPathTree.insertDirected(v, u, g.getLabel(u, v));  // obtains the label of the edge between these two vertices
                }
            }
        }
        return shortestPathTree;  // returns the tree
    }

    /**
     * obtains the shortest path of actors to get to the final actor
     * @param tree the shortest tree graph to look for the shortest path in
     * @param v where we are starting the path from to get to the center
     * @return a list of the path from the given actor to the center
     */
    public static <V, E> List<V> getPath(Graph<V, E> tree, V v) {
        List<V> path = new ArrayList<>();  // creates a list to hold the path
        try {
            path.add(v);  // adds the current actor to the path
            while (tree.outDegree(v) != 0) {  // while the current actor has an out neighbor they're connected to
                for (V u : tree.outNeighbors(v)) {  // for all the actors they're connected to
                    path.add(u); // add them to the path
                    v = u;  // update the current actor
                }
            }
        }
        // catches errors for invalid vertices
        catch (NullPointerException e) {
            System.err.println("Make sure the vertex is valid");
            return null;
        }
        return path;  // returns the path of actors to get to the final actor

    }

    /**
     * obtains the missing vertices that aren't connected to the center
     * @param graph the graph that holds all the actors
     * @param subgraph the graph that holds the center and the path's connected to the center
     * @return a set of all disconnected actors
     */
    public static <V, E> Set<V> missingVertices(Graph<V, E> graph, Graph<V, E> subgraph) {
        Set<V> missingVertices = new HashSet<V>();  // creates a new set for the missing vertices
        for (V v: graph.vertices()) {  // for all the actors
            if (!subgraph.hasVertex(v)) {  // if they are not connected to the current actor
                missingVertices.add(v);  // add them to the list of actors who aren't connected to the target actor
            }
        }
        return missingVertices;  // returns the list of actors that aren't connected to the target actor
    }

    /**
     * returns the average separation between an actor and every other actor connected to them
     * @param tree the shortest path tree that holds the center and all of the shortest paths to other actors
     * @param root the center of the universe
     * @return a double telling the average separation between the center of the universe and other actors
     */
    public static <V, E> double averageSeparation(Graph<V, E> tree, V root) {
        double sum=0;  // initializes the sum
        for(V v: tree.vertices()) {  // for every actor
            sum += averageSeparationHelper(tree, v, root, 0);  // call the helper method and add to the sum
        }
        return sum/(tree.numVertices()-1);  // return the sum divided by the number of vertices in the shortestPathTree subtracted by one to obtain the average
    }

    /**
     * helper method for finding average separation
     * @param tree the shortest path tree that holds the center and all of the shortest paths to other actors
     * @param root the center of the universe
     * @return a double telling the average separation between the center of the universe and other actors
     */
    private static <V, E> double averageSeparationHelper(Graph<V, E> tree, V v, V root, double sum) {
        double newSum = 0;  // initializes the sum
        if (v==root) {  // if current actor is the target actor
            return sum;  // return the sum of the separation
        }
        for(V u: tree.outNeighbors(v)) {  // for all of the actors
            newSum += averageSeparationHelper(tree, u, root, sum  + 1);  // increment the sum by recursion and visit it's neighbors
        }
        return newSum;  // return the new sum
    }

    /**
     * tests the bacon library
     */
    public static void main(String[] args) {
        Graph<String, String> graph= new AdjacencyMapGraph<>();
        graph.insertVertex("Kevin Bacon");
        graph.insertVertex("Bob");
        graph.insertVertex("Alice");
        graph.insertVertex("Charlie");
        graph.insertVertex("Dartmouth");
        graph.insertVertex("Nobody");
        graph.insertVertex("Nobody's Friend");
        graph.insertUndirected("Kevin Bacon", "Bob", "A Movie");
        graph.insertUndirected("Kevin Bacon", "Alice", "A Movie, E Movie");
        graph.insertUndirected("Alice", "Bob", "A Movie");
        graph.insertUndirected("Alice", "Charlie", "D Movie");
        graph.insertUndirected("Charlie", "Bob", "C Movie");
        graph.insertUndirected("Charlie", "Dartmouth", "B Movie");
        graph.insertUndirected("Nobody", "Nobody's Friend", "F Movie");
        System.out.println("Graph");
        System.out.println(graph + "\n");

        Graph<String, String> myGraph= bfs(graph, "Kevin Bacon");
        System.out.println("Shortest path tree");
        System.out.println(myGraph + "\n");

        System.out.println("Path");
        System.out.println(getPath(myGraph, "Dartmouth") + "\n");

        System.out.println("Msiing vertices");
        System.out.println(missingVertices(graph, myGraph) +"\n");

        System.out.println("Average Separation: Charlie");
        Graph<String, String> myGraph2= bfs(graph, "Charlie");
        System.out.println(averageSeparation(myGraph2, "Charlie"));

        System.out.println("Average Separation: Nobody");
        Graph<String, String> myGraph3= bfs(graph, "Nobody");
        System.out.println(averageSeparation(myGraph3, "Nobody"));

        System.out.println("Average Separation: Dartmouth");
        Graph<String, String> myGraph4= bfs(graph, "Dartmouth");
        System.out.println(averageSeparation(myGraph4, "Dartmouth"));
    }
}