package fcampos.rawengine3D.resource;

import java.io.IOException;
import java.util.ArrayList;

import fcampos.rawengine3D.graficos.*;

public class TextureManager {

	private ArrayList<Texture> textures;
	private TextureLoader loader;
	
	public TextureManager()
	{
		textures = new ArrayList<Texture>();
		loader = new TextureLoader();
	}
	
		
	public Texture getNormalImage(String name, boolean mipmap, boolean useAnisotropicFilter) throws IOException
    {
        return loadImage(name, 1, 1, mipmap, useAnisotropicFilter);
    }

    public Texture getMirrorImage(String name, boolean mipmap, boolean useAnisotropicFilter) throws IOException
    {
        return loadImage(name, -1, 1, mipmap, useAnisotropicFilter);
    }


    public Texture getFlippedImage(String name, boolean mipmap, boolean useAnisotropicFilter) throws IOException
    {
        return loadImage(name, 1, -1, mipmap, useAnisotropicFilter);
    }

    private Texture loadImage(String name, int x, int y, boolean mipmap, boolean useAnisotropicFilter) throws IOException
    {
    	/*
    	int index;
    	index = name.lastIndexOf("/") + 1;
        int indice = procuraTextura(name.substring(index));
    	 if(indice != -1)
        {
        	return getTexture(indice);
        }else
        	{
        	*/
        		Texture tex = loader.getTexture(name, x, y, mipmap, useAnisotropicFilter);
        		setTexture(tex);
        		return tex;
        	//}
    }
    
    public void setTexture(Texture tex)
    {
    	textures.add(tex);
    }
    
    public Texture getTexture(int index)
    {
    	return textures.get(index);
    }
    
    public ArrayList<Texture> getTexture()
    {
    	return textures;
    }
     
    /*
    private int procuraTextura(String nome)
 	{
 		for (int i=0; i < textures.size(); i++)
 		{
 			if (nome.equalsIgnoreCase(textures.get(i).getName()))
 			{
 				return i;
 			}
 		}
 		return -1;
 	}
    */
    public TextureLoader getLoader()
    {
    	return loader;
    }
}
