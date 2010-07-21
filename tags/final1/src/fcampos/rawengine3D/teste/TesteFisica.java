package fcampos.rawengine3D.teste;

import fcampos.rawengine3D.input.*;
import fcampos.rawengine3D.resource.*;
import fcampos.rawengine3D.gamecore.*;
import fcampos.rawengine3D.MathUtil.*;
import fcampos.rawengine3D.fps.*;
import fcampos.rawengine3D.fisica.*;

import java.io.*;
import org.lwjgl.input.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.util.glu.GLU.*;

/**
 *
 * @author Fabio
 */
public class TesteFisica extends FisicaGameCore {

    public static void main(String[] args)
    {
        new TesteFisica().run();
    }
    
           
    public GameAction moveLeft;
    public GameAction moveRight;
    public GameAction moveUp;
    public GameAction moveDown;
    public GameAction zoomIn;
    public GameAction zoomOut;
    public GameAction exit;
    public GameAction pause;
    public GameAction fullScreen;
    
    
    
    public InputManager inputManager;
    private DrawString draw;
    
        
    private CameraQuaternion camera;
   
    public boolean paused;
    
    
    private boolean fullscreen;
    
    
    
    /*
    ConstantVelocity is an object from Physics1.h. It is a container for simulating masses. 
    Specifically, it creates a mass and sets its velocity as (1, 0, 0) so that the mass 
    moves with 1.0f meters / second in the x direction.
    */
    public ConstantVelocity constantVelocity = new ConstantVelocity();

    /*
    MotionUnderGravitation is an object from Physics1.h. It is a container for simulating masses. 
    This object applies gravitation to all masses it contains. This gravitation is set by the 
    constructor which is (0.0f, -9.81f, 0.0f) for now (see below). This means a gravitational acceleration 
    of 9.81 meter per (second * second) in the negative y direction. MotionUnderGravitation 
    creates one mass by default and sets its position to (-10, 0, 0) and its velocity to
    (10, 15, 0)
    */
    public MotionUnderGravitation motionUnderGravitation = 
    	new MotionUnderGravitation(new Vector3f(0.0f, -9.81f, 0.0f));

    /*
    MassConnectedWithSpring is an object from Physics1.h. It is a container for simulating masses. 
    This object has a member called connectionPos, which is the connection position of the spring 
    it simulates. All masses in this container are pulled towards the connectionPos by a spring 
    with a constant of stiffness. This constant is set by the constructor and for now it is 2.0 
    (see below).
    */
    public MassConnectedWithSpring massConnectedWithSpring = 
    	new MassConnectedWithSpring(2.0f);

    float slowMotionRatio = 10.0f;									// slowMotionRatio Is A Value To Slow Down The Simulation, Relative To Real World Time
    float timeElapsed = 0;	
    
    
    @Override
    public void init() throws IOException
    {
        super.init();
        
               
             
        
        inputManager = new InputManager();
                
        createGameActions();
        
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);		
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();		
		gluPerspective(45.0f, 800 / 600, 1.0f, 100.0f);
		glMatrixMode(GL_MODELVIEW);
		glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);       
        
		
        paused = false;
        fullscreen = false;
        draw = new DrawString("texturas/font.png");
        
    	
    }
    
 

   
    public void setFullScreen(boolean p)
    {
        if (fullscreen != p)
        {
            this.fullscreen = p;
            screen.setFullScreen(fullscreen);
        }

     }
    
    public boolean isFullScreen()
    {
        return fullscreen;
    }

    public boolean isPaused()
    {
        return paused;
    }

    public void setPaused(boolean p)
    {
        if (paused != p)
        {
            this.paused = p;
            
        }

     }

    public void update(float numOfIterations, float dt)
    {
    	
    	
    	checkSystemInput();
    		
    	
    	  	

        if(!isPaused())
        {
            checkGameInput();
                        
        }
        for (int a = 0; a < numOfIterations; ++a)					// We Need To Iterate Simulations "numOfIterations" Times
		{
			constantVelocity.operate(dt);							// Iterate constantVelocity Simulation By dt Seconds
			motionUnderGravitation.operate(dt);					// Iterate motionUnderGravitation Simulation By dt Seconds
			massConnectedWithSpring.operate(dt);					// Iterate massConnectedWithSpring Simulation By dt Seconds
		}
    }

        public void checkSystemInput()
        {
            if (pause.isPressed())
            {
                setPaused(!isPaused());
                camera.setViewByMouse(!isPaused());
                Mouse.setGrabbed(!isPaused());
            }
            if (exit.isPressed())
            {
                stop();
            }
            
        }

        public void checkGameInput()
        {
          
            if (moveLeft.isPressed())
            {
            	//camera.strafe(-speed * FPSCounter.frameInterval);
            	slowMotionRatio = 1f;
            	
            }

            if (moveRight.isPressed())
            {
            	
            	//camera.strafe(speed * FPSCounter.frameInterval);
            	slowMotionRatio = 10.0f;
            }
           
            if (moveUp.isPressed())
            {
            	camera.move(+speed * FPSCounter.frameInterval);
            	
            }
            if (moveDown.isPressed())
            {
            	
            	camera.move(-speed * FPSCounter.frameInterval);
            }
            
           
            
                        
           
            
           
            if (fullScreen.isPressed())
            {
            	setFullScreen(!isFullScreen());
            }
            
           
            
            
        }

        public void render() 
        {
        	glMatrixMode(GL_MODELVIEW);
        	glLoadIdentity ();											// Reset The Modelview Matrix
        	
        	// Position Camera 40 Meters Up In Z-Direction.
        	// Set The Up Vector In Y-Direction So That +X Directs To Right And +Y Directs To Up On The Window.
        	gluLookAt(0, 0, 40, 0, 0, 0, 0, 1, 0);						
        	
        	glClear (GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);		// Clear Screen And Depth Buffer


        	// Drawing The Coordinate Plane Starts Here.
        	// We Will Draw Horizontal And Vertical Lines With A Space Of 1 Meter Between Them.
        	glColor3f(0, 0, 1.0f);										// Draw In Blue
        	glBegin(GL_LINES);
        	
        	// Draw The Vertical Lines
        	for (float x = -20; x <= 20; x += 1.0f)						// x += 1.0f Stands For 1 Meter Of Space In This Example
        	{
        		glVertex3f(x, 20, 0);
        		glVertex3f(x,-20, 0);
        	}

        	// Draw The Horizontal Lines
        	for (float y = -20; y <= 20; y += 1.0f)						// y += 1.0f Stands For 1 Meter Of Space In This Example
        	{
        		glVertex3f( 20, y, 0);
        		glVertex3f(-20, y, 0);
        	}

        	glEnd();
        	// Drawing The Coordinate Plane Ends Here.

        	// Draw All Masses In constantVelocity Simulation (Actually There Is Only One Mass In This Example Of Code)
        	glColor3f(1.0f, 0, 0);										// Draw In Red
        	
        	for (int a = 0; a < constantVelocity.getNumberOfMasses(); ++a)
        	{
        		Mass mass = (Mass)constantVelocity.getMass(a);
        		Vector3f pos = new Vector3f(mass.getPosition());

        		glPointSize(4);
        		glBegin(GL_POINTS);
        			glVertex3f(pos.x, pos.y, pos.z);
        		glEnd();
        	}
        	// Drawing Masses In constantVelocity Simulation Ends Here.

        	// Draw All Masses In motionUnderGravitation Simulation (Actually There Is Only One Mass In This Example Of Code)
        	glColor3f(1f, 1f, 0);									// Draw In Yellow
        	for (int a = 0; a < motionUnderGravitation.getNumberOfMasses(); ++a)
        	{
        		Mass mass = motionUnderGravitation.getMass(a);
        		Vector3f pos = new Vector3f(mass.getPosition());

        		
        		glPointSize(4);
        		glBegin(GL_POINTS);
        			glVertex3f(pos.x, pos.y, pos.z);
        		glEnd();
        	}
        	// Drawing Masses In motionUnderGravitation Simulation Ends Here.

        	// Draw All Masses In massConnectedWithSpring Simulation (Actually There Is Only One Mass In This Example Of Code)
        	glColor3f(0, 1f, 0);										// Draw In Green
        	for (int a = 0; a < massConnectedWithSpring.getNumberOfMasses(); ++a)
        	{
        		Mass mass = massConnectedWithSpring.getMass(a);
        		Vector3f pos = new Vector3f(mass.getPosition());

        		

        		glPointSize(8);
        		glBegin(GL_POINTS);
        			glVertex3f(pos.x, pos.y, pos.z);
        		glEnd();

        		// Draw A Line From The Mass Position To Connection Position To Represent The Spring
        		glBegin(GL_LINES);
        			glVertex3f(pos.x, pos.y, pos.z);
        			pos.setTo(massConnectedWithSpring.connectionPos);
        			glVertex3f(pos.x, pos.y, pos.z);
        		glEnd();
        	}
        	// Drawing Masses In massConnectedWithSpring Simulation Ends Here.


        	
    		
        	screen.enterOrtho();
        	draw.drawString(1,"QPS: " + timeElapsed, 5, 5);  
        	
        	screen.leaveOrtho();
        	        	
        	
        }
             
      public void createGameActions()
        {
            moveLeft = new GameAction("moveLeft");
            moveRight = new GameAction("moveRight");
            moveUp = new GameAction("moveUp");
            moveDown = new GameAction("moveDown");
            zoomIn = new GameAction("zoomIn");
            zoomOut = new GameAction("zoomIn");
            exit = new GameAction("exit", GameAction.DETECT_INITIAL_PRESS_ONLY);
            pause = new GameAction("pause", GameAction.DETECT_INITIAL_PRESS_ONLY);
            fullScreen  = new GameAction("fullScreen", GameAction.DETECT_INITIAL_PRESS_ONLY);
            
            
            
            inputManager.mapToKey(exit, Keyboard.KEY_ESCAPE);
            inputManager.mapToKey(pause, Keyboard.KEY_P);
           
            inputManager.mapToKey(zoomIn, Keyboard.KEY_X);
            inputManager.mapToKey(zoomOut, Keyboard.KEY_S) ;
            inputManager.mapToKey(moveLeft, Keyboard.KEY_LEFT);
            inputManager.mapToKey(moveRight, Keyboard.KEY_RIGHT);
            inputManager.mapToKey(moveUp, Keyboard.KEY_UP);
            inputManager.mapToKey(moveDown, Keyboard.KEY_DOWN);
            
            
            inputManager.mapToKey(fullScreen, Keyboard.KEY_F1);
           
            
            
            
           
        }
        


}