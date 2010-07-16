package fcampos.rawengine3D.loader;

import java.io.*;

import fcampos.rawengine3D.model.*;
import fcampos.rawengine3D.MathUtil.*;
//This file handles all of the code needed to load a .3DS file.
//Basically, how it works is, you load a chunk, then you check
//the chunk ID.  Depending on the chunk ID, you load the information
//that is stored in that chunk.  If you do not want to read that information,
//you read past it.  You know how many bytes to read past the chunk because
//every chunk stores the length in bytes of that chunk.

///////////////////////////////// CLOAD3DS \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
/////
/////	This constructor initializes the tChunk data
/////
///////////////////////////////// CLOAD3DS \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
public class T3dsLoader{
	
	//>------ Primary Chunk, at the beginning of each file
	private static final int PRIMARY =      0x4D4D;

	//>------ Main Chunks
	private static final int OBJECTINFO =   0x3D3D;				// This gives the version of the mesh and is found right before the material and object information
	private static final int VERSION    =   0x0002;				// This gives the version of the .3ds file
	private static final int EDITKEYFRAME = 0xB000;				// This is the header for all of the key frame info

	//>------ sub defines of OBJECTINFO
	private static final int MATERIAL	=  	  0xAFFF;				// This stored the texture info
	private static final int OBJECT	=	  0x4000;				// This stores the faces, vertices, etc...

	//>------ sub defines of MATERIAL
	private static final int MATNAME  =    0xA000;				// This holds the material name
	private static final int MATDIFFUSE =  0xA020;				// This holds the color of the object/material
	private static final int MATMAP     =  0xA200;				// This is a header for a new material
	private static final int MATMAPFILE =  0xA300;				// This holds the file name of the texture

	private static final int OBJECT_MESH = 0x4100;				// This lets us know that we are reading a new object

	//>------ sub defines of OBJECT_MESH
	private static final int OBJECT_VERTICES  =   0x4110;			// The objects vertices
	private static final int OBJECT_FACES	 =      0x4120;			// The objects faces
	private static final int OBJECT_MATERIAL	=	0x4130;			// This is found if the object has a material, either texture map or color
	private static final int OBJECT_UV		=	0x4140;			// The UV texture coordinates
	
	
	private TChunk m_CurrentChunk;
	private TChunk m_TempChunk;
	private boolean endOfStream = false;


public T3dsLoader()
{
	m_CurrentChunk = new TChunk();				// Initialize and allocate our current chunk
	m_TempChunk = new TChunk();					// Initialize and allocate a temporary chunk
}

///////////////////////////////// IMPORT 3DS \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
/////
/////	This is called by the client to open the .3ds file, read it, then clean up
/////
///////////////////////////////// IMPORT 3DS \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*

public void import3DS(T3dModel pModel, String strFileName) throws IOException
{
	System.out.println(">> Importing scene from 3ds stream ...");
	// Open the 3DS file
	File file = new File(strFileName);
	System.out.println(file.getAbsolutePath());
	FileInputStream inStream = new FileInputStream(file);
    BufferedInputStream in = new BufferedInputStream(inStream);
    try {
    	// Once we have the file open, we need to read the very first data chunk
    	// to see if it's a 3DS file.  That way we don't read an invalid file.
    	// If it is a 3DS file, then the first chunk ID will be equal to PRIMARY (some hex num)

    	// Read the first chuck of the file to see if it's a 3DS file
    	readChunk(m_CurrentChunk, in);

    	// Make sure this is a 3DS file
        if (m_CurrentChunk.getID() != PRIMARY)
    	{
    		System.out.println("Unable to load PRIMARY chuck from file: " + strFileName);
    		
    	}
        //while (!endOfStream) {
        //    readNextJunk(in); // will load into currentObject
       // }
    }
    catch(IOException e)
	{
		e.getMessage();
	}
    
	
	// Now we actually start reading in the data.  ProcessNextChunk() is recursive

	// Begin loading objects, by calling this recursive function
    processNextChunk(pModel, m_CurrentChunk, in);
    
	// After we have read the whole 3DS file, we want to calculate our own vertex normals.
	VectorMath.computeNormals(pModel);

	
	
}



///////////////////////////////// PROCESS NEXT CHUNK\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
/////
/////	This function reads the main sections of the .3DS file, then dives deeper with recursion
/////
///////////////////////////////// PROCESS NEXT CHUNK\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*

public void processNextChunk(T3dModel pModel, TChunk pPreviousChunk, InputStream in) throws IOException
{
	T3dObject newObject;					// This is used to add to our object list
	TMaterialInfo newTexture;				// This is used to add to our material list
	int version = 0;					// This will hold the file version
	

	m_CurrentChunk = new TChunk();				// Allocate a new chunk				

	// Below we check our chunk ID each time we read a new chunk.  Then, if
	// we want to extract the information from that chunk, we do so.
	// If we don't want a chunk, we just read past it.  

	// Continue to read the sub chunks until we have reached the length.
	// After we read ANYTHING we add the bytes read to the chunk and then check
	// check against the length.
	while (pPreviousChunk.getBytesRead() < pPreviousChunk.getLength())
	{
		// Read next Chunk
		readChunk(m_CurrentChunk, in);

		// Check the chunk ID
		switch (m_CurrentChunk.getID())
		{
		case VERSION:							// This holds the version of the file
			
			// This chunk has an unsigned short that holds the file version.
			// Since there might be new additions to the 3DS file format in 4.0,
			// we give a warning to that problem.
			version = readData(in, m_CurrentChunk.getLength() - m_CurrentChunk.getBytesRead());
			// Read the file version and add the bytes read to our bytesRead variable
			//m_CurrentChunk->bytesRead += fread(&version, 1, m_CurrentChunk->length - m_CurrentChunk->bytesRead, m_FilePointer);
			m_CurrentChunk.addBytesRead(m_CurrentChunk.getLength() - m_CurrentChunk.getBytesRead());

			// If the file version is over 3, give a warning that there could be a problem
			if (version > 0x03)
				System.out.println("This 3DS file is over version 3 so it may load incorrectly");
			break;

		case OBJECTINFO:						// This holds the version of the mesh
			
			// This chunk holds the version of the mesh.  It is also the head of the MATERIAL
			// and OBJECT chunks.  From here on we start reading in the material and object info.

			// Read the next chunk
			readChunk(m_TempChunk, in);
			
			version = readData(in, m_TempChunk.getLength() - m_TempChunk.getBytesRead());
			// Get the version of the mesh
			//m_TempChunk->bytesRead += fread(&version, 1, m_TempChunk->length - m_TempChunk->bytesRead, m_FilePointer);
			m_TempChunk.addBytesRead(m_TempChunk.getLength() - m_TempChunk.getBytesRead());

			// Increase the bytesRead by the bytes read from the last chunk
			//m_CurrentChunk->bytesRead += m_TempChunk->bytesRead;
			m_CurrentChunk.addBytesRead(m_TempChunk.getBytesRead());

			// Go to the next chunk, which is the object has a texture, it should be MATERIAL, then OBJECT.
			processNextChunk(pModel, m_CurrentChunk, in);
			break;

		case MATERIAL:							// This holds the material information

			// This chunk is the header for the material info chunks

			// Increase the number of materials
			//pModel->numOfMaterials++;
			newTexture = new TMaterialInfo();
			pModel.addNumOfMaterials(1);
			// Add a empty texture structure to our texture list.
			// If you are unfamiliar with STL's "vector" class, all push_back()
			// does is add a new node onto the list.  I used the vector class
			// so I didn't need to write my own link list functions.  
			pModel.addPMaterials(newTexture);

			// Proceed to the material loading function
			processNextMaterialChunk(pModel, m_CurrentChunk, in);
			break;

		case OBJECT:							// This holds the name of the object being read
				
			// This chunk is the header for the object info chunks.  It also
			// holds the name of the object.

			// Increase the object count
			//pModel->numOfObjects++;
			newObject = new T3dObject();
			pModel.addNumOfObjects(1);
			
			// Add a new tObject node to our list of objects (like a link list)
			pModel.addPObject(newObject);
			
			// Initialize the object and all it's data members
			//memset(&(pModel->pObject[pModel->numOfObjects - 1]), 0, sizeof(t3DObject));
			
			
			String tempName = readString(in);
			newObject.setName(tempName);
			
			// Get the name of the object and store it, then add the read bytes to our byte counter.
			m_CurrentChunk.addBytesRead(tempName.length()+1);
			
			// Now proceed to read in the rest of the object information
			processNextObjectChunk(pModel, pModel.getPObject(pModel.getNumOfObjects()-1), m_CurrentChunk, in);
			break;

		case EDITKEYFRAME:

			// Because I wanted to make this a SIMPLE tutorial as possible, I did not include
			// the key frame information.  This chunk is the header for all the animation info.
			// In a later tutorial this will be the subject and explained thoroughly.
			
			//ProcessNextKeyFrameChunk(pModel, m_CurrentChunk);
			skipJunk(m_CurrentChunk, in);
			// Read past this chunk and add the bytes read to the byte counter
			m_CurrentChunk.addBytesRead(m_CurrentChunk.getLength() - m_CurrentChunk.getBytesRead());
			//m_CurrentChunk->bytesRead += fread(buffer, 1, m_CurrentChunk->length - m_CurrentChunk->bytesRead, m_FilePointer);
			break;

		default: 
			
			// If we didn't care about a chunk, then we get here.  We still need
			// to read past the unknown or ignored chunk and add the bytes read to the byte counter.
			skipJunk(m_CurrentChunk, in);
			// Read past this chunk and add the bytes read to the byte counter
			m_CurrentChunk.addBytesRead(m_CurrentChunk.getLength() - m_CurrentChunk.getBytesRead());
			
			//m_CurrentChunk->bytesRead += fread(buffer, 1, m_CurrentChunk->length - m_CurrentChunk->bytesRead, m_FilePointer);
			break;
		}

		// Add the bytes read from the last chunk to the previous chunk passed in.
		pPreviousChunk.addBytesRead(m_CurrentChunk.getBytesRead());
	}

	// Free the current chunk and set it back to the previous chunk (since it started that way)
	//delete m_CurrentChunk;
	m_CurrentChunk.setTo(pPreviousChunk);
}


///////////////////////////////// PROCESS NEXT OBJECT CHUNK \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
/////
/////	This function handles all the information about the objects in the file
/////
///////////////////////////////// PROCESS NEXT OBJECT CHUNK \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*

public void processNextObjectChunk(T3dModel pModel, T3dObject pObject, TChunk pPreviousChunk, InputStream in) throws IOException
{
	

	// Allocate a new chunk to work with
	m_CurrentChunk = new TChunk();

	// Continue to read these chunks until we read the end of this sub chunk
	while (pPreviousChunk.getBytesRead() < pPreviousChunk.getLength())
	{
		// Read the next chunk
		readChunk(m_CurrentChunk, in);

		// Check which chunk we just read
		switch (m_CurrentChunk.getID())
		{
		case OBJECT_MESH:					// This lets us know that we are reading a new object
		
			// We found a new object, so let's read in it's info using recursion
			processNextObjectChunk(pModel, pObject, m_CurrentChunk, in);
			break;

		case OBJECT_VERTICES:				// This is the objects vertices
			readVertices(pObject, m_CurrentChunk, in);
			break;

		case OBJECT_FACES:					// This is the objects face information
			readVertexIndices(pObject, m_CurrentChunk, in);
			break;

		case OBJECT_MATERIAL:				// This holds the material name that the object has
			
			// This chunk holds the name of the material that the object has assigned to it.
			// This could either be just a color or a texture map.  This chunk also holds
			// the faces that the texture is assigned to (In the case that there is multiple
			// textures assigned to one object, or it just has a texture on a part of the object.
			// Since most of my game objects just have the texture around the whole object, and 
			// they aren't multitextured, I just want the material name.

			// We now will read the name of the material assigned to this object
			readObjectMaterial(pModel, pObject, m_CurrentChunk, in);			
			break;

		case OBJECT_UV:						// This holds the UV texture coordinates for the object

			// This chunk holds all of the UV coordinates for our object.  Let's read them in.
			readUVCoordinates(pObject, m_CurrentChunk, in);
			break;

		default:  

			// Read past the ignored or unknown chunks
			skipJunk(m_CurrentChunk, in);
		// Read past this chunk and add the bytes read to the byte counter
			m_CurrentChunk.addBytesRead(m_CurrentChunk.getLength() - m_CurrentChunk.getBytesRead());
			
			//m_CurrentChunk->bytesRead += fread(buffer, 1, m_CurrentChunk->length - m_CurrentChunk->bytesRead, m_FilePointer);
			break;
		}

		// Add the bytes read from the last chunk to the previous chunk passed in.
		pPreviousChunk.addBytesRead(m_CurrentChunk.getBytesRead());
	}

	// Free the current chunk and set it back to the previous chunk (since it started that way)
	//delete m_CurrentChunk;
	m_CurrentChunk.setTo(pPreviousChunk);
}


///////////////////////////////// PROCESS NEXT MATERIAL CHUNK \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
/////
/////	This function handles all the information about the material (Texture)
/////
///////////////////////////////// PROCESS NEXT MATERIAL CHUNK \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*

public void processNextMaterialChunk(T3dModel pModel, TChunk pPreviousChunk, InputStream in) throws IOException
{
	

	// Allocate a new chunk to work with
	m_CurrentChunk = new TChunk();

	// Continue to read these chunks until we read the end of this sub chunk
	while (pPreviousChunk.getBytesRead() < pPreviousChunk.getLength())
	{
		// Read the next chunk
		readChunk(m_CurrentChunk, in);

		// Check which chunk we just read in
		switch (m_CurrentChunk.getID())
		{
		case MATNAME:							// This chunk holds the name of the material
			
			// Here we read in the material name
			String tempName = readString(in);
			pModel.getPMaterials(pModel.getNumOfMaterials()-1).setName(tempName);
			m_CurrentChunk.addBytesRead(tempName.length()+1);  //modificado
			//m_CurrentChunk.addBytesRead(m_CurrentChunk.getLength() - m_CurrentChunk.getBytesRead());
			//m_CurrentChunk->bytesRead += fread(pModel->pMaterials[pModel->numOfMaterials - 1].strName, 1, m_CurrentChunk->length - m_CurrentChunk->bytesRead, m_FilePointer);
			break;

		case MATDIFFUSE:						// This holds the R G B color of our object
			
			
			readColorChunk(pModel.getPMaterials(pModel.getNumOfMaterials()-1), m_CurrentChunk, in);
			break;
		
		case MATMAP:							// This is the header for the texture info
			
			// Proceed to read in the material information
			processNextMaterialChunk(pModel, m_CurrentChunk, in);
			break;

		case MATMAPFILE:						// This stores the file name of the material

			// Here we read in the material's file name
			String tempName1 = readString(in);
			pModel.getPMaterials(pModel.getNumOfMaterials()-1).setTexFile(tempName1);
			m_CurrentChunk.addBytesRead(tempName1.length()+1);    //modificado
			//m_CurrentChunk.addBytesRead(m_CurrentChunk.getLength() - m_CurrentChunk.getBytesRead());
			//m_CurrentChunk->bytesRead += fread(pModel->pMaterials[pModel->numOfMaterials - 1].strFile, 1, m_CurrentChunk->length - m_CurrentChunk->bytesRead, m_FilePointer);
			break;
		
		default:  

			skipJunk(m_CurrentChunk, in);
		// Read past this chunk and add the bytes read to the byte counter
		   m_CurrentChunk.addBytesRead(m_CurrentChunk.getLength() - m_CurrentChunk.getBytesRead());
			break;
		}

		// Add the bytes read from the last chunk to the previous chunk passed in.
		pPreviousChunk.addBytesRead(m_CurrentChunk.getBytesRead());
	}

	// Free the current chunk and set it back to the previous chunk (since it started that way)
	//m_CurrentChunk = null;
	m_CurrentChunk.setTo(pPreviousChunk);
}

///////////////////////////////// READ CHUNK \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
/////
/////	This function reads in a chunk ID and it's length in bytes
/////
///////////////////////////////// READ CHUNK \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*

public void readChunk(TChunk pChunk, InputStream in) throws IOException
{
	// This reads the chunk ID which is 2 bytes.
	// The chunk ID is like OBJECT or MATERIAL.  It tells what data is
	// able to be read in within the chunks section.  
	
	pChunk.setID(readData(in, Short.SIZE/8));
    pChunk.setLength(readData(in, Integer.SIZE/8));
    pChunk.setBytesRead(6);
    endOfStream = pChunk.getID() < 0;
	//pChunk->bytesRead = fread(&pChunk->ID, 1, 2, m_FilePointer);

	// Then, we read the length of the chunk which is 4 bytes.
	// This is how we know how much to read in, or read past.
	//pChunk->bytesRead += fread(&pChunk->length, 1, 4, m_FilePointer);
}



///////////////////////////////// READ COLOR \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
/////
/////	This function reads in the RGB color data
/////
///////////////////////////////// READ COLOR \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*

public void readColorChunk(TMaterialInfo pMaterial, TChunk pChunk, InputStream in) throws IOException
{
	// Read the color chunk info
	readChunk(m_TempChunk, in);
	assert(m_TempChunk.getLength() - m_TempChunk.getBytesRead() == 3):" fudeu ";
	in.read(pMaterial.getColor());
	//pMaterial.setColor();
	m_TempChunk.addBytesRead(m_TempChunk.getLength() - m_TempChunk.getBytesRead());
	
	
	// Read in the R G B color (3 bytes - 0 through 255)
	//m_TempChunk->bytesRead += fread(pMaterial->color, 1, m_TempChunk->length - m_TempChunk->bytesRead, m_FilePointer);

	// Add the bytes read to our chunk
	pChunk.addBytesRead(m_TempChunk.getBytesRead());
}


///////////////////////////////// READ VERTEX INDECES \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
/////
/////	This function reads in the indices for the vertex array
/////
///////////////////////////////// READ VERTEX INDECES \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*

public void readVertexIndices(T3dObject pObject, TChunk pPreviousChunk, InputStream in) throws IOException
{
	 int index = 0;					// This is used to read in the current face index

	// In order to read in the vertex indices for the object, we need to first
	// read in the number of them, then read them in.  Remember,
	// we only want 3 of the 4 values read in for each face.  The fourth is
	// a visibility flag for 3D Studio Max that doesn't mean anything to us.
	 
	// Read in the number of faces that are in this object (int)
	pObject.setNumFaces(readData(in, Short.SIZE/8));
	pPreviousChunk.addBytesRead(Short.SIZE/8);
	//pPreviousChunk->bytesRead += fread(&pObject->numOfFaces, 1, 2, m_FilePointer);

	// Alloc enough memory for the faces and initialize the structure
	//pObject->pFaces =   new tFace [pObject->numOfFaces];
	//memset(pObject->pFaces, 0, sizeof(tFace)  * pObject->numOfFaces);

	// Go through all of the faces in this object
	for(int i = 0; i < pObject.getNumFaces(); i++)
	{
		// Next, we read in the A then B then C index for the face, but ignore the 4th value.
		// The fourth value is a visibility flag for 3D Studio Max, we don't care about this.
		for(int j = 0; j < 4; j++)
		{
		
			// Read the first vertice index for the current face 
			index = readData(in, Short.SIZE/8);
			pPreviousChunk.addBytesRead(Short.SIZE/8);
			//pPreviousChunk->bytesRead += fread(&index, 1, sizeof(index), m_FilePointer);
			
			if(j < 3)
			{
				// Store the index in our face structure.
				pObject.getFace(i).setVertices(j, index);
			}
		}
	}
}


///////////////////////////////// READ UV COORDINATES \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
/////
/////	This function reads in the UV coordinates for the object
/////
///////////////////////////////// READ UV COORDINATES \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*

public void readUVCoordinates(T3dObject pObject, TChunk pPreviousChunk, InputStream in) throws IOException
{
	// In order to read in the UV indices for the object, we need to first
	// read in the amount there are, then read them in.

	// Read in the number of UV coordinates there are (int)
	pObject.setNumTexcoords(readData(in, Short.SIZE/8));
	pPreviousChunk.addBytesRead(Short.SIZE/8);
	//pPreviousChunk->bytesRead += fread(&pObject->numTexVertex, 1, 2, m_FilePointer);

	// Allocate memory to hold the UV coordinates
	//pObject->pTexVerts = new tVector2 [pObject->numTexVertex];

	// Read in the texture coodinates (an array 2 float)
	assert((pPreviousChunk.getLength() - pPreviousChunk.getBytesRead()) == (pObject.getNumTexcoords() * 2 * Float.SIZE/8)):"fudeu vert";
	pPreviousChunk.addBytesRead(pPreviousChunk.getLength() - pPreviousChunk.getBytesRead());
	
	
	for(int i = 0; i < pObject.getNumTexcoords(); i++)
	{
		
		pObject.getTexcoords(i).s = readFloat(in);
		pObject.getTexcoords(i).t = readFloat(in);
		
	}	
	
}


///////////////////////////////// READ VERTICES \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
/////
/////	This function reads in the vertices for the object
/////
///////////////////////////////// READ VERTICES \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*

public void readVertices(T3dObject pObject, TChunk pPreviousChunk, InputStream in) throws IOException
{
	// Like most chunks, before we read in the actual vertices, we need
	// to find out how many there are to read in.  Once we have that number
	// we then fread() them into our vertice array.

	// Read in the number of vertices (int)
	pObject.setNumVert(readData(in, Short.SIZE/8));
	pPreviousChunk.addBytesRead(Short.SIZE/8);
	//pPreviousChunk->bytesRead += fread(&(pObject->numOfVerts), 1, 2, m_FilePointer);

	// Allocate the memory for the verts and initialize the structure
	//pObject->pVerts = new CVector3 [pObject->numOfVerts];
	//memset(pObject->pVerts, 0, sizeof(CVector3) * pObject->numOfVerts);

	// Read in the array of vertices (an array of 3 floats)
	//pPreviousChunk->bytesRead += fread(pObject->pVerts, 1, pPreviousChunk->length - pPreviousChunk->bytesRead, m_FilePointer);
	assert((pPreviousChunk.getLength() - pPreviousChunk.getBytesRead()) == (pObject.getNumVert() * 3 * 4)):"fudeu vert";
	pPreviousChunk.addBytesRead(pPreviousChunk.getLength() - pPreviousChunk.getBytesRead());
	
	// Now we should have all of the vertices read in.  Because 3D Studio Max
	// Models with the Z-Axis pointing up (strange and ugly I know!), we need
	// to flip the y values with the z values in our vertices.  That way it
	// will be normal, with Y pointing up.  If you prefer to work with Z pointing
	// up, then just delete this next loop.  Also, because we swap the Y and Z
	// we need to negate the Z to make it come out correctly.
	
	// Go through all of the vertices that we just read and swap the Y and Z values
	for(int i = 0; i < pObject.getNumVert(); i++)
	{
		
		pObject.getVertices(i).x = readFloat(in);
		pObject.getVertices(i).z = - readFloat(in);
		pObject.getVertices(i).y = readFloat(in);
		
		
		
		
	}
}


///////////////////////////////// READ OBJECT MATERIAL \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
/////
/////	This function reads in the material name assigned to the object and sets the materialID
/////
///////////////////////////////// READ OBJECT MATERIAL \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*

public void readObjectMaterial(T3dModel pModel, T3dObject pObject, TChunk pPreviousChunk, InputStream in) throws IOException
{
	String strMaterial;			// This is used to hold the objects material name
	

	// *What is a material?*  - A material is either the color or the texture map of the object.
	// It can also hold other information like the brightness, shine, etc... Stuff we don't
	// really care about.  We just want the color, or the texture map file name really.

	// Here we read the material name that is assigned to the current object.
	// strMaterial should now have a string of the material name, like "Material #2" etc..
	strMaterial = readString(in);
	pPreviousChunk.addBytesRead(strMaterial.length()+1);
	//pPreviousChunk->bytesRead += GetString(strMaterial);

	// Now that we have a material name, we need to go through all of the materials
	// and check the name against each material.  When we find a material in our material
	// list that matches this name we just read in, then we assign the materialID
	// of the object to that material index.  You will notice that we passed in the
	// model to this function.  This is because we need the number of textures.
	// Yes though, we could have just passed in the model and not the object too.

	// Go through all of the textures
	for(int i = 0; i < pModel.getPMaterials().size(); i++)
	{
		// If the material we just read in matches the current texture name
		if(pModel.getPMaterials(i).getName().equalsIgnoreCase(strMaterial))
		{
			// Set the material ID to the current index 'i' and stop checking
			pObject.setMaterialID(i);

			// Now that we found the material, check if it's a texture map.
			// If the strFile has a string length of 1 and over it's a texture
			if(pModel.getPMaterials(i).getTexFile().length() > 0) {

				// Set the object's flag to say it has a texture map to bind.
				pObject.setbHasTexture(true);
			}	
			break;
		}
		else
		{
			// Set the ID to -1 to show there is no material for this object
			pObject.setMaterialID(-1);
		}
	}

	// Read past the rest of the chunk since we don't care about shared vertices
	// You will notice we subtract the bytes already read in this chunk from the total length.
	skipJunk(pPreviousChunk, in);
	// Read past this chunk and add the bytes read to the byte counter
	pPreviousChunk.addBytesRead(pPreviousChunk.getLength() - pPreviousChunk.getBytesRead());
	//pPreviousChunk->bytesRead += fread(buffer, 1, pPreviousChunk->length - pPreviousChunk->bytesRead, m_FilePointer);
}			

	
	// P R I V A T E   M E T H O D S

    private String readString(InputStream in) throws IOException {
        String result = new String();
        byte inByte;
        while ( (inByte = (byte) in.read()) != 0)
            result += (char) inByte;
        return result;
    }

    private int readInt(InputStream in) throws IOException {
        return in.read() | (in.read() << 8) | (in.read() << 16) | (in.read() << 24);
    }
    /*
    private int readShort(InputStream in) throws IOException {
        return (in.read() | (in.read() << 8));
    }
    */
    private int readData(InputStream in, int bytes) throws IOException {
        int temp = 0;
    	for(int i = 0; i < bytes; i++)
        {
        	temp += in.read()<< i*8;
        }
    	return temp;
    }

    private float readFloat(InputStream in) throws IOException {
        return Float.intBitsToFloat(readInt(in));
    }
    
    private void skipJunk(TChunk tempChunk, InputStream in) throws IOException, OutOfMemoryError {
        for (int i = 0; (i < tempChunk.getLength() - tempChunk.getBytesRead()) && (!endOfStream); i++) {
            endOfStream = in.read() < 0;
        }
    }
    /*
    private void skipJunk1(TChunk tempChunk, InputStream in) throws IOException, OutOfMemoryError {
    	endOfStream = ((int)in.skip(tempChunk.getLength() - tempChunk.getBytesRead()) < 0);
    	
    	
    }
*/
}


/////////////////////////////////////////////////////////////////////////////////
//
//* QUICK NOTES * 
//
//This was a HUGE amount of knowledge and probably the largest tutorial yet!
//In the next tutorial we will show you how to load a text file format called .obj.
//This is the most common 3D file format that almost ANY 3D software will import.
//
//Once again I should point out that the coordinate system of OpenGL and 3DS Max are different.
//Since 3D Studio Max Models with the Z-Axis pointing up (strange and ugly I know! :), 
//we need to flip the y values with the z values in our vertices.  That way it
//will be normal, with Y pointing up.  Also, because we swap the Y and Z we need to negate 
//the Z to make it come out correctly.  This is also explained and done in ReadVertices().
//
//CHUNKS: What is a chunk anyway?
//
//"The chunk ID is a unique code which identifies the type of data in this chunk 
//and also may indicate the existence of subordinate chunks. The chunk length indicates 
//the length of following data to be associated with this chunk. Note, this may 
//contain more data than just this chunk. If the length of data is greater than that 
//needed to fill in the information for the chunk, additional subordinate chunks are 
//attached to this chunk immediately following any data needed for this chunk, and 
//should be parsed out. These subordinate chunks may themselves contain subordinate chunks. 
//Unfortunately, there is no indication of the length of data, which is owned by the current 
//chunk, only the total length of data attached to the chunk, which means that the only way 
//to parse out subordinate chunks is to know the exact format of the owning chunk. On the 
//other hand, if a chunk is unknown, the parsing program can skip the entire chunk and 
//subordinate chunks in one jump. " - Jeff Lewis (werewolf@worldgate.com)
//
//In a short amount of words, a chunk is defined this way:
//2 bytes - Stores the chunk ID (OBJECT, MATERIAL, PRIMARY, etc...)
//4 bytes - Stores the length of that chunk.  That way you know when that
//        chunk is done and there is a new chunk.
//
//So, to start reading the 3DS file, you read the first 2 bytes of it, then
//the length (using fread()).  It should be the PRIMARY chunk, otherwise it isn't
//a .3DS file.  
//
//Below is a list of the order that you will find the chunks and all the know chunks.
//If you go to www.wosit.org you can find a few documents on the 3DS file format.
//You can also take a look at the 3DS Format.rtf that is included with this tutorial.
//
//
//
//   MAIN3DS  (0x4D4D)
//  |
//  +--EDIT3DS  (0x3D3D)
//  |  |
//  |  +--EDIT_MATERIAL (0xAFFF)
//  |  |  |
//  |  |  +--MAT_NAME01 (0xA000) (See mli Doc) 
//  |  |
//  |  +--EDIT_CONFIG1  (0x0100)
//  |  +--EDIT_CONFIG2  (0x3E3D) 
//  |  +--EDIT_VIEW_P1  (0x7012)
//  |  |  |
//  |  |  +--TOP            (0x0001)
//  |  |  +--BOTTOM         (0x0002)
//  |  |  +--LEFT           (0x0003)
//  |  |  +--RIGHT          (0x0004)
//  |  |  +--FRONT          (0x0005) 
//  |  |  +--BACK           (0x0006)
//  |  |  +--USER           (0x0007)
//  |  |  +--CAMERA         (0xFFFF)
//  |  |  +--LIGHT          (0x0009)
//  |  |  +--DISABLED       (0x0010)  
//  |  |  +--BOGUS          (0x0011)
//  |  |
//  |  +--EDIT_VIEW_P2  (0x7011)
//  |  |  |
//  |  |  +--TOP            (0x0001)
//  |  |  +--BOTTOM         (0x0002)
//  |  |  +--LEFT           (0x0003)
//  |  |  +--RIGHT          (0x0004)
//  |  |  +--FRONT          (0x0005) 
//  |  |  +--BACK           (0x0006)
//  |  |  +--USER           (0x0007)
//  |  |  +--CAMERA         (0xFFFF)
//  |  |  +--LIGHT          (0x0009)
//  |  |  +--DISABLED       (0x0010)  
//  |  |  +--BOGUS          (0x0011)
//  |  |
//  |  +--EDIT_VIEW_P3  (0x7020)
//  |  +--EDIT_VIEW1    (0x7001) 
//  |  +--EDIT_BACKGR   (0x1200) 
//  |  +--EDIT_AMBIENT  (0x2100)
//  |  +--EDIT_OBJECT   (0x4000)
//  |  |  |
//  |  |  +--OBJ_TRIMESH   (0x4100)      
//  |  |  |  |
//  |  |  |  +--TRI_VERTEXL          (0x4110) 
//  |  |  |  +--TRI_VERTEXOPTIONS    (0x4111)
//  |  |  |  +--TRI_MAPPINGCOORS     (0x4140) 
//  |  |  |  +--TRI_MAPPINGSTANDARD  (0x4170)
//  |  |  |  +--TRI_FACEL1           (0x4120)
//  |  |  |  |  |
//  |  |  |  |  +--TRI_SMOOTH            (0x4150)   
//  |  |  |  |  +--TRI_MATERIAL          (0x4130)
//  |  |  |  |
//  |  |  |  +--TRI_LOCAL            (0x4160)
//  |  |  |  +--TRI_VISIBLE          (0x4165)
//  |  |  |
//  |  |  +--OBJ_LIGHT    (0x4600)
//  |  |  |  |
//  |  |  |  +--LIT_OFF              (0x4620)
//  |  |  |  +--LIT_SPOT             (0x4610) 
//  |  |  |  +--LIT_UNKNWN01         (0x465A) 
//  |  |  | 
//  |  |  +--OBJ_CAMERA   (0x4700)
//  |  |  |  |
//  |  |  |  +--CAM_UNKNWN01         (0x4710)
//  |  |  |  +--CAM_UNKNWN02         (0x4720)  
//  |  |  |
//  |  |  +--OBJ_UNKNWN01 (0x4710)
//  |  |  +--OBJ_UNKNWN02 (0x4720)
//  |  |
//  |  +--EDIT_UNKNW01  (0x1100)
//  |  +--EDIT_UNKNW02  (0x1201) 
//  |  +--EDIT_UNKNW03  (0x1300)
//  |  +--EDIT_UNKNW04  (0x1400)
//  |  +--EDIT_UNKNW05  (0x1420)
//  |  +--EDIT_UNKNW06  (0x1450)
//  |  +--EDIT_UNKNW07  (0x1500)
//  |  +--EDIT_UNKNW08  (0x2200)
//  |  +--EDIT_UNKNW09  (0x2201)
//  |  +--EDIT_UNKNW10  (0x2210)
//  |  +--EDIT_UNKNW11  (0x2300)
//  |  +--EDIT_UNKNW12  (0x2302)
//  |  +--EDIT_UNKNW13  (0x2000)
//  |  +--EDIT_UNKNW14  (0xAFFF)
//  |
//  +--KEYF3DS (0xB000)
//     |
//     +--KEYF_UNKNWN01 (0xB00A)
//     +--............. (0x7001) ( viewport, same as editor )
//     +--KEYF_FRAMES   (0xB008)
//     +--KEYF_UNKNWN02 (0xB009)
//     +--KEYF_OBJDES   (0xB002)
//        |
//        +--KEYF_OBJHIERARCH  (0xB010)
//        +--KEYF_OBJDUMMYNAME (0xB011)
//        +--KEYF_OBJUNKNWN01  (0xB013)
//        +--KEYF_OBJUNKNWN02  (0xB014)
//        +--KEYF_OBJUNKNWN03  (0xB015)  
//        +--KEYF_OBJPIVOT     (0xB020)  
//        +--KEYF_OBJUNKNWN04  (0xB021)  
//        +--KEYF_OBJUNKNWN05  (0xB022)  
//
//Once you know how to read chunks, all you have to know is the ID you are looking for
//and what data is stored after that ID.  You need to get the file format for that.
//I can give it to you if you want, or you can go to www.wosit.org for several versions.
//Because this is a proprietary format, it isn't a official document.
//
//I know there was a LOT of information blown over, but it is too much knowledge for
//one tutorial.  In the animation tutorial that I eventually will get to, some of
//the things explained here will be explained in more detail.  I do not claim that
//this is the best .3DS tutorial, or even a GOOD one :)  But it is a good start, and there
//isn't much code out there that is simple when it comes to reading .3DS files.
//So far, this is the best I have seen.  That is why I made it :)
//
//I would like to thank www.wosit.org and Terry Caton (tcaton@umr.edu) for his help on this.
//
//Let me know if this helps you out!
//
//
//Ben Humphrey (DigiBen)
//Game Programmer
//DigiBen@GameTutorials.com
//Co-Web Host of www.GameTutorials.com
//
//


	
	
	
	

