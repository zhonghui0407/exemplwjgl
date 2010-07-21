package fcampos.rawengine3D.teste;



import fcampos.rawengine3D.input.*;
import fcampos.rawengine3D.model.*;
import fcampos.rawengine3D.gamecore.*;
import java.io.*;
import org.lwjgl.input.*;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.util.glu.GLU.*;

/**
 *
 * @author Fabio
 */
public class TesteMD3 extends GameCore {

    public static void main(String[] args)
    {
        new TesteMD3().run();
    }
    
   
    private static String MODEL_PATH =  "lara";							// This is the 3D file we will load.
    private static String MODEL_NAME = "lara";
    private static String GUN_NAME  =   "Railgun";
     
    
    public GameAction moveLeft;
    public GameAction moveRight;
    public GameAction moveUp;
    public GameAction moveDown;
  
    public GameAction drawMode;
    public GameAction modTex;
    public GameAction debug;
 
    
    int   g_ViewMode	  = GL_TRIANGLES;					// We want the default drawing mode to be normal
    boolean  g_bLighting     = true;							// Turn lighting on initially
    float g_RotateX		  = 0.0f;							// This is the current value at which the model is rotated
    float g_RotationSpeed = 0.0005f;	
    float g_TranslationZ  = -120.0f;		// This stores our distance away from the model
    boolean  g_RenderMode    =    true;		// This tells us if we are in wire frame mode or not
    
    // This is the speed that our model rotates.  (-speed rotates left)
    private int modo = GL_REPLACE;
    
   
 // This will store our 3ds scene that we will pass into our octree
    public ModelQuake3 g_World = new ModelQuake3();
        
 // This tells us if we want to display the yellow debug lines for our nodes (Space Bar)
    boolean g_bDisplayNodes = false;
    
    
    @Override
    public void init() throws IOException
    {
        super.init();
        
        screen.setTitle("MD3 Loader");
                   
        createGameActions();
                   
        g_World.loadModel(MODEL_PATH, MODEL_NAME);
        g_World.loadWeapon(MODEL_PATH, GUN_NAME);
                
        
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
    	//glEnable(GL_CULL_FACE);								// Turn culling on
    	//glCullFace(GL_FRONT);		
    	   
        
      
    }
    

    public void update(float elapsedTime)
    {
 	
    	checkSystemInput();
  
      	
        if(!isPaused())
        {
            checkGameInput();
                        
        }
        
        
        
    }

 
        public void checkGameInput()
        {
        	
        	          
        	if(moveUp.isPressed())
        	{
        		g_TranslationZ += .2f;
        	}
        	
        	if(moveDown.isPressed())
        	{
        		g_TranslationZ -= .2f;
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
            	g_RenderMode = !g_RenderMode;	// Change the rendering mode

				// Change the rendering mode to and from lines or triangles
				if(g_RenderMode) 				
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
                     
            if(debug.isPressed())
            {
            	g_bDisplayNodes = !g_bDisplayNodes;
            }
            
           
            
            
        }

        public void render() 
        {
        	glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);	// Clear The Screen And The Depth Buffer
        	glLoadIdentity();									// Reset The matrix


        //////////// *** NEW *** ////////// *** NEW *** ///////////// *** NEW *** ////////////////////

        	// As you can see below, to draw the model it's a piece of cake with our CModelMD3 class.
        	// You'll notice that we added a translation variable to our camera's z position.  This
        	// allows us to zoom in and zoom out without adding some camera code.

        	// Give OpenGL our position,			then view,			then up vector
        	gluLookAt(	0, 5.5f, g_TranslationZ,	0, 5.5f, 0,			0, 1, 0);
        	
        	// We want the model to rotate around the axis so we give it a rotation
        	// value, then increase/decrease it. You can rotate right or left with the arrow keys.

        	glRotatef(g_RotateX, 0, 1.0f, 0);			// Rotate the object around the Y-Axis
        	g_RotateX += g_RotationSpeed;				// Increase the speed of rotation

        	// Now comes the moment we have all been waiting for!  Below we draw our character.

        	g_World.drawModel();	
        	       	
        	
        	
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
            debug = new GameAction("debug", GameAction.DETECT_INITIAL_PRESS_ONLY, Keyboard.KEY_SPACE);
  
        }
}
        


