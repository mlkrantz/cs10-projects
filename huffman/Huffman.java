import java.io.*;
import java.util.*;

import javax.swing.*;

/**
 * Huffman Encoding
 * Class uses Huffman encoding to compress and decompress text files
 * Create a set of prefix-free codes based on character frequencies
 * Dartmouth CS 10, Winter 2014, PS 4
 * @author Matt Krantz
 *
 */
public class Huffman {
	private Map<Character, Integer> frequencyTable; 			// Map to store character frequencies
	private String pathName;									// Name of the input file
	
	private PriorityQueue<BinaryTree<CharCount>> treeQueue;		// PQ of CharCount trees
	private BinaryTree<CharCount> singleTree;					// One tree of all values
	
	private Map<Character, String> codeMap;						// Map to store code sequences
	
	private String compressedPathName;							// Name of the compressed file
	private String decompressedPathName;						// Name of the decompressed file
	
	public Huffman(String pathName) {
		// Initialize the frequency table
		frequencyTable = new HashMap<Character, Integer>();
		// Initialize the code map
		codeMap = new HashMap<Character, String>();
		this.pathName = pathName;	// Input file
	}
	
	
	/**
	  * Puts up a fileChooser and gets path name for file to be opened.
	  * Returns an empty string if the user clicks "cancel".
	  * @return path name of the file chosen	
	  */
	 public static String getFilePath() {
		 JFileChooser fc = new JFileChooser(".");   // start at current directory
		 int returnVal = fc.showOpenDialog(null);
		 if(returnVal == JFileChooser.APPROVE_OPTION) {
			 File file = fc.getSelectedFile();
			 String pathName = file.getAbsolutePath();
			 return pathName;
		 }
		 else {
			 return "";
		 }
	 }
	
	/**
	 * Read the file one character at a time, and update its value in the 
	 * frequency table. Produces a map that maps each character to the number 
	 * of times that it appears in the file!
	 */
	public void setFrequencyTable() throws IOException {
		// Read in the given file
		BufferedReader input = new BufferedReader(new FileReader(pathName));
		try {
			// Look through file character by character
			int c;	// To store character
			while ((c = input.read()) != -1) {
				// Character being read
				char ch = (char) c;
				// If the character is not in the map, add it
				if (!frequencyTable.containsKey(ch)) {
					frequencyTable.put(ch, 1);
				}
				// Otherwise, increment frequency by one
				else {
					int frequency = frequencyTable.get(ch); frequency ++;
					// Replace current with incremented value
					frequencyTable.put(ch, frequency);
				}
			}
		}
		finally {
			// Close the input file
			input.close();
		}
	}
	
	/**
	 * Create size-1 trees for each character, and add those trees to 
	 * a priority queue. Then, use priority queue to create overall 
	 * tree based on frequencies!
	 */
	public void createTree() {
		TreeComparator comparator = new TreeComparator();
		// Priority queue with appropriate comparator
		treeQueue = new PriorityQueue<BinaryTree<CharCount>>(frequencyTable.size() + 1, 
		comparator);
		// For every character that's a key in the frequency table...
		Set<Character> charList = frequencyTable.keySet();
		for (char ch: charList) {
			// Create a new CharCount object and add it to its own tree
			CharCount newCharCount = new CharCount(ch, frequencyTable.get(ch));
			BinaryTree<CharCount> charCountTree = new BinaryTree<CharCount>
			(newCharCount);
			// Add tree to priority queue
			treeQueue.add(charCountTree);
		}
		
		// Handle boundary cases
		if (treeQueue.size() == 0) {
			// If no elements, just initialize empty arbitrary tree 
			// (not read during the compression process)
			singleTree = new BinaryTree<CharCount>(new CharCount('0', 0));
			return;
		}
		if (treeQueue.size() == 1) {
			// If one element, tree with just that element (other element
			// is just arbitrary, not used)
			BinaryTree<CharCount> child = treeQueue.element();
			singleTree = new BinaryTree<CharCount>(null, child, new BinaryTree<CharCount>(new 
			CharCount('0', 0)));
			return;
		}
		
		// Create the singular tree structure
		while (treeQueue.size() > 1) {
			// Remove the two lowest-frequency trees
			BinaryTree<CharCount> tree1 = treeQueue.remove();
			BinaryTree<CharCount> tree2 = treeQueue.remove();
			// New CharCount object with no char, summed frequencies
			CharCount charSum = new CharCount(tree1.getData().getFrequency() +
			tree2.getData().getFrequency());
			// Create a parent tree, attach two children
			BinaryTree<CharCount> parentTree = new BinaryTree<CharCount>(charSum, 
			tree1, tree2);
			// Add the new tree back into the queue
			treeQueue.add(parentTree);
		}
		// Single tree is remaining element in queue
		singleTree = treeQueue.element();
	}
	
	/**
	 * Traverse the single tree to retrieve code map
	 * Uses recursive traverse() helper method
	 */
	public void retrieveCode() {
		// Sequence of bits is initially empty
		String bitSequence = new String();
		// Traverse the tree
		traverse(singleTree, bitSequence);
	}
	
	/**
	 * Create a map (from a single traversal of the tree) that pairs characters 
	 * with code, where code describes path from root to that character
	 * @param bitSequence initially empty
	 */
	public void traverse(BinaryTree<CharCount> tree, String bitSequence) {
		// If the tree has a left subtree...
		if (tree.hasLeft()) {
			// Add a 0 to the bit sequence and recurse
			traverse(tree.getLeft(), bitSequence + "0");
		}
		// If the tree has a right subtree...
		if (tree.hasRight()) {
			// Add a 1 to the bit sequence and recurse
			traverse(tree.getRight(), bitSequence + "1");
		}
		// Otherwise we've reached a leaf node
		else {
			// Store bit sequence in map with char as key
			codeMap.put(tree.data.getCh(), bitSequence);
		}
	}
	
	/**
	 * Compress the given file using the code map
	 */
	public void compress() throws IOException {
		// Name of the compressed file
		compressedPathName = pathName.substring(0, pathName.length() - 4);
		// Removed .txt, now add _compressed.txt
		compressedPathName += "_compressed.txt";
		// BitWriter to write bits to the compressed file
		BufferedBitWriter bitOutput = new BufferedBitWriter(compressedPathName);
		
		// Repeatedly read next character in text file
		BufferedReader input = new BufferedReader(new FileReader(pathName));
		try {
			int c;	// To store character
			while ((c = input.read()) != -1) {
				// Character being read
				char ch = (char) c;
				// Look up the file's code in the code map
				String chCode = codeMap.get(ch);
				// Write each bit in the code to the new file
				for (char bit: chCode.toCharArray()) {
					if (bit == '0') {
						bitOutput.writeBit(0);
					}
					else if (bit == '1') {
						bitOutput.writeBit(1);
					}
				}
			}
		}
		finally {
			// Close the input and output files
			bitOutput.close(); input.close();
		}
	}
	
	/**
	 * Decompress the file using the code tree
	 */
	public void decompress() throws IOException {
		// Name of the decompressed file
		decompressedPathName = pathName.substring(0, pathName.length() - 4);
		// Removed .txt, now add _compressed.txt
		decompressedPathName += "_decompressed.txt";
		// BufferedWriter to write characters to decompressed file
		BufferedWriter output = new BufferedWriter(new FileWriter(decompressedPathName)); 
		
		// BitReader to read bits from the compressed file
		BufferedBitReader bitInput = new BufferedBitReader(compressedPathName);
		try {
			int b;	// Value of bit being read
			// Tree to allow retrieval
			BinaryTree<CharCount> tree = singleTree;
			// Read compressed file bit by bit
			while ((b = bitInput.readBit()) != -1) {
				if (b == 0 && tree.hasLeft()) {
					// If the bit is 0, go left
					tree = tree.getLeft();
				}
				else if (b == 1 && tree.hasRight()) {
					// If the bit is 1, go right
					tree = tree.getRight();
				}
				if (!tree.hasRight() && !tree.hasLeft()) {
					// If we've reached a leaf, return the
					// character at that location...
					char character = tree.getData().getCh();
					// ...write it to the output file...
					output.write(character);
					// ...and go back to the root
					tree = singleTree;
				}
			}
		}
		finally {
			// Close the input and output files
			bitInput.close(); output.close();
		}
	}
	
	/**
	 * Main method for testing and compression/decompression
	 */
	public static void main(String[] args) {
		String file = getFilePath();
		try {
			Huffman huffmanTest = new Huffman(file);
			huffmanTest.setFrequencyTable();
			huffmanTest.createTree();
			huffmanTest.retrieveCode();
			huffmanTest.compress();
			huffmanTest.decompress();
		}
		catch (IOException e) {
			System.err.println("Either no such file or directory exists, " +
			"or the file is corrupt!");
		}
	}
}
