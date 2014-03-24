//{intytsdovtxtdxhxhoid{idofx`ofridofit`ofkh`bido
//PART OF THE NACHOS. DON'T CHANGE CODE OF THIS LINE
package nachos.threads;

import nachos.machine.*;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Comparator;
import java.util.TreeSet;
import java.util.HashSet;
import java.util.Iterator;

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
	return new PriorityQueue(transferPriority);
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

	Lib.assertTrue(priority >= priorityMinimum &&
		   priority <= priorityMaximum);

	getThreadState(thread).setPriority(priority);
    }

    public boolean increasePriority() {
	boolean intStatus = Machine.interrupt().disable();

	KThread thread = KThread.currentThread();

	int priority = getPriority(thread);
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

    /**
     * A <tt>ThreadQueue</tt> that sorts threads by priority.
     */
    protected class PriorityQueue extends ThreadQueue {
    	
    	PriorityQueue(boolean transferPriority) {
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

    	public KThread nextThread() {
    		Lib.assertTrue(Machine.interrupt().disabled());
    		// implement me (have implemented)
    		ThreadState nextThread = pickNextThread();
    		if (nextThread != null){
    			nextThread.acquire(this);
    			return nextThread.thread;
    		}
    		return null;
    	}

    	/**
    	 * Return the next thread that <tt>nextThread()</tt> would return,
    	 * without modifying the state of this queue.
    	 *
    	 * @return	the next thread that <tt>nextThread()</tt> would
    	 *		return.
    	 */
    	protected ThreadState pickNextThread() {
    		// implement me (have implemented)
    		while(!queue.isEmpty()){
    			Integer lastKey = queue.lastKey();
    			Queue<ThreadState> qlist = queue.get(lastKey);
    			
    			if (qlist == null || qlist.isEmpty()){
    				queue.remove(lastKey);
    			}
    			else {
    				// here we pick the first-come-in thread
    				return qlist.peek();
    			}
    		}
    		return null;
    	}

    	public void print() {
    		Lib.assertTrue(Machine.interrupt().disabled());
    		// implement me (if you want)
    	}

    	/**
    	 * <tt>true</tt> if this queue should transfer priority from waiting
    	 * threads to the owning thread.
    	 */
    	public boolean transferPriority;
    	public ThreadState currentOwner = null;
    	/**
    	 * each entry of TreeMap stores a queue of threads 
    	 * with same original priority
    	 */
    	public TreeMap<Integer, Queue<ThreadState>> queue = new TreeMap<Integer, Queue<ThreadState>>();
    }

    /**
     * The scheduling state of a thread. This should include the thread's
     * priority, its effective priority, any objects it owns, and the queue
     * it's waiting for, if any.
     *
     * @see	nachos.threads.KThread#schedulingState
     */
    protected class ThreadState {
	/**
	 * Allocate a new <tt>ThreadState</tt> object and associate it with the
	 * specified thread.
	 *
	 * @param	thread	the thread this state belongs to.
	 */
	public ThreadState(KThread thread) {
	    this.thread = thread;

	    setPriority(priorityDefault);
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
	 * Return the effective priority of the associated thread.
	 *
	 * @return	the effective priority of the associated thread.
	 */
	public int getEffectivePriority() {
		//implement me (have implemented)
	    return priorityCache.last().priority;
	}

	/**
	 * Set the priority of the associated thread to the specified value.
	 *
	 * @param	priority	the new priority.
	 */
	public void setPriority(int priority) {
	    if (this.priority == priority)
	    	return;
	    
	    revokeDonation(null); //remove donations whose PriorityQueue is null
	    registerDonation(priority, null); //set priority by fake registDonation with a null PriorityQueue
	    
	    //this.priority = priority;

	    // implement me (have implemented)
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
	public void waitForAccess(PriorityQueue waitQueue) {
	    // implement me (have implemented)
		assert(waitQueue != null);
		waiting = waitQueue;
		requeue(waiting); //insert itself into waiting
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
	public void acquire(PriorityQueue waitQueue) {
	    // implement me (have implemented)
		assert(waitQueue != null);
		
		dequeue(waitQueue); // Make sure this thread is completely removed from the queue
		
		//If there was a previous owner , perform handover
		if(waitQueue.currentOwner != null && waitQueue.transferPriority) {
			waitQueue.currentOwner.revokeDonation(waitQueue);
			//waitQueue.currentOwner = null;
		}
		
		waitQueue.currentOwner = this; //update currentOwner
		
		//Check if we are waiting on this resource, if so, stop waiting
		if(waiting == waitQueue){
			waiting = null;
		}
		
		//perform donation
		if(waitQueue.transferPriority){
			registerDonation(waitQueue);
		}	
	}
	
	/**
	 * remove this thread from PriorityQueue waitQueue
	 * if a level of priority is cleaned up, then we call notifyQueueOwner(waitQueue)
	 * to update currentOwner's priority
	 * @param waitQueue
	 */
	protected void dequeue(PriorityQueue waitQueue){
		//Check if we really need to do anything
		if(waitQueue == null)
			return;
		
		//Where should this thread have been in the queue?
		int effectivePriority = getEffectivePriority();
		
		if(waitQueue.queue.containsKey(effectivePriority)){
			Queue<ThreadState> qlist = waitQueue.queue.get(effectivePriority);
			qlist.remove(this);
			
			//If it is empty, clean qlist up
			if(qlist.isEmpty()){
				waitQueue.queue.remove(effectivePriority);
				//the currentOwner no longer has any threads of this priority
				notifyQueueOwner(waitQueue);
			}
		}
	}
	
	/**
	 * thread reinsert itself into a PriorityQueue 
	 * @param waitQueue
	 */
	protected void requeue(PriorityQueue waitQueue){
		//Do we really need to do anything?
		if(waitQueue == null)
			return;
		
		//Check if we need to update the order 
		int effectivePriority = getEffectivePriority();
		
		for(Map.Entry<Integer, Queue<ThreadState>> e: waitQueue.queue.entrySet()){
			if(e.getValue().contains(this)){
				if(e.getKey().equals(effectivePriority)){
					//the order is correct, just return
					return;
				}
				else{
					//the order is wrong, prepare to reinsert
					e.getValue().remove(this);
					break;
				}
			}
		}
		
		Queue<ThreadState> qlist = null;
		if(waitQueue.queue.containsKey(effectivePriority)){
			qlist = waitQueue.queue.get(effectivePriority); //Priority level already exists in the queue
		}
		else{
			//Priority Level does not exist in the queue yet
			qlist = new LinkedList<ThreadState>();
			waitQueue.queue.put(effectivePriority, qlist);
		}
		
		qlist.add(this);
		
		//notify the currentOwner to update its priority
		notifyQueueOwner(waitQueue);
	}
	
	protected void requeue(){
		requeue(waiting);
	}
	
	protected void notifyQueueOwner(PriorityQueue waitQueue){
		if (waitQueue.currentOwner != null && waitQueue.transferPriority){
			ThreadState currentOwner = waitQueue.currentOwner;
			currentOwner.revokeDonation(waitQueue);
			currentOwner.registerDonation(waitQueue);
		}
	}
	
	protected int getLeadPriority(PriorityQueue queue){			
		//Figure out the leading priority in this queue
		if(queue != null)
		while(!queue.queue.isEmpty()){
			int qleader = queue.queue.lastKey();
			Queue<ThreadState> list = queue.queue.get(qleader);
			if(list.isEmpty()){
				//Clean up that bucket
				queue.queue.remove(qleader);
			}else{
				//Found the lead:
				return qleader;
			}
		}
		return priorityMinimum;
	}
	
	protected void registerDonation(int lead, PriorityQueue queue){
		PriorityDonation donation = new PriorityDonation(lead, queue);
		priorityCache.add(donation);
		requeue(); //reinsert itself into proper position after receiving donation
	}
	
	protected void registerDonation(PriorityQueue queue){
		registerDonation(getLeadPriority(queue), queue);
	}
	
	protected void revokeDonation(PriorityQueue queue){
		for(PriorityDonation d: priorityCache){
			if(d.queue == queue){
				priorityCache.remove(d);
			}
			requeue();
			return;
		}
	}
	
	
	//PriorityDonation simply extracts the max priority in PriorityQueue queue
	protected class PriorityDonation {
		
		PriorityDonation(int priority, PriorityQueue queue) {
    		this.priority = priority;
    		this.queue = queue;
    	}
		
		public int priority;
		public PriorityQueue queue;
	}
	
	protected class CompareDonation implements Comparator{
		public int compare(Object o1, Object o2){
			PriorityDonation d1 = (PriorityDonation)o1;
			PriorityDonation d2 = (PriorityDonation)o2;
			
			if(d1.priority < d2.priority){
				return -1;
			}else if(d1.priority == d2.priority){
				return 0;
			}else{
				return 1;
			}
		}
	}
	
	/* 
	 * priorityCache stores all the queues(each is a PriorityDonation) this thread owns
	 * waiting stores the queue which this thread is waiting on 
	 */
	protected TreeSet<PriorityDonation> priorityCache = new TreeSet<PriorityDonation>(new CompareDonation());
	protected PriorityQueue waiting = null;
	
	/** The thread with which this object is associated. */
	protected KThread thread;
	/** The priority of the associated thread. */
	protected int priority;
    }
}
