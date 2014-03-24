//{intytsdovtxtdxhxhoid{idofx`ofridofit`ofkh`bido
//PART OF THE NACHOS. DON'T CHANGE CODE OF THIS LINE
package nachos.threads;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Stack;

import nachos.machine.*;

/**
 * An implementation of condition variables that disables interrupt()s for
 * synchronization.
 *
 * <p>
 * You must implement this.
 *
 * @see	nachos.threads.Condition
 */
public class Condition2 {
    /**
     * Allocate a new condition variable.
     *
     * @param	conditionLock	the lock associated with this condition
     *				variable. The current thread must hold this
     *				lock whenever it uses <tt>sleep()</tt>,
     *				<tt>wake()</tt>, or <tt>wakeAll()</tt>.
     */
    public Condition2(Lock conditionLock) {
        this.conditionLock = conditionLock;
    }
    
    /**
     * Atomically release the associated lock and go to sleep on this condition
     * variable until another thread wakes it using <tt>wake()</tt>. The
     * current thread must hold the associated lock. The thread will
     * automatically reacquire the lock before <tt>sleep()</tt> returns.
     */
    public void sleep() {
        Lib.assertTrue(conditionLock.isHeldByCurrentThread());
        
        boolean intStatus = Machine.interrupt().disable();
        
        conditionLock.release();
        waitQueue.waitForAccess(KThread.currentThread());
        KThread.sleep();
        conditionLock.acquire();
        
        Machine.interrupt().restore(intStatus);
    }
    
    /**
     * Wake up at most one thread sleeping on this condition variable. The
     * current thread must hold the associated lock.
     */
    public void wake() {
        Lib.assertTrue(conditionLock.isHeldByCurrentThread());
        
        boolean intStatus = Machine.interrupt().disable();
        
        KThread thread = waitQueue.nextThread();
        if (thread != null) {
            thread.ready();
        }
        
        Machine.interrupt().restore(intStatus);
    }
    
    /**
     * Wake up all threads sleeping on this condition variable. The current
     * thread must hold the associated lock.
     */
    public void wakeAll() {
        Lib.assertTrue(conditionLock.isHeldByCurrentThread());
        
        boolean intStatus = Machine.interrupt().disable();
        
        KThread thread = waitQueue.nextThread();
        while (thread != null) {
            thread.ready();
            thread = waitQueue.nextThread();
        }
        
        Machine.interrupt().restore(intStatus);
    }
    
    private static class Producer implements Runnable {
    Producer(int which, int numProducts) {
	    this.which = which;
	    this.numProducts = numProducts;
    }
	Producer(int which) {
	    this.which = which;
	}

	public void run() {
		//enqueue items
		for (int i = 0; i < numProducts; i++) {
			lock.acquire();
			System.out.println("*** producer " + which + " adds item. ");
			sharedata.add(which);
			dataready.wake();
			lock.release();
		}
	}
	
	private int numProducts = 1;
	private int which;
    }

    private static class Consumer implements Runnable {
	Consumer(int which) {
	    this.which = which;
	}

	public void run() {
		//dequeue items
		lock.acquire();
		System.out.println("*** consumer " + which + " enters. ");

		while (sharedata.isEmpty()) {
			System.out.println("*** consumer " + which + " sleeps. ");
			dataready.sleep();
		}
		
		System.out.println("*** consumer " + which + " gets " + sharedata.pop()
				+ "'s item. ");
		lock.release();
	}
	
	private int which;
    }
    
    private static class ProducerConsumerTest {
    	public ProducerConsumerTest(int numProducer, int numConsumer, 
    			int productsPerProducer) {
    		//Initialize producers and consumers

    		for (int i = 0; i < numProducer; i++) {
    			producers.push(new Producer(i, productsPerProducer));
    			order.push(1);
    		}

    		for (int i = numProducer; i < numProducer + numConsumer; i++) {
    			consumers.push(new Consumer(i));
    			order.push(2);
    		}

    		Collections.shuffle(order);
    		
    		Stack<KThread> joinStack = new Stack<KThread>();
    		
    		for (int i = 0; i < numProducer + numConsumer; i++) {
    			int currentNumber = order.pop();
    			if (currentNumber == 1) {
    				joinStack.push(new KThread(producers.pop()));
    				joinStack.peek().fork();
    			}
    			else if (currentNumber == 2) {
    				joinStack.push(new KThread(consumers.pop()));
    				joinStack.peek().fork();
    			}
    		}
    		
    		while (!joinStack.isEmpty()) {
    			joinStack.pop().join();
    		}
    	}
    	public ProducerConsumerTest(int numProducer, int numConsumer) {
    		//set by default that each producer enqueues only 1 product
    		this(numProducer, numConsumer, 1);
    	}
    	
		private LinkedList<Integer> order = new LinkedList<Integer>(); 
    	private LinkedList<Producer> producers = new LinkedList<Producer>();
    	private LinkedList<Consumer> consumers = new LinkedList<Consumer>();
    	private LinkedList<Integer> sharedata = new LinkedList<Integer>();
    }

    
    public static void selfTest() {
    	//Case 1: 1 producer, 1 consumer
    	System.out.println("Case 1:");
    	new ProducerConsumerTest(1, 1);
    	
    	//Case 2: 1 producer, several consumers
    	System.out.println("Case 2:");
    	new ProducerConsumerTest(1, 4, 4);
    	
    	//Case 3: 3 producer3, 3 consumer3
    	System.out.println("Case 3:");
    	new ProducerConsumerTest(3, 3);
    }

	private static Lock lock = new Lock();
	private static Condition2 dataready = new Condition2(lock);
	private static LinkedList<Integer> sharedata = new LinkedList<Integer>(); 
    
    
    private Lock conditionLock;
    
    private ThreadQueue waitQueue =
	ThreadedKernel.scheduler.newThreadQueue(false);
}
