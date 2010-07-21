package fcampos.rawengine3D.fisica;

import fcampos.rawengine3D.MathUtil.*;

public class SpringRope
{

	private Mass mass1;										//The first mass at one tip of the spring
	private Mass mass2;										//The second mass at the other tip of the spring

	private float springConstant;								//A constant to represent the stiffness of the spring
	private float springLength;									//The length that the spring does not exert any force
	private float frictionConstant;								//A constant to be used for the inner friction of the spring

	public SpringRope(Mass mass1, Mass mass2, 
			float springConstant, float springLength, float frictionConstant)		//Constructor
	{
		this.springConstant = springConstant;									//set the springConstant
		this.springLength = springLength;										//set the springLength
		this.frictionConstant = frictionConstant;								//set the frictionConstant

		this.mass1 = mass1;													//set mass1
		this.mass2 = mass2;													//set mass2
	}

	public void solve()																	//solve() method: the method where forces can be applied
	{
				
		Vector3f springVector = new Vector3f();
		springVector = VectorMath.subtract(mass1.getPosition(), mass2.getPosition());							//vector between the two masses
		
		float r = VectorMath.magnitude(springVector);										//distance between the two masses

		Vector3f force = new Vector3f();																//force initially has a zero value
		Vector3f temp = new Vector3f();

		if (r != 0)	{																//to avoid a division by zero check if r is zero
			//force += (springVector / r) * (r - springLength) * (-springConstant);	//the spring force is added to the force

			temp = VectorMath.divide(springVector,  r);
			float temp1 = (r - springLength) * (-springConstant);
			temp = VectorMath.multiply(temp, temp1);
			force = VectorMath.add(force, temp);
		}
			temp.setZero();
			
			
		//force += -(mass1->vel - mass2->vel) * frictionConstant;						//the friction force is added to the force
							//with this addition we obtain the net force of the spring
			
			temp = VectorMath.subtract(mass1.getVelocity(), mass2.getVelocity());
			temp.negate();
			temp = VectorMath.multiply(temp, frictionConstant);
			force = VectorMath.add(force, temp);

		mass1.applyForce(force);													//force is applied to mass1
		force.negate();
		mass2.applyForce(force);													//the opposite of force is applied to mass2
	}

}