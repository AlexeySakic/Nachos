package nachos.threads;

import java.util.PriorityQueue;

abstract public class BasePriorityThreadQueue extends ThreadQueue{
	BasePriorityThreadQueue(boolean transferPriority) {
		this.transferPriority = transferPriority;
		dequeuedThread = null;
	}

	abstract public void waitForAccess(KThread thread);

	abstract public void acquire(KThread thread);

	abstract public KThread nextThread();

	/**
	 * Return the next thread that <tt>nextThread()</tt> would return,
	 * without modifying the state of this queue.
	 *
	 * @return	the next thread that <tt>nextThread()</tt> would
	 *		return.
	 */
	abstract protected BaseThreadState pickNextThread();

	abstract public void print();

	/**
	 * The base priority queue object.
	 */
	protected PriorityQueue<BaseThreadState> priorityQueue = new PriorityQueue<BaseThreadState>();
	/** The most recently dequeued ThreadState. */
	protected BaseThreadState dequeuedThread = null;
	/**
	 * <tt>true</tt> if this queue should transfer priority from waiting
	 * threads to the owning thread.
	 */
	public boolean transferPriority;
}