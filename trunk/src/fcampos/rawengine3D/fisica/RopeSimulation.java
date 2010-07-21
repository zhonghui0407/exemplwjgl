package fcampos.rawengine3D.fisica;

import fcampos.rawengine3D.MathUtil.*;
import java.util.ArrayList;

/*
class RopeSimulation simulates a rope with 
point-like particles binded with springs. The springs have inner friction and normal length. One tip of 
the rope is stabilized at a point in space called "Vector3D ropeConnectionPos". This point can be 
moved externally by a method "void setRopeConnectionVel(Vector3D ropeConnectionVel)". RopeSimulation 
creates air friction and a planer surface (or ground) with a normal in +y direction. RopeSimulation 
implements the force applied by this surface. In the code, the surface is refered as "ground".
*/
public class RopeSimulation extends Simulation				//An object to simulate a rope interacting with a planer surface and air
{

	private ArrayList<SpringRope> springs;									//Springs binding the masses (there shall be [numOfMasses - 1] of them)

	private Vector3f gravitation;								//gravitational acceleration (gravity will be applied to all masses)

	private Vector3f ropeConnectionPos = new Vector3f();							//A point in space that is used to set the position of the 
														//first mass in the system (mass with index 0)
	
	private Vector3f ropeConnectionVel = new Vector3f();							//a variable to move the ropeConnectionPos (by this, we can swing the rope)

	private float groundRepulsionConstant;						//a constant to represent how much the ground shall repel the masses
	
	private float groundFrictionConstant;						//a constant of friction applied to masses by the ground
														//(used for the sliding of rope on the ground)
	
	private float groundAbsorptionConstant;						//a constant of absorption friction applied to masses by the ground
														//(used for vertical collisions of the rope with the ground)
	
	private float groundHeight;									//a value to represent the y position value of the ground
														//(the ground is a planer surface facing +y direction)

	private float airFrictionConstant;							//a constant of air friction applied to masses

	public RopeSimulation(										//a long long constructor with 11 parameters starts here
		int numOfMasses,								//1. the number of masses
		float m,										//2. weight of each mass
		float springConstant,							//3. how stiff the springs are
		float springLength,								//4. the length that a spring does not exert any force
		float springFrictionConstant,					//5. inner friction constant of spring
		Vector3f gravitation,							//6. gravitational acceleration
		float airFrictionConstant,						//7. air friction constant
		float groundRepulsionConstant,					//8. ground repulsion constant
		float groundFrictionConstant,					//9. ground friction constant
		float groundAbsorptionConstant,					//10. ground absorption constant
		float groundHeight								//11. height of the ground (y position)
		) 					//The super class creates masses with weights m of each
	{
		super(numOfMasses, m);
		this.gravitation = gravitation;
		
		this.airFrictionConstant = airFrictionConstant;

		this.groundFrictionConstant = groundFrictionConstant;
		this.groundRepulsionConstant = groundRepulsionConstant;
		this.groundAbsorptionConstant = groundAbsorptionConstant;
		this.groundHeight = groundHeight;

		for (int a = 0; a < numOfMasses; ++a)			//To set the initial positions of masses loop with for(;;)
		{
			masses.get(a).setPosition(a * springLength, 0, 0);
			//masses.get(a).pos.x = a * springLength;		//Set x position of masses[a] with springLength distance to its neighbor
			//masses.get(a).pos.y = 0;						//Set y position as 0 so that it stand horizontal with respect to the ground
			//masses.get(a).pos.z = 0;						//Set z position as 0 so that it looks simple
		}

		springs = new ArrayList<SpringRope>(numOfMasses - 1);			//create [numOfMasses - 1] pointers for springs
														//([numOfMasses - 1] springs are necessary for numOfMasses)
		
		for (int a = 0; a < numOfMasses - 1; ++a)			//to create each spring, start a loop
		{
			//Create the spring with index "a" by the mass with index "a" and another mass with index "a + 1".
			springs.add(a, new SpringRope(masses.get(a), masses.get(a + 1), 
				springConstant, springLength, springFrictionConstant));
		}
	}

	
	public void solve()										//solve() is overriden because we have forces to be applied
	{
		for (int a = 0; a < numOfMasses - 1; ++a)		//apply force of all springs
		{
			springs.get(a).solve();						//Spring with index "a" should apply its force
		}
		Vector3f temp = new Vector3f();
		for (int a = 0; a < numOfMasses; ++a)				//Start a loop to apply forces which are common for all masses
		{
			masses.get(a).applyForce(VectorMath.multiply(gravitation, masses.get(a).getMass()));				//The gravitational force
			temp.setTo(masses.get(a).getVelocity());
			temp.negate();
			masses.get(a).applyForce(VectorMath.multiply(temp, airFrictionConstant));	//The air friction

			if (masses.get(a).getPosition().y < groundHeight)		//Forces from the ground are applied if a mass collides with the ground
			{
												

				Vector3f v = new Vector3f(masses.get(a).getVelocity());		//A temporary Vector3D	//get the velocity
				v.y = 0;								//omit the velocity component in y direction

				//The velocity in y direction is ommited because we will apply a friction force to create 
				//a sliding effect. Sliding is parallel to the ground. Velocity in y direction will be used
				//in the absorption effect.
				temp.setTo(v);
				temp.negate();
				masses.get(a).applyForce(VectorMath.multiply(temp, groundFrictionConstant));		//ground friction force is applied
				
				
				v.setTo(masses.get(a).getVelocity());						//get the velocity
				v.x = 0;								//omit the x and z components of the velocity
				v.z = 0;								//we will use v in the absorption effect
				
				//above, we obtained a velocity which is vertical to the ground and it will be used in 
				//the absorption force

				if (v.y < 0)							//let's absorb energy only when a mass collides towards the ground
				{
					temp.setTo(v);
					temp.negate();
					masses.get(a).applyForce(VectorMath.multiply(temp, groundAbsorptionConstant));		//the absorption force is applied
				}
				//The ground shall repel a mass like a spring. 
				//By "Vector3D(0, groundRepulsionConstant, 0)" we create a vector in the plane normal direction 
				//with a magnitude of groundRepulsionConstant.
				//By (groundHeight - masses[a]->pos.y) we repel a mass as much as it crashes into the ground.
				//Vector3D force = Vector3D(0, groundRepulsionConstant, 0) * 
				//	(groundHeight - masses[a]->pos.y);
				temp.setTo(0, groundRepulsionConstant, 0);
				Vector3f force = new Vector3f(VectorMath.multiply(temp, (groundHeight - masses.get(a).getPosition().y)));				

				masses.get(a).applyForce(force);			//The ground repulsion force is applied
			}
				
		}


	}

	public void simulate(float dt)								//simulate(float dt) is overriden because we want to simulate 
														//the motion of the ropeConnectionPos
	{
		super.simulate(dt);						//the super class shall simulate the masses
		Vector3f temp = new Vector3f();
		temp = VectorMath.multiply(ropeConnectionVel, dt);
		ropeConnectionPos = VectorMath.add(ropeConnectionPos, temp);	//iterate the positon of ropeConnectionPos

		if (ropeConnectionPos.y < groundHeight)			//ropeConnectionPos shall not go under the ground
		{
			ropeConnectionPos.y = groundHeight;
			ropeConnectionVel.y = 0;
		}

		masses.get(0).setPosition(ropeConnectionPos);				//mass with index "0" shall position at ropeConnectionPos
		masses.get(0).setVelocity(ropeConnectionVel);				//the mass's velocity is set to be equal to ropeConnectionVel
	}

	public void setRopeConnectionVel(Vector3f ropeConnectionVel)	//the method to set ropeConnectionVel
	{
		this.ropeConnectionVel = ropeConnectionVel;
	}
	
	public float getGroundHeight()
	{
		return groundHeight;
	}

}