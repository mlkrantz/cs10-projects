import java.util.*;

/**
 * Compares two CharCount tree nodes
 * Dartmouth CS 10, Winter 2014, PS 4
 * @author Matt Krantz
 *
 */
public class TreeComparator implements Comparator<BinaryTree<CharCount>> {
	/**
	 * Return different values depending on the comparison of frequencies
	 * for CharCount objects in the binary tree
	 */
	public int compare(BinaryTree<CharCount> tree1, BinaryTree<CharCount> tree2) {
		// If tree1's char occurs less frequently than tree2's, return -1
		if (tree1.data.getFrequency() < tree2.data.getFrequency()) {
			return -1;
		}
		// If tree1's char occurs more frequently than tree2's, return 1
		else if (tree1.data.getFrequency() > tree2.data.getFrequency()) {
			return 1;
		}
		// Otherwise they occur equally frequently, so return 0
		else {
			return 0;
		}
	}
}
