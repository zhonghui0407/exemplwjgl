package fcampos.rawengine3D.graficos;

import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import static org.lwjgl.opengl.GL11.*;


/**
 * Uma janela para mostrar e desenhar objetos em LWJGL.
 * 
 * 
 */
public class ScreenManager {
	
	private DisplayMode mode; 
	
	
	/**
	 * Cria um objeto e seta valores de largura, altura e bits de cores
	 */
	public ScreenManager(int width, int height, int bpp) 
	{
		setDisplayMode(width, height, bpp);
	}
		
	/**
	 * Cria um objeto com valores de largura, altura, bits de cores, que você está usando na hora da execução.
	 */
	public ScreenManager() 
	{
		setDisplayMode(getDesktopDisplayMode().getWidth(), getDesktopDisplayMode().getHeight(), getDesktopDisplayMode().getBitsPerPixel());
	}

	
	/**
	 *  Retorna uma lista de DisplayMode que estão disponíveis no computador.
	 */
	public DisplayMode[] getAvailableDisplayModes() throws LWJGLException
	{
		return  Display.getAvailableDisplayModes();	
	}
	
	/**
	 * 
	 * Seta título da janela
	 */
	
	public void setTitle(String title)
	{
		Display.setTitle(title);
	}
	
	
	/**
	 * Seta ou muda parâmetros de configuração da janela.
	 * @param width
	 * @param height
	 * @param bpp
	 */
	public void setDisplayMode(int width, int height, int bpp)
	{
		try {
			
			//procura se existe uma configuração com os parâmetros de entrada
			 mode = findDisplayMode(width, height, bpp);
                    
			
			 
			 //se não achar nenhuma configuração, notifica o usuário que a configuração não existe.
			if (mode == null) {
				Sys.alert("Error", +width+ "x" +height+ "x" +bpp+ " display mode unavailable");
				return;
			}
			
			//se existir, seta o display para a configuração de entrada
			Display.setDisplayMode(mode);		
			
		} catch (LWJGLException e) {
		       
                        e.printStackTrace();
			Sys.alert("Error", "Failed: "+e.getMessage());
		}
	}
	
	/**
	 * Seta janela para modo Tela cheia
	 * @param t
	 */
	public void setFullScreen(boolean t)
	{
		try{

		Display.setFullscreen(t);
		
		}catch (LWJGLException e) {
		       
                        e.printStackTrace();
			Sys.alert("Error", "Failed: "+e.getMessage());
		}
	}
	
	
	/**
	 *  Volta a janela ao estado anterior, saindo do modo tela cheia
	 */
	public void restoreScreen()
	{
		try{

		Display.setFullscreen(false);
		
		}catch (LWJGLException e) {
		       
                        e.printStackTrace();
			Sys.alert("Error", "Failed: "+e.getMessage());
		}
	}
	
	/**
	 *  Cria a janela. Deve ser chamado depois da janela configurada.
	 */
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
	
	
	public boolean isCloseRequested()
	{
		if(Display.isCloseRequested())
		{
			return true;
		}
		
		return false;
		
	}
	
	/**
	 * Procura se os parâmetros de entrada são iguais as configurações existentes na placa de video.
	 * @param width
	 * @param height
	 * @param bpp
	 * @return
	 * @throws LWJGLException
	 */
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
	
	/**
	 * Janela está em modo tela cheia
	 * @return
	 */
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
	
	/**
	 * Retorna a largura da janela
	 * @return
	 */
	public int getWidth()
	{
		return mode.getWidth();
	}
	
	
	/**
	 * Retorna a altura da janela
	 * @return
	 */
	public int getHeight()
	{
		return mode.getHeight();
	}
	
	/**
	 *  Entra no mode 2D
	 */
    public void enterOrtho() 
    {
    	// Armazena o estado corrente dos buffers de desenho
    	glPushAttrib(GL_ALL_ATTRIB_BITS);
    	    	
		glPushMatrix(); //empilha a matriz
		glLoadIdentity(); // reinicia a matriz
		glMatrixMode(GL_PROJECTION); // entra na matriz de projeção
		glPushMatrix();	 // empilha a matriz de projeção
		
		// Agora entra na projeção ortogonal
		glLoadIdentity(); // reinicia a matriz		
		glOrtho(0, getWidth(), getHeight(), 0, -1, 1); // parâmetros de inicialização do modo 2D	
		glDisable(GL_DEPTH_TEST); // desabilita teste de profundidade - necessário por ser 2D
		glDisable(GL_LIGHTING);  // desabilita Luz - necessário por ser 2D
	}

    public void leaveOrtho() 
    {
		// Restaura o estado corrente dos buffers de desenho
		glPopMatrix(); // desempilha matriz
		glMatrixMode(GL_MODELVIEW); // entra na matriz ModelView
		glPopMatrix(); // desempilha matriz 
		glPopAttrib();
		
	}
    
	//Método para desenhar o fundo da janela(Programas 2D)	
	public void drawBackground(Texture background, int x, int y)
	{
		
		glMatrixMode(GL_MODELVIEW);
        glLoadIdentity ();						// Reset The Modelview Matrix

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);   // Clears the screen.
        
		enterOrtho(); // Entra no modo Ortogonal(2D)
		glPushMatrix(); // Empilha a Matriz
		
        background.bind(); // Vincula a Textura de fundo

		//glTranslatef(x, y, 0); //Seta a posição inicial da textura
		glBegin(GL_QUADS); //Desenha um polígono de 4 lados
			glTexCoord2f(0,0); // Seta a coordenada de textura(canto superior esquerdo)
			glVertex2i(0,0); // Seta a coordenada de posição(canto superior esquerdo)
			glTexCoord2f(0,background.getHeight());// Seta a coordenada de textura(canto inferior esquerdo)
			glVertex2i(0,getHeight()); // Seta a coordenada de posição(canto inferior esquerdo)
			glTexCoord2f(background.getWidth(),background.getHeight()); // Seta a coordenada de textura(canto inferior direito)
			glVertex2i(getWidth(),getHeight()); // Seta a coordenada de posição(canto inferior direito)
			glTexCoord2f(background.getWidth(),0); // Seta a coordenada de textura(canto superior direito)
			glVertex2i(getWidth(),0); // Seta a coordenada de posição(canto superior direito)
		glEnd(); //fim do polígono
		
        glPopMatrix(); //Desempilha a Matriz
		
        leaveOrtho(); //Sai do modo Ortogonal(2D)
	}
	

}
