import java.util.ArrayList;

public class HuffPriorityQueue<E extends Comparable<? super E>>{
	
	private ArrayList<E> queue;

	/**
	 * Creates an empty priority queue.
	 */
	public HuffPriorityQueue() {
		queue = new ArrayList<>();
	}
	/**
	 * Enqueues or adds the given TreeNode to this queue which is in sorted order.
	 * @param node, the TreeNode to be added to this queue.
	 */
	public void enqueue(E node) {
		boolean enqueued = false;
		//finds correct position to add
		for (int i = queue.size() - 1; i >= 0 && !enqueued; i--) {
			//finds when given node is less than node in list
			if (queue.get(i).compareTo(node) > 0) {
				queue.add(i + 1, node);
				enqueued = true;
			}
		}
		//if node is either last or first in queue
		if (!enqueued) {
		    queue.add(0, node);
		}
	}
	
	/**
	 * Finds the first element of this queue but does not remove it.
	 * @return the first element of this queue.
	 */
	public E peek() {
		return queue.get(queue.size() - 1);
	}
	
	/**
	 * Removes or dequeues the first value of this queue and returns it.
	 * pre: queue must not be empty
	 * @return the first value of this queue.
	 */
	public E dequeue() {
		if (queue.size() == 0) {
			throw new IllegalArgumentException("Violation of precondition: dequeue."
					+ " This queue must not be empty.");
		}
		E tn = queue.get(queue.size() - 1);
		queue.remove(queue.size() - 1);
		//gets the first value of queue
		return tn;
	}
	
	/**
	 * Gets the number of elements in this queue.
	 * @return the size of this queue.
	 */
	public int size() {
		return queue.size();
	}
	
	/**
	 * @return the values of this queue in order.
	 */
	public String toString() {
		StringBuilder sb = new StringBuilder("[");
		//gets queue values in order
		for (int i = queue.size() - 1; i >= 0; i--) {
			sb.append(queue.get(i));
		}
		return sb.append("]").toString();
	}
}
