package fcampos.rawengine3D.gamecore;

import org.lwjgl.opengl.Display;
import java.io.IOException;
import org.lwjgl.Sys;

public abstract class FisicaGameCore extends GameCore {
	
		
		
		private float elapsedTime;
	    protected float speed = 500.0f;
	    protected float slowMotionRatio = 1.0f;
	    protected float timeElapsed = 0;



			
		public void init() throws IOException
		{
			super.init();
			
	    }
		
				
		public void gameLoop() 
		{
		    	long startTime = Sys.getTime();
				long currTime = startTime;
				
				
				
			while (isRunning) {
				// calculate how long it was since we last came round this loop
				// and hold on to it so we can let the updating/rendering routine
				// know how much time to update by
				
				elapsedTime = (float)(Sys.getTime() - currTime);
				
				long ticks = Sys.getTimerResolution();
				System.out.println(ticks);				    	
		    	float dt = elapsedTime / ticks;							// Let's Convert Milliseconds To Seconds
		    	
		    	if (dt == 0 || dt < 0)
		    	{
		    		dt = 0.001f;
		    	}
		    	
				dt /= slowMotionRatio;										// Divide dt By slowMotionRatio And Obtain The New dt

				timeElapsed += dt;											// Iterate Elapsed Time

		    	float maxPossible_dt = 0.002f;								// Say That The Maximum Possible dt Is 0.1 Seconds
				

		    	int numOfIterations = (int)(dt / maxPossible_dt) + 1;		// Calculate Number Of Iterations To Be Made At This Update Depending On maxPossible_dt And dt
		    	if (numOfIterations != 0)									// Avoid Division By Zero
		    	{
		    		dt = dt / numOfIterations;
		    	}

								
				update(numOfIterations, dt);
				currTime = Sys.getTime();
				render();
				 
				// finally tell the display to cause an update. We've now
				// rendered out scene we just want to get it on the screen
				// As a side effect LWJGL re-checks the keyboard, mouse and
				// controllers for us at this point
				Display.update();
				
				// if the user has requested that the window be closed, either
				// pressing CTRL-F4 on windows, or clicking the close button
				// on the window - then we want to stop the game
				if (Display.isCloseRequested()) {
					isRunning = false;
					Display.destroy();
					System.exit(0);
				}
			}

		}
		

		
	 public void update(float numOfIterations, float dt){}; 
		
	 public abstract void render();
		
			
}
