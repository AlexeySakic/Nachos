//{intytsdovtxtdxhxhoid{idofx`ofridofit`ofkh`bido
//PART OF THE NACHOS. DON'T CHANGE CODE OF THIS LINE
package nachos.userprog;

import nachos.machine.*;
import nachos.threads.*;
import nachos.userprog.*;

import java.io.EOFException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.*;

/**
 * Encapsulates the state of a user process that is not contained in its
 * user thread (or threads). This includes its address translation state, a
 * file table, and information about the program being executed.
 *
 * <p>
 * This class is extended by other classes to support additional functionality
 * (such as additional syscalls).
 *
 * @see	nachos.vm.VMProcess
 * @see	nachos.network.NetProcess
 */
public class UserProcess {

	/**
	 * Allocate a new process.
	 */
	public UserProcess() {
		// int numPhysPages = Machine.processor().getNumPhysPages();
		// pageTable = new TranslationEntry[numPhysPages];
		// for (int i=0; i<numPhysPages; i++)
		// pageTable[i] = new TranslationEntry(i,i, true,false,false,false);

		//assign appropriate fileDescrptors
		fileDescriptorTable = new OpenFile[maxOpen];
		fileDescriptorTable[0] = UserKernel.console.openForReading();
		fileDescriptorTable[1] = UserKernel.console.openForWriting();
		openFileNames = new String[maxOpen];

		//assign a legal process ID
		idMutex.P();
		processID = nextProcessID;
		nextProcessID++;
		idMutex.V();
	}

	/**
	 * Allocate and return a new process of the correct class. The class name
	 * is specified by the <tt>nachos.conf</tt> key
	 * <tt>Kernel.processClassName</tt>.
	 *
	 * @return a new process of the correct class.
	 */
	public static UserProcess newUserProcess() {
		return (UserProcess)Lib.constructObject(Machine.getProcessClassName());
	}

	/**
	 * Execute the specified program with the specified arguments. Attempts to
	 * load the program, and then forks a thread to run it.
	 *
	 * @param name the name of the file containing the executable.
	 * @param args the arguments to pass to the executable.
	 * @return <tt>true</tt> if the program was successfully executed.
	 */
	public boolean execute(String name, String[] args) {
		if (!load(name, args))
			return false;

		new UThread(this).setName(name).fork();

		return true;
	}

	/**
	 * Save the state of this process in preparation for a context switch.
	 * Called by <tt>UThread.saveState()</tt>.
	 */
	//need modification?
	public void saveState() {
	}

	/**
	 * Restore the state of this process after a context switch. Called by
	 * <tt>UThread.restoreState()</tt>.
	 */
	public void restoreState() {
		Machine.processor().setPageTable(pageTable);
	}

	/**
	 * Read a null-terminated string from this process's virtual memory. Read
	 * at most <tt>maxLength + 1</tt> bytes from the specified address, search
	 * for the null terminator, and convert it to a <tt>java.lang.String</tt>,
	 * without including the null terminator. If no null terminator is found,
	 * returns <tt>null</tt>.
	 *
	 * @param vaddr the starting virtual address of the null-terminated
	 * string.
	 * @param maxLength the maximum number of characters in the string,
	 * not including the null terminator.
	 * @return the string read, or <tt>null</tt> if no null terminator was
	 * found.
	 */
	public String readVirtualMemoryString(int vaddr, int maxLength) {
		Lib.assertTrue(maxLength >= 0);

		byte[] bytes = new byte[maxLength+1];

		int bytesRead = readVirtualMemory(vaddr, bytes);

		for (int length=0; length<bytesRead; length++) {
			if (bytes[length] == 0)
				return new String(bytes, 0, length);
		}
		return null;
	}

	/**
	 * Transfer data from this process's virtual memory to all of the specified
	 * array. Same as <tt>readVirtualMemory(vaddr, data, 0, data.length)</tt>.
	 *
	 * @param vaddr the first byte of virtual memory to read.
	 * @param data the array where the data will be stored.
	 * @return the number of bytes successfully transferred.
	 */
	public int readVirtualMemory(int vaddr, byte[] data) {
		return readVirtualMemory(vaddr, data, 0, data.length);
	}

	/**
	 * Transfer data from this process's virtual memory to the specified array.
	 * This method handles address translation details. This method must
	 * <i>not</i> destroy the current process if an error occurs, but instead
	 * should return the number of bytes successfully copied (or zero if no
	 * data could be copied).
	 *
	 * @param vaddr the first byte of virtual memory to read.
	 * @param data the array where the data will be stored.
	 * @param offset the first byte to write in the array.
	 * @param length the number of bytes to transfer from virtual memory to
	 * the array.
	 * @return the number of bytes successfully transferred.
	 */
	public int readVirtualMemory(int vaddr, byte[] data, int offset,
			int length) {
		Lib.assertTrue(offset >= 0 && length >= 0 && offset+length <= data.length);

		byte[] memory = Machine.processor().getMemory();

		// for now, just assume that virtual addresses equal physical addresses
		// if (vaddr < 0 || vaddr >= memory.length)
		// return 0;
		//
		// int amount = Math.min(length, memory.length-vaddr);
		// System.arraycopy(memory, vaddr, data, offset, amount);
		//
		// return amount;
		//----------------------------------------------------------------------
		if(vaddr < 0 || (vaddr/pageSize) >= pageTable.length)
			return 0;

		int tableNum = pageTable.length;
		int startVaddr = vaddr;
		int endVaddr = startVaddr + length;
		int startVpn = startVaddr / pageSize;
		int endVpn = endVaddr / pageSize;
		int start = startVpn;
		int end = endVpn;
		if(end > tableNum - 1)
			end = tableNum;

		int amount = 0;

		for(int vpn = start; vpn <= end; vpn ++) {
			if(!pageTable[vpn].valid)
				return amount;

			int s = 0;
			int e = pageSize;
			if(vpn == startVpn)
				s = startVaddr % pageSize;
			if(vpn == endVpn)
				e = endVaddr % pageSize;

			int paddr = pageTable[vpn].ppn * pageSize + s;
			System.arraycopy(memory, paddr, data, offset, e - s);
			pageTable[vpn].used = true;
			amount += (e - s);
			offset += (e - s);
		}

		return amount;
		//----------------------------------------------------------------------
	}

	/**
	 * Transfer all data from the specified array to this process's virtual
	 * memory.
	 * Same as <tt>writeVirtualMemory(vaddr, data, 0, data.length)</tt>.
	 *
	 * @param vaddr the first byte of virtual memory to write.
	 * @param data the array containing the data to transfer.
	 * @return the number of bytes successfully transferred.
	 */
	public int writeVirtualMemory(int vaddr, byte[] data) {
		return writeVirtualMemory(vaddr, data, 0, data.length);
	}

	/**
	 * Transfer data from the specified array to this process's virtual memory.
	 * This method handles address translation details. This method must
	 * <i>not</i> destroy the current process if an error occurs, but instead
	 * should return the number of bytes successfully copied (or zero if no
	 * data could be copied).
	 *
	 * @param vaddr the first byte of virtual memory to write.
	 * @param data the array containing the data to transfer.
	 * @param offset the first byte to transfer from the array.
	 * @param length the number of bytes to transfer from the array to
	 * virtual memory.
	 * @return the number of bytes successfully transferred.
	 */
	public int writeVirtualMemory(int vaddr, byte[] data, int offset,
			int length) {
		Lib.assertTrue(offset >= 0 && length >= 0 && offset+length <= data.length);

		byte[] memory = Machine.processor().getMemory();

		// for now, just assume that virtual addresses equal physical addresses
		// if (vaddr < 0 || vaddr >= memory.length)
		// return 0;
		//
		// int amount = Math.min(length, memory.length-vaddr);
		// System.arraycopy(data, offset, memory, vaddr, amount);
		//
		// return amount;
		//----------------------------------------------------------------------
		if(vaddr < 0 || (vaddr/pageSize) >= pageTable.length)
			return 0;

		int tableNum = pageTable.length;
		int startVaddr = vaddr;
		int endVaddr = startVaddr + length;
		int startVpn = startVaddr / pageSize;
		int endVpn = endVaddr / pageSize;
		int start = startVpn;
		int end = endVpn;
		if(end > tableNum - 1)
			end = tableNum;

		int amount = 0;

		for(int vpn = start; vpn <= end; vpn ++) {
			if(!pageTable[vpn].valid || pageTable[vpn].readOnly)
				return amount;

			int s = 0;
			int e = pageSize;
			if(vpn == startVpn)
				s = startVaddr % pageSize;
			if(vpn == endVpn)
				e = endVaddr % pageSize;

			int paddr = pageTable[vpn].ppn * pageSize + s;
			System.arraycopy(data, offset, memory, paddr, e - s);
			pageTable[vpn].used = true;
			pageTable[vpn].dirty = true;
			amount += (e - s);
			offset += (e - s);
		}

		return amount;	//----------------------------------------------------------------------
	}

	/**
	 * Load the executable with the specified name into this process, and
	 * prepare to pass it the specified arguments. Opens the executable, reads
	 * its header information, and copies sections and arguments into this
	 * process's virtual memory.
	 *
	 * @param name the name of the file containing the executable.
	 * @param args the arguments to pass to the executable.
	 * @return <tt>true</tt> if the executable was successfully loaded.
	 */
	private boolean load(String name, String[] args) {
		Lib.debug(dbgProcess, "UserProcess.load(\"" + name + "\")");

		OpenFile executable = ThreadedKernel.fileSystem.open(name, false);
		if (executable == null) {
			Lib.debug(dbgProcess, "\topen failed");
			return false;
		}

		try {
			coff = new Coff(executable);
		}
		catch (EOFException e) {
			executable.close();
			Lib.debug(dbgProcess, "\tcoff load failed");
			return false;
		}

		// make sure the sections are contiguous and start at page 0
		numPages = 0;
		for (int s=0; s<coff.getNumSections(); s++) {
			CoffSection section = coff.getSection(s);
			if (section.getFirstVPN() != numPages) {
				coff.close();
				Lib.debug(dbgProcess, "\tfragmented executable");
				return false;
			}
			numPages += section.getLength();
		}

		// make sure the argv array will fit in one page
		byte[][] argv = new byte[args.length][];
		int argsSize = 0;
		for (int i=0; i<args.length; i++) {
			argv[i] = args[i].getBytes();
			// 4 bytes for argv[] pointer; then string plus one for null byte
			argsSize += 4 + argv[i].length + 1;
		}
		if (argsSize > pageSize) {
			coff.close();
			Lib.debug(dbgProcess, "\targuments too long");
			return false;
		}

		// program counter initially points at the program entry point
		initialPC = coff.getEntryPoint();

		// next comes the stack; stack pointer initially points to top of it
		numPages += stackPages;
		initialSP = numPages*pageSize;

		// and finally reserve 1 page for arguments
		numPages++;

		if (!loadSections())
			return false;

		// store arguments in last page
		int entryOffset = (numPages-1)*pageSize;
		int stringOffset = entryOffset + args.length*4;

		this.argc = args.length;
		this.argv = entryOffset;

		for (int i=0; i<argv.length; i++) {
			byte[] stringOffsetBytes = Lib.bytesFromInt(stringOffset);
			Lib.assertTrue(writeVirtualMemory(entryOffset,stringOffsetBytes) == 4);
			entryOffset += 4;
			Lib.assertTrue(writeVirtualMemory(stringOffset, argv[i]) ==
					argv[i].length);
			stringOffset += argv[i].length;
			Lib.assertTrue(writeVirtualMemory(stringOffset,new byte[] { 0 }) == 1);
			stringOffset += 1;
		}

		return true;
	}

	/**
	 * Allocates memory for this process, and loads the COFF sections into
	 * memory. If this returns successfully, the process will definitely be
	 * run (this is the last step in process initialization that can fail).
	 *
	 * @return <tt>true</tt> if the sections were successfully loaded.
	 */
	protected boolean loadSections() {
		// if (numPages > Machine.processor().getNumPhysPages()) {
		// coff.close();
		// Lib.debug(dbgProcess, "\tinsufficient physical memory");
		// return false;
		// }
		//
		// // load sections
		// for (int s=0; s<coff.getNumSections(); s++) {
		// CoffSection section = coff.getSection(s);
		//
		// Lib.debug(dbgProcess, "\tinitializing " + section.getName()
		// + " section (" + section.getLength() + " pages)");
		//
		// for (int i=0; i<section.getLength(); i++) {
		// int vpn = section.getFirstVPN()+i;
		//
		// // for now, just assume virtual addresses=physical addresses
		// section.loadPage(i, vpn);
		// }
		// }
		//
		// return true;
		//--------------------------------------------------------------------------
		UserKernel.ppnListSemaphore.P();
		if(numPages > UserKernel.freePPNList.size()) {
			coff.close();
			Lib.debug(dbgProcess, "\tinsufficient physical memory");
			return false;
		}

		// build PageTable
		pageTable = new TranslationEntry[numPages];

		// load section
		for(int s=0; s < coff.getNumSections(); s++) {

			CoffSection section = coff.getSection(s);

			Lib.debug(dbgProcess, "\tinitializing " + section.getName()
					+ " section (" + section.getLength() + " pages)");

			for (int i=0; i<section.getLength(); i++) {
				int vpn = section.getFirstVPN()+i;
				int ppn = UserKernel.freePPNList.poll();
				pageTable[vpn] = new TranslationEntry(vpn, ppn, true, section.isReadOnly(), false, false);
				section.loadPage(i, ppn);
			}
		}

		// build stack and arg PageTable
		for(int i=0; i < stackPages + 1; i++) {
			//	int vpn = (numPages - 1 - stackPages) + i;
			int vpn = numPages - 1 - i;
			int ppn = UserKernel.freePPNList.poll();
			pageTable[vpn] = new TranslationEntry(vpn, ppn, true, false, false, false);
		}
		UserKernel.ppnListSemaphore.V();

		return true;
		//---------------------------------------------------------------------------------------
	}

	/**
	 * Release any resources allocated by <tt>loadSections()</tt>.
	 */
	protected void unloadSections() {
		//-----------------------------------------------
		int tableNum = pageTable.length;
		UserKernel.ppnListSemaphore.P();
		for(int i=0; i < tableNum; i++) {
			if(pageTable[i] != null && pageTable[i].valid == true) {
				UserKernel.freePPNList.offer(pageTable[i].ppn);
				pageTable[i].valid = false;
				//		pageTable[i] = null; // delete entry
			}	
		}
		UserKernel.ppnListSemaphore.V();
		for (int i=0; i<16; i++){
			if (fileDescriptorTable[i] != null){
				fileDescriptorTable[i].close();
			}
		}	
		coff.close();
		//-----------------------------------------------
	}

	/**
	 * Initialize the processor's registers in preparation for running the
	 * program loaded into this process. Set the PC register to point at the
	 * start function, set the stack pointer register to point at the top of
	 * the stack, set the A0 and A1 registers to argc and argv, respectively,
	 * and initialize all other registers to 0.
	 */
	public void initRegisters() {
		Processor processor = Machine.processor();

		// by default, everything's 0
		for (int i=0; i<processor.numUserRegisters; i++)
			processor.writeRegister(i, 0);

		// initialize PC and SP according
		processor.writeRegister(Processor.regPC, initialPC);
		processor.writeRegister(Processor.regSP, initialSP);

		// initialize the first two argument registers to argc and argv
		processor.writeRegister(Processor.regA0, argc);
		processor.writeRegister(Processor.regA1, argv);
	}

	/**
	 * Set the parent process.
	 */
	private void setParent(UserProcess parent) {
		this.parentProcess = parent;
	}
	/**
	 * Set the process ID.
	 */
	private int getID() {
		return processID;
	}
	/**
	 * real join() method.
	 */
	private boolean join(int status) {
		joinSemaphore.P();
		byte[] data = Lib.bytesFromInt(exitStatus);
		writeVirtualMemory(status, data);
		joinSemaphore.V();
		return normalExit;
	}
	/**
	 * cleanups at exit
	 */
	private void cleanUp() {
		for (OpenFile file : fileDescriptorTable) 
			if (file != null)
				file.close();
		unloadSections();
		for (UserProcess child : childProcessList)
			if (child != null)
				child.setParent(null);
		tableMutex.P();
		userProcessTable.remove(UserKernel.currentProcess().getID());
		tableMutex.V();
		childProcessList.clear();
	}

	/**
	 * Handle the halt() system call. nextProcessID initialized as 0
	 */
	private int handleHalt() {
		if (processID == 0){
			Machine.halt();
		}
		else{
			return -1;
		}

		Lib.assertNotReached("Machine.halt() did not halt machine!");
		return 0;
	}

	/**
	 * the create and open systems calls were both implemented by the handleOpen method
	 */
	private int handleOpen(int addr, boolean whetherCreate){
		if (addr < 0)
			return -1;
		String filename = readVirtualMemoryString(addr, 256);
		//if unlink has been called on that file, we return -1 immediately
		if (filename == null)
			return -1;
		if (UserKernel.getKernel().fileManager.isUnlinked(filename))
			return -1;
		OpenFile file = UserKernel.fileSystem.open(filename, whetherCreate);
		UserKernel.getKernel().fileManager.openFile(filename);
		if (file == null)
			return -1;
		int fd = getFreeDescriptor();
		if (fd == -1){
			file.close();
			return -1;
		}
		fileDescriptorTable[fd] = file;
		openFileNames[fd] = filename;
		return fd;
	}

	private boolean invalidDescriptor(int fd){
		if (fd < 0 || fd >= maxOpen)
			return true;
		else{
			return false;
		}
	}

	/**
	 *  Handle the read(int fd, void *buffer, int count) system call
	 */
	private int handleRead(int fd, int addr, int count){
		if (invalidDescriptor(fd) || count < 0)
			return -1;
		if (fileDescriptorTable[fd] == null)
			return -1;
		if (fd == 1 && openFileNames[fd] == null)
			return -1;
		//byte[] buffer = new byte[count];
		//int size = fileDescriptorTable[fd].read(buffer, 0, count);
		//if (size < 0)
		//	return -1;
		//upgraded version-----------------------------------------------------
		int bytesWriteSum = 0;

		while (count > 0){
			byte[] buffer = new byte[Math.min(count, maxBufferSize)];
			count -= buffer.length;
			int bytesRead = fileDescriptorTable[fd].read(buffer, 0, buffer.length);
			if (bytesRead < 0)
				return -1;
			int bytesWrite = writeVirtualMemory(addr, buffer, 0, bytesRead);
			if (bytesWrite != bytesRead)
				return -1;
			bytesWriteSum += bytesWrite;
			addr += bytesWrite;
			if (bytesRead < buffer.length)
				break;
		}

		return bytesWriteSum;
		//---------------------------------------------------------------------
		//return writeVirtualMemory(addr, buffer, 0, size);
	}

	/**
	 *  Handle the write(int fd, void* buffer, int count) system call
	 */
	private int handleWrite(int fd, int addr, int count){
		if (invalidDescriptor(fd) || count < 0)
			return -1;
		if (fileDescriptorTable[fd] == null)
			return -1;
		if (fd == 0 && openFileNames[fd] == null)
			return -1;
		//		byte[] buffer = new byte[count];
		//		if (readVirtualMemory(addr, buffer) < count) 
		//			return -1;
		//		return fileDescriptorTable[fd].write(buffer, 0, count);
		int bytesWriteSum = 0;

		while(count > 0){
			byte[] buffer = new byte[Math.min(count, maxBufferSize)];
			count -= buffer.length;
			int bytesRead = readVirtualMemory(addr, buffer);
			if (bytesRead != buffer.length)
				return -1;
			consoleLock.acquire();
			int bytesWrite = fileDescriptorTable[fd].write(buffer, 0, buffer.length);
			consoleLock.release();
			bytesWriteSum += bytesRead;
			addr += bytesRead;
			if (bytesWrite < bytesRead)
				break;
		}

		return bytesWriteSum;
	}


	/**
	 *  Handle the close(int fileDescriptor) system call
	 */
	private int handleClose(int fd){
		if (invalidDescriptor(fd))
			return -1;
		if (fileDescriptorTable[fd] == null)
			return -1;
		fileDescriptorTable[fd].close();
		fileDescriptorTable[fd] = null;
		if (openFileNames[fd] != null){
			if (UserKernel.getKernel().fileManager.deCount(openFileNames[fd]) == false)
				return -1;
			openFileNames[fd] = null;
		}
		return 0;
	}

	private int handleUnlink(String filename){
		if (UserKernel.getKernel().fileManager.unlinkFile(filename) == false)
			return -1;
		return 0;
	}

	private int getFreeDescriptor(){
		for (int i = 0; i < maxOpen; i++){
			if (fileDescriptorTable[i] == null)
				return i;
		}
		return -1;
	}

	/**
	 * Handle the exec(char* file, int argc, char* argv[]) system call.
	 */
	private int handleExec(int file, int argc, int argv) {
		String fileName = readVirtualMemoryString(file, 256);
		if ((fileName == null) || (argc < 0))
			return -1;
		if (!fileName.toLowerCase().endsWith(".coff"))
			return -1;
		byte[] pointer = new byte[4];
		String[] args = new String[argc];
		for (int i = 0; i < argc; i++) {
			readVirtualMemory(argv, pointer);
			int pos = Lib.bytesToInt(pointer, argv);
			args[i] = readVirtualMemoryString(pos, 256);
			argv += 4;
		}
		UserProcess childProcess = new UserProcess();
		if (childProcess == null)
			return -1;
		childProcess.setParent(this);
		childProcessList.add(childProcess);
		tableMutex.P();
		userProcessTable.put(childProcess.getID(), childProcess);
		tableMutex.V();
		if (!childProcess.execute(fileName, args))
			return -1;
		return childProcess.getID();
	}

	/**
	 * Handle the join(int processID, int* status) system call.
	 */
	private int handleJoin(int processID, int status) {
		tableMutex.P();
		UserProcess joinProcess = userProcessTable.get(processID);
		tableMutex.V();
		if (joinProcess.parentProcess.getID() != 
				UserKernel.currentProcess().getID())
			return -1;
		int retVal = joinProcess.join(status) ? 1 : 0;
		childProcessList.remove(joinProcess);
		return retVal;
	}

	/**
	 * Handle exit system calls and abnormal exits
	 */
	private int handleExit(int status, boolean normalExit) {
		cleanUp();
		if (userProcessTable.isEmpty())
			Kernel.kernel.terminate();
		exitStatus = status;
		this.normalExit = normalExit;
		joinSemaphore.V();
		KThread.finish();
		return status;
	}

	private static final int
	syscallHalt = 0,
	syscallExit = 1,
	syscallExec = 2,
	syscallJoin = 3,
	syscallCreate = 4,
	syscallOpen = 5,
	syscallRead = 6,
	syscallWrite = 7,
	syscallClose = 8,
	syscallUnlink = 9;

	/**
	 * Handle a syscall exception. Called by <tt>handleException()</tt>. The
	 * <i>syscall</i> argument identifies which syscall the user executed:
	 *
	 * <table>
	 * <tr><td>syscall#</td><td>syscall prototype</td></tr>
	 * <tr><td>0</td><td><tt>void halt();</tt></td></tr>
	 * <tr><td>1</td><td><tt>void exit(int status);</tt></td></tr>
	 * <tr><td>2</td><td><tt>int  exec(char *name, int argc, char **argv);
	 * 								</tt></td></tr>
	 * <tr><td>3</td><td><tt>int  join(int pid, int *status);</tt></td></tr>
	 * <tr><td>4</td><td><tt>int  creat(char *name);</tt></td></tr>
	 * <tr><td>5</td><td><tt>int  open(char *name);</tt></td></tr>
	 * <tr><td>6</td><td><tt>int  read(int fd, char *buffer, int size);
	 *								</tt></td></tr>
	 * <tr><td>7</td><td><tt>int  write(int fd, char *buffer, int size);
	 *								</tt></td></tr>
	 * <tr><td>8</td><td><tt>int  close(int fd);</tt></td></tr>
	 * <tr><td>9</td><td><tt>int  unlink(char *name);</tt></td></tr>
	 * </table>
	 *
	 * @param	syscall	the syscall number.
	 * @param	a0	the first syscall argument.
	 * @param	a1	the second syscall argument.
	 * @param	a2	the third syscall argument.
	 * @param	a3	the fourth syscall argument.
	 * @return	the value to be returned to the user.
	 */
	public int handleSyscall(int syscall, int a0, int a1, int a2, int a3) {
		switch (syscall) {
		case syscallHalt:
			return handleHalt();
		case syscallExit:
			return handleExit(a0, true);
		case syscallJoin:
			return handleJoin(a0, a1);
		case syscallExec:
			return handleExec(a0, a1, a2);
		case syscallCreate:
			return handleOpen(a0, true);
		case syscallOpen:
			return handleOpen(a0, false);
		case syscallRead:
			return handleRead(a0, a1, a2);
		case syscallWrite:
			return handleWrite(a0, a1, a2);
		case syscallClose:
			return handleClose(a0);
		case syscallUnlink:
			return handleUnlink(readVirtualMemoryString(a0, 256));

		default:
			Lib.debug(dbgProcess, "Unknown syscall " + syscall);
			//default as exited abnormally
			handleExit(a0, false);
			Lib.assertNotReached("Unknown system call!");
		}
		return 0;
	}

	/**
	 * Handle a user exception. Called by
	 * <tt>UserKernel.exceptionHandler()</tt>. The
	 * <i>cause</i> argument identifies which exception occurred; see the
	 * <tt>Processor.exceptionZZZ</tt> constants.
	 *
	 * @param	cause	the user exception that occurred.
	 */
	public void handleException(int cause) {
		Processor processor = Machine.processor();

		switch (cause) {
		case Processor.exceptionSyscall:
			int result = handleSyscall(processor.readRegister(Processor.regV0),
					processor.readRegister(Processor.regA0),
					processor.readRegister(Processor.regA1),
					processor.readRegister(Processor.regA2),
					processor.readRegister(Processor.regA3)
					);
			processor.writeRegister(Processor.regV0, result);
			processor.advancePC();
			break;

		default:
			Lib.debug(dbgProcess, "Unexpected exception: " +
					Processor.exceptionNames[cause]);
			Lib.assertNotReached("Unexpected exception");
		}
	}

	/** The program being run by this process. */
	protected Coff coff;

	/** This process's page table. */
	protected TranslationEntry[] pageTable;
	/** The number of contiguous pages occupied by the program. */
	protected int numPages;

	/** The number of pages in the program's stack. */
	protected final int stackPages = 8;

	private int initialPC, initialSP;
	private int argc, argv;

	private static final int pageSize = Processor.pageSize;
	private static final char dbgProcess = 'a';
	//implement by us
	/** Userd by phase 2 task 1 (yhz) **/
	private String[] openFileNames;
	private OpenFile[] fileDescriptorTable;
	private final int maxOpen = 16;
	private final int maxBufferSize = 1 << 20;
	private Lock consoleLock = new Lock();

	private int exitStatus;
	private boolean normalExit;
	private UserProcess parentProcess = null;
	private LinkedList<UserProcess> childProcessList
	= new LinkedList<UserProcess>();
	private static HashMap<Integer, UserProcess> userProcessTable
	= new HashMap<Integer, UserProcess>();
	private static int nextProcessID = 0;
	private int processID;
	private static Semaphore joinSemaphore = new Semaphore(0);
	private static Semaphore idMutex = new Semaphore(1);
	private static Semaphore tableMutex = new Semaphore(1);
}
