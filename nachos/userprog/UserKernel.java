//{intytsdovtxtdxhxhoid{idofx`ofridofit`ofkh`bido
//PART OF THE NACHOS. DON'T CHANGE CODE OF THIS LINE
package nachos.userprog;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import nachos.machine.*;
import nachos.threads.*;
import nachos.userprog.*;

/**
 * A kernel that can support multiple user processes.
 */
public class UserKernel extends ThreadedKernel {
	/**
	 * Allocate a new user kernel.
	 */
	public UserKernel() {
		super();
	}

	/**
	 * Initialize this kernel. Creates a synchronized console and sets the
	 * processor's exception handler.
	 */
	public void initialize(String[] args) {
		super.initialize(args);

		console = new SynchConsole(Machine.console());
		fileManager = new FileManager();

		//------------------------------------------------------------------------------------------------

		ppnListSemaphore = new Semaphore(1);
		ppnListSemaphore.P();
		freePPNList.clear();
		int ppn = Machine.processor().getNumPhysPages();
		for(int i=0; i < ppn; i++)
			Lib.assertTrue(freePPNList.offer(i), "Error: happen in freePPNList " + i +"-th items offer.");
		ppnListSemaphore.V();
		//------------------------------------------------------------------------------------------------
		Machine.processor().setExceptionHandler(new Runnable() {
			public void run() { exceptionHandler(); }
		});
	}

	/**
	 * Test the console device.
	 */
	public void selfTest() {
		super.selfTest();

		System.out.println("Testing the console device. Typed characters");
		System.out.println("will be echoed until q is typed.");

		char c;

		do {
			c = (char) console.readByte(true);
			console.writeByte(c);
		}
		while (c != 'q');

		System.out.println("");
	}

	/**
	 * Returns the current process.
	 *
	 * @return	the current process, or <tt>null</tt> if no process is current.
	 */
	public static UserProcess currentProcess() {
		if (!(KThread.currentThread() instanceof UThread))
			return null;

		return ((UThread) KThread.currentThread()).process;
	}

	/**
	 * The exception handler. This handler is called by the processor whenever
	 * a user instruction causes a processor exception.
	 *
	 * <p>
	 * When the exception handler is invoked, interrupts are enabled, and the
	 * processor's cause register contains an integer identifying the cause of
	 * the exception (see the <tt>exceptionZZZ</tt> constants in the
	 * <tt>Processor</tt> class). If the exception involves a bad virtual
	 * address (e.g. page fault, TLB miss, read-only, bus error, or address
	 * error), the processor's BadVAddr register identifies the virtual address
	 * that caused the exception.
	 */
	public void exceptionHandler() {
		Lib.assertTrue(KThread.currentThread() instanceof UThread);

		UserProcess process = ((UThread) KThread.currentThread()).process;
		int cause = Machine.processor().readRegister(Processor.regCause);
		process.handleException(cause);
	}

	/**
	 * Start running user programs, by creating a process and running a shell
	 * program in it. The name of the shell program it must run is returned by
	 * <tt>Machine.getShellProgramName()</tt>.
	 *
	 * @see	nachos.machine.Machine#getShellProgramName
	 */
	public void run() {
		super.run();

		UserProcess process = UserProcess.newUserProcess();

		String shellProgram = Machine.getShellProgramName();
		Lib.assertTrue(process.execute(shellProgram, new String[] { }));

		KThread.currentThread().finish();
	}

	/**
	 * Terminate this kernel. Never returns.
	 */
	public void terminate() {
		super.terminate();
	}
	
	public class FileManager{
		public FileManager() {
			openFileList = new HashMap<String, FileStruct>();
		}
		
		private class FileStruct{
			FileStruct(String filename){
				this.filename = filename;
				this.count = 1;
				this.unlink = false;
			}
			
			String filename;
			int count;
			boolean unlink;
		}
		
		public void openFile(String filename){
			FileStruct fs = openFileList.get(filename);
			if (fs != null){
				fs.count++;
			}
			else{
				fs = new FileStruct(filename);
				openFileList.put(filename, fs);
			}
		}
		
		public boolean deCount(String filename){
			FileStruct fs = openFileList.get(filename);
			if (fs == null)
				return false;
			fs.count--;
			if (fs.count == 0){
				openFileList.remove(filename);
				if (fs.unlink == true){
					return fileSystem.remove(filename);
				}
			}
			return true;
		}
		
		public boolean unlinkFile(String filename){
			FileStruct fs = (FileStruct)openFileList.get(filename);
			if (fs == null){
				return fileSystem.remove(filename);
			}
			fs.unlink = true;
			return true;
		}
		
		public boolean isUnlinked(String filename){
			FileStruct fs = openFileList.get(filename);
			if (fs == null){
				return false;
			}
			return fs.unlink;
		}
		
		public boolean isFileListEmpty(){
			return openFileList.size() == 0;
		}
		
		private HashMap<String, FileStruct> openFileList;
	}
	
	public static UserKernel getKernel(){
		return (UserKernel)kernel;
	}

	/** Globally accessible reference to the synchronized console. */
	public static SynchConsole console;

	public FileManager fileManager;

	//------------------------------------------------------------------------------------------------
	public static Queue<Integer> freePPNList = new LinkedList<Integer>();
	public static Semaphore ppnListSemaphore;
	//------------------------------------------------------------------------------------------------
	// dummy variables to make javac smarter
	private static Coff dummy1 = null;
}
