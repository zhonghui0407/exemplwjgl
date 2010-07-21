package fcampos.rawengine3D.teste;

public class Entity {
	// Location.
	public float x, y, z;
	// Velocity.
	public float fVelX, fVelY, fVelZ;
	// Minimum Velocity.
	public float fMinVel;
	// Max Velocity.
	public float fMaxVel;
	// Angle/Direction.
	public float fAngle;
	// Acceleration Rate.
	public float fAccel;
	// Specific to our Ball, it's radius.
	public float fRadius;
	// Yaw or turning rate.
	public float fTurnRate;
	
	public Entity(float x, float y, float z, float fVelX, float fVelY, float fVelZ,
				  float fMinVel,float fMaxVel, float fAngle, float fAccel,
				  float fRadius, float fTurnRate){
		
		this.x = x;
		this.y = y;
		this.y = z;
		this.fVelX = fVelX;
		this.fVelY = fVelY;
		this.fVelZ = fVelZ;
		this.fMinVel = fMinVel;
		this.fMaxVel = fMaxVel;
		this.fAngle = fAngle;
		this.fAccel = fAccel;
		this.fRadius = fRadius;
		this.fTurnRate = fTurnRate;
		
		
		
		
	}
}
