//{intytsdovtxtdxhxhoid{idofx`ofridofit`ofkh`bido
//PART OF THE NACHOS. DON'T CHANGE CODE OF THIS LINE
package nachos.threads;

import nachos.machine.*;

/**
 * A <i>communicator</i> allows threads to synchronously exchange 32-bit
 * messages. Multiple threads can be waiting to <i>speak</i>,
 * and multiple threads can be waiting to <i>listen</i>. But there should never
 * be a time when both a speaker and a listener are waiting, because the two
 * threads can be paired off at this point.
 */
public class Communicator {
    /**
     * Allocate a new communicator.
     */
    public Communicator() {
    	lock = new Lock();
    	SpeakerQueue = new Condition(lock);
    	ListenerQueue = new Condition(lock);
    	noActiveSpeaker = true;
    	noActiveListener = true;
    }

    /**
     * Wait for a thread to listen through this communicator, and then transfer
     * <i>word</i> to the listener.
     *
     * <p>
     * Does not return until this thread is paired up with a listening thread.
     * Exactly one listener should receive <i>word</i>.
     *
     * @param	word	the integer to transfer.
     */
    public void speak(int word) {
    	lock.acquire();
    	while(!noActiveSpeaker) {
    		SpeakerQueue.sleep();
    	}
    	noActiveSpeaker = false;
    	this.word = word;
    	if(!noActiveListener) {
        	ListenerQueue.wakeAll();
    	} else {
    		SpeakerQueue.sleep();
    		noActiveSpeaker = true;
    		noActiveListener = true;
        	SpeakerQueue.wake();
        	ListenerQueue.wake();
    	}
    	lock.release();
    }

    /**
     * Wait for a thread to speak through this communicator, and then return
     * the <i>word</i> that thread passed to <tt>speak()</tt>.
     *
     * @return	the integer transferred.
     */
    public int listen() {
    	int result;
    	lock.acquire();
    	while(!noActiveListener) {
    		ListenerQueue.sleep();
    	}
    	noActiveListener = false;
    	if(!noActiveSpeaker) {
    		SpeakerQueue.wakeAll();
    	} else {
    		ListenerQueue.sleep();
        	noActiveSpeaker = true;
        	noActiveListener = true;
        	SpeakerQueue.wake();
        	ListenerQueue.wake();
    	}
    	result = word;
    	lock.release();
    	return result;
    }
    
    private static class Speaker implements Runnable {
    	Speaker(int which, Communicator communicator, int times1, int word) {
    		this.which = which;
    		this.communicator = communicator;
    		this.times1 = times1;
    		this.word = word;
    	}
    	
    	public void run(){
    	    for (int i=0; i<times1; i++) {
    			System.out.println("*** thread " + which + " looped "
    					   + i + " times");
    			KThread.yield();
    	    }
    		System.out.println("*** Speaker: thread " + which + " speakers words " + word);
    		communicator.speak(word);
    		System.out.println("*** Speaker: thread " + which + " finishs speaking");
    	}
    	
    	private int which;
    	private Communicator communicator;
    	private int times1;
    	private int word;
    }
    
    private static class Listener implements Runnable {
    	Listener(int which, Communicator communicator, int times1) {
    		this.which = which;
    		this.communicator = communicator;
    		this.times1 = times1;
    	}
    	
    	public void run(){
    		for (int i=0; i<times1; i++) {
    			System.out.println("*** thread " + which + " looped "
    					   + i + " times");
    			KThread.yield();
    	    }
    		int word;
    		System.out.println("*** Listener: thread " + which + " starts listening");
    		word = communicator.listen();
    		System.out.println("*** Listener: thread " + which + " hears words " + word);
    	}
    	
    	private int which;
    	private Communicator communicator;
    	private int times1;
    }
    
    public static void selfTest() {
    	System.out.println("\nCommunicator Test:");
    	
    	KThread thread1, thread2, thread3, thread4, thread5, thread6;
    	Communicator com1 = new Communicator();
    	Communicator com2 = new Communicator();
    	Communicator com3_1 = new Communicator();
    	Communicator com3_2 = new Communicator();
    	Communicator com4 = new Communicator();

    	/**
    	 * Tests for case 1
    	 */
    	System.out.println("\ncase 1:");
    	thread1 = new KThread(new Speaker(1, com1, 0, -11));
    	thread2 = new KThread(new Listener(2, com1, 2));
    	
    	thread1.fork();
    	thread2.fork();
    	thread1.join();
    	thread2.join();
    	
    	/**
    	 * Tests for case 2
    	 */
    	System.out.println("\ncase 2:");
    	thread1 = new KThread(new Speaker(1, com2, 0, -21));
    	thread2 = new KThread(new Speaker(2, com2, 0, -22));
    	thread3 = new KThread(new Listener(3, com2, 2));
    	thread4 = new KThread(new Listener(4, com2, 10));
    	
    	thread1.fork();
    	thread2.fork();
    	thread3.fork();
    	thread4.fork();
    	thread1.join();
    	thread2.join();
    	thread3.join();
    	thread4.join();
    	
    	/**
    	 * Tests for case 3-1
    	 */
    	System.out.println("\ncase 3-1:");
    	thread1 = new KThread(new Listener(1, com3_1, 0));
    	thread2 = new KThread(new Speaker(2, com3_1, 2, -312));
    	
    	thread1.fork();
    	thread2.fork();
    	thread1.join();
    	thread2.join();
    	
    	/**
    	 * Tests for case 3-2
    	 */
    	System.out.println("\ncase 3-2:");
    	thread1 = new KThread(new Listener(1, com3_2, 0));
    	thread2 = new KThread(new Listener(2, com3_2, 0));
    	thread3 = new KThread(new Speaker(3, com3_2, 2, -323));
    	thread4 = new KThread(new Speaker(4, com3_2, 4, -324));
    	
    	thread1.fork();
    	thread2.fork();
    	thread3.fork();
    	thread4.fork();
    	thread1.join();
    	thread2.join();
    	thread3.join();
    	thread4.join();
    	/**
    	 * Tests for case 4
    	 */
    	System.out.println("\ncase 4:");
    	thread1 = new KThread(new Listener(1, com4, 0));
    	thread2 = new KThread(new Listener(2, com4, 0));
    	thread3 = new KThread(new Listener(3, com4, 0));
    	thread4 = new KThread(new Speaker(4, com4, 0, -44));
    	thread5 = new KThread(new Speaker(5, com4, 0, -45));
    	thread6 = new KThread(new Speaker(6, com4, 0, -46));
    	
    	thread1.fork();
    	thread2.fork();
    	thread3.fork();
    	thread4.fork();
    	thread5.fork();
    	thread6.fork();
    	thread1.join();
    	thread2.join();
    	thread3.join();
    	thread4.join();
    	thread5.join();
    	thread6.join();
    	
    	System.out.println("");

    	thread1 = new KThread(new Listener(1, com4, 0));
    	thread2 = new KThread(new Listener(2, com4, 0));
    	thread3 = new KThread(new Listener(3, com4, 0));
    	thread4 = new KThread(new Speaker(4, com4, 0, -44));
    	thread5 = new KThread(new Speaker(5, com4, 0, -45));
    	thread6 = new KThread(new Speaker(6, com4, 0, -46));
    	
    	thread4.fork();
    	thread5.fork();
    	thread6.fork();
    	thread1.fork();
    	thread2.fork();
    	thread3.fork();
    	
    	thread1.join();
    	thread2.join();
    	thread3.join();
    	thread4.join();
    	thread5.join();
    	thread6.join();
    	
    	System.out.println("");
    }
    
    private Lock lock;
    private Condition SpeakerQueue;
    private Condition ListenerQueue;
    boolean noActiveSpeaker;
    boolean noActiveListener;
    int word;
}

