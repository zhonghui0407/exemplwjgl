package fcampos.rawengine3D.MathUtil;



public class Quaternion extends Vector4f {


private static final float PI_VALUE = 3.141592654f;
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

      public static Quaternion multiply(Quaternion u, Quaternion q)
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
		   	//     ¦ 2xz + 2yw         2yz - 2xw         1 - (2x  + 2y )	0    ¦
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
            m.matrix[0]  = 1.0f - 2.0f * (y * y + z * z); 
            m.matrix[1]  = 2.0f * (x * y + z * w);
            m.matrix[2]  = 2.0f * (x * z - y * w);
            m.matrix[3]  = 0.0f;  

            m.matrix[4]  = 2.0f * (x * y - z * w);  
            m.matrix[5]  = 1.0f - 2.0f * (x * x + z * z); 
            m.matrix[6]  = 2.0f * (z * y + x * w);  
            m.matrix[7]  = 0.0f;  

            m.matrix[8]  = 2.0f * (x * z + y * w);
            m.matrix[9]  = 2.0f * (y * z - x * w);
            m.matrix[10] = 1.0f - 2.0f * (x * x + y * y);  
            m.matrix[11] = 0.0f;  

            m.matrix[12] = 0;  
            m.matrix[13] = 0;  
            m.matrix[14] = 0;  
            m.matrix[15] = 1.0f;
         }

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

	                         // x, y , z, and w axis.
}