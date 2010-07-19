package fcampos.rawengine3D.teste;
import fcampos.rawengine3D.input.*;
import fcampos.rawengine3D.model.T3dModel;
import fcampos.rawengine3D.model.TMaterialInfo;
import fcampos.rawengine3D.model.TObjectLoader;
import fcampos.rawengine3D.resource.*;
import fcampos.rawengine3D.gamecore.GameCore;
import fcampos.rawengine3D.graficos.*;
import fcampos.rawengine3D.MathUtil.*;
import fcampos.rawengine3D.fps.*;

import java.io.*;
import java.nio.FloatBuffer;


import org.lwjgl.BufferUtils;
import org.lwjgl.input.*;
import org.lwjgl.opengl.EXTTextureFilterAnisotropic;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.util.glu.GLU.*;

import java.util.ArrayList;
//*****************************************************
//
// Sala3D.java
// Um programa OpenGL que desenha uma sala de aula
// completa, utilizando texturas e transparência, otimizado
// com o uso de listas de exibição
//
// Controles de navegação:
//
// a,z - avança/recua observador
// seta superior/inferior - anda para frente e para tras
// CTRL+seta esquerda/seta direita - anda para os lados
// 
// Controles de Iluminação/Rendering em geral:
// 1,2,3 - acende/apaga conjunto de luzes no teto (frente, meio, fundos)
// 4 - acende/apaga luz externa ("Sol")
// 5 - acende/apaga luz do observador
// t - chaveia entre os modos de desenho: wireframe, sólidos e com textura
// m - chaveia entre o modo GL_REPLACE e GL_MODULATE para as texturas
// n - ativa/desativa a neblina
// f - chaveia entre os diversos filtros de textura
// F1 - chaveia entre FullScreen e Window
// F2 - seta Anisotropic para Textura

/**
 *
 * @author Fabio
 */
public class TSala3D extends GameCore {

    public static void main(String[] args)
    {
        new TSala3D().run();
    }
    private static final float LOW = 0.3f;
    
    // Posição Z da fila de mesas no fundo
    private static final float Z_INI = -320f;

    // Posição X da primeira columa de mesas
    private static final float X_INI = -220f;

    // Variáveis para controle da projeção
    private float fAspect;
    private float angcam;

    // Define as texturas 2D
    private Texture parede;
    private Texture chao;
    private Texture teto;

    
    public GameAction moveLeft;
    public GameAction moveRight;
    public GameAction moveUp;
    public GameAction moveDown;
    public GameAction zoomIn;
    public GameAction zoomOut;
    public GameAction exit;
    public GameAction pause;
    public GameAction angCam;
    public GameAction solid;
    public GameAction fog;
    public GameAction modTex;
    public GameAction fullScreen;
    public GameAction[] lights = new GameAction[5]; 
    public GameAction saveCamera;
    public GameAction restoreCamera;
    public GameAction anisotropic;
    
    
    public InputManager inputManager;
    public TextureManager texManager;
    private DrawString draw;
    
    // Objetos
    private T3dModel plano = new T3dModel();
    private T3dModel mesa = new T3dModel();
    private T3dModel mesapeq = new T3dModel();
    private T3dModel quadro = new T3dModel();
    private T3dModel porta = new T3dModel();
    private T3dModel janela = new T3dModel();
    private T3dModel lamp = new T3dModel();
    private T3dModel vidro = new T3dModel();
    private T3dModel ceu = new T3dModel();
    private T3dModel cadeira = new T3dModel();
    private T3dModel arena = new T3dModel();
    
    private float speed = 500.0f;
      
    
    // Tipos possíveis para objetos sobre as mesas
    private T3dModel[] tipos = new T3dModel[7];
    
    private ArrayList<T3dModel> objetos = new ArrayList<T3dModel>();
    
        
    // Apontador para material da fonte de luz
    private TMaterialInfo mat_luz;

    private Vector3f[] limitesMesa = new Vector3f[37];
    
    // Filtros de textura
    //private int filtros[] = {GL_NEAREST, GL_LINEAR, GL_NEAREST_MIPMAP_NEAREST,GL_LINEAR_MIPMAP_NEAREST,
    						 //GL_NEAREST_MIPMAP_LINEAR, GL_LINEAR_MIPMAP_LINEAR};

    
    public TObjectLoader lo = new TObjectLoader();
    
    public boolean paused;
    
    private float luzAmb1[] = { 0.1f, 0.1f, 0.1f, 1f };	// luz ambiente
    private float luzDif1[] = { LOW, LOW, LOW, 1.0f };	// luz difusa
    private float luzEsp1[] = { 0.0f, 0.0f, 0.0f, 1.0f };	// luz especular
    private float posLuz1[] = { 0, 200, 250, 1 };	// posição da fonte de luz
    // Luz 2: puntual no teto, meio da sala 
    private float luzDif2[] = { LOW, LOW, LOW, 1.0f };	// luz difusa
    private float posLuz2[] = { 0, 200, 0, 1 };	// posição da fonte de luz
    // Luz 3: puntual no teto, atrás
    private float luzDif3[] = { LOW, LOW, LOW, 1.0f };	// luz difusa
    private float posLuz3[] = { 0f, 200f, -250f, 1f };	// posição da fonte de luz
    // Luz 4: direcional, simulando o Sol 
    private float luzDif4[] = { 0.4f, 0.2f, 0.0f, 1.0f };	// luz difusa
    private float posLuz4[] = { 1f, 0.2f, 0f, 0f };		// direção da fonte de luz
    // Luz 5: spot, na posição da câmera, apontando para a frente
    private float luzDif5[] = { LOW*2, LOW*2, LOW*2, 1.0f };	// luz difusa
    private float posLuz5[] = { 0f, 0f, 0f, 1.0f };		// pos em relação à câmera
    private float dirLuz5[] = { 0f, 0f, -1f, 0.0f };		// aponta para a frente
    
    private boolean luzes[] = {true, true, true, true, false};
    
    private FloatBuffer posLuz1F;	// posição da fonte de luz
    private FloatBuffer posLuz2F;	// posição da fonte de luz
    private FloatBuffer posLuz3F;	// posição da fonte de luz
    private FloatBuffer posLuz4F;		// direção da fonte de luz
    private FloatBuffer posLuz5F;		// pos em relação à câmera
    private FloatBuffer dirLuz5F;		// aponta para a frente
    
    // Define filtro inicial como GL_NEAREST_MIPMAP_LINEAR
    //private int filtro = 4;

    // Define modo de desenho inicial: textura
    //private String modo_des = "t";

    // Arquivo de cena e arquivo de câmera
    private String arqcena = "sala3d.txt";
    private String arqcam = "camera.txt";

    
    private int modo = GL_MODULATE;

    private boolean fullscreen;
    
    private CameraQuaternion camera;
    
    @Override
    public void init() throws IOException
    {
        
    	super.init();
    	
    	screen.setTitle("TSala3D");
    	camera.setPosition(3000.0f, 1600.0f, -13000.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
        
        fAspect = screen.getWidth() / screen.getHeight();
        
        for(int i=0; i < limitesMesa.length; i++)
        {
        	limitesMesa[i] = new Vector3f();
        }
        
       for(int i=0; i < tipos.length; i++){
    	   tipos[i] = new T3dModel();
       }
       
       objetos.add(plano);
       objetos.add(mesa);
       objetos.add(mesapeq);
       objetos.add(quadro);
       objetos.add(porta);
       objetos.add(janela);
       objetos.add(lamp);
       objetos.add(vidro);
       objetos.add(ceu);
       objetos.add(cadeira);
       
       
        
        inputManager = new InputManager();
        texManager = lo.getTexManager();
        
        createGameActions();
        
        posLuz1F = Conversion.allocFloats(posLuz1);
        posLuz2F = Conversion.allocFloats(posLuz2);
        posLuz3F = Conversion.allocFloats(posLuz3);
        posLuz4F = Conversion.allocFloats(posLuz4);
        posLuz5F = Conversion.allocFloats(posLuz5);
        dirLuz5F = Conversion.allocFloats(dirLuz5);
        
        // Define a cor de fundo da janela de visualização como preto
    	glClearColor(0,0,0,1);
    	
    	// Carrega as texturas
    	parede = texManager.getNormalImage("texturas/revmur036.jpg",true, false);
    	chao   = texManager.getNormalImage("texturas/015eyong.jpg",true, false);
    	teto   = texManager.getNormalImage("texturas/revmur037.jpg",true, false);


    	// Seleciona o modo de aplicação da textura
    	glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, modo);

    	// Ajusta iluminação
    	glLight( GL_LIGHT0, GL_AMBIENT,  Conversion.allocFloats(luzAmb1)); 
    	glLight( GL_LIGHT0, GL_DIFFUSE,  Conversion.allocFloats(luzDif1));
    	glLight( GL_LIGHT0, GL_SPECULAR, Conversion.allocFloats(luzEsp1));
    	glLight( GL_LIGHT1, GL_AMBIENT,  Conversion.allocFloats(luzAmb1)); 
    	glLight( GL_LIGHT1, GL_DIFFUSE,  Conversion.allocFloats(luzDif2));
    	glLight( GL_LIGHT2, GL_AMBIENT,  Conversion.allocFloats(luzAmb1)); 
    	glLight( GL_LIGHT2, GL_DIFFUSE,  Conversion.allocFloats(luzDif3));
    	glLight( GL_LIGHT3, GL_AMBIENT,  Conversion.allocFloats(luzAmb1)); 
    	glLight( GL_LIGHT3, GL_DIFFUSE,  Conversion.allocFloats(luzDif4));
    	glLight( GL_LIGHT4, GL_AMBIENT,  Conversion.allocFloats(luzAmb1));
    	glLight( GL_LIGHT4, GL_DIFFUSE,  Conversion.allocFloats(luzDif5));

    	// Habilita todas as fontes de luz
    	glEnable(GL_LIGHT0);
    	glEnable(GL_LIGHT1);
    	glEnable(GL_LIGHT2);
    	glEnable(GL_LIGHT3);
    	glEnable(GL_LIGHT4);
    	glEnable(GL_LIGHTING);

    	// Define coeficientes ambiente e difuso
    	// do material
    	float matAmb[] = { 0.2f,0.2f,0.2f,1f };
    	float matDif[] = { 1f,1f,1f,1f };

    	// Material
    	glMaterial(GL_FRONT,GL_AMBIENT, Conversion.allocFloats(matAmb));
    	glMaterial(GL_FRONT,GL_DIFFUSE, Conversion.allocFloats(matDif));

    	// Seleciona o modo de GL_COLOR_MATERIAL
    	glColorMaterial(GL_FRONT, GL_DIFFUSE);
    	glEnable(GL_COLOR_MATERIAL);

    	// Seta modo de blending
    	glBlendFunc(GL_SRC_ALPHA,GL_ONE_MINUS_SRC_ALPHA);
     
    	// Habilita normalização automática
    	glEnable(GL_NORMALIZE);
    	
    	// Habilita Z-Buffer
    	glEnable(GL_DEPTH_TEST);

    	// Cor da neblina
    	float cor_neblina[] = {0.4f, 0.4f, 0.4f, 1f};

    	// Parâmetros da neblina
    	glFogi(GL_FOG_MODE, GL_EXP2);
    	glFog(GL_FOG_COLOR, Conversion.allocFloats(cor_neblina));
    	glFogf(GL_FOG_DENSITY, 0.0035f);

    	plano.load("parede1.obj",true, false, plano);
    	// Não queremos gerar display list para o plano,
    	// pois a textura varia de acordo com o objeto
    	// desenhado (parede, chão ou teto)
    	lo.desabilitaDisplayList(plano);
    	arena.load("arenaobj_Scene.obj", true, false, arena);
    	mesa.load("mesagrande1.obj",true, false, mesa);
    	mesapeq.load("mesapeq1.obj",true, false, mesapeq);
    	cadeira.load("cadeira.obj",true, false, cadeira);
    	quadro.load("quadronegro1.obj",true, false, quadro);
    	porta.load("porta1.obj",true, false, porta);
    	janela.load("janela1.obj",true, false, janela);
    	
    	lamp.load("lampada1.obj",true, false, lamp);
    	// Como o estado da lâmpada varia, não
    	// é interessante gerar display list para ela
    	lo.desabilitaDisplayList(lamp);
    	
    	vidro.load("vidro1.obj",true, false, vidro);
    	ceu.load("ceu2.obj",true, false, ceu);

    	// Carrega objetos que podem estar sobre as mesas
    	tipos[0].load("lapis1.obj",true, false, tipos[0]);
    	tipos[1].load("livro1.obj",true, false, tipos[1]);
    	tipos[2].load("papel11.obj",true, false, tipos[2]);
    	tipos[3].load("papel21.obj",true, false, tipos[3]);
    	tipos[4].load("papel31.obj",true, false, tipos[4]);
    	tipos[5].load("cuiabomba1.obj",true, false, tipos[5]);
    	tipos[6].load("borracha1.obj",true, false, tipos[6]);
    	
    	// Cria todas as display lists, exceto para
    	// as lâmpadas e para o plano
    	lo.criaDisplayList(mesa, null);
        paused = false;
        fullscreen = false;
        draw = new DrawString("texturas/font.png");
        // Preenche o array com o centro de cada mesa
    	defineLimitesMesas();

    	// Recupera apontador para o material das lâmpadas
    	// (usado durante o desenho, para "ligar" e "desligar")
    	
    	    	
    	mat_luz = lamp.getMaterials(lamp.findMaterial("Luz"));

    	// Seta filtro inicial para texturas
    	//lo.setaFiltroTextura(-1,GL_LINEAR,GL_LINEAR);

    	// Carrega cena salva
    	//restauraObjetos();
    	//restauraCamera();
    	
    	
    }
    
 

    // Seta a escala da matriz de textura
    // (usada para repetir as texturas de acordo
    // com o tamanho das paredes, chão, teto, etc)
    public void setaEscalaTextura(float x,float y)
    {
    	glMatrixMode(GL_TEXTURE);
    	glLoadIdentity();
    	glScalef(x,y,1);
    	glMatrixMode(GL_MODELVIEW);
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

    public void update()
    {
    	
    	
    	checkSystemInput();

        if(!isPaused())
        {
            checkGameInput();
                        
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
            	camera.strafe(-speed * FPSCounter.frameInterval);
            	
            }

            if (moveRight.isPressed())
            {
            	
            	camera.strafe(speed * FPSCounter.frameInterval);	
            }
           
            if (moveUp.isPressed())
            {
            	camera.move(+speed * FPSCounter.frameInterval);
            	
            }
            if (moveDown.isPressed())
            {
            	
            	camera.move(-speed * FPSCounter.frameInterval);
            }
            
            if (angCam.isPressed())
            {
            	angcam += 15;
            	if(angcam > 75)
            	{
            		angcam = 45;
            	}
            	especificaParametrosVisualizacao();
            	
            		
            }
            if (solid.isPressed())
            {
            	for(int i=0; i<objetos.size(); i++)
            	{
            		if (objetos.get(i).getObject(0).getDrawMode().equalsIgnoreCase("w")) 
            			objetos.get(i).getObject(0).setDrawMode("s");
            		else if(objetos.get(i).getObject(0).getDrawMode().equalsIgnoreCase("s")) 
            			objetos.get(i).getObject(0).setDrawMode("t");
            		else if(objetos.get(i).getObject(0).getDrawMode().equalsIgnoreCase("t")) 
            			objetos.get(i).getObject(0).setDrawMode("w");
				
            		lo.criaDisplayList(mesa, null);
            	}
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
            if (fullScreen.isPressed())
            {
            	setFullScreen(!isFullScreen());
            }
            
            if(lights[0].isPressed())
            {
            	luzes[lights[0].getKeyCode()-2] = !luzes[lights[0].getKeyCode()-2];
            	setaLuzes();
            }
            if(lights[1].isPressed())
            {
            	luzes[lights[1].getKeyCode()-2] = !luzes[lights[1].getKeyCode()-2];
            	setaLuzes();
            }
            if(lights[2].isPressed())
            {
            	luzes[lights[2].getKeyCode()-2] = !luzes[lights[2].getKeyCode()-2];
            	setaLuzes();
            }
            if(lights[3].isPressed())
            {
            	luzes[lights[3].getKeyCode()-2] = !luzes[lights[3].getKeyCode()-2];
            	setaLuzes();
            }
            if(lights[4].isPressed())
            {
            	luzes[lights[4].getKeyCode()-2] = !luzes[lights[4].getKeyCode()-2];
            	setaLuzes();
            }
            
            if(saveCamera.isPressed())
            {
            	try
            	{
            		salvaCamera();
            		            		
            		
            	}catch(IOException e)
            		{
            			e.getMessage();
            		}
            }
            if(restoreCamera.isPressed())
            {
            	try
            	{
            		restauraCamera();
            	}catch(IOException e)
            		{
            			e.getMessage();
            		}
            }
            if (anisotropic.isPressed())
            {
            	
            	for(int i=0;i < texManager.getTexture().size(); i++)
        		{
            		glBindTexture(GL_TEXTURE_2D, texManager.getTexture(i).getTexID());
            		
            		final FloatBuffer max_a = BufferUtils.createFloatBuffer(16);
            		max_a.rewind();
            		
            		// Grab the maximum anisotropic filter.
            		glGetFloat(EXTTextureFilterAnisotropic.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT, max_a);

            		// Set up the anisotropic filter.
            		glTexParameterf(GL_TEXTURE_2D, EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT, max_a.get(0));
        		}
            }
				
            
        }

        public void render() 
        {
        	glMatrixMode(GL_MODELVIEW);

        	// Limpa a janela de visualização com a cor  
        	// de fundo definida previamente
        	glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); 

        	// Inicializa sistema de coordenadas do modelo
        	glLoadIdentity();
        	
        	camera.update();
        	camera.look();	

        	// A partir deste ponto, deve-se posicionar
        	// as fontes de luz, observador e desenhar
        	// todos os objetos

        	// Fonte de luz 5 vai estar sempre na posição
        	// do observador, olhando para z=-1, ou seja,
        	// para a frente
        	glLight(GL_LIGHT4, GL_POSITION, posLuz5F);
        	glLight(GL_LIGHT4, GL_SPOT_DIRECTION, dirLuz5F);
    	
        	// Desenha toda a cena

        	glEnable(GL_TEXTURE_2D);

        	glColor3f(1,1,1);   // branco

        	// Agora posiciona demais fontes de luz
        	glLight(GL_LIGHT0, GL_POSITION, posLuz1F); 
        	glLight(GL_LIGHT1, GL_POSITION, posLuz2F); 
        	glLight(GL_LIGHT2, GL_POSITION, posLuz3F); 
        	glLight(GL_LIGHT3, GL_POSITION, posLuz4F); 
        	
        	
        	// Primeiro desenha o céu
         	
        	DesenhaCeu();
        	
        	DesenhaParedes();
        	
        	DesenhaQuadro();
        	DesenhaChao();
        	DesenhaTeto();
        	DesenhaMesa();
        	DesenhaMesas();
        	//DesenhaObjetos();
        	
        	// Desabilitamos alterações no Z-Buffer
        	// e habilitamos "blending" para
        	// desenhar os vidros

        	glEnable(GL_BLEND);
        	glDepthMask(false);

        	// Vidros da janela dos fundos...
        	DesenhaVidros(303,150,-250,10);

        	// Vidros da janela do meio...
        	DesenhaVidros(303,150,0,30);

        	// Vidros da janela da frente
        	DesenhaVidros(303,150,250,7);

        	// Restauramos o estado anterior	
        	glDepthMask(true);
        	glDisable(GL_BLEND);

        	// Update fps
    		
        	screen.enterOrtho();
        	draw.drawString(1,"QPS: " + FPSCounter.get(), 5, 5);  
        	
        	screen.leaveOrtho();
        	        	
        	
        }
        
     // Desenha o "céu"
        void DesenhaCeu()
        {
        	glDisable(GL_LIGHTING);
        	// Somente no modo de desenho sólido
        	if(ceu.getObject(0).getDrawMode().equalsIgnoreCase("s"))
        	{
        		// Desenha um plano com gradiente,
        		// para simular o céu
        		glBegin(GL_QUAD_STRIP);
        		glColor3f(0.94f, 0.92f , 0.21f); // horizonte
        		glVertex3f(310,70,-500);
        		glVertex3f(310,70, 500);
        		glColor3f(1.0f, 0.53f,0);
        		glVertex3f(310,85,-500);
        		glVertex3f(310,85,500);
        		glColor3f(0.77f, 0.04f, 0.24f);
        		glVertex3f(310,100,-500);
        		glVertex3f(310,100,500);
        		glColor3f(0.10f, 0.07f, 0.57f);
        		glVertex3f(310,150,-500);
        		glVertex3f(310,150,500);
        		glColor3f(0,0,0);
        		glVertex3f(310,250,-500);
        		glVertex3f(310,250,500);
        		glEnd();
        	}
        	else if(ceu.getObject(0).getDrawMode().equalsIgnoreCase("t"))
        	{
        		// Desenha o hemisfério com textura
        		glPushMatrix();
        		glScalef(5,5,5);
        		ceu.draw(ceu);
        		glPopMatrix();
        	}
        	glEnable(GL_LIGHTING);
        }
        
     // Desenha 4 paredes
        private void DesenhaParedes()
        {
        	setaEscalaTextura(6,3);
        	
        	// Desenha a porta
        	glPushMatrix();
        	glTranslatef(-301,100,300);
        	glRotatef(90,0,1,0);
        	porta.draw(porta);
        	
        	glPopMatrix();
        	
        	
        	/**********************************
        	***          Paredes            ***
        	**********************************/

        	// Se estivermos desenhando com textura
        	// é necessário que a cor seja branca
        	if(plano.getObject(0).getDrawMode().equalsIgnoreCase("t"))
        	{
        		glColor3f(1,1,1);
        	}
        	else
        	{
        		glColor3f(0.76f, 0.82f, 0.72f);
        	}
        	// Parede dos fundos
        	glPushMatrix();
        	glTranslatef(0,150,-400);
        	glScalef(6,3,1);
        	// Associa a textura de parede ao plano
        	plano.getObject(0).setMaterialID(parede.getTexID());
        	//lo.desenhaObjeto(plano);
        	plano.draw(plano);
        	glPopMatrix();
        
        	// Parede da frente
        	glPushMatrix();
        	glTranslatef(0,150,400);
        	glRotatef(180,0,1,0);
        	glScalef(6,3,1);
        	//lo.desenhaObjeto(plano);
        	plano.draw(plano);
        	glPopMatrix();

        	setaEscalaTextura(8,3);

        	// Parede esquerda
        	glPushMatrix();
        	glTranslatef(-300,150,0);
        	glRotatef(90,0,1,0);
        	glScalef(8,3,1);
        	//lo.desenhaObjeto(plano);
        	plano.draw(plano);
        	glPopMatrix();

        	//
        	// Parede direita
        	// 
        	// Ajusta escala da textura
        	setaEscalaTextura(8,0.75f);

        	// Parte inferior
        	glPushMatrix();
        	glTranslatef(300f,37.5f,0f);
        	glRotatef(-90,0,1,0);
        	glScalef(8f,0.75f,1f);	// 8 m x 75 cm
        	//lo.desenhaObjeto(plano);
        	plano.draw(plano);
        	glPopMatrix();

        	// Parte superior
        	glPushMatrix();
        	glTranslatef(300f,300f-37.5f,0f);
        	glRotatef(-90,0,1,0);
        	glScalef(8f,0.75f,1f);
        	//lo.desenhaObjeto(plano);
        	plano.draw(plano);
        	glPopMatrix();

        	
        	// Parte do meio
        	// Ajusta escala da textura
        	setaEscalaTextura(0.5f,1.5f); // 0.5 m x 1.5 m

        	// fundos
        	glPushMatrix();
        	glTranslatef(300,150,-375);
        	glRotatef(-90,0,1,0);
        	glScalef(0.5f,1.5f,1f);
        	//lo.desenhaObjeto(plano);
        	plano.draw(plano);
        	glPopMatrix();

        	// antes do meio
        	glPushMatrix();
        	glTranslatef(300,150,-125);
        	glRotatef(-90,0,1,0);
        	glScalef(0.5f,1.5f,1f);
        	//lo.desenhaObjeto(plano);
        	plano.draw(plano);
        	glPopMatrix();

        	// depois do meio
        	glPushMatrix();
        	glTranslatef(300,150,125);
        	glRotatef(-90,0,1,0);
        	glScalef(0.5f,1.5f,1f);
        	//lo.desenhaObjeto(plano);
        	plano.draw(plano);
        	glPopMatrix();

        	// frente
        	glPushMatrix();
        	glTranslatef(300f,150f,375f);
        	glRotatef(-90,0,1,0);
        	glScalef(0.5f,1.5f,1f);
        	//lo.desenhaObjeto(plano);
        	plano.draw(plano);
        	glPopMatrix();

        	/**********************************
        	***          Janelas            ***
        	**********************************/
        	
        	// Primeiro, desenhamos as janelas
        	// em si

        	// Janela dos fundos
        	glPushMatrix();
        	glTranslatef(300f,150f,-250f);
        	glRotatef(-90,0,1,0);
        	//lo.desenhaObjeto(janela);
        	janela.draw(janela);
        	glPopMatrix();

        	// Janela do meio
        	glPushMatrix();
        	glTranslatef(300f,150f,0f);
        	glRotatef(-90,0,1,0);
        	//lo.desenhaObjeto(janela);
        	janela.draw(janela);
        	glPopMatrix();

        	// Janela da frente
        	glPushMatrix();
        	glTranslatef(300f,150f,250f);
        	glRotatef(-90,0,1,0);
        	//lo.desenhaObjeto(janela);
        	janela.draw(janela);
        	glPopMatrix();

        	//
        	// Lâmpadas
        	//

        	// 2 colunas de lâmpadas
        	for(int x=-200; x <= 300; x+=200)
        	{
        		int cont = 0;
        		// 3 filas de lâmpadas
        		for(int z=-250; z <= 300; z+=250, cont++)
        		{
        			float emissao = 0.0f;
        			// Seta o coeficiente de emissão para
        			// as luzes que estiverem ativas
        			
        			if(luzes[2-cont]) 
        				{
        					emissao = 1.0f;
        				}
        			mat_luz.setKe(emissao);
        			glPushMatrix();
        			glTranslatef(x,297,z);
        			glRotatef(90,1,0,0);
        			glRotatef(90,0,0,1);
        			//lo.desenhaObjeto(lamp);
        			lamp.draw(lamp);
        			glPopMatrix();
        		}
        	}

        	// Restaura a escala 1:1 da matriz de textura	
        	setaEscalaTextura(1,1); 
        }
        
     // Desenha o quadro-negro
        private void DesenhaQuadro()
        {
        	glPushMatrix();
        	// Posiciona, orienta e desenha o quadro
        	glTranslatef(0,160,398);
        	glRotatef(180,0,1,0);
        	//lo.desenhaObjeto(quadro);
        	quadro.draw(quadro);
        	arena.draw(arena);
        	glPopMatrix();
        	
        }
        
     // Desenha o chão
        private void DesenhaChao()
        {
        	setaEscalaTextura(8,8); 

        	// Se estivermos desenhando com textura
        	// é necessário que a cor seja branca
        	if(plano.getObject(0).getDrawMode().equalsIgnoreCase("t"))
        	{
        		glColor3f(1,1,1);
        	}
        	else
        	{
        		glColor3f(0.57f,0.41f, 0.22f);
        	}

        	glPushMatrix();
        	glTranslatef(0,0,0);
        	glRotatef(-90,1,0,0);
        	glScalef(6,8,1);
        	// Associa a textura de chão ao plano
        	plano.getObject(0).setMaterialID(chao.getTexID());
        	//lo.desenhaObjeto(plano);
        	plano.draw(plano);
        	glPopMatrix();

        	setaEscalaTextura(1,1); 
        }
        
     // Desenha o teto
        private void DesenhaTeto()
        {
        	setaEscalaTextura(6,8); 

        	// Se estivermos desenhando com textura
        	// é necessário que a cor seja branca
        	if(plano.getObject(0).getDrawMode().equalsIgnoreCase("t"))
        	{
        		glColor3f(1,1,1);
        	}
        	else
        	{
        		glColor3f(0.83f, 0.78f, 0.72f);
        	}

        	glPushMatrix();
        	glTranslatef(0,300,0);
        	glRotatef(90,1,0,0);
        	glScalef(6,8,1);
        	// Associa a textura de teto ao plano
        	plano.getObject(0).setMaterialID(teto.getTexID());
        	//lo.desenhaObjeto(plano);
        	plano.draw(plano);
        	glPopMatrix();

        	setaEscalaTextura(1,1); 
        }
        
     // Desenha a mesa
        private void DesenhaMesa()
        {
        	glPushMatrix();

        	// Posiciona a mesa
        	glTranslatef(limitesMesa[36].x,limitesMesa[36].y-12,limitesMesa[36].z);
        	//lo.desenhaObjeto(mesa);
        	mesa.draw(mesa);

        	// Posiciona e orienta a cadeira
        	glTranslatef(0,-15,30);
        	glRotatef(180,0,1,0);
        	//lo.desenhaObjeto(cadeira);
        	cadeira.draw(cadeira);

        	glPopMatrix();

        }
        
        // Desenha as mesas dos alunos
        private void DesenhaMesas()
        {
        	
        	// Desenha todas as mesas, exceto a do professor
        	for(int i=0; i < 36; ++i)
        	{
        		glPushMatrix();
        		// Posiciona uma mesa
        		glTranslatef(limitesMesa[i].x,limitesMesa[i].y-10,limitesMesa[i].z);
        		//lo.desenhaObjeto(mesapeq);
        		mesapeq.draw(mesapeq);
        		// Posiciona a cadeira em relação à mesa
        		glTranslatef(0,-10,-30);
        		//lo.desenhaObjeto(cadeira);
        		cadeira.draw(cadeira);
        		glPopMatrix();
        	}
        }
      /*  
     // Desenha os objetos sobre todas as mesas
        private void DesenhaObjetos()
        {
        	
        	// Varre o array de objetos
        	for(int i=0; i < objetos.size(); ++i)
        	{
        		glPushMatrix();
        		// Obtém a mesa associada ao objeto
        		Vector3f mesa = limitesMesa[objetos.get(i).getMesa()];
        		// Usa o centro da mesa como origem para desenhar o objeto
        		glTranslatef(mesa.x,mesa.y,mesa.z);
        		// Acumula translação e rotação do próprio objeto
        		glTranslatef(objetos.get(i).getTransl(0), objetos.get(i).getTransl(1), objetos.get(i).getTransl(2));
        		glRotatef(objetos.get(i).getRot(0), objetos.get(i).getRot(1), objetos.get(i).getRot(2), objetos.get(i).getRot(3));
        		//lo.desenhaObjeto(objetos.get(i));
        		objetos.get(i).draw(lo.world);
        		glPopMatrix();
        	}
        }
       */ 

                
        // Desenha os vidros de uma janela, centralizados na posição
        // especificada (x,y,z) e com o deslocamento informado
        // de abertura (a)
        private void DesenhaVidros(float x, float y, float z, float a)
        {
        	// Adicionamos 25, pois é o deslocamento mínimo
        	// em relação ao centro
        	a+=25f;
        	glPushMatrix();
        	glTranslatef(x,y,z);
        	glRotatef(-90,0,1,0);
        	// Vidros de trás
        	glTranslatef(-a,0,0);
        	//lo.desenhaObjeto(vidro);
        	vidro.draw(vidro);
        	glTranslatef(a*2,0,0);
        	//lo.desenhaObjeto(vidro);
        	vidro.draw(vidro);
        	// Vidros da frente
        	glTranslatef(75-a,0,3);
        	//lo.desenhaObjeto(vidro);
        	vidro.draw(vidro);
        	glTranslatef(-150,0,0);
        	//lo.desenhaObjeto(vidro);
        	vidro.draw(vidro);
        	glPopMatrix();
        }
       
        // Liga/desliga luzes de acordo com o estado
        private void setaLuzes()
        {
        	for(int luz=0; luz < 5; ++luz)
        	{
        		if(luzes[luz]) 
        			{
        				glEnable(GL_LIGHT0+luz);
        			}
        		else 
        			{
        				glDisable(GL_LIGHT0+luz);
        			}
        	}
        }
       /* 
        // Recupera objetos salvos num arquivo texto
        private void restauraObjetos() throws IOException
        {
        	//informa que serão utilizados os nomes padrão
    		System.out.println("Usando arquivo padrão: sala3d.txt");
    		 		
        	BufferedReader reader = lo.openArq(arqcena);
        	System.out.println("Carregando cena...");
        	while(true)
    		{
    			String line = reader.readLine();
    			
                if (line == null)
                {
                    reader.close();
                    break;
                }
                
                
                String[] lin = line.split(" ");
                
                            
                           	
                Obj p = new Obj(tipos[Integer.parseInt(lin[0])]);
                	p.setTransl(Conversion.convert(lin[1]), Conversion.convert(lin[2]), 
					 		    Conversion.convert(lin[3]));
                	p.setRot(Conversion.convert(lin[4]), Conversion.convert(lin[5]), 
              			     Conversion.convert(lin[6]), Conversion.convert(lin[7]));
                	p.setMesa(Integer.parseInt(lin[8]));
                	objetos.add(p);
                
    		}
        	
        	System.out.println("*** Cena restaurada");
        }
     */   
        // Salva posição e orientação da câmera,
        // e estado das 5 fontes de luz
        private void salvaCamera() throws IOException
        {
        	String arqcamW = "camera.txt";
        	BufferedWriter write = new BufferedWriter(new FileWriter(arqcamW));
        	write.write(angcam +" "+ camera.getPosition().x +" "+ camera.getPosition().y +" "+ camera.getPosition().z +" " +
    			    camera.getView().x +" "+ camera.getPosition().y +" "+ camera.getPosition().z +" "+
    			    camera.getUpVector().x +" "+ camera.getUpVector().y +" "+ camera.getUpVector().z);
        	write.newLine();
        	write.write(luzes[0] +" "+ luzes[1] +" "+ luzes[2] +" "+ luzes[3] +" "+ luzes[4]);
        	System.out.println("Camera salva");
        	write.close();
        	
        }

        // Restaura posição e orientação da câmera,
        // e estado das 5 fontes de luz
        private void restauraCamera() throws IOException
        {
        	System.out.println("Usando arquivo padrão: camera.txt");
        	BufferedReader reader = lo.openArq(arqcam);
        	String[] line = new String[2];
        	int cont = 0;
        	while(cont < 2)
    		{
    			
    			line[cont] = reader.readLine();

                if (line == null)
                {
                    reader.close();
                    break;
                }
                cont++;
    		}
        	String[][] lin = new String[2][];
        	lin[0] = line[0].split(" ");
        	lin[1] = line[1].split(" ");
        	
        	angcam = Conversion.convert(lin[0][0]);
        	camera.setPosition(Conversion.convert(lin[0][1]), Conversion.convert(lin[0][2]), Conversion.convert(lin[0][3]), 
        					   Conversion.convert(lin[0][4]), Conversion.convert(lin[0][5]), Conversion.convert(lin[0][6]), 
        					   Conversion.convert(lin[0][7]), Conversion.convert(lin[0][8]), Conversion.convert(lin[0][9]));
        	
        	
        	        	
        	for(int i=0; i < lin[1].length; i++)
        	{
        		if(lin[1][i].equalsIgnoreCase("false"))
        		{
        			luzes[i] = false;
        		}else
        		{
        			luzes[i] = true;
        		}
        	}
        	
        	System.out.println("Camera restaurada");
        	especificaParametrosVisualizacao();
        	setaLuzes();
        	
       
        }
        
        
        // Função usada para especificar o volume de visualização
        private void especificaParametrosVisualizacao()
        {
        	// Especifica sistema de coordenadas de projeção
        	glMatrixMode(GL_PROJECTION);
        	// Inicializa sistema de coordenadas de projeção
        	glLoadIdentity();

        	// Especifica a projeção perspectiva 
        	// (angulo, aspecto, zMin, zMax)
        	gluPerspective(angcam,fAspect,0.1f,1000f);

        	// Especifica sistema de coordenadas do modelo
        	glMatrixMode(GL_MODELVIEW);
        }

        
     // Define limites de cada mesa e preenche
     // o vetor
     private void defineLimitesMesas()
     {
     
     	float x = 0;
     	float z = Z_INI;
     	// Pos indica em qual mesa estamos
     	int pos = 0;
     	// 6 filas
     	for(int l=0;l <= 5; ++l)
     	{
     		x = X_INI;
     		// 3 colunas, 2 mesas em cada
     		for(int c=0; c < 3;++c)
     		{
     			// X e Z contêm as coordenadas do CENTRO
     			// da mesa
     			limitesMesa[pos].x = x;
     			limitesMesa[pos].z = z;	
     			limitesMesa[pos].y = 57;
     			pos++;
     			
     			// Para a segunda mesa, soma 61 cm em x
     			x = x + 61;
     			limitesMesa[pos].x = x;
     			limitesMesa[pos].z = z;	
     			limitesMesa[pos].y = 57;
     			pos++;
     			// Para a próxima coluna, soma 120 cm
     			x = x+120;
     		}
     		z = z+95;
     	}
     	
     	// Define centro da mesa do professor
     	limitesMesa[pos].x = 200;
     	limitesMesa[pos].z = 300;	
     	limitesMesa[pos].y = 70;
     	System.out.println("Total de mesas: " + pos);
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
            angCam = new GameAction("angCam", GameAction.DETECT_INITIAL_PRESS_ONLY);
            solid = new GameAction("solid", GameAction.DETECT_INITIAL_PRESS_ONLY);
            fog = new GameAction("fog", GameAction.DETECT_INITIAL_PRESS_ONLY);
            modTex = new GameAction("modTex", GameAction.DETECT_INITIAL_PRESS_ONLY);
            fullScreen  = new GameAction("fullScreen", GameAction.DETECT_INITIAL_PRESS_ONLY);
            saveCamera = new GameAction("saveCamera", GameAction.DETECT_INITIAL_PRESS_ONLY);
            restoreCamera  = new GameAction("restoreCamera", GameAction.DETECT_INITIAL_PRESS_ONLY);
            anisotropic = new GameAction("anisotropic", GameAction.DETECT_INITIAL_PRESS_ONLY);
            
            for(int i=0; i < lights.length; i++)
            {
            	lights[i] = new GameAction("Light"+i, GameAction.DETECT_INITIAL_PRESS_ONLY);
            	inputManager.mapToKey(lights[i], Keyboard.KEY_1+i);
            }

            inputManager.mapToKey(exit, Keyboard.KEY_ESCAPE);
            inputManager.mapToKey(pause, Keyboard.KEY_P);
           
            inputManager.mapToKey(zoomIn, Keyboard.KEY_X);
            inputManager.mapToKey(zoomOut, Keyboard.KEY_S) ;
            inputManager.mapToKey(moveLeft, Keyboard.KEY_LEFT);
            inputManager.mapToKey(moveRight, Keyboard.KEY_RIGHT);
            inputManager.mapToKey(moveUp, Keyboard.KEY_UP);
            inputManager.mapToKey(moveDown, Keyboard.KEY_DOWN);
            
            inputManager.mapToKey(angCam, Keyboard.KEY_A);
            inputManager.mapToKey(solid, Keyboard.KEY_T);
            inputManager.mapToKey(fog, Keyboard.KEY_N);
            inputManager.mapToKey(modTex, Keyboard.KEY_M);
            inputManager.mapToKey(fullScreen, Keyboard.KEY_F1);
            inputManager.mapToKey(saveCamera, Keyboard.KEY_F11);
            inputManager.mapToKey(restoreCamera, Keyboard.KEY_F12);
            inputManager.mapToKey(anisotropic, Keyboard.KEY_F2);
            
            
            
           
        }



	/**
	 * @param arqcena the arqcena to set
	 */
	public void setArqcena(String arqcena) {
		this.arqcena = arqcena;
	}



	/**
	 * @return the arqcena
	 */
	public String getArqcena() {
		return arqcena;
	}
        


}
