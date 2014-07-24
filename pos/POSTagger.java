import java.io.*;
import java.util.*;

/**
 * Class to label each word in a sentence with its part of speech
 * using hidden Markov model and data from the Brown corpus
 * Dartmouth CS 10, Winter 2014, PS 6
 * @author Matt Krantz
 *
 */
public class POSTagger {
	ArrayList<String[]> corpusWords;                            // Store array of words from corpus
	ArrayList<String[]> corpusTags;                             // Store array of tags from corpus
    
	HashMap<String, HashMap<String, Double>> emissions;         // Emission probabilities
	HashMap<String, HashMap<String, Double>> transitions;       // Transition probabilities
	
	static final Double UNKNOWN_VALUE = -200.0;                 // Constant for unobserved word
    
	public POSTagger() {
		// Initialize instance variables
		corpusWords = new ArrayList<String[]>();
		corpusTags = new ArrayList<String[]>();
		
		emissions = new HashMap<String, HashMap<String, Double>>();
		transitions = new HashMap<String, HashMap<String, Double>>();
	}
	
	/**
	 * Read and parse input from the given files
	 */
	public void load() throws IOException {
		// BufferedReaders to read in from the word and tag files
		BufferedReader wordsIn = new BufferedReader(new FileReader("inputs/brown-words."
		+ "txt"));
		BufferedReader tagsIn = new BufferedReader(new FileReader("inputs/brown-tags.txt"));
		
		// Parse the word file
		try {
			String wordLine;    // Line we're reading in
			while ((wordLine = wordsIn.readLine()) != null) {
				// Convert all characters to lower case
				String lowerCaseLine = wordLine.toLowerCase();
				// Split the line based on spaces
				String[] splitWordLine = lowerCaseLine.split(" ");
				// Add split line to proper index in ArrayList
				corpusWords.add(splitWordLine);
			}
		}
		finally {
			// Close the input file
			wordsIn.close();
		}
		// Parse the tag file
		try {
			String tagLine;    // Line we're reading in
			while ((tagLine = tagsIn.readLine()) != null) {
				// Split the line based on spaces
				String[] splitTagLine = tagLine.split(" ");
				// Add split line to proper index in ArrayList
				corpusTags.add(splitTagLine);
			}
		}
		finally {
			// Close the input file
			tagsIn.close();
		}
	}
	
	/**
	 * Train the model (emission and transition probabilities) on
	 * corresponding lines from the input files
	 * @param wordList what words to train on
	 * @param tagList what tags to train on
	 * @param n how much of the file to read in
	 */
	public void train(ArrayList<String[]> wordList, ArrayList<String[]> tagList, int n) {
		// Reset transitions and emissions maps
		emissions = new HashMap<String, HashMap<String, Double>>();
		transitions = new HashMap<String, HashMap<String, Double>>();
		
		// Loop through words and tags in ArrayLists
		for (int i = 0; i < Math.min(n, wordList.size()); i++) {
			for (int j = 0; j < wordList.get(i).length; j++) {
				// Word we're looking at
				String word = wordList.get(i)[j];
				// Corresponding part of speech
				String POS = tagList.get(i)[j];
				
				// If the word is not already in emissions, put it there
				if (!emissions.containsKey(word)) {
					emissions.put(word, new HashMap<String, Double>());
				}
				// Add the corresponding part of speech as key of inner map
				if (!emissions.get(word).containsKey(POS)) {
					// Occurred once, so frequency is one
					emissions.get(word).put(POS, 1.0);
				}
				else {
					// Increment frequency by one, assign to that POS
					Double newFrequency = emissions.get(word).get(POS) + 1;
					emissions.get(word).put(POS, newFrequency);
				}
				
				// If POS not already in transitions, put it there
				if (!transitions.containsKey(POS)) {
					transitions.put(POS, new HashMap<String, Double>());
				}
				// If j is 0, we're at the beginning of a sentence
				if (j == 0) {
					if (!transitions.containsKey("#")) {
						transitions.put("#", new HashMap<String, Double>());
						transitions.get("#").put(POS, 1.0);
					}
					else if (!transitions.get("#").containsKey(POS)) {
						transitions.get("#").put(POS, 1.0);
					}
					else {
						Double newFrequency = transitions.get("#").get(POS) + 1;
						transitions.get("#").put(POS, newFrequency);
					}
				}
				// Add POS tag following that POS
				if (j < corpusTags.get(i).length - 1) {
					// Tag after current tag
					String nextPOS = corpusTags.get(i)[j + 1];
					if (!transitions.get(POS).containsKey(nextPOS)) {
						// Occurred once, so frequency is one
						transitions.get(POS).put(nextPOS, 1.0);
					}
					else {
						// Increment frequency by one, assign to transition
						Double newFrequency = transitions.get(POS).get(nextPOS) + 1;
						transitions.get(POS).put(nextPOS, newFrequency);
					}
				}
			}
		}
		
		// Map to store frequencies of POS tags
		HashMap<String, Double> frequencyMap = new HashMap<String, Double>();
		for (String word: emissions.keySet()) {
			for (String POS: emissions.get(word).keySet()) {
				// Look at how many times any word appears as tag
				Double overallTagFrequency = 0.0;
				if (emissions.get(word).containsKey(POS)) {
					// Keep track of emission frequency
					overallTagFrequency += emissions.get(word).get(POS);
				}
				// If frequency map doesn't have POS, add it
				if (!frequencyMap.containsKey(POS)) {
					frequencyMap.put(POS, overallTagFrequency);
				}
				else {
					// If POS already in frequency map, add new value to
					// existing value
					Double newFrequency = frequencyMap.get(POS) + 1;
					frequencyMap.put(POS, newFrequency);
				}
			}
		}
		
		// Go over all states, normalize emission probabilities
		for (String word: emissions.keySet()) {
			for (String POS: emissions.get(word).keySet()) {
				// Look at how many times word appears as that tag
				Double tagFrequency = emissions.get(word).get(POS);
				Double overallTagFrequency = frequencyMap.get(POS);
				// Assign the probability of word appearing as POS tag divided
				// by any word appearing as that tag to the word
				Double emissionProbability = Math.log(tagFrequency / 
				overallTagFrequency);
				emissions.get(word).put(POS, emissionProbability);
			}
		}
		
		// Go over all states, normalize transition probabilities
		for (String tag1: transitions.keySet()) {
			// Look at how many times any tag follows tag1
			Double overallTransitionFrequency = 0.0;
			for (String otherTag: transitions.get(tag1).keySet()) {
				// Keep track of transition frequency
				overallTransitionFrequency += transitions.get(tag1).
				get(otherTag);
			}
			for (String tag2: transitions.get(tag1).keySet()) {
				// Look at how many times tag2 follows tag1
				Double transitionFrequency = transitions.get(tag1).get(tag2);
				// Transition probability is number of times tag2 follows tag1
				// divided by number of times any tag follows tag1
				Double transitionProbability = Math.log(transitionFrequency / 
				overallTransitionFrequency);
				transitions.get(tag1).put(tag2, transitionProbability);
			}
		}
	}
	
	/**
	 * Train the model based on the entire data set
	 */
	public void train() {
		train(corpusWords, corpusTags, corpusWords.size());
	}
	
	/**
	 * Use Viterbi tagging to find the best sequence of tags for a line
	 * Keep track of best probability for current and previous states
	 * @return ArrayList of tags
	 */
	public ArrayList<String> tag(String input) {
		Double emissionsValue;    // What is the value of the word in emissions?
		
		// Map to keep track of possible states
		ArrayList<Map<String, String>> backTrace = new ArrayList<Map<String, String>>();
		// Map to keep track of best possible score from previous
		HashMap<String, Double> prevScores = new HashMap<String, Double>();
		
		// Make all input to lower case
		String lowerCaseLine = input.toLowerCase();
		// Split input based on spaces
		String[] splitInput = lowerCaseLine.split(" ");
		// Initially previous score is 0, beginning of sentence so tag is *
		prevScores.put("#", 0.0);
		
		// Initialize backTrace ArrayList
		for (int i = 0; i < splitInput.length; i ++) {
			backTrace.add(i, new HashMap<String, String>());
		}
		
		// Loop through every word in the input sentence
		for (int i = 0; i < splitInput.length; i++) {
			// New map to store next scores
			HashMap<String, Double> nextScores = new HashMap<String, Double>();
			// For each possible state
			for (String state: prevScores.keySet()) {
				if (transitions.get(state) != null) {
					// For each possible transition from state to next state
					for (String nextState: transitions.get(state).keySet()) {
						// Check if we've seen emissions value before
						if (!emissions.containsKey(splitInput[i])) {
							// If we haven't, set it to U
							emissionsValue = UNKNOWN_VALUE;
						}
						else if (!emissions.get(splitInput[i]).containsKey(nextState)) {
							// Set it to U if we haven't seen POS
							emissionsValue = UNKNOWN_VALUE;
						}
						else {
							// If we have, just use its value
							emissionsValue = emissions.get(splitInput[i]).
							get(nextState);
						}
						// Find next score based on previous score
						Double nextScore = prevScores.get(state) + transitions.get(state).
						get(nextState) + emissionsValue;
						// Adjust nextScores values
						if (!nextScores.containsKey(nextState) || nextScore > nextScores.
						get(nextState)) {
							nextScores.put(nextState, nextScore);
							// Remember that we got to next state for i from that state
							backTrace.get(i).put(nextState, state);
						}
					}
				}
			}
			// Set previous scores equal to next scores
			prevScores = nextScores;
		}
		
		// Store the most likely sequence of tags
		ArrayList<String> tagSequence = new ArrayList<String>();
		// Most likely tag for the last item
		String mostLikelyTag = "";
		// Frequency of most likely tag
		Double highestFrequency = Double.NEGATIVE_INFINITY;
		
		// Determine most likely tag for last item
		for (String tag: prevScores.keySet()) {
			if (prevScores.get(tag) > highestFrequency) {
				highestFrequency = prevScores.get(tag);
				mostLikelyTag = tag;
			}
		}
		
		// Use backTrace list to determine most probable sequence of tags
		String previousTag = mostLikelyTag;
		// Start at the end of the list, work backwards
		int i = splitInput.length - 1;
		while (!previousTag.equals("#")) {
			// Add most likely tag to the sequence
			tagSequence.add(mostLikelyTag);
			// Get the most likely previous tag
			previousTag = backTrace.get(i).get(mostLikelyTag);
			// Decrement i and assign previous tag to new 
			// most likely tag
			mostLikelyTag = previousTag; i--;
		}
		// Reverse the sequence of tags
		Collections.reverse(tagSequence);
		return tagSequence;
	}
	
	/**
	 * Take user input and tag it based on POS
	 */
	public void tagInput() {
		// Take input in the console from user
		String sentence = " ";                          // Input sentence
		Scanner input = new Scanner(System.in);         // Get user input
		
		// Tell user how to quit
		System.out.print("To quit the program, type return in answer to a "
		+ "question");
		// Prompt the user until they quit
		while (!sentence.equals("return")) {
			System.out.print("\nPlease enter a sentence: ");
			sentence = input.nextLine();
			
			// Quit the program if the command is return
			if (sentence.equals("return")) {
				System.out.println("\nBye!");
				return;
			}
			// Otherwise, tag the sentence
			else {
				// Print the sentence and its tags
				System.out.println("\n" + sentence);
				System.out.println(tag(sentence));
			}
		}
	}
	
	/**
	 * Train on small partition, test on all other partitions
	 * Helper method for cross-validation
	 * @param partitions number of partitions
	 * @param first first partition to set aside
	 * @param n lines to load and process
	 * @return percentage accuracy
	 */
	private double test(int partitions, int first, int n) {
		// Tell the user we're still testing
		System.out.print("... ");
		// ArrayLists to store words we want to train and test on
		ArrayList<String[]> wordsToTrain = new ArrayList<String[]>();
		ArrayList<String[]> wordsToTest = new ArrayList<String[]>();
		
		// ArrayLists to store tags we want to train and test on
		ArrayList<String[]> tagsToTrain = new ArrayList<String[]>();
		ArrayList<String[]> tagsToTest = new ArrayList<String[]>();
		
		// Split the file into (1 / number of partitions) parts
		for (int i = 0; i < Math.min(n, corpusWords.size()); i++) {
			if ((i + first) % partitions == 0) {
				// Test on one out of every n lines
				wordsToTest.add(corpusWords.get(i));
				tagsToTest.add(corpusTags.get(i));
			}
			else {
				// Train on the remaining lines
				wordsToTrain.add(corpusWords.get(i));
				tagsToTrain.add(corpusTags.get(i));
			}
		}

		// Train based on the words and tags to train
		train(wordsToTrain, tagsToTrain, wordsToTrain.size());
		
		Double matchingTags = 0.0;    // number of tags that match
		Double totalTags = 0.0;	      // number of tags we've seen
		
		// Tag based on model we just trained
		for (int i = 0; i < wordsToTest.size(); i++) {
			String sentenceToTest = "";
			for (String word: wordsToTest.get(i)) {	
				// Form sentence we want to test on
				sentenceToTest += word + " ";
			}
			// Remove the last space in the sentence
			sentenceToTest = sentenceToTest.substring(0, sentenceToTest.length() - 1);
			// Tag each sample sentence
			ArrayList<String> testingTags = tag(sentenceToTest);
			for (int j = 0; j < testingTags.size(); j++) {
				// If tag matches expected tag, add one to expected tags
				if (testingTags.get(j).equals(tagsToTest.get(i)[j])) {
					matchingTags += 1.0;
				}
				// Add one to total number of tags
				totalTags += 1.0;
			}
		}
		// Return the percentage accuracy
		return((matchingTags / totalTags) * 100);
	}
	
	/**
	 * Cross-validate the data set, returning an average probability
	 * @param partitions number of partitions
	 * @param n lines to load and process
	 * @return average percent accuracy
	 */
	public double crossValidate(int partitions, int n) {
		// Tell the user we're testing
		System.out.print("Now testing... ");
		// Store the average percent accuracy
		Double averagePercentAccuracy = 0.0; 
		for (int i = 0; i < partitions; i ++) {
			// Percent accuracy from training on portion
			Double percentAccuracy = test(partitions, i, n);
			// Add to average percent accuracy
			averagePercentAccuracy += percentAccuracy;
		}
		// Get and return average percent accuracy
		averagePercentAccuracy /= (partitions);
		return (averagePercentAccuracy);
	}
	
	/**
	 * Main method for testing
	 */
	public static void main(String[] args) {
		POSTagger test = new POSTagger();
		try {
			test.load();
			System.out.println("\n\nAverage accuracy is " + test.crossValidate(5, 1000) +
			"%\n");
			test.train(); 
			test.tagInput();
		} 
		catch (IOException e) {
			System.err.println("Either no such file or directory exists, or "
			+ "the file is corrupt!");
		}
	}
}
