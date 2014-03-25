//{intytsdovtxtdxhxhoid{idofx`ofridofit`ofkh`bido
//PART OF THE NACHOS. DON'T CHANGE CODE OF THIS LINE
package nachos.threads;

import nachos.machine.*;

import java.util.LinkedList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.PriorityQueue;

/**
 * A scheduler that chooses threads based on their priorities.
 *
 * <p>
 * A priority scheduler associates a priority with each thread. The next thread
 * to be dequeued is always a thread with priority no less than any other
 * waiting thread's priority. Like a round-robin scheduler, the thread that is
 * dequeued is, among all the threads of the same (highest) priority, the
 * thread that has been waiting longest.
 *
 * <p>
 * Essentially, a priority scheduler gives access in a round-robin fassion to
 * all the highest-priority threads, and ignores all other threads. This has
 * the potential to
 * starve a thread if there's always a thread waiting with higher priority.
 *
 * <p>
 * A priority scheduler must partially solve the priority inversion problem; in
 * particular, priority must be donated through locks, and through joins.
 */
public class PriorityScheduler extends Scheduler {
	/**
	 * Allocate a new priority scheduler.
	 */
	public PriorityScheduler() {
	}

	/**
	 * Allocate a new priority thread queue.
	 *
	 * @param	transferPriority	<tt>true</tt> if this queue should
	 *					transfer priority from waiting threads
	 *					to the owning thread.
	 * @return	a new priority thread queue.
	 */
	public ThreadQueue newThreadQueue(boolean transferPriority) {
		return new PriorityThreadQueue(transferPriority);
	}

	public int getPriority(KThread thread) {
		Lib.assertTrue(Machine.interrupt().disabled());

		return getThreadState(thread).getPriority();
	}

	public int getEffectivePriority(KThread thread) {
		Lib.assertTrue(Machine.interrupt().disabled());

		return getThreadState(thread).getEffectivePriority();
	}

	public void setPriority(KThread thread, int priority) {
		Lib.assertTrue(Machine.interrupt().disabled());

		/*
		Lib.assertTrue(priority >= priorityMinimum &&
				priority <= priorityMaximum);
		 */
		// maybe it is better to modify instead of assert true.
		priority = priority >= priorityMinimum ?
				(priority <= priorityMaximum ? priority : priorityMaximum)
				: priorityMinimum; 

				getThreadState(thread).setPriority(priority);
	}

	public boolean increasePriority() {
		boolean intStatus = Machine.interrupt().disable();

		KThread thread = KThread.currentThread();

		int priority = getPriority(thread);
		// NOTE!!
		// why can you return without restoring interrupt?
		if (priority == priorityMaximum)
			return false;

		setPriority(thread, priority+1);

		Machine.interrupt().restore(intStatus);
		return true;
	}

	public boolean decreasePriority() {
		boolean intStatus = Machine.interrupt().disable();

		KThread thread = KThread.currentThread();

		int priority = getPriority(thread);
		// NOTE!!
		// why can you return without restoring interrupt?
		if (priority == priorityMinimum)
			return false;

		setPriority(thread, priority-1);

		Machine.interrupt().restore(intStatus);
		return true;
	}

	/**
	 * The default priority for a new thread. Do not change this value.
	 */
	public static final int priorityDefault = 1;
	/**
	 * The minimum priority that a thread can have. Do not change this value.
	 */
	public static final int priorityMinimum = 0;
	/**
	 * The maximum priority that a thread can have. Do not change this value.
	 */
	public static final int priorityMaximum = 7;

	/**
	 * Return the scheduling state of the specified thread.
	 *
	 * @param	thread	the thread whose scheduling state to return.
	 * @return	the scheduling state of the specified thread.
	 */
	protected ThreadState getThreadState(KThread thread) {
		if (thread.schedulingState == null)
			thread.schedulingState = new ThreadState(thread);

		return (ThreadState) thread.schedulingState;
	}

	private static void printThreadInfo(PriorityScheduler s, KThread thread){
		System.out.println("*** "+ thread.getName() + ": " +
				"EffectPriority="+s.getThreadState(thread).getEffectivePriority() + ", "+
				"OriginPriority="+s.getThreadState(thread).getPriority());
	}
	public static void selfTest() {
		System.out.println("\nPriorityScheduler test");
		PriorityScheduler s = new PriorityScheduler();
		ThreadQueue queue1 = s.newThreadQueue(true);
		ThreadQueue queue2 = s.newThreadQueue(true);
		ThreadQueue queue3 = s.newThreadQueue(true);

		/**
		 * Prepare for test
		 */
		KThread thread1 = new KThread();
		KThread thread2 = new KThread();
		KThread thread3 = new KThread();
		KThread thread4 = new KThread();
		KThread thread5 = new KThread();
		thread1.setName("Thread1");
		thread2.setName("Thread2");
		thread3.setName("Thread3");
		thread4.setName("Thread4");
		thread5.setName("Thread5");


		boolean intStatus = Machine.interrupt().disable();

		/**
		 * Test for case 1
		 */
		System.out.println("\nCase 1:");
		queue1.acquire(thread1);
		System.out.println("Thread 1 acquires resource 1");
		queue1.waitForAccess(thread2);
		System.out.println("Thread 2 waits for resource 1");
		queue1.waitForAccess(thread5);
		System.out.println("Thread 5 waits for resource 1");
		queue2.acquire(thread1);
		System.out.println("Thread 1 acquires resource 2");
		queue2.waitForAccess(thread4);
		System.out.println("Thread 4 waits for resource 2");
		queue3.acquire(thread5);
		System.out.println("Thread 5 acquires resource 3");
		queue3.waitForAccess(thread3);
		System.out.println("Thread 3 waits for resource 3");
		printThreadInfo(s, thread1);
		printThreadInfo(s, thread2);
		printThreadInfo(s, thread3);
		printThreadInfo(s, thread4);
		printThreadInfo(s, thread5);

		/**
		 * Test for case 2
		 */
		System.out.println("\nCase 2:");
		s.getThreadState(thread2).setPriority(4);
		System.out.println("Thread 2 set priority to 4");
		printThreadInfo(s, thread1);
		printThreadInfo(s, thread2);
		printThreadInfo(s, thread3);
		printThreadInfo(s, thread4);
		printThreadInfo(s, thread5);

		/**
		 * Test for case 3
		 */
		System.out.println("\nCase 3:");
		queue2.waitForAccess(thread4);
		System.out.println("Thread 4 waits for resource 2");
		s.getThreadState(thread3).setPriority(5);
		System.out.println("Thread 3 set priority to 5");

		printThreadInfo(s, thread1);
		printThreadInfo(s, thread2);
		printThreadInfo(s, thread3);
		printThreadInfo(s, thread4);
		printThreadInfo(s, thread5);

		/**
		 * Test for case 4
		 */
		System.out.println("\nCase 4:");
		s.getThreadState(thread3).setPriority(3);
		System.out.println("Thread 3 set priority to 3");

		printThreadInfo(s, thread1);
		printThreadInfo(s, thread2);
		printThreadInfo(s, thread3);
		printThreadInfo(s, thread4);
		printThreadInfo(s, thread5);

		/**
		 * Test for case 5
		 */
		System.out.println("\nCase 5:");
		queue1.nextThread();
		System.out.println("Thread 1 release resource 1");

		printThreadInfo(s, thread1);
		printThreadInfo(s, thread2);
		printThreadInfo(s, thread3);
		printThreadInfo(s, thread4);
		printThreadInfo(s, thread5);

		/**
		 * Test for case 6
		 */
		System.out.println("\nCase 6:");
		s.getThreadState(thread4).setPriority(2);
		System.out.println("Thread 4 set priority to 2");

		printThreadInfo(s, thread1);
		printThreadInfo(s, thread2);
		printThreadInfo(s, thread3);
		printThreadInfo(s, thread4);
		printThreadInfo(s, thread5);

		Machine.interrupt().restore(intStatus);
		System.out.println("");
	}

	/**
	 * A <tt>ThreadQueue</tt> that sorts threads by priority.
	 */
	protected class PriorityThreadQueue extends ThreadQueue {
		PriorityThreadQueue(boolean transferPriority) {
			this.transferPriority = transferPriority;
		}

		public void waitForAccess(KThread thread) {
			Lib.assertTrue(Machine.interrupt().disabled());
			getThreadState(thread).waitForAccess(this);
		}

		public void acquire(KThread thread) {
			Lib.assertTrue(Machine.interrupt().disabled());
			getThreadState(thread).acquire(this);
		}
		/**We need this function to remove the highest priority thread from the wait queue.
		 * once it is removed calculate its effective priority(which can depend on multiple waitqueues
		 * @return HighestPriority KThread
		 */
		public KThread nextThread() {
			// implement me
			// seems to have finished
			Lib.assertTrue(Machine.interrupt().disabled());
			ThreadState threadState = this.pickNextThread();
			if (threadState != null){
				//System.out.println(threadState.thread.toString());
				//System.out.println(threadState.age);
			}
			priorityQueue.remove(threadState);
			if (transferPriority && threadState != null) {
				this.dequeuedThread.removeQueue(this);
				threadState.waitingOn = null;
				threadState.addQueue(this);
			}
			this.dequeuedThread = threadState;
			if (threadState == null){
				this.priorityQueue = new PriorityQueue<ThreadState>();
				return null;
			}
			return threadState.thread;
		}

		/**
		 * Return the next thread that <tt>nextThread()</tt> would return,
		 * without modifying the state of this queue.
		 *
		 * @return	the next thread that <tt>nextThread()</tt> would
		 *		return.
		 */
		protected ThreadState pickNextThread() {
			// implement me
			// seems to have finished
			return this.priorityQueue.peek();
		}

		public void print() {
			Lib.assertTrue(Machine.interrupt().disabled());
			// implement me (if you want)
		}

		/**
		 * The base priority queue object.
		 */
		protected PriorityQueue<ThreadState> priorityQueue = new PriorityQueue<ThreadState>();
		/** The most recently dequeued ThreadState. */
		protected ThreadState dequeuedThread = null;
		/**
		 * <tt>true</tt> if this queue should transfer priority from waiting
		 * threads to the owning thread.
		 */
		public boolean transferPriority;
	}

	/**
	 * The scheduling state of a thread. This should include the thread's
	 * priority, its effective priority, any objects it owns, and the queue
	 * it's waiting for, if any.
	 *
	 * @see	nachos.threads.KThread#schedulingState
	 */
	protected class ThreadState implements Comparable<ThreadState> {
		/**
		 * Allocate a new <tt>ThreadState</tt> object and associate it with the
		 * specified thread.
		 *
		 * @param thread the thread this state belongs to.
		 */
		public ThreadState(KThread thread) {
			this.thread = thread;
			//initialize the onQueue linkedlist
			this.waitedBy = new LinkedList<PriorityThreadQueue>();
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
			if (waitedBy.size() != 0){
				//System.out.println(this.thread+", EffectPriority="+this.getEffectivePriority());
				int size = waitedBy.size();
				//System.out.println(size);
				for(int i = 0; i < size; i++){
					PriorityThreadQueue current = waitedBy.get(i);

					/*
					PriorityQueue<ThreadState> priorityQueue = new PriorityQueue<ThreadState>(current.priorityQueue);
					System.out.println("---printing a heap");
					System.out.println(priorityQueue.poll());
					System.out.println(priorityQueue.poll());
					*/

					ThreadState donator = current.pickNextThread();
					if (donator != null){
						// System.out.println(donator.thread+", EffectPriority="+donator.getEffectivePriority());
						if ((donator.getEffectivePriority() > maxEffectivePriority) && current.transferPriority)
							maxEffectivePriority = donator.getEffectivePriority();
					}
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
			this.waitingOn.priorityQueue.remove(this);
			this.priority = priority;
			this.waitingOn.priorityQueue.add(this);

			this.updateEffectivePriority();
			if(this.waitingOn != null && this.waitingOn.dequeuedThread != null)
				this.waitingOn.dequeuedThread.updateEffectivePriority();
			//this.waiting.dequeuedThread.calcEffectivePriority();
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
		public void waitForAccess(PriorityThreadQueue waitQueue) {
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
		public void acquire(PriorityThreadQueue waitQueue) {
			//Seems good, checks to see if queue is empty, if it is just make it dequeued thread.
			//needs to add waitQueue
			Lib.assertTrue(Machine.interrupt().disabled());
			Lib.assertTrue(waitQueue.priorityQueue.isEmpty());
			waitQueue.dequeuedThread = this;
			this.addQueue(waitQueue);
			this.updateEffectivePriority();
		}

		public int compareTo(ThreadState threadState){
			//changed first if from > to <
			if (threadState == null)
				return 1;
			if (this.getEffectivePriority() < threadState.getEffectivePriority()){
				return -1;
			}else{ if (this.getEffectivePriority() > threadState.getEffectivePriority()){
				return 1;
			}else{
				if (this.age >= threadState.age)
					return -1;
				else{ return 1; }
			}
			}
		}

		public void removeQueue(PriorityThreadQueue queue){
			waitedBy.remove(queue);
			this.updateEffectivePriority();
		}
		public void addQueue(PriorityThreadQueue queue){
			waitedBy.add(queue);
			this.updateEffectivePriority();
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
		protected LinkedList<PriorityThreadQueue> waitedBy;
		protected int effectivePriority;
		protected PriorityThreadQueue waitingOn;
	}
}
