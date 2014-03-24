//{intytsdovtxtdxhxhoid{idofx`ofridofit`ofkh`bido
//PART OF THE NACHOS. DON'T CHANGE CODE OF THIS LINE
package nachos.threads;

import java.util.*;

import nachos.machine.*;

/**
 * Uses the hardware timer to provide preemption, and to allow threads to sleep
 * until a certain time.
 */
public class Alarm {
    /**
     * Allocate a new Alarm. Set the machine's timer interrupt handler to this
     * alarm's callback.
     *
     * <p><b>Note</b>: Nachos will not function correctly with more than one
     * alarm.
     */
    public Alarm() {
	Machine.timer().setInterruptHandler(new Runnable() {
		public void run() { timerInterrupt(); }
	    });
    }

    /**
     * The timer interrupt handler. This is called by the machine's timer
     * periodically (approximately every 500 clock ticks). Causes the current
     * thread to yield, forcing a context switch if there is another thread
     * that should be run.
     */
    public void timerInterrupt() {
    Machine.interrupt().disable();
   	while (!queue.isEmpty() &&
   			(queue.peek().wakeTime < Machine.timer().getTime())) {
   		queue.poll().thread.ready();
   	}
   	Machine.interrupt().enable();

   	KThread.yield();
//	KThread.currentThread().yield();
    }

    /**
     * Put the current thread to sleep for at least <i>x</i> ticks,
     * waking it up in the timer interrupt handler. The thread must be
     * woken up (placed in the scheduler ready set) during the first timer
     * interrupt where
     *
     * <p><blockquote>
     * (current time) >= (WaitUntil called time)+(x)
     * </blockquote>
     *
     * @param	x	the minimum number of clock ticks to wait.
     *
     * @see	nachos.machine.Timer#getTime()
     */
    public void waitUntil(long x) {
    Machine.interrupt().disable();
	long wakeTime = Machine.timer().getTime() + x;
	queue.offer(new AlarmThread(KThread.currentThread(), wakeTime));
	KThread.sleep();
	Machine.interrupt().enable();
    }
    
    private class AlarmThread implements Comparable {
    	AlarmThread(KThread thread, long wakeTime) {
    		this.thread = thread;
    		this.wakeTime = wakeTime; 
    	}
		@Override
		public int compareTo(Object o) {
			if (this.wakeTime < ((AlarmThread)o).wakeTime)
				return -1;
			else if (this.wakeTime > ((AlarmThread)o).wakeTime)
				return 1;
			else
				return 0;
		}
		
    	KThread thread;
    	long wakeTime;
    }
    
    private static class sleepThread implements Runnable {
    	sleepThread(int which, long x) {
    		this.which = which;
    		this.x = x;
    	}
		@Override
		public void run() {
    		long startTime = Machine.timer().getTime();
			System.out.println("*** thread " + which + " sleep for "
				   + x + " ticks\n\tat time " + startTime);
			ThreadedKernel.alarm.waitUntil(x);
			long currentTime = Machine.timer().getTime();
			System.out.println("*** thread " + which + " wakes after "
			+ (currentTime - startTime)
				   + " ticks\n\tat time " + currentTime);
		}
		int which; 
    	long x;
    }
    
    public static void sleepTest(int numThread, long waitTime) {
    	KThread thread = new KThread(new sleepThread(0, 0));
    	thread.fork();
    	thread.join();
    }
    
    public static void selfTest() {
    //Case 1
    	System.out.println("Case 1:");
    	KThread thread = new KThread(new sleepThread(0, 0));
    	thread.fork();
    	thread.join();
    	
    //Case 2
    	System.out.println("Case 2:");
    	thread = new KThread(new sleepThread(1, 100));
    	thread.fork();
    	thread.join();
    	
    //Case 3
    	System.out.println("Case 3:");
    	thread = new KThread(new sleepThread(2, 600));
    	thread.fork();
    	thread.join();
    	
    //Case 4
    	System.out.println("Case 4:");
    	LinkedList<KThread> threadList = new LinkedList<KThread>();
		
    	Random rand = new Random();
    	
		for (int i = 0; i < 5; i++) {
			threadList.add(new KThread(new sleepThread(i + 3, rand.nextInt(400) + 300)));
			threadList.get(i).fork();
		}
		for (int i = 0; i < 5; i++) {
			threadList.poll().join();
		}
    }
    
    PriorityQueue<AlarmThread> queue = new PriorityQueue<AlarmThread>();
}
