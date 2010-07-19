package fcampos.rawengine3D.graficos;

//This is the final version of our octree code.  It will allow you to take  
//a 3D model, regardless of the file format (as long as it conforms to the t3DModel struct).
//The difference from the last 2 tutorials is that we store face indices, rather
//than vertices.  Check out the top of Main.cpp for a explanation of why we chose this way.
//
//Here is a list of the important new functions added to our COctree class:
//
//	//This returns the number of polygons in our entire scene
//	int GetSceneTriangleCount(t3DModel *pWorld);
//
//// This adds the current object index to our object list
//	void AddObjectIndexToList(int index);
//
//	// This recursively creates a display list ID for every end node in the octree
//	void CreateDisplayList(COctree *pNode, t3DModel *pRootWorld, int displayListOffset);
//
//Since we need to know the total triangle count for each node that we will
//be potentially splitting, GetSceneTriangleCount() was created to go through
//all of the objects in the model and add up their triangles to a total number.
//This number is then returned and used for the root node to pass in.
//
//Instead of going through every object in the model's object list when drawing
//each end node, we store a list of indices into the object list that are in the end node.  
//This way we don't do unnecessary looping if the triangles in that end node aren't in 
//those objects.  The AddObjectIndexToList() function adds the passed in index to our 
//index list, if it's not already there.
//
//To get a greater efficiency out of the drawing of our octree, display lists are used.
//CreateDisplayList() recursively goes through each node and creates a display list ID for each.
//This is only done once, then we can use the display ID to render that end node liquid fast.
//
//Keep in mind, that we do NOT split the triangles over the node's planes.  Instead, we
//are just storing the face indices for each object. You might wonder how this works, since 
//our t3DModel structure has multiple objects, with multiple face index arrays.  Simple,
//we just create a pointer to a t3DModel structure for every end node (m_pWorld).  Then,
//we only store the objects that are in that end node.  Of course, there is no need to
//store anything but the face indices, number of faces and objects.  We will then use
//the face indices stored in our m_pWorld pointer to pass into the original world model's
//structure to draw it.  Remember, we don't store all the objects from the original
//model's structure, just the ones that are in our node's dimensions.
//
//There is so much that is going on in this tutorial, that I suggest having a second
//window open to look at the Octree2 project, so you can contrast between what is changed
//and what isn't.  This was necessary even for me when creating this tutorial.  Also,
//I think this helps you to understand the new code if you understand what the simplified
//octree code was doing.  Follow along each function with the old code and the new code.
//The same concepts are being coded, but with full world data, not just vertices.
//
//



/* Standard imports.
 */

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Vector;

import fcampos.rawengine3D.model.*;
import fcampos.rawengine3D.MathUtil.*;
import fcampos.rawengine3D.resource.Conversion;
import fcampos.rawengine3D.input.*;
import fcampos.rawengine3D.gamecore.*;

import static org.lwjgl.opengl.GL11.*;



public class TOctree{

	
	public static final int TOP_LEFT_FRONT		= 0;
	public static final int TOP_LEFT_BACK		= 1;
	public static final int TOP_RIGHT_BACK		= 2;
	public static final int TOP_RIGHT_FRONT		= 3;
	public static final int BOTTOM_LEFT_FRONT	= 4;
	public static final int BOTTOM_LEFT_BACK	= 5;
	public static final int BOTTOM_RIGHT_BACK	= 6;
	public static final int BOTTOM_RIGHT_FRONT	= 7;
	
//This holds the current amount of subdivisions we are currently at.
//This is used to make sure we don't go over the max amount
int g_CurrentSubdivision = 0;

//The maximum amount of triangles per node
public static int g_MaxTriangles;

// The maximum amount of subdivisions allow (Levels of subdivision)
public static int g_MaxSubdivisions;

// The amount of end nodes created in the octree (That hold vertices)
public static int g_EndNodeCount;

// This stores the amount of nodes that are in the frustum
public static int g_TotalNodesDrawn;

// Turn lighting on initially
public static boolean g_bLighting;

// This stores the render mode (LINES = false or TRIANGLES = true)
public static boolean g_RenderMode;

// The number of Nodes we've checked for collision.
public static int g_iNumNodesCollided;

// Wheter the Object is Colliding with anything in the World or not.
public static boolean g_bObjectColliding;

// Wheter we test the whole world for collision or just the nodes we are in.
public static boolean g_bOctreeCollisionDetection;

//This tells us if we have divided this node into more sub nodes
private boolean m_bSubDivided;

// This is the size of the cube for this current node
private float m_Width;

// This holds the amount of triangles stored in this node
private int m_TriangleCount;

// This is the center (X, Y, Z) point in this node
private Vector3f m_vCenter;

// This holds all the scene information (verts, normals, texture info, etc..) for this node
private T3dModel m_pWorld;

// This stores the indices into the original model's object list
private Vector<Integer>	m_pObjectList;

// This holds the display list ID for the current node, which increases the rendering speed
private int m_DisplayListID;

// These are the eight nodes branching down from this current node
private TOctree[] m_pOctreeNodes;

//Extern our debug object because we use it in the octree code
public static Debug g_Debug;



public static boolean drawMode = false;

//private int mode = GL_TRIANGLES;




private class tFaceList
{
	public tFaceList()
	{
		pFaceList = new Vector<Boolean>();
		totalFaceCount = 0;
	} 
	// This is a vector of booleans to store if the face index is in the nodes 3D Space
	public Vector<Boolean> pFaceList;	

	// This stores the total face count that is in the nodes 3D space (how many "true"'s)
	public int totalFaceCount;
}


///////////////////////////////// RENDER DEBUG LINES \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
/////
/////	This goes through all of the lines that we stored in our list and draws them
/////
///////////////////////////////// RENDER DEBUG LINES \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*

//Our debug class -----------------------------------------------------\\

public class Debug
{
	ArrayList<Vector3f> m_vLines = new ArrayList<Vector3f>();

	///////////////////////////////// RENDER DEBUG LINES \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This goes through all of the lines that we stored in our list and draws them
	/////
	///////////////////////////////// RENDER DEBUG LINES \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public Debug(){}

	public void renderDebugLines()			// This renders all of the lines
	{
		glDisable(GL_LIGHTING);		// Turn OFF lighting so the debug lines are bright yellow

		glBegin(GL_LINES);		// Start rendering lines

			glColor3f(1.0f, 1.0f, 0.0f);			// Turn the lines yellow

			// Go through the whole list of lines stored in the vector m_vLines.
			for(int i = 0; i < m_vLines.size(); i++)
			{
				// Pass in the current point to be rendered as part of a line
				Vector3f temp = m_vLines.get(i);
				glVertex3f(temp.x, temp.y, temp.z);
			}	

		glEnd();			// Stop rendering lines

		// If we have lighting turned on, turn the lights back on
		if(g_bLighting) 
			glEnable(GL_LIGHTING);
	}

	///////////////////////////////// ADD DEBUG LINE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This adds a debug LINE to the stack of lines
	/////
	///////////////////////////////// ADD DEBUG LINE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*

	public void addDebugLine(Vector3f vPoint1, Vector3f vPoint2)
	{
		// Add the 2 points that make up the line into our line list.
		m_vLines.add(vPoint1);
		m_vLines.add(vPoint2);
	}

	///////////////////////////////// ADD DEBUG RECTANGLE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This adds a debug RECTANGLE to the stack of lines
	/////
	///////////////////////////////// ADD DEBUG RECTANGLE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*

	public void addDebugRectangle(Vector3f vCenter, float width, float height, float depth)
	{
		// So we can work with the code better, we divide the dimensions in half.
		// That way we can create the cube from the center outwards.
		width /= 2.0f;
		height /= 2.0f;
		depth /= 2.0f;

		// Below we create all the 8 points so it will be easier to input the lines
		// of the cube.  With the dimensions we calculate the points.
		Vector3f vTopLeftFront		= new Vector3f(vCenter.x - width, vCenter.y + height, vCenter.z + depth);
		Vector3f vTopLeftBack		= new Vector3f(vCenter.x - width, vCenter.y + height, vCenter.z - depth);
		Vector3f vTopRightBack		= new Vector3f(vCenter.x + width, vCenter.y + height, vCenter.z - depth);
		Vector3f vTopRightFront		= new Vector3f(vCenter.x + width, vCenter.y + height, vCenter.z + depth);

		Vector3f vBottom_LeftFront	= new Vector3f(vCenter.x - width, vCenter.y - height, vCenter.z + depth);
		Vector3f vBottom_LeftBack	= new Vector3f(vCenter.x - width, vCenter.y - height, vCenter.z - depth);
		Vector3f vBottomRightBack	= new Vector3f(vCenter.x + width, vCenter.y - height, vCenter.z - depth);
		Vector3f vBottomRightFront	= new Vector3f(vCenter.x + width, vCenter.y - height, vCenter.z + depth);

		////////// TOP LINES ////////// 

		// Store the top front line of the box
		m_vLines.add(vTopLeftFront);			m_vLines.add(vTopRightFront);

		// Store the top back line of the box
		m_vLines.add(vTopLeftBack);  			m_vLines.add(vTopRightBack);

		// Store the top left line of the box
		m_vLines.add(vTopLeftFront);			m_vLines.add(vTopLeftBack);

		// Store the top right line of the box
		m_vLines.add(vTopRightFront);			m_vLines.add(vTopRightBack);

		////////// BOTTOM LINES ////////// 

		// Store the bottom front line of the box
		m_vLines.add(vBottom_LeftFront);		m_vLines.add(vBottomRightFront);

		// Store the bottom back line of the box
		m_vLines.add(vBottom_LeftBack);			m_vLines.add(vBottomRightBack);

		// Store the bottom left line of the box
		m_vLines.add(vBottom_LeftFront);		m_vLines.add(vBottom_LeftBack);

		// Store the bottom right line of the box
		m_vLines.add(vBottomRightFront);		m_vLines.add(vBottomRightBack);

		////////// SIDE LINES ////////// 

		// Store the bottom front line of the box
		m_vLines.add(vTopLeftFront);			m_vLines.add(vBottom_LeftFront);

		// Store the back left line of the box
		m_vLines.add(vTopLeftBack);				m_vLines.add(vBottom_LeftBack);

		// Store the front right line of the box
		m_vLines.add(vTopRightBack);			m_vLines.add(vBottomRightBack);

		// Store the front left line of the box
		m_vLines.add(vTopRightFront);			m_vLines.add(vBottomRightFront);
	}

	///////////////////////////////// CLEAR \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This clears all of the debug lines
	/////
	///////////////////////////////// CLEAR \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*

	public void clear()						
	{
		// Destroy the list using the standard vector clear() function
		m_vLines.clear();
	}
}



///////////////////////////////// OCTREE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
/////
/////	The COctree contstructor which calls our init function
/////
///////////////////////////////// OCTREE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*

public TOctree()
{
	// Set the subdivided flag to false
	m_bSubDivided = false;

	// Set the dimensions of the box to false
	m_Width = 0; 

	// Initialize the triangle count
	setM_TriangleCount(0);

	// Initialize the center of the box to the 0
	m_vCenter = new Vector3f();
	
	// Notice that we got rid of our InitOctree() function and just stuck the
	// initialization code in our constructor.  This is because we no longer need
	// to create the octree in real-time.

	// Initialize our world data to NULL.  This stores all the object's
	// face indices that need to be drawn for this node.  
	m_pWorld = null;
	
	m_pObjectList = new Vector<Integer>();

	// Set the sub nodes to NULL
	m_pOctreeNodes = new TOctree[8];
	
	
}


///////////////////////////////// ~OCTREE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
/////
/////	The COctree destructor which calls our destroy function
/////
///////////////////////////////// ~OCTREE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*



////////////////////////////GET SCENE TRIANGLE COUNT \\\\\\\\\\\\\\\\\\\\\\\\\\*
/////
/////	This returns the total number of polygons in our scene
/////
////////////////////////////GET SCENE TRIANGLE COUNT \\\\\\\\\\\\\\\\\\\\\\\\\\*

public int getSceneTriangleCount(T3dModel pWorld)
{
	// This function is only called once, right before we create our first root node.
	// Basically, we just go through all of the objects in our scene and add up their triangles.

	// Initialize a variable to hold the total amount of polygons in the scene
	int numberOfTriangles = 0;

	// Go through all the objects and add up their polygon count
	for(int i = 0; i < pWorld.getNumOfObjects(); i++)
	{
		// Increase the total polygon count
		numberOfTriangles += pWorld.getPObject(i).getNumFaces();
	}

	// Return the number of polygons in the scene
	return numberOfTriangles;
}


///////////////////////////////// OCTREE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
/////
/////	This sets our initial width of the scene, as well as our center point
/////
///////////////////////////////// OCTREE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*

public void getSceneDimensions(T3dModel pWorld)
{
	
	// Return from this function if we passed in bad data.  This used to be a check
	// to see if the vertices passed in were allocated, now it's a check for world data.
	if(pWorld == null) return;

	// Initialize a variable to hold the total amount of vertices in the scene
	int numberOfVerts = 0;

	// This code is still doing the same things as in the previous tutorials,
	// except that we have to go through every object in the scene to find the
	// center point.

	// Go through all of the object's vertices and add them up to eventually find the center
	for(int i = 0; i < pWorld.getNumOfObjects(); i++)
	{
		// Increase the total vertice count
		numberOfVerts += pWorld.getPObject(i).getNumVert();
		pWorld.getPObject(i).setDimension();
		//System.out.println(pWorld.getPObject(i).toString());

		// Add the current object's vertices up
		for(int n = 0; n < pWorld.getPObject(i).getNumVert(); n++)
		{
			// Add the current vertex to the center variable
			m_vCenter = VectorMath.add(m_vCenter, pWorld.getPObject(i).getVertices(n));
		}
	}

	// Divide the total by the number of vertices to get the center point.
	// We could have overloaded the / symbol but I chose not to because we rarely use it.
	m_vCenter.x /= numberOfVerts;
	m_vCenter.y /= numberOfVerts;	
	m_vCenter.z /= numberOfVerts;

	// Now that we have the center point, we want to find the farthest distance from
	// our center point.  That will tell us how big the width of the first node is.
	// Once we get the farthest height, width and depth, we then check them against each
	// other.  Which ever one is higher, we then use that value for the cube width.

	int currentWidth = 0, currentHeight = 0, currentDepth = 0;

	// Initialize some temporary variables to hold the max dimensions found
	float maxWidth = 0, maxHeight = 0, maxDepth = 0;
	
	// This code still does the same thing as in the previous octree tutorials,
	// except we need to go through each object in the scene to find the max dimensions.

	// Go through all of the scene's objects
	for(int i = 0; i < pWorld.getNumOfObjects(); i++)
	{
		// Go through all of the current objects vertices
		for(int j = 0; j < pWorld.getPObject(i).getNumVert(); j++)
		{
			// Get the distance in width, height and depth this vertex is from the center.
			currentWidth  = (int)Math.abs(pWorld.getPObject(i).getVertices(j).x - m_vCenter.x);	
			currentHeight = (int)Math.abs(pWorld.getPObject(i).getVertices(j).y - m_vCenter.y);		
			currentDepth  = (int)Math.abs(pWorld.getPObject(i).getVertices(j).z - m_vCenter.z);

			// Check if the current width value is greater than the max width stored.
			if(currentWidth  > maxWidth)	maxWidth  = currentWidth;

			// Check if the current height value is greater than the max height stored.
			if(currentHeight > maxHeight)	maxHeight = currentHeight;

			// Check if the current depth value is greater than the max depth stored.
			if(currentDepth > maxDepth)		maxDepth  = currentDepth;
		}
	}

	// Set the member variable dimensions to the max ones found.
	// We multiply the max dimensions by 2 because this will give us the
	// full width, height and depth.  Otherwise, we just have half the size
	// because we are calculating from the center of the scene.

	maxWidth *= 2;		maxHeight *= 2;		maxDepth *= 2;

	// Check if the width is the highest value and assign that for the cube dimension
	if(maxWidth > maxHeight && maxWidth > maxDepth)
		m_Width = maxWidth;

	// Check if the height is the heighest value and assign that for the cube dimension
	else if(maxHeight > maxWidth && maxHeight > maxDepth)
		m_Width = maxHeight;

	// Else it must be the depth or it's the same value as some of the other ones
	else
		m_Width = maxDepth;
}


///////////////////////////////// GET NEW NODE CENTER \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
/////
/////	This returns the center point of the new subdivided node, depending on the ID
/////
///////////////////////////////// GET NEW NODE CENTER \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*

public Vector3f getNewNodeCenter(Vector3f vCenter, float width, int nodeID)
{
	// I created this function which takes an enum ID to see which node's center
	// we need to calculate.  Once we find that we need to subdivide a node we find
	// the centers of each of the 8 new nodes.  This is what that function does.
	// We just tell it which node we want.

	// Initialize the new node center
	Vector3f vNodeCenter = new Vector3f(0, 0, 0);

	// Create a dummy variable to cut down the code size
	Vector3f vCtr = new Vector3f(vCenter);

	// Switch on the ID to see which subdivided node we are finding the center
	switch(nodeID)							
	{
		case TOP_LEFT_FRONT:
			// Calculate the center of this new node
			vNodeCenter.setTo(vCtr.x - width/4, vCtr.y + width/4, vCtr.z + width/4);
			break;

		case TOP_LEFT_BACK:
			// Calculate the center of this new node
			vNodeCenter.setTo(vCtr.x - width/4, vCtr.y + width/4, vCtr.z - width/4);
			break;

		case TOP_RIGHT_BACK:
			// Calculate the center of this new node
			vNodeCenter.setTo(vCtr.x + width/4, vCtr.y + width/4, vCtr.z - width/4);
			break;

		case TOP_RIGHT_FRONT:
			// Calculate the center of this new node
			vNodeCenter.setTo(vCtr.x + width/4, vCtr.y + width/4, vCtr.z + width/4);
			break;

		case BOTTOM_LEFT_FRONT:
			// Calculate the center of this new node
			vNodeCenter.setTo(vCtr.x - width/4, vCtr.y - width/4, vCtr.z + width/4);
			break;

		case BOTTOM_LEFT_BACK:
			// Calculate the center of this new node
			vNodeCenter.setTo(vCtr.x - width/4, vCtr.y - width/4, vCtr.z - width/4);
			break;

		case BOTTOM_RIGHT_BACK:
			// Calculate the center of this new node
			vNodeCenter.setTo(vCtr.x + width/4, vCtr.y - width/4, vCtr.z - width/4);
			break;

		case BOTTOM_RIGHT_FRONT:
			// Calculate the center of this new node
			vNodeCenter.setTo(vCtr.x + width/4, vCtr.y - width/4, vCtr.z + width/4);
			break;
	}

	// Return the new node center
	return vNodeCenter;
}

///////////////////////////////// CREATE NEW NODE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
/////
/////	This figures out the new node information and then passes it into CreateNode()
/////
///////////////////////////////// CREATE NEW NODE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*

public void createNewNode(T3dModel pWorld, Vector<tFaceList> pList, int triangleCount,
					  	    Vector3f vCenter, float width, int nodeID)
{
	// This function is used as our helper function to partition the world data
	// to pass into the subdivided nodes.  The same things go on as in the previous
	// tutorials, but it's dealing with more than just vertices.  We are given
	// the world data that needs to be partitioned, the list of faces that are in
	// the new node about to be created, the triangle count, the parent node's center
	// and width, along with the enum ID that tells us which new node is being created.
	//
	// The tFaceList structure stores a vector of booleans, which tell us if that face
	// index is in our end node (true) or not (false).  It also contains a integer
	// to tell us how many of those faces (triangles) are "true", or in other words, 
	// are in our node that is being created.  

	// Check if the first node found some triangles in it, if not we don't continue
	if(triangleCount < 0 || triangleCount == 0) return;
	
	// Here we create the temporary partitioned data model, which will contain
	// all the objects and triangles in this end node.
	T3dModel pTempWorld = new T3dModel();

	// Intialize the temp model data and assign the object count to it
	
	pTempWorld.setNumOfObjects(pWorld.getNumOfObjects());
	
		
	// Go through all of the objects in the current partition passed in
	for(int i = 0; i < pWorld.getNumOfObjects(); i++)
	{
		// Get a pointer to the current object to avoid ugly code
		T3dObject pObject = pWorld.getPObject(i);

		// Create a new object, initialize it, then add it to our temp partition
		T3dObject newObject = new T3dObject();
		
		pTempWorld.addPObject(newObject);

		// Assign the new node's face count, material ID, texture boolean and 
		// vertices to the new object.  Notice that it's not that pObject's face
		// count, but the pList's.  Also, we are just assigning the pointer to the
		// vertices, not copying them.
		//pTempWorld->pObject[i].numOfFaces  = pList[i].totalFaceCount;
		pTempWorld.getPObject(i).setNumFaces(pList.get(i).totalFaceCount);
		//pTempWorld->pObject[i].materialID  = pObject->materialID;
		pTempWorld.getPObject(i).setMaterialID(pObject.getMaterialID());
		//pTempWorld->pObject[i].bHasTexture = pObject->bHasTexture;
		pTempWorld.getPObject(i).setbHasTexture(pObject.isbHasTexture());
		//pTempWorld->pObject[i].pVerts      = pObject->pVerts;
		pTempWorld.getPObject(i).setVertices(pObject.getVertices());

		// Allocate memory for the new face list
		//pTempWorld->pObject[i].pFaces = new tFace [pTempWorld->pObject[i].numOfFaces];
		
		// Create a counter to count the current index of the new node vertices
		int index = 0;

		// Go through all of the current object's faces and only take the ones in this new node
		for(int j = 0; j < pObject.getNumFaces(); j++)
		{
			// If this current triangle is in the node, assign it's index to our new face list
			if(pList.get(i).pFaceList.get(j))	
			{
				//pTempWorld->pObject[i].pFaces[index] = pObject->pFaces[j];
				pTempWorld.getPObject(i).setFaces(index, pObject.getFace(j));
				index++;
			}
		}
	}

	// Now comes the initialization of the node.  First we allocate memory for
	// our node and then get it's center point.  Depending on the nodeID, 
	// GetNewNodeCenter() knows which center point to pass back (TOP_LEFT_FRONT, etc..)

	// Allocate a new node for this octree
	m_pOctreeNodes[nodeID] = new TOctree();

	// Get the new node's center point depending on the nodexIndex (which of the 8 subdivided cubes).
	Vector3f vNodeCenter = getNewNodeCenter(vCenter, width, nodeID);
		
	// Below, before and after we recurse further down into the tree, we keep track
	// of the level of subdivision that we are in.  This way we can restrict it.

	// Increase the current level of subdivision
	g_CurrentSubdivision++;

	// This chance is just that we pass in the temp partitioned world for this node,
	// instead of passing in just straight vertices.

	// Recurse through this node and subdivide it if necessary
	m_pOctreeNodes[nodeID].createNode(pTempWorld, triangleCount, vNodeCenter, width / 2);

	// Decrease the current level of subdivision
	g_CurrentSubdivision--;

	// To free the temporary partition, we just go through all of it's objects and
	// free the faces.  The rest of the dynamic data was just being pointed too and
	// does not to be deleted.  Finally, we delete the allocated pTempWorld.

	// Go through all of the objects in our temporary partition
	//for(int i = 0; i < pWorld.getNumOfObjects(); i++)
	//{
		// If there are faces allocated for this object, delete them
	//	if(pTempWorld.getPObject(i).getNumFaces() > 0)
	//		pTempWorld.getPObject(i).setNumFaces(0);
	//}

	// Delete the allocated partition
	pTempWorld = null;
}


///////////////////////////////// CREATE NODE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
/////
/////	This is our recursive function that goes through and subdivides our nodes
/////
///////////////////////////////// CREATE NODE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*

public void createNode(T3dModel pWorld, int numberOfTriangles, Vector3f vCenter, float width)
{
	// Initialize this node's center point.  Now we know the center of this node.
	m_vCenter.setTo(vCenter);

	// Initialize this nodes cube width.  Now we know the width of this current node.
	m_Width = width;

	// Add the current node to our debug rectangle list so we can visualize it.
	g_Debug.addDebugRectangle(vCenter, width, width, width);

	// Check if we have too many triangles in this node and we haven't subdivided
	// above our max subdivisions.  If so, then we need to break this node into
	// 8 more nodes (hence the word OCTree).  Both must be true to divide this node.
	if( (numberOfTriangles > g_MaxTriangles) && (g_CurrentSubdivision < g_MaxSubdivisions) )
	{
		// Since we need to subdivide more we set the divided flag to true.
		// This let's us know that this node does NOT have any vertices assigned to it,
		// but nodes that perhaps have vertices stored in them (Or their nodes, etc....)
		// We will query this variable when we are drawing the octree.
		m_bSubDivided = true;

		// Create a list for each new node to store if a triangle should be stored in it's
		// triangle list.  For each index it will be a true or false to tell us if that triangle
		// is in the cube of that node.  Below we check every point to see where it's
		// position is from the center (I.E. if it's above the center, to the left and 
		// back it's the TOP_LEFT_BACK node).  Depending on the node we set the pList 
		// index to true.  This will tell us later which triangles go to which node.
		// You might catch that this way will produce doubles in some nodes.  Some
		// triangles will intersect more than 1 node right?  We won't split the triangles
		// in this tutorial just to keep it simple, but the next tutorial we will.
		
		// This function pretty much stays the same, except a small twist because
		// we are dealing with multiple objects for the scene, not just an array of vertices.
		// In the previous tutorials, we used a vector<> of booleans, but now we use our
		// tFaceList to store a vector of booleans for each object.
		
		// Create the list of tFaceLists for each child node
		Vector<tFaceList> pList1 = new Vector<tFaceList>(pWorld.getNumOfObjects());		// TOP_LEFT_FRONT node list
		Vector<tFaceList> pList2 = new Vector<tFaceList>(pWorld.getNumOfObjects());		// TOP_LEFT_BACK node list
		Vector<tFaceList> pList3 = new Vector<tFaceList>(pWorld.getNumOfObjects());		// TOP_RIGHT_BACK node list
		Vector<tFaceList> pList4 = new Vector<tFaceList>(pWorld.getNumOfObjects());		// TOP_RIGHT_FRONT node list
		Vector<tFaceList> pList5 = new Vector<tFaceList>(pWorld.getNumOfObjects());		// BOTTOM_LEFT_FRONT node list
		Vector<tFaceList> pList6 = new Vector<tFaceList>(pWorld.getNumOfObjects());		// BOTTOM_LEFT_BACK node list
		Vector<tFaceList> pList7 = new Vector<tFaceList>(pWorld.getNumOfObjects());		// BOTTOM_RIGHT_BACK node list
		Vector<tFaceList> pList8 = new Vector<tFaceList>(pWorld.getNumOfObjects());		// BOTTOM_RIGHT_FRONT node list
		
		for (int i=0; i < pWorld.getNumOfObjects(); i++)
		{
			pList1.add(new tFaceList());
			pList2.add(new tFaceList());
			pList3.add(new tFaceList());
			pList4.add(new tFaceList());
			pList5.add(new tFaceList());
			pList6.add(new tFaceList());
			pList7.add(new tFaceList());
			pList8.add(new tFaceList());
		}
	
		// Create this variable to cut down the thickness of the code below (easier to read)
		Vector3f vCtr = new Vector3f(vCenter);

		// Go through every object in the current partition of the world
		for(int i = 0; i < pWorld.getNumOfObjects(); i++)
		{
			// Store a point to the current object
			T3dObject pObject = pWorld.getPObject(i);

			// Now, we have a face list for each object, for every child node.
			// We need to then check every triangle in this current object
			// to see if it's in any of the child nodes dimensions.  We store a "true" in
			// the face list index to tell us if that's the case.  This is then used
			// in CreateNewNode() to create a new partition of the world for that child node.
			
			
			// Resize the current face list to be the size of this object's face count
			/*
			pList1.get(i).pFaceList.setSize(pObject.getNumFaces());
			pList2.get(i).pFaceList.setSize(pObject.getNumFaces());
			pList3.get(i).pFaceList.setSize(pObject.getNumFaces());
			pList4.get(i).pFaceList.setSize(pObject.getNumFaces());
			pList5.get(i).pFaceList.setSize(pObject.getNumFaces());
			pList6.get(i).pFaceList.setSize(pObject.getNumFaces());
			pList7.get(i).pFaceList.setSize(pObject.getNumFaces());
			pList8.get(i).pFaceList.setSize(pObject.getNumFaces());
			*/
			for(int a=0; a < pObject.getNumFaces(); a++ )
			{
				pList1.get(i).pFaceList.add(a, new Boolean(false));
				pList2.get(i).pFaceList.add(a, new Boolean(false));
				pList3.get(i).pFaceList.add(a, new Boolean(false));
				pList4.get(i).pFaceList.add(a, new Boolean(false));
				pList5.get(i).pFaceList.add(a, new Boolean(false));
				pList6.get(i).pFaceList.add(a, new Boolean(false));
				pList7.get(i).pFaceList.add(a, new Boolean(false));
				pList8.get(i).pFaceList.add(a, new Boolean(false));
			}

			// Go through all the triangles for this object
			for(int j = 0; j < pObject.getNumFaces(); j++)
			{
				// Check every vertex in the current triangle to see if it's inside a child node
				for(int whichVertex = 0; whichVertex < 3; whichVertex++)
				{
					// Store the current vertex to be checked against all the child nodes
					Vector3f vPoint = new Vector3f(pObject.getVertices(pObject.getFace(j).getVertices(whichVertex)));

					// Check if the point lies within the TOP LEFT FRONT node
					if( (vPoint.x <= vCtr.x) && (vPoint.y >= vCtr.y) && (vPoint.z >= vCtr.z) ) 
						pList1.get(i).pFaceList.set(j, true);

					// Check if the point lies within the TOP LEFT BACK node
					if( (vPoint.x <= vCtr.x) && (vPoint.y >= vCtr.y) && (vPoint.z <= vCtr.z) ) 
						pList2.get(i).pFaceList.set(j, true);

					// Check if the point lies within the TOP RIGHT BACK node
					if( (vPoint.x >= vCtr.x) && (vPoint.y >= vCtr.y) && (vPoint.z <= vCtr.z) ) 
						pList3.get(i).pFaceList.set(j, true);

					// Check if the point lies within the TOP RIGHT FRONT node
					if( (vPoint.x >= vCtr.x) && (vPoint.y >= vCtr.y) && (vPoint.z >= vCtr.z) ) 
						pList4.get(i).pFaceList.set(j, true);

					// Check if the point lies within the BOTTOM LEFT FRONT node
					if( (vPoint.x <= vCtr.x) && (vPoint.y <= vCtr.y) && (vPoint.z >= vCtr.z) ) 
						pList5.get(i).pFaceList.set(j, true);

					// Check if the point lies within the BOTTOM LEFT BACK node
					if( (vPoint.x <= vCtr.x) && (vPoint.y <= vCtr.y) && (vPoint.z <= vCtr.z) ) 
						pList6.get(i).pFaceList.set(j, true);

					// Check if the point lies within the BOTTOM RIGHT BACK node
					if( (vPoint.x >= vCtr.x) && (vPoint.y <= vCtr.y) && (vPoint.z <= vCtr.z) ) 
						pList7.get(i).pFaceList.set(j, true);

					// Check if the point lines within the BOTTOM RIGHT FRONT node
					if( (vPoint.x >= vCtr.x) && (vPoint.y <= vCtr.y) && (vPoint.z >= vCtr.z) ) 
						pList8.get(i).pFaceList.set(j, true);
				}
			}	

			// Here we initialize the face count for each list that holds how many triangles
			// were found for each of the 8 subdivided nodes.
			pList1.get(i).totalFaceCount = 0;		pList2.get(i).totalFaceCount = 0;
			pList3.get(i).totalFaceCount = 0;		pList4.get(i).totalFaceCount = 0;
			pList5.get(i).totalFaceCount = 0;		pList6.get(i).totalFaceCount = 0;
			pList7.get(i).totalFaceCount = 0;		pList8.get(i).totalFaceCount = 0;
		}

		// Here we create a variable for each list that holds how many triangles
		// were found for each of the 8 subdivided nodes.
		int triCount1 = 0;	int triCount2 = 0;	int triCount3 = 0;	int triCount4 = 0;
		int triCount5 = 0;	int triCount6 = 0;	int triCount7 = 0;	int triCount8 = 0;
			
		// Go through all of the objects of this current partition
		for(int i = 0; i < pWorld.getNumOfObjects(); i++)
		{
			// Go through all of the current objects triangles
			for(int j = 0; j < pWorld.getPObject(i).getNumFaces(); j++)
			{
				// Increase the triangle count for each node that has a "true" for the index i.
				// In other words, if the current triangle is in a child node, add 1 to the count.
				// We need to store the total triangle count for each object, but also
				// the total for the whole child node.  That is why we increase 2 variables.
				if(pList1.get(i).pFaceList.get(j))	{ pList1.get(i).totalFaceCount++; triCount1++; }
				if(pList2.get(i).pFaceList.get(j))	{ pList2.get(i).totalFaceCount++; triCount2++; }
				if(pList3.get(i).pFaceList.get(j))	{ pList3.get(i).totalFaceCount++; triCount3++; }
				if(pList4.get(i).pFaceList.get(j))	{ pList4.get(i).totalFaceCount++; triCount4++; }
				if(pList5.get(i).pFaceList.get(j))	{ pList5.get(i).totalFaceCount++; triCount5++; }
				if(pList6.get(i).pFaceList.get(j))	{ pList6.get(i).totalFaceCount++; triCount6++; }
				if(pList7.get(i).pFaceList.get(j))	{ pList7.get(i).totalFaceCount++; triCount7++; }
				if(pList8.get(i).pFaceList.get(j))	{ pList8.get(i).totalFaceCount++; triCount8++; }
			}
		}

		// Next we do the dirty work.  We need to set up the new nodes with the triangles
		// that are assigned to each node, along with the new center point of the node.
		// Through recursion we subdivide this node into 8 more potential nodes.

		// Create the subdivided nodes if necessary and then recurse through them.
		// The information passed into CreateNewNode() are essential for creating the
		// new nodes.  We pass the 8 ID's in so it knows how to calculate it's new center.
		createNewNode(pWorld, pList1, triCount1, vCenter, width, TOP_LEFT_FRONT);
		createNewNode(pWorld, pList2, triCount2, vCenter, width, TOP_LEFT_BACK);
		createNewNode(pWorld, pList3, triCount3, vCenter, width, TOP_RIGHT_BACK);
		createNewNode(pWorld, pList4, triCount4, vCenter, width, TOP_RIGHT_FRONT);
		createNewNode(pWorld, pList5, triCount5, vCenter, width, BOTTOM_LEFT_FRONT);
		createNewNode(pWorld, pList6, triCount6, vCenter, width, BOTTOM_LEFT_BACK);
		createNewNode(pWorld, pList7, triCount7, vCenter, width, BOTTOM_RIGHT_BACK);
		createNewNode(pWorld, pList8, triCount8, vCenter, width, BOTTOM_RIGHT_FRONT);
	}
	else
	{
		// If we get here we must either be subdivided past our max level, or our triangle
		// count went below the minimum amount of triangles so we need to store them.
		
		// We pass in the current partition of world data to be assigned to this end node
		assignTrianglesToNode(pWorld, numberOfTriangles);
	}
}

////////////////////////////ADD OBJECT INDEX TO LIST \\\\\\\\\\\\\\\\\\\\\\\\\\\*
/////
/////	This adds the index into the model's object list to our object index list
/////
////////////////////////////ADD OBJECT INDEX TO LIST \\\\\\\\\\\\\\\\\\\\\\\\\\\*

public void addObjectIndexToList(int index)
{
	// To eliminate the need to loop through all of the objects in the original
	// model, when drawing the end nodes, we create an instance of our t3DModel
	// structure to hold only the objects that lie in the child node's 3D space.

	// Go through all of the objects in our face index list
	for(int i = 0; i < m_pObjectList.size(); i++)
	{
		// If we already have this index stored in our object index list, don't add it.
		if(m_pObjectList.get(i) == index)
			return;
	}

	// Add this index to our object index list, which indexes into the root world object list
	m_pObjectList.add(index);
}


////////////////////////////ASSIGN TRIANGLES TO NODE \\\\\\\\\\\\\\\\\\\\\\\\\\\*
/////
/////	This allocates memory for the face indices to assign to the current end node
/////
////////////////////////////ASSIGN TRIANGLES TO NODE \\\\\\\\\\\\\\\\\\\\\\\\\\\*

public void assignTrianglesToNode(T3dModel pWorld, int numberOfTriangles)
{
	// We take our pWorld partition and then copy it into our member variable
	// face list, m_pWorld.  This holds the face indices that need to be rendered.
	// Since we are using vertex arrays, we can't use the tFace structure for the
	// indices, so we need to create an array that has all the face indices in a row.
	// This will be stored in our pIndices array, which is of type unsigned int.
	// Remember, it must be unsigned int for vertex arrays to register it.

	// Since we did not subdivide this node we want to set our flag to false
	m_bSubDivided = false;

	// Initialize the triangle count of this end node 
	setM_TriangleCount(numberOfTriangles);

	// Create and init an instance of our model structure to store the face index information
	m_pWorld = new T3dModel();
	
	// Assign the number of objects to our face index list
	m_pWorld.setNumOfObjects(pWorld.getNumOfObjects());

	// Go through all of the objects in the partition that was passed in
	for(int i = 0; i < m_pWorld.getNumOfObjects(); i++)
	{
		// Create a pointer to the current object
		T3dObject pObject = pWorld.getPObject(i);

		// Create and init a new object to hold the face index information
		T3dObject newObject = new T3dObject();
		
		// If this object has face information, add it's index to our object index list
		if(pObject.getNumFaces() > 0)
			addObjectIndexToList(i);

		// Add our new object to our face index list
		m_pWorld.addPObject(newObject);

		// Store the number of faces in a local variable
		int numOfFaces = pObject.getNumFaces();

		// Assign the number of faces to this current face list
		m_pWorld.getPObject(i).setNumFaces(numOfFaces);

		//Remember, we also have faces indices
		// in a row, pIndices, which can be used to pass in for vertex arrays.  
				
		m_pWorld.getPObject(i).startPIndices(numOfFaces * 3);

		// Initialize the face indices for vertex arrays (are copied below
		//memset(m_pWorld->pObject[i].pIndices, 0, sizeof(UINT) * numOfFaces * 3);

		// Copy the faces from the partition passed in to our end nodes face index list
		//memcpy(m_pWorld->pObject[i].pFaces, pObject->pFaces, sizeof(tFace) * numOfFaces);
		m_pWorld.getPObject(i).setFaces(pObject.getFace());
		
		// Since we are using vertex arrays, we want to create a array with all of the
		// faces in a row.  That way we can pass it into glDrawElements().  We do this below.

		// Go through all the faces and assign them in a row to our pIndices array
		for(int j = 0; j < numOfFaces * 3; j += 3)
		{
			//m_pWorld->pObject[i].pIndices[j]     = m_pWorld->pObject[i].pFaces[j / 3].vertIndex[0];
			m_pWorld.getPObject(i).setPIndices(j, m_pWorld.getPObject(i).getFace(j / 3).getVertices(0));
			//m_pWorld->pObject[i].pIndices[j + 1] = m_pWorld->pObject[i].pFaces[j / 3].vertIndex[1];
			m_pWorld.getPObject(i).setPIndices(j + 1, m_pWorld.getPObject(i).getFace(j / 3).getVertices(1));
			//m_pWorld->pObject[i].pIndices[j + 2] = m_pWorld->pObject[i].pFaces[j / 3].vertIndex[2];
			m_pWorld.getPObject(i).setPIndices(j + 2, m_pWorld.getPObject(i).getFace(j / 3).getVertices(2));
		}

		// We can now free the pFaces list if we want since it isn't going to be used from here
		// on out.  If you do NOT want to use vertex arrays, don't free the pFaces, and get
		// rid of the loop up above to store the pIndices.

		/* NOTE ******************************************************/
		/* Don't delete these. We use the faces to check collisions. */
		//delete [] m_pWorld->pObject[i].pFaces;
		//m_pWorld->pObject[i].pFaces = NULL;
		/* NOTE ******************************************************/
	}

	// Assign the current display list ID to be the current end node count
	m_DisplayListID = g_EndNodeCount;

	// Increase the amount of end nodes created (Nodes with vertices stored)
	g_EndNodeCount++;
}

////////////////////////////////DRAW OCTREE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
/////
/////	This function recurses through all the nodes and draws the end node's vertices
/////
////////////////////////////////DRAW OCTREE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*

public void drawOctree(TOctree pNode, T3dModel pRootWorld)
{
	// To draw our octree, all that needs to be done is call our display list ID.
	// First we want to check if the current node is even in our frustum.  If it is,
	// we make sure that the node isn't subdivided.  We only can draw the end nodes.
	// Make sure a valid node was passed in, otherwise go back to the last node
	if(pNode == null) return;
	
	// Check if the current node is in our frustum
	
	if(!GameCore.gFrustum.cubeInFrustum(pNode.m_vCenter.x, pNode.m_vCenter.y, 
								pNode.m_vCenter.z, pNode.m_Width / 2) )
	{
		return;
	}

	// Check if this node is subdivided. If so, then we need to recurse and draw it's nodes
	if(pNode.isSubDivided())
	{
		// Recurse to the bottom of these nodes and draw the end node's vertices
		// Like creating the octree, we need to recurse through each of the 8 nodes.
		drawOctree(pNode.m_pOctreeNodes[TOP_LEFT_FRONT],		pRootWorld);
		drawOctree(pNode.m_pOctreeNodes[TOP_LEFT_BACK],			pRootWorld);
		drawOctree(pNode.m_pOctreeNodes[TOP_RIGHT_BACK],		pRootWorld);
		drawOctree(pNode.m_pOctreeNodes[TOP_RIGHT_FRONT],		pRootWorld);
		drawOctree(pNode.m_pOctreeNodes[BOTTOM_LEFT_FRONT],		pRootWorld);
		drawOctree(pNode.m_pOctreeNodes[BOTTOM_LEFT_BACK],		pRootWorld);
		drawOctree(pNode.m_pOctreeNodes[BOTTOM_RIGHT_BACK],		pRootWorld);
		drawOctree(pNode.m_pOctreeNodes[BOTTOM_RIGHT_FRONT],	pRootWorld);
	}
	else
	{
		// Increase the amount of nodes in our viewing frustum (camera's view)
		g_TotalNodesDrawn++;

		// Make sure we have valid data assigned to this node
		if(pNode.m_pWorld == null) return;
			
		// Call the list with our end node's display list ID
		glCallList(pNode.m_DisplayListID);
	}
}

////////////////////////////////CREATE DISPLAY LIST \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
/////
/////	This function recurses through all the nodes and creates a display list for them
/////
////////////////////////////////CREATE DISPLAY LIST \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*

public void createDisplayList(TOctree pNode, T3dModel pRootWorld, int displayListOffset)
{
	// This function handles our rendering code in the beginning and assigns it all
	// to a display list.  This increases our rendering speed, as long as we don't flood
	// the pipeline with a TON of data.  Display lists can actually be to bloated or too small.
	// Like our DrawOctree() function, we need to find the end nodes by recursing down to them.
	// We only create a display list for the end nodes and ignore the rest.  The 
	// displayListOffset is used to add to the end nodes current display list ID, in case
	// we created some display lists before creating the octree.  Usually it is just 1 otherwise.

	// Make sure a valid node was passed in, otherwise go back to the last node
	if(pNode == null) return;

	// Check if this node is subdivided. If so, then we need to recurse down to it's nodes
	if(pNode.isSubDivided())
	{
		// Recurse down to each one of the children until we reach the end nodes
		createDisplayList(pNode.m_pOctreeNodes[TOP_LEFT_FRONT],		pRootWorld, displayListOffset);
		createDisplayList(pNode.m_pOctreeNodes[TOP_LEFT_BACK],		pRootWorld, displayListOffset);
		createDisplayList(pNode.m_pOctreeNodes[TOP_RIGHT_BACK],		pRootWorld, displayListOffset);
		createDisplayList(pNode.m_pOctreeNodes[TOP_RIGHT_FRONT],	pRootWorld, displayListOffset);
		createDisplayList(pNode.m_pOctreeNodes[BOTTOM_LEFT_FRONT],	pRootWorld, displayListOffset);
		createDisplayList(pNode.m_pOctreeNodes[BOTTOM_LEFT_BACK],	pRootWorld, displayListOffset);
		createDisplayList(pNode.m_pOctreeNodes[BOTTOM_RIGHT_BACK],	pRootWorld, displayListOffset);
		createDisplayList(pNode.m_pOctreeNodes[BOTTOM_RIGHT_FRONT],	pRootWorld, displayListOffset);
	}
	else 
	{
		// Make sure we have valid data assigned to this node
		if(pNode.m_pWorld == null) return;

		// Add our display list offset to our current display list ID
		pNode.m_DisplayListID += displayListOffset;

		// Start the display list and assign it to the end nodes ID
		glNewList(pNode.m_DisplayListID,GL_COMPILE);

		// Create a temp counter for our while loop below to store the objects drawn
		int counter = 0;
		
		// Store the object count and material count in some local variables for optimization
		int objectCount = pNode.m_pObjectList.size();
		int materialCount = pRootWorld.getNumOfMaterials();

		// Go through all of the objects that are in our end node
		while(counter < objectCount)
		{
			// Get the first object index into our root world
			int i = pNode.m_pObjectList.get(counter);

			// Store pointers to the current face list and the root object 
			// that holds all the data (verts, texture coordinates, normals, etc..)
			T3dObject pObject     = pNode.m_pWorld.getPObject(i);
			T3dObject pRootObject = pRootWorld.getPObject(i);

			// Check to see if this object has a texture map, if so, bind the texture to it.
			if(pRootObject.getNumTexcoords() > 0) 
			{
				// Turn on texture mapping and turn off color
				glEnable(GL_TEXTURE_2D);

				// Reset the color to normal again
				glColor3f(1f, 1f, 1f);

				// Bind the texture map to the object by it's materialID
				glBindTexture(GL_TEXTURE_2D, pRootObject.getMaterialID());
			} 
			else 
			{
				// Turn off texture mapping and turn on color
				glDisable(GL_TEXTURE_2D);

				// Reset the color to normal again
				glColor3f(1f, 1f, 1f);
			}

			// Check to see if there is a valid material assigned to this object
			if((materialCount > 0) && (pRootObject.getMaterialID() >= 0)) 
			{
				
				byte[] pColor = pRootWorld.getPMaterials(pRootObject.getMaterialID()).getColor();
				
				
				glColor3ub(pColor[0], pColor[1], pColor[2]);
				//glColor3ub((byte)0,(byte) 255, (byte)0);
				
			}

			// Now we get to the more unknown stuff, vertex arrays.  If you haven't
			// dealt with vertex arrays yet, let me give you a brief run down on them.
			// Instead of doing loops to go through and pass in each of the vertices
			// of a model, we can just pass in the array vertices, then an array of
			// indices that MUST be an unsigned int, which gives the indices into
			// the vertex array.  That means that we can send the vertices to the video
			// card with one call to glDrawElements().  There are a bunch of other
			// functions for vertex arrays that do different things, but I am just going
			// to mention this one.  Since texture coordinates, normals and colors are also
			// associated with vertices, we are able to point OpenGL to these arrays before
			// we draw the geometry.  It uses the same indices that we pass to glDrawElements()
			// for each of these arrays.  Below, we point OpenGL to our texture coordinates,
			// vertex and normal arrays.  This is done with calls to glTexCoordPointer(), 
			// glVertexPointer() and glNormalPointer().
			//
			// Before using any of these functions, we need to enable their states.  This is
			// done with glEnableClientState().  You just pass in the ID of the type of array 
			// you are wanting OpenGL to look for.  If you don't have data in those arrays,
			// the program will most likely crash.
			//
			// If you don't want to use vertex arrays, you can just render the world like normal.
			// That is why I saved the pFace information, as well as the pIndices info.  This
			// way you can use what ever method you are comfortable with.  I tried both, and
			// by FAR the vertex arrays are incredibly faster.  You decide :)

			// Make sure we have texture coordinates to render
			if(pRootObject.getNumTex() > 0) 
			{
				// Turn on the texture coordinate state
				glEnableClientState(GL_TEXTURE_COORD_ARRAY);

				// Point OpenGL to our texture coordinate array.
				// We have them in a pair of 2, of type float and 0 bytes of stride between them.
				//glTexCoordPointer(2, GL_FLOAT, 0, pRootObject->pTexVerts);
				float[] temp = new float[pRootObject.getNumTex() * 2];
				int b = 0;
				for(int a=0; a < pRootObject.getNumTex(); a++)
				{
					temp[b] = pRootObject.getTexcoords()[a].s;
					temp[b+1] = pRootObject.getTexcoords()[a].t;
					b +=2;
					
				}
				
				FloatBuffer temp1 = Conversion.allocFloats(temp);
				
				glTexCoordPointer(2, 0, temp1);
			}

			// Make sure we have vertices to render
			if(pRootObject.getNumVert() > 0)
			{
				// Turn on the vertex array state
				glEnableClientState(GL_VERTEX_ARRAY);

				// Point OpenGL to our vertex array.  We have our vertices stored in
				// 3 floats, with 0 stride between them in bytes.
				float[] temp = new float[pRootObject.getNumVert() * 3];
				int b = 0;
				for(int a=0; a < pRootObject.getNumVert(); a++)
				{
					temp[b] = pRootObject.getVertices(a).x;
					temp[b+1] = pRootObject.getVertices(a).y;
					temp[b+2] = pRootObject.getVertices(a).z;
					b +=3;
					
				}
				
				FloatBuffer temp1 = Conversion.allocFloats(temp);
				glVertexPointer(3, 0, temp1);
			}

			// Make sure we have normals to render
			if(pRootObject.getNumNorm() > 0)
			{
				// Turn on the normals state
				glEnableClientState(GL_NORMAL_ARRAY);

				// Point OpenGL to our normals array.  We have our normals
				// stored as floats, with a stride of 0 between.
				
				float[] temp = new float[pRootObject.getNumNorm() * 3];
				int b = 0;
				for(int a=0; a < pRootObject.getNumNorm(); a++)
				{
					temp[b] = pRootObject.getNormal(a).x;
					temp[b+1] = pRootObject.getNormal(a).y;
					temp[b+2] = pRootObject.getNormal(a).z;
					b +=3;
					
				}
				
				FloatBuffer temp1 = Conversion.allocFloats(temp);
				glNormalPointer(0, temp1);
			}

			// Here we pass in the indices that need to be rendered.  We want to
			// render them in triangles, with numOfFaces * 3 for indice count,
			// and the indices are of type UINT (important).
			int[] temp = new int[pObject.getPIndices().size()]; 
			for(int a=0; a < pObject.getPIndices().size(); a++)
			{
				temp[a] = pObject.getPIndices(a);
				
			}
			
			IntBuffer temp1 = Conversion.allocInts(temp);
			glDrawElements(GL_TRIANGLES, temp1);
			
			// Increase the current object count rendered
			counter++;
		}

		// End the display list for this ID
		glEndList();
	}
}

/////// * /////////// * /////////// * NEW * /////// * /////////// * /////////// *
//Check to see if a line intersects with a polygon in the Octree.
//
//	[in]	pNode			The Octree Node to check.
//	[in]	vLine			The Line to check intersection for.
//	[in]	vIntersectionPt	The Point at which the line intersected.
//	[return]		Wheter there was an intersection or not.
public boolean intersectLineWithOctree(TOctree pNode, T3dModel pWorld, Vector3f vLine[], Vector3f vIntersectionPt )
{
	// If the passed in node is invalid, leave.
	if ( pNode == null )
		return false;
	
	float fLeft, fRight, fBottom, fTop, fBack, fFront;

	Vector3f pCenter = new Vector3f(pNode.getCenter());

	// Find the Left, Right, Front and Back of this Node's AABB.
	fLeft = pCenter.x - pNode.getWidth();
	fRight = pCenter.x + pNode.getWidth();
	fBottom = pCenter.y - pNode.getWidth();
	fTop = pCenter.y + pNode.getWidth();
	// Be careful here, depth is different in DirectX's Left handed coordinate system.
	fBack = pCenter.z - pNode.getWidth();
	fFront = pCenter.z + pNode.getWidth();

	// If BOTH Vertices of the Line are not in this Node, than there can not possibly
	// be an intersection, return false.
	if ( g_bOctreeCollisionDetection &&
		 (( vLine[0].x < fLeft || vLine[0].x > fRight ) ||
		 ( vLine[0].y < fBottom || vLine[0].y > fTop ) ||
		 ( vLine[0].z < fBack || vLine[0].z > fFront )) 
		 &&
		 (( vLine[1].x < fLeft || vLine[1].x > fRight ) ||
		 ( vLine[1].y < fBottom || vLine[1].y > fTop ) ||
		 ( vLine[1].z < fBack || vLine[1].z > fFront )) )
			return false;

	// If this node is subdivided, traverse to it's children.
	if ( pNode.isSubDivided() )
	{
		// Lots of Logic Tests, but with a purpose. If ANY node comes back saying there was a collision in it or one
		// of it's sub-nodes, return immediately without checking anymore nodes. This echos back recursivly to the root.
		if ( intersectLineWithOctree( pNode.m_pOctreeNodes[TOP_LEFT_FRONT], pWorld, vLine, vIntersectionPt ) )
			return true;
		if ( intersectLineWithOctree( pNode.m_pOctreeNodes[TOP_LEFT_BACK], pWorld, vLine, vIntersectionPt ) )
			return true;
		if ( intersectLineWithOctree( pNode.m_pOctreeNodes[TOP_RIGHT_BACK], pWorld, vLine, vIntersectionPt ) )
			return true;
		if ( intersectLineWithOctree( pNode.m_pOctreeNodes[TOP_RIGHT_FRONT], pWorld, vLine, vIntersectionPt ) )
			return true;
		if ( intersectLineWithOctree( pNode.m_pOctreeNodes[BOTTOM_LEFT_FRONT], pWorld, vLine, vIntersectionPt ) )
			return true;
		if ( intersectLineWithOctree( pNode.m_pOctreeNodes[BOTTOM_LEFT_BACK], pWorld, vLine, vIntersectionPt ) )
			return true;
		if ( intersectLineWithOctree( pNode.m_pOctreeNodes[BOTTOM_RIGHT_BACK], pWorld, vLine, vIntersectionPt ) )
			return true;
		if ( intersectLineWithOctree( pNode.m_pOctreeNodes[BOTTOM_RIGHT_FRONT], pWorld, vLine, vIntersectionPt ) )
			return true;
	}
	else
	{
		// Make sure there is a world to test.
		if (pNode.m_pWorld == null )
			return false;

		// Increment the Count of how many collisions with terminal Nodes we have encountered.
		g_iNumNodesCollided++;

		Vector3f[] vTempFace = new Vector3f[3];
		int i, j, k;

		// Check all of this Nodes World Objects.
		for ( i = 0; i < pNode.m_pWorld.getNumOfObjects(); i++ )
		{
			T3dObject pObject = pNode.m_pWorld.getPObject(i);

			// Check all of the Worlds Faces.
			for ( j = 0; j < pObject.getNumFaces(); j++ )
			{
				// Look at the 3 Vertices of this Face.
				for ( k = 0; k < 3; k++ )
				{
					// Get the Vertex Index;
					int iIndex = pObject.getFace(j).getVertices(k);

					// Now look in the Root World and just get the Vertices we need.
					vTempFace[k] = new Vector3f(pWorld.getPObject(i).getVertices(iIndex));
				}
				CollisionMath collision = new CollisionMath();

				// If we had a Line to Polygon Intersection, return true, which should echo down to the root function call.
				if ( collision.intersectedPolygon( vTempFace, vLine, 3, vIntersectionPt ) )
				{
					TOctree.g_bObjectColliding = true;
					return true;
				}
			}
		}
	}

	// No intersection detected.
	return false;
}

public boolean checkCameraCollision(TOctree pNode, T3dModel pWorld, CameraQuaternion camera)
{	
	// This function is pretty much a direct rip off of SpherePolygonCollision()
	// We needed to tweak it a bit though, to handle the collision detection once 
	// it was found, along with checking every triangle in the list if we collided.  
	// pVertices is the world data. If we have space partitioning, we would pass in 
	// the vertices that were closest to the camera. What happens in this function 
	// is that we go through every triangle in the list and check if the camera's 
	// sphere collided with it.  If it did, we don't stop there.  We can have 
	// multiple collisions so it's important to check them all.  One a collision 
	// is found, we calculate the offset to move the sphere off of the collided plane.
	
	// If the passed in node is invalid, leave.
	if ( pNode == null )
		return false;
	
	float fLeft, fRight, fBottom, fTop, fBack, fFront;

	Vector3f pCenter = new Vector3f(pNode.getCenter());

	// Find the Left, Right, Front and Back of this Node's AABB.
	fLeft = pCenter.x - pNode.getWidth();
	fRight = pCenter.x + pNode.getWidth();
	fBottom = pCenter.y - pNode.getWidth();
	fTop = pCenter.y + pNode.getWidth();
	// Be careful here, depth is different in DirectX's Left handed coordinate system.
	fBack = pCenter.z - pNode.getWidth();
	fFront = pCenter.z + pNode.getWidth();

	// If BOTH Vertices of the Line are not in this Node, than there can not possibly
	// be an intersection, return false.
	if ( g_bOctreeCollisionDetection &&
		 (( camera.getPosition().x < fLeft || camera.getPosition().x > fRight ) ||
		 ( camera.getPosition().y < fBottom || camera.getPosition().y > fTop ) ||
		 ( camera.getPosition().z < fBack || camera.getPosition().z > fFront )) 
		 &&
		 (( camera.getPosition().x+1 < fLeft || camera.getPosition().x+1 > fRight ) ||
		 ( camera.getPosition().y+1 < fBottom || camera.getPosition().y+1 > fTop ) ||
		 ( camera.getPosition().z+1 < fBack || camera.getPosition().z+1 > fFront )) )
			return false;
	
	
	
	// If this node is subdivided, traverse to it's children.
	if ( pNode.isSubDivided() )
	{
		// Lots of Logic Tests, but with a purpose. If ANY node comes back saying there was a collision in it or one
		// of it's sub-nodes, return immediately without checking anymore nodes. This echos back recursivly to the root.
		if (checkCameraCollision( pNode.m_pOctreeNodes[TOP_LEFT_FRONT], pWorld, camera))
			return true;
		if (checkCameraCollision( pNode.m_pOctreeNodes[TOP_LEFT_BACK], pWorld, camera))
			return true;
			
		if (checkCameraCollision( pNode.m_pOctreeNodes[TOP_RIGHT_BACK], pWorld, camera ))
			return true;
		if (checkCameraCollision( pNode.m_pOctreeNodes[TOP_RIGHT_FRONT], pWorld, camera ))
			return true;
		if (checkCameraCollision( pNode.m_pOctreeNodes[BOTTOM_LEFT_FRONT], pWorld, camera ))
			return true;
		if (checkCameraCollision( pNode.m_pOctreeNodes[BOTTOM_LEFT_BACK], pWorld, camera ))
			return true;
		if (checkCameraCollision( pNode.m_pOctreeNodes[BOTTOM_RIGHT_BACK], pWorld, camera ))
			return true;
		if (checkCameraCollision( pNode.m_pOctreeNodes[BOTTOM_RIGHT_FRONT], pWorld, camera ))
			return true;
	}
	else
	{
		// Make sure there is a world to test.
		if (pNode.m_pWorld == null )
			return false;

		// Increment the Count of how many collisions with terminal Nodes we have encountered.
		g_iNumNodesCollided++;

		Vector3f[] vTempFace = new Vector3f[3];
		Vector3f vNormal = new Vector3f();
		int i, j, k;

		// Check all of this Nodes World Objects.
		for ( i = 0; i < pNode.m_pWorld.getNumOfObjects(); i++ )
		{
			T3dObject pObject = pNode.m_pWorld.getPObject(i);

			// Check all of the Worlds Faces.
			for ( j = 0; j < pObject.getNumFaces(); j++ )
			{
				// Look at the 3 Vertices of this Face.
				for ( k = 0; k < 3; k++ )
				{
					// Get the Vertex Index;
					int iIndex = pObject.getFace(j).getVertices(k);
					
					// Now look in the Root World and just get the Vertices we need.
					vTempFace[k] = new Vector3f(pWorld.getPObject(i).getVertices(iIndex));
					
				}
				
				vNormal.setTo(VectorMath.normal(vTempFace));
				
				
				CollisionMath collision = new CollisionMath();
				Fl distance = new Fl();
				
				// This is where we determine if the sphere is in FRONT, BEHIND, or INTERSECTS the plane
				int classification = collision.classifySphere(camera.getPosition(), vNormal, vTempFace[0], camera.getRadius(), distance);
				// If the sphere intersects the polygon's plane, then we need to check further
				if(classification == CollisionMath.INTERSECTS) 
				{
					// 2) STEP TWO - Finding the psuedo intersection point on the plane

					// Now we want to project the sphere's center onto the triangle's plane
					Vector3f vOffset = new Vector3f(VectorMath.multiply(vNormal, distance.abc));

					// Once we have the offset to the plane, we just subtract it from the center
					// of the sphere.  "vIntersection" is now a point that lies on the plane of the triangle.
					Vector3f vIntersection = new Vector3f(VectorMath.subtract(camera.getPosition(), vOffset));

					// 3) STEP THREE - Check if the intersection point is inside the triangles perimeter

					// We first check if our intersection point is inside the triangle, if not,
					// the algorithm goes to step 4 where we check the sphere again the polygon's edges.

					// We do one thing different in the parameters for EdgeSphereCollision though.
					// Since we have a bulky sphere for our camera, it makes it so that we have to 
					// go an extra distance to pass around a corner. This is because the edges of 
					// the polygons are colliding with our peripheral view (the sides of the sphere).  
					// So it looks likes we should be able to go forward, but we are stuck and considered 
					// to be colliding.  To fix this, we just pass in the radius / 2.  Remember, this
					// is only for the check of the polygon's edges.  It just makes it look a bit more
					// realistic when colliding around corners.  Ideally, if we were using bounding box 
					// collision, cylinder or ellipses, this wouldn't really be a problem.

					if(collision.insidePolygon(vIntersection, vTempFace, 3) ||
							collision.edgeSphereCollision(camera.getPosition(), vTempFace, 3, camera.getRadius()/3))
					{
						// If we get here, we have collided!  To handle the collision detection
						// all it takes is to find how far we need to push the sphere back.
						// GetCollisionOffset() returns us that offset according to the normal,
						// radius, and current distance the center of the sphere is from the plane.
						vOffset.setTo(collision.getCollisionOffset(vNormal, camera.getRadius(), distance.abc));

						// Now that we have the offset, we want to ADD it to the position and
						// view vector in our camera.  This pushes us back off of the plane.  We
						// don't see this happening because we check collision before we render
						// the scene.
						//vOffset.negate();
						camera.setPosition(VectorMath.add(camera.getPosition(), vOffset));
						camera.setView(VectorMath.add(camera.getView(), vOffset));
						
						TOctree.g_bObjectColliding = true;
						return true;
					}
				}

				
			}
		}
	}
		return false;

	
}

//This returns if this node is subdivided or not
private boolean isSubDivided()  
	{   
		return m_bSubDivided;	
	}

//This returns the center of this node
public Vector3f getCenter() {	 return m_vCenter;	}

//This returns the widht of this node (since it's a cube the height and depth are the same)
public float getWidth() {	 return m_Width;	}

//This returns this nodes display list ID
public int getDisplayListID()		{   return m_DisplayListID;		}

// This sets the nodes display list ID
public void setDisplayListID(int displayListID)	{	m_DisplayListID = displayListID;  }


/**
 * @param m_TriangleCount the m_TriangleCount to set
 */
public void setM_TriangleCount(int m_TriangleCount) {
	this.m_TriangleCount = m_TriangleCount;
}


/**
 * @return the m_TriangleCount
 */
public int getM_TriangleCount() {
	return m_TriangleCount;
}





}
