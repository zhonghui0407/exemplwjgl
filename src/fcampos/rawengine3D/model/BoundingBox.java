package fcampos.rawengine3D.model;

import static org.lwjgl.opengl.GL11.GL_ALL_ATTRIB_BITS;
import static org.lwjgl.opengl.GL11.GL_LIGHTING;
import static org.lwjgl.opengl.GL11.GL_LINES;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glColor3f;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glPopAttrib;
import static org.lwjgl.opengl.GL11.glPushAttrib;
import static org.lwjgl.opengl.GL11.glVertex3f;

import java.util.ArrayList;

import fcampos.rawengine3D.MathUtil.Vector3f;

public final class BoundingBox {
	
	private ArrayList<Vector3f> linesDebug;
	
	public final static float[] YELLOW = {1.0f, 1.0f, 0.0f};
	public final static float[] RED = {1.0f, 0.0f, 0.0f};
	public final static float[] GREEN = {0.0f, 1.0f, 0.0f};
	public final static float[] BLUE = {0.0f, 0.0f, 1.0f};
	public final static float[] WHITE = {1.0f, 1.0f, 1.0f};
	
	private float[] color;
	
	
	public BoundingBox()
	{
		linesDebug = new ArrayList<Vector3f>();
		color = WHITE;
	}
	
	public float[] getColor() 
	{
		return color;
	}


	public void setColor(float[] color) 
	{
		this.color = color;
	}
	
	public void drawBoundingBox()			// This renders all of the lines
	{
		// Armazena o estado corrente dos buffers de desenho
    	glPushAttrib(GL_ALL_ATTRIB_BITS);
		glDisable(GL_LIGHTING);		// Turn OFF lighting so the debug lines are bright yellow

		glBegin(GL_LINES);		// Start rendering lines

			glColor3f(color[0], color[1], color[2]);			// Turn the lines yellow

			// Go through the whole list of lines stored in the vector m_vLines.
			for(int i = 0; i < linesDebug.size(); i++)
			{
				// Pass in the current point to be rendered as part of a line
				Vector3f temp = linesDebug.get(i);
				glVertex3f(temp.x, temp.y, temp.z);
			}	

		glEnd();			// Stop rendering lines

		glPopAttrib();
	}

	///////////////////////////////// ADD DEBUG LINE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This adds a debug LINE to the stack of lines
	/////
	///////////////////////////////// ADD DEBUG LINE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*

	private void addDebugLine(Vector3f vPoint1, Vector3f vPoint2)
	{
		// Add the 2 points that make up the line into our line list.
		linesDebug.add(vPoint1);
		linesDebug.add(vPoint2);
	}

	///////////////////////////////// ADD DEBUG RECTANGLE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This adds a debug RECTANGLE to the stack of lines
	/////
	///////////////////////////////// ADD DEBUG RECTANGLE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*

	public void createBoundingBox(Vector3f center, float width, float height, float depth)
	{
		// So we can work with the code better, we divide the dimensions in half.
		// That way we can create the cube from the center outwards.
		
		if(width > 0 && height > 0 && depth > 0)
		{
			width 	/= 2.0f;
			height 	/= 2.0f;
			depth 	/= 2.0f;
		}
		
		// Below we create all the 8 points so it will be easier to input the lines
		// of the cube.  With the dimensions we calculate the points.
		Vector3f topLeftFront		= new Vector3f(center.x - width, center.y + height, center.z + depth);
		Vector3f topLeftBack		= new Vector3f(center.x - width, center.y + height, center.z - depth);
		Vector3f topRightBack		= new Vector3f(center.x + width, center.y + height, center.z - depth);
		Vector3f topRightFront		= new Vector3f(center.x + width, center.y + height, center.z + depth);

		Vector3f bottomLeftFront	= new Vector3f(center.x - width, center.y - height, center.z + depth);
		Vector3f bottomLeftBack		= new Vector3f(center.x - width, center.y - height, center.z - depth);
		Vector3f bottomRightBack	= new Vector3f(center.x + width, center.y - height, center.z - depth);
		Vector3f bottomRightFront	= new Vector3f(center.x + width, center.y - height, center.z + depth);
		
		addAllDebugLines(topLeftFront, topLeftBack, topRightBack, topRightFront, 
						 bottomLeftFront, bottomLeftBack, bottomRightBack, bottomRightFront);
	}
	
	public void createBoundingBox(Vector3f dimMin, Vector3f dimMax)
	{
		Vector3f topLeftFront		= new Vector3f(dimMin.x, dimMax.y, dimMax.z);
		Vector3f topLeftBack		= new Vector3f(dimMin.x, dimMax.y, dimMin.z);
		Vector3f topRightBack		= new Vector3f(dimMax.x, dimMax.y, dimMin.z);
		Vector3f topRightFront		= new Vector3f(dimMax.x, dimMax.y, dimMax.z);

		Vector3f bottomLeftFront	= new Vector3f(dimMin.x, dimMin.y, dimMax.z);
		Vector3f bottomLeftBack	= new Vector3f(dimMin.x, dimMin.y, dimMin.z);
		Vector3f bottomRightBack	= new Vector3f(dimMax.x, dimMin.y, dimMin.z);
		Vector3f bottomRightFront	= new Vector3f(dimMax.x, dimMin.y, dimMax.z);
		
		
		addAllDebugLines(topLeftFront, topLeftBack, topRightBack, topRightFront, 
						 bottomLeftFront, bottomLeftBack, bottomRightBack, bottomRightFront);
	}
	
	
	
	private void addAllDebugLines(Vector3f topLeftFront, Vector3f topLeftBack, Vector3f topRightBack, Vector3f topRightFront,
			 Vector3f bottomLeftFront, Vector3f bottomLeftBack, Vector3f bottomRightBack, Vector3f bottomRightFront)
	{
		

		////////// TOP LINES ////////// 

		// Store the top front line of the box
		addDebugLine(topLeftFront, topRightFront);

		// Store the top back line of the box
		addDebugLine(topLeftBack, topRightBack);

		// Store the top left line of the box
		addDebugLine(topLeftFront, topLeftBack);

		// Store the top right line of the box
		addDebugLine(topRightFront, topRightBack);

		////////// BOTTOM LINES ////////// 

		// Store the bottom front line of the box
		addDebugLine(bottomLeftFront, bottomRightFront);

		// Store the bottom back line of the box
		addDebugLine(bottomLeftBack, bottomRightBack);

		// Store the bottom left line of the box
		addDebugLine(bottomLeftFront, bottomLeftBack);

		// Store the bottom right line of the box
		addDebugLine(bottomRightFront, bottomRightBack);

		////////// SIDE LINES ////////// 

		// Store the bottom front line of the box
		addDebugLine(topLeftFront, bottomLeftFront);

		// Store the back left line of the box
		addDebugLine(topLeftBack, bottomLeftBack);

		// Store the front right line of the box
		addDebugLine(topRightBack, bottomRightBack);

		// Store the front left line of the box
		addDebugLine(topRightFront, bottomRightFront);
	}

	///////////////////////////////// CLEAR \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This clears all of the debug lines
	/////
	///////////////////////////////// CLEAR \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*

	public void clear()						
	{
		// Destroy the list using the standard vector clear() function
		linesDebug.clear();
	}
}

	

