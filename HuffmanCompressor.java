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

import java.io.IOException;
import java.util.HashMap;
import java.util.TreeMap;

public class HuffmanCompressor implements IHuffConstants{

	private BitInputStream bis;
	private HuffmanTree ht;
	private int headerFormat;
	private TreeMap<Integer, Integer> freqMap;
	private int originalSize;
	private int compressedSize;
	
	/**
	 * Creates a new HuffmanCompressor object from the given BitInputStream and header format.
	 * @param newStream, the BitInputStream containing the file to compress.
	 * @param hf, the header format for the file.
	 * @throws IOException
	 */
	public HuffmanCompressor(BitInputStream newStream, int hf) throws IOException {
		originalSize = 0;
		bis = newStream;
		headerFormat = hf;
		//holds each values frequency
		freqMap = getFreqMap();
		//sorts each value frequency combo into a queue
		HuffPriorityQueue<TreeNode> freqQueue = getFreqQueue(freqMap);
		ht = new HuffmanTree(freqQueue);
				compressedSize = compSize();
	}
	
	/**
	 * Helper method for HuffmanCompressor that puts every value and its frequency into a map.
	 * @return a map of every value and its frequency.
	 * @throws IOException
	 */
	private TreeMap<Integer, Integer> getFreqMap() throws IOException{
		TreeMap<Integer, Integer> freqMap = new TreeMap<>();
		int currentWord = bis.readBits(BITS_PER_WORD);
		//traverses entire file
		while (currentWord != -1) {
			//updates the original size of the file counter
			originalSize += 8;
			//updates frequencies of already present values
			if (freqMap.containsKey(currentWord)) {
				freqMap.put(currentWord, freqMap.get(currentWord) + 1);
			} else { //adds new values to map
				freqMap.put(currentWord, 1);
			}
			//gets next value
			currentWord = bis.readBits(BITS_PER_WORD);
		}
		freqMap.put(PSEUDO_EOF, 1);
		bis.close();
		return freqMap;
	}
	
	/**
	 * Helper method for HuffmanCompressor that creates a TreeNode out
	 * of every value and its frequency and then sorts the nodes into a queue.
	 * @param freqMap, the map containing each value and its frequency.
	 * @return a HuffPriorityQueue that contains sorted TreeNodes of
	 * the values and their frequencies from this BitInputStream.
	 */
	private HuffPriorityQueue<TreeNode> getFreqQueue(TreeMap<Integer, Integer> freqMap) {
		HuffPriorityQueue<TreeNode> freqQueue = new HuffPriorityQueue<>();
		//traverses entire map
		for (Integer value: freqMap.keySet()) {
			//sorts new TreeNode into queue
			TreeNode newNode = new TreeNode(value, freqMap.get(value));
			freqQueue.enqueue(newNode);
		}
		return freqQueue;
	}
	
	/**
	 * Finds and returns the original size of this file.
	 * @return the original size of this file.
	 */
	public int originalSize() {
		return originalSize;
	}
	
	/**
	 * @return the HuffmanTree of this BitInputStream.
	 */
	public HuffmanTree getTree() {
		return ht;
	}
	
	public void writeOutBits(BitInputStream newBis, BitOutputStream bos) throws IOException {
		//writes magic number
		bos.writeBits(BITS_PER_INT, MAGIC_NUMBER);
		compressedSize += BITS_PER_INT;
		//writes either counts or tree header
		if (headerFormat == STORE_COUNTS) {			
			countsHeader(bos);
		} else if (headerFormat == STORE_TREE) {
			treeHeader(bos);
		}
		//writes data of file and peof
		writeData(newBis, bos);
		bos.close();
		newBis.close();
	}
	
	private void countsHeader(BitOutputStream bos) {
		//writes counts header value
		bos.writeBits(BITS_PER_INT, STORE_COUNTS);
		compressedSize += BITS_PER_INT;
		//writes frequency of elements
		for (int i = 0; i < ALPH_SIZE; i++) {
			if (freqMap.containsKey(i)) {
				bos.writeBits(BITS_PER_INT, freqMap.get(i));
			} else {
				bos.writeBits(BITS_PER_INT, 0);
			}
			compressedSize += BITS_PER_INT;
		}
	}
	
	private void treeHeader(BitOutputStream bos) {
		//writes tree header value
		bos.writeBits(BITS_PER_INT, STORE_TREE);
		compressedSize += BITS_PER_INT;
		final int BITS_PER_VALUE = 9;
		int treeSize = (ht.numLeafNodes() * BITS_PER_VALUE) + ht.size();
		//writes tree size
		bos.writeBits(BITS_PER_INT, treeSize);
		compressedSize += BITS_PER_INT;
		//writes tree
		writeTree(bos, ht.getRoot());
	}
	
	private void writeTree(BitOutputStream bos, TreeNode tn) {
		//writes 1 and node value if a leaf
		if (tn.isLeaf()) {
			bos.writeBits(1, 1);
			compressedSize += 1;
			bos.writeBits(BITS_PER_WORD + 1, tn.getValue());
			compressedSize += BITS_PER_WORD + 1;
		} else { //writes 0 and traverses rest of tree
			bos.writeBits(1, 0);
			compressedSize += 1;
			writeTree(bos, tn.getLeft());
			writeTree(bos, tn.getRight());
		}
	}
	
	private void writeData(BitInputStream newBis, BitOutputStream bos) throws IOException {
		HashMap<Integer, String> codeMap = ht.newCodeMap();
		int currentWord = newBis.readBits(BITS_PER_WORD);
		//traverses entire file
		while (currentWord != -1) {
			String code = codeMap.get(currentWord);
			//writes out each bit from new huffman encoding for each value
			for (int i = 0; i < code.length(); i++) {
				String subCode = code.substring(i, i + 1);
				bos.writeBits(1, Integer.parseInt(subCode));
				compressedSize++;
			}
			currentWord = newBis.readBits(BITS_PER_WORD);
		}
		//writes bits for huffman encoding for peof value
		String peofCode = codeMap.get(PSEUDO_EOF);
		for (int i = 0; i < peofCode.length(); i++) {
			String subCode = peofCode.substring(i, i + 1);
			bos.writeBits(1, Integer.parseInt(subCode));
			compressedSize++;
		}
	}
	
	/**
	 * Finds and returns the size of the compressed file.
	 * @return the size of the compressed file.
	 */
	private int compSize() {
		int comSize = 0;
		//for the magic number
		comSize += BITS_PER_INT;
		//for the count or tree header
		comSize += BITS_PER_INT;
		if (headerFormat == STORE_COUNTS) {
			//for the counts
			comSize += BITS_PER_INT * ALPH_SIZE;
		} else if (headerFormat == STORE_TREE) {
			//for the tree size
			comSize += BITS_PER_INT;
			int treeSize = ht.size();
			int numLeaves = ht.numLeafNodes();
			//for the leaf nodes
			comSize += numLeaves;
			comSize += (1 + BITS_PER_WORD) * numLeaves;
			//for the internal nodes
			comSize += treeSize - numLeaves;
		}
		HashMap<Integer, String> codeMap = ht.newCodeMap();
		for (Integer value: codeMap.keySet()) {
			comSize += codeMap.get(value).length() * freqMap.get(value);
		}
		return comSize;
	}
	
	/**
	 * @return the size of the compressed file.
	 */
	public int compressedSize() {
		return compressedSize;
	}
	
	
	
	
	
	
	
	
}
