package fcampos.rawengine3D.model;

import fcampos.rawengine3D.MathUtil.Vector3f;
import fcampos.rawengine3D.graficos.TextureCoord;


public class FaceObj {
	
	
	private Vector3f[] vertices;
	private Vector3f[] normal;
	private TextureCoord[] texCoords;
	
	
	private int indMat;		// índice para o material (se houver)
	private int texId;	// índice para a textura (se houver)
	
	private int numVert;		// número de vértices na face
	private int numNorm;
	private int numTex;
	
	private Material material;
	
	private boolean perVertexNormal;
	
	
		
	
	public FaceObj()
	{
		indMat = -1;
		texId = -1;
		material = null;
		perVertexNormal = false;
	}
	
		
	public int getNumNorm()
	{
		return numNorm;
	}
	
	public void setNumNorm(int numNorm)
	{
		this.numNorm = numNorm;
		normal = new Vector3f[numNorm];
	}
	
	public int getNumTex()
	{
		return numTex;
	}
	
	public void setNumTex(int numTex)
	{
		this.numTex = numTex;
		texCoords = new TextureCoord[numTex];
		
		for(int i=0; i < texCoords.length; i++)
		{
			texCoords[i] = new TextureCoord();
		}
	}
	
	/**
	 * @param numVertFaces the numVertFaces to set
	 */
	public void setNumVert(int numVert) {
		this.numVert = numVert;
		vertices = new Vector3f[numVert];
		
	}
	/**
	 * @return the numVertFaces
	 */
	public int getNumVert() {
		return numVert;
	}
	/**
	 * @param indVert the indVert to set
	 */
	
	/**
	 * @param indMat the indMat to set
	 */
	public void setIndMat(int indMat) {
		this.indMat = indMat;
	}
	/**
	 * @return the indMat
	 */
	public int getIndMat() {
		return indMat;
	}
	/**
	 * @param indTexId the indTexId to set
	 */
	public void setTexId(int texId) {
		this.texId = texId;
	}
	/**
	 * @return the indTexId
	 */
	public int getTexId() {
		return texId;
	}

	
	/**
	 * @param vertices the vertices to set
	 */
	public void setVertices(Vector3f vertices, int index) {
		this.vertices[index] = vertices;
	}
	
	/**
	 * @return the vertices
	 */
	public Vector3f getVertices(int index) {
		return vertices[index];
	}
	
	
	/**
	 * @param normal the normal to set
	 */
	public void setNormal(Vector3f normal, int index) {
		this.normal[index] = normal;
	}
	
	
	/**
	 * @return the normal
	 */
	public Vector3f getNormal(int index) {
		return normal[index];
	}
	
	
	/**
	 * @param texcoords the texcoords to set
	 */
	public void setTexcoords(Vector3f texCoord, int index) {
		this.texCoords[index].s = texCoord.x;
		this.texCoords[index].t = texCoord.y;
		this.texCoords[index].r = texCoord.z;
		
		
	}

	/**
	 * @return the texcoords
	 */
	public TextureCoord getTexcoords(int index) {
		return texCoords[index];
	}
	
		/**
	 * @param material the material to set
	 */
	public void setMaterial(Material material) {
		this.material = material;
	}

	/**
	 * @return the material
	 */
	public Material getMaterial() {
		return material;
	}

	/**
	 * @param perVertexNormal the perVertexNormal to set
	 */
	public void setPerVertexNormal(boolean perVertexNormal) {
		this.perVertexNormal = perVertexNormal;
	}

	/**
	 * @return the perVertexNormal
	 */
	public boolean isPerVertexNormal() {
		return perVertexNormal;
	}
	
	
		

}
