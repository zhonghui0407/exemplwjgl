package fcampos.rawengine3D.resource;

import java.io.IOException;

import fcampos.rawengine3D.font.BitmapFont;
import fcampos.rawengine3D.graficos.Texture;


public class DrawString {

	private BitmapFont draw;
	private Texture fontTexture;
	private TextureManager texManager;
		
	public DrawString(String arqName)
	{
		texManager = new TextureManager();
		
		try
		{
			fontTexture = texManager.getFlippedImage(arqName, false, false);
			draw = new BitmapFont(fontTexture, 32, 32);
		}
			catch(IOException e)
			{
				e.getMessage();
			}
		
	}
	
		
	public void drawString(int font, String text, int x, int y)
	{
		draw.drawString(font, text, x, y);
	}
}
