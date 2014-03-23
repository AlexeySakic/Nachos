//{intytsdovtxtdxhxhoid{idofx`ofridofit`ofkh`bido
//PART OF THE NACHOS. DON'T CHANGE CODE OF THIS LINE
package nachos.threads;
import nachos.ag.BoatGrader;

public class Boat
{
    static BoatGrader bg;
    static BoatChecker bc;
    static boolean isRowerSet;
    static boolean isRiderSet;
    static int population;
    static Lock AllMove;
    static Semaphore End;
    static Condition CVEnd;
    static Lock RowerMove;
    static Condition CVRowerMove;
    static Lock RiderMove;
    static Condition CVRiderMove;
    static Lock BeingCarried;
    static Semaphore Register;
    static Semaphore LockTransmitted;
    static boolean isAdult;
    
    static Semaphore errOccurs;
    static String errMessage;

    public static void selfTest()
    {
	BoatGrader b = new BoatGrader();

	try{
		begin(10, 8, b);
	}
	catch(Exception e)
	{
		System.out.println(e.toString());
	}

//	System.out.println("\n ***Testing Boats with 2 children, 1 adult***");
//  	begin(1, 2, b);

//  	System.out.println("\n ***Testing Boats with 3 children, 3 adults***");
//  	begin(3, 3, b);
    }

    public static void begin( int adults, int children, BoatGrader b ) throws Exception
    {
	// Store the externally generated autograder in a class
	// variable to be accessible by children.
	bg = b;
	bc = new BoatChecker(adults, children);
	isRowerSet = false;
	isRiderSet = false;
	population = 0;
	AllMove = new Lock();
	End = new Semaphore(0);
	RowerMove = new Lock();
	CVRowerMove = new Condition(RowerMove);
	RiderMove = new Lock();
	CVRiderMove = new Condition(RiderMove);
	BeingCarried = new Lock();
	Register = new Semaphore(0);
	LockTransmitted = new Semaphore(0);
	
	errOccurs = new Semaphore(1);
	errMessage = null;

	// Instantiate global variables here

	// Create threads here. See section 3.4 of the Nachos for Java
	// Walkthrough linked from the projects page.

	AllMove.acquire();
	
	Runnable child = new Runnable() {
	    public void run() {
            ChildItinerary();
        }
    };
    
	Runnable adult = new Runnable() {
	    public void run() {
            AdultItinerary();
        }
    };
    
    for(int i = 0; i < children; i ++)
    {
    	KThread t = new KThread(child);
    	t.setName("Child Thread " + i);
    	t.fork();
    	Register.P();    	
    }
	for(int i = 0; i < adults; i ++)
	{
    	KThread t = new KThread(adult);
    	t.setName("Adult Thread " + i);
    	t.fork();
    	Register.P();    
	}
	AllMove.release();
	End.P();
	Halting();
    }

    static void AdultItinerary()
    {
    	try{
	bg.initializeAdult(); //Required for autograder interface. Must be the first thing called.
	//DO NOT PUT ANYTHING ABOVE THIS LINE.

	/* This is where you should put your solutions. Make calls
	   to the BoatGrader to show that it is synchronized. For
	   example:
	       bg.AdultRowToMolokai();
	   indicates that an adult has rowed the boat across to Molokai
	*/
	population ++;
	Register.V();
	AllMove.acquire();
	AllMove.release();
	BeingCarried.acquire();
	isAdult = true;
	RowerMove.acquire();
	CVRowerMove.wake();
	CVRowerMove.sleep();
	AdultRowToMolokai();
	population --;
	CVRowerMove.wake();
	RowerMove.release();
	LockTransmitted.P();
	BeingCarried.release();
    	}
    	catch(Exception e)
    	{
    		errOccurs.P();
    		errMessage = e.getMessage();
    		End.V();
    	}
    }

    static void ChildItinerary()
    {
    	try{
	bg.initializeChild(); //Required for autograder interface. Must be the first thing called.
	//DO NOT PUT ANYTHING ABOVE THIS LINE.
	population ++;
	boolean isRower = false;
	boolean isRider = false;
	if(isRowerSet == false)
	{
		isRowerSet = true;
		isRower = true;
	}
	else if(isRiderSet == false)
	{

		isRiderSet = true;
		isRider = true;
	}
	if(isRower == true) Rower();
	else if(isRider == true) Rider();
	else
	{
		Register.V();
		AllMove.acquire();
		AllMove.release();
		BeingCarried.acquire();
		isAdult = false;
		RowerMove.acquire();
		CVRowerMove.wake();
		CVRowerMove.sleep();
		ChildRideToMolokai();
		population --;
		CVRowerMove.wake();
		RowerMove.release();
		LockTransmitted.P();
		BeingCarried.release();
	}
	}
    	catch(Exception e)
    	{
    		errOccurs.P();
    		errMessage = e.getMessage();
    		End.V();
    	}
    }

    static void Rower() throws Exception
    {
    	RowerMove.acquire();
    	RiderMove.acquire();
    	Register.V();
    	AllMove.acquire();
    	AllMove.release();
    	while(population >= 3)
    	{
    		CVRowerMove.sleep();
    		if(isAdult == true)
    		{
    			ChildRowToMolokai();
    			CVRiderMove.wake();
    			CVRiderMove.sleep();
    			CVRowerMove.wake();
    			CVRowerMove.sleep();
    			ChildRowToOahu();
    			LockTransmitted.V();
    		}
    		else
    		{

    			ChildRowToMolokai();
    			CVRowerMove.wake();
    			CVRowerMove.sleep();
    			ChildRowToOahu();
    			LockTransmitted.V();
    		}
    	}
    	RowerMove.release();
    	ChildRowToMolokai();
    	CVRiderMove.wake();
    	CVRiderMove.sleep();
    	RiderMove.release();
    	End.V();
    }
    
    static void Rider() throws Exception
    {
    	Register.V();
    	AllMove.acquire();
    	AllMove.release();
    	RiderMove.acquire();
    	while(population >= 3)
    	{

    		ChildRideToMolokai();
    		ChildRowToOahu();
    		CVRiderMove.wake();
    		CVRiderMove.sleep();
    	}
    	ChildRideToMolokai();
    	CVRiderMove.wake();
    	RiderMove.release();
    }
    
    static void ChildRideToMolokai() throws Exception
    {
    	bg.ChildRideToMolokai();
    	bc.ChildRideToMolokai();
    }
    static void ChildRowToMolokai() throws Exception
    {
    	bg.ChildRowToMolokai();
    	bc.ChildRowToMolokai();
    }
    static void AdultRowToMolokai() throws Exception
    {
    	bg.AdultRowToMolokai();
    	bc.AdultRowToMolokai();
    }
    static void ChildRowToOahu() throws Exception
    {
    	bg.ChildRowToOahu();
    	bc.ChildRowToOahu();
    }
    static void ChildRideToOahu() throws Exception
    {
    	bg.ChildRideToOahu();
    	bc.ChildRideToOahu();
    }
    static void AdultRowToOahu() throws Exception
    {
    	bg.AdultRowToOahu();
    	bc.AdultRowToOahu();
    }
    static void Halting() throws Exception
    {
    	if(errMessage != null)
    	{
    		throw new Exception(errMessage);
    	}
    	bc.Halting();
    }
}

class BoatChecker
{
	private int adultOnOahu;
	private int childOnOahu;
	private int adultOnMolokai;
	private int childOnMolokai;
	private int step;
	enum State
	{
		START,
		CHILD_ROW_MOL,
		CHILD_RID_MOL,
		ADULT_ROW_MOL,
		CHILD_ROW_OAH,
		ADULT_ROW_OAH,
		CHILD_RID_OAH,
		HALT
	}
	private State state;
	
	public BoatChecker(int adults, int children)
	{
		adultOnOahu = adults;
		adultOnMolokai = 0;
		childOnOahu = children;
		childOnMolokai = 0;
		step = 0;
		state = State.START;
	}
	
	public void ChildRowToMolokai() throws Exception
	{
		step ++;
		switch(state)
		{
		case CHILD_ROW_OAH:
		case CHILD_RID_OAH:
		case ADULT_ROW_OAH:
		case START:
			state = State.CHILD_ROW_MOL;
			break;
		default:
			throw new Exception("On step " + step + ", transition error: boat is not on Oahu");
		}
		childOnOahu --;
		childOnMolokai ++;
		if(childOnOahu < 0)
		{
			throw new Exception("On step " + step + ", population error: there's no child on Oahu");
		}
	}
	
	public void ChildRideToMolokai() throws Exception
	{
		step ++;
		if(state == State.CHILD_ROW_MOL)
		{
			state = State.CHILD_RID_MOL;
		}
		else
		{
			throw new Exception("On step " + step + ", transition error: no one is rowing the boat on Oahu");
		}
		childOnOahu --;
		childOnMolokai ++;
		if(childOnOahu < 0)
		{
			throw new Exception("On step " + step + ", population error: there's no child on Oahu");
		}
	}
	
	public void AdultRowToMolokai() throws Exception
	{
		step ++;
		switch(state)
		{
		case CHILD_ROW_OAH:
		case ADULT_ROW_OAH:
		case CHILD_RID_OAH:
		case START:
			state = State.ADULT_ROW_MOL;
			break;
			default:
				throw new Exception("On step " + step + ", transition error: boat is not on Oahu");
		}
		adultOnOahu --;
		adultOnMolokai ++;
		if(adultOnOahu < 0)
		{
			throw new Exception("On step " + step + ", population error: there's no adult on Oahu");
		}
	}
	
	public void ChildRowToOahu() throws Exception
	{
		step ++;
		switch(state)
		{
		case CHILD_ROW_MOL:
		case CHILD_RID_MOL:
		case ADULT_ROW_MOL:
			state = State.CHILD_ROW_OAH;
			break;
		default:
			throw new Exception("On step " + step + ", transition error: boat is not on Molokai");
		}
		childOnMolokai --;
		childOnOahu ++;
		if(childOnMolokai < 0)
		{
			throw new Exception("On step " + step + ", population error: there's no child on Oahu");
		}
	}
	
	public void ChildRideToOahu() throws Exception
	{
		step ++;
		if(state == State.CHILD_ROW_OAH)
		{
			state = State.CHILD_RID_OAH;
		}
		else
		{
			throw new Exception("On step " + step + ", transition error: no one is rowing the boat on Molokai");
		}
		childOnOahu ++;
		childOnMolokai --;
		if(childOnOahu < 0)
		{
			throw new Exception("On step " + step + ", population error: there's no child on Molokai");
		}
	}
	
	public void AdultRowToOahu() throws Exception
	{
		step ++;
		switch(state)
		{
		case CHILD_ROW_MOL:
		case ADULT_ROW_MOL:
		case CHILD_RID_MOL:
		case START:
			state = State.ADULT_ROW_OAH;
			break;
			default:
				throw new Exception("On step " + step + ", transition error: boat is not on Molokai");
		}
		adultOnOahu ++;
		adultOnMolokai --;
		if(adultOnOahu < 0)
		{
			throw new Exception("On step " + step + ", population error: there's no adult on Molokai");
		}
	}
	
	public void Halting() throws Exception
	{
		switch(state)
		{
		case ADULT_ROW_MOL:
		case CHILD_ROW_MOL:
		case CHILD_RID_MOL:
			state = State.HALT;
			break;
			default:
				throw new Exception("On step " + step + ", halting error: boat is not on Molokai");
		}
		if(childOnOahu > 0 || adultOnOahu > 0)
		{
			throw new Exception("On step " + step + ", halting error: there are still people on Oahu" + childOnOahu + " / " + adultOnOahu);
		}
		int minStep = 5 * adultOnMolokai + 3 * childOnMolokai - 4;
		System.out.println("Successfully carried " + adultOnMolokai + " adults and " + childOnMolokai + " children from Oahu to Molokai within " + step + "/" + minStep + " steps");
	}
}