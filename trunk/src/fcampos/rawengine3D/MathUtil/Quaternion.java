package fcampos.rawengine3D.MathUtil;



public class Quaternion extends Vector4f {


	private static final float PI_VALUE = 3.141592654f;
	public static final float EPSILON	= 0.005f;		// error tolerance for check

//public float x, y, z, w;


     public Quaternion()
         {
            // Initialize each member variables.
	    super(0.0f, 0.0f, 0.0f, 1.0f);		
            //x = y = z = 0.0f;
            //w = 1.0f;
         }
	   
	   public Quaternion(float xAxis, float yAxis, float zAxis, float wAxis) 
	      {
            // Initialize each member variables.
	    super(xAxis, yAxis, zAxis, wAxis);
	      }

      public Quaternion(Quaternion q)
         {
            // This will make this quaternion equal to q.
            this(q.x, q.y, q.z, q.w);
         }

      public final static Quaternion multiply(Quaternion u, Quaternion q)
         {
            return new Quaternion(u.w * q.x + u.x * q.w + u.y * q.z - u.z * q.y,
                               	  u.w * q.y - u.x * q.z + u.y * q.w + u.z * q.x,
                                  u.w * q.z + u.x * q.y - u.y * q.x + u.z * q.w,
                                  u.w * q.w - u.x * q.x - u.y * q.y - u.z * q.z);
         }

	  
      public Quaternion add(Quaternion q)
         {
            Quaternion result = new Quaternion();

            Vector4f v1 = new Vector4f();
            Vector4f v2 = new Vector4f();
            Vector3f cross = new Vector3f();
            Vector4f v3 = new Vector4f();
            v1.setTo(x, y, z, 0.0f);
            v2.setTo(q.x, q.y, q.z, 0.0f);

            cross = VectorMath.cross_product((Vector3f)v2, (Vector3f)v1);
            v1 = VectorMath.multiply(v1, q.w);
            v2 = VectorMath.multiply(v2, w);
            
            v3 = VectorMath.add(v1, v2);
            v3 = VectorMath.add((Vector4f)cross, v3);

            v3.w = w * q.w - (x * q.x + y * q.y + z * q.z);
            
            result.x = v3.x; 
            result.y = v3.y; 
            result.z = v3.z; 
            result.w = v3.w;

            return result;
         }

      public void createQuatFromAxis(float degree, float x, float y, float z)
      {
    	// This function takes an angle and an axis of rotation, then converts
    		// it to a quaternion.  An example of an axis and angle is what we pass into
    		// glRotatef().  That is an axis angle rotation.  It is assumed an angle in 
    		// degrees is being passed in.  Instead of using glRotatef(), we can now handle
    		// the rotations our self.

    		// The equations for axis angle to quaternions are such:

    		// w = cos( theta / 2 )
    		// x = X * sin( theta / 2 )
    		// y = Y * sin( theta / 2 )
    		// z = Z * sin( theta / 2 )

    		// First we want to convert the degrees to radians 
    		// since the angle is assumed to be in radians
    		float angle = (float)((degree / 180.0f) * PI_VALUE);

    		// Here we calculate the sin( theta / 2) once for optimization
    		float result = (float)Math.sin( angle / 2.0f );
    			
    		// Calcualte the w value by cos( theta / 2 )
    		this.w = (float)Math.cos( angle / 2.0f );

    		// Calculate the x, y and z of the quaternion
    		this.x = (float)(x * result);
    		this.y = (float)(y * result);
    		this.z = (float)(z * result);
      }
      
      public void createQuatFromAxis(Vector4f a, float radians)
         {
            float sine = 0.0f;
            
            radians = radians * 0.5f;
            sine = (float)Math.sin(radians);
            
            VectorMath.normalize(a);

            x = a.x; 
            y = a.y; 
            z = a.z;

            x *= sine;
            y *= sine;
            z *= sine;
            w = (float)Math.cos(radians);
         }
      
      public void eulerToQuat(float angleX, float angleY, float angleZ)
      {
      	double	halfX, halfY, halfZ;		// temp half euler angles
      	double	cosRoll, cosPitch, cosYaw, sinRoll, sinPitch, sinYaw, cpcy, spsy;		// temp vars in roll,pitch yaw

      	halfX = Math.toRadians(angleX) / 2.0;	// convert to rads and half them
      	halfY = Math.toRadians(angleY) / 2.0;
      	halfZ = Math.toRadians(angleZ) / 2.0;

      	cosRoll = Math.cos(halfX);
      	cosPitch = Math.cos(halfY);
      	cosYaw = Math.cos(halfZ);

      	sinRoll = Math.sin(halfX);
      	sinPitch = Math.sin(halfY);
      	sinYaw = Math.sin(halfZ);

      	cpcy = cosPitch * cosYaw;
      	spsy = sinPitch * sinYaw;

      	this.w = (float) (cosRoll * cpcy + sinRoll * spsy);

      	this.x = (float) (sinRoll * cpcy - cosRoll * spsy);
      	this.y = (float) (cosRoll * sinPitch * cosYaw + sinRoll * cosPitch * sinYaw);
      	this.z = (float) (cosRoll * cosPitch * sinYaw - sinRoll * sinPitch * cosYaw);

      	normalize();
      	
      	
      }	
      
      public float getAxisAngle(Vector3f v)
      {
      	double	temp_angle;		// temp angle
      	float angle = 0;
      	float	scale;			// temp vars

      	temp_angle = Math.acos(w);

      	//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
      	// Another version where scale is sqrt (x2 + y2 + z2)
      	//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
      	scale = (float)Math.sqrt(x*x + y*y + z*z);
//      	scale = (float)sin(temp_angle);

      	assert(0 <= temp_angle);		// make sure angle is 0 - PI
      	assert(PI_VALUE >= temp_angle);

      	if (floatEquality(0.0f, scale))		// angle is 0 or 360 so just simply set axis to 0,0,1 with angle 0
      	{
      		angle = 0.0f;

      		v.setTo(0.0f, 0.0f, 1.0f);		// any axis will do
      	}
      	else
      	{
      		angle = (float)(temp_angle * 2.0);		// angle in radians

      		v.setTo((x / scale), (y / scale), (z / scale));
      		VectorMath.normalize(v);

      		assert(0.0f <= angle);			// make sure rotation around axis is 0 - 360
      		assert(2*PI_VALUE >= angle);
      		assert(isUnit(v));				// make sure a unit axis comes up
      	}

      	return angle;
      }	// end void GetAxisAngle(..)


      public Vector3f getEulerAngles()
      {
      	Matrix4f matrix = new Matrix4f();			// temp matrix

      	createMatrix(matrix);		// get matrix of this quaternion

      	Vector3f v = matrix.getEulerAngles();

      	return v;
      }	// end void GetEulerAngles(.,)


      public float length()
         {
            return (float)Math.sqrt(x * x + y * y + z * z + w * w);
         }


      void normalize()
         {
            float len = length();
            len = 1 / len;

            x = x * len;
            y = y * len;
            z = z * len;
            w = w * len;
         }


      public Quaternion conjugate()
      { 
          return new Quaternion(-x, -y, -z, w);
      }
      
      
      
    
      public boolean isEqual(Quaternion q1)
      {
    	  if(q1.x == x && q1.y == y && q1.z == z && q1.w == w) 
    	  {
    			return true;
    	  }else{
    		  return false;
    	  }
    	  
      }
      
      public static boolean isEqual(Quaternion q1, Quaternion q2)
      {
    	  if(q1.x == q2.x && q1.y == q2.y && q1.z == q2.z && q1.w == q2.w) 
    	  {
    			return true;
    	  }else{
    		  return false;
    	  }
    	  
      }
      
      public Quaternion crossProduct(Quaternion q)
         {
            Quaternion crossProduct = new Quaternion();

            crossProduct.x = w * q.x + x * q.w + y * q.z - z * q.y;
            crossProduct.y = w * q.y + x * q.z + y * q.w - z * q.x;
            crossProduct.z = w * q.z + x * q.y + y * q.x - z * q.w;
            crossProduct.w = w * q.w - x * q.x - y * q.y - z * q.z;

            return crossProduct;
         }

	   public void rotatef(float degree, float xAxis, float yAxis, float zAxis)
         {
            // Normalize if we have to.
            if((xAxis + yAxis + zAxis) != 1)
            {
               float length = (float)Math.sqrt(xAxis * xAxis + yAxis * yAxis + zAxis * zAxis);
               xAxis /= length; yAxis /= length; zAxis /= length;
            }
            
            createQuatFromAxis(degree,  xAxis,  yAxis,  zAxis);
            /*
            // Convert the angle degrees into radians.
            float angle = (PI_VALUE * (amount / 180));

            // Call this once for optimization.
	         float sine = (float)Math.sin(angle / 2.0f);

            // Create the quaternion.
	         x = xAxis * sine;
	         y = yAxis * sine;
	         z = zAxis * sine;
             w = (float)Math.cos(angle / 2.0f);
             */
         }

      public void rotationRadiansf(double X, double Y, double Z)
         {
            double cosX, cosY, cosZ;
            double sinX, sinY, sinZ;
            double cosXY, sinXY;

            sinX = Math.sin(X * 0.5);
            cosX = Math.cos(X * 0.5);
            
            sinY = Math.sin(Y * 0.5);
            cosY = Math.cos(Y * 0.5);
            
            sinZ = Math.sin(Z * 0.5);
            cosZ = Math.cos(Z * 0.5);

            cosXY = cosX * cosY;
            sinXY = sinX * sinY;

            x = (float)(sinX * cosY * cosZ - cosX * sinY * sinZ);
            y = (float)(cosX * sinY * cosZ + sinX * cosY * sinZ);
            z = (float)(cosXY * sinZ - sinXY * cosZ);
            w = (float)(cosXY * cosZ + sinXY * sinZ); 
         }
      
  ///////////////////////////////// CREATE FROM MATRIX \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
  /////
  /////	This creates a quaternion from a 3x3 or a 4x4 matrix, depending on rowColumnCount
  /////
  ///////////////////////////////// CREATE FROM MATRIX \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*

  public void createFromMatrix(float[] pTheMatrix, int rowColumnCount)
  {
  	// Make sure the matrix has valid memory and it's not expected that we allocate it.
  	// Also, we do a check to make sure the matrix is a 3x3 or a 4x4 (must be 3 or 4).
  	if(pTheMatrix == null || ((rowColumnCount != 3) && (rowColumnCount != 4))) return;

  	// This function is used to take in a 3x3 or 4x4 matrix and convert the matrix
  	// to a quaternion.  If rowColumnCount is a 3, then we need to convert the 3x3
  	// matrix passed in to a 4x4 matrix, otherwise we just leave the matrix how it is.
  	// Since we want to apply a matrix to an OpenGL matrix, we need it to be 4x4.

  	// Point the matrix pointer to the matrix passed in, assuming it's a 4x4 matrix
  	Matrix4f pMatrix = null;

  	// Create a 4x4 matrix to convert a 3x3 matrix to a 4x4 matrix (If rowColumnCount == 3)
  	//float m4x4[16] = {0};

  	// If the matrix is a 3x3 matrix (which it is for Quake3), then convert it to a 4x4
  	if(rowColumnCount == 3)
  	{
  		pMatrix = new Matrix4f();
  		// Set the 9 top left indices of the 4x4 matrix to the 9 indices in the 3x3 matrix.
  		// It would be a good idea to actually draw this out so you can visualize it.
  		pMatrix.matrix[0]  = pTheMatrix[0];	pMatrix.matrix[1]  = pTheMatrix[1];	pMatrix.matrix[2]  = pTheMatrix[2];	
  		pMatrix.matrix[4]  = pTheMatrix[3];	pMatrix.matrix[5]  = pTheMatrix[4];	pMatrix.matrix[6]  = pTheMatrix[5];
  		pMatrix.matrix[8]  = pTheMatrix[6];	pMatrix.matrix[9]  = pTheMatrix[7];	pMatrix.matrix[10] = pTheMatrix[8];

  		// Since the bottom and far right indices are zero, set the bottom right corner to 1.
  		// This is so that it follows the standard diagonal line of 1's in the identity matrix.
  		pMatrix.matrix[15] = 1;
  		// Set the matrix pointer to the first index in the newly converted matrix
  		//pMatrix = &m4x4[0];
  	}
  	
  	if(rowColumnCount == 4)
  	{
  		pMatrix = new Matrix4f(pTheMatrix);
  	}

  	// The next step, once we made sure we are dealing with a 4x4 matrix, is to check the
  	// diagonal of the matrix.  This means that we add up all of the indices that comprise
  	// the standard 1's in the identity matrix.  If you draw out the identity matrix of a
  	// 4x4 matrix, you will see that they 1's form a diagonal line.  Notice we just assume
  	// that the last index (15) is 1 because it is not effected in the 3x3 rotation matrix.

  	// Find the diagonal of the matrix by adding up it's diagonal indices.
  	// This is also known as the "trace", but I will call the variable diagonal.
  	float diagonal = pMatrix.matrix[0] + pMatrix.matrix[5] + pMatrix.matrix[10] + 1;
  	float scale = 0.0f;

  	// Below we check if the diagonal is greater than zero.  To avoid accidents with
  	// floating point numbers, we substitute 0 with 0.00000001.  If the diagonal is
  	// great than zero, we can perform an "instant" calculation, otherwise we will need
  	// to identify which diagonal element has the greatest value.  Note, that it appears
  	// that %99 of the time, the diagonal IS greater than 0 so the rest is rarely used.

  	// If the diagonal is greater than zero
  	if(diagonal > 0.00000001)
  	{
  		//System.out.println("if");
  		// Calculate the scale of the diagonal
  		scale = (float) (Math.sqrt(diagonal ) * 2);
  		
  		// Calculate the x, y, z and w of the quaternion through the respective equation
  		x = ( pMatrix.matrix[9] - pMatrix.matrix[6] ) / scale;
  		y = ( pMatrix.matrix[2] - pMatrix.matrix[8] ) / scale;
  		z = ( pMatrix.matrix[4] - pMatrix.matrix[1] ) / scale;
  		w = 0.25f * scale;
  		//System.out.println("6: "+ pMatrix.matrix[6]);
  		//System.out.println("9: " + pMatrix.matrix[9]);
  		
  	}
  	else 
  	{
  		System.out.println("primeiro else");
  		// If the first element of the diagonal is the greatest value
  		if ( pMatrix.matrix[0] > pMatrix.matrix[5] && pMatrix.matrix[0] > pMatrix.matrix[10] )  
  		{	
  			// Find the scale according to the first element, and double that value
  			scale  = (float) (Math.sqrt( 1.0f + pMatrix.matrix[0] - pMatrix.matrix[5] - pMatrix.matrix[10] ) * 2.0f);

  			// Calculate the x, y, x and w of the quaternion through the respective equation
  			x = 0.25f * scale;
  			y = (pMatrix.matrix[4] + pMatrix.matrix[1] ) / scale;
  			z = (pMatrix.matrix[2] + pMatrix.matrix[8] ) / scale;
  			w = (pMatrix.matrix[9] - pMatrix.matrix[6] ) / scale;	
  		} 
  		// Else if the second element of the diagonal is the greatest value
  		else if ( pMatrix.matrix[5] > pMatrix.matrix[10] ) 
  		{
  			System.out.println("segundo else");
  			// Find the scale according to the second element, and double that value
  			scale  = (float) (Math.sqrt( 1.0f + pMatrix.matrix[5] - pMatrix.matrix[0] - pMatrix.matrix[10] ) * 2.0f);
  			
  			// Calculate the x, y, x and w of the quaternion through the respective equation
  			x = (pMatrix.matrix[4] + pMatrix.matrix[1] ) / scale;
  			y = 0.25f * scale;
  			z = (pMatrix.matrix[9] + pMatrix.matrix[6] ) / scale;
  			w = (pMatrix.matrix[2] - pMatrix.matrix[8] ) / scale;
  		} 
  		// Else the third element of the diagonal is the greatest value
  		else 
  		{	
  			System.out.println("terceiro else");
  			// Find the scale according to the third element, and double that value
  			scale  = (float) (Math.sqrt( 1.0f + pMatrix.matrix[10] - pMatrix.matrix[0] - pMatrix.matrix[5] ) * 2.0f);

  			// Calculate the x, y, x and w of the quaternion through the respective equation
  			x = (pMatrix.matrix[2] + pMatrix.matrix[8] ) / scale;
  			y = (pMatrix.matrix[9] + pMatrix.matrix[6] ) / scale;
  			z = 0.25f * scale;
  			w = (pMatrix.matrix[4] - pMatrix.matrix[1] ) / scale;
  		}
  	}
  }

	   public void createMatrix(Matrix4f m)
         {
		// This function is a necessity when it comes to doing almost anything
			// with quaternions.  Since we are working with OpenGL, which uses a 4x4
			// homogeneous matrix, we need to have a way to take our quaternion and
			// convert it to a rotation matrix to modify the current model view matrix.
			// We pass in a 4x4 matrix, which is a 1D array of 16 floats.  This is how OpenGL
			// allows us to pass in a matrix to glMultMatrixf(), so we use a single dimensioned array.
			// After about 300 trees murdered and 20 packs of chalk depleted, the
			// mathematicians came up with these equations for a quaternion to matrix conversion:
			//
			//     ¦        2     2								 				 ¦
		    //     ¦ 1 - (2y  + 2z )   2xy + 2zw         2xz - 2yw			0	 ¦
		   	//     ¦									 						 ¦
		   	//     ¦                          2     2							 ¦
		    // M = ¦ 2xy - 2zw         1 - (2x  + 2z )   2zy + 2xw			0	 ¦
		   	//     ¦									 						 ¦
		    //     ¦                                            2     2			 ¦
		   	//     ¦ 2xz + 2yw         2zy - 2xw         1 - (2x  + 2y )	0    ¦
		   	//     ¦									 						 ¦
			//     ¦									 						 ¦
			//     ¦ 0						0		 			0			1	 |													 ¦
			//     ¦									 						 ¦
			// 
			// This is of course a 4x4 matrix.  Notice that a rotational matrix can just
			// be a 3x3 matrix, but since OpenGL uses a 4x4 matrix, we need to conform to the man.
			// Remember that the identity matrix of a 4x4 matrix has a diagonal of 1's, where
			// the rest of the indices are 0.  That is where we get the 0's lining the sides, and
			// the 1 at the bottom-right corner.  Since OpenGL matrices are row by column, we fill
			// in our matrix accordingly below.
	         

	         // Calculate the first row.
            m.matrix[0]  = 1.0f - 2.0f * ( y * y + z * z );  
            m.matrix[1]  = 2.0f * ( x * y - w * z );  
            m.matrix[2]  = 2.0f * ( x * z + w * y );  
            m.matrix[3]  = 0.0f;  

            m.matrix[4]  = 2.0f * ( x * y + z * w );  
            m.matrix[5]  = 1.0f - 2.0f * ( x * x + z * z );  
            m.matrix[6]  = 2.0f * ( y * z - w * x );  
            m.matrix[7]  = 0.0f;  

            m.matrix[8]  = 2.0f * ( x * z - w * y );  
            m.matrix[9]  = 2.0f * ( y * z + w * x );  
            m.matrix[10] = 1.0f - 2.0f * ( x * x + y * y );  
            m.matrix[11] = 0.0f;  

            m.matrix[12] = 0;  
            m.matrix[13] = 0;  
            m.matrix[14] = 0;  
            m.matrix[15] = 1.0f;
         }
/*
      public void Slerp(Quaternion q1, Quaternion q2, float t)
      {
         float cosTheta = 0.0f;
         float sinTheta = 0.0f;
         float beta = 0.0f;
         float[] q2Array = new float[4];

         // Temporary array to hold second quaternion.
         q2Array[0] = q2.x; q2Array[1] = q2.y; q2Array[2] = q2.z; q2Array[3] = q2.w;

         cosTheta = q1.x * q2.x + q1.y * q2.y + q1.z * q2.z + q1.w * q2.w;

         if(cosTheta < 0.0f)
            {
               // Flip sigh if so.
               q2Array[0] = -q2Array[0]; q2Array[1] = -q2Array[1];
               q2Array[2] = -q2Array[2]; q2Array[3] = -q2Array[3];
               cosTheta = -cosTheta;
            }

         beta = 1.0f - t;

         if(1.0f - cosTheta > 0.001f)
            {
               // We are using spherical interpolation.
               cosTheta = (float)Math.acos(cosTheta);
               sinTheta = 1.0f / (float)Math.sin(cosTheta);
               beta = (float)Math.sin(cosTheta * beta) * sinTheta;
               t = (float)Math.sin(cosTheta * t) * sinTheta;
            }

         // Interpolation.
         x = beta * q1.x + t * q2Array[0];
         y = beta * q1.y + t * q2Array[1];
         z = beta * q1.z + t * q2Array[2];
         w = beta * q1.w + t * q2Array[3];
      }
  */    

	   public void slerp(Quaternion q1, Quaternion q2, float t)
	   {
		// This function is the milk and honey of our quaternion code, the rest of
			// the functions are an appendage to what is done here.  Everyone understands
			// the terms, "matrix to quaternion", "quaternion to matrix", "create quaternion matrix",
			// "quaternion multiplication", etc.. but "SLERP" is the stumbling block, even a little 
			// bit after hearing what it stands for, "Spherical Linear Interpolation".  What that
			// means is that we have 2 quaternions (or rotations) and we want to interpolate between 
			// them.  The reason what it's called "spherical" is that quaternions deal with a sphere.  
			// Linear interpolation just deals with 2 points primarily, where when dealing with angles
			// and rotations, we need to use sin() and cos() for interpolation.  If we wanted to use
			// quaternions for camera rotations, which have much more instant and jerky changes in 
			// rotations, we would use Spherical-Cubic Interpolation.  The equation for SLERP is this:
			//
			// q = (((q1.q2)^-1)^t)q1
			//
			// Go here for an a detailed explanation and proofs of this equation:
			//
			// http://www.magic-software.com/Documentation/quat.pdf
			//
			// Now, Let's code it
	        	        	        
	         if(isEqual(q1, q2))
	         {
	        	 x = q1.x;
	        	 y = q1.y;
	        	 z = q1.z;
	        	 w = q1.w;
	         }

	         // Temporary array to hold second quaternion.
	       
	        float cosTheta = q1.x * q2.x + q1.y * q2.y + q1.z * q2.z + q1.w * q2.w;

	        if(cosTheta < 0.0f)
	        {
	               // Flip sigh if so.
	           q2 = new Quaternion(-q2.x, -q2.y, -q2.z, -q2.w);
	           cosTheta = -cosTheta;
	        }

	        	         
	      // Set the first and second scale for the interpolation
	     	float scale0 = 1.0f - t;
	     	float scale1 = t;
	     	
	     	// Next, we want to actually calculate the spherical interpolation.  Since this
	    	// calculation is quite computationally expensive, we want to only perform it
	    	// if the angle between the 2 quaternions is large enough to warrant it.  If the
	    	// angle is fairly small, we can actually just do a simpler linear interpolation
	    	// of the 2 quaternions, and skip all the complex math.  We create a "delta" value
	    	// of 0.1 to say that if the cosine of the angle (result of the dot product) between
	    	// the 2 quaternions is smaller than 0.1, then we do NOT want to perform the full on 
	    	// interpolation using.  This is because you won't really notice the difference.

	    	// Check if the angle between the 2 quaternions was big enough to warrant such calculations
	        if(1.0f - cosTheta > 0.1f)
	        {
	               // We are using spherical interpolation.
	               float theta = (float)Math.acos(cosTheta);
	               float sinTheta = (float)Math.sin(theta);
	               scale0 = (float)Math.sin(( 1 - t ) * theta) / sinTheta;
	               scale1 = (float)Math.sin(( t * theta)) / sinTheta;
	         }

	         // Interpolation.
	         x = (scale0 * q1.x) + (scale1 * q2.x);
	         y = (scale0 * q1.y) + (scale1 * q2.y);
	         z = (scale0 * q1.z) + (scale1 * q2.z);
	         w = (scale0 * q1.w) + (scale1 * q2.w);
	         
	        
	   }
	   
      private boolean floatEquality(float x, float v)
      {
    	  return ( ((v) - EPSILON) < (x) && (x) < ((v) + EPSILON) );		// float equality test
      }
      
      private boolean isUnit(Vector3f v)
      {
      	return(floatEquality(1.0f, VectorMath.magnitude(v)) );
      }	// end int IsUnit()

	                        
}