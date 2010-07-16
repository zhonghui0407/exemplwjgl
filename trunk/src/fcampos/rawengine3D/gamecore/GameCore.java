package fcampos.rawengine3D.gamecore;

import org.lwjgl.opengl.Display;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.util.glu.GLU.gluPerspective;

import java.io.IOException;
import org.lwjgl.Sys;

import fcampos.rawengine3D.graficos.*;



/*
 * Está é uma classe abstrata e nela é onde criamos nossa primeira janela e nosso primeiro
 * loop que será usado para fazer animações, simulações, enfim, qualquer movimento com objetos.
 * Os métodos, funções, variáveis estão com os nomes em inglês devido a facilidade e integração
 * na programação, pois todo material de pesquisa está em inglês.
 */

public abstract class GameCore {

	protected ScreenManager screen; //Cria um objeto do Tipo ScreenManager para gerenciar nossa janela
	protected boolean isRunning;    //variável que controla o início e o término do programa
	protected float elapsedTime;	//variável que controla o dt(delta de tempo) que o programa irá iterar

     
 // Este é o objeto global Frustum que manterá nosso Frustum durante a execução do programa
    public static Frustum gFrustum = new Frustum();
    
    private boolean paused; //variável para controlar quando a simulação irá pausar

    
    //Método principal: Responsável pela inicialização e fluxo do programa
	protected void run() 
	{
		try
       {
            init();
            gameLoop();
        
       }catch(IOException e)
        {
           e.printStackTrace();
           Sys.alert("Error", "Failed: "+e.getMessage());
        }
       
	}
	
	//Cria a janela e seta todos os estados iniciais do OpenGL;
	protected void init() throws IOException
	{
		screen = new ScreenManager(800, 600, 32); //Inicializa o objeto e seta a janela com Resolução(800X600 e 32bits);
		setFullScreen(false); //Seta a janela para não abrir em FullScreen e sim no modo Window
		screen.create(); //Cria janela
		
		glEnable(GL_TEXTURE_2D); // Habilita Texture Mapping
        glShadeModel(GL_SMOOTH); // Habilita Smooth Shading		
		glClearColor(0.0f, 0.0f, 0.0f, 1.0f); //Limpa o fundo com a cor preta
		glClearDepth(1.0); // Seta Depth Buffer
		glEnable(GL_DEPTH_TEST); // Habilita teste de Profundidade
        glDepthFunc(GL_LEQUAL); // Tipo de teste de profundidade que irá fazer
        
		glMatrixMode(GL_PROJECTION); // Selecione a Matrix de Projeção
		glLoadIdentity(); //Reseta a Matrix de Projeção		
		
		// Calcula a proporção da Janela
		gluPerspective(45.0f, screen.getWidth() / screen.getHeight(), 1.0f, 1000.0f);
		glMatrixMode(GL_MODELVIEW); // Selecione a Matrix de ModelView
		
		// Seta ótimos calculos de perspectiva		
		glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);
		
		isRunning = true; //Seta início do programa para verdadeiro
	
     }
	
	//Método que finaliza o programa
	public void stop()
    {
        isRunning = false;
        Display.destroy();
		System.exit(0);
    }
	
	//Método para setar FullScreen(Janela cheia) ou não.
	protected void setFullScreen(boolean fullscreen)
	{
		 screen.setFullScreen(fullscreen);
		 		
	}
		
	//Método que checa se a Janela está em FullScreen
	protected boolean isFullScreen()
	{
		 return screen.isFullscreen();
	}

	//Método que checa se o programa está no modo pause 
	protected boolean isPaused()
	{
		 return paused;
	}

	//Métodos para entrar ou sair do modo pause, a função do método depende do estado do programa
	//pausado ou rodando
	protected void setPaused()
	{
		 paused = !paused;
	}
	
	
	//Looping do programa, depois de iniciado só terminará ao chamar o método stop()		
	public void gameLoop() 
	{
	    	long startTime = Sys.getTime(); //variável que recebe o tempo inicial
			long currTime = startTime; //variável que recebe o tempo corrente(inicialmente igual o tempo inicial)
			
			
		//loop principal	
		while (isRunning) 
		{
			// calcula o quanto passou desde que nós entramos neste loop
			// e guarda este valor para que as rotinas de update(atualizar)
			// e render(desenhar) saibam quanto tempo tem para atualização de desenho.
						
			elapsedTime = (float)(Sys.getTime() - currTime);
			currTime += elapsedTime;
			
			elapsedTime /= 1000;
			
			update(elapsedTime);
			
			currTime = Sys.getTime(); //recebe o tempo corrente do sistema
			
			render(); //chama método desenhar
			 
			// finalmente dizemos para o Display fazer uma atualização.
			// agora que nós desenhamos toda nossa cena, nós apenas atualizamos ela
			// na janela.
			
			Display.update();
			
			// Se o usuário fechar a janela, apertando CTRL+F4, ou clicando no
			// no botão de fechar então nós precisamos para o loop e terminar o programa.
			
			if (Display.isCloseRequested()) {
				stop();
			}
		}
		
	}
	
	//Método que pode ser usado para atualizar estados de objetos(Não implementado nesta classe)
	protected void update(float elapsedtime){}; 
	
	//Método abstrato, obrigando a ser implementado numa classe filha. Deve conter a rotina de desenho.
	protected abstract void render();
	
	//Método para desenhar o fundo da janela(Programas 2D)	
	protected void drawBackground(Texture background, int x, int y)
	{
		
		screen.enterOrtho(); // Entra no modo Ortogonal(2D)
		glPushMatrix(); // Empilha a Matrix
		
        background.bind(); // Vincula a Textura de fundo

		glTranslatef(x, y, 0); //Seta a posição inicial da textura
		glBegin(GL_QUADS); //Desenha um polígono de 4 lados
			glTexCoord2f(0,0); // Seta a coordenada de textura(canto superior esquerdo)
			glVertex2i(0,0); // Seta a coordenada de posição(canto superior esquerdo)
			glTexCoord2f(0,1);// Seta a coordenada de textura(canto inferior esquerdo)
			glVertex2i(0,screen.getHeight()); // Seta a coordenada de posição(canto inferior esquerdo)
			glTexCoord2f(1,1); // Seta a coordenada de textura(canto inferior direito)
			glVertex2i(screen.getWidth(),screen.getHeight()); // Seta a coordenada de posição(canto inferior direito)
			glTexCoord2f(1,0); // Seta a coordenada de textura(canto superior direito)
			glVertex2i(screen.getWidth(),0); // Seta a coordenada de posição(canto superior direito)
		glEnd(); //fim do polígono
		
        glPopMatrix(); //Desempilha a Matrix
		
        screen.leaveOrtho(); //Sai do modo Ortogonal(2D)
	}
	
	/*
 // Calcula e retorna a taxa de quadros por segundo
    protected int CalculaQPS()
    {
    	// Incrementa o contador de quadros
    	numquadro++;

    	// Obtém o tempo atual
    	float tempo = getElapsedTime();
    	// Verifica se passou mais um segundo
    	if (tempo - tempoAnterior > 1000)
    	{
    		// Calcula a taxa atual
    		ultqps = numquadro*1000.0f/(tempo - tempoAnterior);
    		// Ajusta as variáveis de tempo e quadro
    	 	tempoAnterior = tempo;
    		numquadro = 0;
    	}
    	// Retorna a taxa atual
    	return (int)ultqps;
    }
*/
}
