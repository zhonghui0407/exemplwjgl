package fcampos.rawengine3D.model;

import java.nio.FloatBuffer;

import fcampos.rawengine3D.resource.Conversion;




public class Material {

	private String name;	// Identificação do material
	private FloatBuffer ka;	// Ambiente
	private FloatBuffer kd;	// Difuso
	private FloatBuffer ks;	// Especular
	private FloatBuffer ke;	// Emissão
	private float spec;	// Fator de especularidade
	public static final int SIZE_FLOAT = 4;

	
	public Material()
	{
		setKa(new float[SIZE_FLOAT]);
		setKd(new float[SIZE_FLOAT]);
		setKs(new float[SIZE_FLOAT]);
		setKe(new float[SIZE_FLOAT]);
		setSpec(0.0f);
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param ka the ka to set
	 */
	public void setKa(float[] ka) {
		this.ka = Conversion.allocFloats(ka);
		
	}

	/**
	 * @return the ka
	 */
	public FloatBuffer getKa() {
		
		return ka;
	}

	/**
	 * @param kd the kd to set
	 */
	public void setKd(float[] kd) {
		this.kd = Conversion.allocFloats(kd);
	}

	/**
	 * @return the kd
	 */
	public FloatBuffer getKd() {
		
		return kd;
	}

	/**
	 * @param ks the ks to set
	 */
	public void setKs(float[] ks) {
		this.ks = Conversion.allocFloats(ks);
	}

	/**
	 * @return the ks
	 */
	public FloatBuffer getKs() {
		
		return ks;
	}

	/**
	 * @param ke the ke to set
	 */
	public void setKe(float[] ke) {
		this.ke = Conversion.allocFloats(ke);
	}
	
	public void setKe(float ke) {
		this.ke.put(0, ke);
		this.ke.put(1, ke);
		this.ke.put(2, ke);
		
	}

	/**
	 * @return the ke
	 */
	public FloatBuffer getKe() {
		
		return ke;
	}

	/**
	 * @param spec the spec to set
	 */
	public void setSpec(float spec) {
		this.spec = spec;
	}
	
	public void setAlpha(float spec)
	{
		ka.put(3, spec);
		kd.put(3, spec);
		ks.put(3, spec);
		ke.put(3, spec);
		
		
	}

	/**
	 * @return the spec
	 */
	public float getSpec() {
		return spec;
	}

	
	
}
