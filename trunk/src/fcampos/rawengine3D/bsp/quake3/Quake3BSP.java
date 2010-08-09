package fcampos.rawengine3D.bsp.quake3;

import static org.lwjgl.opengl.GL11.*;

import java.io.File;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import fcampos.rawengine3D.MathUtil.Vector3f;
import fcampos.rawengine3D.graficos.TextureCoord;
import fcampos.rawengine3D.loader.BinaryLoader;
import fcampos.rawengine3D.resource.Conversion;
import fcampos.rawengine3D.resource.TextureManager;
import fcampos.rawengine3D.teste.TesteBSP;

public class Quake3BSP {
	
	public final static int FACE_POLYGON = 1;

	private int  numOfVerts;			// The number of verts in the model
	private int  numOfFaces;			// The number of faces in the model
	private int  numOfIndices;		// The number of indices for the model
	private int  numOfTextures;		// The number of texture maps

	private int[] pIndices;	// The object's indices for rendering
	private BSPVertex[]  pVerts;		// The object's vertices
	private BSPFace[]	 pFaces;		// The faces information of the object
								// The texture and lightmap array for the level
	private int[] textures;	
								
	private BitSet facesDrawn;		// The bitset for the faces that have/haven't been drawn
	
	private BinaryLoader loader;
	private TextureManager texManager = new TextureManager();
	
	
	public Quake3BSP()
	{
		// Here we simply initialize our member variables to 0
		numOfVerts    = 0;	
		numOfFaces    = 0;
		numOfIndices  = 0;
		numOfTextures = 0;

		pVerts = null;
		pFaces = null;
		pIndices = null;
		
	}
	

	// This is our integer vector structure
	private class Vector3i
	{
		private int x, y, z;				// The x y and z position of our integer vector
		
		
	}


	// This is our BSP header structure
	public class BSPHeader
	{
	    String strID;				// This should always be 'IBSP'
	    int version;				// This should be 0x2e for Quake 3 files
	    
	    public BSPHeader()
	    {
	    	strID = loader.readString(4);
	    	version = loader.readInt();
	    }
	} 


	// This is our BSP lump structure
	public class BSPLump
	{
		int offset;					// The offset into the file for the start of this lump
		int length;					// The length in bytes for this lump
		
		public BSPLump()
		{
			offset = loader.readInt();
			length = loader.readInt();
		}

	}


	// This is our BSP vertex structure
	public class BSPVertex
	{
	    Vector3f vPosition;				// (x, y, z) position. 
	    TextureCoord vTextureCoord;		// (u, v) texture coordinate
	    TextureCoord vLightmapCoord;	// (u, v) lightmap coordinate
	    Vector3f vNormal;				// (x, y, z) normal vector
	    byte[] color = new byte[4];		// RGBA color for the vertex 
	    
	    public BSPVertex()
	    {
	    	vPosition = new Vector3f(loader.readFloat(), loader.readFloat(), loader.readFloat());
	    	vTextureCoord = new TextureCoord(loader.readFloat(), loader.readFloat());
	    	vLightmapCoord = new TextureCoord(loader.readFloat(), loader.readFloat());
	    	vNormal = new Vector3f(loader.readFloat(), loader.readFloat(), loader.readFloat());
	    	
	    	for(int i=0; i<color.length; i++)
	    	{
	    		color[i] = (byte) loader.readByte();
	    	}
	    }
	}


	// This is our BSP face structure
	public class BSPFace
	{
	    int textureID;				// The index into the texture array 
	    int effect;					// The index for the effects (or -1 = n/a) 
	    int type;					// 1=polygon, 2=patch, 3=mesh, 4=billboard 
	    int startVertIndex;			// The starting index into this face's first vertex 
	    int numOfVerts;				// The number of vertices for this face 
	    int startIndex;				// The starting index into the indices array for this face
	    int numOfIndices;			// The number of indices for this face
	    int lightmapID;				// The texture index for the lightmap 
	    int[] lMapCorner = new int[2];			// The face's lightmap corner in the image 
	    int[] lMapSize = new int[2];			// The size of the lightmap section 
	    Vector3f lMapPos;			// The 3D origin of lightmap. 
	    Vector3f[] lMapVecs = new Vector3f[2];		// The 3D space for s and t unit vectors. 
	    Vector3f vNormal;			// The face normal. 
	    int[] size = new int[2];				// The bezier patch dimensions. 
	    
	    public BSPFace()
	    {
	    	textureID = loader.readInt();
		    effect = loader.readInt();
		    type = loader.readInt();
		    startVertIndex = loader.readInt();
		    numOfVerts = loader.readInt();
		    startIndex = loader.readInt();
		    numOfIndices = loader.readInt();
		    lightmapID = loader.readInt();
		    lMapCorner[0] = loader.readInt();
		    lMapCorner[1] = loader.readInt();
		    lMapSize[0] = loader.readInt();
		    lMapSize[1] = loader.readInt();
		    lMapPos = new Vector3f(loader.readFloat(), loader.readFloat(), loader.readFloat());
		    lMapVecs[0] = new Vector3f(loader.readFloat(), loader.readFloat(), loader.readFloat());
		    lMapVecs[1] = new Vector3f(loader.readFloat(), loader.readFloat(), loader.readFloat());
		    vNormal = new Vector3f(loader.readFloat(), loader.readFloat(), loader.readFloat());
		    size[0] = loader.readInt();
		    size[1] = loader.readInt();
	    	
	    }
	}


	// This is our BSP texture structure
	public class BSPTexture
	{
	    String strName;				// The name of the texture w/o the extension 
	    int flags;					// The surface flags (unknown) 
	    int contents;				// The content flags (unknown)
	    
	    public BSPTexture()
	    {
	    	strName = loader.readString(64);
	    	flags = loader.readInt();
	    	contents = loader.readInt();
	    }
	}
	
	
	////////////////////////////FIND TEXTURE EXTENSION \\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This attaches the image extension to the texture name, if found
	/////
	//////////////////////////// FIND TEXTURE EXTENSION \\\\\\\\\\\\\\\\\\\\\\\\\\\*
	
	private String findTextureExtension(String strFileName)
	{
		
		File fileJpg = new File(strFileName + ".jpg");
		File fileTga = new File(strFileName + ".tga");
		// This function is a very basic way to find the extension
		// of the texture that is being passed in.  Quake saves it's
		// textures with just the name, and omits the extension.  I
		// still haven't figured out why they do this, but I imagine
		// it has to do with allowing you to change images to different 
		// image formats without having to use the level editor again.
		// What we do hear is just assume that it's either going to be
		// a jpeg or targa file.  I haven't seen any other type
		// be used.  If you just add on either one of those extensions
		// to the current name and see if a file with that name exits,
		// then it must be the texture extension.  If fopen() returns
		// a NULL, there is no file with that name.  Keep in mind that
		// most levels use the textures that come with Quake3.  That means
		// you won't be able to load them unless you try and read from their
		// pk3 files if the texture isn't found in the level's .pk3 file.
		// Also, I have found that some shader textures store the file name
		// in the shader.  So, don't be surprised if not all the textures are loaded.
	
		// Get the current path we are in
		//GetCurrentDirectory(MAX_PATH, strJPGPath);
	
		// Add on a '\' and the file name to the end of the current path.
		// We create 2 seperate strings to test each image extension.
		//strcat(strJPGPath, "\\");
		//strcat(strJPGPath, strFileName);
		//strcpy(strTGAPath, strJPGPath);
		
		// Add the extensions on to the file name and path
		//strcat(strJPGPath, ".jpg");
		//strcat(strTGAPath, ".tga");
		
		if(fileJpg.exists())
		{
			return fileJpg.getAbsolutePath();
		}else if(fileTga.exists())
		{
			return fileTga.getAbsolutePath();
	
		}
		/*
		// Check if there is a jpeg file with the texture name
		if((fp = fopen(strJPGPath, "rb")) != NULL)
		{
			// If so, then let's add ".jpg" onto the file name and return
			strcat(strFileName, ".jpg");
			return;
		}
	
		// Check if there is a targa file with the texture name
		if((fp = fopen(strTGAPath, "rb")) != NULL)
		{
			// If so, then let's add a ".tga" onto the file name and return
			strcat(strFileName, ".tga");
			return;
		}
	*/
		// Otherwise, it must be a special texture or given in the shader file,
		// or possibly a base Quake texture used in the game.  There are some
		// special names like "textures\caulk" and such that mean special things.
		// They aren't actual textures.  This took me a lot of pulling hair to find this out.
		return null;
	}


	//////////////////////////// LOAD BSP \\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This loads in all of the .bsp data for the level
	/////
	//////////////////////////// LOAD BSP \\\\\\\\\\\\\\\\\\\\\\\\\\\*
	
	public boolean loadBSP(String strFileName) throws IOException
	{
		File file = new File(strFileName);
		
		if(file.exists())
		{
			System.out.println(file.getAbsolutePath());
			loader = new BinaryLoader(file);
		}
		int i = 0;
	
		// This is the bread and butter of our tutorial.  All the level
		// information is loaded from here.  The HTML file that comes with
		// this tutorial should explain about the .bsp file format, but let
		// me give a quick recap.
		//
		// The .bsp file format stores the data in lumps.  Look at the
		// tBSPLump enum to see all the lumps in their order.  A lump
		// is just like a chunk like in binary formats such as .3ds.
		// It stores an offset into the file for that lump, as well as
		// the lump's size in bytes.  We first want to read the BSP header,
		// then read in all the lump info.  We can then seek to the correct
		// offset for each lump and read in it's data.  To find out how many
		// structures instances the lump has, you just divide the lump's
		// length by the sizeof(<theStructure>).  Check out the HTML file for
		// more detail on this.  The only one that is handled a bit differently
		// is the visibility lump.  There is only one of these for the .bsp file.
		
		// Check if the .bsp file could be opened
		if(!file.exists())
		{
			// Display an error message and quit if the file can't be found.
			System.out.println("Could not find BSP file!");
			return false;
		}
	
		// Initialize the header and lump structures
		BSPHeader header = null;
		BSPLump[] lumps = new BSPLump[Lumps.kMaxLumps.ordinal()];
	
		// Read in the header and lump data
		//fread(&header, 1, sizeof(tBSPHeader), fp);
		header = new BSPHeader();
		
		//fread(&lumps, kMaxLumps, sizeof(tBSPLump), fp);
		for(int j=0; j<lumps.length; j++)
		{
			lumps[j] = new BSPLump();
		}
		
		// Now we know all the information about our file.  We can
		// then allocate the needed memory for our member variables.
	
		// Allocate the vertex memory
		numOfVerts = lumps[Lumps.kVertices.ordinal()].length / 44;
		pVerts     = new BSPVertex [numOfVerts];
	
		// Allocate the face memory
		numOfFaces = lumps[Lumps.kFaces.ordinal()].length / 104;
		pFaces     = new BSPFace [numOfFaces];
	
		// Allocate the index memory
		numOfIndices = lumps[Lumps.kIndices.ordinal()].length / 4;
		pIndices     = new int[numOfIndices];
	
		// Allocate memory to read in the texture information.
		// We create a local pointer of tBSPTextures because we don't need
		// that information once we create texture maps from it.
		numOfTextures = lumps[Lumps.kTextures.ordinal()].length / 72;
		BSPTexture[] pTextures = new BSPTexture [numOfTextures];
	
		// Seek to the position in the file that stores the vertex information
		//fseek(fp, lumps[kVertices].offset, SEEK_SET);
		loader.seekMarkOffset(lumps[Lumps.kVertices.ordinal()].offset);
		
		// Since Quake has the Z-axis pointing up, we want to convert the data so
		// that Y-axis is pointing up (like normal!) :)
	
		// Go through all of the vertices that need to be read
		for(i = 0; i < numOfVerts; i++)
		{
			// Read in the current vertex
			//fread(&m_pVerts[i], 1, sizeof(tBSPVertex), fp);
			pVerts[i] = new BSPVertex();
			// Swap the y and z values, and negate the new z so Y is up.
			float temp = pVerts[i].vPosition.y;
			pVerts[i].vPosition.y = pVerts[i].vPosition.z;
			pVerts[i].vPosition.z = -temp;
		}	
	
		// Seek to the position in the file that stores the index information
		//fseek(fp, lumps[kIndices].offset, SEEK_SET);
		loader.seekMarkOffset(lumps[Lumps.kIndices.ordinal()].offset);
		
		// Read in all the index information
		//fread(m_pIndices, m_numOfIndices, sizeof(int), fp);
		for(int k=0; k<pIndices.length; k++)
		{
			pIndices[k] = loader.readInt();
		}
		
	
		// Seek to the position in the file that stores the face information
		//fseek(fp, lumps[kFaces].offset, SEEK_SET);
		loader.seekMarkOffset(lumps[Lumps.kFaces.ordinal()].offset);
	
		// Read in all the face information
		//fread(m_pFaces, m_numOfFaces, sizeof(tBSPFace), fp);
		for(int p=0; p<numOfFaces; p++)
		{
			pFaces[p] = new BSPFace();
		}
	
		// Seek to the position in the file that stores the texture information
		//fseek(fp, lumps[kTextures].offset, SEEK_SET);
		loader.seekMarkOffset(lumps[Lumps.kTextures.ordinal()].offset);
		
		// Read in all the texture information
		//fread(pTextures, m_numOfTextures, sizeof(tBSPTexture), fp);
		for(int y=0; y<numOfTextures; y++)
		{
			pTextures[y] = new BSPTexture();
		}
		
	
		// Now that we have the texture information, we need to load the
		// textures.  Since the texture names don't have an extension, we need
		// to find it first.
	
		// Go through all of the textures
		for(i = 0; i < numOfTextures; i++)
		{
			// Find the extension if any and append it to the file name
			pTextures[i].strName = findTextureExtension(pTextures[i].strName);
			
			// Create a texture from the image
			//CreateTexture(m_textures[i], pTextures[i].strName);
			
			// If there is a valid texture name passed in, we want to set the texture data
			if(pTextures[i].strName != null)
			{
								
				
				//Texture tex = texManager.getNormalImage("texturas/" + strTexture,true, true);
				texManager.getFlippedImage(pTextures[i].strName,false, false);
				//Texture tex = texManager.getMirrorImage("texturas/" + strTexture,true, true);
			}
				
		}
		
		// We can now free all the texture information since we already loaded them
		//delete [] pTextures;
	
		// Close the file
		//fclose(fp);
			
	
		// I decided to put in a really big optimization for rendering.
		// I create a bitset that holds a bit slot for every face in the level.
		// Once the face is drawn, the slot saved for that face is set to 1.
		// If we try and draw it again, it checks first to see if it has already
		// been drawn.  We need to do this because many leafs stores faces that
		// are the same in other leafs.  If we don't check if it's already been drawn,
		// you can sometimes draw double the faces that you need to.  In this first
		// tutorial we draw every face once, so it doesn't matter, but when we get
		// into leafs we will need this.  I just choose to add it in the beginning
		// so I don't cram a ton of code down your throat when we get to the 
		// BSP nodes/leafs.
	
		// Here we allocate enough bits to store all the faces for our bitset
		//m_FacesDrawn.Resize(m_numOfFaces);
			facesDrawn = new BitSet();
			facesDrawn.resize(numOfFaces);
	
		// Return a success
		return true;
	}
	
	////////////////////////////RENDER FACE \\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This renders a face, determined by the passed in index
	/////
	//////////////////////////// RENDER FACE \\\\\\\\\\\\\\\\\\\\\\\\\\\*
	
	private void renderFace(int faceIndex)
	{
		// Here we grab the face from the index passed in
		BSPFace pFace = pFaces[faceIndex];
		
		
		// To get a huge speed boost to our rendering, we are going to use vertex arrays.
		// This makes it so we don't have to call glVertex*() or glTex*() functions for
		// every vertice.  It also cuts down on slow loops which kill our frame rate.
	
		// First, we need to give OpenGL a pointer to our first vertice.
		// We also tell OpenGL that there is 3 floats, and the offset between
		// each vertices is the size of tBSPVertex in bytes.  That way we don't have
		// to have all the vertices in one contiguous array of floats, back to back.
		// We need to them give an address to the start of this face's vertices, startVertIndex.
		
		glEnableClientState(GL_VERTEX_ARRAY);
		// Point OpenGL to our vertex array.  We have our vertices stored in
		// 3 floats, with 0 stride between them in bytes.
		float[] temp = new float[pFace.numOfVerts*3];
		int b = 0;
		for(int a=pFace.startVertIndex; a < pFace.startVertIndex+pFace.numOfVerts; a++)
		{
			temp[b] = pVerts[a].vPosition.x;
			temp[b+1] = pVerts[a].vPosition.y;
			temp[b+2] = pVerts[a].vPosition.z;
			b +=3;
			
		}
		
		FloatBuffer temp1 = Conversion.allocFloats(temp);
		glVertexPointer(3, 0, temp1);
		//glVertexPointer(3, GL_FLOAT, sizeof(tBSPVertex), &(m_pVerts[pFace->startVertIndex].vPosition));
	
		// Next, we pass in the address of the first texture coordinate.  We also tell 
		// OpenGL that there are 2 UV coordinates that are floats, and the offset between 
		// each texture coordinate is the size of tBSPVertex in bytes.  
		// We need to them give an address to the start of this face's indices, startVertIndex.
		
		glEnableClientState(GL_TEXTURE_COORD_ARRAY);
		
		float[] tempTex = new float[pFace.numOfVerts * 2];
		int c = 0;
		for(int a=pFace.startVertIndex; a < pFace.startVertIndex+pFace.numOfVerts; a++)
		{
			tempTex[c] = pVerts[a].vTextureCoord.s;
			tempTex[c+1] = pVerts[a].vTextureCoord.t;
			c +=2;
			
		}
		
		FloatBuffer temp2 = Conversion.allocFloats(tempTex);
		
		glTexCoordPointer(2, 0, temp2);
		
		//glTexCoordPointer(2, GL_FLOAT, sizeof(tBSPVertex), &(m_pVerts[pFace->startVertIndex].vTextureCoord));
	
		// Finally, we want to turn on the vertex and texture coordinate options for
		// our vertex arrays.  That tells OpenGL to pay attention to our vertices
		// and texture coordinates when we call the appropriate vertex arrays functions.
		
		
	
		// If we want to render the textures
		if(TesteBSP.g_RenderMode)
		{			
			// Turn on texture mapping and bind the face's texture map
			glEnable(GL_TEXTURE_2D);
			//glBindTexture(GL_TEXTURE_2D,  texManager.getTexture(pFace.textureID).getTexID());
			texManager.getTexture(pFace.textureID).bind();
		}
	
		// Now, to draw the face with vertex arrays we just need to tell OpenGL
		// which indices we want to draw and what primitive the format is in.
		// The faces are stored in triangles.  We give glDrawElements() a pointer
		// to our indices, but it's not a normal indice array.  The indices are stored
		// according to the pFace->startVertIndex into the vertices array.  If you were
		// to print all of our indices out, they wouldn't go above the number 5.  If there
		// is over 70 vertices though, how is that possible for the indices to work?  Well,
		// that is why we give our vertex array functions above a pointer to the startVertIndex,
		// then the indice array acts according to that.  This is very important to do it this
		// way, otherwise we will not get more than 5 vertices display, and for all our faces.
	
		// We are going to draw triangles, pass in the number of indices for this face, then
		// say the indices are stored as ints, then pass in the starting address in our indice
		// array for this face by indexing it by the startIndex variable of our current face.
		
		int[] tempIndices = new int[pIndices.length]; 
		for(int a=pFace.startIndex; a < tempIndices.length; a++)
		{
			tempIndices[a] = pIndices[a];
		}
		
		IntBuffer temp3 = Conversion.allocInts(tempIndices);
		glDrawElements(GL_TRIANGLES, temp3);
		//glDrawElements(GL_TRIANGLES, pFace->numOfIndices, GL_UNSIGNED_INT, &(m_pIndices[pFace->startIndex]) );
		 
	}


	//////////////////////////// RENDER LEVEL \\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	Goes through all of the faces and draws them if the type is FACE_POLYGON
	/////
	//////////////////////////// RENDER LEVEL \\\\\\\\\\\\\\\\\\\\\\\\\\\*
	
	public void renderLevel(Vector3f vPos)
	{
		// In our current state, rendering the level is simple.
		// We just need to go through all of the faces and draw them.
	
		// Get the number of faces in our level
		int i = numOfFaces;
	
		// Reset our bitset so all the slots are zero.  This isn't really
		// utilized in this tutorial, but I thought I might as well add it
		// not so that we spread out the code between the tutorials.
		facesDrawn.clearAll();
	
		// Go through all the faces
		while(i-- > 0)
		{
			// Before drawing this face, make sure it's a normal polygon
			if(pFaces[i].type != FACE_POLYGON)
			{
				continue;
			}
	
			// If this face already hasn't been drawn
			if(facesDrawn.on(i) != 1) 
			{
				// Set this face as drawn and render it
				facesDrawn.set(i);
				renderFace(i);
			}
		}
	}



	/**
	 * @param numOfVerts the numOfVerts to set
	 */
	public void setNumOfVerts(int numOfVerts) 
	{
		this.numOfVerts = numOfVerts;
	}


	/**
	 * @return the numOfVerts
	 */
	public int getNumOfVerts() 
	{
		return numOfVerts;
	}


	/**
	 * @param numOfFaces the numOfFaces to set
	 */
	public void setNumOfFaces(int numOfFaces) 
	{
		this.numOfFaces = numOfFaces;
	}


	/**
	 * @return the numOfFaces
	 */
	public int getNumOfFaces() 
	{
		return numOfFaces;
	}


	/**
	 * @param numOfIndices the numOfIndices to set
	 */
	public void setNumOfIndices(int numOfIndices) 
	{
		this.numOfIndices = numOfIndices;
	}


	/**
	 * @return the numOfIndices
	 */
	public int getNumOfIndices() 
	{
		return numOfIndices;
	}


	/**
	 * @param numOfTextures the numOfTextures to set
	 */
	public void setNumOfTextures(int numOfTextures) 
	{
		this.numOfTextures = numOfTextures;
	}


	/**
	 * @return the numOfTextures
	 */
	public int getNumOfTextures() 
	{
		return numOfTextures;
	}


	/**
	 * @param pIndices the pIndices to set
	 */
	public void setpIndices(int[] pIndices) 
	{
		this.pIndices = pIndices;
	}


	/**
	 * @return the pIndices
	 */
	public int[] getpIndices()
	{
		return pIndices;
	}


	/**
	 * @param pVerts the pVerts to set
	 */
	public void setpVerts(BSPVertex[] pVerts) 
	{
		this.pVerts = pVerts;
	}


	/**
	 * @return the pVerts
	 */
	public BSPVertex[] getpVerts() 
	{
		return pVerts;
	}


	/**
	 * @param pFaces the pFaces to set
	 */
	public void setpFaces(BSPFace[] pFaces) 
	{
		this.pFaces = pFaces;
	}


	/**
	 * @return the pFaces
	 */
	public BSPFace[] getpFaces() 
	{
		return pFaces;
	}


	/**
	 * @param textures the textures to set
	 */
	public void setTextures(int[] textures) 
	{
		this.textures = textures;
	}


	/**
	 * @return the textures
	 */
	public int[] getTextures() 
	{
		return textures;
	}


	/**
	 * @param facesDrawn the facesDrawn to set
	 */
	public void setFacesDrawn(BitSet facesDrawn) 
	{
		this.facesDrawn = facesDrawn;
	}


	/**
	 * @return the facesDrawn
	 */
	public BitSet getFacesDrawn() 
	{
		return facesDrawn;
	}
}
