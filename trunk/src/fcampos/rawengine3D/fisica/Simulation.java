package fcampos.rawengine3D.fisica;


import java.util.ArrayList;

public class Simulation
{

	protected int numOfMasses;								// number of masses in this container
	protected ArrayList<Mass> masses;									// masses are held by pointer to pointer. (Here Mass** represents a 1 dimensional array)
	
	
	public Simulation(int numOfMasses, float m)			// Constructor creates some masses with mass values m
	{
		this.numOfMasses = numOfMasses;
		
		masses = new ArrayList<Mass>(numOfMasses);			// Create an array of pointers

		for (int a = 0; a < numOfMasses; ++a)		// We will step to every pointer in the array
			masses.add(a, new Mass(m));				// Create a Mass as a pointer and put it in the array
	}

	
	public Mass getMass(int index)
	{
		if (index < 0 || index >= numOfMasses)		// if the index is not in the array
			return null;							// then return NULL

		return masses.get(index);						// get the mass at the index
	}

	public void init()								// this method will call the init() method of every mass
	{
		for (int a = 0; a < numOfMasses; ++a)		// We will init() every mass
			masses.get(a).init();						// call init() method of the mass
	}

	public void solve()							// no implementation because no forces are wanted in this basic container
	{
													// in advanced containers, this method will be overrided and some forces will act on masses
	}

	public void simulate(float dt)					// Iterate the masses by the change in time
	{
		for (int a = 0; a < numOfMasses; ++a)		// We will iterate every mass
			masses.get(a).simulate(dt);				// Iterate the mass and obtain new position and new velocity
	}

	public void operate(float dt)					// The complete procedure of simulation
	{
		init();										// Step 1: reset forces to zero
		solve();									// Step 2: apply forces
		simulate(dt);								// Step 3: iterate the masses by the change in time
	}

	
	public int getNumberOfMasses()
	{
		return numOfMasses;
	}
}
