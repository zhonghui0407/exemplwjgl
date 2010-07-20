package fcampos.rawengine3D.model;

import java.io.IOException;

import fcampos.rawengine3D.MathUtil.Vector3f;
import fcampos.rawengine3D.graficos.Texture;
import fcampos.rawengine3D.loader.*;
import fcampos.rawengine3D.resource.TextureManager;

import static org.lwjgl.opengl.GL11.*;

public class ModelQuake3 {
	// These are are models for the character's head and upper and lower body parts
	private Model3d m_Head;
	private Model3d m_Upper;
	private Model3d m_Lower;

	// This store the players weapon model (optional load)
	private Model3d m_Weapon;
	
	public static TextureManager texManager;
	public TMD3Loader loadMd3;					// This object allows us to load the.md3 and .shader file
	
	public ModelQuake3()
	{
		m_Head = new Model3d();
		m_Upper = new Model3d();
		m_Lower = new Model3d();
		m_Weapon = new Model3d();
		loadMd3 = new TMD3Loader();
		texManager = new TextureManager();
		
	}
	
///////////////////////////////// LOAD MODEL \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
/////
/////	This loads our Quake3 model from the given path and character name
/////
///////////////////////////////// LOAD MODEL \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*

public void loadModel(String strPath, String strModel) throws IOException
{
	String strLowerModel;					// This stores the file name for the lower.md3 model
	String strUpperModel;					// This stores the file name for the upper.md3 model
	String strHeadModel;					// This stores the file name for the head.md3 model
	String strLowerSkin;					// This stores the file name for the lower.md3 skin
	String strUpperSkin;					// This stores the file name for the upper.md3 skin
	String strHeadSkin;						// This stores the file name for the head.md3 ski
	
	
	// This function is where all the character loading is taken care of.  We use
	// our CLoadMD3 class to load the 3 mesh and skins for the character. Since we
	// just have 1 name for the model, we add that to _lower.md3, _upper.md3 and _head.md3
	// to load the correct mesh files.

	// Make sure valid path and model names were passed in
	if(strPath == null || strModel == null) return;
	
	// Store the correct files names for the .md3 and .skin file for each body part.
	  // We concatinate this on top of the path name to be loaded from.
	strLowerModel = strPath + "/" + strModel + "_lower.md3";
	strUpperModel = strPath + "/" + strModel + "_upper.md3";
	strHeadModel = strPath + "/" + strModel + "_head.md3";
	
	// Get the skin file names with their path
	strLowerSkin = strPath + "/" + strModel + "_lower.skin";
	strUpperSkin = strPath + "/" + strModel + "_upper.skin";
	strHeadSkin = strPath + "/" + strModel + "_head.skin";
	
	// Next we want to load the character meshes.  The CModelMD3 class has member
	// variables for the head, upper and lower body parts.  These are of type t3DModel.
	// Depending on which model we are loading, we pass in those structures to ImportMD3.
	// This returns a true of false to let us know that the file was loaded okay.  The
	// appropriate file name to load is passed in for the last parameter.

	// Load the head mesh (*_head.md3) and make sure it loaded properly
	if(!loadMd3.importMD3(m_Head,  strHeadModel))
	{
		System.out.println("[Error]: unable to load the HEAD part from model \"" + strModel + "\".");
		System.exit(0);
	}

	 // Load the upper mesh (*_head.md3) and make sure it loaded properly
	if(!loadMd3.importMD3(m_Upper, strUpperModel))		
	{
		System.out.println("[Error]: unable to load the UPPER part from model \"" + strModel + "\".");
		System.exit(0);
	}

	  // Load the lower mesh (*_lower.md3) and make sure it loaded properly
	if(!loadMd3.importMD3(m_Lower, strLowerModel))
	{
		System.out.println("[Error]: unable to load the LOWER part from model \"" + strModel + "\".");
		System.exit(0);
	}

	  // Load the lower skin (*_upper.skin) and make sure it loaded properly
	if(!loadMd3.loadSkin(m_Lower, strLowerSkin))
	{
		System.out.println("[Error]: unable to load the LOWER part from model's skin \"" + strModel + "\".");
		System.exit(0);
	}

	  // Load the upper skin (*_upper.skin) and make sure it loaded properly
	if(!loadMd3.loadSkin(m_Upper, strUpperSkin))
	{
		System.out.println("[Error]: unable to load the UPPER part from model's skin \"" + strModel + "\".");
		System.exit(0);
	}

	  // Load the head skin (*_head.skin) and make sure it loaded properly
	if(!loadMd3.loadSkin(m_Head, strHeadSkin))
	{
		System.out.println("[Error]: unable to load the HEAD part from model's skin \"" + strModel + "\".");
		System.exit(0);
	}


	// Once the models and skins were loaded, we need to load then textures.
	// We don't do error checking for this because we call CreateTexture() and 
	// it already does error checking.  Most of the time there is only
	// one or two textures that need to be loaded for each character.  There are
	// different skins though for each character.  For instance, you could have a
	// army looking Lara Croft, or the normal look.  You can have multiple types of
	// looks for each model.  Usually it is just color changes though.

	// Load the lower, upper and head textures.  
	loadModelTextures(m_Lower, strPath);
	loadModelTextures(m_Upper, strPath);
	loadModelTextures(m_Head, strPath);

	// The character data should all be loaded when we get here (except the weapon).
	// Now comes the linking of the body parts.  This makes it so that the legs (lower.md3)
	// are the parent node, then the torso (upper.md3) is a child node of the legs.  Finally,
	// the head is a child node of the upper body.  What I mean by node, is that you can
	// think of the model having 3 bones and 2 joints.  When you translate the legs you want
	// the whole body to follow because they are inseparable (unless a magic trick goes wrong).
	// The same goes for the head, it should go wherever the body goes.  When we draw the
	// lower body, we then recursively draw all of it's children, which happen to be just the
	// upper body.  Then we draw the upper body's children, which is just the head.  So, to
	// sum this all up, to set each body part's children, we need to link them together.
	// For more information on tags, refer to the Quick Notes and the functions below.
	 // Link the lower body to the upper body when the tag "tag_torso" is found in our tag array
	linkModel(m_Lower, m_Upper, "tag_torso");

	  // Link the upper body to the head when the tag "tag_head" is found in our tag array
	linkModel(m_Upper, m_Head, "tag_head");
		
	
}


///////////////////////////////// LOAD WEAPON \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
/////
/////	This loads a Quake3 weapon model from the given path and weapon name
/////
///////////////////////////////// LOAD WEAPON \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*

public final void loadWeapon(String strPath, String strModel) throws IOException
{
	String strWeaponModel;					// This stores the file name for the weapon model
	String strWeaponShader;					// This stores the file name for the weapon shader.

	// Make sure valid path and model names were passed in
	if(strPath == null || strModel == null) return;
	
	  // Concatenate the path and model name together
	strWeaponModel = strPath + "/" + strModel + ".md3";
	

	
	
	// Next we want to load the weapon mesh.  The CModelMD3 class has member
	// variables for the weapon model and all it's sub-objects.  This is of type t3DModel.
	// We pass in a reference to this model in to ImportMD3 to save the data read.
	// This returns a true of false to let us know that the weapon was loaded okay.  The
	// appropriate file name to load is passed in for the last parameter.

	 // Load the weapon mesh (*.md3) and make sure it loaded properly
	if(!loadMd3.importMD3(m_Weapon, strWeaponModel))
	{
		System.out.println("[Error]: unable to load the weapon model \"" + strModel + "\".");
		System.exit(0);
	}

	// Unlike the other .MD3 files, a weapon has a .shader file attached with it, not a
	// .skin file.  The shader file has it's own scripting language to describe behaviors
	// of the weapon.  All we care about for this tutorial is it's normal texture maps.
	// There are other texture maps in the shader file that mention the ammo and sphere maps,
	// but we don't care about them for our purposes.  I gutted the shader file to just store
	// the texture maps.  The order these are in the file is very important.  The first
	// texture refers to the first object in the weapon mesh, the second texture refers
	// to the second object in the weapon mesh, and so on.  I didn't want to write a complex
	// .shader loader because there is a TON of things to keep track of.  It's a whole
	// scripting language for goodness sakes! :)  Keep this in mind when downloading new guns.

	 // Add the path, file name and .shader extension together to get the file name and path
	strWeaponShader = strPath + "/" + strModel + ".shader";

	  // Load our textures associated with the gun from the weapon shader file
	if(!loadMd3.loadShader(m_Weapon, strWeaponShader))
	{
		System.out.println("[Error]: unable to load the shader for weapon \"" + strModel + "\".");
		System.exit(0);
	}

	// We should have the textures needed for each weapon part loaded from the weapon's
	// shader, so let's load them in the given path.
	loadModelTextures(m_Weapon, strPath);

	// Just like when we loaded the character mesh files, we need to link the weapon to
	// our character.  The upper body mesh (upper.md3) holds a tag for the weapon.
	// This way, where ever the weapon hand moves, the gun will move with it.

	// Link the weapon to the model's hand that has the weapon tag
	linkModel(m_Upper, m_Weapon, "tag_weapon");
		
	
}



///////////////////////////////// LOAD MODEL TEXTURES \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
/////
/////	This loads the textures for the current model passed in with a directory
/////
///////////////////////////////// LOAD WEAPON \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*

private final void loadModelTextures(Model3d pModel, String strPath) throws IOException
{
	// This function loads the textures that are assigned to each mesh and it's
	// sub-objects.  For instance, the Lara Croft character has a texture for the body
	// and the face/head, and since she has the head as a sub-object in the lara_upper.md3 model, 
	// the MD3 file needs to contain texture information for each separate object in the mesh.
	// There is another thing to note too...  Some meshes use the same texture map as another 
	// one. We don't want to load 2 of the same texture maps, so we need a way to keep track of
	// which texture is already loaded so that we don't double our texture memory for no reason.
	// This is controlled with a STL vector list of "strings".  Every time we load a texture
	// we add the name of the texture to our list of strings.  Before we load another one,
	// we make sure that the texture map isn't already in our list.  If it is, we assign
	// that texture index to our current models material texture ID.  If it's a new texture,
	// then the new texture is loaded and added to our characters texture array: m_Textures[].
		// Go through all the materials that are assigned to this model
		for(int i = 0; i < pModel.getMaterials().size(); i++)
		{
			  // Check to see if there is a file name to load in this material
			if(pModel.getMaterials(i).getName() != null)
			{
				 
				String strFullPath;

				  // Add the file name and path together so we can load the texture
				strFullPath = strPath + "/" + pModel.getMaterials(i).getName();
				
				//Texture tex = texManager.getNormalImage("texturas/" + strTexture,true, true);
				Texture tex = texManager.getFlippedImage(strFullPath,true, false);
				//Texture tex = texManager.getMirrorImage("texturas/" + strTexture,true, true);
				  // Go through all the textures in our string list to see if it's already loaded
				
						  // Assign the texture index to our current material textureID.
				pModel.getMaterials(i).setTexureId(tex.getTexID());
					
				}

				
		}
	}




///////////////////////////////// LINK MODEL \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
/////
/////	This links the body part models to each other, along with the weapon
/////
///////////////////////////////// LINK MODEL \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*

public final void linkModel(Model3d pModel, Model3d pLink, String strTagName)
{
	
	// Make sure we have a valid model, link and tag name, otherwise quit this function
	if(pModel == null || pLink == null || strTagName == null) return;
	
	// This function is used to link 2 models together at a psuedo joint.  For instance,
	// if we were animating an arm, we would link the top part of the arm to the shoulder,
	// then the forearm to would be linked to the top part of the arm, then the hand to
	// the forearm.  That is what is meant by linking.  That way, when we rotate the
	// arm at the shoulder, the rest of the arm will move with it because they are attached
	// to the same matrix that moves the top of the arm.  You can think of the shoulder
	// as the arm's parent node, and the rest are children that are subject to move to where
	// ever the top part of the arm goes.  That is how bone/skeletal animation works.
	//
	// So, we have an array of tags that have a position, rotation and name.  If we want
	// to link the lower body to the upper body, we would pass in the lower body mesh first,
	// then the upper body mesh, then the tag "tag_torso".  This is a tag that quake set as
	// as a standard name for the joint between the legs and the upper body.  This tag was
	// saved with the lower.md3 model.  We just need to loop through the lower body's tags,
	// and when we find "tag_torso", we link the upper.md3 mesh too that tag index in our
	// pLinks array.  This is an array of pointers to hold the address of the linked model.
	// Quake3 models are set up in a weird way, but usually you would just forget about a
	// separate array for links, you would just have a pointer to a t3DModel in the tag
	// structure, which in retrospect, you wouldn't have a tag array, you would have
	// a bone/joint array.  Stayed tuned for a bone animation tutorial from scratch.  This
	// will show you exactly what I mean if you are confused.


		// Go through all of our tags and find which tag contains the strTagName, then link'em
		for(int i = 0; i < pModel.getNumOfTags(); i++)
		{
			  // If this current tag index has the tag name we are looking for
			if(pModel.getTags(i).strName.equals(strTagName))
			{
				  // Link the model's link index to the link (or model/mesh) and return
				pModel.setLinks(pLink, i);
				return;
			}
		}
	
}


///////////////////////////////// DRAW MODEL \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
/////
/////	This recursively draws all the character nodes, starting with the legs
/////
///////////////////////////////// DRAW MODEL \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*

public void drawModel()
{
	// This is the function that is called by the client (you) when using the 
	// CModelMD3 class object.  You will notice that we rotate the model by
	// -90 degrees along the x-axis.  This is because most modelers have z up
	// so we need to compensate for this.  Usually I would just switch the
	// z and y values when loading in the vertices, but the rotations that
	// are stored in the tags (joint info) are a matrix, which makes it hard
	// to change those to reflect Y up.  I didn't want to mess with that so
	// this 1 rotate will fix this problem.

	// Rotate the model to compensate for the z up orientation that the model was saved
	glRotatef(-90, 1, 0, 0);

	// You might be thinking to draw the model we would just call RenderModel()
	// 4 times for each body part and the gun right?  That sounds logical, but since
	// we are doing a bone/joint animation... and the models need to be linked together,
	// we can't do that.  It totally would ignore the tags.  Instead, we start at the
	// root model, which is the legs.  The legs drawn first, then we go through each of
	// the legs linked tags (just the upper body) and then it with the tag's rotation
	// and translation values.  I ignored the rotation in this tutorial since we aren't
	// doing any animation.  I didn't want to overwhelm you with quaternions just yet :)
	// Normally in skeletal animation, the root body part is the hip area.  Then the legs
	// bones are created as children to the torso.  The upper body is also a child to
	// the torso.  Since the legs are one whole mesh, this works out somewhat the same way.  
	// This wouldn't work if the feet and legs weren't connected in the same mesh because
	// the feet rotations and positioning don't directly effect the position and rotation
	// of the upper body, the hips do.  If that makes sense...  That is why the root starts
	// at the hips and moves down the legs, and also branches out to the upper body and
	// out to the arms.

	// Draw the first link, which is the lower body.  This will then recursively go
	// through the models attached to this model and drawn them.
	drawLink(m_Lower);
}


///////////////////////////////// DRAW LINK \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
/////
/////	This draws the current mesh with an effected matrix stack from the last mesh
/////
///////////////////////////////// DRAW LINK \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*

private void drawLink(Model3d pModel)
{
	// This function is our recursive function that handles the bone animation
	// so to speak.  We first draw the model that is passed in (first the legs),
	// then go through all of it's tags and draw them.  Notice that when we
	// draw the model that is linked to the current model a new matrix scope
	// is created with glPushMatrix() and glPopMatrix().  This is because each tag
	// has a rotation and translation operation assigned to it.  For instance, when
	// Lara does her back flip death animation, the legs send a rotation and translation 
	// to the rest of the body to be rotated along with the legs as they flip backwards.  
	// If you didn't do this, Lara's body and head would stay in the same place as the
	// legs did a back flipped and landed on the floor.  Of course, this would look really
	// stupid.  A 270-degree rotation to the rest of the body is done for that animation.
	// Keep in mind, the legs mesh is NEVER translated or rotated.  It only rotates and
	// translates the upper parts of the body.  All the rotation and translation of the
	// legs is done in the canned animation that was created in the modeling program.
	// Keep in mind that I ignore the rotation value for that is given in the tag since
	// it doesn't really matter for a static model.  Also, since the rotation is given
	// in a 3x3 rotation matrix, it would be a bit more code that could make you frustrated.

	// Draw the current model passed in (Initially the legs)
	renderModel(pModel);

	// Now we need to go through all of this models tags and draw them.
	for(int i = 0; i < pModel.getNumOfTags(); i++)
	{
		// Get the current link from the models array of links (Pointers to models)
		Model3d pLink = pModel.getLinks(i);

		// If this link has a valid address, let's draw it!
		if(pLink != null)
		{			
			// Let's grab the translation for this new model that will be drawn 
			Vector3f vPosition = pModel.getTags(i).vPosition;

			// Start a new matrix scope
			glPushMatrix();
			
				// Translate the new model to be drawn to it's position
				glTranslatef(vPosition.x, vPosition.y, vPosition.z);

				// Recursively draw the next model that is linked to the current one.
				// This could either be a body part or a gun that is attached to
				// the hand of the upper body model.
				drawLink(pLink);

			// End the current matrix scope
			glPopMatrix();
		}
	}

}


///////////////////////////////// RENDER MODEL \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
/////
/////	This renders the model data to the screen
/////
///////////////////////////////// RENDER MODEL \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*

private void renderModel(Model3d pModel)
{
	// This function actually does the rendering to OpenGL.  If you have checked out
	// our other file loading tutorials, it looks pretty much the same as those.  I
	// left out the normals though.  You can go to any other loading and copy the code
	// from those.  Usually the Quake models creating the lighting effect in their textures
	// anyway.  

	// Make sure we have valid objects just in case. (size() is in the STL vector class)
	if(pModel.getObject().size() <= 0) return;

	// Go through all of the objects stored in this model
	for(int i = 0; i < pModel.getObject().size(); i++)
	{
		// Get the current object that we are displaying
		Object3d pObject = pModel.getObject(i);

		// If the object has a texture assigned to it, let's bind it to the model.
		// This isn't really necessary since all models have textures, but I left this
		// in here to keep to the same standard as the rest of the model loaders.
		if(pObject.isbHasTexture())
		{
			// Turn on texture mapping
			glEnable(GL_TEXTURE_2D);

			// Grab the texture index from the materialID index into our material list
			int textureID = pModel.getMaterials(pObject.getMaterialID()).getTexureId();

			// Bind the texture index that we got from the material textureID
			glBindTexture(GL_TEXTURE_2D, textureID);
		}
		else
		{
			// Turn off texture mapping
			glDisable(GL_TEXTURE_2D);
		}

		// Start drawing our model triangles
		glBegin(GL_TRIANGLES);

			// Go through all of the faces (polygons) of the object and draw them
			for(int j = 0; j < pObject.getNumFaces(); j++)
			{
				// Go through each vertex of the triangle and draw it.
				for(int whichVertex = 0; whichVertex < 3; whichVertex++)
				{
					// Get the index for the current point in the face list
					int index = pObject.getFace(j).getVertices(whichVertex);
								
					// Make sure there is texture coordinates for this (%99.9 likelyhood)
					if(pObject.getNumTexcoords() > 0) 
					{
						// Assign the texture coordinate to this vertex
						glTexCoord2f(pObject.getTexcoords(index).s, pObject.getTexcoords(index).t);
					}
					
					// Get the vertex that we are dealing with.  This code will change
					// a bunch when we doing our key frame animation in the next .MD3 tutorial.
					Vector3f vPoint1 = new Vector3f(pObject.getVertices(index));

					// Render the current vertex
					glVertex3f(vPoint1.x, vPoint1.y, vPoint1.z);
				}
			}

		// Stop drawing polygons
		glEnd();
	}
}


	
}
