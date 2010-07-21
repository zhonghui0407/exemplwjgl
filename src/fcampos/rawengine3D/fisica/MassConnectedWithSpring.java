package fcampos.rawengine3D.fisica;

import fcampos.rawengine3D.MathUtil.*;

/*
class MassConnectedWithSpring is derived from class Simulation
It creates 1 mass with mass value 1 kg and binds the mass to an arbitrary constant point with a spring. 
This point is refered as the connectionPos and the spring has a springConstant value to represent its 
stiffness.
*/
public class MassConnectedWithSpring extends Simulation
{

	public float springConstant;													//more the springConstant, stiffer the spring force
	public Vector3f connectionPos;													//the arbitrary constant point that the mass is connected
	public Vector3f springVector  = new Vector3f();

	public MassConnectedWithSpring(float springConstant) 		//Constructor firstly constructs its super class with 1 mass and 1 kg
	{
		super(1, 1.0f);
		this.springConstant = springConstant;								//set the springConstant

		connectionPos = new Vector3f(0.0f, -5.0f, 0.0f);						//set the connectionPos

		masses.get(0).setPosition(VectorMath.add(connectionPos, 10.0f, 0.0f, 0.0f));		//set the position of the mass 10 meters to the right side of the connectionPos
		masses.get(0).setVelocity(0, 0, 0);						//set the velocity of the mass to zero
	}

	public void solve()													//the spring force will be applied
	{
		for (int a = 0; a < numOfMasses; ++a)								//we will apply force to all masses (actually we have 1 mass, but we can extend it in the future)
		{
			springVector.setTo(VectorMath.subtract(masses.get(a).getPosition(), connectionPos));			//find a vector from the position of the mass to the connectionPos
			
			Vector3f temp = new Vector3f(springVector);
			temp.negate();
			masses.get(a).applyForce(VectorMath.multiply(temp, springConstant));			//apply the force according to the famous spring force formulation
			springVector.setZero();
		}
	}
	
};
