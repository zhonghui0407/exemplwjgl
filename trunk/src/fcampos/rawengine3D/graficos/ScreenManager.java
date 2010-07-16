package fcampos.rawengine3D.graficos;

import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import static org.lwjgl.opengl.GL11.*;


/**
 * A window to display the game in LWJGL.
 * 
 * @author Kevin Glass
 */
public class ScreenManager {
	
	private DisplayMode mode; 
	
	/**
	 * Create a new game window
	 */
	public ScreenManager(int width, int height, int bpp) {
		
		setDisplayMode(width, height, bpp);
			}
			
	public ScreenManager() 
	{
		setDisplayMode(getDesktopDisplayMode().getWidth(), getDesktopDisplayMode().getHeight(), getDesktopDisplayMode().getBitsPerPixel());
	}

	public DisplayMode[] getAvailableDisplayModes() throws LWJGLException
	{
		return  Display.getAvailableDisplayModes();	
	}
	
	public void setTitle(String title)
	{
		Display.setTitle(title);
	}
	
	public void setDisplayMode(int width, int height, int bpp)
	{
		try {
			// find out what the current bits per pixel of the desktop is
			//int currentBpp = Display.getDisplayMode().getBitsPerPixel();
			// find a display mode at 800x600
			 mode = findDisplayMode(width, height, bpp);
                    
			
			 //if can't find a mode, notify the user the give up
			if (mode == null) {
				Sys.alert("Error", +width+ "x" +height+ "x" +bpp+ " display mode unavailable");
				return;
			}
			
			Display.setDisplayMode(mode);		
			
		} catch (LWJGLException e) {
		       
                        e.printStackTrace();
			Sys.alert("Error", "Failed: "+e.getMessage());
		}
	}
	
	public void setFullScreen(boolean t)
	{
		try{

		Display.setFullscreen(t);
		
		}catch (LWJGLException e) {
		       
                        e.printStackTrace();
			Sys.alert("Error", "Failed: "+e.getMessage());
		}
	}
	
	public void restoreScreen()
	{
		try{

		Display.setFullscreen(false);
		
		}catch (LWJGLException e) {
		       
                        e.printStackTrace();
			Sys.alert("Error", "Failed: "+e.getMessage());
		}
	}
	
	public void create()
	{
		try{

		Display.create();
		
		}catch (LWJGLException e) {
		       
                        e.printStackTrace();
			Sys.alert("Error", "Failed: "+e.getMessage());
		}
	}
	
	
	public DisplayMode getCurrentDisplayMode()
	{
		return Display.getDisplayMode();
	}
	
	
	public DisplayMode getDesktopDisplayMode()
	{
		return Display.getDesktopDisplayMode();
	}
	
	
	public void update()
	{
		Display.update();
	}
	
	public void close()
	{
		if (Display.isCloseRequested())
		System.exit(0);
	}
	
	private DisplayMode findDisplayMode(int width, int height, int bpp) throws LWJGLException {
		DisplayMode[] modes = Display.getAvailableDisplayModes();
		DisplayMode mode1 = null;
		
		for (int i=0;i<modes.length;i++) {
			if ((modes[i].getBitsPerPixel() == bpp) || (mode1 == null)) {
				if ((modes[i].getWidth() == width) && (modes[i].getHeight() == height)) {
					mode1 = modes[i];
				}
			}
		}
		
		return mode1;
	}
	
	public boolean isFullscreen() 
	{
		return Display.isFullscreen();
	}
	public boolean isDirty()
	{
		return Display.isDirty();
	}
	
	public void setVSyncEnabled(boolean t)
	{
		Display.setVSyncEnabled(t);
	}
	
	public int getWidth()
	{
		return mode.getWidth();
	}
	
	public int getHeight()
	{
		return mode.getHeight();
	}
	
    public void enterOrtho() 
    {
		// store the current state of the renderer
		glPushAttrib(GL_DEPTH_BUFFER_BIT | GL_ENABLE_BIT);
		glPushMatrix();
		glLoadIdentity();
		glMatrixMode(GL_PROJECTION); 
		glPushMatrix();	
		
		// now enter orthographic projection
		glLoadIdentity();		
		glOrtho(0, getWidth(), getHeight(), 0, -1, 1);		
		glDisable(GL_DEPTH_TEST);
		glDisable(GL_LIGHTING);  
	}

    public void leaveOrtho() 
    {
		// restore the state of the renderer
		glPopMatrix();
		glMatrixMode(GL_MODELVIEW);
		glPopMatrix();
		glPopAttrib();
	}

}
