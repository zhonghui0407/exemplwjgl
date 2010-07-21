package fcampos.rawengine3D.graficos;

import fcampos.rawengine3D.MathUtil.*;

public class TextureCoord {
	
	public float s;
	public float t;
	public float r;
	
	public TextureCoord()
	{
		setS(0);
		setT(0);
		setR(0);
	}
	
	public TextureCoord(Vector3f tex)
	{
		setS(tex.x);
		setT(tex.y);
		setR(tex.z);
		
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

	/**
	 * @return the t
	 */
	

	/**
	 * @param r the r to set
	 */
	public void setR(float r) {
		this.r = r;
	}

	
	

}
