package fcampos.rawengine3D.teste;
import fcampos.rawengine3D.input.*;
import fcampos.rawengine3D.model.BoundingBox;
import fcampos.rawengine3D.model.Model3d;
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
import static org.lwjgl.util.glu.GLU.*;

/**
 *
 * @author Fabio
 */
public class Testesala extends GameCore {

    public static void main(String[] args)
    {
        new Testesala().run();
    }
    
    private CameraQuaternion camera;
    
 // 2 times PI.
    private static final float AR_2PI	= 6.28318530717958647692f;

    // PI divided by 180.
    //private static final float AR_PI_DIV_180 = 0.017453292519943296f;

    // 180 divided by PI ( Inverse of PI / 180 ).
   // private static final float AR_INV_PI_DIV_180 = 57.2957795130823229f;
    
 // Gravity: -9.8 Meters Per Second Squared normally.
    private static final float GRAVITY	= -9.8f;
    
    public Entity g_BallEntity = new Entity(0.0f, 5.0f, 0.0f, // Position.
    								 0.0f, 0.0f, 0.0f, // Velocity.
    								 -1f, // Min Velocity.
    								 6.0f, // Max Velocity.
    								 0.0f, // Angle.
    								 6.0f, // Acceleration.
    								 0.5f, // Radius
    								 120.0f // Turn Rate.
    								);	
    
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
    float SPEED	=1000.0f;
    
 

    // Here we initialize our single Octree object.  This will hold all of our vertices
    Octree octree = new Octree();;

    

    
    
 // This will store our 3ds scene that we will pass into our octree
    public Model3d g_World = new Model3d();;
    
 // This tells us if we want to display the yellow debug lines for our nodes (Space Bar)
    boolean g_bDisplayNodes = false;

    /////// * /////////// * /////////// * NEW * /////// * /////////// * /////////// *
    // This is the line that represents the bottom collision.
    public Vector3f[] g_vGroundISector = { new Vector3f( 0.0f, g_BallEntity.fRadius, 0.0f ), new Vector3f( 0.0f, -g_BallEntity.fRadius, 0.0f ) }; 
    

    // This line represents the Balls forward vector.
    public Vector3f[] g_vForwardISector = { new Vector3f( 0.0f, 0.0f, 0.0f ), new Vector3f( 0.0f, 0.0f, g_BallEntity.fRadius ) };
    /////// * /////////// * /////////// * NEW * /////// * /////////// * /////////// *

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
    public GameAction right;
    
    
    public InputManager inputManager;
    
      
    
      
   
    private static final float LOW = 0.5f;
       
       
    public boolean paused;
    
    private float luzAmb1[] = { 0.4f, 0.4f, 0.4f, 1f };	// luz ambiente
    private float luzDif1[] = { LOW, LOW, LOW, 1.0f };	// luz difusa
    private float luzEsp1[] = { 0.0f, 0.0f, 0.0f, 1.0f };	// luz especular
    private float spec[] = { 1.0f, 1.0f, 1.0f, 1.0f };	// luz especular
    private float posLuz1[] = { 0, 10, 17f, 1 };	// posição da fonte de luz
       
    private FloatBuffer posLuz1F;	// posição da fonte de luz
    
    
    
       
    
    
    public void LoadWorld()throws IOException
    {
    	// Here we load the world from a .3ds file
    	//g_World.carregaObjeto("arenaobj_Scene2.obj", true, false, g_World);
    	//g_World.carregaObjeto("arenaobj_10.obj", true, false, g_World);
    	
    	//g_World.carregaObjeto(g_World, "Jupiter2_CrashlandModel.3ds");
    	//g_World.carregaObjeto(g_World, "Park.3ds");
    	g_World.load("collision_arena.3DS");
    	//g_World.carregaObjeto(g_World, "teste.3DS");
    	
    	// The maximum amount of triangles per node.  If a node has equal or less 
        // than this, stop subdividing and store the face indices in that node
        //g_Octree.g_MaxTriangles = 20;
        // The current amount of end nodes in our tree (The nodes with vertices stored in them)
       //g_World.getPObject(6).setMaterialID(0);

        // This stores the amount of nodes that are in the frustum
        //g_Octree.g_TotalNodesDrawn = 0;

        

        // The maximum amount of subdivisions allowed (Levels of subdivision)
       // g_Octree.g_MaxSubdivisions = 4;

        // The number of Nodes we've checked for collision.
        //g_Octree.g_iNumNodesCollided = 0;
        
        
        //System.out.println("antes de passar: " + teste.abc);
        //System.out.println("antes de passar  x: " + temp.x + "y: " + temp.y + "z: " + temp.z);
    	// Nothing new, setup the Octree normally.
    	//g_Octree.getSceneDimensions(g_World, teste);
    	
    	//System.out.println("depois de passar: " +teste.abc);
        octree.getSceneDimensions(g_World);
    	
    	//System.out.println("depois de passar  x: " + temp.x + "y: " + temp.y + "z: " + temp.z);
    	
    	int TotalTriangleCount = octree.getSceneTriangleCount(g_World);
    	octree.createNode(g_World, TotalTriangleCount, octree.getCenter(), octree.getWidth());
    	octree.setDisplayListID( glGenLists(Octree.totalNodesCount) );
    	octree.createDisplayList(octree, g_World, octree.getDisplayListID());

    	// Hide our cursor since we are using first person camera mode
    	Mouse.setGrabbed(true);

    	
    	//glEnable(GL_COLOR_MATERIAL);						// Allow color
    }
    
    
    
    
    @Override
    public void init() throws IOException
    {
        
    	super.init();
    	camera = new CameraQuaternion(true);
    	camera.setPosition(-17f, 20f, 17f,	0, 0, 0,	0, 1, 0);
    	float df=100.0f;
    	
    	
    	
              
    	// Precalculate the Sine and Cosine Lookup Tables.
    	// Basically, loop through 360 Degrees and assign the Radian
    	// value to each array index (which represents the Degree).
    	for ( int i = 0; i < 360; i++ )
    	{
    		g_fSinTable[i] = (float)Math.sin( AR_DegToRad( i ) );
    		g_fCosTable[i] = (float) Math.cos( AR_DegToRad( i ) );
    	}
       
       
    	pObj = new Sphere();
    	pObj.setOrientation(GLU_OUTSIDE);
    	
    	Octree.debug = new BoundingBox();
    	// Turn lighting on initially
    	Octree.turnLighting     = true;	

    	// The current amount of end nodes in our tree (The nodes with vertices stored in them)
    	Octree.totalNodesCount = 0;

    	// This stores the amount of nodes that are in the frustum
    	Octree.totalNodesDrawn = 0;

    	// The maximum amount of triangles per node.  If a node has equal or less 
    	// than this, stop subdividing and store the face indices in that node
    	Octree.maxTriangles = 800;

    	// The maximum amount of subdivisions allowed (Levels of subdivision)
    	Octree.maxSubdivisions = 5;

    	// The number of Nodes we've checked for collision.
    	Octree.numNodesCollided = 0;

    	// Wheter the Object is Colliding with anything in the World or not.
    	octree.setObjectColliding(false);

    	// Wheter we test the whole world for collision or just the nodes we are in.
    	Octree.octreeCollisionDetection = true;
    	
    	LoadWorld();
    	
    	
    	
    	//for(int i=0; i < g_World.getNumOfMaterials(); i++)
    	//{
    	//	System.out.println(g_World.getMaterials(i).getName() + " indice " + i);
    		
    	//}
    	
    	
    	//for(int i=0; i < g_World.getNumOfObjects(); i++)
    	//{
    	//	System.out.println(g_World.getObject(i).getName());
    	//	System.out.println(g_World.getObject(i).getMaterialID());
    		//System.out.println(g_World.getPObject(i).getMaterialID());
    	//}
    	
    // System.out.println(g_World.getPMaterials(12).getColor()[0] + " " + g_World.getPMaterials(12).getColor()[1]
    //                    + " " + g_World.getPMaterials(12).getColor()[2]);
     //System.out.println(g_World.getPMaterials(g_World.getPObject(6).getMaterialID()));
     
    	
       
       
        
        inputManager = new InputManager();
      
        
        createGameActions();
        
        posLuz1F = Conversion.allocFloats(posLuz1);
        
        
        // Define a cor de fundo da janela de visualização como preto
    	glClearColor(0,0,0,1);
    	
    	

    	
    	// Ajusta iluminação
    	glLight( GL_LIGHT0, GL_AMBIENT,  Conversion.allocFloats(luzAmb1)); 
    	glLight( GL_LIGHT0, GL_DIFFUSE,  Conversion.allocFloats(luzDif1));
    	glLight( GL_LIGHT0, GL_SPECULAR, Conversion.allocFloats(luzEsp1));
    	
    	

    	// Habilita todas as fontes de luz
    	glEnable(GL_LIGHT0);
    	
    	glEnable(GL_LIGHTING);
    	// Agora posiciona demais fontes de luz
    	glLight(GL_LIGHT0, GL_POSITION, posLuz1F); 
    	
    	    	// Habilita Z-Buffer
    	glEnable(GL_DEPTH_TEST);
    	
    	// Seleciona o modo de GL_COLOR_MATERIAL
    	//glColorMaterial(GL_FRONT, GL_DIFFUSE);
    	glEnable(GL_COLOR_MATERIAL);
    	glMaterial( GL_FRONT, GL_SPECULAR , Conversion.allocFloats(spec) );
    	glMaterialf( GL_FRONT, GL_SHININESS, df );
    
	    	
    }
 
    protected void update(float elapsedTime)
    {
    	
    	
    	checkSystemInput();

        if(!isPaused())
        {
            checkGameInput();
            camera.update();
                        
        }
    }

 
        public void checkGameInput()
        {
        	
        	if (drawMode.isPressed())
        	{
        		octree.setRenderMode(!octree.isRenderMode());
        		octree.setObjectColliding(false);
        		if(octree.isRenderMode())
        		{
        			glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);	// Render the triangles in fill mode		
        		}else {
        			glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);	// Render the triangles in wire frame mode
        		}
        		octree.createDisplayList(octree, g_World, octree.getDisplayListID());
        	}
        	
        	if (fullScreen.isPressed())
            {
            	setFullScreen(!isFullScreen());
            }
            
            if(enter.isPressed())
            {
            	Octree.octreeCollisionDetection = !Octree.octreeCollisionDetection;
            }
          
            if (left.isPressed())
            {
            	camera.strafe(-SPEED/10 * elapsedTime);
            	
            }

            if (right.isPressed())
            {
            	
            	camera.strafe(SPEED/10 * elapsedTime);	
            }
           
            if (zoomIn.isPressed())
            {
            	camera.move(+SPEED/10 * elapsedTime);
            	
            }
            if (zoomOut.isPressed())
            {
            	
            	camera.move(-SPEED/10 * elapsedTime);
            }
            
            if (moveLeft.isPressed())
            {
            	g_BallEntity.fAngle += (float)AR_DegToRad( g_BallEntity.fTurnRate ) * elapsedTime;
            	
            }

            if (moveRight.isPressed())
            {
            	
            	// Rotate the Ball Angle Counter Clockwise.
        		g_BallEntity.fAngle -= (float)AR_DegToRad( g_BallEntity.fTurnRate ) * elapsedTime;	
            }
           
         // Clamp values above 2 * PI or 360 Deg's.
        	if ( g_BallEntity.fAngle >= AR_2PI )
        		g_BallEntity.fAngle = g_BallEntity.fAngle - AR_2PI;

        	// Clamp values below 0.
        	if ( g_BallEntity.fAngle < 0.0f )
        		g_BallEntity.fAngle = AR_2PI + g_BallEntity.fAngle;           
                        
           
            
            if(debug.isPressed())
            {
            	g_bDisplayNodes = !g_bDisplayNodes;
            }
            
         
        	
        	if (moveUp.isPressed())
            {
            	if ( g_BallEntity.fVelX + (g_BallEntity.fAccel * elapsedTime) < g_BallEntity.fMaxVel )
        		{
        			g_BallEntity.fVelX += g_BallEntity.fAccel * elapsedTime;
        			g_BallEntity.fVelZ += g_BallEntity.fAccel * elapsedTime;
        		}
            
            	
            }
            if (moveDown.isPressed())
            {
            	
            	// Move the Ball Backwards.
        		if ( g_BallEntity.fVelX - (g_BallEntity.fAccel * elapsedTime) > g_BallEntity.fMinVel )
        		{
        			g_BallEntity.fVelX -= g_BallEntity.fAccel * elapsedTime;
        			g_BallEntity.fVelZ -= g_BallEntity.fAccel * elapsedTime;
        		}
            }
          
        	// Apply Gravity to this Entity (using time based motion) if he's not colliding with anything.
        	if ( !octree.isObjectColliding() )
        		g_BallEntity.fVelY += (GRAVITY * elapsedTime);
        	
        	
        	
        	// Apply (spherical based) motion.
        	
        	g_BallEntity.x += (g_fSinTable[(int)AR_RadToDeg( g_BallEntity.fAngle )] * g_BallEntity.fVelX) * elapsedTime;
        	g_BallEntity.y += g_BallEntity.fVelY * elapsedTime;
        	g_BallEntity.z += (g_fCosTable[(int)AR_RadToDeg( g_BallEntity.fAngle )] * g_BallEntity.fVelZ) * elapsedTime;
        	
                	
        	// Adjust the Forward I-Sectors Endpoint.
        	g_vForwardISector[1].x = g_fSinTable[(int)AR_RadToDeg( g_BallEntity.fAngle )] * g_BallEntity.fRadius * 0.2f;
        	g_vForwardISector[1].y = 0.0f;
        	g_vForwardISector[1].z = g_fCosTable[(int)AR_RadToDeg( g_BallEntity.fAngle )] * g_BallEntity.fRadius * 0.2f;

        	
        	
        	// Slow this guy down (friction).
        	if ( g_BallEntity.fVelX > g_fFriction * elapsedTime )
        	{
        		g_BallEntity.fVelX -= g_fFriction * elapsedTime;
        	}
        	
        	if ( g_BallEntity.fVelZ > g_fFriction * elapsedTime )
        	{
        		g_BallEntity.fVelZ -= g_fFriction * elapsedTime;
        	}
        	
        	if ( g_BallEntity.fVelX < g_fFriction * elapsedTime )
        	{
        		g_BallEntity.fVelX += g_fFriction * elapsedTime;
        	}
        	
        	if ( g_BallEntity.fVelZ < g_fFriction * elapsedTime )
        	{
        		g_BallEntity.fVelZ += g_fFriction * elapsedTime;
        	}

        	// If this Ball falls outside the world, drop back from the top.
        	if ( g_BallEntity.y < -30 )
        	{
        		g_BallEntity.x = g_BallEntity.z = 0.0f;
        		g_BallEntity.y = 5.0f;
        		g_BallEntity.fVelX = g_BallEntity.fVelY = g_BallEntity.fVelZ = 0.0f;
        	}

        	Vector3f[] vGroundLine = {new Vector3f(), new Vector3f()};
        	Vector3f[] vForwardLine = {new Vector3f(), new Vector3f()};
        	     	        	
        	
        	// Prepare a Temporary line transformed to the Balls exact world position.
        	vGroundLine[0].x = g_BallEntity.x + g_vGroundISector[0].x;
        	vGroundLine[0].y = g_BallEntity.y + g_vGroundISector[0].y;
        	vGroundLine[0].z = g_BallEntity.z + g_vGroundISector[0].z;
        	
        	vGroundLine[1].x = g_BallEntity.x + g_vGroundISector[1].x;
        	vGroundLine[1].y = g_BallEntity.y + g_vGroundISector[1].y;
        	vGroundLine[1].z = g_BallEntity.z + g_vGroundISector[1].z;

        	// Prepare a Temporary line transformed to the Balls exact world position.
        	vForwardLine[0].x = g_BallEntity.x + g_vForwardISector[0].x;
        	vForwardLine[0].y = g_BallEntity.y + g_vForwardISector[0].y;
        	vForwardLine[0].z = g_BallEntity.z + g_vForwardISector[0].z;
        	
        	vForwardLine[1].x = g_BallEntity.x + g_vForwardISector[1].x;
        	vForwardLine[1].y = g_BallEntity.y + g_vForwardISector[1].y;
        	vForwardLine[1].z = g_BallEntity.z + g_vForwardISector[1].z;

        	// A temporary Vector holding the Intersection Point of our Intersection Check.
        	vIntersectionPt = new Vector3f();

        	// Reset the Status of the Object (wheter it is colliding or not).
        	octree.setObjectColliding(false);

        	// Reset the Nodes collided to zero so we can start with a fresh count.
        	Octree.numNodesCollided = 0;

        	// Test the line for an intersection with the Octree Geometry.
        	if ( octree.intersectLineWithOctree( octree, g_World, vGroundLine, vIntersectionPt ) )
        	{
        		// Move the Ball up from the point at which it collided with the ground. This is what
        		// ground clamping is!
        		g_BallEntity.x = vIntersectionPt.x;
        		// NOTE: Make sure it is above the surface, AND half it's height (so it isn't half underground).
        		// This would only apply if you placed entity's by their exact center.
        		g_BallEntity.y = vIntersectionPt.y + g_BallEntity.fRadius;
        		g_BallEntity.z = vIntersectionPt.z;

        		// Stop your up-down velocity.
        		g_BallEntity.fVelY = 0.0f;
        	}
        	
        	// Test the line for an intersection with the Octree Geometry.
        	if ( octree.intersectLineWithOctree( octree, g_World, vForwardLine, vIntersectionPt ) )
        	{
        		// Move the Ball up from the point at which it collided with the ground. This is what
        		// ground clamping is!
        		g_BallEntity.x = vIntersectionPt.x;
        		// NOTE: Make sure it is above the surface, AND half it's height (so it isn't half underground).
        		// This would only apply if you placed entity's by their exact center.
        		g_BallEntity.y = vIntersectionPt.y + g_BallEntity.fRadius;
        		g_BallEntity.z = vIntersectionPt.z;

        		// Stop your up-down velocity.
        		g_BallEntity.fVelY = 0.0f;
        	}
        	
        	
        }

        protected void render() 
        {
        	

        	glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);	// Clear The Screen And The Depth Buffer

        	glLoadIdentity();
        	
        	camera.look();	
        	
        	// Each frame we calculate the new frustum.  In reality you only need to
        	// calculate the frustum when we move the camera.
        	GameCore.gFrustum.calculateFrustum();

        	// Initialize the total node count that is being draw per frame
        	Octree.totalNodesDrawn = 0;

        	glPushMatrix();
        		// Here we draw the octree, starting with the root node and recursing down each node.
        		// This time, we pass in the root node and just the original world model.  You could
        		// just store the world in the root node and not have to keep the original data around.
        		// This is up to you.  I like this way better because it's easy, though it could be 
        		// more error prone.
        		octree.drawOctree(octree, g_World );
        	glPopMatrix();

        	// Render the cubed nodes to visualize the octree (in wire frame mode)
        	if( g_bDisplayNodes )
        		Octree.debug.drawBoundingBox();

        	glPushMatrix();
        		// If there was a collision, make the Orange ball Red.
        		if ( octree.isObjectColliding() )
        		{
        			glColor3f( 1.0f, 0.0f, 0.0f );
        		}else{
        			glColor3f( 1.0f, 0.5f, 0.0f );// Disable Lighting.
        		}
        		// Move the Ball into place.
        		glTranslatef(g_BallEntity.x, g_BallEntity.y, g_BallEntity.z);
        		
        		glDisable( GL_LIGHTING );
        		// Draw the Ground Intersection Line.
        		glBegin( GL_LINES );
        			glColor3f( 1, 1, 1 );
        			glVertex3f( g_vGroundISector[0].x, g_vGroundISector[0].y, g_vGroundISector[0].z );
        			glVertex3f( g_vGroundISector[1].x, g_vGroundISector[1].y, g_vGroundISector[1].z );	
        		glEnd();

        		// Draw the Forward Intersection Line.
        		glBegin( GL_LINES );
        			glColor3f( 1, 1, 0 );
        			glVertex3f( g_vForwardISector[0].x * 10.0f, g_vForwardISector[0].y, g_vForwardISector[0].z * 10.0f );
        			glVertex3f( g_vForwardISector[1].x * 10.0f, g_vForwardISector[1].y, g_vForwardISector[1].z * 10.0f );	
        		glEnd();

        		// Re-enable lighting.
        		glEnable( GL_LIGHTING );
        		
        		

        		
        		//System.out.println("x " + g_BallEntity.x + " y " + g_BallEntity.y);
        		// Draw it!
        		pObj.draw(g_BallEntity.fRadius, 20, 20 );
        		

        		
        	glPopMatrix();

        
        	screen.setTitle("Triangles: " + Octree.maxTriangles + "  -Total Draw: " + Octree.totalNodesDrawn + "  -Subdivisions: " +  Octree.maxSubdivisions +
        			 "  -FPS: " + FPSCounter.get() + "  -Node Collisions: " + Octree.numNodesCollided + "  -Object Colliding? " +
        				   octree.isObjectColliding() ); 	
        	
        }
            
        
       
      public void createGameActions()
        {
           
            super.createGameActions();
      	  
            moveLeft = new GameAction("moveLeft",GameAction.NORMAL, Keyboard.KEY_LEFT);
            moveRight = new GameAction("moveRight",GameAction.NORMAL, Keyboard.KEY_RIGHT);
            moveUp = new GameAction("moveUp",GameAction.NORMAL, Keyboard.KEY_UP);
            moveDown = new GameAction("moveDown",GameAction.NORMAL, Keyboard.KEY_DOWN);
            zoomIn = new GameAction("zoomIn",GameAction.NORMAL, Keyboard.KEY_W);
            zoomOut = new GameAction("zoomOut", GameAction.NORMAL, Keyboard.KEY_S);
            exit = new GameAction("exit", GameAction.DETECT_INITIAL_PRESS_ONLY, Keyboard.KEY_ESCAPE);
            
            left = new GameAction("left",GameAction.NORMAL, Keyboard.KEY_A);
            
            enter = new GameAction("enter", GameAction.DETECT_INITIAL_PRESS_ONLY, Keyboard.KEY_RETURN);
            debug = new GameAction("debug", GameAction.DETECT_INITIAL_PRESS_ONLY, Keyboard.KEY_SPACE);
            fullScreen  = new GameAction("fullScreen", GameAction.DETECT_INITIAL_PRESS_ONLY, Keyboard.KEY_F1);
            saveCamera = new GameAction("saveCamera", GameAction.DETECT_INITIAL_PRESS_ONLY, Keyboard.KEY_F11);
            drawMode  = new GameAction("drawMode", GameAction.DETECT_INITIAL_PRESS_ONLY, Keyboard.KEY_T);
            right = new GameAction("right",GameAction.NORMAL, Keyboard.KEY_D);
                        
   
        }
        
   // Convert from Degrees to Radians.
      private float AR_DegToRad(float x)
      {
    	 
    	  return (float) Math.toRadians(x);
      }

      // Convert from Radians to Degrees.
      private float AR_RadToDeg(float x) 
      {
    	
    	  return (float) Math.toDegrees(x);
      }

}

