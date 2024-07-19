/*  Student information for assignment:
 *
 *  On my honor, Dominic Paruolo, this programming assignment is my own work
 *  and I have not provided this code to any other student.
 *
 *  Number of slip days used: 2
 *
 *  Student 1 (Student whose Canvas account is being used)
 *  UTEID: dmp3588
 *  email address: dominicparuolo78@gmail.com
 *  Grader name: Nidhi
 *
 */

import java.util.HashMap;

public class HuffmanTree {

	private TreeNode root;
	
	/**
	 * Creates a binary Huffman Tree from a given queue of elements.
	 * pre: Every element of the given queue must be a TreeNode object.
	 * @param pq, a queue of elements to be added to this tree.
	 */
	public HuffmanTree(HuffPriorityQueue<TreeNode> pq) {
		while (pq.size() > 1) {
			//removes first two nodes and adds new node back into queue
			TreeNode newNode = new TreeNode(pq.dequeue(), -1, pq.dequeue());
			pq.enqueue(newNode);
		}
		root = (TreeNode) pq.dequeue();
	}
	
	/**
	 * Gets all of the elements of this tree through an in order traversal.
	 */
	public String toString() {
		StringBuilder sb = new StringBuilder("[");
		//adds all elements of this tree to sb
		toStringHelp(root, sb);
		return sb.append("]").toString();
	}
	
	
	/**
	 * Helper method for toString that adds all elements of this tree to a StringBuilder.
	 * @param tn, the current TreeNode.
	 * @param sb, the StringBuilder object to append the given TreeNode to.
	 */
	private void toStringHelp(TreeNode tn, StringBuilder sb) {
		//no-op if current node is null
		if (tn != null) {
			//adds left subtree, this node, and the right subtree
			toStringHelp(tn.getLeft(), sb);
			sb.append(tn.getFrequency());
			toStringHelp(tn.getRight(), sb);
		}
	}
	
	/**
	 * Creates and returns a map of the Huffman Codings for each unique element of this tree.
	 * @return a map of the Huffman codings for each element of this tree.
	 */
	public HashMap<Integer, String> newCodeMap(){
		HashMap<Integer, String> codeMap = new HashMap<>();
		//updates the map with the huffman codings
		codeMapHelp(codeMap, "", root);
		return codeMap;
	}
	
	/**
	 * Helper method for newCodeMap that adds every Huffman Coding for each element of this 
	 * tree to the given map.
	 * @param map, the map to append the huffman codings to.
	 * @param code, the current huffman coding for an element.
	 * @param node, the current node of this tree this iteration is at.
	 */
	private void codeMapHelp(HashMap<Integer, String> map, String code, TreeNode node) {
		//base case if at a leaf node, add the current huffman coding to the map
		if (node.isLeaf()) {
			map.put(node.getValue(), code);
		} else { // traverse the left and right subtrees of this node
			String curCode = new String(code);
			curCode += 0;
			codeMapHelp(map, curCode, node.getLeft());
			curCode = curCode.substring(0, curCode.length() - 1);
			curCode += 1;
			codeMapHelp(map, curCode, node.getRight());
		}
	}
	
	/**
	 * Finds and returns the number of nodes in this tree.
	 * @return the size of this tree.
	 */
	public int size() {
		//gets the size
		return sizeHelp(root);
	}
	
	/**
	 * Helper method for size that finds and returns the number of nodes in this tree.
	 * @param tn, the current node in this tree.
	 * @return the size of this tree.
	 */
	private int sizeHelp(TreeNode tn) {
		//no more nodes on this path
		if (tn == null) {
			return 0;
		} else {
			//increments size and traverses left and right subtrees
			int size = 0;
			size += sizeHelp(tn.getLeft());
			size += sizeHelp(tn.getRight());
			return size + 1;
		}
	}
	
	/**
	 * Finds and returns the number of leaf nodes in this tree.
	 * @return the total number of leaf nodes.
	 */
	public int numLeafNodes() {
		return leafHelp(root);
	}
	
	/**
	 * Helper method for numLeafNodes that finds the number of leaf nodes in this tree.
	 * @param tn, the current node in this tree.
	 * @return the total number of leaf nodes.
	 */
	private int leafHelp(TreeNode tn) {
		//if the current node is a leaf
		if (tn.isLeaf()) {
			return 1;
		} else {
			//if current node is not a leaf node, traverse left and right subtrees
			int num = 0;
			num += leafHelp(tn.getLeft());
			num += leafHelp(tn.getRight());
			return num;
		}
	}
	
	/**
	 * @return the root node of this tree.
	 */
	public TreeNode getRoot() {
		return root;
	}
}
