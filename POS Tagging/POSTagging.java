import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

public class POSTagging {
    public HashMap<String, HashMap<String, Double>> transitionScore = new HashMap<>();  // creates a new map to store the scores of transitions between parts of speech
    public HashMap<String, HashMap<String, Double>> observationScore = new HashMap<>();  // creates a new map from the parts of speech to the observed words and their scores
    public HashMap<String, Double> currentScore = new HashMap<>();  // creates a map to store the value of the current score
    public HashMap<String, Double> nextScores = new HashMap<>();  // creates a map to store the value of the score in the next state
    public final int unobservedPenalty = 100;  // sets the penalty for visiting an unobserved word
    public int error;  // the amount of deviations from correct tags
    public int correct;  // the amount of matches with the correct tags

    /**
     * Trains the algorithm for the given file of sentences and correct tags
     *
     * @param sentences  the filename for the file with sentences to train
     * @param correctTags the filename for the file with the correct tags for sentences
     */
    public void trainSentence(String sentences, String correctTags) {
        try {
            BufferedReader sentenceReader = new BufferedReader(new FileReader(sentences));  // initializes a new buffered reader to read from the file containing the sentences
            BufferedReader tagReader = new BufferedReader(new FileReader(correctTags));  // initializes a new buffered reader to read from the file of correct tags

            transitionScore.put("#", new HashMap<>());  // puts the starting state of "#" on the transition score map

            String sentence;  // the sentence to be read
            String tags;  // the corresponding tags
            while ((sentence = sentenceReader.readLine()) != null && (tags = tagReader.readLine()) != null) {  // while there are more lines to read from both sentences and tags
                String[] words = sentence.toLowerCase().split(" ");  // sets all words to lower case and splits by a space
                String[] tag = tags.split(" ");  // splits the tags by a space

                for (int i = 0; i < words.length; i++) {  // for every word
                    if (!observationScore.containsKey(tag[i])) {  // if the state has not been observed before
                        observationScore.put(tag[i], new HashMap<>());  // put the state in the map
                        observationScore.get(tag[i]).put(words[i], 0.0);  // set the number of times the word in that state has been observed to zero
                    }

                    if (!observationScore.get(tag[i]).containsKey(words[i])) {  // if the word hasn't been observed before in that state
                        observationScore.get(tag[i]).put(words[i], 0.0);  // put the word in with a frequency of zero
                    }

                    observationScore.get(tag[i]).put(words[i], (observationScore.get(tag[i]).get(words[i])) + 1);  // increments the observation score

                    if (i == words.length - 1) {  // if the end is reached
                        if (!transitionScore.get("#").containsKey(tag[0])) {  // if the starting state doesn't contain the first tag already
                            transitionScore.get("#").put(tag[0], 0.0);  // put it in and set the first tag's score to zero
                        }
                        transitionScore.get("#").put(tag[0], (transitionScore.get("#").get(tag[0])) + 1);  // increment the score for the start
                    }

                    else {  // while there are words left to read
                        if (!transitionScore.containsKey(tag[i])) {  // if the transition score doesn't contain the current state
                            transitionScore.put(tag[i], new HashMap<>());  // create a new map for the state
                            transitionScore.get(tag[i]).put(tag[i + 1], 0.0);  // initializes the score of the next state to zero
                        }
                        if (!transitionScore.get(tag[i]).containsKey(tag[i + 1])) {  // if the transition score doesn't contain the next state
                            transitionScore.get(tag[i]).put(tag[i + 1], 0.0);  // initializes the score of the next state to zero
                        }
                        transitionScore.get(tag[i]).put(tag[i + 1], (transitionScore.get(tag[i]).get(tag[i + 1]) + 1));  // increment the score of the next state
                    }
                }
            }

            calculateProbabilitiesHelper(); // call the helper method to calculate probabilities and update transition scores and observation scores

            sentenceReader.close();  // close the reader
            tagReader.close();  // close the reader
        }
        catch (Exception e) {  // handles exceptions
            System.err.println("Error reading file!");
        }
    }

    /**
     * Trains the algorithm for the given file of sentences and correct tags
     *
     * @param sentence  the array of strings to be tagged
     * @return a list of tags determined by our viterbi algorithm
     */
    public List<String> ViterbiTagging(String[] sentence) {
        List<String> path = new ArrayList<>();  // creates a list to hold the tags in the correct order
        List<HashMap<String, String>> backtrace = new ArrayList<>();  // creates a list to hold the different states that could be traced back
        currentScore.put("#", 0.0);  // sets the score to 0 at the start
        String highest = null;  // initializes the state with the highest score to null
        int observation = 0;  // initializes the number of words observed

        while (observation < sentence.length) {  // while the sentence has not yet been fully read
            nextScores = new HashMap<>();  // create a new map to hold the next state of the current word
            backtrace.add(new HashMap<>());  // adds to the backtrace of the current word
            Double score;  // initializes the score

            for (String current : currentScore.keySet()) {  // for the current observed state
                if (!current.equals(".")) {
                    for (String nextState : transitionScore.get(current).keySet()) {  // look at the next possible states for the current state
                        if (observationScore.get(nextState).containsKey(sentence[observation])) {  // if the word is observed in the current state
                            score = currentScore.get(current) + transitionScore.get(current).get(nextState) +  // set the score to the current score
                                    observationScore.get(nextState).get(sentence[observation]);  // plus the score from transitioning to a different state along with the score of the word being observed
                        } else {  // if the word has not been observed in this state
                            score = currentScore.get(current) + transitionScore.get(current).get(nextState) - unobservedPenalty;  // don't add the observation score, and subtract the constant 10
                        }
                        if ((!nextScores.containsKey(nextState)) || nextScores.get(nextState) < score) {  // if the next set of scores doesn't contain the next state or if the score is bigger than previously observed
                            nextScores.put(nextState, score);  // add it to the map of next scores
                            backtrace.get(observation).put(nextState, current);  // update the backtrace for this state
                        }
                        highest = nextState;  // set the highest to the next state
                    }
                }
            }

            currentScore = nextScores;  // update the current score
            observation += 1;  // increment the number of words observed
        }

        if (highest != null) {  // if there are words to observe
            for (String possibleHighest : backtrace.get(observation - 1).keySet()) {  // for every state for the last word
                if (nextScores.get(possibleHighest) > nextScores.get(highest)) {  // find the highest score
                    highest = possibleHighest; // store it in highest
                }
            }
            observation = sentence.length - 1;  // start from the last word
            path.add(highest);  // add the highest to the path

            while (observation > 0) {  // while there are words left to read
                path.add(0, backtrace.get(observation).get(highest));  // follow the backtrace and add it to the path
                highest = backtrace.get(observation).get(highest);  // set highest to be the next word from the right
                observation--;  // increments the number of words left to read
            }
        }
        return path;  // returns the path
    }

    /**
     * Helper method to update the frequencies to probabilities in observationScore and transitionScore
     */
    public void calculateProbabilitiesHelper() {
        for (String state : observationScore.keySet()) {  // for every state that is observed
            double total = 0.0;  // initializes the total to zero
            for (String word : observationScore.get(state).keySet()) {  // for every word observed in that state
                total += observationScore.get(state).get(word);  // add to the total for that state
            }

            for (String word : observationScore.get(state).keySet()) {  // for every word observed in that state
                double probability = Math.log(observationScore.get(state).get(word) / total);  // sets the log score for each word
                observationScore.get(state).put(word, probability);  // updates the score to be a probability
            }
        }

        for (String state : transitionScore.keySet()) {  // for every state seen in transitions
            double total = 0.0;  // initializes the total to zero
            for (String nextState : transitionScore.get(state).keySet()) {  // for every next state in the state currently observed
                total += transitionScore.get(state).get(nextState);  // add the frequency of the next state to the total
            }

            for (String nextState : transitionScore.get(state).keySet()) {  // for every next state in all states currently observed
                double probability = Math.log(transitionScore.get(state).get(nextState) / total);  // converts the frequencies to probabilities
                transitionScore.get(state).put(nextState, probability);  // turns the frequencies into probabilities
            }
        }
    }

    /**
     * Runs a test for a given set of sentences to train and correct tags for those sentences. Prints the error for these files with our Viterbi algorithm
     *
     * @param sentences  the filename for the file with sentences to train
     * @param correctTags the filename for the file with the correct tags for sentences
     */
    public void runTest(String sentences, String correctTags) {
        try {
            BufferedReader sentenceReader = new BufferedReader(new FileReader(sentences)); // initializes a new buffered reader to read from the file containing the sentences
            BufferedReader tagReader = new BufferedReader(new FileReader(correctTags)); // initializes a new buffered reader to read from the file containing the correct tags
            String sentence;  // the sentence to be read
            String tags;  // the corresponding tags
            error = 0;  // sets the error to initially 0
            correct = 0;  // sets the correct to initially 0
            while ((sentence = sentenceReader.readLine()) != null && (tags = tagReader.readLine()) != null) {
                String[] words = sentence.toLowerCase().split(" ");  // determine the words to analyze from the file
                String[] tag = tags.split(" ");  // determine the correct tags from the file
                List<String> tagList = ViterbiTagging(words); // analyze the words from the file and return a list of tags
                compareDataSets(tagList, tag); // compare the analyzed list of tags with the correct tags
            }

            System.out.println("error: " + error);  // print the errors
            System.out.println("correct: " + correct);  // print the correct ones

            // close both readers
            sentenceReader.close();
            tagReader.close();

        } catch (Exception e) {  // throw an exception if there are errors when reading the file
            System.err.println("Error reading file!");
        }

    }
    /**
     * Brought from the given POSLib created by author Chris Bailey-Kellogg, Dartmouth CS 10, Winter 2021
     * Loads the tagged sentences from filename into sentences and tags,
     * in parallel order (i.e., sentences.get(i) is tagged with tags.get(i)).
     * Each line is of the form word1/tag1 word2/tag2 ...
     * except that lines that are empty or start with "!" (a comment line) are ignored.
     * Not particularly robust; ignores tokens that don't take the form word/tag.
     *
     * @param filename input filename, in specified format
     * @param sentences a list into which the sentences are added
     * @param tags a list into which the tags are added
     * @throws Exception
     */
    public static void loadTaggedSentences(String filename, List<String[]> sentences, List<String[]> tags) throws Exception {
        BufferedReader in = new BufferedReader(new FileReader(filename));
        String line;
        int lineNum = 0;
        while ((line = in.readLine()) != null) {
            if (line.length() == 0 || line.charAt(0) == '!') continue;
            String[] taggedWords = line.split(" ");
            if (taggedWords.length == 0) continue;
            String[] sentence = new String[taggedWords.length];
            String[] tag = new String[taggedWords.length];
            sentences.add(sentence);
            tags.add(tag);
            int i = 0;
            for (String taggedWord : taggedWords) {
                String[] wordAndTag = taggedWord.split("/");
                if (wordAndTag.length != 2) {
                    System.err.println("bad token " + i + " on line " + lineNum + ":" + line);
                } else {
                    sentence[i] = wordAndTag[0].toLowerCase();
                    tag[i] = wordAndTag[1];
                    i++;
                }
            }
            lineNum++;
        }
        in.close();
    }


    public void compareDataSets(List<String> tags, String[] correctTags) {
        for(int i=0; i< tags.size(); i++) {  // for every tag in tags
            if(!tags.get(i).equals(correctTags[i])) {  // if the tag is not the correct tag
                error+=1;  // increments the error
            } else{  // if the tags are correct
                correct+=1;  // increments the number of correct tags
            }
        }
    }


    public static void main(String[] args) throws Exception {
        // hard coded tests for the ice cream test
        System.out.println("Test 1");
        POSTagging test = new POSTagging();
        test.transitionScore.put("#", new HashMap<>());
        test.transitionScore.get("#").put("Cold", 5.0);
        test.transitionScore.get("#").put("Hot", 5.0);
        test.transitionScore.put("Cold", new HashMap<>());
        test.transitionScore.get("Cold").put("Cold", 7.0);
        test.transitionScore.get("Cold").put("Hot", 3.0);
        test.transitionScore.put("Hot", new HashMap<>());
        test.transitionScore.get("Hot").put("Hot", 7.0);
        test.transitionScore.get("Hot").put("Cold", 3.0);
        test.observationScore.put("Cold", new HashMap<>());
        test.observationScore.get("Cold").put("one", 7.0);
        test.observationScore.get("Cold").put("two", 2.0);
        test.observationScore.get("Cold").put("three", 1.0);
        test.observationScore.put("Hot", new HashMap<>());
        test.observationScore.get("Hot").put("one", 2.0);
        test.observationScore.get("Hot").put("two", 3.0);
        test.observationScore.get("Hot").put("three", 5.0);
        String[] sentence = {"two", "three", "two", "one"};

        List<String> iceCreamTags = test.ViterbiTagging(sentence);
        String[] correctTags = {"Hot", "Hot", "Hot", "Cold"};
        System.out.println("Calculated tags: " + iceCreamTags);
        System.out.println("Correct tags: Hot, Hot, Hot, Cold");
        test.correct = 0;
        test.error = 0;
        test.compareDataSets(iceCreamTags, correctTags);
        System.out.println("error: " + test.error);
        System.out.println("correct: " + test.correct);
        System.out.println();

        // tests from the files simple test, simple train, and brown train
        System.out.println("Test 2");
        POSTagging test2 = new POSTagging();
        System.out.println("Simple train");
        test2.trainSentence("texts/simple-train-sentences.txt", "texts/simple-train-tags.txt");
        test2.runTest("texts/simple-train-sentences.txt", "texts/simple-train-tags.txt");

        POSTagging test5 = new POSTagging();
        System.out.println("Simple test");
        test5.trainSentence("texts/simple-test-sentences.txt", "texts/simple-test-tags.txt");
        test5.runTest("texts/simple-test-sentences.txt", "texts/simple-test-tags.txt");

        POSTagging test6 = new POSTagging();
        System.out.println("Brown train");
        test6.trainSentence("texts/brown-train-sentences.txt", "texts/brown-train-tags.txt");
        test6.runTest("texts/brown-train-sentences.txt", "texts/brown-train-tags.txt");

        POSTagging test7 = new POSTagging();
        System.out.println("Brown test");
        test7.trainSentence("texts/brown-test-sentences.txt", "texts/brown-test-tags.txt");
        test7.runTest("texts/brown-test-sentences.txt", "texts/brown-test-tags.txt");
        System.out.println();

        // tests corpus
        System.out.println("Test 3");
        POSTagging test3 = new POSTagging();
        test3.trainSentence("texts/example-sentences.txt", "texts/example-tags.txt");
        test3.runTest("texts/example-sentences.txt", "texts/example-tags.txt");
        ArrayList<String[]> sentences = new ArrayList<>();
        ArrayList<String[]> tags = new ArrayList<>();
        loadTaggedSentences("inputs/cs10corpus.txt", sentences, tags);
        for (int i = 0; i < sentences.size(); i++) {
            List<String> corpus = test3.ViterbiTagging(sentences.get(i));
            test3.correct = 0;
            test3.error = 0;
            test3.compareDataSets(corpus, tags.get(i));
            System.out.println("Error: " + test.error);
            System.out.println("Correct: " + test.correct);
            System.out.println();
        }
        System.out.println();

        // console driven tests initialized from brown train
        System.out.println("Test 4");
        POSTagging test4 = new POSTagging();
        test4.trainSentence("texts/brown-train-sentences.txt", "texts/brown-train-tags.txt");
        Scanner commandScanner = new Scanner(System.in); // initializes a scanner to read in lines
        String command = "";  // the initial command is empty
        while (!command.toLowerCase().equals("q")) { // quits when the command equals "q"
            System.out.println("Please enter a sentence: ");  // ask for a center
            command = commandScanner.nextLine().toLowerCase();// asks for a user command
            if (!command.equals("q")) {
                String[] inputSentence = command.toLowerCase().split(" ");
                if (command.length() != 0) {
                    System.out.println(test4.ViterbiTagging(inputSentence));
                } else {
                    System.err.println("Input a valid sentence\n");  // prints if an invalid actor is input
                }
            }
        }
        System.out.println();

    }

}