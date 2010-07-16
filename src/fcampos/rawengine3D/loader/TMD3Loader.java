package fcampos.rawengine3D.loader;

import java.io.BufferedReader;
import java.io.FileReader;

import fcampos.rawengine3D.MathUtil.*;
import fcampos.rawengine3D.model.*;


public class TMD3Loader {

		
	// Member variables		

	private TMD3Header				m_Header;			// The header data
	private TMD3Skin[]				m_pSkins;			// The skin name data (not used)
	private TMD3TexCoord[]			m_pTexCoords;		// The texture coordinates
	private TMD3Face[]				m_pTriangles;		// Face index information
	private TMD3Triangle[]			m_pVertices;		// Vertex/UV indices
	private TMD3Bone[]				m_pBones;			// This stores the bone data (not used)
	private BinaryLoader loader;
	
	
	
	/** This holds the header information that is read in at the beginning of the file.
	 */
	
	private class TMD3Header
	{ 
		String	fileID;							// This stores the file ID - Must be "IDP3"
		int		version;						// This stores the file version - Must be 15
		String	strFile;						// This stores the name of the file
		int		numFrames;						// This stores the number of animation frames
		int		numTags;						// This stores the tag count
		int		numMeshes;						// This stores the number of sub-objects in the mesh
		int		numMaxSkins;					// This stores the number of skins for the mesh
		int		headerSize;						// This stores the mesh header size
		int		tagStart;						// This stores the offset into the file for tags
		int		tagEnd;							// This stores the end offset into the file for tags
		int		fileSize;						// This stores the file size

		protected TMD3Header()
		{
			fileID		= loader.readString(4);
			version		= loader.readInt();
			strFile		= loader.readString(68);
			numFrames	= loader.readInt();
			numTags		= loader.readInt();
			numMeshes	= loader.readInt();
			numMaxSkins = loader.readInt();
			headerSize	= loader.readInt();
			tagStart	= loader.readInt();
			tagEnd		= loader.readInt();
			fileSize	= loader.readInt();
		}
	
	};

	
	/** This structure is used to read in the mesh data for the .md3 models.
	 */

	private class TMD3MeshInfo
	{
		String	meshID;							// This stores the mesh ID
		String	strName;						// This stores the mesh name
		int		numMeshFrames;					// This stores the mesh aniamtion frame count
		int		numSkins;						// This stores the mesh skin count
		int     numVertices;					// This stores the mesh vertex count
		int		numTriangles;					// This stores the mesh face count
		int		triStart;						// This stores the starting offset for the triangles
		int		headerSize;						// This stores the header size for the mesh
		int     uvStart;						// This stores the starting offset for the UV coordinates
		int		vertexStart;					// This stores the starting offset for the vertex indices
		int		meshSize;						// This stores the total mesh size

		protected TMD3MeshInfo()
		{
			meshID			= loader.readString(4);
			strName			= loader.readString(68);
			numMeshFrames	= loader.readInt();
			numSkins		= loader.readInt();
			numVertices		= loader.readInt();
			numTriangles	= loader.readInt();
			triStart		= loader.readInt();
			headerSize		= loader.readInt();
			uvStart			= loader.readInt();
			vertexStart		= loader.readInt();
			meshSize		= loader.readInt();
		}
	};

	// This is our tag structure for the .MD3 file format.  These are used link other
	// models to and the rotate and transate the child models of that model.
	public class TMD3Tag
	{
		public String	 strName;						// This stores the name of the tag (I.E. "tag_torso")
		public Vector3f vPosition = new Vector3f();	// This stores the translation that should be performed
		public float[]	 rotation = new float[9];		// This stores the 3x3 rotation matrix for this frame

		public TMD3Tag()
		{
			strName = loader.readString(64);
			vPosition.x = loader.readFloat();
			vPosition.y = loader.readFloat();
			vPosition.z = loader.readFloat();

			for (int i=0; i < 9; i++ )
			{
				rotation[i] = loader.readFloat();
			}
		}

	};
	
	/** This stores the bone information (useless as far as I can see...).
	 */
	
	private class TMD3Bone
	{
		float[]	mins = new float[3];			// This is the min (x, y, z) value for the bone
		float[]	maxs = new float[3];			// This is the max (x, y, z) value for the bone
		float[]	position = new float[3];		// This supposedly stores the bone position???
		float	scale;							// This stores the scale of the bone
		String	creator;						// The modeler used to create the model (I.E. "3DS Max")

		protected TMD3Bone()
		{
			mins[0]		= loader.readFloat();
			mins[1]		= loader.readFloat();
			mins[2]		= loader.readFloat();
			maxs[0]		= loader.readFloat();
			maxs[1]		= loader.readFloat();
			maxs[2]		= loader.readFloat();
			position[0] = loader.readFloat();
			position[1] = loader.readFloat();
			position[2] = loader.readFloat();
			scale		= loader.readFloat();
			creator		= loader.readString(16);
		}
	};

	
	// This stores the normals and vertices for the frames
	private class TMD3Triangle
	{
	  short[] vertex = new short[3];
	  int[] normal = new int[2];
	   
	  protected TMD3Triangle()
	   {
			vertex[0] = loader.readShort();
			vertex[1] = loader.readShort();
			vertex[2] = loader.readShort();
			normal[0] = loader.readByte();
			normal[1] = loader.readByte();
	   }
	};
	 
	

	// This stores the indices into the vertex and texture coordinate arrays
	private class TMD3Face
	{
	   int[] vertexIndices = new int[3];
	   
	   
	   protected TMD3Face()
	   {
		   	vertexIndices[0] = loader.readInt();
			vertexIndices[1] = loader.readInt();
			vertexIndices[2] = loader.readInt();
           

	   }
	};

	// This stores UV coordinates
	private class TMD3TexCoord
	{
	   float u, v;
	   
	   protected TMD3TexCoord()
	   {
		   u = loader.readFloat();
		   v = loader.readFloat();
	   }
	};
	
	/** This stores a skin name (We don't use this, just the name of the model to get the texture).
	 */
	
	private class TMD3Skin 
	{
		String strName;							// Skin name

		protected TMD3Skin()
		{
			strName = loader.readString(68);
		}
	};

	
	

	

	
	
///////////////////////////////// IMPORT MD2 \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
/////
/////	This is called by the client to open the .Md2 file, read it, then clean up
/////
///////////////////////////////// IMPORT MD2 \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*

public boolean importMD3(T3dModel pModel, String strFileName)
{
	
try
{
	
	loader = new BinaryLoader(strFileName);
	 
	  // Read the header data and store it in our m_Header member variable
	m_Header = new TMD3Header();

	
	 // Get the 4 character ID
	String ID = m_Header.fileID;

	  // Make sure the ID == IDP3 and the version is this crazy number '15' or else it's a bad egg
	if(!ID.equals("IDP3") || m_Header.version != 15)
	{
		System.out.println("[Error]: file " + strFileName + " version is not valid.");
		System.exit(0);
	}
	
	 
	// Now that we made sure the header had correct data, we want to read in the
	// rest of the data.  Once the data is read in, we need to convert it to our structures.
	// Since we are only reading in the first frame of animation, there will only
	// be ONE object in our t3DObject structure, held within our pModel variable.
	readMD3Data(pModel);
	

	// After we have read the whole MD2 file, we want to calculate our own vertex normals.
	//VectorMath.computeNormals(pModel);
	
	

}
catch(Exception e)
{
	System.out.println("[Error]: can't read " + strFileName + " correctly.");
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

private void readMD3Data(T3dModel pModel) throws Exception
{


int i;
	
	// Here we allocate memory for the bone information and read the bones in.
	m_pBones = new TMD3Bone[m_Header.numFrames];

	for (i = 0; i < m_Header.numFrames ; i++)
		m_pBones[i] = new TMD3Bone();
	
	 // Free the unused bones
	m_pBones = null;
	
	
	// Next, after the bones are read in, we need to read in the tags.
	pModel.setNumPTags(m_Header.numFrames * m_Header.numTags);
//System.out.println(pModel.getPTags().length);
	for(i = 0; i < pModel.getPTags().length; i++)
	{
		TMD3Tag pTags = new TMD3Tag();
		pModel.setPTags(pTags, i);
	}
	  // Assign the number of tags to our model
	pModel.setNumOfTags(m_Header.numTags);
	
	  // Now we want to initialize our links.
	pModel.setNumLinks(m_Header.numTags);
	
   // Get the current offset into the file
	//loader.markPos();
	int meshOffset = loader.getFileIndex();
	
	  // Create a local meshHeader that stores the info about the mesh
	TMD3MeshInfo meshHeader;// = new TMD3MeshInfo();
	
	  // Go through all of the sub-objects in this mesh
	for (int j = 0; j < m_Header.numMeshes; j++)
	{
		  // Seek to the start of this mesh and read in it's header
		loader.setOffset(meshOffset);
		
		meshHeader = new TMD3MeshInfo();

		  // Here we allocate all of our memory from the header's information
		m_pSkins     = new TMD3Skin[meshHeader.numSkins];
		m_pTexCoords = new TMD3TexCoord[meshHeader.numVertices];
		m_pTriangles = new TMD3Face[meshHeader.numTriangles];
		m_pVertices  = new TMD3Triangle[meshHeader.numVertices * meshHeader.numMeshFrames];
		
		  // Read in the skin information
		loader.setOffset(meshOffset + meshHeader.numSkins);
		for (i = 0; i < meshHeader.numSkins ; i++)
			m_pSkins[i] = new TMD3Skin();
		
		  // Seek to the start of the triangle/face data, then read it in
		loader.setOffset(meshOffset + meshHeader.triStart);
		//m_FilePointer = meshOffset + meshHeader.triStart;

		for (i = 0; i < meshHeader.numTriangles; i++)
			m_pTriangles[i] = new TMD3Face();

		  // Seek to the start of the UV coordinate data, then read it in
		loader.setOffset(meshOffset + meshHeader.uvStart);
		//m_FilePointer = meshOffset + meshHeader.uvStart;

		for (i = 0; i < meshHeader.numVertices; i++)
			m_pTexCoords[i] = new TMD3TexCoord();

		  // Seek to the start of the vertex/face index information, then read it in.
		loader.setOffset(meshOffset + meshHeader.vertexStart);
		//m_FilePointer = meshOffset + meshHeader.vertexStart;
		for(i = 0; i < meshHeader.numMeshFrames * meshHeader.numVertices; i++)
			m_pVertices[i] = new TMD3Triangle();

		  // Now that we have the data loaded into the md3 structures, let's convert them to
		  // our data types like Model3D and Object3D.
		convertDataStructures(pModel, meshHeader);

		  // Free all the memory for this mesh since we just converted it to our structures
		m_pSkins = null;    
		m_pTexCoords = null;
		m_pTriangles = null;
		m_pVertices = null;   

		  // Increase the offset into the file
		meshOffset += meshHeader.meshSize;
            
        }
    }


/** This loads the texture information for the model from the *.skin file.
 *  @param pModel Current model.
 *  @param strSkin Skin path.
 */

public boolean loadSkin(T3dModel pModel, String strSkin)
{
	
	// Make sure valid data was passed in
	if(pModel == null || strSkin == null) return false;

	// This function is used to load a .skin file for the .md3 model associated
	// with it.  The .skin file stores the textures that need to go with each
	// object and subject in the .md3 files.  For instance, in our Lara Croft model,
	// her upper body model links to 2 texture; one for her body and the other for
	// her face/head.  The .skin file for the lara_upper.md3 model has 2 textures:
	//
	// u_torso,models/players/laracroft/default.bmp
	// u_head,models/players/laracroft/default_h.bmp
	//
	// Notice the first word, then a comma.  This word is the name of the object
	// in the .md3 file.  Remember, each .md3 file can have many sub-objects.
	// The next bit of text is the Quake3 path into the .pk3 file where the 
	// texture for that model is stored  Since we don't use the Quake3 path
	// because we aren't making Quake, I just grab the texture name at the
	// end of the string and disregard the rest.  of course, later this is
	// concatenated to the original MODEL_PATH that we passed into load our character.
	// So, for the torso object it's clear that default.bmp is assigned to it, where
	// as the head model with the pony tail, is assigned to default_h.bmp.  Simple enough.
	// What this function does is go through all the lines of the .skin file, and then
	// goes through all of the sub-objects in the .md3 file to see if their name is
	// in that line as a sub string.  We use our cool IsInString() function for that.
	// If it IS in that line, then we know that we need to grab it's texture file at
	// the end of the line.  I just parse backwards until I find the last '/' character,
	// then copy all the characters from that index + 1 on (I.E. "default.bmp").
	// Remember, it's important to note that I changed the texture files from .tga
	// files to .bmp files because that is what all of our tutorials use.  That way
	// you don't have to sift through tons of image loading code.  You can write or
	// get your own if you really want to use the .tga format.
	try
	{
		  // Wrap a buffer to make reading more efficient (faster)
		BufferedReader reader = new BufferedReader(new FileReader(strSkin));

		  // These 2 variables are for reading in each line from the file, then storing
		  // the index of where the bitmap name starts after the last '/' character.
		String strLine;
		int textureNameStart = 0;

		  // Go through every line in the .skin file
		while((strLine = reader.readLine()) != null)
		{
			  // Loop through all of our objects to test if their name is in this line
			for(int i = 0; i < pModel.getPObject().size(); i++)
			{
				  // Check if the name of this object appears in this line from the skin file
				if (strLine.contains(pModel.getPObject(i).getName()))
						
				{			
					  // To extract the texture name, we loop through the string, starting
					  // at the end of it until we find a '/' character, then save that index + 1.
					textureNameStart = strLine.lastIndexOf("/") + 1;

					  // Create a local material info structure
					TMaterialInfo texture = new TMaterialInfo();

					  // Copy the name of the file into our texture file name variable.
					texture.setName(strLine.substring(textureNameStart));
					
										
					// The tile or scale for the UV's is 1 to 1 (but Quake saves off a 0-256 ratio)
					texture.setUTile(1);
					texture.setVTile(1);
					
					// Add the local material info to our model's material list
					 // Store the material ID for this object and set the texture boolean to true
					pModel.getPObject(i).setMaterialID(pModel.getPMaterials().size());
					pModel.getPObject(i).setbHasTexture(true);

					
					  // Add the local material info structure to our model's material list
					pModel.addPMaterials(texture);
				}
			}
		}

		  // Close the file and return a success
		reader.close();
	}
	catch(Exception e)
	{
		return false;
	}

	return true;
}


///////////////////////////////// CONVERT DATA STRUCTURES \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
/////
/////	This function converts the .md2 structures to our own model and object structures
/////
///////////////////////////////// CONVERT DATA STRUCTURES \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*

private void convertDataStructures(T3dModel pModel, TMD3MeshInfo meshHeader)
{
int i = 0;


T3dObject currentFrame = new T3dObject();


// Assign the vertex, texture coord and face count to our new structure
currentFrame.setNumVert(meshHeader.numVertices * meshHeader.numMeshFrames);
currentFrame.setNumTex(meshHeader.numVertices);
currentFrame.setNumFaces(meshHeader.numTriangles);
currentFrame.setName(meshHeader.strName);

// Go through all of the vertices and assign them over to our structure
for (i=0; i < currentFrame.getNumVert(); i++)
{
	Vector3f temp = new Vector3f(m_pVertices[i].vertex[0]/64.0f, m_pVertices[i].vertex[1]/64.0f,
								 m_pVertices[i].vertex[2]/64.0f);
	
	currentFrame.setVertices(temp, i);
	
}


for (i=0; i < currentFrame.getNumTexcoords(); i++)
{
	Vector3f temp = new Vector3f(m_pTexCoords[i].u , -m_pTexCoords[i].v, 0);
	currentFrame.setTexcoords(temp, i);
	
}

// Go through all of the face data and assign it over to OUR structure
for(i=0; i < currentFrame.getNumFaces(); i++)
{
	
	currentFrame.getFace(i).setVertices(0, m_pTriangles[i].vertexIndices[0]);
	currentFrame.getFace(i).setVertices(1, m_pTriangles[i].vertexIndices[1]);
	currentFrame.getFace(i).setVertices(2, m_pTriangles[i].vertexIndices[2]);
	// Assign the vertex indices to our face data
	//currentFrame.pFaces[j].vertIndex[0] = m_pTriangles[j].vertexIndices[0];
	//currentFrame.pFaces[j].vertIndex[1] = m_pTriangles[j].vertexIndices[1];
	//currentFrame.pFaces[j].vertIndex[2] = m_pTriangles[j].vertexIndices[2];
	
	
	
	currentFrame.getFace(i).setTexCoords(0, m_pTriangles[i].vertexIndices[0]);
	currentFrame.getFace(i).setTexCoords(1, m_pTriangles[i].vertexIndices[1]);
	currentFrame.getFace(i).setTexCoords(2, m_pTriangles[i].vertexIndices[2]);
	// Assign the texture coord indices to our face data
	//currentFrame.pFaces[j].coordIndex[0] = m_pTriangles[j].textureIndices[0];
	//currentFrame.pFaces[j].coordIndex[1] = m_pTriangles[j].textureIndices[1];
	//currentFrame.pFaces[j].coordIndex[2] = m_pTriangles[j].textureIndices[2];
}
currentFrame.setDimension();
// Here we add the current object (or frame) to our list object list
pModel.addPObject(currentFrame);
}

public boolean loadShader(T3dModel pModel, String strShader)
{
	try
	{
		  // Wrap a buffer to make reading more efficient (faster)
		BufferedReader reader = new BufferedReader(new FileReader(strShader));

		  // These variables are used to read in a line at a time from the file, and also
		  // to store the current line being read so that we can use that as an index for the 
		  // textures, in relation to the index of the sub-object loaded in from the weapon model.
		String strLine;
		int currentIndex = 0;
		
		  // Go through and read in every line of text from the file
		while((strLine = reader.readLine()) != null)
		{
			  // Create a local material info structure
			TMaterialInfo texture = new TMaterialInfo();

			  // Copy the name of the file into our texture file name variable
			texture.setName(strLine);
					
			// The tile or scale for the UV's is 1 to 1 (but Quake saves off a 0-256 ratio)
			texture.setUTile(1);
			texture.setVTile(1);

			// Add the local material info to our model's material list
			 // Store the material ID for this object and set the texture boolean to true
			pModel.getPObject(currentIndex).setMaterialID(pModel.getPMaterials().size());
			pModel.getPObject(currentIndex).setbHasTexture(true);

			
			  // Add the local material info structure to our model's material list
			pModel.addPMaterials(texture);

			
			  // Here we increase the material index for the next texture (if any)
			currentIndex++;
		}

		  // Close the file and return a success
		reader.close();
	}
	catch(Exception e)
	{
		return false;
	}

	return true;
}

}
