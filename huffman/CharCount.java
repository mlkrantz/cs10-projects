/**
 * Class to store a character and its frequency
 * Data type for binary tree used in Huffman.java
 * Dartmouth CS 10, Winter 2014, PS 4
 * @author Matt Krantz
 *
 */
public class CharCount {
	private Character ch;				// Character stored in the object
	private Integer frequency; 			// Frequency of the character
	
	public CharCount(char ch, int frequency) {
		// Store the character and its frequency in the object
		this.ch = ch; this.frequency = frequency;
	}
	
	public CharCount(int frequency) {
		// If no character specified, just store frequency
		this.ch = null; this.frequency = frequency;
	}

	// Getters and setters for character
	public char getCh() {
		return ch;
	}

	public void setCh(char ch) {
		this.ch = ch;
	}

	// Getters and setters for frequencies
	public int getFrequency() {
		return frequency;
	}

	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}
	
	// Convert CharCount object to string
	public String toString() {
		if (ch != null) {
			return ch.toString() + ": " + frequency.toString();
		}
		else {
			return "*: " + frequency.toString();
		}
	}
}
