package fcampos.rawengine3D.loader;

import fcampos.rawengine3D.MathUtil.*;
import fcampos.rawengine3D.model.*;
import fcampos.rawengine3D.resource.*;
import fcampos.rawengine3D.graficos.*;


public class TMD2Loader {

	// These are the needed defines for the max values when loading .MD2 files
	public static final int MD2_MAX_TRIANGLES	=	4096;
	public static final int MD2_MAX_VERTICES	=	2048;
	public static final int MD2_MAX_TEXCOORDS	=	2048;
	public static final int MD2_MAX_FRAMES		=	512;
	public static final int MD2_MAX_SKINS		=	32;
	public static final int MD2_MAX_FRAMESIZE	=	(MD2_MAX_VERTICES * 4 + 128);
	


	
	// Member variables		

	private TMD2Header				m_Header;			// The header data
	private String[] 				m_pSkins;			// The skin data
	private TMD2TexCoord[]			m_pTexCoords;		// The texture coordinates
	private TMD2Face[]				m_pTriangles;		// Face index information
	private TMD2Frame[]				m_pFrames;			// The frames of animation (vertices)
	private TextureManager texManager = new TextureManager();
	private BinaryLoader loader;
	
	
	
	// This holds the header information that is read in at the beginning of the file
	private class TMD2Header
	{ 
	   int magic;					// This is used to identify the file
	   int version;					// The version number of the file (Must be 8)
	   int skinWidth;				// The skin width in pixels
	   int skinHeight;				// The skin height in pixels
	   int frameSize;				// The size in bytes the frames are
	   int numSkins;				// The number of skins associated with the model
	   int numVertices;				// The number of vertices (constant for each frame)
	   int numTexCoords;			// The number of texture coordinates
	   int numTriangles;			// The number of faces (polygons)
	   int numGlCommands;			// The number of gl commands
	   int numFrames;				// The number of animation frames
	   int offsetSkins;				// The offset in the file for the skin data
	   int offsetTexCoords;			// The offset in the file for the texture data
	   int offsetTriangles;			// The offset in the file for the face data
	   int offsetFrames;			// The offset in the file for the frames data
	   int offsetGlCommands;		// The offset in the file for the gl commands data
	   int offsetEnd;				// The end of the file offset
	   
	   TMD2Header()
	   {
		   magic = loader.readInt();					// This is used to identify the file
		   version = loader.readInt();					// The version number of the file (Must be 8)
		   skinWidth = loader.readInt();				// The skin width in pixels
		   skinHeight = loader.readInt();				// The skin height in pixels
		   frameSize = loader.readInt();				// The size in bytes the frames are
		   numSkins = loader.readInt();				// The number of skins associated with the model
		   numVertices = loader.readInt();				// The number of vertices (constant for each frame)
		   numTexCoords = loader.readInt();			// The number of texture coordinates
		   numTriangles = loader.readInt();			// The number of faces (polygons)
		   numGlCommands = loader.readInt();			// The number of gl commands
		   numFrames = loader.readInt();				// The number of animation frames
		   offsetSkins = loader.readInt();				// The offset in the file for the skin data
		   offsetTexCoords = loader.readInt();			// The offset in the file for the texture data
		   offsetTriangles = loader.readInt();			// The offset in the file for the face data
		   offsetFrames = loader.readInt();			// The offset in the file for the frames data
		   offsetGlCommands = loader.readInt();		// The offset in the file for the gl commands data
		   offsetEnd = loader.readInt();			
	   }
	   
	};

	// This is used to store the vertices that are read in for the current frame
	/*
	private class TMD2AliasTriangle
	{
	   int[] vertex = new int[3];
	   int lightNormalIndex;
	   
	   protected TMD2AliasTriangle(int num)
	   {
		   vertex[0] = Conversion.byte2byte();
		   vertex[1] = Conversion.byte2byte();
		   vertex[2] = Conversion.byte2byte();
		   lightNormalIndex = Conversion.byte2byte();
	   }
	};
*/
	// This stores the normals and vertices for the frames
	private class TMD2Triangle
	{
	  Vector3f vertex = new Vector3f();
	  Vector3f normal = new Vector3f();
	   
	 //  protected TMD2Triangle()
	   //{
		   //vertex.x = Conversion.byte2float();
		   //vertex.y = Conversion.byte2float();
		  // vertex.z = Conversion.byte2float();
		  // normal.x = Conversion.byte2float();
		  // normal.y = Conversion.byte2float();
		  // normal.z = Conversion.byte2float();
	  // }
	};

	// This stores the indices into the vertex and texture coordinate arrays
	private class TMD2Face
	{
	   int[] vertexIndices = new int[3];
	   int[] textureIndices = new int[3];
	   
	   protected TMD2Face()
	   {
		   vertexIndices = new int[] { loader.readShort(), loader.readShort(),
				   loader.readShort() };
           textureIndices = new int[] { loader.readShort(), loader.readShort(),
        		   loader.readShort() };

	   }
	};

	// This stores UV coordinates
	private class TMD2TexCoord
	{
	   float u, v;
	   
	   protected TMD2TexCoord()
	   {
		   u = (float)loader.readShort();
		   v = (float)loader.readShort();
	   }
	};

	
	// This stores the animation scale, translation and name information for a frame, plus verts
	private class TMD2AliasFrame
	{
	   private Vector3f scale = new Vector3f();
	   private Vector3f translate = new Vector3f();
	   private String name;
	  // TMD2AliasTriangle[] aliasVertices = new TMD2AliasTriangle[1];
	   
	   TMD2AliasFrame()
	   {
		   scale.x = loader.readFloat();
		   scale.y = loader.readFloat();
		   scale.z = loader.readFloat();
		   
		   translate.x = loader.readFloat();
		   translate.y = loader.readFloat();
		   translate.z = loader.readFloat();
		   
		   name = loader.readString(16);
		   
		  // aliasVertices[0] = new TMD2AliasTriangle();
	   }
	};

	// This stores the frames vertices after they have been transformed
	private class TMD2Frame
	{
	   String strName;
	   TMD2Triangle[] pVertices;
	   
	  protected TMD2Frame()
	   {
		//   strName = Conversion.byte2string(16); Loader.m_FilePointer += 16;
		   //pVertices = new TMD2Triangle();
	   }
	};


	/** This stores a skin name (We don't use this, just the name of the model to get the texture).
	 */
	/*
	private final class TMD2Skin 
	{
		String strName;							// Skin name

		protected TMD2Skin()
		{
			strName = Conversion.byte2string(64);	Loader.m_FilePointer += 64;
		}
	};
*/
	
///////////////////////////////// IMPORT MD2 \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
/////
/////	This is called by the client to open the .Md2 file, read it, then clean up
/////
///////////////////////////////// IMPORT MD2 \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*

public boolean importMD2(T3dModel pModel, String strFileName, String strTexture)
{
	
try
{
	/*
	// Open the MD2 file in binary
	//File f = new File(strFileName);
	FileInputStream f = new FileInputStream(strFileName);

	  // Wrap a buffer to make reading more efficient (faster).
	//BufferedInputStream bis = new BufferedInputStream(f);
	DataInputStream bis=new DataInputStream(f);
	*/
	
		
	//Loader.fileContents = new byte[(int)f.length()];
	
	  // Read the entire file into memory.
	//bis.read(Loader.fileContents, 0, (int)f.length());
	//bis.readFully(Loader.fileContents);
	
	loader = new BinaryLoader(strFileName);
	  // Close the .md3 file that we opened
	
	//bis.close();
	//System.out.println(Loader.fileContents.length);
	  // Open the MD3 file in binary
	//Loader.m_FilePointer = 0;

	  // Read the header data and store it in our m_Header member variable
	m_Header = new TMD2Header();

	
	  // Make sure the ID == IDP3 and the version is this crazy number '15' or else it's a bad egg
	if(m_Header.version != 8)
	{
		System.out.println("[Error]: file " + strFileName + " version is not valid.");
		System.exit(0);
	}
	
	 
	// Now that we made sure the header had correct data, we want to read in the
	// rest of the data.  Once the data is read in, we need to convert it to our structures.
	// Since we are only reading in the first frame of animation, there will only
	// be ONE object in our t3DObject structure, held within our pModel variable.
	readMD2Data();
	// Here we pass in our model structure to it can store the read Quake data
	// in our own model and object structure data
	convertDataStructures(pModel);

	// After we have read the whole MD2 file, we want to calculate our own vertex normals.
	//VectorMath.computeNormals(pModel);
	
	// If there is a valid texture name passed in, we want to set the texture data
	if(strTexture != null)
	{
		// Create a local material info structure
		TMaterialInfo texture = new TMaterialInfo();
		
		
		//Texture tex = texManager.getNormalImage("texturas/" + strTexture,true, true);
		Texture tex = texManager.getFlippedImage(strTexture,true, false);
		//Texture tex = texManager.getMirrorImage("texturas/" + strTexture,true, true);
		
		texture.setTex(tex);
		// Copy the name of the file into our texture file name variable
		texture.setTexFile(tex.getName());
		
		// Since there is only one texture for a .Md2 file, the ID is always 0
		texture.setTexureId(tex.getTexID());

		// The tile or scale for the UV's is 1 to 1 (but Quake saves off a 0-256 ratio)
		texture.setUTile(1);
		texture.setVTile(1);

		// We only have 1 material for a model
		//pModel.setNumOfMaterials(1);

		// Add the local material info to our model's material list
		pModel.addPMaterials(texture);
		pModel.getPObject(pModel.getPMaterials().size()-1).setMaterialID(tex.getTexID());
		
	}


}
catch(Exception e)
{
	e.getMessage();
	//System.out.println("[Error]: can't read " + strFileName + " correctly.");
	System.exit(0);
}

	// Return a success
	return true;
}

///////////////////////////////// READ MD2 DATA \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
/////
/////	This function reads in all of the model's data, except the animation frames
/////
///////////////////////////////// READ MD2 DATA \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*

private void readMD2Data() throws Exception
{


// Here we allocate all of our memory from the header's information
m_pSkins = new String[m_Header.numSkins];
m_pTexCoords = new TMD2TexCoord [m_Header.numTexCoords];
m_pTriangles = new TMD2Face [m_Header.numTriangles];
m_pFrames    = new TMD2Frame [m_Header.numFrames];


//start with skins. Move the file pointer to the correct position.
loader.setOffset(m_Header.offsetSkins);
//Read in each skin for this model
for (int j = 0; j < m_Header.numSkins; j++) {
	m_pSkins[j] = loader.readString(64);
}
	
	// Now read in texture coordinates.
    loader.setOffset(m_Header.offsetTexCoords);
    for (int j = 0; j < m_Header.numTexCoords; j++) {
    	m_pTexCoords[j] = new TMD2TexCoord();
        
    }


    // read the vertex data.
    loader.setOffset(m_Header.offsetTriangles);
    for (int j = 0; j < m_Header.numTriangles; j++) {
    	m_pTriangles[j] = new TMD2Face();
    }
    
    loader.setOffset(m_Header.offsetFrames);
    // Each keyframe has the same type of data, so read each
    // keyframe one at a time.
    for (int i = 0; i < m_Header.numFrames; i++) {
    	TMD2AliasFrame frame = new TMD2AliasFrame();
        m_pFrames[i] = new TMD2Frame();

        m_pFrames[i].pVertices = new TMD2Triangle[m_Header.numVertices];
        Vector3f[] aliasVertices = new Vector3f[m_Header.numVertices];
        int[] aliasLightNormals = new int[m_Header.numVertices];

        // Read in the first frame of animation
        for (int j = 0; j < m_Header.numVertices; j++) {
            aliasVertices[j] = new Vector3f(loader.readByte(), loader.readByte(),loader.readByte());
            aliasLightNormals[j] = loader.readByte();
        }

     // Copy the name of the animation to our frames array
        m_pFrames[i].strName = frame.name;
        TMD2Triangle[] verices = m_pFrames[i].pVertices;

        for (int j = 0; j < m_Header.numVertices; j++) {
            verices[j] = new TMD2Triangle();
            verices[j].vertex.x = aliasVertices[j].x * frame.scale.x
                    + frame.translate.x;
            verices[j].vertex.z = -1
                    * (aliasVertices[j].y * frame.scale.y + frame.translate.y);
            verices[j].vertex.y = aliasVertices[j].z * frame.scale.z
                    + frame.translate.z;

            
        }
    }
    loader.setOffset(m_Header.offsetGlCommands);


}

///////////////////////////////// PARSE ANIMATIONS \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
/////
/////	This function fills in the animation list for each animation by name and frame
/////
///////////////////////////////// PARSE ANIMATIONS \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*

public void parseAnimations(T3dModel pModel)
{
	TAnimationInfo animation = new TAnimationInfo();
	String strLastName = "";

// This function felt like a hack when I wrote it.  You aren't really given
// any good information about the animations, other than the fact that each
// key frame has a name assigned to it with a frame number for that animation.
// For instance, the first animation is the "stand" animation.  The first frame
// would have the name of: "stand01" or perhaps "stand1".  The 40th frame is
// usually the last frame for the standing animation, so it would look like:
// "stand40".  After this frame, the next animation is the running animation.
// The next frame is labeled something like "run01".  You now know that the
// standing animation is from frame 1 to frame 40 of the total frames.  The
// start of the run animation is 41.  We will know how long the run animation
// goes when we run into another animation name besides "run..".  That is how
// I went about finding out the animation information.  I just grab each frame
// name and check if it's the same animation name as the last name we found.
// If it is, I just ignore it and continue to the next frame.  Once I find that
// it's not, I then have the last frame saved off from the index 'i', and then
// I create a new animation to add to the list, then start from the beginning.
// It wasn't until later that I found on www.planetquake.com that there is a
// standard frame count for each animation and they are all the same.  I figure
// this way makes it modular so you don't have to stick to those standards anyway.

// Go through all of the frames of animation and parse each animation
for(int i = 0; i < pModel.getNumOfObjects(); i++)
{
	// Assign the name of this frame of animation to a string object
	String strName  = m_pFrames[i].strName;
	char[] temp = strName.toCharArray();
	int frameNum = 0;
	
	//int temp = strName.codePointCount(strName.length()-2, strName.length());
	
	// Go through and extract the frame numbers and erase them from the name
	for(int j = 0; j < temp.length; j++)
	{
		
		// If the current index is a number and it's one of the last 2 characters of the name
		if( Character.isDigit(temp[j]) && j >= temp.length - 2)
		{
			// Use a C function to convert the character to a integer.
			// Notice we use the address to pass in the current character and on
			//int b = j;
			//int a = temp.length;
			frameNum = Integer.parseInt(strName.substring(j, temp.length));

			// Erase the frame number from the name so we extract the animation name
			//strName.erase(j, strName.length() - j);
			strName = strName.substring(0, j);
			break;
		}
	}

	// Check if this animation name is not the same as the last frame,
	// or if we are on the last frame of animation for this model
	if(!strName.equalsIgnoreCase(strLastName) || i == pModel.getPObject().size() - 1)
	{
		// If this animation frame is NOT the first frame
		if(strLastName != "")
		{
			// Copy the last animation name into our new animation's name
			animation.setAnimName(strLastName);

			// Set the last frame of this animation to i
			animation.setEndFrame(i);

			// Add the animation to our list and reset the animation object for next time
			pModel.addAnimations(animation);
			animation = new TAnimationInfo();
			//memset(&animation, 0, sizeof(tAnimationInfo));

			// Increase the number of animations for this model
			//pModel->numOfAnimations++;
		}

		// Set the starting frame number to the current frame number we just found,
		// minus 1 (since 0 is the first frame) and add 'i'.
		animation.setStartFrame(frameNum - 1 + i);
	}

	// Store the current animation name in the strLastName string to check it latter
	strLastName = strName;
}
}

///////////////////////////////// CONVERT DATA STRUCTURES \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
/////
/////	This function converts the .md2 structures to our own model and object structures
/////
///////////////////////////////// CONVERT DATA STRUCTURES \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*

public void convertDataStructures(T3dModel pModel)
{
int j = 0;

// Assign the number of objects, which is 1 since we only want 1 frame
// of animation.  In the next tutorial each object will be a key frame
// to interpolate between.
pModel.setNumOfObjects(m_Header.numFrames);



//Create our animation list and store it in our model
parseAnimations(pModel);

for(int i =0; i < pModel.getNumOfObjects(); i++)
{
// Create a local object to store the first frame of animation's data
TMD2Object currentFrame = new TMD2Object();

// Assign the vertex, texture coord and face count to our new structure
currentFrame.setNumVert(m_Header.numVertices);


// Allocate memory for the vertices, texture coordinates and face data.
//currentFrame.pVerts    = new CVector3 [currentFrame.numOfVerts];
//currentFrame.pTexVerts = new CVector2 [currentFrame.numTexVertex];
//currentFrame.pFaces    = new tFace [currentFrame.numOfFaces];

// Go through all of the vertices and assign them over to our structure
for (j=0; j < currentFrame.getNumVert(); j++)
{
	Vector3f temp = new Vector3f(m_pFrames[i].pVertices[j].vertex.x, m_pFrames[i].pVertices[j].vertex.y,
								m_pFrames[i].pVertices[j].vertex.z);
	
	currentFrame.setVertices(temp, j);
	
}

//Check if we are past the first key frame
if(i > 0)
{
	// Here we add the current object (or frame) to our list object list
	pModel.addPObject(currentFrame);
	continue;	// Go on to the next key frame
}

//We will only get here ONE because we just need this information
// calculated for the first key frame.

// We can now free the old vertices stored in this frame of animation
//delete m_pFrames[0].pVertices;
currentFrame.setNumTex(m_Header.numTexCoords);
currentFrame.setNumFaces(m_Header.numTriangles);

// Go through all of the uv coordinates and assign them over to our structure.
// The UV coordinates are not normal uv coordinates, they have a pixel ratio of
// 0 to 256.  We want it to be a 0 to 1 ratio, so we divide the u value by the
// skin width and the v value by the skin height.  This gives us our 0 to 1 ratio.
// For some reason also, the v coodinate is flipped upside down.  We just subtract
// the v coordinate from 1 to remedy this problem.
for (j=0; j < currentFrame.getNumTexcoords(); j++)
{
	Vector3f temp = new Vector3f((float)(m_pTexCoords[j].u / (float)(m_Header.skinWidth)), 
								(float)(1 - m_pTexCoords[j].v / (float)(m_Header.skinHeight)), 0);
	currentFrame.setTexcoords(temp, j);
	
}

// Go through all of the face data and assign it over to OUR structure
for(j=0; j < currentFrame.getNumFaces(); j++)
{
	
	currentFrame.getFace(j).setVertices(0, m_pTriangles[j].vertexIndices[0]);
	currentFrame.getFace(j).setVertices(1, m_pTriangles[j].vertexIndices[1]);
	currentFrame.getFace(j).setVertices(2, m_pTriangles[j].vertexIndices[2]);
	// Assign the vertex indices to our face data
	//currentFrame.pFaces[j].vertIndex[0] = m_pTriangles[j].vertexIndices[0];
	//currentFrame.pFaces[j].vertIndex[1] = m_pTriangles[j].vertexIndices[1];
	//currentFrame.pFaces[j].vertIndex[2] = m_pTriangles[j].vertexIndices[2];
	
	
	
	currentFrame.getFace(j).setTexCoords(0, m_pTriangles[j].textureIndices[0]);
	currentFrame.getFace(j).setTexCoords(1, m_pTriangles[j].textureIndices[1]);
	currentFrame.getFace(j).setTexCoords(2, m_pTriangles[j].textureIndices[2]);
	// Assign the texture coord indices to our face data
	//currentFrame.pFaces[j].coordIndex[0] = m_pTriangles[j].textureIndices[0];
	//currentFrame.pFaces[j].coordIndex[1] = m_pTriangles[j].textureIndices[1];
	//currentFrame.pFaces[j].coordIndex[2] = m_pTriangles[j].textureIndices[2];
}
currentFrame.setDimension();
// Here we add the current object (or frame) to our list object list
pModel.addPObject(currentFrame);
}

}
}
