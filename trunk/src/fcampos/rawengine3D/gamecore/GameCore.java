package fcampos.rawengine3D.gamecore;



import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.util.glu.GLU.gluPerspective;



import java.io.IOException;
import org.lwjgl.Sys;

import fcampos.rawengine3D.fps.FPSCounter;
import fcampos.rawengine3D.graficos.Frustum;
import fcampos.rawengine3D.graficos.ScreenManager;
import fcampos.rawengine3D.input.GameAction;





/*
 * Está é uma classe abstrata e nela é onde criaremos nossa primeira janela e nosso primeiro
 * loop que será usado para fazer animações, simulações, enfim, qualquer atualização de objetos.
 * Os métodos, funções, variáveis estão com os nomes em inglês devido a facilidade e integração
 * na programação, pois todo material de pesquisa está em inglês, mas se alguém tiver dificuldade
 * é só me avisar.
 */

public abstract class GameCore {

	protected ScreenManager screen; //Cria um objeto do Tipo ScreenManager para gerenciar nossa janela
	protected boolean isRunning;    //Variável que controla o início e o término do programa
	protected float elapsedTime;	//Variável que controla o variação de tempo que o programa irá iterar
	
    // Cria ações para interagir com o programa     
	public GameAction pause;
    public GameAction exit; 
    public GameAction fullScreen;
    public static Frustum gFrustum = new Frustum();
    private boolean paused; //Variável para controlar quando a simulação irá pausar

    
    
    // Método principal: Responsável pela inicialização e fluxo do programa
	public void run() 
	{
		try
       {
            init();
            gameLoop();
        
       }catch(IOException e)
        {
           e.printStackTrace();
           Sys.alert("Erro", "Falhou: "+e.getMessage());
        }
       
	}
	
    protected void createGameActions()
    {
       	/* Cria dois objetos GameAction passando como parâmetro o nome da Ação, o tipo de acionamento
       	 * de tecla, que nesse caso só detectará o clique inicial e a tecla que será utilizada para a ação.
       	 */
    	pause = new GameAction("pause", GameAction.DETECT_INITIAL_PRESS_ONLY, Keyboard.KEY_P);
      	exit = new GameAction("Exit", GameAction.DETECT_INITIAL_PRESS_ONLY, Keyboard.KEY_ESCAPE);
        fullScreen  = new GameAction("FullScreen", GameAction.DETECT_INITIAL_PRESS_ONLY, Keyboard.KEY_F1);
     }
    
    
	
	//Cria a janela e define todos os estados iniciais do OpenGL;
	protected void init() throws IOException
	{
		screen = new ScreenManager(800, 600, 32); // Inicializa o objeto e define a janela com Resolução(800X600 e 32bits);
		setFullScreen(false); // Define a janela para não abrir em FullScreen e sim no modo Window
		screen.create(); // Cria janela
		
		
		/*
		 * A próxima linha habilita o shade suave. Shade suave mistura muito bem as cores e suaviza a iluminação
		 */
		glShadeModel(GL_SMOOTH); // Habilita Smooth Shading		
		
		
		/*
		 * As linhas a seguir definem a cor da janela quando ela sofre a operação de limpeza. Se você não sabe como
		 * as cores funcionam no OpenGL, eu explicarei rapidamente. Os valores das cores variam de 0.0f até 1.0f.
		 * 0.0f sendo o mais escuro e 1.0f o mais claro. O primeiro parâmetro depois do glClearColor é a intensidade
		 * de Vermelho, o segundo parametro é o Verde e o terceiro parametro é o Azul, assim formando o formato de cores
		 * RGB(Red-Vermelho, Green-Verde e Blue-Azul). Quanto mais próximo um número chegar de 1.0f, mais brilhante ou mais 
		 * clara será aquela cor específica. O último e quarto número é um valor Alpha.
		 * Quando fazemos operação de limpeza da janela, não devemos nos preocupar com esse quarto valor. Por agora
		 * deixe ele em 0.0f. Eu explicarei seu uso em outro tutorial.
		 */
		
		/*
		 * Você pode criar diversas cores, apenas misturando as 3 cores primárias aditivas(Vermelho, Verde e Azul).
		 * Então, se você tem glClearColor(0.0f,0.0f,1.0f,0.0f), você pode limpar a janela com a cor Azul. Se você tem
		 * glClearColor(0.5f,0.0f,0.0f,0.0f) você pode limpar a janela com um Vermelho médio. Nem claro(1.0f) e nem escuro(0.0f).
		 * Para fazer um fundo de cor branca, você pode definir todas as cores para o máximo possível(1.0f). Para fazer 
		 * um fundo de cor preta, você pode definir todas as cores para o mínimo possível(0.0f).
		 */
				
		glClearColor(0.0f, 0.0f, 0.0f, 1.0f); //Limpa o fundo com a cor preta
		
		
		/*
		 * As próximas 3 linhas tem haver como o Depth Buffer(Buffer de profundidade). Pense no Depth Buffer como camadas dentro
		 * da janela. O Depth Buffer controla o quão profundo os objetos estão na tela. Nós realmente não usaremos depth buffer
		 * neste tutorial, mas todos os programas em OpenGL, que desenham em 3D na tela usarão depth buffer. Ele classifica qual
		 * objeto será desenhado primeiro, assim um quadrado que você desenhou atrás de um círuclo não aparecerá na frente dele.
		 * O depth buffer é uma parte muito importante do OpenGL.
		 */
		
		glClearDepth(1.0); 			// Define Depth Buffer
		glEnable(GL_DEPTH_TEST); 	// Habilita teste de Profundidade
        glDepthFunc(GL_LEQUAL); 	// Tipo de teste de profundidade que irá fazer
        
        
        
        /*
         *  As linhas a seguir configuram a janela para ser visualizada em modo de perspectiva. Significa que
         *  quanto maior a distância do observador, menores os objetos ficarão. Isto cria uma cena com aspecto muito 
         *  realista.
         *  A perspectiva neste caso é calculada com um ângulo de visibilidade de 45 graus baseado na largura e altura da
         *  janela.
         *  As distâncias 0.1f e 100.0f, são respectivamente o ponto inicial e o ponto final, de quão distante
         *  podemos desenhar dentro da janela.
         *  
         *  glMatrixMode(GL_PROJECTION)indica que as próximas 2 linhas de código afetará a matriz de projeção.
         *  A matriz de projeção é responsável por adicionar perspectiva para nossa cena. glLoadIdentity() é similar
         *  a um "reset", um reinício. Ele restaura a matriz selecionada, nesse caso a matriz de projeção, para
         *  o seu estado inicial. Depois de chamar glLoadIdentity(), nós configuraremos a perspectiva para nossa cena.
         *  
         *  glMatrixMode(GL_MODELVIEW) indica que qualquer transformação(translação, rotação ou combinação dos dois) 
         *  afetará a matriz ModelView.
         *  A matriz ModelView é onde as informações referente aos objetos são armazenadas. Finalmente nós reiniciamos 
         *  nossa matriz ModelView.
         */
        
               
		glMatrixMode(GL_PROJECTION); // Selecione a Matrix de Projeção
		glLoadIdentity(); //Reinicia a Matriz de Projeção		
		
		// Calcula a proporção da Janela
		gluPerspective(45.0f, screen.getWidth() / screen.getHeight(), 0.1f, 500.0f);
		glMatrixMode(GL_MODELVIEW); // Selecione a Matrix de ModelView
		glLoadIdentity(); //Reinicia a Matriz ModelView		
		
		
		/*
		 * Na linha abaixo nós diremos a OpenGL que nós queremos que a melhor correção de perspectiva seja feita. Isto causa
		 * uma pequenina queda de performance, mas faz com que a visão da perspectiva seja muito melhor.
		 */
		
		glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST); // Define para ótimos cálculos de perspectiva		
		
		
		isRunning = true; //Define início do programa para verdadeiro
		
		createGameActions(); // Cria ações para interagirmos com o programa(Este classe executa duas ações;
		 // 1- Quando clicamos em F1 o programa troca o estado para FullScreen(Tela cheia) ou modo janela.
		 // 2- Quando clicamos na tecla ESC, o programa termina.
	
     }
	
	//Método que finaliza o programa
	protected void stop()
    {
        isRunning = false;
        Display.destroy();
		System.exit(0);
    }
	
	//Método para definir FullScreen(Janela cheia) ou não.
	protected void setFullScreen(boolean fullscreen)
	{
		 screen.setFullScreen(fullscreen);
		 		
	}
		
	//Método que verifica se a Janela está em FullScreen(Tela cheia)
	protected boolean isFullScreen()
	{
		 return screen.isFullscreen();
	}

	//Método que verifica se o programa está no modo pause 
	protected boolean isPaused()
	{
		 return paused;
	}

	//Método para entrar ou sair do modo pause, a função do método depende do estado do programa, pausado ou rodando
	protected void setPaused()
	{
		 paused = !paused;
	}
	
	
	//Looping do programa, depois de iniciado só terminará ao chamar o método stop()		
	public void gameLoop() 
	{
	    	long startTime = Sys.getTime(); //variável que recebe o tempo inicial
			long currTime = startTime; //variável que recebe o tempo corrente(inicialmente igual ao tempo inicial)
			
			
		//loop principal	
		while (isRunning) 
		{
			// calcula o quanto passou desde que nós entramos neste loop
			// e guarda este valor para que as rotinas de update(atualizar)
			// e render(desenhar) saibam quanto tempo tem para atualização de desenho.
						
			elapsedTime = (float)(Sys.getTime() - currTime);
			
			elapsedTime /= 1000; // tempo em  milisegundos
			FPSCounter.update(elapsedTime);
			
			//System.out.println(elapsedTime);
			currTime = Sys.getTime(); //recebe o tempo corrente do sistema
			update(elapsedTime);
			
			
			render(); //chama método desenhar
			 
			
			
			// finalmente dizemos para o Display fazer uma atualização.
			// agora que nós desenhamos toda nossa cena, nós apenas atualizamos ela
			// na janela.
			
			screen.update();
			
			
			// Se o usuário fechar a janela, apertando CTRL+F4, ou clicando no
			// no botão de fechar então nós precisamos parar o looping e terminar o programa.
			if (screen.isCloseRequested()) 
			{
				stop();
			}
		}
		
	}
	
	//Método básico que é usado para atualizar estados de objetos. Para uma maior utilidade, deve ser sobreecrito
	//numa classe filha.
    protected void update(float elapsedTime)
    {
       	checkSystemInput();
    	checkGameInput();
    }

    // Verifica se a ação de saída foi acionada
    protected void checkSystemInput()
    {
    	if (pause.isPressed())
        {
            setPaused();
        }
      	if (exit.isPressed())
    	{
    		stop();
    	}
    }
        
    /* Verifica se as teclas utilizadas nos nossos programas forma pressionadas
     * Este método deve ser sobreescrito em classes filhas de acordo com a funcionalidade do programa a ser escrito.
     */
    protected void checkGameInput()
	{
    	if (fullScreen.isPressed())
    	{
    		setFullScreen(!isFullScreen());
    	}
	}

	
	//Método abstrato, obrigando a ser implementado numa classe filha. Deve conter a rotina de desenho.
	protected abstract void render();
	

}