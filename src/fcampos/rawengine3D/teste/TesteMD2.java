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
public class TesteMD2 extends GameCore {

    public static void main(String[] args)
    {
        new TesteMD2().run();
    }
    
    
   // private static String FILE_NAME =  "tris.md2";							// This is the 3D file we will load.
    //private static String TEXTURE_NAME = "hobgoblin.jpg";
    
   // private static String FILE_NAME =  "modelsd2/model8/body.md2";							// This is the 3D file we will load.
   // private static String TEXTURE_NAME = "modelsd2/model8/body.png";
    
    private static String FILE_NAME =  "models/Ogros.md2";						// This is the 3D file we will load.
    private static String TEXTURE_NAME = "models/igdosh.jpg";
     
    
    public GameAction moveLeft;
    public GameAction moveRight;
    public GameAction moveUp;
    public GameAction moveDown;
    public GameAction zoomIn;
    public GameAction zoomOut;
  
    public GameAction drawMode;
    public GameAction modTex;
    public GameAction debug;
    
    int   g_ViewMode	  = GL_TRIANGLES;					// We want the default drawing mode to be normal
    boolean  g_bLighting     = true;							// Turn lighting on initially
    float g_RotateX		  = 0.0f;							// This is the current value at which the model is rotated
    float g_RotationSpeed = 0.05f;							// This is the speed that our model rotates.  (-speed rotates left)
    private int modo = GL_MODULATE;
    
  
    
 // This will store our 3ds scene that we will pass into our octree
    public ModelMD2 g_World = new ModelMD2(TEXTURE_NAME);
   
    
 // This tells us if we want to display the yellow debug lines for our nodes (Space Bar)
    boolean g_bDisplayNodes = false;
    
    
    @Override
    public void init() throws IOException
    {
        super.init();
        
        screen.setTitle("MD2 Loader");
        
                   
        createGameActions();
                   
        
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);		
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();		
		gluPerspective(45.0f, 800 / 600, 1.0f, 2000.0f);
		glMatrixMode(GL_MODELVIEW);
		glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);
        
        
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
    	glEnable(GL_CULL_FACE);								// Turn culling on
    	glCullFace(GL_FRONT);		
    	glEnable(GL_TEXTURE_2D);     
    	g_World.load(FILE_NAME);
        //g_LoadMd2.importMD2(g_World, "modelsd2/model8/head.md2", "modelsd2/model8/head.png");
       // g_LoadMd2.importMD2(g_World, "modelsd2/model8/throne.md2", "modelsd2/model8/throne.png");
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

        public void render() 
        {
        	glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);	// Clear The Screen And The Depth Buffer
        	glLoadIdentity();									// Reset The matrix

        //////////// *** NEW *** ////////// *** NEW *** ///////////// *** NEW *** ////////////////////

        	// Give OpenGL our position,	then view,		then up vector
        	gluLookAt(		0, 1.5f, 100,		0, .5f, 0,			0, 1, 0);
        	
        	// We want the model to rotate around the axis so we give it a rotation
        	// value, then increase/decrease it. You can rotate right of left with the arrow keys.
        	
        	glRotatef(g_RotateX, 0, 1.0f, 0);						// Rotate the object around the Y-Axis
        	g_RotateX += g_RotationSpeed;							// Increase the speed of rotation

        	// Make sure we have valid objects just in case. (size() is in the vector class)
        	if(g_World.getObject().size() <= 0) return;
        	
        	for (int i=0; i < g_World.getObject().size(); i++)
        	{
        	// Get the current object that we are displaying
        	Object3d pObject = g_World.getObject(i);
        	
        	glBindTexture(GL_TEXTURE_2D,pObject.getMaterialID());
        	
        	
        	// Render lines or normal triangles mode, depending on the global variable
        	glBegin(g_ViewMode);

        		// Go through all of the faces (polygons) of the object and draw them
        		for(int j = 0; j < pObject.getNumFaces(); j++)
        		{
        			// Go through each corner of the triangle and draw it.
        			for(int whichVertex = 0; whichVertex < 3; whichVertex++)
        			{
        				// Get the index for each point in the face
        				int index = pObject.getFace(j).getVertices(whichVertex);

        				// Get the index for each texture coord in the face
        				int index2 = pObject.getFace(j).getTexCoords(whichVertex);
        			
        				// Give OpenGL the normal for this vertex.  Notice that we put a 
        				// - sign in front.  It appears that because of the ordering of Quake2's
        				// polygons, we need to invert the normal
        				//glNormal3f(-pObject.getNormal(index).x, -pObject.getNormal(index).y, -pObject.getNormal(index).z);
        					
        				// Make sure there was a UVW map applied to the object or else it won't have tex coords.
        				if(pObject.getNumTexcoords() > 0) 
        				{
        					glTexCoord2f(pObject.getTexcoords(index2).s, pObject.getTexcoords(index2).t);
        				}
        				
        				// Pass in the current vertex of the object (Corner of current face)
        				glVertex3f(pObject.getVertices(index).x, pObject.getVertices(index).y, pObject.getVertices(index).z);
        			}
        		}

        	glEnd(); 
        }
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
    	  super.createGameActions();
            moveLeft = new GameAction("moveLeft", GameAction.NORMAL, Keyboard.KEY_LEFT);
            moveRight = new GameAction("moveRight", GameAction.NORMAL, Keyboard.KEY_RIGHT);
            moveUp = new GameAction("moveUp", GameAction.DETECT_INITIAL_PRESS_ONLY, Keyboard.KEY_UP);
            moveDown = new GameAction("moveDown", GameAction.NORMAL, Keyboard.KEY_DOWN);
            zoomIn = new GameAction("zoomIn", GameAction.NORMAL, Keyboard.KEY_HOME);
            zoomOut = new GameAction("zoomOut", GameAction.NORMAL, Keyboard.KEY_END);
           
           
            drawMode  = new GameAction("drawMode", GameAction.DETECT_INITIAL_PRESS_ONLY, Keyboard.KEY_T);
            modTex = new GameAction("modTex", GameAction.DETECT_INITIAL_PRESS_ONLY, Keyboard.KEY_M);
            debug = new GameAction("debug", GameAction.DETECT_INITIAL_PRESS_ONLY, Keyboard.KEY_SPACE);
  
        }


}