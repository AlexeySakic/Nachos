//{intytsdovtxtdxhxhoid{idofx`ofridofit`ofkh`bido
//PART OF THE NACHOS. DON'T CHANGE CODE OF THIS LINE
package nachos.threads;

import nachos.machine.*;

import java.util.LinkedList;
import java.util.TreeSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;


/**
 * A scheduler that chooses threads using a lottery.
 *
 * <p>
 * A lottery scheduler associates a number of tickets with each thread. When a
 * thread needs to be dequeued, a random lottery is held, among all the tickets
 * of all the threads waiting to be dequeued. The thread that holds the winning
 * ticket is chosen.
 *
 * <p>
 * Note that a lottery scheduler must be able to handle a lot of tickets
 * (sometimes billions), so it is not acceptable to maintain state for every
 * ticket.
 *
 * <p>
 * A lottery scheduler must partially solve the priority inversion problem; in
 * particular, tickets must be transferred through locks, and through joins.
 * Unlike a priority scheduler, these tickets add (as opposed to just taking
 * the maximum).
 */
public class LotteryScheduler extends PriorityScheduler {
	/**
	 * Allocate a new lottery scheduler.
	 */
	public LotteryScheduler() {
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
		return new LotteryThreadQueue(transferPriority);
	}	

    public ThreadQueue newThreadQueue(boolean transferPriority, KThread thread){
    	ThreadQueue q =  new LotteryThreadQueue(transferPriority);
    	((BasePriorityThreadQueue)q).dequeuedThread = getThreadState(thread);
    	return q;
    }

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
	
	// @Override
	public void setPriority(KThread thread, int priority){
		/*
		Lib.assertTrue(Machine.interrupt().disabled());
		priority = priority >= priorityMinimum ?
				(priority <= priorityMaximum ? priority : priorityMaximum)
				: priorityMinimum; 
				*/

		this.getThreadState(thread).setPriority(priority);
	}

	public static final int priorityMinimum = 1;
	/**
	 * The maximum priority that a thread can have. Do not change this value.
	 */
	public static final int priorityMaximum = Integer.MAX_VALUE;

	protected static void printInfo(PriorityScheduler s,
			KThread [] threads, ThreadQueue [] queues){
		for(int i = 0; i < threadsNum; i++)
			printThreadInfo(s, threads[i]);
		for(int i = 0; i < queueNum; i++)
			queues[i].print();
	}

	static int queueNum = 10;
	static int threadsNum = 12;
	// static int PRINTING = 0;
	
	public static void selfTestRun(KThread t1, int p1, KThread t2, int p2, KThread t3, int p3) {
		boolean int_state = Machine.interrupt().disable();
		ThreadedKernel.scheduler.setPriority(t1, p1);
		ThreadedKernel.scheduler.setPriority(t2, p2);
		ThreadedKernel.scheduler.setPriority(t3, p3);
		Machine.interrupt().restore(int_state);
		t1.setName("Thread 1").fork();		
		t2.setName("Thread 2").fork();		
		t3.setName("Thread 3").fork();
		t1.join();
		t2.join();
		t3.join();
	}
	
//	@Override
	public static void selfTestRun(KThread t1, int p1, KThread t2, int p2, KThread t3, int p3, KThread t4, int p4) {

		boolean int_state = Machine.interrupt().disable();
		ThreadedKernel.scheduler.setPriority(t1, p1);
		ThreadedKernel.scheduler.setPriority(t2, p2);
		ThreadedKernel.scheduler.setPriority(t3, p3);
		ThreadedKernel.scheduler.setPriority(t4, p4);
		Machine.interrupt().restore(int_state);
		t1.setName("Thread 1").fork();		
		t2.setName("Thread 2").fork();		
		t3.setName("Thread 3").fork();
		t4.setName("Thread 4").fork();
		t1.join();
		t2.join();
		t3.join();
		t4.join();
	}	
	
	public static void selfTest__() {

		KThread t1, t2, t3;
		final Lock lock;
		final Condition condition;
		final LotteryScheduler l = new LotteryScheduler();
		
		
		/*
		 * Test 0: without donation and priority change
		 *
		 */

		System.out.println("Test 0");

		t1 = new KThread(new Runnable() {
			public void run() {
				System.out.println(KThread.currentThread().getName());
				for(int i = 0; i < 10; ++i) {
					System.out.println(KThread.currentThread().getName() + " working " + i);	
					KThread.yield();
				}
				System.out.println(KThread.currentThread().getName());
			}
		});
		
		t2 = new KThread(new Runnable() {
			public void run() {
				System.out.println(KThread.currentThread().getName());
				for(int i = 0; i < 10; ++i) {
					System.out.println(KThread.currentThread().getName() + " working " + i);	
					KThread.yield();
				}
				System.out.println(KThread.currentThread().getName());
			}
		});
		
		t3 = new KThread(new Runnable() {
			public void run() {
				System.out.println(KThread.currentThread().getName());
				for(int i = 0; i < 10; ++i) {
					System.out.println(KThread.currentThread().getName() + " working " + i);	
					KThread.yield();
				}
				System.out.println(KThread.currentThread().getName());
			}
		});
	
		selfTestRun(t1, 60000, t2, 500, t3, 4);

		/*
		 * Test 1: without donation and priority change
		 *
		 */

		System.out.println("Test 1");

		t1 = new KThread(new Runnable() {
				public void run() {
					System.out.println(KThread.currentThread().getName());
					for(int i = 0; i < 10; ++i) {
						System.out.println(KThread.currentThread().getName() + " working " + i);	
						KThread.yield();
					}
					System.out.println(KThread.currentThread().getName());
				}
			});
		
		t2 = new KThread(new Runnable() {
			public void run() {
				System.out.println(KThread.currentThread().getName());
				for(int i = 0; i < 10; ++i) {
					System.out.println(KThread.currentThread().getName() + " working " + i);	
					KThread.yield();
				}
				System.out.println(KThread.currentThread().getName());
			}
		});
		
		t3 = new KThread(new Runnable() {
			public void run() {
				System.out.println(KThread.currentThread().getName());
				for(int i = 0; i < 10; ++i) {
					System.out.println(KThread.currentThread().getName() + " working " + i);	
					KThread.yield();
				}
				System.out.println(KThread.currentThread().getName());
			}
		});

		selfTestRun(t1, 6, t2, 5, t3, 4);

		/*
		 * Test 2: without donation but with priority change
		 *
		 */
		
		
		System.out.println("Test 2");

		t1 = new KThread(new Runnable() {
			public void run() {
				System.out.println(KThread.currentThread().getName());
				for(int i = 0; i < 10; ++i) {
					System.out.println(KThread.currentThread().getName() + " working " + i);
					if (i == 2) {
						System.out.println(KThread.currentThread().getName() + " changes priority from 6 to 1");
						boolean int_state = Machine.interrupt().disable();
						ThreadedKernel.scheduler.setPriority(1);
						KThread a = KThread.currentThread();
						System.out.println(l.getEffectivePriority(a));
						Machine.interrupt().restore(int_state);
					}
					KThread.yield();
				}
				System.out.println(KThread.currentThread().getName());
			}
		});
	
		t2 = new KThread(new Runnable() {
			public void run() {
				System.out.println(KThread.currentThread().getName());
				for(int i = 0; i < 10; ++i) {
					System.out.println(KThread.currentThread().getName() + " working " + i);	
					if (i == 5) {
						System.out.println(KThread.currentThread().getName() + " changes priority from 5 to 7");
						boolean int_state = Machine.interrupt().disable();
						ThreadedKernel.scheduler.setPriority(7000000);
						KThread a = KThread.currentThread();
						System.out.println(l.getEffectivePriority(a));
						Machine.interrupt().restore(int_state);
					}
					KThread.yield();
				}
				System.out.println(KThread.currentThread().getName());
			}
		});
	
		t3 = new KThread(new Runnable() {
			public void run() {
				System.out.println(KThread.currentThread().getName());
				for(int i = 0; i < 10; ++i) {
					System.out.println(KThread.currentThread().getName() + " working " + i);
					KThread.yield();
				}
				System.out.println(KThread.currentThread().getName());
			}
		});

		selfTestRun(t1, 60000, t2, 500, t3, 4);

		/*
		 * Test 3: with donation and priority change
		 *
		 */
		
		System.out.println("Test 3");

		lock = new Lock();
	//	final Lock lock2 = new Lock();
		condition = new Condition( lock );
	//	final Condition condition2 = new Condition(lock2);

		final KThread t01 = new KThread(new Runnable() {
			public void run() {
		//		lock2.acquire();
	//			KThread.yield();
		//		KThread.yield();
				System.out.println(KThread.currentThread().getName());
				boolean int_state = Machine.interrupt().disable();
				ThreadedKernel.scheduler.setPriority(1);
				KThread a = KThread.currentThread();
				System.out.println(l.getEffectivePriority(a));
				Machine.interrupt().restore(int_state);
				System.out.println(KThread.currentThread().getName());
				KThread.yield();
				int_state = Machine.interrupt().disable();
				a = KThread.currentThread();
				System.out.println(l.getEffectivePriority(a));
				Machine.interrupt().restore(int_state);
				System.out.println(KThread.currentThread().getName());
				KThread.yield();
				System.out.println(KThread.currentThread().getName());
		//		lock2.release();
			}
		});

		final KThread t02 = new KThread(new Runnable() {
			public void run() {
//				KThread.yield();
//				KThread.yield();
				System.out.println(KThread.currentThread().getName());
				lock.acquire();
				boolean int_state = Machine.interrupt().disable();
				ThreadedKernel.scheduler.setPriority(2);
				KThread a = KThread.currentThread();
				System.out.println(l.getEffectivePriority(a));
				Machine.interrupt().restore(int_state);
				System.out.println(KThread.currentThread().getName()+"yield");
				KThread.yield();
			//	lock2.acquire();
				System.out.println("t01 join t01");
				t01.join();
				System.out.println(KThread.currentThread().getName());
				lock.release();
			//	lock2.release();
				System.out.println(KThread.currentThread().getName());
			}
		});

		final KThread t03 = new KThread(new Runnable() {
			public void run() {
	//			KThread.yield();
	//			KThread.yield();
				System.out.println(KThread.currentThread().getName());
				lock.acquire();
				System.out.println(KThread.currentThread().getName());
			}
		});
		
		final KThread t04 = new KThread(new Runnable() {
			public void run() {
	//			KThread.yield();
	//			KThread.yield();
				System.out.println(KThread.currentThread().getName());
				System.out.println("t01:" + l.getThreadState(t01).effectivePriority);
				System.out.println("t02:" + l.getThreadState(t02).effectivePriority);
				System.out.println("t03:" + l.getThreadState(t03).effectivePriority);
				System.out.println("t04:" + l.getThreadState(KThread.currentThread()).effectivePriority);
				for( int i = 0; i < 2; ++i ) {
					System.out.println( KThread.currentThread().getName() + " working " + i );	
					KThread.yield();
				}
				System.out.println(KThread.currentThread().getName());
			}
		});

		selfTestRun(t01, 1000000, t02, 10000, t03, 5, t04, 4);
	}
	
	public static void selfTest() {
		System.out.println("---------LotteryScheduler test---------------------");
		LotteryScheduler s = new LotteryScheduler();
		
		ThreadQueue [] queues = new ThreadQueue[queueNum];
		for(int i = 0; i < queueNum; i++){
			queues[i] = s.newThreadQueue(true);
		}

		KThread [] threads = new KThread[threadsNum];
		for(int i = 0; i < threadsNum; i++){
			threads[i] = new KThread();
			threads[i].setName("thread" + i);
		}

		KThread thread_temp;

		boolean intStatus = Machine.interrupt().disable();
		// PRINTING = 1;
		/**
		 * Test for case 1
		 */
		System.out.println("\nCase 1:");
		queues[0].acquire(threads[0]);
		System.out.println("Thread 0 acquires resource 0");
		queues[0].waitForAccess(threads[6]);
		System.out.println("Thread 6 waits for resource 0");
		queues[0].waitForAccess(threads[7]);
		System.out.println("Thread 7 waits for resource 0");

		queues[4].acquire(threads[6]);
		System.out.println("Thread 6 acquires resource 4");
		queues[4].waitForAccess(threads[8]);
		System.out.println("Thread 8 waits for resource 4");
		queues[4].waitForAccess(threads[9]);
		System.out.println("Thread 9 waits for resource 4");

		queues[5].acquire(threads[7]);
		System.out.println("Thread 7 acquires resource 5");
		queues[5].waitForAccess(threads[10]);
		System.out.println("Thread 10 waits for resource 5");
		queues[5].waitForAccess(threads[11]);
		System.out.println("Thread 11 waits for resource 5");

		queues[1].acquire(threads[1]);
		System.out.println("Thread 1 acquires resource 1");
		queues[1].waitForAccess(threads[2]);
		System.out.println("Thread 2 waits for resource 1");
		queues[1].waitForAccess(threads[5]);
		System.out.println("Thread 5 waits for resource 1");
		queues[2].acquire(threads[1]);
		System.out.println("Thread 1 acquires resource 2");
		s.getThreadState(threads[4]).setPriority(4);
		System.out.println("Thread 4 set priority to 4");

		queues[1].waitForAccess(threads[0]);
		System.out.println("Thread 0 waits for resource 1");

		/*
		queue2.waitForAccess(thread4);
		System.out.println("Thread 4 waits for resource 2");
		*/
		queues[3].acquire(threads[5]);
		System.out.println("Thread 5 acquires resource 3");
		queues[3].waitForAccess(threads[3]);
		System.out.println("Thread 3 waits for resource 3");

		queues[6].acquire(threads[8]);
		System.out.println("Thread 8 acquires resource 6");
		queues[6].waitForAccess(threads[9]);
		System.out.println("Thread 9 waits for resource 6");
		printInfo(s, threads, queues);
		// PRINTING = 0;
		/*
		System.out.println("[r0](t0)<-(t6,t7)");
		System.out.println("[r4](t6)<-(t8,t9)");
		System.out.println("[r5](t7)<-(t10,t11)");
		System.out.println("[r1](t1)<-(t2,t5,t0)");
		System.out.println("[r2](t1)");
		System.out.println("[r3](t5)<-(t3)");
		*/

		/**
		 * Test for case 2
		 */
		System.out.println("\nCase 2:");
		s.getThreadState(threads[2]).setPriority(5);
		System.out.println("Thread 2 set priority to 5");
		s.setPriority(threads[6], 5);
		System.out.println("Thread 6 set priority to 5");
		printInfo(s, threads, queues);
		/*
		System.out.println("[r0](t0)<-(t6,t7)");
		System.out.println("[r4](t6)<-(t8,t9)");
		System.out.println("[r5](t7)<-(t10,t11)");
		System.out.println("[r1](t1)<-(t2,t5)");
		System.out.println("[r2](t1)");
		System.out.println("[r3](t5)<-(t3)");
		*/

		/**
		 * Test for case 3
		 */
		System.out.println("\nCase 3:");
		queues[2].waitForAccess(threads[4]);
		System.out.println("Thread 4 waits for resource 2");
		s.getThreadState(threads[3]).setPriority(6);
		System.out.println("Thread 3 set priority to 6");

		printInfo(s, threads, queues);
		/*
		System.out.println("[r0](t0)<-(t6,t7)");
		System.out.println("[r4](t6)<-(t8,t9)");
		System.out.println("[r5](t7)<-(t10,t11)");
		System.out.println("[r1](t1)<-(t2,t5)");
		System.out.println("[r2](t1)<-(t4)");
		System.out.println("[r3](t5)<-(t3)");
		*/

		/**
		 * Test for case 4
		 */
		/*
		System.out.println("\nCase 4:");
		s.getThreadState(thread3).setPriority(5);
		System.out.println("Thread 3 set priority to 3");

		printInfo(s, threads);
		System.out.println("[r1](t1)<-(t2,t5)");
		System.out.println("[r2](t1)<-(t4)");
		System.out.println("[r3](t5)<-(t3)");
		*/

		/**
		 * Test for case 5
		 */
		System.out.println("\nCase 5:");
		thread_temp = queues[1].nextThread();
		System.out.println("Thread 1 release resource 1");
		printThreadInfo(s, thread_temp);
		System.out.println("-------------");

		printInfo(s, threads, queues);
		/*
		System.out.println("[r0](t0)<-(t6,t7)");
		System.out.println("[r4](t6)<-(t8,t9)");
		System.out.println("[r5](t7)<-(t10,t11)");
		System.out.println("[r1]()<-(t2,t5)");
		System.out.println("[r2](t1)<-(t4)");
		System.out.println("[r3](t5)<-(t3)");
		*/

		/**
		 * Test for case 6
		 */
		System.out.println("\nCase 6:");
		s.getThreadState(threads[4]).setPriority(2);
		System.out.println("Thread 4 set priority to 2");

		printInfo(s, threads, queues);
		/*
		System.out.println("[r1]()<-(t2,t5)");
		System.out.println("[r2](t1)<-(t4)");
		System.out.println("[r3](t5)<-(t3)");
		*/

		/*
		queue1.acquire(thread1);
		System.out.println("Thread 1 acquires resource 1");
		queue1.waitForAccess(thread2);
		System.out.println("Thread 2 waits for resource 1");
		queue1.waitForAccess(thread3);
		System.out.println("Thread 3 waits for resource 1");
		printInfo(s, threads);

		System.out.println("~~~~~~~~Thread4 aquires queue2 thread1 waits~~~~~~~~~`");
		queue2.acquire(thread4);
		System.out.println("Thread 4 acquires resource 2");
		queue2.waitForAccess(thread1);
		System.out.println("Thread 1 waits for resource 2");
		printInfo(s, threads);

		System.out.println("~~~~~~~~thread2 priority changed to 2~~~~~~~~~`");
		s.setPriority(thread2, 2);
		System.out.println("Thread 2 set priority to 2");
		printInfo(s, threads);

		System.out.println("~~~~~~~~thread2 priority changed to 1~~~~~~~~~`");
		s.setPriority(thread2, 1);
		System.out.println("Thread 2 set priority to 1");
		printInfo(s, threads);

		System.out.println("~~~~~~~~Thread5 waits on queue1~~~~~~~~~`");
		queue1.waitForAccess(thread5);
		System.out.println("Thread 5 waits for resource 1");

		printInfo(s, threads);

		System.out.println("~~~~~~~~thread2 priority changed to 8~~~~~~~~~`");
		s.setPriority(thread2, 8);
		System.out.println("Thread 2 set priority to 8");
		printInfo(s, threads);
		*/
		ThreadQueue newQueue;

		KThread thread00, thread10, thread20;
		int tot00 = 0, tot10 = 0, tot20 = 0;
		for (int i =0; i<7000; i++){
			newQueue = s.newThreadQueue(true);
			thread00 = new KThread();
			thread10 = new KThread();
			thread20 = new KThread();
			thread00.setName("thread00");
			thread10.setName("thread10");
			thread20.setName("thread20");
			s.setPriority(thread00, 1);
			s.setPriority(thread10, 2);
			s.setPriority(thread20, 4);
			newQueue.waitForAccess(thread00);
			newQueue.waitForAccess(thread10);
			newQueue.waitForAccess(thread20);
			/*
			printThreadInfo(s, thread00);
			printThreadInfo(s, thread10);
			printThreadInfo(s, thread20);
			*/
			KThread threadTemp = newQueue.nextThread();
			if (threadTemp == thread10)
				tot10 += 1;
			else if (threadTemp == thread20)
				tot20 += 1;	
			else
				tot00 += 1;
		}

		// System.out.printf("tot00 = %d, tot10 = %d, tot20 = %d\n", tot00, tot10, tot20);
		System.out.print("tot00 = ");
		System.out.print(tot00);
		System.out.print(", tot10 = ");
		System.out.print(tot10);
		System.out.print(", tot20 = ");
		System.out.print(tot20);
		System.out.println();
		System.out.println("~~~~~~~~~~~~~~~~~`");
		
		KThread thread30, thread40, thread50;
		tot00 = tot10 = tot20 = 0;
		ThreadQueue q1, q2, q3;
		for (int i =0; i<19000; i++){
			newQueue = s.newThreadQueue(true);
			q1 = s.newThreadQueue(true);
			q2 = s.newThreadQueue(true);
			q3 = s.newThreadQueue(true);
			thread00 = new KThread();
			thread10 = new KThread();
			thread20 = new KThread();
			thread30 = new KThread();
			thread40 = new KThread();
			thread50 = new KThread();
			s.setPriority(thread00, 1);
			s.setPriority(thread10, 2);
			s.setPriority(thread20, 4);

			s.setPriority(thread30, 4);
			s.setPriority(thread40, 4);
			s.setPriority(thread50, 4);
			q1.acquire(thread00);
			q1.waitForAccess(thread30);
			q2.acquire(thread10);
			q2.waitForAccess(thread40);
			q3.acquire(thread30);
			q3.waitForAccess(thread50);
			newQueue.waitForAccess(thread00);
			newQueue.waitForAccess(thread10);
			newQueue.waitForAccess(thread20);
			/*
			printThreadInfo(s, thread00);
			printThreadInfo(s, thread10);
			printThreadInfo(s, thread20);
			*/
			KThread threadTemp = newQueue.nextThread();
			if (threadTemp == thread10)
				tot10 += 1;
			else if (threadTemp == thread20)
				tot20 += 1;	
			else
				tot00 += 1;
		}

		// System.out.printf("tot00 = %d, tot10 = %d, tot20 = %d\n", tot00, tot10, tot20);
		System.out.print("tot00 = ");
		System.out.print(tot00);
		System.out.print(", tot10 = ");
		System.out.print(tot10);
		System.out.print(", tot20 = ");
		System.out.print(tot20);
		System.out.println();
		System.out.println("~~~~~~~~~~~~~~~~~`");	

		/*
		queue3.acquire(thread5);
		queue3.waitForAccess(thread4);
		System.out.println("thread5 EP="+s.getThreadState(thread5).getEffectivePriority());
		System.out.println("thread4 EP="+s.getThreadState(thread4).getEffectivePriority());

		queue2.nextThread();
		System.out.println("thread4 EP="+s.getThreadState(thread4).getEffectivePriority());
		System.out.println("thread1 EP="+s.getThreadState(thread1).getEffectivePriority());
		System.out.println("thread5 EP="+s.getThreadState(thread5).getEffectivePriority());
		 */
		Machine.interrupt().restore(intStatus);
		System.out.println("");
	}

	protected class LotteryThreadQueue extends BasePriorityThreadQueue{

		LotteryThreadQueue(boolean transferPriority) {
			super(transferPriority);
		}
		
		public void waitForAccess(KThread thread) {
			// System.out.println("LotteryThreadQueue.waitForAccess()");
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
		public KThread nextThread(){
			Lib.assertTrue(Machine.interrupt().disabled());

			BaseThreadState pickedThread = pickNextThread();
			if (transferPriority && pickedThread != null) {
				if (this.dequeuedThread != null) {
					this.dequeuedThread.removeQueue(this);
				}
				pickedThread.waitingOn = null;
				pickedThread.addQueue(this);
			}
			this.dequeuedThread = pickedThread;
			if (this.dequeuedThread != null){
				this.priorityQueue.remove(dequeuedThread);
				this.dequeuedThread.updateEffectivePriority();
				return this.dequeuedThread.thread;
			}
			else
				return null;
		}

		/**
		 * Return the next thread that <tt>nextThread()</tt> would return,
		 * without modifying the state of this queue.
		 *
		 * @return the next thread that <tt>nextThread()</tt> would
		 * return.
		 */
		protected BaseThreadState pickNextThread() {
			boolean intStatus = Machine.interrupt().disable();

			//ensure priorityQueue is properly ordered
			//does this take the old priorityQueue and reorder it? YES!!!
			//this.priorityQueue = new PriorityQueue<ThreadState>(priorityQueue);

			Machine.interrupt().restore(intStatus);
			//Create a counter for total number of tickets
			int totTickets = 0;
			//create a LinkedList of threads and another of the tickets associated with that thread
			LinkedList<BaseThreadState> threads = new LinkedList<BaseThreadState>();
			LinkedList<Integer> tickets = new LinkedList<Integer>();
			//Iterate through the threadStates waiting to acquire the resource
			//store their thread and effective priority
			Iterator<BaseThreadState> queue = priorityQueue.iterator();
			for (int i = 0; queue.hasNext(); i++){
				BaseThreadState current = queue.next();
				int ticketNum = current.getEffectivePriority();
				totTickets += ticketNum;
				threads.add(current);
				tickets.add(ticketNum);
				Lib.assertTrue(totTickets >= 0);
			}

			//set return thread to null for now
			BaseThreadState pickedThread = null;
			//if some threads exist go through all of the threads and if the random
			//ticket falls in the range of the thread give pickedThread to that thread
			if (totTickets > 0){
				Integer ticketChoice = generator.nextInt(totTickets);
				Integer ticketCount = ticketChoice;
				// System.out.printf("totTickets = %d, ticketChoice = %d\n", totTickets, ticketChoice);
				for(int j = 0; j < tickets.size(); j++){
					ticketCount -= tickets.get(j);
					if (ticketCount < 0){
						pickedThread = threads.get(j);
						break;
					}
				}
			}
			return pickedThread;
		}

		/**
		 * This method needs to be changed from its implementation in PriorityScheduler.
		 * This method should pick the next dequeued thread randomly from the tickets available
		 * in the waitList. For this to work the method must figure out the total number of tickets
		 * available in the queue and pick one randomly. Whichever ticket gets picked should be linked
		 * to a specific ThreadState. That ThreadState is then picked to acquire the resource, and
		 * the ThreadState that previously held the resource is removed. Each ThreadState that has 
		 * been changed must have its effectivePriority recalculated. 
		 * Note:Don't need pickNextThread() anymore.
		 * 
		 * @return KThread associated with a randomly chosen ticket
		 */


		protected Random generator = new Random();
	}

	
	protected class ThreadState extends BaseThreadState{
		/**
		 * We need to change this method from its implementation in PriorityScheduler because
		 * now instead of a max of all priorities, effectivePriority equals the sum of all the
		 * threads' priorities waiting on a resource. Since each ThreadState holds a LinkedList
		 * of all the ThreadQueues it currently holds the resource for, calculating the effective
		 * priority should be simple. Each thread's effectivePriority is calculated by the sum of
		 * its own priority and the effectivePriority of each thread that is waiting on the 
		 * resource held by this thread. Once the new value is calculated, calcEffectivePriority
		 * must be called on the thread that currently holds the resource for which this thread 
		 * is waiting on (if one exists). This will recursively compute the new effective priority
		 * for all threads that could be changed.
		 * @return total number of tickets this Thread has available to it.
		 */
		/**
		 * Allocate a new <tt>ThreadState</tt> object and associate it with the
		 * specified thread.
		 *
		 * @param thread the thread this state belongs to.
		 */
		public ThreadState(KThread thread) {
			super(thread);
			waitedBySet.clear();
		}

		/**
		 * Calculate the Effective Priority of a thread and the thread that currently holds the resource
		 * it is waiting on.
		 */
		@Override
		public void updateEffectivePriority() {
			int totEP = 0;
			waitedBySet.clear();
			waitedBySet.add(this);
			for(int i = 0; i < waitedBy.size(); i++){
				LotteryThreadQueue current = (LotteryThreadQueue) waitedBy.get(i);
				if (!current.transferPriority)
					continue;
				Iterator<BaseThreadState> threadIT = current.priorityQueue.iterator();
				while(threadIT.hasNext()){
					ThreadState currentThread = (ThreadState) threadIT.next();
					waitedBySet.add(currentThread);
					waitedBySet.addAll(currentThread.waitedBySet);
					// outsideEP += currentThread.getEffectivePriority();
				}
			}
			LotteryThreadQueue queue = (LotteryThreadQueue) thread.joinQueue;
			if (queue != null)
				if (queue.transferPriority){
					Iterator<BaseThreadState> threadIT = queue.priorityQueue.iterator();
					while(threadIT.hasNext()){
						ThreadState currentThread = (ThreadState) threadIT.next();
						waitedBySet.add(currentThread);
						waitedBySet.addAll(currentThread.waitedBySet);
					}
				}
			/*
			if (PRINTING > 0 && this.thread.getName().equals("thread6")){
				System.out.println("---------------------");
				System.out.println(this.toString());
				for(ThreadState currentThread:waitedBySet){
					System.out.println(currentThread.toString());
				}
			}
			*/
			for(ThreadState currentThread:waitedBySet){
				totEP += currentThread.priority;
			}
			effectivePriority = totEP;
			Lib.assertTrue( effectivePriority > 0,
					"new effectivePriority should be positive");
			//now that my own effectivePriority Changes I have to recalculate the threads which i am waiting on
			if (waitingOn != null && waitingOn.dequeuedThread != null){
				//System.out.println(totEP - initialEffective);
// 				((ThreadState) waitingOn.dequeuedThread).addToAllEffective(effectivePriority - initialEffective);
				waitingOn.dequeuedThread.updateEffectivePriority();
			}
		}


		/**
		 * Set the priority of the associated thread to the specified value.
		 *
		 * @param priority the new priority.
		 */
		@Override
		public void setPriority(int priority) {
			this.updatePriority(priority);
			this.updateEffectivePriority();
		}

		public void addToAllEffective(int diff){
			Lib.assertTrue(effectivePriority + diff > 0,
					"new effectivePriority should be positive");
			effectivePriority += diff;
			if (waitingOn != null && waitingOn.dequeuedThread != null){
				((ThreadState) waitingOn.dequeuedThread).addToAllEffective(diff);
			}
		}

		/**
		 * Called when <tt>waitForAccess(thread)</tt> (where <tt>thread</tt> is
		 * the associated thread) is invoked on the specified priority queue.
		 * The associated thread is therefore waiting for access to the
		 * resource guarded by <tt>waitQueue</tt>. This method is only called
		 * if the associated thread cannot immediately obtain access.
		 *
		 * @param waitQueue the queue that the associated thread is
		 * now waiting on.
		 *
		 * @see nachos.threads.ThreadQueue#waitForAccess
		 */
		@Override
		public void waitForAccess(BasePriorityThreadQueue waitQueue) {
			// System.out.println("in LotteryScheduler.java, ThreadState.waitForAccess");
			Lib.assertTrue(Machine.interrupt().disabled());
			long time = Machine.timer().getTime();
			this.age = time;
			waitQueue.priorityQueue.add(this);
			if (waitQueue.transferPriority){
				this.waitingOn = waitQueue;
			}
			this.updateEffectivePriority();
			if (waitQueue.dequeuedThread != null) {
				// System.out.println("!!!!!------------Updating effectivePriority in thread.waitForAccess(queue)");
				waitQueue.dequeuedThread.updateEffectivePriority();
			}
		}

		/**
		 * Called when the associated thread has acquired access to whatever is
		 * guarded by <tt>waitQueue</tt>. This can occur either as a result of
		 * <tt>acquire(thread)</tt> being invoked on <tt>waitQueue</tt> (where
		 * <tt>thread</tt> is the associated thread), or as a result of
		 * <tt>nextThread()</tt> being invoked on <tt>waitQueue</tt>.
		 *
		 * @see nachos.threads.ThreadQueue#acquire
		 * @see nachos.threads.ThreadQueue#nextThread
		 */
		public void acquire(BasePriorityThreadQueue waitQueue) {
			Lib.assertTrue(Machine.interrupt().disabled());
			Lib.assertTrue(waitQueue.priorityQueue.isEmpty());
			waitQueue.dequeuedThread = this;
			if (waitQueue.transferPriority) {
				this.addQueue(waitQueue);
			}
			this.updateEffectivePriority();
		}

		Set<ThreadState> waitedBySet = new HashSet<ThreadState>();
		protected boolean dirty;
	}
}