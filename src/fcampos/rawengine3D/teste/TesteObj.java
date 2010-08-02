package fcampos.rawengine3D.teste;

import static org.lwjgl.opengl.GL11.*;

import static org.lwjgl.opengl.GL11.GL_AMBIENT;

import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_DIFFUSE;
import static org.lwjgl.opengl.GL11.GL_FILL;
import static org.lwjgl.opengl.GL11.GL_FRONT_AND_BACK;
import static org.lwjgl.opengl.GL11.GL_LIGHT0;
import static org.lwjgl.opengl.GL11.GL_LIGHTING;
import static org.lwjgl.opengl.GL11.GL_LINE;
import static org.lwjgl.opengl.GL11.GL_POSITION;
import static org.lwjgl.opengl.GL11.GL_SPECULAR;

import java.io.IOException;
import java.nio.FloatBuffer;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import fcampos.rawengine3D.gamecore.GameCore;
import fcampos.rawengine3D.graficos.Octree;
import fcampos.rawengine3D.input.Camera;
import fcampos.rawengine3D.input.GameAction;
import fcampos.rawengine3D.model.BoundingBox;
import fcampos.rawengine3D.model.ModelObj;
import fcampos.rawengine3D.resource.Conversion;

public class TesteObj extends GameCore {
	
	public static void main(String[] args)
    {
        new TesteObj().run();
    }
	
	public GameAction moveLeft;
    public GameAction moveRight;
    public GameAction moveUp;
    public GameAction moveDown;
    public GameAction zoomIn;
    public GameAction zoomOut;
    public GameAction angCam;
    public GameAction fog;
    public GameAction modTex;
    public GameAction debug;
    public GameAction drawMode;
   
    boolean displayNodes = false;

    private float SPEED = 50.0f;
    
    private int modo = GL_MODULATE;

    
    private Camera camera;
    
  
    private float angcam;
    
    public ModelObj cadeira;
    
    private static final float LOW = 0.5f;
    
    private float luzAmb1[] = { 0.4f, 0.4f, 0.4f, 1f };	// luz ambiente
    private float luzDif1[] = { LOW, LOW, LOW, 1.0f };	// luz difusa
    private float luzEsp1[] = { 0.0f, 0.0f, 0.0f, 1.0f };	// luz especular
    //private float spec[] 	= { 1.0f, 1.0f, 1.0f, 1.0f };	// luz especular
    private float posLuz1[] = { 0, 10, 17f, 1 };	// posição da fonte de luz
       
    private FloatBuffer posLuz1F;	// posição da fonte de luz

    Octree octree = new Octree();
    
	
    @Override
    public void init() throws IOException
    {
        
    	super.init();
    	
    	screen.setTitle("TSala3D");
    	camera = new Camera(true);
    	
    	camera.setPosition(0.0f, 30.0f, 15.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
    	camera.setFixedAxis(Camera.Y_AXIS);
    	
    	//float df=100.0f;
    	
    	 posLuz1F = Conversion.allocFloats(posLuz1);
    	// Turn the color back to blue'ish purple and disable fog
 		glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        
        	// Habilita todas as fontes de luz
        	glEnable(GL_LIGHT0);
        	
        	glEnable(GL_LIGHTING);
        	
        	// Ajusta iluminação
        	glLight( GL_LIGHT0, GL_AMBIENT,  Conversion.allocFloats(luzAmb1)); 
        	glLight( GL_LIGHT0, GL_DIFFUSE,  Conversion.allocFloats(luzDif1));
        	glLight( GL_LIGHT0, GL_SPECULAR, Conversion.allocFloats(luzEsp1));
        	
        	    	// Habilita Z-Buffer
        	glEnable(GL_DEPTH_TEST);
        	//glCullFace(GL_FRONT);								// Don't draw the back sides of polygons
        	//glEnable(GL_CULL_FACE);	
        	
        	// Seleciona o modo de GL_COLOR_MATERIAL
        	//glColorMaterial(GL_FRONT, GL_DIFFUSE);
        	
        	// Seleciona o modo de GL_COLOR_MATERIAL
        	//glColorMaterial(GL_FRONT, GL_DIFFUSE);
        	//glEnable(GL_COLOR_MATERIAL);
        	
        	        	
    	createGameActions();
    	
    	// We need to specify our camera's radius in the beginning, I chose 1.
    	camera.setRadius(1.5f);
  
    	Octree.debug = new BoundingBox();
    	Octree.debug.setColor(BoundingBox.YELLOW);
    	
    	// Turn lighting on initially
    	Octree.turnLighting     = true;	

    	// The current amount of end nodes in our tree (The nodes with vertices stored in them)
    	Octree.totalNodesCount = 0;

    	// This stores the amount of nodes that are in the frustum
    	Octree.totalNodesDrawn = 0;

    	// The maximum amount of triangles per node.  If a node has equal or less 
    	// than this, stop subdividing and store the face indices in that node
    	Octree.maxTriangles = 1000;

    	// The maximum amount of subdivisions allowed (Levels of subdivision)
    	Octree.maxSubdivisions = 5;

    	// The number of Nodes we've checked for collision.
    	Octree.numNodesCollided = 0;

    	// Wheter the Object is Colliding with anything in the World or not.
    	//TOctree.setObjectColliding(true);

    	// Wheter we test the whole world for collision or just the nodes we are in.
    	Octree.octreeCollisionDetection = true;
    	
    	cadeira = new ModelObj();
    	
    	cadeira.load("foot.obj",true, false);
    	//glEnable(GL_FOG);									// This enables our OpenGL Fog

    	glLight(GL_LIGHT0, GL_POSITION, posLuz1F); 
    	
    	octree.getSceneDimensions(cadeira);
    	  
    	int TotalTriangleCount = octree.getSceneTriangleCount(cadeira);
    	octree.createNode(cadeira, TotalTriangleCount, octree.getCenter(), octree.getWidth());
    	octree.setDisplayListID(glGenLists(Octree.totalNodesCount) );
    	octree.createDisplayList(octree, cadeira, octree.getDisplayListID());
    	
    	// Hide our cursor since we are using first person camera mode
    	Mouse.setGrabbed(true);
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

       

    protected void checkGameInput(float elapsedTime)
        {
    		//System.out.println(FPSCounter.frameInterval);
        	super.checkGameInput(); // Checamos se as teclas foram pressionadas ou não.
        	
        	// Once we have the frame interval, we find the current speed
        	float speed = (SPEED * elapsedTime);
          
            if (moveLeft.isPressed())
            {
            	camera.strafe(-speed);
            	
            }

            if (moveRight.isPressed())
            {
            	
            	camera.strafe(speed);	
            }
           
            if (moveUp.isPressed())
            {
            	camera.move(+speed);
            	
            }
            if (moveDown.isPressed())
            {
            	
            	camera.move(-speed );
            }
            
            if (angCam.isPressed())
            {
            	angcam += 15;
            	if(angcam > 75)
            	{
            		angcam = 45;
            	}
            	
            	
            		
            }
            
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
        		
        		octree.createDisplayList(octree, cadeira, octree.getDisplayListID());
        	}
            
            if(debug.isPressed())
            {
            	displayNodes = !displayNodes;
            }
                        
            if(fog.isPressed())
            {
            	if(glIsEnabled(GL_FOG))
            	{
            		glDisable(GL_FOG);
            	}
            	else
            	{
            		glEnable(GL_FOG);
            	}
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
            
            
            
        }

    
	@Override
	protected void render() {
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);	// Clear The Screen And The Depth Buffer

    	glLoadIdentity();// To calculate our collision detection with the camera, it just takes one function
    	// call from the client side.  We just pass in the vertices in the world that we
    	// want to check, and then the vertex count.  
    	// Each frame we calculate the new frustum.  In reality you only need to
    	// calculate the frustum when we move the camera.
    	//
    	
    	camera.look();	
		//cadeira.draw();
    	
    	
    	GameCore.gFrustum.calculateFrustum();
    	// Agora posiciona demais fontes de luz
    	//glLight(GL_LIGHT0, GL_POSITION, posLuz1F); 
    	//gluLookAt(camera.getPosition()[0].x, camera.getPosition()[0].y, camera.getPosition()[0].z,
    	//		camera.getPosition()[1].x,	camera.getPosition()[1].y,	 camera.getPosition()[1].z,
    	//		camera.getPosition()[2].x, camera.getPosition()[2].y, camera.getPosition()[2].z);	
    	// Initialize the total node count that is being draw per frame
    	Octree.totalNodesDrawn = 0;
    	
    	glPushMatrix();
    		// Here we draw the octree, starting with the root node and recursing down each node.
    		// This time, we pass in the root node and just the original world model.  You could
    		// just store the world in the root node and not have to keep the original data around.
    		// This is up to you.  I like this way better because it's easy, though it could be 
    		// more error prone.
    		octree.drawOctree(octree, cadeira );
    	glPopMatrix();
    	
    	// Render the cubed nodes to visualize the octree (in wire frame mode)
    	if( displayNodes )
    	{
    		Octree.debug.drawBoundingBox();
    		
    	}
    	
	}
	
	
    public void createGameActions()
    {
  	  super.createGameActions();
          moveLeft = new GameAction("moveLeft", GameAction.NORMAL, Keyboard.KEY_LEFT);
          moveRight = new GameAction("moveRight", GameAction.NORMAL, Keyboard.KEY_RIGHT);
          moveUp = new GameAction("moveUp", GameAction.NORMAL, Keyboard.KEY_UP);
          moveDown = new GameAction("moveDown", GameAction.NORMAL, Keyboard.KEY_DOWN);
          zoomIn = new GameAction("zoomIn", GameAction.NORMAL, Keyboard.KEY_X);
          zoomOut = new GameAction("zoomIn", GameAction.NORMAL, Keyboard.KEY_S);
          debug = new GameAction("debug", GameAction.DETECT_INITIAL_PRESS_ONLY, Keyboard.KEY_SPACE);
          angCam = new GameAction("angCam", GameAction.DETECT_INITIAL_PRESS_ONLY, Keyboard.KEY_A);
          drawMode  = new GameAction("drawMode", GameAction.DETECT_INITIAL_PRESS_ONLY, Keyboard.KEY_T);
          fog = new GameAction("fog", GameAction.DETECT_INITIAL_PRESS_ONLY, Keyboard.KEY_N);
          modTex = new GameAction("modTex", GameAction.DETECT_INITIAL_PRESS_ONLY, Keyboard.KEY_M);
   
      }
	
	

}
