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
import java.util.Vector;

import fcampos.rawengine3D.model.*;
import fcampos.rawengine3D.MathUtil.*;
import fcampos.rawengine3D.resource.Conversion;
import fcampos.rawengine3D.input.*;
import fcampos.rawengine3D.gamecore.*;

import static org.lwjgl.opengl.GL11.*;

public class Octree{

	
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
	private int currentSubdivision = 0;
	
	//The maximum amount of triangles per node
	public static int maxTriangles;
	
	// The maximum amount of subdivisions allow (Levels of subdivision)
	public static int maxSubdivisions;
	
	// The amount of end nodes created in the octree (That hold vertices)
	public static int totalNodesCount;
	
	// This stores the amount of nodes that are in the frustum
	public static int totalNodesDrawn;
	
	// Turn lighting on initially
	public static boolean turnLighting;
	
	// Wheter we test the whole world for collision or just the nodes we are in.
	public static boolean octreeCollisionDetection;
	
	// This stores the render mode (LINES = false or TRIANGLES = true)
	private boolean renderMode;
	
	// The number of Nodes we've checked for collision.
	public static int numNodesCollided;
	
	// Wheter the Object is Colliding with anything in the World or not.
	private boolean objectColliding;
	
	//This tells us if we have divided this node into more sub nodes
	private boolean subDivided;
	
	// This is the size of the cube for this current node
	private float sizeWidth;
	
	// This holds the amount of triangles stored in this node
	private int triangleCount;
	
	// This is the center (X, Y, Z) point in this node
	private Vector3f centerNode;
	
	// This holds all the scene information (verts, normals, texture info, etc..) for this node
	private Model3d world;
	
	// This stores the indices into the original model's object list
	private Vector<Integer>	objectList;
	
	// This holds the display list ID for the current node, which increases the rendering speed
	private int displayListID;
	
	// These are the eight nodes branching down from this current node
	private Octree[] octreeNodes;
	
	//Extern our debug object because we use it in the octree code
	public static BoundingBox debug;

		
	private class FaceList
	{
		
		// This is a vector of booleans to store if the face index is in the nodes 3D Space
		public Vector<Boolean> faceList;	
	
		// This stores the total face count that is in the nodes 3D space (how many "true"'s)
		public int totalFaceCount;
		
		public FaceList()
		{
			faceList = new Vector<Boolean>();
			totalFaceCount = 0;
		} 
		
	}



///////////////////////////////// OCTREE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
/////
/////	The COctree contstructor which calls our init function
/////
///////////////////////////////// OCTREE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*

	public Octree()
	{
		// Set the subdivided flag to false
		subDivided = false;
	
		// Set the dimensions of the box to false
		sizeWidth = 0; 
	
		// Initialize the triangle count
		setTriangleCount(0);
	
		// Initialize the center of the box to the 0
		centerNode = new Vector3f();
		
		// Notice that we got rid of our InitOctree() function and just stuck the
		// initialization code in our constructor.  This is because we no longer need
		// to create the octree in real-time.
	
		// Initialize our world data to NULL.  This stores all the object's
		// face indices that need to be drawn for this node.  
		world = null;
		
		objectList = new Vector<Integer>();
	
		// Set the sub nodes to NULL
		octreeNodes = new Octree[8];
		
		setObjectColliding(false);
		
		setRenderMode(false);
	}



	////////////////////////////GET SCENE TRIANGLE COUNT \\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This returns the total number of polygons in our scene
	/////
	////////////////////////////GET SCENE TRIANGLE COUNT \\\\\\\\\\\\\\\\\\\\\\\\\\*
	
	public int getSceneTriangleCount(Model3d world)
	{
		// This function is only called once, right before we create our first root node.
		// Basically, we just go through all of the objects in our scene and add up their triangles.
	
		// Initialize a variable to hold the total amount of polygons in the scene
		int numberOfTriangles = 0;
	
		// Go through all the objects and add up their polygon count
		for(int i = 0; i < world.getNumOfObjects(); i++)
		{
			// Increase the total polygon count
			numberOfTriangles += world.getObject(i).getNumFaces();
		}
	
		// Return the number of polygons in the scene
		return numberOfTriangles;
	}


	///////////////////////////////// OCTREE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This sets our initial width of the scene, as well as our center point
	/////
	///////////////////////////////// OCTREE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	
	public void getSceneDimensions(Model3d world)
	{
		
		// Return from this function if we passed in bad data.  This used to be a check
		// to see if the vertices passed in were allocated, now it's a check for world data.
		if(world == null) return;
	
		// Initialize a variable to hold the total amount of vertices in the scene
		int numberOfVerts = 0;
	
		// This code is still doing the same things as in the previous tutorials,
		// except that we have to go through every object in the scene to find the
		// center point.
	
		// Go through all of the object's vertices and add them up to eventually find the center
		for(int i = 0; i < world.getNumOfObjects(); i++)
		{
			// Increase the total vertice count
			numberOfVerts += world.getObject(i).getNumVert();
			world.getObject(i).setDimension();
				
			// Add the current object's vertices up
			for(int n = 0; n < world.getObject(i).getNumVert(); n++)
			{
				// Add the current vertex to the center variable
				centerNode = VectorMath.add(centerNode, world.getObject(i).getVertices(n));
			}
		}
	
		// Divide the total by the number of vertices to get the center point.
		// We could have overloaded the / symbol but I chose not to because we rarely use it.
		
		centerNode = VectorMath.divide(centerNode, numberOfVerts);
		//centerNode.x /= numberOfVerts;
		//centerNode.y /= numberOfVerts;	
		//centerNode.z /= numberOfVerts;
	
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
		for(int i = 0; i < world.getNumOfObjects(); i++)
		{
			// Go through all of the current objects vertices
			for(int j = 0; j < world.getObject(i).getNumVert(); j++)
			{
				// Get the distance in width, height and depth this vertex is from the center.
				currentWidth  = (int)Math.abs(world.getObject(i).getVertices(j).x - centerNode.x);	
				currentHeight = (int)Math.abs(world.getObject(i).getVertices(j).y - centerNode.y);		
				currentDepth  = (int)Math.abs(world.getObject(i).getVertices(j).z - centerNode.z);
	
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
			sizeWidth = maxWidth;
	
		// Check if the height is the heighest value and assign that for the cube dimension
		else if(maxHeight > maxWidth && maxHeight > maxDepth)
			sizeWidth = maxHeight;
	
		// Else it must be the depth or it's the same value as some of the other ones
		else
			sizeWidth = maxDepth;
	}


	///////////////////////////////// GET NEW NODE CENTER \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This returns the center point of the new subdivided node, depending on the ID
	/////
	///////////////////////////////// GET NEW NODE CENTER \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	
	public Vector3f getNewNodeCenter(Vector3f center, float width, int nodeID)
	{
		// I created this function which takes an enum ID to see which node's center
		// we need to calculate.  Once we find that we need to subdivide a node we find
		// the centers of each of the 8 new nodes.  This is what that function does.
		// We just tell it which node we want.
	
		// Initialize the new node center
		Vector3f nodeCenter = new Vector3f(0, 0, 0);
	
		// Create a dummy variable to cut down the code size
		Vector3f vCtr = new Vector3f(center);
	
		// Switch on the ID to see which subdivided node we are finding the center
		switch(nodeID)							
		{
			case TOP_LEFT_FRONT:
				// Calculate the center of this new node
				nodeCenter.setTo(vCtr.x - width/4, vCtr.y + width/4, vCtr.z + width/4);
				break;
	
			case TOP_LEFT_BACK:
				// Calculate the center of this new node
				nodeCenter.setTo(vCtr.x - width/4, vCtr.y + width/4, vCtr.z - width/4);
				break;
	
			case TOP_RIGHT_BACK:
				// Calculate the center of this new node
				nodeCenter.setTo(vCtr.x + width/4, vCtr.y + width/4, vCtr.z - width/4);
				break;
	
			case TOP_RIGHT_FRONT:
				// Calculate the center of this new node
				nodeCenter.setTo(vCtr.x + width/4, vCtr.y + width/4, vCtr.z + width/4);
				break;
	
			case BOTTOM_LEFT_FRONT:
				// Calculate the center of this new node
				nodeCenter.setTo(vCtr.x - width/4, vCtr.y - width/4, vCtr.z + width/4);
				break;
	
			case BOTTOM_LEFT_BACK:
				// Calculate the center of this new node
				nodeCenter.setTo(vCtr.x - width/4, vCtr.y - width/4, vCtr.z - width/4);
				break;
	
			case BOTTOM_RIGHT_BACK:
				// Calculate the center of this new node
				nodeCenter.setTo(vCtr.x + width/4, vCtr.y - width/4, vCtr.z - width/4);
				break;
	
			case BOTTOM_RIGHT_FRONT:
				// Calculate the center of this new node
				nodeCenter.setTo(vCtr.x + width/4, vCtr.y - width/4, vCtr.z + width/4);
				break;
		}
	
		// Return the new node center
		return nodeCenter;
	}



	///////////////////////////////// CREATE NODE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This is our recursive function that goes through and subdivides our nodes
	/////
	///////////////////////////////// CREATE NODE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	
	public void createNode(Model3d world, int numberOfTriangles, Vector3f center, float width)
	{
		// Initialize this node's center point.  Now we know the center of this node.
		centerNode.setTo(center);
	
		// Initialize this nodes cube width.  Now we know the width of this current node.
		sizeWidth = width;
	
		// Add the current node to our debug rectangle list so we can visualize it.
		debug.createBoundingBox(center, width, width, width);
	
		// Check if we have too many triangles in this node and we haven't subdivided
		// above our max subdivisions.  If so, then we need to break this node into
		// 8 more nodes (hence the word OCTree).  Both must be true to divide this node.
		if( (numberOfTriangles > maxTriangles) && (currentSubdivision < maxSubdivisions) )
		{
			// Since we need to subdivide more we set the divided flag to true.
			// This let's us know that this node does NOT have any vertices assigned to it,
			// but nodes that perhaps have vertices stored in them (Or their nodes, etc....)
			// We will query this variable when we are drawing the octree.
			subDivided = true;
	
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
			Vector<FaceList> list1 = new Vector<FaceList>(world.getNumOfObjects());		// TOP_LEFT_FRONT node list
			Vector<FaceList> list2 = new Vector<FaceList>(world.getNumOfObjects());		// TOP_LEFT_BACK node list
			Vector<FaceList> list3 = new Vector<FaceList>(world.getNumOfObjects());		// TOP_RIGHT_BACK node list
			Vector<FaceList> list4 = new Vector<FaceList>(world.getNumOfObjects());		// TOP_RIGHT_FRONT node list
			Vector<FaceList> list5 = new Vector<FaceList>(world.getNumOfObjects());		// BOTTOM_LEFT_FRONT node list
			Vector<FaceList> list6 = new Vector<FaceList>(world.getNumOfObjects());		// BOTTOM_LEFT_BACK node list
			Vector<FaceList> list7 = new Vector<FaceList>(world.getNumOfObjects());		// BOTTOM_RIGHT_BACK node list
			Vector<FaceList> list8 = new Vector<FaceList>(world.getNumOfObjects());		// BOTTOM_RIGHT_FRONT node list
			
			for (int i=0; i < world.getNumOfObjects(); i++)
			{
				list1.add(new FaceList());
				list2.add(new FaceList());
				list3.add(new FaceList());
				list4.add(new FaceList());
				list5.add(new FaceList());
				list6.add(new FaceList());
				list7.add(new FaceList());
				list8.add(new FaceList());
			}
		
			// Create this variable to cut down the thickness of the code below (easier to read)
			Vector3f vCtr = new Vector3f(center);
	
			// Go through every object in the current partition of the world
			for(int i = 0; i < world.getNumOfObjects(); i++)
			{
				// Store a point to the current object
				Object3d object = world.getObject(i);
	
				// Now, we have a face list for each object, for every child node.
				// We need to then check every triangle in this current object
				// to see if it's in any of the child nodes dimensions.  We store a "true" in
				// the face list index to tell us if that's the case.  This is then used
				// in CreateNewNode() to create a new partition of the world for that child node.
				
				
				
				for(int a=0; a < object.getNumFaces(); a++ )
				{
					list1.get(i).faceList.add(a, new Boolean(false));
					list2.get(i).faceList.add(a, new Boolean(false));
					list3.get(i).faceList.add(a, new Boolean(false));
					list4.get(i).faceList.add(a, new Boolean(false));
					list5.get(i).faceList.add(a, new Boolean(false));
					list6.get(i).faceList.add(a, new Boolean(false));
					list7.get(i).faceList.add(a, new Boolean(false));
					list8.get(i).faceList.add(a, new Boolean(false));
				}
	
				// Go through all the triangles for this object
				for(int j = 0; j < object.getNumFaces(); j++)
				{
					// Check every vertex in the current triangle to see if it's inside a child node
					for(int whichVertex = 0; whichVertex < 3; whichVertex++)
					{
						// Store the current vertex to be checked against all the child nodes
						Vector3f point = new Vector3f(object.getVertices(object.getFace(j).getVertices(whichVertex)));
	
						// Check if the point lies within the TOP LEFT FRONT node
						if( (point.x <= vCtr.x) && (point.y >= vCtr.y) && (point.z >= vCtr.z) ) 
							list1.get(i).faceList.set(j, true);
	
						// Check if the point lies within the TOP LEFT BACK node
						if( (point.x <= vCtr.x) && (point.y >= vCtr.y) && (point.z <= vCtr.z) ) 
							list2.get(i).faceList.set(j, true);
	
						// Check if the point lies within the TOP RIGHT BACK node
						if( (point.x >= vCtr.x) && (point.y >= vCtr.y) && (point.z <= vCtr.z) ) 
							list3.get(i).faceList.set(j, true);
	
						// Check if the point lies within the TOP RIGHT FRONT node
						if( (point.x >= vCtr.x) && (point.y >= vCtr.y) && (point.z >= vCtr.z) ) 
							list4.get(i).faceList.set(j, true);
	
						// Check if the point lies within the BOTTOM LEFT FRONT node
						if( (point.x <= vCtr.x) && (point.y <= vCtr.y) && (point.z >= vCtr.z) ) 
							list5.get(i).faceList.set(j, true);
	
						// Check if the point lies within the BOTTOM LEFT BACK node
						if( (point.x <= vCtr.x) && (point.y <= vCtr.y) && (point.z <= vCtr.z) ) 
							list6.get(i).faceList.set(j, true);
	
						// Check if the point lies within the BOTTOM RIGHT BACK node
						if( (point.x >= vCtr.x) && (point.y <= vCtr.y) && (point.z <= vCtr.z) ) 
							list7.get(i).faceList.set(j, true);
	
						// Check if the point lines within the BOTTOM RIGHT FRONT node
						if( (point.x >= vCtr.x) && (point.y <= vCtr.y) && (point.z >= vCtr.z) ) 
							list8.get(i).faceList.set(j, true);
					}
				}	
	
				// Here we initialize the face count for each list that holds how many triangles
				// were found for each of the 8 subdivided nodes.
				list1.get(i).totalFaceCount = 0;		list2.get(i).totalFaceCount = 0;
				list3.get(i).totalFaceCount = 0;		list4.get(i).totalFaceCount = 0;
				list5.get(i).totalFaceCount = 0;		list6.get(i).totalFaceCount = 0;
				list7.get(i).totalFaceCount = 0;		list8.get(i).totalFaceCount = 0;
			}
	
			// Here we create a variable for each list that holds how many triangles
			// were found for each of the 8 subdivided nodes.
			int triCount1 = 0;	int triCount2 = 0;	int triCount3 = 0;	int triCount4 = 0;
			int triCount5 = 0;	int triCount6 = 0;	int triCount7 = 0;	int triCount8 = 0;
				
			// Go through all of the objects of this current partition
			for(int i = 0; i < world.getNumOfObjects(); i++)
			{
				// Go through all of the current objects triangles
				for(int j = 0; j < world.getObject(i).getNumFaces(); j++)
				{
					// Increase the triangle count for each node that has a "true" for the index i.
					// In other words, if the current triangle is in a child node, add 1 to the count.
					// We need to store the total triangle count for each object, but also
					// the total for the whole child node.  That is why we increase 2 variables.
					if(list1.get(i).faceList.get(j))	{ list1.get(i).totalFaceCount++; triCount1++; }
					if(list2.get(i).faceList.get(j))	{ list2.get(i).totalFaceCount++; triCount2++; }
					if(list3.get(i).faceList.get(j))	{ list3.get(i).totalFaceCount++; triCount3++; }
					if(list4.get(i).faceList.get(j))	{ list4.get(i).totalFaceCount++; triCount4++; }
					if(list5.get(i).faceList.get(j))	{ list5.get(i).totalFaceCount++; triCount5++; }
					if(list6.get(i).faceList.get(j))	{ list6.get(i).totalFaceCount++; triCount6++; }
					if(list7.get(i).faceList.get(j))	{ list7.get(i).totalFaceCount++; triCount7++; }
					if(list8.get(i).faceList.get(j))	{ list8.get(i).totalFaceCount++; triCount8++; }
				}
			}
	
			// Next we do the dirty work.  We need to set up the new nodes with the triangles
			// that are assigned to each node, along with the new center point of the node.
			// Through recursion we subdivide this node into 8 more potential nodes.
	
			// Create the subdivided nodes if necessary and then recurse through them.
			// The information passed into CreateNewNode() are essential for creating the
			// new nodes.  We pass the 8 ID's in so it knows how to calculate it's new center.
			createNewNode(world, list1, triCount1, center, width, TOP_LEFT_FRONT);
			createNewNode(world, list2, triCount2, center, width, TOP_LEFT_BACK);
			createNewNode(world, list3, triCount3, center, width, TOP_RIGHT_BACK);
			createNewNode(world, list4, triCount4, center, width, TOP_RIGHT_FRONT);
			createNewNode(world, list5, triCount5, center, width, BOTTOM_LEFT_FRONT);
			createNewNode(world, list6, triCount6, center, width, BOTTOM_LEFT_BACK);
			createNewNode(world, list7, triCount7, center, width, BOTTOM_RIGHT_BACK);
			createNewNode(world, list8, triCount8, center, width, BOTTOM_RIGHT_FRONT);
		}
		else
		{
			// If we get here we must either be subdivided past our max level, or our triangle
			// count went below the minimum amount of triangles so we need to store them.
			
			// We pass in the current partition of world data to be assigned to this end node
			assignTrianglesToNode(world, numberOfTriangles);
		}
	}
	
///////////////////////////////// CREATE NEW NODE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
/////
/////	This figures out the new node information and then passes it into CreateNode()
/////
///////////////////////////////// CREATE NEW NODE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*

public void createNewNode(Model3d world, Vector<FaceList> listFaces, int triangleCount,
					  	  Vector3f center, float width, int nodeID)
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
	Model3d tempWorld = new Model3d();

	// Intialize the temp model data and assign the object count to it
	
	tempWorld.setNumOfObjects(world.getNumOfObjects());
	
		
	// Go through all of the objects in the current partition passed in
	for(int i = 0; i < world.getNumOfObjects(); i++)
	{
		// Get a pointer to the current object to avoid ugly code
		Object3d pObject = world.getObject(i);

		// Create a new object, initialize it, then add it to our temp partition
		Object3d newObject = new Object3d();
		
		tempWorld.addObject(newObject);

		// Assign the new node's face count, material ID, texture boolean and 
		// vertices to the new object.  Notice that it's not that pObject's face
		// count, but the pList's.  Also, we are just assigning the pointer to the
		// vertices, not copying them.
		//pTempWorld->pObject[i].numOfFaces  = pList[i].totalFaceCount;
		tempWorld.getObject(i).setNumFaces(listFaces.get(i).totalFaceCount);
		//pTempWorld->pObject[i].materialID  = pObject->materialID;
		tempWorld.getObject(i).setMaterialID(pObject.getMaterialID());
		//pTempWorld->pObject[i].bHasTexture = pObject->bHasTexture;
		tempWorld.getObject(i).setbHasTexture(pObject.isbHasTexture());
		//pTempWorld->pObject[i].pVerts      = pObject->pVerts;
		tempWorld.getObject(i).setVertices(pObject.getVertices());

		// Allocate memory for the new face list
		//pTempWorld->pObject[i].pFaces = new tFace [pTempWorld->pObject[i].numOfFaces];
		
		// Create a counter to count the current index of the new node vertices
		int index = 0;

		// Go through all of the current object's faces and only take the ones in this new node
		for(int j = 0; j < pObject.getNumFaces(); j++)
		{
			// If this current triangle is in the node, assign it's index to our new face list
			if(listFaces.get(i).faceList.get(j))	
			{
				//pTempWorld->pObject[i].pFaces[index] = pObject->pFaces[j];
				tempWorld.getObject(i).setFaces(index, pObject.getFace(j));
				index++;
			}
		}
	}

	// Now comes the initialization of the node.  First we allocate memory for
	// our node and then get it's center point.  Depending on the nodeID, 
	// GetNewNodeCenter() knows which center point to pass back (TOP_LEFT_FRONT, etc..)

	// Allocate a new node for this octree
	octreeNodes[nodeID] = new Octree();

	// Get the new node's center point depending on the nodexIndex (which of the 8 subdivided cubes).
	Vector3f nodeCenter = getNewNodeCenter(center, width, nodeID);
		
	// Below, before and after we recurse further down into the tree, we keep track
	// of the level of subdivision that we are in.  This way we can restrict it.

	// Increase the current level of subdivision
	currentSubdivision++;

	// This chance is just that we pass in the temp partitioned world for this node,
	// instead of passing in just straight vertices.

	// Recurse through this node and subdivide it if necessary
	octreeNodes[nodeID].createNode(tempWorld, triangleCount, nodeCenter, width / 2);

	// Decrease the current level of subdivision
	currentSubdivision--;

	// To free the temporary partition, we just go through all of it's objects and
	// free the faces.  The rest of the dynamic data was just being pointed too and
	// does not to be deleted.  Finally, we delete the allocated pTempWorld.

	
	// Delete the allocated partition
	tempWorld = null;
}



////////////////////////////ASSIGN TRIANGLES TO NODE \\\\\\\\\\\\\\\\\\\\\\\\\\\*
/////
/////	This allocates memory for the face indices to assign to the current end node
/////
////////////////////////////ASSIGN TRIANGLES TO NODE \\\\\\\\\\\\\\\\\\\\\\\\\\\*

public void assignTrianglesToNode(Model3d tempWorld, int numberOfTriangles)
{
	// We take our pWorld partition and then copy it into our member variable
	// face list, m_pWorld.  This holds the face indices that need to be rendered.
	// Since we are using vertex arrays, we can't use the tFace structure for the
	// indices, so we need to create an array that has all the face indices in a row.
	// This will be stored in our pIndices array, which is of type unsigned int.
	// Remember, it must be unsigned int for vertex arrays to register it.

	// Since we did not subdivide this node we want to set our flag to false
	subDivided = false;

	// Initialize the triangle count of this end node 
	setTriangleCount(numberOfTriangles);

	// Create and init an instance of our model structure to store the face index information
	world = new Model3d();
	
	// Assign the number of objects to our face index list
	world.setNumOfObjects(tempWorld.getNumOfObjects());

	// Go through all of the objects in the partition that was passed in
	for(int i = 0; i < world.getNumOfObjects(); i++)
	{
		// Create a pointer to the current object
		Object3d pObject = tempWorld.getObject(i);

		// Create and init a new object to hold the face index information
		Object3d newObject = new Object3d();
		
		// If this object has face information, add it's index to our object index list
		if(pObject.getNumFaces() > 0)
		{
			addObjectIndexToList(i);
		}

		// Add our new object to our face index list
		world.addObject(newObject);

		// Store the number of faces in a local variable
		int numOfFaces = pObject.getNumFaces();

		// Assign the number of faces to this current face list
		world.getObject(i).setNumFaces(numOfFaces);

		//Remember, we also have faces indices
		// in a row, pIndices, which can be used to pass in for vertex arrays.  
				
		world.getObject(i).startIndices(numOfFaces * 3);

		// Initialize the face indices for vertex arrays (are copied below
		//memset(m_pWorld->pObject[i].pIndices, 0, sizeof(UINT) * numOfFaces * 3);

		// Copy the faces from the partition passed in to our end nodes face index list
		//memcpy(m_pWorld->pObject[i].pFaces, pObject->pFaces, sizeof(tFace) * numOfFaces);
		world.getObject(i).setFaces(pObject.getFace());
		
		// Since we are using vertex arrays, we want to create a array with all of the
		// faces in a row.  That way we can pass it into glDrawElements().  We do this below.

		// Go through all the faces and assign them in a row to our pIndices array
		for(int j = 0; j < numOfFaces * 3; j += 3)
		{
			//m_pWorld->pObject[i].pIndices[j]     = m_pWorld->pObject[i].pFaces[j / 3].vertIndex[0];
			world.getObject(i).setIndices(j, world.getObject(i).getFace(j / 3).getVertices(0));
			//m_pWorld->pObject[i].pIndices[j + 1] = m_pWorld->pObject[i].pFaces[j / 3].vertIndex[1];
			world.getObject(i).setIndices(j + 1, world.getObject(i).getFace(j / 3).getVertices(1));
			//m_pWorld->pObject[i].pIndices[j + 2] = m_pWorld->pObject[i].pFaces[j / 3].vertIndex[2];
			world.getObject(i).setIndices(j + 2, world.getObject(i).getFace(j / 3).getVertices(2));
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
	displayListID = totalNodesCount;

	// Increase the amount of end nodes created (Nodes with vertices stored)
	totalNodesCount++;
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
		for(int i = 0; i < objectList.size(); i++)
		{
			// If we already have this index stored in our object index list, don't add it.
			if(objectList.get(i) == index)
				return;
		}
		
		// Add this index to our object index list, which indexes into the root world object list
		objectList.add(index);
	}



	////////////////////////////////DRAW OCTREE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This function recurses through all the nodes and draws the end node's vertices
	/////
	////////////////////////////////DRAW OCTREE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	
	public void drawOctree(Octree node, Model3d pRootWorld)
	{
		// To draw our octree, all that needs to be done is call our display list ID.
		// First we want to check if the current node is even in our frustum.  If it is,
		// we make sure that the node isn't subdivided.  We only can draw the end nodes.
		// Make sure a valid node was passed in, otherwise go back to the last node
		if(node == null) return;
		
		// Check if the current node is in our frustum
		
		if(!GameCore.gFrustum.cubeInFrustum(node.centerNode, node.sizeWidth / 2) )
		{
			return;
		}
	
		// Check if this node is subdivided. If so, then we need to recurse and draw it's nodes
		if(node.isSubDivided())
		{
			// Recurse to the bottom of these nodes and draw the end node's vertices
			// Like creating the octree, we need to recurse through each of the 8 nodes.
			drawOctree(node.octreeNodes[TOP_LEFT_FRONT],		pRootWorld);
			drawOctree(node.octreeNodes[TOP_LEFT_BACK],			pRootWorld);
			drawOctree(node.octreeNodes[TOP_RIGHT_BACK],		pRootWorld);
			drawOctree(node.octreeNodes[TOP_RIGHT_FRONT],		pRootWorld);
			drawOctree(node.octreeNodes[BOTTOM_LEFT_FRONT],		pRootWorld);
			drawOctree(node.octreeNodes[BOTTOM_LEFT_BACK],		pRootWorld);
			drawOctree(node.octreeNodes[BOTTOM_RIGHT_BACK],		pRootWorld);
			drawOctree(node.octreeNodes[BOTTOM_RIGHT_FRONT],	pRootWorld);
		}
		else
		{
			// Increase the amount of nodes in our viewing frustum (camera's view)
			totalNodesDrawn++;
	
			// Make sure we have valid data assigned to this node
			if(node.world == null) return;
				
			// Call the list with our end node's display list ID
			glCallList(node.displayListID);
		}
	}

	////////////////////////////////CREATE DISPLAY LIST \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This function recurses through all the nodes and creates a display list for them
	/////
	////////////////////////////////CREATE DISPLAY LIST \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	
	public void createDisplayList(Octree node, Model3d rootWorld, int displayListOffset)
	{
		// This function handles our rendering code in the beginning and assigns it all
		// to a display list.  This increases our rendering speed, as long as we don't flood
		// the pipeline with a TON of data.  Display lists can actually be to bloated or too small.
		// Like our DrawOctree() function, we need to find the end nodes by recursing down to them.
		// We only create a display list for the end nodes and ignore the rest.  The 
		// displayListOffset is used to add to the end nodes current display list ID, in case
		// we created some display lists before creating the octree.  Usually it is just 1 otherwise.
	
		// Make sure a valid node was passed in, otherwise go back to the last node
		if(node == null) return;
	
		// Check if this node is subdivided. If so, then we need to recurse down to it's nodes
		if(node.isSubDivided())
		{
			// Recurse down to each one of the children until we reach the end nodes
			createDisplayList(node.octreeNodes[TOP_LEFT_FRONT],		rootWorld, displayListOffset);
			createDisplayList(node.octreeNodes[TOP_LEFT_BACK],		rootWorld, displayListOffset);
			createDisplayList(node.octreeNodes[TOP_RIGHT_BACK],		rootWorld, displayListOffset);
			createDisplayList(node.octreeNodes[TOP_RIGHT_FRONT],	rootWorld, displayListOffset);
			createDisplayList(node.octreeNodes[BOTTOM_LEFT_FRONT],	rootWorld, displayListOffset);
			createDisplayList(node.octreeNodes[BOTTOM_LEFT_BACK],	rootWorld, displayListOffset);
			createDisplayList(node.octreeNodes[BOTTOM_RIGHT_BACK],	rootWorld, displayListOffset);
			createDisplayList(node.octreeNodes[BOTTOM_RIGHT_FRONT],	rootWorld, displayListOffset);
		}
		else 
		{
			// Make sure we have valid data assigned to this node
			if(node.world == null) return;
	
			// Add our display list offset to our current display list ID
			node.displayListID += displayListOffset;
	
			// Start the display list and assign it to the end nodes ID
			glNewList(node.displayListID,GL_COMPILE);
	
			// Create a temp counter for our while loop below to store the objects drawn
			int counter = 0;
			
			// Store the object count and material count in some local variables for optimization
			int objectCount = node.objectList.size();
			int materialCount = rootWorld.getNumOfMaterials();
	
			// Go through all of the objects that are in our end node
			while(counter < objectCount)
			{
				// Get the first object index into our root world
				int i = node.objectList.get(counter);
	
				// Store pointers to the current face list and the root object 
				// that holds all the data (verts, texture coordinates, normals, etc..)
				Object3d object     = node.world.getObject(i);
				Object3d rootObject = rootWorld.getObject(i);
	
				// Check to see if this object has a texture map, if so, bind the texture to it.
				if(rootObject.getNumTexcoords() > 0) 
				{
					// Turn on texture mapping and turn off color
					glEnable(GL_TEXTURE_2D);
	
					// Reset the color to normal again
					glColor3f(1f, 1f, 1f);
	
					// Bind the texture map to the object by it's materialID
					glBindTexture(GL_TEXTURE_2D, rootObject.getMaterialID());
				} 
				else 
				{
					// Turn off texture mapping and turn on color
					glDisable(GL_TEXTURE_2D);
	
					// Reset the color to normal again
					glColor3f(1f, 1f, 1f);
				}
	
				// Check to see if there is a valid material assigned to this object
				if((materialCount > 0) && (rootObject.getMaterialID() >= 0)) 
				{
					
					byte[] pColor = rootWorld.getMaterials(rootObject.getMaterialID()).getColor();
					
					
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
				if(rootObject.getNumTex() > 0) 
				{
					// Turn on the texture coordinate state
					glEnableClientState(GL_TEXTURE_COORD_ARRAY);
	
					// Point OpenGL to our texture coordinate array.
					// We have them in a pair of 2, of type float and 0 bytes of stride between them.
					//glTexCoordPointer(2, GL_FLOAT, 0, pRootObject->pTexVerts);
					float[] temp = new float[rootObject.getNumTex() * 2];
					int b = 0;
					for(int a=0; a < rootObject.getNumTex(); a++)
					{
						temp[b] = rootObject.getTexcoords()[a].s;
						temp[b+1] = rootObject.getTexcoords()[a].t;
						b +=2;
						
					}
					
					FloatBuffer temp1 = Conversion.allocFloats(temp);
					
					glTexCoordPointer(2, 0, temp1);
				}
	
				// Make sure we have vertices to render
				if(rootObject.getNumVert() > 0)
				{
					// Turn on the vertex array state
					glEnableClientState(GL_VERTEX_ARRAY);
	
					// Point OpenGL to our vertex array.  We have our vertices stored in
					// 3 floats, with 0 stride between them in bytes.
					float[] temp = new float[rootObject.getNumVert() * 3];
					int b = 0;
					for(int a=0; a < rootObject.getNumVert(); a++)
					{
						temp[b] = rootObject.getVertices(a).x;
						temp[b+1] = rootObject.getVertices(a).y;
						temp[b+2] = rootObject.getVertices(a).z;
						b +=3;
						
					}
					
					FloatBuffer temp1 = Conversion.allocFloats(temp);
					glVertexPointer(3, 0, temp1);
				}
	
				// Make sure we have normals to render
				if(rootObject.getNumNorm() > 0)
				{
					// Turn on the normals state
					glEnableClientState(GL_NORMAL_ARRAY);
	
					// Point OpenGL to our normals array.  We have our normals
					// stored as floats, with a stride of 0 between.
					
					float[] temp = new float[rootObject.getNumNorm() * 3];
					int b = 0;
					for(int a=0; a < rootObject.getNumNorm(); a++)
					{
						temp[b] = rootObject.getNormal(a).x;
						temp[b+1] = rootObject.getNormal(a).y;
						temp[b+2] = rootObject.getNormal(a).z;
						b +=3;
						
					}
					
					FloatBuffer temp1 = Conversion.allocFloats(temp);
					glNormalPointer(0, temp1);
				}
	
				// Here we pass in the indices that need to be rendered.  We want to
				// render them in triangles, with numOfFaces * 3 for indice count,
				// and the indices are of type UINT (important).
				int[] temp = new int[object.getIndices().size()]; 
				for(int a=0; a < object.getIndices().size(); a++)
				{
					temp[a] = object.getIndices(a);
					
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
	public boolean intersectLineWithOctree(Octree node, Model3d world, Vector3f line[], Vector3f intersectionPoint )
	{
		// If the passed in node is invalid, leave.
		if ( node == null )
			return false;
		
		
		float left, right, bottom, top, back, front;
		
		Vector3f center = new Vector3f(node.getCenter());
	
		// Find the Left, Right, Front and Back of this Node's AABB.
		left = center.x - node.getWidth();
		right = center.x + node.getWidth();
		bottom = center.y - node.getWidth();
		top = center.y + node.getWidth();
		// Be careful here, depth is different in DirectX's Left handed coordinate system.
		back = center.z - node.getWidth();
		front = center.z + node.getWidth();
	
		// If BOTH Vertices of the Line are not in this Node, than there can not possibly
		// be an intersection, return false.
		if ( octreeCollisionDetection &&
			 (( line[0].x < left || line[0].x > right ) ||
			 ( line[0].y < bottom || line[0].y > top ) ||
			 ( line[0].z < back || line[0].z > front )) 
			 &&
			 (( line[1].x < left || line[1].x > right ) ||
			 ( line[1].y < bottom || line[1].y > top ) ||
			 ( line[1].z < back || line[1].z > front )) )
				return false;
	
		// If this node is subdivided, traverse to it's children.
		if ( node.isSubDivided() )
		{
			// Lots of Logic Tests, but with a purpose. If ANY node comes back saying there was a collision in it or one
			// of it's sub-nodes, return immediately without checking anymore nodes. This echos back recursivly to the root.
			if ( intersectLineWithOctree( node.octreeNodes[TOP_LEFT_FRONT], world, line, intersectionPoint ) )
				return true;
			if ( intersectLineWithOctree( node.octreeNodes[TOP_LEFT_BACK], world, line, intersectionPoint ) )
				return true;
			if ( intersectLineWithOctree( node.octreeNodes[TOP_RIGHT_BACK], world, line, intersectionPoint ) )
				return true;
			if ( intersectLineWithOctree( node.octreeNodes[TOP_RIGHT_FRONT], world, line, intersectionPoint ) )
				return true;
			if ( intersectLineWithOctree( node.octreeNodes[BOTTOM_LEFT_FRONT], world, line, intersectionPoint ) )
				return true;
			if ( intersectLineWithOctree( node.octreeNodes[BOTTOM_LEFT_BACK], world, line, intersectionPoint ) )
				return true;
			if ( intersectLineWithOctree( node.octreeNodes[BOTTOM_RIGHT_BACK], world, line, intersectionPoint ) )
				return true;
			if ( intersectLineWithOctree( node.octreeNodes[BOTTOM_RIGHT_FRONT], world, line, intersectionPoint ) )
				return true;
		}
		else
		{
			// Make sure there is a world to test.
			if (node.world == null )
				return false;
	
			// Increment the Count of how many collisions with terminal Nodes we have encountered.
			numNodesCollided++;
	
			Vector3f[] tempFace = new Vector3f[3];
			int i, j, k;
	
			// Check all of this Nodes World Objects.
			for ( i = 0; i < node.world.getNumOfObjects(); i++ )
			{
				Object3d object = node.world.getObject(i);
	
				// Check all of the Worlds Faces.
				for ( j = 0; j < object.getNumFaces(); j++ )
				{
					// Look at the 3 Vertices of this Face.
					for ( k = 0; k < 3; k++ )
					{
						// Get the Vertex Index;
						int index = object.getFace(j).getVertices(k);
	
						// Now look in the Root World and just get the Vertices we need.
						tempFace[k] = new Vector3f(world.getObject(i).getVertices(index));
					}
					CollisionMath collision = new CollisionMath();
	
					// If we had a Line to Polygon Intersection, return true, which should echo down to the root function call.
					if ( collision.intersectedPolygon( tempFace, line, 3, intersectionPoint ) )
					{
						setObjectColliding(true);
						return true;
					}
				}
			}
		}
	
		// No intersection detected.
		return false;
	}

	public boolean checkCameraCollision(Octree node, Model3d world, CameraQuaternion camera)
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
		if ( node == null )
			return false;
		
		float left, right, bottom, top, back, front;
	
		Vector3f center = new Vector3f(node.getCenter());
	
		// Find the Left, Right, Front and Back of this Node's AABB.
		left = center.x - node.getWidth();
		right = center.x + node.getWidth();
		bottom = center.y - node.getWidth();
		top = center.y + node.getWidth();
		// Be careful here, depth is different in DirectX's Left handed coordinate system.
		back = center.z - node.getWidth();
		front = center.z + node.getWidth();
	
		// If BOTH Vertices of the Line are not in this Node, than there can not possibly
		// be an intersection, return false.
		float radius = camera.getRadius();
		if ( octreeCollisionDetection &&
			 //(( camera.getPosition().x < left || camera.getPosition().x > right ) ||
			 //( camera.getPosition().y < bottom || camera.getPosition().y > top ) ||
			 //( camera.getPosition().z < back || camera.getPosition().z > front )) 
			// &&
			 (( camera.getPosition().x+radius < left || camera.getPosition().x+radius > right ) ||
			 ( camera.getPosition().y+radius < bottom || camera.getPosition().y+radius > top ) ||
			 ( camera.getPosition().z+radius < back || camera.getPosition().z+radius > front )) )
				return false;
		
		
		
		// If this node is subdivided, traverse to it's children.
		if ( node.isSubDivided() )
		{
			// Lots of Logic Tests, but with a purpose. If ANY node comes back saying there was a collision in it or one
			// of it's sub-nodes, return immediately without checking anymore nodes. This echos back recursivly to the root.
			if (checkCameraCollision( node.octreeNodes[TOP_LEFT_FRONT], world, camera))
				return true;
			if (checkCameraCollision( node.octreeNodes[TOP_LEFT_BACK], world, camera))
				return true;
				
			if (checkCameraCollision( node.octreeNodes[TOP_RIGHT_BACK], world, camera ))
				return true;
			if (checkCameraCollision( node.octreeNodes[TOP_RIGHT_FRONT], world, camera ))
				return true;
			if (checkCameraCollision( node.octreeNodes[BOTTOM_LEFT_FRONT], world, camera ))
				return true;
			if (checkCameraCollision( node.octreeNodes[BOTTOM_LEFT_BACK], world, camera ))
				return true;
			if (checkCameraCollision( node.octreeNodes[BOTTOM_RIGHT_BACK], world, camera ))
				return true;
			if (checkCameraCollision( node.octreeNodes[BOTTOM_RIGHT_FRONT], world, camera ))
				return true;
		}
		else
		{
			// Make sure there is a world to test.
			if (node.world == null )
				return false;
	
			// Increment the Count of how many collisions with terminal Nodes we have encountered.
			numNodesCollided++;
	
			Vector3f[] tempFace = new Vector3f[3];
			Vector3f normal = new Vector3f();
			int i, j, k;
	
			// Check all of this Nodes World Objects.
			for ( i = 0; i < node.world.getNumOfObjects(); i++ )
			{
				Object3d object = node.world.getObject(i);
	
				// Check all of the Worlds Faces.
				for ( j = 0; j < object.getNumFaces(); j++ )
				{
					// Look at the 3 Vertices of this Face.
					for ( k = 0; k < 3; k++ )
					{
						// Get the Vertex Index;
						int index = object.getFace(j).getVertices(k);
						
						// Now look in the Root World and just get the Vertices we need.
						tempFace[k] = new Vector3f(world.getObject(i).getVertices(index));
						
					}
					
					normal.setTo(VectorMath.normal(tempFace));
					
					
					CollisionMath collision = new CollisionMath();
					Distance distance = new Distance();
					
					// This is where we determine if the sphere is in FRONT, BEHIND, or INTERSECTS the plane
					int classification = collision.classifySphere(camera.getPosition(), normal, tempFace[0], radius, distance);
					// If the sphere intersects the polygon's plane, then we need to check further
					if(classification == Distance.INTERSECTS) 
					{
						// 2) STEP TWO - Finding the pseudo intersection point on the plane
	
						// Now we want to project the sphere's center onto the triangle's plane
						Vector3f offset = new Vector3f(VectorMath.multiply(normal, distance.distance));
	
						// Once we have the offset to the plane, we just subtract it from the center
						// of the sphere.  "vIntersection" is now a point that lies on the plane of the triangle.
						Vector3f intersection = new Vector3f(VectorMath.subtract(camera.getPosition(), offset));
	
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
	
						if(collision.insidePolygon(intersection, tempFace, 3) ||
						   collision.edgeSphereCollision(camera.getPosition(), tempFace, 3, radius/3))
						{
							// If we get here, we have collided!  To handle the collision detection
							// all it takes is to find how far we need to push the sphere back.
							// GetCollisionOffset() returns us that offset according to the normal,
							// radius, and current distance the center of the sphere is from the plane.
							offset.setTo(collision.getCollisionOffset(normal, radius, distance.distance));
	
							// Now that we have the offset, we want to ADD it to the position and
							// view vector in our camera.  This pushes us back off of the plane.  We
							// don't see this happening because we check collision before we render
							// the scene.
							//vOffset.negate();
							camera.setPosition(VectorMath.add(camera.getPosition(), offset));
							camera.setView(VectorMath.add(camera.getView(), offset));
							
							setObjectColliding(true);
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
		return subDivided;	
	}
	
	//This returns the center of this node
	public Vector3f getCenter() 
	{	 
		return centerNode;	
	}
	
	//This returns the widht of this node (since it's a cube the height and depth are the same)
	public float getWidth() 
	{	 
		return sizeWidth;
	}
	
	//This returns this nodes display list ID
	public int getDisplayListID()		
	{   
		return displayListID;		
	}
	
	// This sets the nodes display list ID
	public void setDisplayListID(int displayListID)	
	{	
		this.displayListID = displayListID;  
	}
	
	
	/**
	 * @param m_TriangleCount the m_TriangleCount to set
	 */
	public void setTriangleCount(int triangleCount) {
		this.triangleCount = triangleCount;
	}
	
	
	/**
	 * @return the m_TriangleCount
	 */
	public int getTriangleCount() {
		return triangleCount;
	}
	
	
	/**
	 * @param objectColliding the objectColliding to set
	 */
	public void setObjectColliding(boolean objectColliding) {
		this.objectColliding = objectColliding;
	}
	
	
	/**
	 * @return the objectColliding
	 */
	public boolean isObjectColliding() {
		return objectColliding;
	}


	/**
	 * @param renderMode the renderMode to set
	 */
	public void setRenderMode(boolean renderMode) {
		this.renderMode = renderMode;
	}


	/**
	 * @return the renderMode
	 */
	public boolean isRenderMode() {
		return renderMode;
	}





}
