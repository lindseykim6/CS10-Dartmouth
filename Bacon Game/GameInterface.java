import java.io.*;
import java.util.*;
/**
 * @author Lindsey Kim
 * @author Kenneth Wu
 *
 * Loads from files and creates the game interface to build a Bacon's game where the commands are
 * u: changes the center of the network
 * p: finds the shortest path from a given vertex to the center
 * i: finds the disconnected actors from the center
 * n: finds the number of actors with a path to the given center
 * a: finds the average path length to the current center
 * m: finds the best center based on average path length
 * n: finds the best center based on number of costars or degree
 * q: quits the game
 */

public class GameInterface {
    public Map<String, String> actorIDMap = new TreeMap<>();  // maps the actor IDS to their respective actors
    public Map<String, String> movieIDMap = new TreeMap<>();  // maps the movie IDS to their respective movies
    public Map<String, List<String>> movieActorMap = new TreeMap<>();  // maps the actors to their movies
    public BufferedReader input1;  // initializes the actors file reader
    public BufferedReader input2;  // initializes the movies file reader
    public BufferedReader input3;  // initializes the movies to actors file reader
    public Graph<String, List<String>> actorMovieGraph = new AdjacencyMapGraph<>();

    /**
     * Loads the files into maps for use when building the graph
     * @param actorsToIDs the name of the actorIDs to actor file
     * @param moviesToIDs the name of the movieIDs to movie file
     * @param movieToActors the name of the movieID to actorID file
     */
    public void loadFiles(String actorsToIDs, String moviesToIDs, String movieToActors) {
        try {
            input1 = new BufferedReader(new FileReader(actorsToIDs));  // utilizes BufferedReader to read from the actors file
            input2 = new BufferedReader(new FileReader(moviesToIDs));  // utilizes BufferedReader to read from the movies file
            input3 = new BufferedReader(new FileReader(movieToActors));  // utilizes BufferedReader to read from the moviesToActors file
        } catch (FileNotFoundException e) {
            System.err.println("Make sure the files exist. \n" + e.getMessage());  // prints an exception if the file does
        }


        try {
            String line;
            while ((line = input1.readLine()) != null) {  // while there are lines to read in the actor text
                String actorID = line.split("\\|")[0];  // grabs the part of the line before the | and stores it as actorID
                String actor = line.split("\\|")[1];  // grabs the part of the line after the | and stores it as actor
                actorIDMap.put(actorID, actor);
            }

            input1.close(); // closes the actor file reader
        } catch (IOException e) {
            System.err.println("IO exception.\n" + e.getMessage());  // prints an IO exception if it can't be read
        }

        try {
            String line;
            while ((line = input2.readLine()) != null) {  // while there are lines to read in the movie text
                String movieID = line.split("\\|")[0];  // grabs the part of the line before the | and stores it as movieID
                String movie = line.split("\\|")[1];  // grabs the part of the line after the | and stores it as movie
                movieIDMap.put(movieID, movie);
            }

            input2.close();  // closes the movie file reader
        } catch (IOException e) {
            System.err.println("IO exception.\n" + e.getMessage());  // prints an IO exception if it can't be read
        }

        try {
            String line;
            while ((line = input3.readLine()) != null) {  // while there are lines to read in the movie to actors text
                String movieID = line.split("\\|")[0];  // grabs the part of the line before the | and stores it as movieID
                String actorID = line.split("\\|")[1];  // grabs the part of the line after the | and stores it as actorID
                if (!movieActorMap.containsKey(movieIDMap.get(movieID))) {  // if the movie given by the ID doesn't already exist in the map
                    movieActorMap.put(movieIDMap.get(movieID), new ArrayList<>()); // add it and a list
                }
                movieActorMap.get(movieIDMap.get(movieID)).add(actorIDMap.get(actorID)); // add the actor to their movie's list of actors
            }

            input3.close(); // closes the movie to actor file reader
        } catch (IOException e) {
            System.err.println("IO exception.\n" + e.getMessage());  // prints an IO exception if it can't be read
        }
    }

    /**
     * Builds a graph that holds all the actors and their connections using loaded maps of actorIDS to actors, movieIDs to movies
     * and movies to a list of actors
     * @return returns a graph that holds all the actors and their connections
     */
    public Graph<String, List<String>> buildGraph() {
        for (String actor : actorIDMap.values()) { // for every actor to exist in the file
            actorMovieGraph.insertVertex(actor);  // add it as a vertex to the map
        }

        for (String movie : movieActorMap.keySet()) {  // for every movie
            List<String> actors = movieActorMap.get(movie);  // get a list of all the actors in it
            for (int i = 0; i < actors.size(); i++) {
                for (int j = i + 1; j < actors.size(); j++) {
                    if (!actorMovieGraph.hasEdge(actors.get(i), actors.get(j))) {
                        actorMovieGraph.insertUndirected(actors.get(i), actors.get(j), new ArrayList<>());  // make an undirected edge for every pair if it doesn't exist
                    }
                    actorMovieGraph.getLabel(actors.get(i), actors.get(j)).add(movie);  // add each common movie to the edge between the pair
                }
            }
        }
        return actorMovieGraph;
    }

    /**
     * Creates the game interface for Bacon's game where the commands are
     * u: changes the center of the network
     * p: finds the shortest path from a given vertex to the center
     * i: finds the disconnected actors from the center
     * n: finds the number of actors with a path to the given center
     * a: finds the average path length to the current center
     * m: finds the best center based on average path length
     * d: finds the best center based on number of costars or degree
     * q: quits the game
     */

    public static void main(String[] args) {
        // creates a new game
        GameInterface game = new GameInterface();

        // opens and loads the files to read from
        game.loadFiles("inputs/actors.txt", "inputs/movies.txt", "inputs/movie-actors.txt");
        Graph<String, List<String>> graph = game.buildGraph(); // builds the graph from the text

        String center = "Kevin Bacon";  // sets the default center to Kevin Bacon
        Graph<String, List<String>> shortestPathTree = Bacon.bfs(graph, center);  // sets the shortestPathTree to be from Kevin Bacon by default

        Scanner commandScanner = new Scanner(System.in); // initializes a scanner to read in lines
        String command = "";  // the initial command is empty


        class degreeCompare implements Comparator<String> {  // compares to vertices by their degree
            public int compare(String s1, String s2) {
                int degree = graph.inDegree(s1) - graph.inDegree(s2);
                // sorts from largest degree to smallest degree
                if (degree < 0) {
                    return 1;
                } else if (degree == 0) {
                    return 0;
                } else {
                    return -1;
                }
            }
        }

        Comparator<String> degreeComparator = new degreeCompare();
        PriorityQueue<String> degreeQueue;  // sorts the queue by the vertices' degree

        // prints the menu
        String border="********************************************************************";
        System.out.println();
        System.out.println(border);
        System.out.println( "*                        Program Menu                              *");
        System.out.println(border);
        System.out.println("u: changes the center of the network");
        System.out.println("p: finds the shortest path from a given vertex to the center");
        System.out.println("i: finds the disconnected actors from the center");
        System.out.println("n: finds the number of actors with a path to the given center");
        System.out.println("a: finds the average path length to the current center");
        System.out.println("m: finds the best center based on average path length");
        System.out.println("d: finds the best center based on number of costars or degree");
        System.out.println("q: quits the game");
        System.out.println(border);

        while (!command.toLowerCase().equals("q")) { // quits when the command equals "q"
            System.out.println("Enter a command ");
            command = commandScanner.nextLine().toLowerCase();// asks for a user command

            if (command.equals("u")) {  // if the command is u
                System.out.println("\nPlease enter a center ");  // ask for a center
                center = commandScanner.nextLine(); // stores the user input as the new center
                if(graph.hasVertex(center)) {
                    shortestPathTree = Bacon.bfs(graph, center);  // find the shortest path tree of the new center
                    Double averageSeparation = Bacon.averageSeparation(shortestPathTree, center);  // find the average separation from the new center

                    System.out.println(center + " is now the center of the acting universe based on average separation, " +
                            "with " + graph.inDegree(center) + " costars and connected to " + (shortestPathTree.numVertices() - 1)
                            + " actors with average separation " + averageSeparation + "\n");
                } else {
                    System.out.println("Actor not found\n");  // prints if an invalid actor is input
                }
            }

            else if (command.equals("p")) {  // if the command is p
                System.out.println("\nPlease enter the starting name "); // asks for a path to search from to the center
                String startingName = commandScanner.nextLine(); // scans the starting name and stores it as the starting name

                if(shortestPathTree.hasVertex(startingName)) { //checks if the actor is connected to the source
                    List<String> path = Bacon.getPath(shortestPathTree, startingName);  // finds the shortest path for the center from the starting vertex
                    System.out.println("\n" + center + " game >");
                    System.out.println(startingName + "'s number is " + (path.size() - 1));  // prints how far away the starting vertex is from the center

                    for (int i = 0; i < path.size() - 1; i++) {
                        // iterates through the path and prints out the common movies
                        System.out.println(path.get(i) + " appeared in " + graph.getLabel(path.get(i), path.get(i + 1)) + " with " + path.get(i + 1));
                    }
                    System.out.print("\n");
                }
                else {
                    System.out.println("Actor not connected to source\n"); // prints if invalid actor is requested
                }
            }

            else if (command.equals("i")) {  // if command is i
                // print all the disconnected actors
                System.out.println("For center " + center + ", the disconnected actors are " + Bacon.missingVertices(graph, shortestPathTree) + "\n");
            }

            else if (command.equals("n")) { // if command is n
                // prints all the n
                System.out.println(center + " is connected with " + (shortestPathTree.numVertices() - 1) + " actors.\n");
            }

            else if (command.equals("a")) {  // if command is a
                // print the average path length to the center
                System.out.println("The average path length for the center is " + Bacon.averageSeparation(shortestPathTree, center) + "\n");
            }

            else if (command.equals("m")) {  // if command is a m
                String bestCenter = center;  // initially sets the bestCenter to the current center
                shortestPathTree = Bacon.bfs(graph, center);  // finds the shortest path tree to the center
                double lowest = Bacon.averageSeparation(shortestPathTree, center);  // sets the lowest to be the average separation

                for (String actor : Bacon.bfs(graph, "Kevin Bacon").vertices()) {  // checks all the actors connected to Kevin Bacon
                    shortestPathTree = Bacon.bfs(graph, actor);  // sets the shortest path tree to one actor as center
                    Double averageSeparation = Bacon.averageSeparation(shortestPathTree, actor);  // finds the average separation for that actor
                    if (averageSeparation < lowest) {  // if their average separation is lower than Kevin's
                        lowest = averageSeparation;  // set the lowest to be this average separation
                        bestCenter = actor; // sets the best center as this actor
                    }
                }

                center = bestCenter;  // sets the center to the best center
                shortestPathTree = Bacon.bfs(graph, center);  // finds the shortest path tree to the center

                System.out.println(center + " is now the center of the acting universe based on average separation, " +
                        "with " + graph.inDegree(center) + " costars and connected to " + (shortestPathTree.numVertices() - 1)
                        + " actors with average separation " + lowest + "\n");

            }

            else if (command.equals("d")) {  // if the command is a d
                degreeQueue = new PriorityQueue<String>(degreeComparator); // creates a new priority queue for the degree of actors

                for (String actor : Bacon.bfs(graph, "Kevin Bacon").vertices()) {
                    degreeQueue.add(actor);  // adds each actor in Kevin Bacon's world to the degree queue
                }

                center = degreeQueue.peek();  // sets the center to first one in the priority queue (the one with the highest degree)
                shortestPathTree = Bacon.bfs(graph, center);  // sets the shortest path tree to be for this center
                Double averageSeparation = Bacon.averageSeparation(shortestPathTree, center);  // finds the average separation for this center

                System.out.println(center + " is now the center of the acting universe based on average separation, " +
                        "with " + graph.inDegree(center) + " costars and connected to " + (shortestPathTree.numVertices() - 1)
                        + " actors with average separation " + averageSeparation + "\n");

            }

            else if (command.equals("q")) {  // if command is q
                //quit the game
                System.out.println("Goodbye!");
                break;
            }

            else {
                // catches invalid commands
                System.out.println("Invalid command\n");
            }
        }
        commandScanner.close(); // closes the command scanner
    }

}
