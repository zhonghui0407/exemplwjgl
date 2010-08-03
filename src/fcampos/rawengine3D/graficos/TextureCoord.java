package fcampos.rawengine3D.graficos;

import fcampos.rawengine3D.MathUtil.*;

public class TextureCoord {
	
	public float s;
	public float t;
	
	
	public TextureCoord()
	{
		setS(0);
		setT(0);
	
	}
	
	public TextureCoord(Vector3f tex)
	{
		this(tex.x, tex.y);
		
		
	}
	
	public TextureCoord(float u, float v)
	{
		setS(u);
		setT(v);
		
	}

	/**
	 * @param s the s to set
	 */
	public void setS(float s) {
		this.s = s;
	}

	/**
	 * @return the s
	 */
	

	/**
	 * @param t the t to set
	 */
	public void setT(float t) {
		this.t = t;
	}

	

}
