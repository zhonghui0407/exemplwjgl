package fcampos.rawengine3D.teste;


import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.ARBMultitexture.*;
import static org.lwjgl.util.glu.GLU.gluPerspective;

import java.io.BufferedReader;
import java.io.IOException;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import fcampos.rawengine3D.MathUtil.Vector3f;
import fcampos.rawengine3D.bsp.quake3.Quake3BSP;
import fcampos.rawengine3D.fps.FPSCounter;
import fcampos.rawengine3D.gamecore.GameCore;
import fcampos.rawengine3D.input.Camera;
import fcampos.rawengine3D.input.GameAction;
import fcampos.rawengine3D.io.TextFile;


public class TesteBSP extends GameCore {

    public static void main(String[] args)
    {
        new TesteBSP().run();
    }
    
   
     
    // This is how fast our camera moves
    private float SPEED	=300.0f;
     
    
    public GameAction moveLeft;
    public GameAction moveRight;
    public GameAction moveUp;
    public GameAction moveDown;
    public GameAction drawMode;
    public GameAction modTex;
  
    
    boolean  g_bLighting     = true;							// Turn lighting on initially
     
    // This is the speed that our model rotates.  (-speed rotates left)
    private int modo = GL_MODULATE;
 
 // This will store our 3ds scene that we will pass into our octree
    public Quake3BSP level = new Quake3BSP();
 
    private Camera camera;
    
    @Override
    public void init() throws IOException
    {
        super.init();
        
        glMatrixMode(GL_PROJECTION);						// Select The Projection Matrix
    	glLoadIdentity();									// Reset The Projection Matrix

    														// Calculate The Aspect Ratio Of The Window
    				  // FOV		// Ratio				//  The farthest distance before it stops drawing)
    	gluPerspective(70.0f, screen.getWidth()/screen.getHeight(), 10.0f , 4000.0f);

    	glMatrixMode(GL_MODELVIEW);							// Select The Modelview Matrix
    	glLoadIdentity();									// Reset The Modelview Matrix
        
        screen.setTitle("BSP Loader");
        
     // Create the camera with mouse look enabled.
		camera = new Camera(true);
        
        BufferedReader reader = TextFile.openFile("Config.ini");
        String strLine = null;
        String nameLevel = null;
    	String gammaFactor = null;
        while((strLine = reader.readLine()) != null)
		{
        	
        	if(strLine.contains("[Level]"))
        	{
        		nameLevel = strLine.substring(8);
        	}
        	
        	if(strLine.contains("[Gamma]"))
        	{
        		gammaFactor = strLine.substring(8);
        	}
        	
        	
		}
        level.loadBSP(nameLevel, gammaFactor);
                   
        createGameActions();
                   
     // Position the camera to the starting point since we have
    	// not read in the entities yet, which gives the starting points.
    	camera.setPosition( 80, 350, 16,	80, 350, 17,	0, 1, 0);

    	
    	
    	
    /////// * /////////// * /////////// * NEW * /////// * /////////// * /////////// *

    	// Turn on depth testing and texture mapping
    	glEnable(GL_DEPTH_TEST);	
    	glEnable(GL_TEXTURE_2D);
    	// Enable front face culling, since that's what Quake3 does
    	glCullFace(GL_FRONT);
     	glEnable(GL_CULL_FACE);
    	// To make our model render somewhat faster, we do some front back culling.
    	// It seems that Quake2 orders their polygons clock-wise.
    	// Seleciona o modo de aplicação da textura
    	glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, modo);
    	    	   
        Mouse.setGrabbed(true);
      
    }
    

    public void update(float elapsedTime)
    {
    	checkSystemInput();
    	
    	    	
        if(!isPaused())
        {
        	if(!Mouse.isGrabbed())
        	{
        		Mouse.setGrabbed(true);
        	}
        	camera.update(); 
        	checkGameInput(elapsedTime);
                     
        }else{
        	Mouse.setGrabbed(false);
        }
    }

 
        public void checkGameInput(float elapsedTime)
        {
        	
        	super.checkGameInput(); // Checamos se as teclas foram pressionadas ou não.
        	
        	// Once we have the frame interval, we find the current speed
        	float speed = (SPEED * elapsedTime);
        	
        	// Before we move our camera we want to store the old position.  We then use 
        	// this data to test collision detection.
        	Vector3f oldPosition = new Vector3f(camera.getPosition());
        	
        	if (moveLeft.isPressed())
            {
            	camera.strafe(-speed );
            	
            }

            if (moveRight.isPressed())
            {
            	
            	camera.strafe(speed);	
            }
           
            if (moveUp.isPressed())
            {
            	camera.move(speed );
            	
            }
            if (moveDown.isPressed())
            {
            	
            	camera.move(-speed);
            }
        	
         
            if(modTex.isPressed())
            {
            	level.setHasTextures(!level.isTextures());
            	if(!level.isTextures())								// If we don't want lightmaps
        		{	
        			glActiveTextureARB(GL_TEXTURE0_ARB);		// Turn the second texture off
                    glDisable(GL_TEXTURE_2D);
        		}else{
        			glEnable(GL_TEXTURE_2D);
        		}
            	
            }
            if (drawMode.isPressed())
        	{
            	level.setHasTextures(!level.isTextures());
            	level.setHasLightmaps(false);
				// Change the rendering mode to and from lines or triangles
				if(level.isTextures()) 				
				{
					// Render the triangles in fill mode		
					glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);	
				}
				else 
				{
					// Render the triangles in wire frame mode
					glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);	
				}
            	
        	}
            
            // Now that we moved, let's get the current position and test our movement
        	// vector against the level data to see if there is a collision.
        	Vector3f currentPosition = new Vector3f(camera.getPosition());
        	
        	// Here we call our function TraceSphere() to check our movement vector (last
        	// and current position) against the world.  We pass in a radius for our sphere
        	// of 25.  If there is anything in the range of our sphere, then we collide.
        	Vector3f newPosition = level.traceSphere(oldPosition, currentPosition, 25.0f);
        	
        	
        	// Set the new position that was returned from our trace function
        	camera.setPosition(newPosition);

        }

        public void render() 
        {
        	
        	
        	glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);	// Clear The Screen And The Depth Buffer
        	glLoadIdentity();									// Reset The matrix

        	// Give OpenGL our camera coordinates to look at
        	camera.look();


        	// Since we are using frustum culling to only draw the visible BSP leafs,
        	// we need to calculate the frustum every frame.  This needs to happen
        	// right after we position our camera.  Now the frustum planes can be defined.
        	GameCore.gFrustum.calculateFrustum();
        	
        	// Easy as pie - just call our render function.  We pass in the camera
        	// because in later tutorials we will need it's position when we start
        	// dealing with the BSP nodes and leafs.
        	level.renderLevel(camera.getPosition());
        	
        	screen.setTitle("FPS: " + FPSCounter.get() + " VisibleFaces: " + Quake3BSP.visibleFaces); 
        	
        	
        }
 

             
        public void createGameActions()
        {
    	  super.createGameActions();
            moveLeft = new GameAction("moveLeft", GameAction.NORMAL, Keyboard.KEY_LEFT);
            moveRight = new GameAction("moveRight", GameAction.NORMAL, Keyboard.KEY_RIGHT);
            moveUp = new GameAction("moveUp", GameAction.NORMAL, Keyboard.KEY_UP);
            moveDown = new GameAction("moveDown", GameAction.NORMAL, Keyboard.KEY_DOWN);
                     
            drawMode  = new GameAction("drawMode", GameAction.DETECT_INITIAL_PRESS_ONLY, Keyboard.KEY_T);
            modTex = new GameAction("modTex", GameAction.DETECT_INITIAL_PRESS_ONLY, Keyboard.KEY_M);
             
        }
}
        


