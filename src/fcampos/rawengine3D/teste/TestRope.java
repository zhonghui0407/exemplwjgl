package fcampos.rawengine3D.teste;

import fcampos.rawengine3D.input.*;
import fcampos.rawengine3D.gamecore.*;
import fcampos.rawengine3D.MathUtil.*;
import fcampos.rawengine3D.fisica.*;

import java.io.*;
import org.lwjgl.input.*;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.util.glu.GLU.*;

/**
 *
 * @author Fabio
 */
public class TestRope extends FisicaGameCore { 

    public static void main(String[] args)
    {
        new TestRope().run();
    }
    
           
    public GameAction moveLeft;
    public GameAction moveRight;
    public GameAction moveForward;
    public GameAction moveBackward;
    public GameAction moveUp;
    public GameAction moveDown;
    public GameAction exit;
    public GameAction fullScreen;
    public GameAction pause;

  
    RopeSimulation ropeSimulation = new RopeSimulation(
			80,						// 80 Particles (Masses)
			0.05f,					// Each Particle Has A Weight Of 50 Grams
			10000.0f,				// springConstant In The Rope
			0.05f,					// Normal Length Of Springs In The Rope
			0.9f,					// Spring Inner Friction Constant
			new Vector3f(0, -9.81f, 0), // Gravitational Acceleration
			0.02f,					// Air Friction Constant
			10.0f,					// Ground Repel Constant
			0.2f,					// Ground Slide Friction Constant
			10.0f,					// Ground Absorption Constant
			-1.5f);	

    @Override
    public void init() throws IOException
    {
        super.init();
        
        screen.setTitle("Rope");
		screen.setFullScreen(false);
           
        createGameActions();
        
        ropeSimulation.getMass(ropeSimulation.getNumberOfMasses() - 1).getVelocity().z = 10.0f;
       
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);		
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();		
		gluPerspective(45.0f, screen.getHeight() / screen.getHeight(), 1.0f, 100.0f);
		glMatrixMode(GL_MODELVIEW);
		glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);
		glShadeModel(GL_SMOOTH);
	
    }
    
    public void createGameActions()
    {
        moveLeft = new GameAction("moveLeft");
        moveLeft.setKeyCode(Keyboard.KEY_LEFT);
        
        moveRight = new GameAction("moveRight");
        moveRight.setKeyCode(Keyboard.KEY_RIGHT);
        
        moveForward = new GameAction("moveUp");
        moveForward.setKeyCode(Keyboard.KEY_UP);
        
        moveBackward = new GameAction("moveDown");
        moveBackward.setKeyCode(Keyboard.KEY_DOWN);
        
        moveUp = new GameAction("zoomIn");
        moveUp.setKeyCode(Keyboard.KEY_HOME);
        
        moveDown = new GameAction("zoomIn");
        moveDown.setKeyCode(Keyboard.KEY_END);
        
        exit = new GameAction("exit", GameAction.DETECT_INITIAL_PRESS_ONLY);
        exit.setKeyCode(Keyboard.KEY_ESCAPE);
        
        fullScreen  = new GameAction("fullScreen", GameAction.DETECT_INITIAL_PRESS_ONLY);
        fullScreen.setKeyCode(Keyboard.KEY_F1);
        
        pause = new GameAction("pause", GameAction.DETECT_INITIAL_PRESS_ONLY);
        pause.setKeyCode(Keyboard.KEY_P);         
         
    }

  
    public void update(float numOfIterations, float dt)
    {
    	
    	
    	checkSystemInput();
    	
    	if(pause.isPressed())
    	{
    		setPaused();
    	}
    	
       
        if(!isPaused())
        {
            checkGameInput();
       
            for (int a = 0; a < numOfIterations; ++a)					
            {
            	ropeSimulation.operate(dt);							
			}
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
          
        	 if (fullScreen.isPressed())
             {
             	setFullScreen(!isFullScreen());
             
             }
          
        	 
        	Vector3f ropeConnectionVel = new Vector3f();   
        	
            if (moveLeft.isPressed())
            {
            	ropeConnectionVel.x -= 3.0f;
            	
            }

            if (moveRight.isPressed())
            {
            	
            	ropeConnectionVel.x += 3.0f;
            }
           
            if (moveForward.isPressed())
            {
            	
            	ropeConnectionVel.z -= 3.0f;
            	
            }
            if (moveBackward.isPressed())
            {
            	
            	
            	ropeConnectionVel.z += 3.0f;
            }
            
            if (moveUp.isPressed())
            {
            	
            	ropeConnectionVel.y += 3.0f;
            	
            }
            if (moveDown.isPressed())
            {
            	
            	
            	ropeConnectionVel.y -= 3.0f;
            }
            
            ropeSimulation.setRopeConnectionVel(ropeConnectionVel);           
 
        }

        public void render() 
        {
        	glMatrixMode(GL_MODELVIEW);
        	glLoadIdentity ();														// Reset The Modelview Matrix
        	
        	// Position Camera 40 Meters Up In Z-Direction.
        	// Set The Up Vector In Y-Direction So That +X Directs To Right And +Y Directs To Up On The Window.
        	gluLookAt(0, 0, 4, 0, 0, 0, 0, 1, 0);						

        	glClear (GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);					// Clear Screen And Depth Buffer

        	// Draw A Plane To Represent The Ground (Different Colors To Create A Fade)
        	glBegin(GL_QUADS);
        		glColor3f(0, 0, 1);												// Set Color To Light Blue
        		glVertex3f(20, ropeSimulation.getGroundHeight(), 20);
        		glVertex3f(-20, ropeSimulation.getGroundHeight(), 20);
        		glColor3f(0, 0, 0);												// Set Color To Black
        		glVertex3f(-20, ropeSimulation.getGroundHeight(), -20);
        		glVertex3f(20, ropeSimulation.getGroundHeight(), -20);
        	glEnd();
        	
        	// Start Drawing Shadow Of The Rope
        	glColor3f(0, 0, 0);													// Set Color To Black
        	for (int a = 0; a < ropeSimulation.getNumberOfMasses() - 1; ++a)
        	{
        		Mass mass1 = ropeSimulation.getMass(a);
        		Vector3f pos1 = new Vector3f(mass1.getPosition());

        		Mass mass2 = ropeSimulation.getMass(a + 1);
        		Vector3f pos2 = new Vector3f(mass2.getPosition());

        		glLineWidth(1);
        		glBegin(GL_LINES);
        			glVertex3f(pos1.x, ropeSimulation.getGroundHeight(), pos1.z);		// Draw Shadow At groundHeight
        			glVertex3f(pos2.x, ropeSimulation.getGroundHeight(), pos2.z);		// Draw Shadow At groundHeight
        		glEnd();
        	}
        	// Drawing Shadow Ends Here.

        	// Start Drawing The Rope.
        	glColor3f(1, 1, 0);		// Set Color To Yellow
        	for (int a = 0; a < ropeSimulation.getNumberOfMasses() - 1; ++a)
        	{
        		Mass mass1 = ropeSimulation.getMass(a);
        		Vector3f pos1 = new Vector3f(mass1.getPosition());

        		Mass mass2 = ropeSimulation.getMass(a + 1);
        		Vector3f pos2 = new Vector3f(mass2.getPosition());

        		glLineWidth(3);
        		glBegin(GL_LINES);
        			glVertex3f(pos1.x, pos1.y, pos1.z);
        			glVertex3f(pos2.x, pos2.y, pos2.z);
        		glEnd();
        	}
        	
        	
        }
 
}