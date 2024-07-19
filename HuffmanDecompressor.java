import java.io.IOException;

public class HuffmanDecompressor implements IHuffConstants{
	
	private BitInputStream bis;
	private BitOutputStream bos;
	private int size;
	
	/**
	 * Creates a new HuffmanDecompressor object from the given BitInputStream and BitOutputStream.
	 * @param inputStream, the BitInputStream containing the compressed file.
	 * @param outputStream, the BitOutputStream to write the uncompressed information to.
	 */
	public HuffmanDecompressor (BitInputStream inputStream, BitOutputStream outputStream) {
		bis = inputStream;
		bos = outputStream;
		size = 0;
	}
	
	/**
	 * Finds if the given file is a huffman file by checking if it starts with the 
	 * huff magic number.
	 * @return true if the given file was a huffman file and false otherwise.
	 * @throws IOException
	 */
	public boolean isHuffman() throws IOException {
		//checks for the magic number
		return bis.readBits(BITS_PER_INT) == MAGIC_NUMBER;
	}
	
	/**
	 * Uncompresses the bits of the compressed file using whichever header was
	 * given in the file header.
	 * @throws IOException
	 */
	public void writeOutBits() throws IOException {
		//finds the header type
		int headerType = bis.readBits(BITS_PER_INT);
		HuffPriorityQueue<TreeNode> hq = new HuffPriorityQueue<>();
		if (headerType == STORE_COUNTS) {
			//creates a queue based on the counts header
			hq = countsHeader();
		} else if (headerType == STORE_TREE) {
			//creates a queue based on the tree header
			hq = treeHeader();
		} else {
			throw new IOException("This file cannot be uncompressed as this file"
					+ " contains an unsuported header format.");
		}
		HuffmanTree ht = new HuffmanTree(hq);
		//writes the uncompressed information
		writeBits(ht);
		bis.close();
		bos.close();
	}
	
	/**
	 * Helper method for writeOutBits that writes out the uncompressed information
	 * using the given Huffman Tree.
	 * @param ht, the huffman tree that contains the values of the information.
	 * @throws IOException
	 */
	private void writeBits(HuffmanTree ht) throws IOException {
		boolean foundPeof = false;
		//stops running if peof value has been found
		while(!foundPeof) {
			TreeNode tn = ht.getRoot();
			//stops running when a leaf is found
			while (!tn.isLeaf()) {
				int nextBit = bis.readBits(1);
				if (nextBit == 0) {
					//traverse left down the tree if at a 0
					tn = tn.getLeft();
				} else if (nextBit == 1){
					//traverse right down the tree if at a 1
					tn = tn.getRight();
				} else {
					throw new IOException("Error reading compressed file. \n" + 
							"unexpected end of input. No PSEUDO_EOF value.");
				}
			}
			//if current value is the peof value
			if (tn.getValue() == PSEUDO_EOF) {
				foundPeof = true;
			} else {
				//writes uncompressed data for current value
				bos.writeBits(BITS_PER_WORD, tn.getValue());
				size += BITS_PER_WORD;
			}
		}
	}
	
	/**
	 * Helper method writeOutBits that creates a HuffPriorityQueue
	 * containing every value and its frequency using the counts method.
	 * @return a HuffPriorityQueue containing the values of this file and their frequencies.
	 * @throws IOException
	 */
	private HuffPriorityQueue<TreeNode> countsHeader() throws IOException {
		HuffPriorityQueue<TreeNode> hq = new HuffPriorityQueue<>();
		//runs for every ASCII value
		for (int i = 0; i < ALPH_SIZE; i++) {
			int freq = bis.readBits(BITS_PER_INT);
			//only adds values with frequencies higher than 0
			if (freq > 0) {
				TreeNode tn = new TreeNode(i, freq);
				hq.enqueue(tn);
			}
		}
		//adds the peof value to the queue
		TreeNode tn = new TreeNode(PSEUDO_EOF, 1);
		hq.enqueue(tn);
		return hq;
	}
	
	/**
	 * Helper method writeOutBits that creates a HuffPriorityQueue
	 * containing every value and its frequency using the tree method.
	 * @return
	 * @throws IOException
	 */
	private HuffPriorityQueue<TreeNode> treeHeader() throws IOException {
		//to read over the size of tree
		bis.readBits(BITS_PER_INT);
		TreeNode rootNode = new TreeNode(-1, -1);
		//builds an entire huffman tree from the given data and adds it to the queue.
		rootNode = treeHeaderHelp(rootNode);
		HuffPriorityQueue<TreeNode> hq = new HuffPriorityQueue<>();
		hq.enqueue(rootNode);
		return hq;
	}
	
	/**
	 * Helper method for treeHeader that builds a huffman tree on a singular TreeNode.
	 * @param tn, the current node of this tree.
	 * @return a TreeNode that contains the entire huffman tree for this file.
	 * @throws IOException
	 */
	private TreeNode treeHeaderHelp(TreeNode tn) throws IOException {
		int nextBit = bis.readBits(1);
		if (nextBit == 0) {
			//if next bit is a 0, add an internal node and continue traversing the tree
			TreeNode internalNode = new TreeNode(-1, -1);
			internalNode.setLeft(treeHeaderHelp(internalNode));
			internalNode.setRight(treeHeaderHelp(internalNode));
			return internalNode;
		} else if (nextBit == 1) {
			//if the next bit is a 1, add a leaf node with the specified value
			//and stop traversing the tree
			final int BITS_PER_VALUE = 9;
			TreeNode leafNode = new TreeNode(bis.readBits(BITS_PER_VALUE), -1);
			return leafNode;
	    } else {
	    	//if there is an error in the file
	    	throw new IOException("Format of file is bad, ran out of bits.");
	    }
	}
	
	/**
	 * Finds and returns the size of the uncompressed file.
	 */
	public int getSize() {
		return size;
	}
}
