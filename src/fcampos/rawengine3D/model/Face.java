package fcampos.rawengine3D.model;


public class Face {
	
	
	private int[] vertices;
	private int[] normal;
	private int[] texCoords;
	private int indMat;		//índice para o material (se houver)
	private int texId;	// índice para a textura (se houver)
	private boolean perVertexNormal;
	
	
		
	public Face()
	{
		setVertices(new int[3]);
		setNormal(new int[3]);
		setTexCoords(new int[3]);
		indMat = -1;
		texId = -1;
	}



	/**
	 * @param vertices the vertices to set
	 */
	public void setVertices(int[] vertices) {
		this.vertices = vertices;
	}

	public void setVertices(int indexVec, int index) {
		this.vertices[indexVec] = index;
	}

	/**
	 * @return the vertices
	 */
	public int[] getVertices() {
		return vertices;
	}
	public int getVertices(int index) {
		return vertices[index];
	}


	/**
	 * @param normal the normal to set
	 */
	public void setNormal(int[] normal) {
		this.normal = normal;
	}

	public void setNormal(int indexVec, int index) {
		this.normal[indexVec] = index;
	}

	/**
	 * @return the normal
	 */
	public int[] getNormal() {
		return normal;
	}
	
	public int getNormal(int index) {
		return normal[index];
	}



	/**
	 * @param texCoords the texCoords to set
	 */
	public void setTexCoords(int[] texCoords) {
		this.texCoords = texCoords;
	}

	public void setTexCoords(int indexVec, int index) {
		this.texCoords[indexVec] = index;
	}


	/**
	 * @return the texCoords
	 */
	public int[] getTexCoords() {
		return texCoords;
	}

	public int getTexCoords(int index) {
		return texCoords[index];
	}

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
	 * @param texId the texId to set
	 * @throws Exception 
	 */
	public void setTexId(int texId){
		this.texId = texId;
		//try{
		//	if(texId != -1)
		//	throw new Exception();
		//}catch (Exception e) {
		//	e.printStackTrace();
		//}
		
	}



	/**
	 * @return the texId
	 */
	public int getTexId() {
		return texId;
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
