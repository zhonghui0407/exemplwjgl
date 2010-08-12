package fcampos.rawengine3D.bsp.quake3;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.ARBMultitexture.*;


import static org.lwjgl.util.glu.GLU.*;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;


import fcampos.rawengine3D.MathUtil.Vector3f;
import fcampos.rawengine3D.gamecore.GameCore;
import fcampos.rawengine3D.graficos.Texture;
import fcampos.rawengine3D.graficos.TextureCoord;
import fcampos.rawengine3D.loader.BinaryLoader;
import fcampos.rawengine3D.resource.Conversion;
import fcampos.rawengine3D.resource.TextureManager;

public class Quake3BSP {
	
	public final static int FACE_POLYGON = 1;
	public static int visibleFaces;

	private int numOfVerts;			// The number of verts in the model
	private int numOfFaces;			// The number of faces in the model
	private int numOfIndices;		// The number of indices for the model
	private int numOfTextures;		// The number of texture maps
	private int numOfLightmaps;
	private int numOfNodes;			// The number of nodes in the level
	private int numOfLeafs;			// The leaf count in the level
	private int numOfLeafFaces;		// The number of leaf faces in the level
	private int numOfPlanes;			// The number of planes in the level

	private int[] indices;	// The object's indices for rendering
	private BSPVertex[]  verts;		// The object's vertices
	private BSPFace[]	 faces;		// The faces information of the object
	private BSPNode[]    nodes;		// The nodes in the bsp tree
	private BSPLeaf[]    leafs;		// The leafs in the bsp tree
	private BSPPlane[]   planes;		// The planes stored in the bsp tree
	private int[]        leafFaces;	// The leaf's faces in the bsp tree
	private BSPVisData   clusters;	// The clusters in the bsp tree for space partitioning
								
	private int[] textures;	
								
	private BitSet facesDrawn;		// The bitset for the faces that have/haven't been drawn
	
	private BinaryLoader loader;
	private TextureManager texManager;
	private TextureManager texManagerLight;
	
	IntBuffer lightBuffer;
	

	private Texture texLight;
	
	private boolean hasTextures;
	private boolean hasLightmaps;
	
	
	public Quake3BSP()
	{
		// Here we simply initialize our member variables to 0
		numOfVerts    = 0;	
		numOfFaces    = 0;
		numOfIndices  = 0;
		numOfTextures = 0;
		setNumOfLightmaps(0);

		verts = null;
		faces = null;
		indices = null;
		
		texManager = new TextureManager();
		texManagerLight = new TextureManager();
		
		lightBuffer = ByteBuffer.allocateDirect(4*100).order(ByteOrder.nativeOrder()).asIntBuffer();
		
			
		setHasTextures(true);
		setHasLightmaps(true);
		
	}
	

	// This is our integer vector structure
	public class Vector3i
	{
		int x;
		int y;
		int z;				// The x y and z position of our integer vector
		
		public Vector3i()
		{
			x = loader.readInt();
			y = loader.readInt();
			z = loader.readInt();
		}
		
		
	}


	// This is our BSP header structure
	public class BSPHeader
	{
	    String nameID;				// This should always be 'IBSP'
	    int version;				// This should be 0x2e for Quake 3 files
	    
	    public BSPHeader()
	    {
	    	nameID = loader.readString(4);
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
	
	// This is our BSP lightmap structure which stores the 128x128 RGB values
	public class BSPLightMap
	{
	   byte[] imageBits = new byte[128*128*3];   // The RGB data in a 128x128 image
	   	   
	   public BSPLightMap()
	   {
		   int count = 0;
		   for(int i=0; i < 128; i++)
		   {
			   for(int j=0; j < 128; j++)
			   {
				   for(int k=0; k<3; k++)
				   {
					   imageBits[count] = (byte) loader.readByte();
					   count++;
				   }
			   }
		   }

	   }
	    
	    
	}

	// This is our BSP vertex structure
	public class BSPVertex
	{
	    Vector3f position;				// (x, y, z) position. 
	    TextureCoord textureCoord;		// (u, v) texture coordinate
	    TextureCoord lightmapCoord;		// (u, v) lightmap coordinate
	    Vector3f normal;				// (x, y, z) normal vector
	    byte[] color = new byte[4];		// RGBA color for the vertex 
	    
	    public BSPVertex()
	    {
	    	position = new Vector3f(loader.readFloat(), loader.readFloat(), loader.readFloat());
	    	textureCoord = new TextureCoord(loader.readFloat(), loader.readFloat());
	    	lightmapCoord = new TextureCoord(loader.readFloat(), loader.readFloat());
	    	normal = new Vector3f(loader.readFloat(), loader.readFloat(), loader.readFloat());
	    	
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
	    Vector3f normal;			// The face normal. 
	    int[] size = new int[2];				// The bezier patch dimensions. 
	    	   
	    FloatBuffer vertFloatBuffer;
	    FloatBuffer texFloatBuffer;
	    FloatBuffer lightFloatBuffer;
	    
	    IntBuffer indiceIntBuffer;
	    
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
		    normal = new Vector3f(loader.readFloat(), loader.readFloat(), loader.readFloat());
		    size[0] = loader.readInt();
		    size[1] = loader.readInt();
		    
		    vertFloatBuffer = getVertFloatBuffer();
		    
			texFloatBuffer = getTexFloatBuffer();
			
			lightFloatBuffer = getLightFloatBuffer();
			
			indiceIntBuffer = getIndiceIntBuffer();
			
			
	    }
	    
	    private FloatBuffer getVertFloatBuffer()
	    {
	    	int b = 0;
	    	float[] tempVertFloat = new float[numOfVerts*3];
			for(int a=startVertIndex; a < startVertIndex+numOfVerts; a++)
			{
				tempVertFloat[b]   = verts[a].position.x;
				tempVertFloat[b+1] = verts[a].position.y;
				tempVertFloat[b+2] = verts[a].position.z;
				b +=3;
				
			}
			
			return Conversion.allocFloats(tempVertFloat);
	    }
	    
	    private FloatBuffer getTexFloatBuffer()
	    {
	    	int c = 0;
	    	float[] tempTexFloat = new float[numOfVerts * 2];
			for(int a=startVertIndex; a < startVertIndex+numOfVerts; a++)
			{
				tempTexFloat[c] = verts[a].textureCoord.s;
				tempTexFloat[c+1] = verts[a].textureCoord.t;
				c +=2;
				
			}
			
			return Conversion.allocFloats(tempTexFloat);
	    	
	    }
	    
	    private FloatBuffer getLightFloatBuffer()
	    {
	    	float[] tempLightFloat = new float[numOfVerts * 2];
			int c = 0;
			for(int a=startVertIndex; a < startVertIndex+numOfVerts; a++)
			{
				tempLightFloat[c] = verts[a].lightmapCoord.s;
				tempLightFloat[c+1] = verts[a].lightmapCoord.t;
				c +=2;
				
			}
			
			return Conversion.allocFloats(tempLightFloat);
	    }
	    
	    private IntBuffer getIndiceIntBuffer()
	    {
	    	int[] tempIndicesInt = new int[numOfIndices]; 
			
			for(int a=0; a < tempIndicesInt.length; a++)
			{
				tempIndicesInt[a] = indices[a+startIndex];
			}
			
			return  Conversion.allocInts(tempIndicesInt);
	    }
	    
	}


	// This is our BSP texture structure
	public class BSPTexture
	{
	    String textureName;				// The name of the texture w/o the extension 
	    int flags;					// The surface flags (unknown) 
	    int contents;				// The content flags (unknown)
	    
	    public BSPTexture()
	    {
	    	textureName = loader.readString(64);
	    	flags = loader.readInt();
	    	contents = loader.readInt();
	    }
	}
	
	// This stores a node in the BSP tree
	public class BSPNode
	{
	    int plane;					// The index into the planes array 
	    int front;					// The child index for the front node 
	    int back;					// The child index for the back node 
	    Vector3i min;				// The bounding box min position. 
	    Vector3i max;				// The bounding box max position. 
	    
	    public BSPNode()
	    {
	    	plane = loader.readInt();
	    	front = loader.readInt();
	    	back = loader.readInt();
	    	min = new Vector3i();
	    	max = new Vector3i();
	    }
	} 
	
	// This stores a leaf (end node) in the BSP tree
	public class BSPLeaf
	{
	    int cluster;				// The visibility cluster 
	    int area;					// The area portal 
	    Vector3i min;				// The bounding box min position 
	    Vector3i max;				// The bounding box max position 
	    int leafFace;				// The first index into the face array 
	    int numOfLeafFaces;			// The number of faces for this leaf 
	    int leafBrush;				// The first index for into the brushes 
	    int numOfLeafBrushes;		// The number of brushes for this leaf
	    
	    public BSPLeaf()
	    {
	    	cluster = loader.readInt();
	    	area = loader.readInt();
	    	min = new Vector3i();
	    	max = new Vector3i();
	    	leafFace = loader.readInt();
	    	numOfLeafFaces = loader.readInt();
	    	leafBrush = loader.readInt();
	    	numOfLeafBrushes = loader.readInt();
	    }
	}
	
	// This stores a splitter plane in the BSP tree
	public class BSPPlane
	{
	    Vector3f normal;			// Plane normal. 
	    float distance;				// The plane distance from origin
	    
	    public BSPPlane()
	    {
	    	normal = new Vector3f(loader.readFloat(), loader.readFloat(), loader.readFloat());
	    	distance = loader.readFloat();
	    }
	}

	// This stores the cluster data for the PVS's
	public class BSPVisData
	{
		int numOfClusters;			// The number of clusters
		int bytesPerCluster;		// The amount of bytes (8 bits) in the cluster's bitset
		byte[] bitSets = null;			// The array of bytes that holds the cluster bitsets
		
		public BSPVisData()
		{
			numOfClusters = loader.readInt();
			bytesPerCluster = loader.readInt();
			
			int size = numOfClusters * bytesPerCluster;
			bitSets = new byte[size];
			for(int i=0; i<bitSets.length; i++)
			{
				bitSets[i] = (byte) loader.readByte();
			}
		}
	}

	
	
	////////////////////////////FIND TEXTURE EXTENSION \\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This attaches the image extension to the texture name, if found
	/////
	//////////////////////////// FIND TEXTURE EXTENSION \\\\\\\\\\\\\\\\\\\\\\\\\\\*
	
	private String findTextureExtension(String fileName)
	{
		
		File fileJpg = new File(fileName + ".jpg");
		File fileTga = new File(fileName + ".tga");
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
	
		if(fileJpg.exists())
		{
			return fileJpg.getAbsolutePath();
		}else if(fileTga.exists())
		{
			return fileTga.getAbsolutePath();
	
		}
		
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
	
	public boolean loadBSP(String fileName, String factorGamma) throws IOException
	{
		File file = new File(fileName);
		
		if(file.exists())
		{
			System.out.println(file.getAbsolutePath());
			loader = new BinaryLoader(file);
		}
		
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
		@SuppressWarnings("unused")
		BSPHeader header = null;
		BSPLump[] lumps = new BSPLump[Lumps.kMaxLumps.ordinal()];
	
		// Read in the header and lump data
		header = new BSPHeader();
		
		for(int j=0; j<lumps.length; j++)
		{
			lumps[j] = new BSPLump();
		}
		
		// Now we know all the information about our file.  We can
		// then allocate the needed memory for our member variables.
	
		// Allocate the vertex memory
		numOfVerts = lumps[Lumps.kVertices.ordinal()].length / 44;
		verts     = new BSPVertex [numOfVerts];
	
		// Allocate the face memory
		numOfFaces = lumps[Lumps.kFaces.ordinal()].length / 104;
		faces     = new BSPFace [numOfFaces];
	
		// Allocate the index memory
		numOfIndices = lumps[Lumps.kIndices.ordinal()].length / 4;
		indices     = new int[numOfIndices];
	
		// Allocate memory to read in the texture information.
		// We create a local pointer of BSPTextures because we don't need
		// that information once we create texture maps from it.
		numOfTextures = lumps[Lumps.kTextures.ordinal()].length / 72;
		BSPTexture[] textures = new BSPTexture [numOfTextures];

		// Allocate memory to read in the lightmap data.  Like the texture
		// data, we just need to create a local array to be destroyed real soon.
		numOfLightmaps = lumps[Lumps.kLightmaps.ordinal()].length / 49152;
		BSPLightMap[] lightmaps = new BSPLightMap[numOfLightmaps];
	
		// Seek to the position in the file that stores the vertex information
		loader.seekMarkOffset(lumps[Lumps.kVertices.ordinal()].offset);
		
		// Since Quake has the Z-axis pointing up, we want to convert the data so
		// that Y-axis is pointing up (like normal!) :)
	
		// Go through all of the vertices that need to be read
		for(int i = 0; i < numOfVerts; i++)
		{
			// Read in the current vertex
			verts[i] = new BSPVertex();
			// Swap the y and z values, and negate the new z so Y is up.
			float temp = verts[i].position.y;
			verts[i].position.y = verts[i].position.z;
			verts[i].position.z = -temp;
		}	
	
		// Seek to the position in the file that stores the index information
		loader.seekMarkOffset(lumps[Lumps.kIndices.ordinal()].offset);
		
		// Read in all the index information
		for(int i=0; i < indices.length; i++)
		{
			indices[i] = loader.readInt();
		}

		// Seek to the position in the file that stores the face information
		loader.seekMarkOffset(lumps[Lumps.kFaces.ordinal()].offset);
	
		// Read in all the face information
		for(int i=0; i < numOfFaces; i++)
		{
			faces[i] = new BSPFace();
		}
	
		// Seek to the position in the file that stores the texture information
		loader.seekMarkOffset(lumps[Lumps.kTextures.ordinal()].offset);
		
		// Read in all the texture information
		for(int i=0; i < numOfTextures; i++)
		{
			textures[i] = new BSPTexture();
		}

		// Now that we have the texture information, we need to load the
		// textures.  Since the texture names don't have an extension, we need
		// to find it first.
	
		// Go through all of the textures
		for(int i = 0; i < numOfTextures; i++)
		{
			// Find the extension if any and append it to the file name
			textures[i].textureName = findTextureExtension(textures[i].textureName);
			
			// Create a texture from the image
						
			// If there is a valid texture name passed in, we want to set the texture data
			if(textures[i].textureName != null)
			{
				texManager.getLoader().setPosition(i);
				texManager.getNormalImage(textures[i].textureName, false, false);
			}else{
				int id = texManager.getTexture(i-1).getTexID();
				texManager.setTexture(new Texture(GL_TEXTURE_2D,id+1));
			}
				
		}
		
		// Seek to the position in the file that stores the lightmap information
		//fseek(fp, lumps[kLightmaps].offset, SEEK_SET);
		loader.seekMarkOffset(lumps[Lumps.kLightmaps.ordinal()].offset);
		// Go through all of the lightmaps and read them in
		for(int i = 0; i < numOfLightmaps ; i++)
		{
			// Read in the RGB data for each lightmap
			lightmaps[i] = new BSPLightMap();
			// Create a texture map for each lightmap that is read in.  The lightmaps
			// are always 128 by 128.
			createLightmapTexture(lightBuffer, i, lightmaps[i], 128, 128, factorGamma);
		}
		
		// In this function we read from a bunch of new lumps.  These include
		// the BSP nodes, the leafs, the leaf faces, BSP splitter planes and
		// visibility data (clusters).

		// Store the number of nodes and allocate the memory to hold them
		numOfNodes = lumps[Lumps.kNodes.ordinal()].length / 36;
		nodes     = new BSPNode[numOfNodes];

		// Seek to the position in the file that hold the nodes and store them in m_pNodes
		loader.seekMarkOffset(lumps[Lumps.kNodes.ordinal()].offset);
		for(int i=0; i < numOfNodes; i++)
		{
			nodes[i] = new BSPNode();
		}
		
		// Store the number of leafs and allocate the memory to hold them
		numOfLeafs = lumps[Lumps.kLeafs.ordinal()].length / 48;
		leafs     = new BSPLeaf[numOfLeafs];

		// Seek to the position in the file that holds the leafs and store them in m_pLeafs
		loader.seekMarkOffset(lumps[Lumps.kLeafs.ordinal()].offset);
		// Now we need to go through and convert all the leaf bounding boxes
		// to the normal OpenGL Y up axis.
		for(int i=0; i < numOfLeafs; i++)
		{
			leafs[i] = new BSPLeaf();
			
			int temp = leafs[i].min.y;
			leafs[i].min.y = leafs[i].min.z;
			leafs[i].min.z = -temp;

			// Swap the max y and z values, then negate the new Z
			temp = leafs[i].max.y;
			leafs[i].max.y = leafs[i].max.z;
			leafs[i].max.z = -temp;
		}

	
		// Store the number of leaf faces and allocate the memory for them
		numOfLeafFaces = lumps[Lumps.kLeafFaces.ordinal()].length / 4;
		leafFaces     = new int[numOfLeafFaces];

		// Seek to the leaf faces lump, then read it's data
		//fseek(fp, lumps[kLeafFaces].offset, SEEK_SET);
		loader.seekMarkOffset(lumps[Lumps.kLeafFaces.ordinal()].offset);

		// Read in all the index information
		for(int i=0; i < leafFaces.length; i++)
		{
			leafFaces[i] = loader.readInt();
		}

		// Store the number of planes, then allocate memory to hold them
		numOfPlanes = lumps[Lumps.kPlanes.ordinal()].length / 16;
		planes     = new BSPPlane[numOfPlanes];

		// Seek to the planes lump in the file, then read them into m_pPlanes
		loader.seekMarkOffset(lumps[Lumps.kPlanes.ordinal()].offset);
		
		// Go through every plane and convert it's normal to the Y-axis being up
		for(int i = 0; i < numOfPlanes; i++)
		{
			planes[i] = new BSPPlane();
			
			float temp = planes[i].normal.y;
			planes[i].normal.y = planes[i].normal.z;
			planes[i].normal.z = -temp;
		}

		// Seek to the position in the file that holds the visibility lump
		loader.seekMarkOffset(lumps[Lumps.kVisData.ordinal()].offset);
		// Check if there is any visibility information first
		if(lumps[Lumps.kVisData.ordinal()].length > 0) 
		{
						
			clusters = new BSPVisData();
			
		}
		
	
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
		
			facesDrawn = new BitSet();
			facesDrawn.resize(numOfFaces);
	
		// Return a success
		return true;
	}
	
	//////////////////////////////CREATE LIGHTMAP TEXTURE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This creates a texture map from the light map image bits
	/////
	////////////////////////////// CREATE LIGHTMAP TEXTURE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	
	private void createLightmapTexture(IntBuffer texture, int position, BSPLightMap imageBits, int width, int height, String factorGamma) throws IOException
	{
		// This function takes in the lightmap image bits and creates a texture map
		// from them.  The width and height is usually 128x128 anyway....
		
		// Generate a texture with the associative texture ID stored in the array
		//IntBuffer temp = ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder()).asIntBuffer();
		
		glGenTextures(lightBuffer);
		int textID = lightBuffer.get(position);
		//System.out.println(textID);
		texLight = new Texture(GL_TEXTURE_2D, textID);
		// This sets the alignment requirements for the start of each pixel row in memory.
		glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
		
		// Bind the texture to the texture arrays index and init the texture
		texLight.bind();
		texManagerLight.setTexture(texLight);
	
		// Change the lightmap gamma values by our desired gamma
		changeGamma(imageBits, imageBits.imageBits.length, factorGamma);
	 
		ByteBuffer imageBuffer = ByteBuffer.allocateDirect(imageBits.imageBits.length); 
        imageBuffer.order(ByteOrder.nativeOrder()); 
        imageBuffer.put(imageBits.imageBits, 0, imageBits.imageBits.length); 
        imageBuffer.flip();
      
		//Build Mipmaps (builds different versions of the picture for distances - looks better)
		gluBuild2DMipmaps(GL_TEXTURE_2D, 3, width, height, GL_RGB, GL_UNSIGNED_BYTE, imageBuffer);
            	
		//Assign the mip map levels		
		glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MAG_FILTER,GL_LINEAR);	
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_NEAREST);
		glTexEnvf(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE);
		
	}
	
	
	////////////////////////////CHANGE GAMMA \\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This manually changes the gamma of an image
	/////
	//////////////////////////// CHANGE GAMMA \\\\\\\\\\\\\\\\\\\\\\\\\\\*
	
	private void changeGamma(BSPLightMap light, int size, String factorGamma)
	{
		//  This function was taken from a couple engines that I saw,
		// which most likely originated from the Aftershock engine.
		// Kudos to them!  What it does is increase/decrease the intensity
		// of the lightmap so that it isn't so dark.  Quake uses hardware to
		// do this, but we will do it in code.
	
		float factor = Float.parseFloat(factorGamma);
		
		// Go through every pixel in the lightmap
		//for(int i = 0; i < size / 3; i++, pImage += 3) 
		
				
		for(int i = 0; i < size; i+=3) 
		{
			float scale = 1.0f, temp = 0.0f;
			float r = 0, g = 0, b = 0;
	
			// extract the current RGB values
			
			
			r = (float)(light.imageBits[i] & 0xff);
			g = (float)(light.imageBits[i+1] & 0xff);
			b = (float)(light.imageBits[i+2] & 0xff);
	
			// Multiply the factor by the RGB values, while keeping it to a 255 ratio
			r = r * factor / 255.0f;
			g = g * factor / 255.0f;
			b = b * factor / 255.0f;
			
			// Check if the the values went past the highest value
			if(r > 1.0f && (temp = (1.0f/r)) < scale) scale=temp;
			if(g > 1.0f && (temp = (1.0f/g)) < scale) scale=temp;
			if(b > 1.0f && (temp = (1.0f/b)) < scale) scale=temp;
	
			// Get the scale for this pixel and multiply it by our pixel values
			scale*=255.0f;		
			r*=scale;	g*=scale;	b*=scale;
	
			// Assign the new gamma'nized RGB values to our image
			light.imageBits[i] = (byte)r;
			light.imageBits[i+1] = (byte)g;
			light.imageBits[i+2] = (byte)b;
		}
	}
	
	////////////////////////////FIND LEAF \\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This returns the leaf our camera is in
	/////
	//////////////////////////// FIND LEAF \\\\\\\\\\\\\\\\\\\\\\\\\\\*
	
	public int findLeaf(Vector3f position)
	{
		int i = 0;
		float distance = 0.0f;
	
		// This function takes in our camera position, then goes and walks
		// through the BSP nodes, starting at the root node, finding the leaf node
		// that our camera resides in.  This is done by checking to see if
		// the camera is in front or back of each node's splitting plane.
		// If the camera is in front of the camera, then we want to check
		// the node in front of the node just tested.  If the camera is behind
		// the current node, we check that nodes back node.  Eventually, this
		// will find where the camera is according to the BSP tree.  Once a
		// node index (i) is found to be a negative number, that tells us that
		// that index is a leaf node, not another BSP node.  We can either calculate
		// the leaf node index from -(i + 1) or ~1.  This is because the starting
		// leaf index is 0, and you can't have a negative 0.  It's important
		// for us to know which leaf our camera is in so that we know which cluster
		// we are in.  That way we can test if other clusters are seen from our cluster.
		
		// Continue looping until we find a negative index
		while(i >= 0)
		{
			// Get the current node, then find the slitter plane from that
			// node's plane index.  Notice that we use a constant reference
			// to store the plane and node so we get some optimization.
			BSPNode  node = nodes[i];
			BSPPlane plane = planes[node.plane];
	
			// Use the Plane Equation (Ax + by + Cz + D = 0) to find if the
			// camera is in front of or behind the current splitter plane.
			
	        distance =	plane.normal.x * position.x + 
						plane.normal.y * position.y + 
						plane.normal.z * position.z - plane.distance;
	
			// If the camera is in front of the plane
	        if(distance >= 0)	
			{
				// Assign the current node to the node in front of itself
	            i = node.front;
	        }
			// Else if the camera is behind the plane
	        else		
			{
				// Assign the current node to the node behind itself
	            i = node.back;
	        }
	    }
	
		// Return the leaf index (same thing as saying:  return -(i + 1)).
	    return ~i;  // Binary operation
	}
	
	
	////////////////////////////IS CLUSTER VISIBLE \\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This tells us if the "current" cluster can see the "test" cluster
	/////
	//////////////////////////// IS CLUSTER VISIBLE \\\\\\\\\\\\\\\\\\\\\\\\\\\*
	
	private final int isClusterVisible(int current, int test)
	{
		// This function is used to test the "current" cluster against
		// the "test" cluster.  If the "test" cluster is seen from the
		// "current" cluster, we can then draw it's associated faces, assuming
		// they aren't frustum culled of course.  Each cluster has their own
		// bitset containing a bit for each other cluster.  For instance, if there
		// is 10 clusters in the whole level (a tiny level), then each cluster
		// would have a bitset of 10 bits that store a 1 (visible) or a 0 (not visible) 
		// for each other cluster.  Bitsets are used because it's faster and saves
		// memory, instead of creating a huge array of booleans.  It seems that
		// people tend to call the bitsets "vectors", so keep that in mind too.
	
		// Make sure we have valid memory and that the current cluster is > 0.
		// If we don't have any memory or a negative cluster, return a visibility (1).
		if(clusters.bitSets == null || current < 0) return 1;
	
		// Use binary math to get the 8 bit visibility set for the current cluster
		byte visSet = clusters.bitSets[(current*clusters.bytesPerCluster) + (test / 8)];
		
		// Now that we have our vector (bitset), do some bit shifting to find if
		// the "test" cluster is visible from the "current" cluster, according to the bitset.
		int result = visSet & (1 << ((test) & 7));
	
		// Return the result ( either 1 (visible) or 0 (not visible) )
		return ( result );
	}
	
	////////////////////////////RENDER FACE \\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This renders a face, determined by the passed in index
	/////
	//////////////////////////// RENDER FACE \\\\\\\\\\\\\\\\\\\\\\\\\\\*
	
	private void renderFace(int faceIndex)
	{
		// Here we grab the face from the index passed in
		BSPFace face = faces[faceIndex];
		
		
		// Now, in this function you don't might get all messed up and confused with
		// what function is for vertex arrays and which function is for multi-texturing.
		// The gl*Pointer() and glEnableClientState() functions are for vertex arrays.
		// The glActiveTextureARG() and glClientActiveTextureARB() stuff is for multi-texturing.  
		// Since we allow the user to right or left click the mouse, turning on and off the 
		// light maps and textures, we need to make those checks in this function to know 
		// what we should render.
		
		
		// Point OpenGL to our vertex array.  We have our vertices stored in
		// 3 floats, with 0 stride between them in bytes.
		/*
		float[] tempVertFloat = new float[face.numOfVerts*3];
		int b = 0;
		for(int a=face.startVertIndex; a < face.startVertIndex+face.numOfVerts; a++)
		{
			tempVertFloat[b]   = verts[a].position.x;
			tempVertFloat[b+1] = verts[a].position.y;
			tempVertFloat[b+2] = verts[a].position.z;
			b +=3;
			
		}
		
		FloatBuffer vertFloatBuffer = Conversion.allocFloats(tempVertFloat);
		*/
		glVertexPointer(3, 0, face.vertFloatBuffer);
		//glVertexPointer(3, GL_FLOAT, sizeof(tBSPVertex), &(m_pVerts[pFace->startVertIndex].vPosition));
		glEnableClientState(GL_VERTEX_ARRAY);
		// Next, we pass in the address of the first texture coordinate.  We also tell 
		// OpenGL that there are 2 UV coordinates that are floats, and the offset between 
		// each texture coordinate is the size of tBSPVertex in bytes.  
		// We need to them give an address to the start of this face's indices, startVertIndex.
		

		// If we want to render the textures
		if(isTextures())
		{		
			// Set the current pass as the first texture (For multi-texturing)
			glActiveTextureARB(GL_TEXTURE0_ARB);

			// Since we are using vertex arrays, we need to tell OpenGL which texture
			// coordinates to use for each texture pass.  We switch our current texture
			// to the first one, then set our texture coordinates.
			glClientActiveTextureARB(GL_TEXTURE0_ARB);
			/*
			float[] tempTexFloat = new float[face.numOfVerts * 2];
			int c = 0;
			for(int a=face.startVertIndex; a < face.startVertIndex+face.numOfVerts; a++)
			{
				tempTexFloat[c] = verts[a].textureCoord.s;
				tempTexFloat[c+1] = verts[a].textureCoord.t;
				c +=2;
				
			}
			
			FloatBuffer texFloatBuffer = Conversion.allocFloats(tempTexFloat);
			*/
			glTexCoordPointer(2, 0, face.texFloatBuffer);
			
			// Set our vertex array client states for allowing texture coordinates
			glEnableClientState(GL_TEXTURE_COORD_ARRAY);

			// Turn on texture arrays for the first pass
			glClientActiveTextureARB(GL_TEXTURE0_ARB);

			// To enable each texture pass, we want to turn on the texture coord array
			// state for each pass.  This needs to be done since we are using vertex arrays.
			glEnable(GL_TEXTURE_2D);
			//System.out.println(face.textureID);
			texManager.getTexture(face.textureID).bind();
		}
		
		// If we want to render the textures
		if(isHasLightmaps() && face.lightmapID >= 0)
		{		
			// Set the current pass as the second lightmap texture_
			glActiveTextureARB(GL_TEXTURE1_ARB);

			// Turn on texture arrays for the second lightmap pass
			glClientActiveTextureARB(GL_TEXTURE1_ARB);
			glEnableClientState(GL_TEXTURE_COORD_ARRAY);

			// Next, we need to specify the UV coordinates for our lightmaps.  This is done
			// by switching to the second texture and giving OpenGL our lightmap array.
			glClientActiveTextureARB(GL_TEXTURE1_ARB);
			/*
			float[] tempLightFloat = new float[face.numOfVerts * 2];
			int c = 0;
			for(int a=face.startVertIndex; a < face.startVertIndex+face.numOfVerts; a++)
			{
				tempLightFloat[c] = verts[a].lightmapCoord.s;
				tempLightFloat[c+1] = verts[a].lightmapCoord.t;
				c +=2;
				
			}
			
			FloatBuffer lightFloatBuffer = Conversion.allocFloats(tempLightFloat);
			*/
			glTexCoordPointer(2, 0, face.lightFloatBuffer);
						
			// Turn on texture mapping and bind the face's texture map
			glEnable(GL_TEXTURE_2D);
			//texLight.bind();
			texManagerLight.getTexture(face.lightmapID).bind();
			
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
		/*
		int[] tempIndicesInt = new int[face.numOfIndices]; 
		
		for(int a=0; a < tempIndicesInt.length; a++)
		{
			tempIndicesInt[a] = indices[a+face.startIndex];
		}
		
		IntBuffer indiceIntBuffer = Conversion.allocInts(tempIndicesInt);
		*/
		glDrawElements(GL_TRIANGLES, face.indiceIntBuffer);
		//glDrawElements(GL_TRIANGLES, pFace->numOfIndices, GL_UNSIGNED_INT, &(m_pIndices[pFace->startIndex]) );
		 
	}


	//////////////////////////// RENDER LEVEL \\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	Goes through all of the faces and draws them if the type is FACE_POLYGON
	/////
	//////////////////////////// RENDER LEVEL \\\\\\\\\\\\\\\\\\\\\\\\\\\*
	
	public void renderLevel(Vector3f position)
	{

		// Reset our bitset so all the slots are zero.
		facesDrawn.clearAll();


	/////// * /////////// * /////////// * NEW * /////// * /////////// * /////////// *

		// In this new revision of RenderLevel(), we do things a bit differently.
		// Instead of looping through all the faces, we now want to loop through
		// all of the leafs.  Each leaf stores a list of faces assign to it.
		// We call FindLeaf() to find the current leaf index that our camera is
		// in.  This leaf will then give us the cluster that the camera is in.  The
		// cluster is then used to test visibility between our current cluster
		// and other leaf clusters.  If another leaf's cluster is visible from our 
		// current cluster, the leaf's bounding box is checked against our frustum.  
		// Assuming the bounding box is inside of our frustum, we draw all the faces
		// stored in that leaf.  

		// Grab the leaf index that our camera is in
		int leafIndex = findLeaf(position);

		// Grab the cluster that is assigned to the leaf
		int cluster = leafs[leafIndex].cluster;

		// Initialize our counter variables (start at the last leaf and work down)
		int i = numOfLeafs;
		visibleFaces = 0;

		// Go through all the leafs and check their visibility
		while(i-- > 0)
		{
			// Get the current leaf that is to be tested for visibility from our camera's leaf
			BSPLeaf leaf = leafs[i];

			// If the current leaf can't be seen from our cluster, go to the next leaf
			if(isClusterVisible(cluster, leaf.cluster) == 0) 
				continue;

			// If the current leaf is not in the camera's frustum, go to the next leaf
			if(!GameCore.gFrustum.boxInFrustum((float)leaf.min.x, (float)leaf.min.y, (float)leaf.min.z,
			  	 				       (float)leaf.max.x, (float)leaf.max.y, (float)leaf.max.z))
				continue;
			
			// If we get here, the leaf we are testing must be visible in our camera's view.
			// Get the number of faces that this leaf is in charge of.
			int faceCount = leaf.numOfLeafFaces;

			// Loop through and render all of the faces in this leaf
			while(faceCount-- > 0)
			{
				// Grab the current face index from our leaf faces array
				int faceIndex = leafFaces[leaf.leafFace + faceCount];

				// Before drawing this face, make sure it's a normal polygon
				if(faces[faceIndex].type != FACE_POLYGON) continue;

				// Since many faces are duplicated in other leafs, we need to
				// make sure this face already hasn't been drawn.
				if(facesDrawn.on(faceIndex)) 
				{
					// Increase the rendered face count to display for fun
					visibleFaces++;
					//System.out.println(visibleFaces);
					// Set this face as drawn and render it
					facesDrawn.set(faceIndex);
					renderFace(faceIndex);
				}
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
	public void setIndices(int[] indices) 
	{
		this.indices = indices;
	}


	/**
	 * @return the pIndices
	 */
	public int[] getIndices()
	{
		return indices;
	}


	/**
	 * @param pVerts the pVerts to set
	 */
	public void setVerts(BSPVertex[] verts) 
	{
		this.verts = verts;
	}


	/**
	 * @return the pVerts
	 */
	public BSPVertex[] getVerts() 
	{
		return verts;
	}


	/**
	 * @param pFaces the pFaces to set
	 */
	public void setFaces(BSPFace[] faces) 
	{
		this.faces = faces;
	}


	/**
	 * @return the pFaces
	 */
	public BSPFace[] getFaces() 
	{
		return faces;
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


	/**
	 * @param renderFill the renderFill to set
	 */
	public void setHasTextures(boolean renderFill) {
		this.hasTextures = renderFill;
	}


	/**
	 * @return the renderFill
	 */
	public boolean isTextures() {
		return hasTextures;
	}


	/**
	 * @param numOfLightmaps the numOfLightmaps to set
	 */
	public void setNumOfLightmaps(int numOfLightmaps) {
		this.numOfLightmaps = numOfLightmaps;
	}


	/**
	 * @return the numOfLightmaps
	 */
	public int getNumOfLightmaps() {
		return numOfLightmaps;
	}


	/**
	 * @param hasLightmaps the hasLightmaps to set
	 */
	public void setHasLightmaps(boolean hasLightmaps) {
		this.hasLightmaps = hasLightmaps;
	}


	/**
	 * @return the hasLightmaps
	 */
	public boolean isHasLightmaps() {
		return hasLightmaps;
	}
	
	/*
	private void export(byte[] b) throws IOException
	{
		FileWriter file = new FileWriter("c:\\testeWriter.txt");
		BufferedWriter writer = new BufferedWriter(file);
		int count=0;
		for(int i=0; i<b.length;i+=3)
		{
		 	String temp = Byte.toString(b[i]);
		 	String temp1 = Byte.toString(b[i+1]);
		 	String temp2 = Byte.toString(b[i+2]);
		   	int te = Integer.parseInt(temp);
		   	int te1 = Integer.parseInt(temp1);
		   	int te2 = Integer.parseInt(temp2);
		   
		   	if(te < 10)
		   	{
		   		temp = "0" + temp;
		   	}
		   	if(te1 < 10)
		   	{
		   		temp1 = "0" + temp1;
		   	}
		   	if(te2 < 10)
		   	{
		   		temp2 = "0" + temp2;
		   	}
			writer.write("[");
			writer.write(temp);
			writer.write(",");
			writer.write(temp1);
			writer.write(",");
			writer.write(temp2);
			writer.write("]");
			count++;
			if(count > 128)
			{
				writer.newLine();
				count = 0;
			}
			
		}
		writer.close();
	}
	
	private void export(byte[][][] imageBits) throws IOException
	{
		FileWriter file = new FileWriter("c:\\testeWriter.txt");
		BufferedWriter writer = new BufferedWriter(file);
		
		  for(int i=0; i < imageBits.length; i++)
		   {
			 
			   for(int j=0; j < imageBits[i].length; j++)
			   {
				   writer.write("[");
				   for(int k=0; k<imageBits[i][j].length; k++)
				   {
					   	String temp = Byte.toString(imageBits[i][j][k]);
					   	int te = Integer.parseInt(temp);
					   
					   	if(te < 10)
					   	{
					   		temp = "0" + temp;
					   	}
						writer.write(temp);
						if(k != 2)
						writer.write(",");
					
				   }
				   writer.write("]");
			   }
			   writer.newLine();
		   }
			
			
		
		writer.close();
	}
	*/
}
