package fcampos.rawengine3D.fps;





/* Standard imports.
 */



/* Static imports.
 */



/** A fps counter.
 */

public final class FPSCounter
{


	  /** Frame interval. **/
	public static float frameInterval = 0;

	  /** Last frame's time. **/
	//private static long frameTime = 0;

	  /** Hold the time from the last frame. **/
	//private static long lastTime = 0;

	  /** Fps number. **/
	private static int fps = 0;

	  /** Old fps number. **/
	private static int old_fps = 0;
	
	private static float timeAccum = 0;


 
	/** FPSCounter constructor. Cannot be instantiated.
	 */

	private FPSCounter()
	{
	}



	/** Calculate the frame rate.
	 *  @return The FPS number.
	 */
	
	/*
	public final static int get()
	{
		long currentTime = Sys.getTime();	
		
		

		frameInterval = ((float)(currentTime - frameTime)) / 1000;
		frameTime = currentTime;

		fps++;

		if(currentTime - lastTime > Sys.getTimerResolution())
		{
			lastTime = currentTime;
			old_fps = fps;
			fps = 0;
			return fps;
		}
		else
		{
			return old_fps;
		}
	}
	*/
	public final static void update(float elapsedTime)
	{
		
		
		timeAccum += elapsedTime;

				
		fps++;

		if(timeAccum  > 1)
		{
			timeAccum = 0;
			old_fps = fps;
			fps = 0;
			
		}
		
	}
	
	public final static int get()
	{
		return old_fps;
	}
	

}