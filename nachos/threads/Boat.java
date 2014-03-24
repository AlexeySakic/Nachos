//{intytsdovtxtdxhxhoid{idofx`ofridofit`ofkh`bido
//PART OF THE NACHOS. DON'T CHANGE CODE OF THIS LINE
package nachos.threads;
import nachos.ag.BoatGrader;

public class Boat
{
    static BoatGrader bg;
    static boolean isRowerSet;
    static boolean isRiderSet;
    static int population;
    
    static boolean isAdult;
    static Lock move;
    static Condition rowerMove;
    static Condition riderMove;
    static Condition passengerMove;
    static Condition mainMove;
    static Condition waitingMove;
    

    public static void selfTest()
    {
    	BoatChecker bc = new BoatChecker();

		begin(10, 10, bc);
		bc.Halting();

//	System.out.println("\n ***Testing Boats with 2 children, 1 adult***");
//  	begin(1, 2, b);

//  	System.out.println("\n ***Testing Boats with 3 children, 3 adults***");
//  	begin(3, 3, b);
    }

    public static void begin( int adults, int children, BoatGrader b )
    {
	// Store the externally generated autograder in a class
	// variable to be accessible by children.
	bg = b;
	isRowerSet = false;
	isRiderSet = false;
	population = 0;
	
	move = new Lock();
	riderMove = new Condition(move);
	rowerMove = new Condition(move);
	passengerMove = new Condition(move);
	waitingMove = new Condition(move);
	mainMove = new Condition(move);
	

	// Instantiate global variables here

	// Create threads here. See section 3.4 of the Nachos for Java
	// Walkthrough linked from the projects page.

	move.acquire();
	
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
    	mainMove.sleep();
    }
	for(int i = 0; i < adults; i ++)
	{
    	KThread t = new KThread(adult);
    	t.setName("Adult Thread " + i);
    	t.fork();
    	mainMove.sleep();
	}
	rowerMove.wake();
	mainMove.sleep();
    }

    static void AdultItinerary()
    {
	bg.initializeAdult(); //Required for autograder interface. Must be the first thing called.
	//DO NOT PUT ANYTHING ABOVE THIS LINE.

	/* This is where you should put your solutions. Make calls
	   to the BoatGrader to show that it is synchronized. For
	   example:
	       bg.AdultRowToMolokai();
	   indicates that an adult has rowed the boat across to Molokai
	*/
	move.acquire();
	population ++;
	mainMove.wake();
	waitingMove.sleep();
	isAdult = true;
	rowerMove.wake();
	passengerMove.sleep();
	bg.AdultRowToMolokai();
	population --;
	rowerMove.wake();
    move.release();
    }

    static void ChildItinerary()
    {
	bg.initializeChild(); //Required for autograder interface. Must be the first thing called.
	//DO NOT PUT ANYTHING ABOVE THIS LINE.
	move.acquire();
	population ++;
	if(isRowerSet == false)
	{
		isRowerSet = true;
		Rower();
	}
	else if(isRiderSet == false)
	{

		isRiderSet = true;
		Rider();
	}
	else
	{
		mainMove.wake();
		waitingMove.sleep();
		isAdult = false;
		rowerMove.wake();
		passengerMove.sleep();
		bg.ChildRideToMolokai();
		population --;
		rowerMove.wake();
	}
    move.release();
    	
    }

    static void Rower()
    {
    	mainMove.wake();
    	rowerMove.sleep();
    	while(population >= 3)
    	{
    		waitingMove.wake();
    		rowerMove.sleep();
    		if(isAdult == true)
    		{
    			bg.ChildRowToMolokai();
    			riderMove.wake();
    			rowerMove.sleep();
    			passengerMove.wake();
    			rowerMove.sleep();
    			bg.ChildRowToOahu();
    		}
    		else
    		{

    			bg.ChildRowToMolokai();
    			passengerMove.wake();
    			rowerMove.sleep();
    			bg.ChildRowToOahu();
    		}
    	}
    	bg.ChildRowToMolokai();
    	riderMove.wake();
    }
    
    static void Rider()
    {
    	mainMove.wake();
    	riderMove.sleep();
    	while(population >= 3)
    	{

    		bg.ChildRideToMolokai();
    		bg.ChildRowToOahu();
    		rowerMove.wake();
    		riderMove.sleep();
    	}
    	bg.ChildRideToMolokai();
    	mainMove.wake();
    }
}

class BoatChecker extends BoatGrader
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
	
	public BoatChecker()
	{
		super();
		adultOnOahu = 0;
		adultOnMolokai = 0;
		childOnOahu = 0;
		childOnMolokai = 0;
		step = 0;
		state = State.START;
	}
	
	public void initializeChild()
	{
		super.initializeChild();
		childOnOahu ++;
	}
	
	public void initializeAdult()
	{
		super.initializeAdult();
		adultOnOahu ++;
	}
	
	public void ChildRowToMolokai()
	{
		super.ChildRowToMolokai();
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
			System.out.println("On step " + step + ", transition error: boat is not on Oahu");
		}
		childOnOahu --;
		childOnMolokai ++;
		if(childOnOahu < 0)
		{
			System.out.println("On step " + step + ", population error: there's no child on Oahu");
		}
	}
	
	public void ChildRideToMolokai()
	{
		super.ChildRideToMolokai();
		step ++;
		if(state == State.CHILD_ROW_MOL)
		{
			state = State.CHILD_RID_MOL;
		}
		else
		{
			System.out.println("On step " + step + ", transition error: no one is rowing the boat on Oahu");
		}
		childOnOahu --;
		childOnMolokai ++;
		if(childOnOahu < 0)
		{
			System.out.println("On step " + step + ", population error: there's no child on Oahu");
		}
	}
	
	public void AdultRowToMolokai()
	{
		super.AdultRowToMolokai();
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
				System.out.println("On step " + step + ", transition error: boat is not on Oahu");
		}
		adultOnOahu --;
		adultOnMolokai ++;
		if(adultOnOahu < 0)
		{
			System.out.println("On step " + step + ", population error: there's no adult on Oahu");
		}
	}
	
	public void ChildRowToOahu()
	{
		super.ChildRowToOahu();
		step ++;
		switch(state)
		{
		case CHILD_ROW_MOL:
		case CHILD_RID_MOL:
		case ADULT_ROW_MOL:
			state = State.CHILD_ROW_OAH;
			break;
		default:
			System.out.println("On step " + step + ", transition error: boat is not on Molokai");
		}
		childOnMolokai --;
		childOnOahu ++;
		if(childOnMolokai < 0)
		{
			System.out.println("On step " + step + ", population error: there's no child on Oahu");
		}
	}
	
	public void ChildRideToOahu()
	{
		super.ChildRideToOahu();
		step ++;
		if(state == State.CHILD_ROW_OAH)
		{
			state = State.CHILD_RID_OAH;
		}
		else
		{
			System.out.println("On step " + step + ", transition error: no one is rowing the boat on Molokai");
		}
		childOnOahu ++;
		childOnMolokai --;
		if(childOnOahu < 0)
		{
			System.out.println("On step " + step + ", population error: there's no child on Molokai");
		}
	}
	
	public void AdultRowToOahu()
	{
		super.AdultRowToOahu();
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
				System.out.println("On step " + step + ", transition error: boat is not on Molokai");
		}
		adultOnOahu ++;
		adultOnMolokai --;
		if(adultOnOahu < 0)
		{
			System.out.println("On step " + step + ", population error: there's no adult on Molokai");
		}
	}
	
	public void Halting()
	{
		switch(state)
		{
		case ADULT_ROW_MOL:
		case CHILD_ROW_MOL:
		case CHILD_RID_MOL:
			state = State.HALT;
			break;
			default:
				System.out.println("On step " + step + ", halting error: boat is not on Molokai");
		}
		if(childOnOahu > 0 || adultOnOahu > 0)
		{
			System.out.println("On step " + step + ", halting error: there are still people on Oahu" + childOnOahu + " / " + adultOnOahu);
		}
		int minStep = 5 * adultOnMolokai + 3 * childOnMolokai - 4;
		System.out.println("Successfully carried " + adultOnMolokai + " adults and " + childOnMolokai + " children from Oahu to Molokai within " + step + "/" + minStep + " steps");
	}
}