package nachos.threads;

import java.util.LinkedList;

import nachos.machine.Lib;
import nachos.machine.Machine;
import nachos.threads.PriorityScheduler.PriorityThreadQueue;
import nachos.threads.Constants;

/**
 * The scheduling state of a thread. This should include the thread's
 * priority, its effective priority, any objects it owns, and the queue
 * it's waiting for, if any.
 *
 * @see	nachos.threads.KThread#schedulingState
 */
class BaseThreadState implements Comparable<BaseThreadState>, Constants {
	/**
	 * Allocate a new <tt>ThreadState</tt> object and associate it with the
	 * specified thread.
	 *
	 * @param thread the thread this state belongs to.
	 */
	public BaseThreadState(KThread thread) {
		this.thread = thread;
		//initialize the onQueue linkedlist
		this.waitedBy = new LinkedList<BasePriorityThreadQueue>();
		this.age = Machine.timer().getTime();
		this.effectivePriority = priorityDefault;
		this.waitingOn = null;
	}

	/**
	 * Return the priority of the associated thread.
	 *
	 * @return	the priority of the associated thread.
	 */
	public int getPriority() {
		return priority;
	}


	/**
	 * Calculate the Effective Priority of a thread and the thread that currently holds the resource
	 * it is waiting on.
	 */
	public void updateEffectivePriority() {
		int initialPriority = this.getPriority();
		int maxEffectivePriority = initialPriority;
		for(int i = 0; i < waitedBy.size(); i++){
			BasePriorityThreadQueue current = waitedBy.get(i);

			/*
				PriorityQueue<ThreadState> priorityQueue = new PriorityQueue<ThreadState>(current.priorityQueue);
				System.out.println("---printing a heap");
				System.out.println(priorityQueue.poll());
				System.out.println(priorityQueue.poll());
			 */

			BaseThreadState donator = (BaseThreadState) current.pickNextThread();
			if (donator != null){
				// System.out.println(donator.thread+", EffectPriority="+donator.getEffectivePriority());
				if ((donator.getEffectivePriority() > maxEffectivePriority) && current.transferPriority)
					maxEffectivePriority = donator.getEffectivePriority();
			}
		}
		this.effectivePriority = maxEffectivePriority;
		// pass the modification of effective on
		if (this.waitingOn != null && this.waitingOn.dequeuedThread != null){
			if (this.effectivePriority != this.waitingOn.dequeuedThread.effectivePriority){
				this.waitingOn.dequeuedThread.updateEffectivePriority();
			}
		};
		//System.out.println(this.effectivePriority);
	}

	/**
	 * Return the effective priority of the associated thread.
	 *
	 * @return	the effective priority of the associated thread.
	 */
	public int getEffectivePriority() {
		return this.effectivePriority;
	}
	/**
	 * Set the priority of the associated thread to the specified value.
	 *
	 * @param	priority	the new priority.
	 */
	public void setPriority(int priority) {
		// implement me
		// seems to have finished
		if (this.priority == priority)
			return;
		
		/*
		 * this was added since PriorityQueue do not reorder
		 * itself when modified
		 */
		this.updatePriority(priority);

		this.updateEffectivePriority();
		//this.waiting.dequeuedThread.calcEffectivePriority();
	}
	
	protected void updatePriority(int priority){
		this.priority = priority;
		if (this.waitingOn != null){
		    this.waitingOn.priorityQueue.remove(this);
		    this.waitingOn.priorityQueue.add(this);
        }
	}

	/**
	 * Called when <tt>waitForAccess(thread)</tt> (where <tt>thread</tt> is
	 * the associated thread) is invoked on the specified priority queue.
	 * The associated thread is therefore waiting for access to the
	 * resource guarded by <tt>waitQueue</tt>. This method is only called
	 * if the associated thread cannot immediately obtain access.
	 *
	 * @param	waitQueue	the queue that the associated thread is
	 *				now waiting on.
	 *
	 * @see	nachos.threads.ThreadQueue#waitForAccess
	 */
	public void waitForAccess(BasePriorityThreadQueue waitQueue) {
		System.out.println("in BaseThreadState.java, BaseThreadState.waitForAccess");
		Lib.assertTrue(Machine.interrupt().disabled());
		// added to avoid starvation
		this.age = Machine.timer().getTime();
		waitQueue.priorityQueue.add(this);
		this.waitingOn = waitQueue;
		this.updateEffectivePriority();
	}

	/**
	 * Called when the associated thread has acquired access to whatever is
	 * guarded by <tt>waitQueue</tt>. This can occur either as a result of
	 * <tt>acquire(thread)</tt> being invoked on <tt>waitQueue</tt> (where
	 * <tt>thread</tt> is the associated thread), or as a result of
	 * <tt>nextThread()</tt> being invoked on <tt>waitQueue</tt>.
	 *
	 * @see	nachos.threads.ThreadQueue#acquire
	 * @see	nachos.threads.ThreadQueue#nextThread
	 */
	public void acquire(BasePriorityThreadQueue waitQueue) {
		//Seems good, checks to see if queue is empty, if it is just make it dequeued thread.
		//needs to add waitQueue
		Lib.assertTrue(Machine.interrupt().disabled());
		Lib.assertTrue(waitQueue.priorityQueue.isEmpty());
		waitQueue.dequeuedThread = this;
		this.addQueue(waitQueue);
		this.updateEffectivePriority();
	}

	public int compareTo(BaseThreadState threadState){
		// bigger is polled first
		// -1:<, 0:=, 1:>
		//changed first if from > to <
		if (threadState == null)
			return 1;
		if (this.getEffectivePriority() < threadState.getEffectivePriority()){
			return -1;
		}else{ 
			if (this.getEffectivePriority() > threadState.getEffectivePriority()){
				return 1;
			}else{
				if (this.age >= threadState.age)
					return -1;
				else{ return 1; }
			}
		}
	}
	public void removeQueue(BasePriorityThreadQueue queue){
		waitedBy.remove(queue);
		updateEffectivePriority();
	}
	public void addQueue(BasePriorityThreadQueue queue){
		waitedBy.add(queue);
		updateEffectivePriority();
	}
	public String toString() {
		return "ThreadState thread=" + thread + ", priority=" + getPriority() + ", effective priority=" + getEffectivePriority();
	}
	/** The thread with which this object is associated. */
	protected KThread thread;
	/** The priority of the associated thread. */
	protected int priority = priorityDefault;
	/** The age of the thread state, used in comparator to avoid starvation */
	public long age = Machine.timer().getTime();
	/** a linked list representing all the queues it is getting priority from.*/
	protected LinkedList<BasePriorityThreadQueue> waitedBy;
	protected int effectivePriority;
	protected BasePriorityThreadQueue waitingOn;
}
