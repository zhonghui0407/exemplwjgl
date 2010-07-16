package fcampos.rawengine3D.input;


import org.lwjgl.input.Keyboard;


public class GameAction {

/**
	Comportamento "Normal". O método isPressed() retornará verdadeiro enquanto 
	a tecla estiver pressionada.
*/
public static final int NORMAL = 0;

/**
	Comportamento "Só Detectar o Pressionamento Inicial". O método isPressed() retornará verdadeiro somente 
	após a tecla ser pressionada pela primeira vez, e não novamente até que a tecla seja solta 
	e pressionada novamente.
*/
public static final int DETECT_INITIAL_PRESS_ONLY = 1;

/**
 * Variáveis constantes representando os possíveis estados de uma tecla.
 */
private static final int STATE_RELEASED = 0;
private static final int STATE_PRESSED = 1;
private static final int STATE_WAITING_FOR_RELEASE = 2;


private String name; // Nome da ação que a tecla representará

private int behavior; // Comportamento da ação

private int amount; // Quantidade que poderá ser usada para movimentação com mouse
private int state;  // Estado da tecla
private int keyCode; // Código da tecla.


// Inicia a ação com compartamento padrão(NORMAL)
public GameAction(String name)
{
    this(name, NORMAL);
}


/**
 *  Inicia a ação com compartamento passado pelo usuário e reinicia o estado da tecla e quantidade de quanto ela ficou
 * 	pressionada
 */

public GameAction(String name, int behavior)
{
    this.name = name;
    this.behavior = behavior;
    reset();
}


/**
 * Faz mesmo procedimento do construtor acima e configura a tecla que será utilizada para a ação.
 */
public GameAction(String name, int behavior, int keyCode)
{
	this(name, behavior);
    setKeyCode(keyCode);
}

/**
 *  Retorna o nome da ação.
 */
public String getName()
{
    return name;
}

/**
 *  Define qual tecla será utilizada para a ação
 */
public void setKeyCode(int keyCode)
{
    this.keyCode = keyCode;
}

/**
 *  Retorna o código da tecla
 */
public int getKeyCode()
{
    return keyCode;
}

/**
 *  Retorna o nome da tecla
 */
public String getKeyName()
{
    return Keyboard.getKeyName(keyCode);
}

/**
 Reinicia o estado da tecla e a quantidade que ela ficou pressionada
 */
public void reset()
{
    state = STATE_RELEASED;
    amount = 0;
}

/**
Este método é como se Apertasse a tecla e soltasse rapidamente. Mesma coisa que chamar press() seguido de release().
*/
public synchronized void tap() 
{
	press();
	release();
}

/**
Muda o estado para tecla pressionada.
*/
public synchronized void press()
{
    press(1);
}


/**
Marca que a tecla foi pressionada à um determinado número de vezes, 
ou que o mouse se moveu à uma determinada distância.
*/
public synchronized void press(int amount)
{
    if (state != STATE_WAITING_FOR_RELEASE)
    {
        this.amount += amount;
        state = STATE_PRESSED;
    }
}

/**
Muda o estado para tecla solta.
*/
public synchronized void release()
{
   state = STATE_RELEASED;
}

/**
Retorna se a tecla foi pressionada ou não
desde a última verificação.
*/
public synchronized boolean isPressed()
{
    if (Keyboard.isKeyDown(keyCode))
    {
    	press();
        if (getAmount() != 0)
        {
           	return true;
        }else{
            return false;
             }
    }else {
        release();
    }
    return false;
}


/**
Para as teclas, este método retorna o número de vezes que a tecla 
foi pressionada desde a última verificação.
Para o movimento do mouse, este método retorna a distância
que ele foi movido.
*/
public synchronized int getAmount()
{
    int retVal = amount;
    if (retVal != 0)
    {
        if (state == STATE_RELEASED)
        {
            amount = 0;
        } else if (behavior == DETECT_INITIAL_PRESS_ONLY)
        	{
            	state = STATE_WAITING_FOR_RELEASE;
            	amount = 0;
        	}
    }
    return retVal;
}

/**
 	Método de Debug
 */

@Override
public String toString()
{
	return (name + "- "+ state + "- "+ behavior);
}
}
