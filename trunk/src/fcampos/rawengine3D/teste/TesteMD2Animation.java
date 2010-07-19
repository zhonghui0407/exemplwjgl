package fcampos.rawengine3D.teste;



import fcampos.rawengine3D.input.*;
import fcampos.rawengine3D.model.*;
import fcampos.rawengine3D.gamecore.*;
import fcampos.rawengine3D.loader.*;
import fcampos.rawengine3D.MathUtil.*;


import java.io.*;

import org.lwjgl.Sys;
import org.lwjgl.input.*;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.util.glu.GLU.*;

/**
 *
 * @author Fabio
 */
public class TesteMD2Animation extends GameCore {

    public static void main(String[] args)
    {
        new TesteMD2Animation().run();
    }
    
    
    //private static String FILE_NAME =  "tris.md2";							// This is the 3D file we will load.
    //private static String TEXTURE_NAME = "hobgoblin.jpg";
    
   private static String FILE_NAME =  "models/Ogros.md2";							// This is the 3D file we will load.
    private static String TEXTURE_NAME = "models/igdosh.jpg";
    
    //private static String FILE_NAME =  "modelpack19/hang8/hang8.md2";							// This is the 3D file we will load.
    //private static String TEXTURE_NAME = "modelpack19/hang8/hang8.png";
	public static final float kAnimationSpeed	=	5.0f;
    
    public GameAction moveLeft;
    public GameAction moveRight;
    public GameAction moveUp;
    public GameAction moveDown;
    public GameAction zoomIn;
    public GameAction zoomOut;
    public GameAction exit;
    public GameAction fullScreen;
    public GameAction drawMode;
    public GameAction modTex;
    public GameAction debug;
    
    int   g_ViewMode	  = GL_TRIANGLES;					// We want the default drawing mode to be normal
    boolean  g_bLighting     = true;							// Turn lighting on initially
    float g_RotateX		  = 0.0f;							// This is the current value at which the model is rotated
    float g_RotationSpeed = 0.0f;							// This is the speed that our model rotates.  (-speed rotates left)
    private int modo = GL_MODULATE;
    
    
    public InputManager inputManager;
   
    
 // This will store our 3ds scene that we will pass into our octree
    public T3dModel g_World = new T3dModel();
    public TMD2Loader g_LoadMd2 = new TMD2Loader();
    
 // This tells us if we want to display the yellow debug lines for our nodes (Space Bar)
    boolean g_bDisplayNodes = false;
    
    
    @Override
    public void init() throws IOException
    {
        super.init();
        
        screen.setTitle("MD2 Loader");
        
        inputManager = new InputManager();
                
        createGameActions();
                   
               
        
       
     // Here, we turn on a lighting and enable lighting.  We don't need to
    	// set anything else for lighting because we will just take the defaults.
    	// We also want color, so we turn that on
     
    	    	// Habilita Z-Buffer
    	glEnable(GL_DEPTH_TEST);
    	glEnable(GL_LIGHT0);								// Turn on a light with defaults set
    	glEnable(GL_LIGHTING);								// Turn on lighting
    	glEnable(GL_COLOR_MATERIAL);						// Allow color

    	// To make our model render somewhat faster, we do some front back culling.
    	// It seems that Quake2 orders their polygons clock-wise.
    	// Seleciona o modo de aplicação da textura
    	glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, modo);
    	
    	glEnable(GL_TEXTURE_2D);     
        g_LoadMd2.importMD2(g_World, FILE_NAME, TEXTURE_NAME);
       // g_LoadMd2.importMD2(g_World, "models/Weapon.md2", "models/Weapon.jpg");
       // g_LoadMd2.importMD2(g_World, "modelsd2/model8/throne.md2", "modelsd2/model8/throne.png");
    }
    
 
////////////*** NEW *** ////////// *** NEW *** ///////////// *** NEW *** ////////////////////

///////////////////////////////// RETURN CURRENT TIME \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
/////
/////	This returns time t for the interpolation between the current and next key frame
/////
///////////////////////////////// RETURN CURRENT TIME \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
     static float elapsedTime   = 0.0f;
	  static float lastTime	  = 0.0f;
private float returnCurrentTime(T3dModel pModel, int nextFrame)
{
	
	// This function is very similar to finding the frames per second.
	// Instead of checking when we reach a second, we check if we reach
	// 1 second / our animation speed. (1000 ms / kAnimationSpeed).
	// That's how we know when we need to switch to the next key frame.
	// In the process, we get the t value for how we are at to going to the
	// next animation key frame.  We use time to do the interpolation, that way
	// it runs the same speed on any persons computer, regardless of their specs.
	// It might look chopier on a junky computer, but the key frames still be
	// changing the same time as the other persons, it will just be not as smooth
	// of a transition between each frame.  The more frames per second we get, the
	// smoother the animation will be.

	// Get the current time in milliseconds
	float time = (float)Sys.getTime();
	//System.out.println("time : " + time);
	// Find the time that has elapsed since the last time that was stored
	elapsedTime = time - lastTime;
	
	//System.out.println("elapsedTime : " + elapsedTime);
	//System.out.println("last : " + lastTime);
	

	// To find the current t we divide the elapsed time by the ratio of 1 second / our anim speed.
	// Since we aren't using 1 second as our t = 1, we need to divide the speed by 1000
	// milliseconds to get our new ratio, which is a 5th of a second.
	float t = elapsedTime / (1000.0f / kAnimationSpeed);

	// If our elapsed time goes over a 5th of a second, we start over and go to the next key frame
	if (elapsedTime >= (1000.0f / kAnimationSpeed) )
	{
		// Set our current frame to the next key frame (which could be the start of the anim)
		pModel.setCurrentFrame(nextFrame);
		//System.out.println("current: "+pModel.getCurrentFrame());

		// Set our last time to the current time just like we would when getting our FPS.
		lastTime = time;
	}
	//System.out.println("t: "+t);
	// Return the time t so we can plug this into our interpolation.
	return t;
}

       
 
    protected void update(float elapsedTime)
    {
    	
    	
    	checkSystemInput();
    		
    	
    	  	

        if(!isPaused())
        {
            checkGameInput();
                        
        }
        
        
        
    }

        public void checkSystemInput()
        {
            
            if (exit.isPressed())
            {
                stop();
            }
            
        }

        public void checkGameInput()
        {
          
        	if(moveUp.isPressed())
        	{
        		// To cycle through the animations, we just increase the model's current animation
        		// by 1.  You'll notice that we also mod this result by the total number of
        		// animations in our model, to make sure we go back to the beginning once we reach
        		// the end of our animation list.  

        		// Increase the current animation and mod it by the max animations
        		g_World.setCurrentAnim((g_World.getCurrentAnim()+ 1) % (g_World.getAnimations().size()));
        		//System.out.println("numero de animacoes:  "+g_World.getPAnimations().size());
        		// Set the current frame to be the starting frame of the new animation
        		g_World.setCurrentFrame(g_World.getAnimations(g_World.getCurrentAnim()).getStartFrame());
        		
        		screen.setTitle("Animation: " + g_World.getAnimations(g_World.getCurrentAnim()).getAnimName());
        	}
        	     	
            if (moveLeft.isPressed())
            {
            	g_RotationSpeed -= 0.001f;	
            	
            }

            if (moveRight.isPressed())
            {
            	
            	g_RotationSpeed += 0.001f;	
            }
           
         
            if(modTex.isPressed())
            {
            	if(modo == GL_REPLACE) 
            		{
            			modo = GL_MODULATE; 
            		}
            		else 
            			{
            				modo = GL_REPLACE;
            			}
				// Ajusta o modo de aplicação da textura
				glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, modo);
            }
            if (drawMode.isPressed())
        	{
        		
            	if(g_ViewMode == GL_TRIANGLES) {				// We our drawing mode is at triangles
        			g_ViewMode = GL_LINE_STRIP;					// Go to line stips
        		} else {
        			g_ViewMode = GL_TRIANGLES;					// Go to triangles
        		}
        	}
           
            if (fullScreen.isPressed())
            {
            	setFullScreen(!isFullScreen());
            	            	
            }
            
            if(debug.isPressed())
            {
            	g_bDisplayNodes = !g_bDisplayNodes;
            }
            
           
            
            
        }
        
///////////////////////////////// ANIMATE MD2 MODEL \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
    /////
    /////	This draws and animates the .md2 model by interpoloated key frame animation
    /////
    ///////////////////////////////// ANIMATE MD2 MODEL \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*

    private void animateMD2Model(T3dModel pModel)
    {
    	// Now comes the juice of our tutorial.  Fear not, this is actually very intuitive
    	// if you drool over it for a while (stay away from the keyboard though...).
    	// What's going on here is, we are getting our current animation that we are
    	// on, finding the current frame of that animation that we are on, then interpolating
    	// between that frame and the next frame.  To make a smooth constant animation when
    	// we get to the end frame, we interpolate between the last frame of the animation 
    	// and the first frame of the animation.  That way, if we are doing the running 
    	// animation let's say, when the last frame of the running animation is hit, we don't
    	// have a huge jerk when going back to the first frame of that animation.  Remember,
    	// because we have the texture and face information stored in the first frame of our
    	// animation, we need to reference back to this frame every time when drawing the
    	// model.  The only thing the other frames store is the vertices, but no information
    	// about them.
    	
    	// Make sure we have valid objects just in case. (size() is in the vector class)
    	if(pModel.getObject().size() <= 0) return;

    	// Here we grab the current animation that we are on from our model's animation list
    	TAnimationInfo pAnim = pModel.getAnimations(pModel.getCurrentAnim());

    	// This gives us the current frame we are on.  We mod the current frame plus
    	// 1 by the current animations end frame to make sure the next frame is valid.
    	// If the next frame is past our end frame, then we go back to zero.  We check this next.
    	int nextFrame = (pModel.getCurrentFrame() + 1) % pAnim.getEndFrame();

    	// If the next frame is zero, that means that we need to start the animation over.
    	// To do this, we set nextFrame to the starting frame of this animation.
    	if(nextFrame == 0) 
    		nextFrame =  pAnim.getStartFrame();

    	// Get the current key frame we are on
    	T3dObject pFrame =		 pModel.getObject(pModel.getCurrentFrame());

    	// Get the next key frame we are interpolating too
    	T3dObject pNextFrame =  pModel.getObject(nextFrame);

    	// Get the first key frame so we have an address to the texture and face information
    	T3dObject pFirstFrame = pModel.getObject(0);

    	// Next, we want to get the current time that we are interpolating by.  Remember,
    	// if t = 0 then we are at the beginning of the animation, where if t = 1 we are at the end.
    	// Anyhing from 0 to 1 can be thought of as a percentage from 0 to 100 percent complete.
    	float t = returnCurrentTime(pModel, nextFrame);

    	// Start rendering lines or triangles, depending on our current rendering mode (Lft Mouse Btn)
    	glBegin(g_ViewMode);

    		// Go through all of the faces (polygons) of the current frame and draw them
    		for(int j = 0; j < pFirstFrame.getNumFaces(); j++)
    		{
    			// Go through each corner of the triangle and draw it.
    			for(int whichVertex = 0; whichVertex < 3; whichVertex++)
    			{
    				// Get the index for each point of the face
    				int vertIndex = pFirstFrame.getFace(j).getVertices(whichVertex);

    				// Get the index for each texture coordinate for this face
    				int texIndex  = pFirstFrame.getFace(j).getTexCoords(whichVertex);
    						
    				// Make sure there was a UVW map applied to the object.  Notice that
    				// we use the first frame to check if we have texture coordinates because
    				// none of the other frames hold this information, just the first by design.
    				if(pFirstFrame.getNumTexcoords() > 0) 
    				{
    					// Pass in the texture coordinate for this vertex
    					
    					glTexCoord2f(pFirstFrame.getTexcoords(texIndex).s, pFirstFrame.getTexcoords(texIndex).t);
    				}

    				// Now we get to the interpolation part! (*Bites his nails*)
    				// Below, we first store the vertex we are working on for the current
    				// frame and the frame we are interpolating too.  Next, we use the
    				// linear interpolation equation to smoothly transition from one
    				// key frame to the next.
    				
    				// Store the current and next frame's vertex
    				Vector3f vPoint1 = new Vector3f(pFrame.getVertices(vertIndex));
    				Vector3f vPoint2 = new Vector3f(pNextFrame.getVertices(vertIndex));

    				// By using the equation: p(t) = p0 + t(p1 - p0), with a time t
    				// passed in, we create a new vertex that is closer to the next key frame.
    				glVertex3f(vPoint1.x + t * (vPoint2.x - vPoint1.x), // Find the interpolated X
    						   vPoint1.y + t * (vPoint2.y - vPoint1.y), // Find the interpolated Y
    						   vPoint1.z + t * (vPoint2.z - vPoint1.z));// Find the interpolated Z
    			}
    		}

    	// Stop rendering the triangles
    	glEnd();	
    }

        public void render() 
        {
        	glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);	// Clear The Screen And The Depth Buffer
        	glLoadIdentity();									// Reset The matrix

        	// Give OpenGL our position,	then view,		then up vector
        	gluLookAt(		0, 1.5f, 100,		0, 0.5f, 0,			0, 1, 0);

        	glRotatef(g_RotateX, 0, 1.0f, 0);					// Rotate the object around the Y-Axis
        	g_RotateX += g_RotationSpeed;						// Increase the speed of rotation if any


        //////////// *** NEW *** ////////// *** NEW *** ///////////// *** NEW *** ////////////////////

        	// This is where we call our animation function to draw and animate our character.
        	// You can pass in any model into here and it will draw and animate it.  Of course,
        	// it would be a good idea to stick this function in your model class.

        	animateMD2Model(g_World);
        	
        	
        
        	// Render the cubed nodes to visualize the octree (in wire frame mode)
        	if( g_bDisplayNodes ){
        		//TOctree.g_Debug.renderDebugLines();
        		for(int j=0; j < g_World.getObject().size(); j++)
        		{
        			g_World.getObject(j).drawBoundingBox();
        		}
        	}
        }
             
      public void createGameActions()
        {
            moveLeft = new GameAction("moveLeft");
            moveRight = new GameAction("moveRight");
            moveUp = new GameAction("moveUp", GameAction.DETECT_INITIAL_PRESS_ONLY);
            moveDown = new GameAction("moveDown");
            zoomIn = new GameAction("zoomIn");
            zoomOut = new GameAction("zoomIn");
            exit = new GameAction("exit", GameAction.DETECT_INITIAL_PRESS_ONLY);
            fullScreen  = new GameAction("fullScreen", GameAction.DETECT_INITIAL_PRESS_ONLY);
            drawMode  = new GameAction("drawMode", GameAction.DETECT_INITIAL_PRESS_ONLY);
            modTex = new GameAction("modTex", GameAction.DETECT_INITIAL_PRESS_ONLY);
            debug = new GameAction("debug", GameAction.DETECT_INITIAL_PRESS_ONLY);
            
            inputManager.mapToKey(debug, Keyboard.KEY_SPACE);            
            inputManager.mapToKey(modTex, Keyboard.KEY_M);
            inputManager.mapToKey(exit, Keyboard.KEY_ESCAPE);
            inputManager.mapToKey(drawMode, Keyboard.KEY_T);           
            inputManager.mapToKey(zoomIn, Keyboard.KEY_HOME);
            inputManager.mapToKey(zoomOut, Keyboard.KEY_END) ;
            inputManager.mapToKey(moveLeft, Keyboard.KEY_LEFT);
            inputManager.mapToKey(moveRight, Keyboard.KEY_RIGHT);
            inputManager.mapToKey(moveUp, Keyboard.KEY_UP);
            inputManager.mapToKey(moveDown, Keyboard.KEY_DOWN);
            
            
            inputManager.mapToKey(fullScreen, Keyboard.KEY_F1);
            
            
            
            
           
        }
        

}

