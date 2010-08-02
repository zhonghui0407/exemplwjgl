package fcampos.rawengine3D.teste;



import fcampos.rawengine3D.input.*;
import fcampos.rawengine3D.model.*;
import fcampos.rawengine3D.resource.Conversion;
import fcampos.rawengine3D.gamecore.*;
import java.io.*;
import java.nio.FloatBuffer;

import org.lwjgl.input.*;

import static org.lwjgl.opengl.GL11.*;

/**
 *
 * @author Fabio
 */
public class TesteSTL extends GameCore {

    public static void main(String[] args)
    {
        new TesteSTL().run();
    }
    
   
   	// This is the 3D file we will load.
    private static String MODEL_NAME = "c:\\Matriz_Peugeot_Dig_FASE3.stl";
        
    
    public GameAction moveLeft;
    public GameAction moveRight;
    public GameAction moveUp;
    public GameAction moveDown;
    
    
    public GameAction drawMode;
    public GameAction modTex;
    public GameAction debug;
 
    float SPEED	=500.0f;
    
    int   g_ViewMode	  = GL_TRIANGLES;					// We want the default drawing mode to be normal
    boolean  g_bLighting     = true;							// Turn lighting on initially
    float g_RotateX		  = 0.0f;							// This is the current value at which the model is rotated
    float g_RotationSpeed = 0.00005f;	
    float g_TranslationZ  = -120.0f;		// This stores our distance away from the model
    boolean  g_RenderMode    =    true;		// This tells us if we are in wire frame mode or not
    
    // This is the speed that our model rotates.  (-speed rotates left)
    private int modo = GL_REPLACE;
    
   
 // This will store our 3ds scene that we will pass into our octree
    public ModelSTL stl = new ModelSTL();
    
    private static final float LOW = 0.5f;
    
    private float luzAmb1[] = { 0.5f, 0.5f, 0.5f, 1f };	// luz ambiente
    private float luzDif1[] = { LOW, LOW, LOW, 1.0f };	// luz difusa
    private float luzEsp1[] = { 1.0f, 1.0f, 1.0f, 1.0f };	// luz especular
    private float posLuz1[] = { 50, 30, -2500f, 1f };	// posição da fonte de luz
    private float posLuz2[] = { -50, 30, -2500f, 1f };	// posição da fonte de luz
       
    private FloatBuffer posLuz1F;	// posição da fonte de luz
    private FloatBuffer posLuz2F;	// posição da fonte de luz

    // This tells us if we want to display the yellow debug lines for our nodes (Space Bar)
    boolean g_bDisplayNodes = false;
    private Camera camera;
    
    @Override
    public void init() throws IOException
    {
        super.init();
        
        screen.setTitle("STL Loader");
        
        // Create the camera with mouse look enabled.
		camera = new Camera(true);

		// Posiciona e orienta observador
		
		camera.setPosition(-30f, 20f, -1000f,	-30, 20f, 0,	0, 1, 0);
		camera.setFixedAxis(Camera.Y_AXIS);
                   
        createGameActions();
                   
        stl.load(MODEL_NAME);
 
    	glEnable(GL_LIGHTING);								// Turn on lighting
    	glEnable(GL_COLOR_MATERIAL);						// Allow color
    	
    	 glEnable(GL_LIGHT0);								// Turn on a light with defaults set
         glEnable(GL_LIGHT1);								// Turn on a light with defaults set
         
    	// Ajusta iluminação
    	glLight( GL_LIGHT0, GL_AMBIENT,  Conversion.allocFloats(luzAmb1)); 
    	glLight( GL_LIGHT0, GL_DIFFUSE,  Conversion.allocFloats(luzDif1));
    	glLight( GL_LIGHT0, GL_SPECULAR, Conversion.allocFloats(luzEsp1));
    	
            
        //glEnable(GL_CULL_FACE);								// Turn back face culling on
    	//glCullFace(GL_BACK);								// Quake3 uses front face culling apparently

    	//glEnable(GL_TEXTURE_2D);							// Enables Texture Mapping
    	glEnable(GL_DEPTH_TEST);						// Enables Depth Testing
    	
    	 posLuz1F = Conversion.allocFloats(posLuz1);
    	 posLuz2F = Conversion.allocFloats(posLuz2);
    

    	// To make our model render somewhat faster, we do some front back culling.
    	// It seems that Quake2 orders their polygons clock-wise.
    	// Seleciona o modo de aplicação da textura
    	glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, modo);
    	//glEnable(GL_CULL_FACE);								// Turn culling on
    	//glCullFace(GL_FRONT);		
    	   
        Mouse.setGrabbed(true);
        
        glLight(GL_LIGHT0, GL_POSITION, posLuz1F); 
     	 glLight(GL_LIGHT1, GL_POSITION, posLuz2F); 

      
    }
    

    public void update(float elapsedTime)
    {
 	
    	checkSystemInput();
  
      	
        if(!isPaused())
        {
            checkGameInput(elapsedTime);
            camera.update();
                        
        }
        
        
        
    }

 
        public void checkGameInput(float elapsedTime)
        {
        	
        
        	//System.out.println(FPSCounter.frameInterval);
        	super.checkGameInput(); // Checamos se as teclas foram pressionadas ou não.
        	
        	// Once we have the frame interval, we find the current speed
        	float speed = (SPEED * elapsedTime);
        	
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

        	camera.look();	
        	// Give OpenGL our position,			then view,			then up vector
        	//gluLookAt(	0, 5.5f, g_TranslationZ,	0, 5.5f, 0,			0, 1, 0);
        	
        	// We want the model to rotate around the axis so we give it a rotation
        	// value, then increase/decrease it. You can rotate right or left with the arrow keys.

        	glRotatef(g_RotateX, 0, 1.0f, 0);			// Rotate the object around the Y-Axis
        	g_RotateX += g_RotationSpeed;				// Increase the speed of rotation

        	// Now comes the moment we have all been waiting for!  Below we draw our character.
        	glColor3f(.5f, .5f, .5f);
        	stl.draw();	
        	       	
        	
        	
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