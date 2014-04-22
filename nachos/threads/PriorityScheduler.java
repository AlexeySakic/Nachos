//{intytsdovtxtdxhxhoid{idofx`ofridofit`ofkh`bido
//PART OF THE NACHOS. DON'T CHANGE CODE OF THIS LINE
package nachos.threads;

import nachos.machine.*;

import java.util.LinkedList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.PriorityQueue;

import nachos.threads.Constants;
import nachos.threads.BaseThreadState;

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
public class PriorityScheduler extends Scheduler implements Constants{
	/**
	 * The maximum priority that a thread can have. Do not change this value.
	 */
	public static final int priorityMaximum = 7;
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
	 * Return the scheduling state of the specified thread.
	 *
	 * @param	thread	the thread whose scheduling state to return.
	 * @return	the scheduling state of the specified thread.
	 */
	protected BaseThreadState getThreadState(KThread thread) {
		if (thread.schedulingState == null)
			thread.schedulingState = new BaseThreadState(thread);

		return (BaseThreadState) thread.schedulingState;
	}

	protected static void printThreadInfo(PriorityScheduler s, KThread thread){
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
		ThreadQueue queue4 = s.newThreadQueue(true);

		/**
		 * Prepare for test
		 */
		KThread thread1 = new KThread();
		KThread thread2 = new KThread();
		KThread thread3 = new KThread();
		KThread thread4 = new KThread();
		KThread thread5 = new KThread();
		KThread thread_temp;
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
		s.getThreadState(thread4).setPriority(4);
		System.out.println("Thread 4 set priority to 4");
		System.out.println("[r1](t1)<-(t2,t5)");
		System.out.println("[r2](t1)");
		/*
		queue2.waitForAccess(thread4);
		System.out.println("Thread 4 waits for resource 2");
		*/
		queue3.acquire(thread5);
		System.out.println("Thread 5 acquires resource 3");
		queue3.waitForAccess(thread3);
		System.out.println("Thread 3 waits for resource 3");
		printThreadInfo(s, thread1);
		printThreadInfo(s, thread2);
		printThreadInfo(s, thread3);
		printThreadInfo(s, thread4);
		printThreadInfo(s, thread5);
		System.out.println("[r1](t1)<-(t2,t5)");
		System.out.println("[r2](t1)");
		System.out.println("[r3](t5)<-(t3)");

		/**
		 * Test for case 2
		 */
		System.out.println("\nCase 2:");
		s.getThreadState(thread2).setPriority(5);
		System.out.println("Thread 2 set priority to 5");
		printThreadInfo(s, thread1);
		printThreadInfo(s, thread2);
		printThreadInfo(s, thread3);
		printThreadInfo(s, thread4);
		printThreadInfo(s, thread5);
		System.out.println("[r1](t1)<-(t2,t5)");
		System.out.println("[r2](t1)");
		System.out.println("[r3](t5)<-(t3)");

		/**
		 * Test for case 3
		 */
		System.out.println("\nCase 3:");
		queue2.waitForAccess(thread4);
		System.out.println("Thread 4 waits for resource 2");
		s.getThreadState(thread3).setPriority(6);
		System.out.println("Thread 3 set priority to 6");

		printThreadInfo(s, thread1);
		printThreadInfo(s, thread2);
		printThreadInfo(s, thread3);
		printThreadInfo(s, thread4);
		printThreadInfo(s, thread5);
		System.out.println("[r1](t1)<-(t2,t5)");
		System.out.println("[r2](t1)<-(t4)");
		System.out.println("[r3](t5)<-(t3)");

		/**
		 * Test for case 4
		 */
		/*
		System.out.println("\nCase 4:");
		s.getThreadState(thread3).setPriority(5);
		System.out.println("Thread 3 set priority to 3");

		printThreadInfo(s, thread1);
		printThreadInfo(s, thread2);
		printThreadInfo(s, thread3);
		printThreadInfo(s, thread4);
		printThreadInfo(s, thread5);
		System.out.println("[r1](t1)<-(t2,t5)");
		System.out.println("[r2](t1)<-(t4)");
		System.out.println("[r3](t5)<-(t3)");
		*/

		/**
		 * Test for case 5
		 */
		System.out.println("\nCase 5:");
		thread_temp = queue1.nextThread();
		System.out.println("Thread 1 release resource 1");
		printThreadInfo(s, thread_temp);
		System.out.println("-------------");

		printThreadInfo(s, thread1);
		printThreadInfo(s, thread2);
		printThreadInfo(s, thread3);
		printThreadInfo(s, thread4);
		printThreadInfo(s, thread5);
		System.out.println("[r1]()<-(t2,t5)");
		System.out.println("[r2](t1)<-(t4)");
		System.out.println("[r3](t5)<-(t3)");

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
		System.out.println("[r1]()<-(t2,t5)");
		System.out.println("[r2](t1)<-(t4)");
		System.out.println("[r3](t5)<-(t3)");

		Machine.interrupt().restore(intStatus);
		System.out.println("");
	}

	/**
	 * A <tt>ThreadQueue</tt> that sorts threads by priority.
	 */
	protected class PriorityThreadQueue extends BasePriorityThreadQueue {
		PriorityThreadQueue(boolean transferPriority) {
			super(transferPriority);
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
			BaseThreadState threadState = this.pickNextThread();
			if (threadState != null){
				//System.out.println(threadState.thread.toString());
				//System.out.println(threadState.age);
			}
			priorityQueue.remove(threadState);
			if (transferPriority && threadState != null) {
				if (dequeuedThread != null)
					this.dequeuedThread.removeQueue(this);
				threadState.waitingOn = null;
				threadState.addQueue(this);
			}
			this.dequeuedThread = threadState;
			if (threadState == null){
				this.priorityQueue = new PriorityQueue<BaseThreadState>();
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
		protected BaseThreadState pickNextThread() {
			// implement me
			// seems to have finished
			boolean intStatus = Machine.interrupt().disable();

			//ensure priorityQueue is properly ordered
			//does this take the old priorityQueue and reorder it? YES!!!
			/*
			PriorityQueue<BaseThreadState> tempQueue = new PriorityQueue<BaseThreadState>(priorityQueue);
			PriorityQueue<BaseThreadState> tempQueue1 = new PriorityQueue<BaseThreadState>();
			System.out.println("In pickNextThread");
			while (!tempQueue.isEmpty()){
				tempQueue1.add(tempQueue.peek());
				System.out.println(tempQueue.poll().toString());
			}
			System.out.println("after first printing in pickNextThread");
			while (!tempQueue1.isEmpty()){
				System.out.println(tempQueue1.poll().toString());
			}
			System.out.println("after printing in pickNextThread");
			*/
			this.priorityQueue = new PriorityQueue<BaseThreadState>(priorityQueue);

			Machine.interrupt().restore(intStatus);
			return (BaseThreadState) this.priorityQueue.peek();
		}

		public void print() {
			Lib.assertTrue(Machine.interrupt().disabled());
			// implement me (if you want)
		}

	}
}