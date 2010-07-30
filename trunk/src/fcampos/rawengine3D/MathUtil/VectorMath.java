package fcampos.rawengine3D.MathUtil;

import static java.lang.Math.*;
import fcampos.rawengine3D.model.Model3d;
import fcampos.rawengine3D.model.Object3d;



/** Class for handling vector maths.
 */

public final class VectorMath
{



	/** VectorMath constructor.
	 */

	private VectorMath()
	{
	}



	/** Addition.
	 *  @param v1 The first vector.
	 *  @param v2 The second vector.
	 *  @return Put the result in a new vector.
	 */
	
	public final static Vector3f add(Vector3f v1, Vector3f v2)
	{
		return (new Vector3f(v1.x+ v2.x, v1.y + v2.y, v1.z + v2.z));
	}

	
	public final static Vector4f add(Vector4f v1, Vector4f v2)
	{
		return (new Vector4f(v1.x + v2.x, v1.y + v2.y, v1.z + v2.z, v1.w + v2.w));
	}

	/**
        Adds the specified (x, y, z) values to this vector.
    */
    public final static Vector3f add(Vector3f v1, float x, float y, float z) 
    {
             
        return (new Vector3f(v1.x + x, v1.y + y, v1.z + z  ));
    }
	

	public final static Vector4f add(Vector4f v1, float x, float y, float z, float w) 
    	{
             
        	return (new Vector4f(v1.x + x, v1.y + y, v1.z + z, v1.w + w));
    	}

	/** Substraction.
	 *  @param v1 The first vector.
	 *  @param v2 The second vector.
	 *  @return Put the result in a new vector.
	 */
	
	public final static Vector3f subtract(Vector3f v1, Vector3f v2)
	{
		return (new Vector3f(v1.x - v2.x, v1.y - v2.y, v1.z - v2.z));
	}



    public final static Vector3f subtract(Vector3f v1, float x, float y, float z) 
    {
        return (new Vector3f(v1.x - x, v1.y - y, v1.z - z  ));
    }


	public final static Vector4f subtract(Vector4f v1, Vector4f v2)
	{
		return (new Vector4f(v1.x - v2.x, v1.y - v2.y, v1.z - v2.z, v1.w - v2.w));
	}



    public final static Vector4f subtract(Vector4f v1, float x, float y, float z, float w) 
    {
        return (new Vector4f(v1.x - x, v1.y - y, v1.z - z, v1.w - w));
    }

	/** Multiplication.
	 *  @param v1 The first vector.
	 *  @param m Multiplication factor.
	 *  @return Put the result in a new vector.
	 */
	
	public final static Vector3f multiply(Vector3f v1, float m)
	{
		return (new Vector3f(v1.x * m, v1.y * m, v1.z * m));
	}

	

	public final static Vector4f multiply(Vector4f v1, float m)
	{
		return (new Vector4f(v1.x * m, v1.y * m, v1.z * m, v1.w * m));
	}

	/** Division.
	 *  @param v1 The first vector.
	 *  @param m Division factor.
	 *  @return Put the result in a new vector.
	 */
	
	public final static Vector3f divide(Vector3f v1, float m)
	{
		return (new Vector3f(v1.x / m, v1.y / m, v1.z / m));
	}

	

	public final static Vector4f divide(Vector4f v1, float m)
	{
		return (new Vector4f(v1.x / m, v1.y / m, v1.z / m, v1.w / m));
	}

	/** Calcul the cross product of 2 vectors.
	 *  @param v1 The first vector.
	 *  @param v2 The second vector.
	 *  @return Put the result in a new vector.
	 */
	
	public final static Vector3f cross_product(Vector3f v1, Vector3f v2)
	{
		Vector3f v3 = new Vector3f();

		v3.x = (v1.y * v2.z) - (v1.z * v2.y);
		v3.y = (v1.z * v2.x) - (v1.x * v2.z);
		v3.z = (v1.x * v2.y) - (v1.y * v2.x);

		return v3;
	}

	public final static Vector3f normal(Vector3f triangle[])					
	{	
		// Get 2 vectors from the polygon (2 sides), Remember the order!
		Vector3f vector1 = subtract(triangle[2], triangle[0]);
		Vector3f vector2 = subtract(triangle[1], triangle[0]);

		Vector3f normal = cross_product(vector1, vector2);		// Take the cross product of our 2 vectors to get a perpendicular vector

		// Now we have a normal, but it's at a strange length, so let's make it length 1.

		normalize(normal);						// Use our function we created to normalize the normal (Makes it a length of one)

		return normal;										// Return our normal at our desired length
	}
	
	//*Note* 
	//
	//Below are some math functions for calculating vertex normals.  We want vertex normals
	//because it makes the lighting look really smooth and life like.  You probably already
	//have these functions in the rest of your engine, so you can delete these and call
	//your own.  I wanted to add them so I could show how to calculate vertex normals.


	public final static void computeNormals(Model3d model)
	{
		Vector3f vector1 = new Vector3f();
		Vector3f vector2 = new Vector3f();
		Vector3f normal = new Vector3f();
		Vector3f[] poly = new Vector3f[3];

		// If there are no objects, we can skip this part
		if(model.getObject().size() <= 0)
			return;

		// What are vertex normals?  And how are they different from other normals?
		// Well, if you find the normal to a triangle, you are finding a "Face Normal".
		// If you give OpenGL a face normal for lighting, it will make your object look
		// really flat and not very round.  If we find the normal for each vertex, it makes
		// the smooth lighting look.  This also covers up blocky looking objects and they appear
		// to have more polygons than they do.    Basically, what you do is first
		// calculate the face normals, then you take the average of all the normals around each
		// vertex.  It's just averaging.  That way you get a better approximation for that vertex.

		// Go through each of the objects to calculate their normals
		for(int index = 0; index < model.getObject().size(); index++)
		{
			// Get the current object
			Object3d object = model.getObject(index);

			// Here we allocate all the memory we need to calculate the normals
			Vector3f[] normals		= new Vector3f[object.getNumFaces()];
			Vector3f[] tempNormals	= new Vector3f[object.getNumFaces()];
			object.setNumNormais(object.getNumVertices());

			// Go though all of the faces of this object
			for(int i=0; i < object.getNumFaces(); i++)
			{												
				// To cut down LARGE code, we extract the 3 points of this face
				poly[0] = object.getVertices(object.getFace(i).getVertices(0));
				poly[1] = object.getVertices(object.getFace(i).getVertices(1));
				poly[2] = object.getVertices(object.getFace(i).getVertices(2));

				// Now let's calculate the face normals (Get 2 vectors and find the cross product of those 2)

				vector1 = VectorMath.subtract(poly[2], poly[1]);				// Get the vector of the polygon (we just need 2 sides for the normal)
				vector2 = VectorMath.subtract(poly[2], poly[0]);				// Get a second vector of the polygon

				normal  = VectorMath.cross_product(vector1, vector2);		// Return the cross product of the 2 vectors (normalize vector, but not a unit vector)
				tempNormals[i] = new Vector3f(normal);					// Save the un-normalized normal for the vertex normals
				VectorMath.normalize(normal);				// Normalize the cross product to give us the polygons normal

				normals[i] = new Vector3f(normal);						// Assign the normal to the list of normals
			}

			//////////////// Now Get The Vertex Normals /////////////////

			Vector3f sum = new Vector3f();
			int shared=0;

			for (int i = 0; i < object.getNumVertices(); i++)			// Go through all of the vertices
			{
				for (int j = 0; j < object.getNumFaces(); j++)	// Go through all of the triangles
				{												// Check if the vertex is shared by another face
					if (object.getFace(j).getVertices(0) == i || 
						object.getFace(j).getVertices(1) == i || 
						object.getFace(j).getVertices(2) == i)
					{
						sum = VectorMath.add(sum, tempNormals[j]);			// Add the un-normalized normal of the shared face
						shared++;								// Increase the number of shared triangles
					}
				}      
				
				// Get the normal by dividing the sum by the shared.  We negate the shared so it has the normals pointing out.
				Vector3f tNormal = VectorMath.divide(sum, (float)(-shared));
				
				VectorMath.normalize(tNormal);	
				
				object.setNormal(tNormal, i);
				
				// Normalize the normal for the final vertex normal
				
				sum.setZero();									// Reset the sum
				shared = 0;										// Reset the shared
			}
		
		}
	}

	public final static float getDotProduct(Vector3f v1, Vector3f v2) 
	{
        return (v1.x*v2.x + v1.y*v2.y + v1.z*v2.z);
    }

	public final static float getDotProduct(Vector4f v1, Vector4f v2) {
        return (v1.x*v2.x + v1.y*v2.y + v1.z*v2.z + v1.w*v2.w);
    }

	
/////////////////////////////////// ANGLE BETWEEN VECTORS \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This checks to see if a point is inside the ranges of a polygon
	/////
	/////////////////////////////////// ANGLE BETWEEN VECTORS \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*

	public final static double angleBetweenVectors(Vector3f Vector1, Vector3f Vector2)
	{							
		// Remember, above we said that the Dot Product of returns the cosine of the angle
		// between 2 vectors?  Well, that is assuming they are unit vectors (normalize vectors).
		// So, if we don't have a unit vector, then instead of just saying  arcCos(DotProduct(A, B))
		// We need to divide the dot product by the magnitude of the 2 vectors multiplied by each other.
		// Here is the equation:   arc cosine of (V . W / || V || * || W || )
		// the || V || means the magnitude of V.  This then cancels out the magnitudes dot product magnitudes.
		// But basically, if you have normalize vectors already, you can forget about the magnitude part.

		// Get the dot product of the vectors
		float dotProduct = getDotProduct(Vector1, Vector2);				

		// Get the product of both of the vectors magnitudes
		float vectorsMagnitude = magnitude(Vector1) * magnitude(Vector2) ;

		// Get the arc cosine of the (dotProduct / vectorsMagnitude) which is the angle in RADIANS.
		// (IE.   PI/2 radians = 90 degrees      PI radians = 180 degrees    2*PI radians = 360 degrees)
		// To convert radians to degress use this equation:   radians * (PI / 180)
		// TO convert degrees to radians use this equation:   degrees * (180 / PI)
		double angle = Math.acos( dotProduct / vectorsMagnitude );

		// Here we make sure that the angle is not a -1.#IND0000000 number, which means indefinate.
		// acos() thinks it's funny when it returns -1.#IND0000000.  If we don't do this check,
		// our collision results will sometimes say we are colliding when we aren't.  I found this
		// out the hard way after MANY hours and already wrong written tutorials :)  Usually
		// this value is found when the dot product and the maginitude are the same value.
		// We want to return 0 when this happens.
		if(angle == Float.NaN)
			return 0;
		
		// Return the angle in radians
		return( angle );
	}

    /**
        Sets this vector to the cross product of the two
        specified vectors. Either of the specified vectors can
        be this vector.
    */
    public final static Vector4f cross_product(Vector4f v1, Vector4f v2, Vector4f v3) {

        Vector4f v4 = new Vector4f();
                
       float x = (v1.y * v2.z * v3.w) + (v1.z * v2.w * v3.y) + (v1.w * v2.y * v3.z) - 
           	 (v1.y * v2.w * v3.z) - (v1.z * v2.y * v3.w) - (v1.w * v2.z * v3.y);

       float y = (v1.x * v2.w * v3.z) + (v1.z * v2.x * v3.w) + (v1.w * v2.z * v3.x) -
   	   	 (v1.x * v2.z * v3.w) - (v1.z * v2.w * v3.x) - (v1.w * v2.x * v3.z);

       float z = (v1.x * v2.y * v3.w) + (v1.y * v2.w * v3.x) + (v1.w * v2.x * v3.y) -
                 (v1.x * v2.w * v3.y) - (v1.y * v2.x * v3.w) - (v1.w * v2.y * v3.x);

       float w = (v1.x * v2.z * v3.y) + (v1.y * v2.x * v3.z) + (v1.z * v2.y * v3.x) -
     		 (v1.x * v2.y * v3.z) - (v1.y * v2.z * v3.x) - (v1.z * v2.x * v3.y);
        
        v4.setTo(x, y, z, w);
	return v4;
        
    }
    
    public final static float distance(Vector3f v1, Vector3f v2)
    {
    	// This is the classic formula used in beginning algebra to return the
    	// distance between 2 points.  Since it's 3D, we just add the z dimension:
    	// 
    	// Distance = sqrt(  (P2.x - P1.x)^2 + (P2.y - P1.y)^2 + (P2.z - P1.z)^2 )
    	//
    	Vector3f res = VectorMath.subtract(v2, v1);
    	
    	float distance = VectorMath.magnitude(res);

    	// Return the distance between the 2 points
    	return distance;
    }

	/** Returns the magnitude (length) of a vector.
	 *  @param v1 The vector.
	 *  @return The magnitude (length) of a vector.
	 */
	
	public final static float magnitude(Vector3f v1)
	{
		float r = (float)sqrt( (v1.x * v1.x) + (v1.y * v1.y) + (v1.z * v1.z) );
		if (r <= 0.00001)
			return 0f;
		else
			return r;
	}

	public final static float magnitude(Vector4f v1)
	{
		return (float)sqrt( (v1.x * v1.x) + (v1.y * v1.y) +
				    (v1.z * v1.z) + (v1.w * v1.w));
	}


	/** Normalize a vector.
	 *  @param v1 The vector.
	 */
	
	public final static void normalize(Vector3f v1)
	{
		float f = magnitude(v1);

		v1.x /= f;
		v1.y /= f;
		v1.z /= f;
	}
	
	public final static void normalize(Vector4f v1)
	{
		float f = magnitude(v1);

		v1.x /= f;
		v1.y /= f;
		v1.z /= f;
		v1.w /= f;
	}

	/**
        Checks if this Vector3f is equal to the specified
        x, y, and z coordinates.
    */
    public final static boolean equals(Vector3f v1, float x, float y, float z) {
        return (v1.x == x && v1.y == y && v1.z == z);
    }
    
    public final static boolean equals(Vector3f v1, Vector3f v2) {
        return (v1.x == v2.x && v1.y == v2.y && v1.z == v2.z);
    }

    public final static boolean equals(Vector4f v1, Vector4f v2) {
        return (v1.x == v2.x && v1.y == v2.y && v1.z == v2.z && v1.w == v2.w);
    
    }

    public boolean equals(Vector4f v1, float x, float y, float z, float w) {
        return (v1.x == x && v1.y == y && v1.z == z && v1.w == w);
    }
}