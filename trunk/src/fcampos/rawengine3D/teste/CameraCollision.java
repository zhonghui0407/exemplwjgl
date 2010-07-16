package fcampos.rawengine3D.teste;
import fcampos.rawengine3D.input.*;
import fcampos.rawengine3D.model.T3dModel;
import fcampos.rawengine3D.resource.*;
import fcampos.rawengine3D.gamecore.GameCore;
import fcampos.rawengine3D.graficos.*;
import fcampos.rawengine3D.MathUtil.*;

import fcampos.rawengine3D.fps.*;

import java.io.*;
import java.nio.FloatBuffer;


import org.lwjgl.input.*;
import org.lwjgl.util.glu.Sphere;

import static org.lwjgl.opengl.GL11.*;

/**
 *
 * @author Fabio
 */
public class CameraCollision extends GameCore {

    public static void main(String[] args)
    {
        new CameraCollision().run();
    }
    
   
    
 

 // Gravity: -9.8 Meters Per Second Squared normally.
    private static final float GRAVITY	= -15f;
    
 // We need to have a gravity and jump acceleration for our simple physics
   
    final static float kJumpAcceleration = 4;
    
    
    
 // Our Global Friction.
    public float g_fFriction = 4f;

    // The Sine and Cosine Tables.
    // We create a table of all possible values because the trig functions
    // sin() and cos() are quite expensive, so, since we will only need 360
    // degree's of precision, we precalculate and store these values in an array.
    public float[] g_fSinTable = new float[360];
    public float[] g_fCosTable = new float[360];

    // The Time Elapsed since the last frame (in seconds).
    public float g_fTimeLapsed = 0.0f;
    
    public Vector3f vIntersectionPt;
    
    

    // A Quadric Object.
    Sphere pObj;

    // This is how fast our camera moves
    float SPEED	=5.0f;
    
 

    // Here we initialize our single Octree object.  This will hold all of our vertices
    TOctree g_Octree = new TOctree();;

        
 // This will store our 3ds scene that we will pass into our octree
    public T3dModel g_World = new T3dModel();
    
 // This tells us if we want to display the yellow debug lines for our nodes (Space Bar)
    boolean g_bDisplayNodes = false;

    

    // Variáveis para controle da projeção
       
    
    public GameAction moveLeft;
    public GameAction moveRight;
    public GameAction moveUp;
    public GameAction moveDown;
    public GameAction zoomIn;
    public GameAction zoomOut;
    public GameAction exit;
    public GameAction pause;
    public GameAction left;
    public GameAction enter;
    public GameAction debug;
    public GameAction fullScreen;
    public GameAction saveCamera;
    public GameAction drawMode;
    public GameAction fog;
    public GameAction right;
    public GameAction run;
    
    public InputManager inputManager;
    
      
 
      
   
    private static final float LOW = 0.5f;
       
       
    public boolean paused;
    
    private float luzAmb1[] = { 0.4f, 0.4f, 0.4f, 1f };	// luz ambiente
    private float luzDif1[] = { LOW, LOW, LOW, 1.0f };	// luz difusa
    private float luzEsp1[] = { 0.0f, 0.0f, 0.0f, 1.0f };	// luz especular
    private float spec[] = { 1.0f, 1.0f, 1.0f, 1.0f };	// luz especular
    private float posLuz1[] = { 0, 10, 17f, 1 };	// posição da fonte de luz
       
    private FloatBuffer posLuz1F;	// posição da fonte de luz
    
    private CameraQuaternion camera;
    
      
    public static Vector3f velocity = new Vector3f();
    
       
    
    public void LoadWorld()throws IOException
    {
    	// Here we load the world from a .3ds file
    	//g_World.carregaObjeto("arenaobj_Scene2.obj", true, false, g_World);
    	//g_World.carregaObjeto("arenaobj_10.obj", true, false, g_World);
    	
    	//g_World.carregaObjeto(g_World, "2.3ds");
    	    	
    	//g_World.carregaObjeto(g_World, "Jupiter2_CrashlandModel.3ds");
    	g_World.carregaObjeto(g_World, "Park.3ds");
    	//g_World.carregaObjeto(g_World, "collision_arena.3DS");
    	//g_World.carregaObjeto(g_World, "car.3ds");
    	
    	
    	
    	g_Octree.getSceneDimensions(g_World);
    	
    	
    	
    	int TotalTriangleCount = g_Octree.getSceneTriangleCount(g_World);
    	g_Octree.createNode(g_World, TotalTriangleCount, g_Octree.getCenter(), g_Octree.getWidth());
    	g_Octree.setDisplayListID( glGenLists(TOctree.g_EndNodeCount) );
    	g_Octree.createDisplayList(g_Octree, g_World, g_Octree.getDisplayListID());

    	// Hide our cursor since we are using first person camera mode
    	 	Mouse.setGrabbed(true);
    }
    
    
    
    
    @Override
    public void init() throws IOException
    {
        
    	super.init();
    	
    	// Create the camera with mouse look enabled.
		camera = new CameraQuaternion(true);

		// Posiciona e orienta observador
		
		camera.setPosition(2f, 3.3f, -5f,	0, 0, 0,	0, 1, 0);
		camera.setFixedAxis(CameraQuaternion.Y_AXIS);
		
    	
    	//camera.setPosition(-17f, 17f, 17f,	0, 0, 0,	0, 1, 0);
    	
    	//camera.setPosition(1f, 1f, -5f,	0,0, 0,	0, 1, 0);
    	
    	
    	float df=100.0f;
    	// We need to specify our camera's radius in the beginning, I chose 1.
    	camera.setRadius(1.5f);
    	
    	
              
     
       
          	
    	TOctree.g_Debug = g_Octree.new Debug();
    	// Turn lighting on initially
    	TOctree.g_bLighting     = true;	

    	// The current amount of end nodes in our tree (The nodes with vertices stored in them)
    	TOctree.g_EndNodeCount = 0;

    	// This stores the amount of nodes that are in the frustum
    	TOctree.g_TotalNodesDrawn = 0;

    	// The maximum amount of triangles per node.  If a node has equal or less 
    	// than this, stop subdividing and store the face indices in that node
    	TOctree.g_MaxTriangles = 800;

    	// The maximum amount of subdivisions allowed (Levels of subdivision)
    	TOctree.g_MaxSubdivisions = 4;

    	// The number of Nodes we've checked for collision.
    	TOctree.g_iNumNodesCollided = 0;

    	// Wheter the Object is Colliding with anything in the World or not.
    	TOctree.g_bObjectColliding = false;

    	// Wheter we test the whole world for collision or just the nodes we are in.
    	TOctree.g_bOctreeCollisionDetection = true;
    	
    	LoadWorld();
    	
           
        createGameActions();
        
        posLuz1F = Conversion.allocFloats(posLuz1);
        
     // Turn the color back to blue'ish purple and disable fog
		glClearColor(0.5f, 0.5f, 1.0f, 1.0f);
    	
    	// Ajusta iluminação
    	glLight( GL_LIGHT0, GL_AMBIENT,  Conversion.allocFloats(luzAmb1)); 
    	glLight( GL_LIGHT0, GL_DIFFUSE,  Conversion.allocFloats(luzDif1));
    	glLight( GL_LIGHT0, GL_SPECULAR, Conversion.allocFloats(luzEsp1));
    	
    	

    	// Habilita todas as fontes de luz
    	glEnable(GL_LIGHT0);
    	
    	glEnable(GL_LIGHTING);
    	
    	    	// Habilita Z-Buffer
    	glEnable(GL_DEPTH_TEST);
    	//glCullFace(GL_BACK);								// Don't draw the back sides of polygons
    	//glEnable(GL_CULL_FACE);	
    	
    	// Seleciona o modo de GL_COLOR_MATERIAL
    	//glColorMaterial(GL_FRONT, GL_DIFFUSE);
    	glEnable(GL_COLOR_MATERIAL);
    	glMaterial( GL_FRONT, GL_SPECULAR , Conversion.allocFloats(spec) );
    	glMaterialf( GL_FRONT, GL_SHININESS, df );
    	
    	
    	// Cor da neblina
    	float cor_neblina[] = {0.5f, 0.5f, 0.5f, 1.0f};
    	glFogi(GL_FOG_MODE, GL_EXP2);						// Assign the fog mode to EXP2 (realistic)
    	glFog(GL_FOG_COLOR, Conversion.allocFloats(cor_neblina));					// Set Fog Color
    	glFogf(GL_FOG_DENSITY, 0.07f);						// How Dense Will The Fog Be
    	glHint(GL_FOG_HINT, GL_DONT_CARE);					// The Fog's calculation accuracy
    	glFogf(GL_FOG_START, 0);							// Fog Start Depth
    	glFogf(GL_FOG_END, 50.0f);							// Fog End Depth

    	//glEnable(GL_FOG);									// This enables our OpenGL Fog

    
		
    	createGameActions();
        
              
    	
    	
    }
    
 


   

    public void update(float elapsedTime)
    {
    	checkSystemInput();
    	
    	    	
        if(!isPaused())
        {
        	
        	checkGameInput();
            camera.update();           
        }
    }

       

    protected void checkGameInput()
        {
        	super.checkGameInput(); // Checamos se as teclas foram pressionadas ou não.
        	
        	// Once we have the frame interval, we find the current speed
        	float speed = (float)(SPEED * FPSCounter.frameInterval);
        	
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
        	
        	if (run.isPressed())
        	{
        		speed = 10*speed;
        	}
        	
        
        	if (drawMode.isPressed())
        	{
        		TOctree.drawMode = !TOctree.drawMode;
        		if(TOctree.drawMode)
        		glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);	// Render the triangles in fill mode		
    		
    		else {
    			glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);	// Render the triangles in wire frame mode
    		}
        		g_Octree.createDisplayList(g_Octree, g_World, g_Octree.getDisplayListID());
        	}
        	
        	if (fullScreen.isPressed())
            {
            	setFullScreen(!isFullScreen());
            }
            
            if(enter.isPressed())
            {
            	TOctree.g_bOctreeCollisionDetection = !TOctree.g_bOctreeCollisionDetection;
            }
          
            if (left.isPressed())
            {
            	camera.strafe(-speed);
            	
            }

            if (right.isPressed())
            {
            	
            	camera.strafe(speed);	
            }
           
            if (zoomIn.isPressed())
            {
            	camera.move(+speed);
            	
            }
            if (zoomOut.isPressed())
            {
            	
            	camera.move(-speed);
            	
            }
            
            if(fog.isPressed())
            {
            	if(glIsEnabled(GL_FOG))
            	{
            		// Turn the color back to blue'ish purple and disable fog
					glClearColor(0.5f, 0.5f, 1.0f, 1.0f);
            		glDisable(GL_FOG);
            	}
            	else
            	{
            		// Set the background color to grey and enable fog
					glClearColor(0.5f, 0.5f, 0.5f, 1.0f);
            		glEnable(GL_FOG);
            	}
            }
            
              	
            
            if(debug.isPressed())
            {
            	g_bDisplayNodes = !g_bDisplayNodes;
            }
             	
            //if ( !TOctree.g_bObjectColliding )
        	//	velocity.y += -((GRAVITY * GRAVITY) * FPSCounter.frameInterval);
            
           // camera.getPosition().y += velocity.y * elapsedTime;
           // camera.getView().y += velocity.y * elapsedTime;
           camera.setPosition(VectorMath.add(camera.getPosition(),VectorMath.multiply(velocity, FPSCounter.frameInterval)));
            camera.setView(VectorMath.add(camera.getView(),VectorMath.multiply(velocity, FPSCounter.frameInterval)));
            
       
        	TOctree.g_bObjectColliding = false;

        	// Reset the Nodes collided to zero so we can start with a fresh count.
        	TOctree.g_iNumNodesCollided = 0;
        	
        	if (g_Octree.checkCameraCollision(g_Octree, g_World, camera))
        		velocity.y = 0;
        }

        protected void render() 
        {
        	

        	glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);	// Clear The Screen And The Depth Buffer

        	glLoadIdentity();// To calculate our collision detection with the camera, it just takes one function
        	// call from the client side.  We just pass in the vertices in the world that we
        	// want to check, and then the vertex count.  
        	// Each frame we calculate the new frustum.  In reality you only need to
        	// calculate the frustum when we move the camera.
        	//
        	
        	
        	
        	
        	
        	
        	camera.look();	
        	GameCore.gFrustum.calculateFrustum();
        	// Agora posiciona demais fontes de luz
        	glLight(GL_LIGHT0, GL_POSITION, posLuz1F); 
        	//gluLookAt(camera.getPosition()[0].x, camera.getPosition()[0].y, camera.getPosition()[0].z,
        	//		camera.getPosition()[1].x,	camera.getPosition()[1].y,	 camera.getPosition()[1].z,
        	//		camera.getPosition()[2].x, camera.getPosition()[2].y, camera.getPosition()[2].z);	

        	
        	
        	// Initialize the total node count that is being draw per frame
        	TOctree.g_TotalNodesDrawn = 0;

        	glPushMatrix();
        		// Here we draw the octree, starting with the root node and recursing down each node.
        		// This time, we pass in the root node and just the original world model.  You could
        		// just store the world in the root node and not have to keep the original data around.
        		// This is up to you.  I like this way better because it's easy, though it could be 
        		// more error prone.
        		g_Octree.drawOctree(g_Octree, g_World );
        	glPopMatrix();

        	// Render the cubed nodes to visualize the octree (in wire frame mode)
        	if( g_bDisplayNodes ){
        		//TOctree.g_Debug.renderDebugLines();
        		for(int i=0; i < g_World.getNumOfObjects(); i++)
        		{
        			g_World.getPObject(i).renderBoundingBox();
        		}
        	}
        	
        	// Update fps
    		
        	//screen.enterOrtho();
        	//draw.drawString(1,"QPS: " + FPSCounter.get(), 5, 5);  
        	
        	//screen.leaveOrtho();
        	//FPSCounter.get();
        	screen.setTitle("Triangles: " + TOctree.g_MaxTriangles + "  -Total Draw: " + TOctree.g_TotalNodesDrawn + "  -Subdivisions: " +  TOctree.g_MaxSubdivisions +
        			 "  -FPS: " + FPSCounter.get() + "  -Node Collisions: " + TOctree.g_iNumNodesCollided + "  -Object Colliding? " +
        				   TOctree.g_bObjectColliding ); 
        	
        	//System.out.println(FPSCounter.frameInterval);
        	
        }
            
        
       
      protected void createGameActions()
        {
    	  	super.createGameActions();
    	  
            moveLeft = new GameAction("moveLeft",GameAction.NORMAL, Keyboard.KEY_LEFT);
            moveRight = new GameAction("moveRight",GameAction.NORMAL, Keyboard.KEY_RIGHT);
            moveUp = new GameAction("moveUp",GameAction.NORMAL, Keyboard.KEY_UP);
            moveDown = new GameAction("moveDown",GameAction.NORMAL, Keyboard.KEY_DOWN);
            zoomIn = new GameAction("zoomIn",GameAction.NORMAL, Keyboard.KEY_W);
            zoomOut = new GameAction("zoomOut", GameAction.NORMAL, Keyboard.KEY_S);
            exit = new GameAction("exit", GameAction.DETECT_INITIAL_PRESS_ONLY, Keyboard.KEY_ESCAPE);
            pause = new GameAction("pause", GameAction.DETECT_INITIAL_PRESS_ONLY, Keyboard.KEY_P);
            run = new GameAction("run", GameAction.DETECT_INITIAL_PRESS_ONLY, Keyboard.KEY_LSHIFT);
            left = new GameAction("left");
            enter = new GameAction("enter", GameAction.DETECT_INITIAL_PRESS_ONLY);
            debug = new GameAction("debug", GameAction.DETECT_INITIAL_PRESS_ONLY);
            fullScreen  = new GameAction("fullScreen", GameAction.DETECT_INITIAL_PRESS_ONLY);
            saveCamera = new GameAction("saveCamera", GameAction.DETECT_INITIAL_PRESS_ONLY);
            drawMode  = new GameAction("drawMode", GameAction.DETECT_INITIAL_PRESS_ONLY);
            right = new GameAction("right");
            fog = new GameAction("fog", GameAction.DETECT_INITIAL_PRESS_ONLY);
            
            /*
            
            inputManager.mapToKey(exit, Keyboard.KEY_ESCAPE);
            inputManager.mapToKey(pause, Keyboard.KEY_P);
           
            inputManager.mapToKey(zoomIn, Keyboard.KEY_W);
            inputManager.mapToKey(zoomOut, Keyboard.KEY_S) ;
            inputManager.mapToKey(moveLeft, Keyboard.KEY_LEFT);
            inputManager.mapToKey(moveRight, Keyboard.KEY_RIGHT);
            inputManager.mapToKey(moveUp, Keyboard.KEY_UP);
            inputManager.mapToKey(moveDown, Keyboard.KEY_DOWN);
            
            
            inputManager.mapToKey(left, Keyboard.KEY_A);
            inputManager.mapToKey(enter, Keyboard.KEY_RETURN);
            inputManager.mapToKey(debug, Keyboard.KEY_SPACE);
            inputManager.mapToKey(fullScreen, Keyboard.KEY_F1);
            inputManager.mapToKey(saveCamera, Keyboard.KEY_F11);
            inputManager.mapToKey(drawMode, Keyboard.KEY_T);
            inputManager.mapToKey(right, Keyboard.KEY_D);
            inputManager.mapToKey(run,Keyboard.KEY_LSHIFT);
            inputManager.mapToKey(fog, Keyboard.KEY_F);
            */
            
           
        }
        
   
}


