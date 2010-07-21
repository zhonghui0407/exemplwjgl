package fcampos.rawengine3D.fisica;

import fcampos.rawengine3D.MathUtil.*;

public class Mass
{

	private float mass;									// The mass value
	private Vector3f position;								// Position in space
	private Vector3f velocity;
	private Vector3f force;								// Force applied on this mass at an instance
	

	public Mass(float mass)								// Constructor
	{
		setMass(mass);
		position = new Vector3f();
		velocity = new Vector3f();
		force = new Vector3f();
		
	}

	/*
	  void applyForce(Vector3D force) method is used to add external force to the mass. 
	  At an instance in time, several sources of force might affect the mass. The vector sum 
	  of these forces make up the net force applied to the mass at the instance.
	*/
	public void applyForce(Vector3f force)
	{
		this.force.setTo(VectorMath.add(this.force, force));					// The external force is added to the force of the mass
	}

	/*
	  void init() method sets the force values to zero
	*/
	public void init()
	{
			
		force.setZero();
		
	}

	/*
	  void simulate(float dt) method calculates the new velocity and new position of 
	  the mass according to change in time (dt). Here, a simulation method called
	  "The Euler Method" is used. The Euler Method is not always accurate, but it is 
	  simple. It is suitable for most of physical simulations that we know in common 
	  computer and video games.
	*/
	public void simulate(float dt)
	{
		//vel += (force / m) * dt;				// Change in velocity is added to the velocity.
		Vector3f temp = new Vector3f();
											// The change is proportinal with the acceleration (force / m) and change in time
		
		temp.setTo(VectorMath.divide(force, mass));
		temp.setTo(VectorMath.multiply(temp, dt));
		velocity = VectorMath.add(velocity, temp);
		
		temp.setZero();
		
		temp.setTo(VectorMath.multiply(velocity, dt));
		position.setTo(VectorMath.add(position, temp));
		
		temp.setZero();
		//pos += vel * dt;						// Change in position is added to the position.
												// Change in position is velocity times the change in time
	}

	/**
	 * @param mass the mass to set
	 */
	public void setMass(float mass) {
		this.mass = mass;
	}

	/**
	 * @return the mass
	 */
	public float getMass() {
		return mass;
	}

	/**
	 * @param position the position to set
	 */
	public void setPosition(Vector3f position) 
	{
		this.position.setTo(position);
	}
	
	/**
	 * @param position the position to set
	 */
	public void setPosition(float x, float y, float z) 
	{
		this.position.setTo(x, y, z);
	}

	/**
	 * @return the position
	 */
	public Vector3f getPosition() {
		return position;
	}

	/**
	 * @param velocity the velocity to set
	 */
	public void setVelocity(Vector3f velocity) {
		this.velocity = velocity;
	}
	
	/**
	 * @param velocity the velocity to set
	 */
	public void setVelocity(float x, float y, float z) 
	{
		this.velocity.setTo(x, y, z);
	}

	/**
	 * @return the velocity
	 */
	public Vector3f getVelocity() {
		return velocity;
	}

}