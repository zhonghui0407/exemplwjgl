package fcampos.rawengine3D.resource;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import fcampos.rawengine3D.MathUtil.*;

public final class Conversion {

	
	private static final int SIZE_FLOAT = 4;
	private static final int SIZE_INT = 4;
	
	
	
	public static float convert(String lin)
	{
		return Float.parseFloat(lin);
	}
	
	public static float[] convertK4(String[] lin)
	{
		float f[] = {Float.parseFloat(lin[0]), Float.parseFloat(lin[1]), Float.parseFloat(lin[2]), 0.0f};
		return f;
	}
	
	public static float[] convertK3(String[] lin)
	{
		float f[] = {Float.parseFloat(lin[0]), Float.parseFloat(lin[1]), 
					 Float.parseFloat(lin[2])};
		return f;
	}
	
	public static Vector4f convertK4f(String[] lin)
	{
		Vector4f f = new Vector4f(Float.parseFloat(lin[0]), Float.parseFloat(lin[1]), Float.parseFloat(lin[2]), 0.0f);
		return f;
	}
	
	public static Vector3f convertK3f(String[] lin)
	{
		if (lin[0].contains("nan") || lin[1].contains("nan") || lin[2].contains("nan"))
		{
			lin[0] = lin[1] = lin[2] = "0.0";
		}
		Vector3f f = new Vector3f(Float.parseFloat(lin[0]), Float.parseFloat(lin[1]), 
					 Float.parseFloat(lin[2]));
		return f;
	}
	
	public static Vector3f convertK3f(String lin, String lin1, String lin2)
	{
		if (lin.contains("nan") || lin1.contains("nan") || lin2.contains("nan"))
		{
			lin = lin1 = lin2 = "0.0";
		}
		Vector3f f = new Vector3f(Float.parseFloat(lin), Float.parseFloat(lin1), 
					 Float.parseFloat(lin2));
		return f;
	}
	
	public static Vector3f convertK3f(float lin, float lin1, float lin2)
	{
		
		Vector3f f = new Vector3f(lin, lin1, lin2);
		return f;
	}
	
	public static FloatBuffer allocFloats(float[] floatarray) 
	{
		FloatBuffer fb = ByteBuffer.allocateDirect(floatarray.length * SIZE_FLOAT).order(
				ByteOrder.nativeOrder()).asFloatBuffer();
		fb.put(floatarray).flip();
		return fb;
	}
	
	public static IntBuffer allocInts(int[] intarray) 
	{
		IntBuffer ib = ByteBuffer.allocateDirect(intarray.length * SIZE_INT).order(
				ByteOrder.nativeOrder()).asIntBuffer();
		ib.put(intarray).flip();
		return ib;
	}
	
	/* File browsing utilities methods.
	 */

	
	
}
