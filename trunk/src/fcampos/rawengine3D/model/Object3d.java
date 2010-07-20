package fcampos.rawengine3D.model;

import static org.lwjgl.opengl.GL11.*;
import fcampos.rawengine3D.MathUtil.*;
import fcampos.rawengine3D.resource.Conversion;
import fcampos.rawengine3D.graficos.TextureCoord;

import java.nio.FloatBuffer;
import java.util.ArrayList;

public class Object3d {
	
	protected int numVertices;
	protected int numFaces;
	protected int numNormais;
	protected int numTexcoords;
	
	protected boolean normaisPorVertice;	// true se houver normais por vértice
	protected boolean bHasTexture; 			// true se houver materiais
	protected int materialID;
	protected int numDisplayList;				// display list, se houver
	
	protected ArrayList<TFace> faces;
	protected ArrayList<Integer> indices;
	
	protected String name;
	
	protected Vector3f dimMin;
	protected Vector3f dimMax;
	protected Vector3f center;
	
	protected Vector3f[] vertices;
	protected Vector3f[] normal;
	protected TextureCoord[] texCoords;
	
	protected String drawMode;
	
	public final static float branco[] = { 1.0f, 1.0f, 1.0f, 1.0f };	// constante para cor branca
	public final static FloatBuffer br = Conversion.allocFloats(branco);
	
	private BoundingBox boundingBox;
	
	
	
	public Object3d()
	{
		
		//faces = new ArrayList<TFace>();
		dimMin = new Vector3f();
		dimMax = new Vector3f();
		center = new Vector3f();
		faces = new ArrayList<TFace>();
		setNumDisplayList(-1);
		drawMode = "t";
		materialID = -1;
		
			
	}
	
	public Object3d(Object3d obj)
	{
		
		this.faces = obj.faces;
		this.name = obj.name;
		this.normaisPorVertice = obj.normaisPorVertice;
		this.numDisplayList = obj.numDisplayList;
		this.numFaces = obj.numFaces;
		this.numNormais = obj.numNormais;
		this.numTexcoords = obj.numTexcoords;
		this.numVertices = obj.numVertices;
		this.dimMin = obj.dimMin;
		this.dimMax = obj.dimMax;
		this.center = obj.center;
		this.drawMode = obj.drawMode;
	}
	
	protected void startFaces(int total)
	{
		
		for (int i = 0; i < total; i++)
		{
			
			faces.add(new TFace());
		}
		
	}

	
	/**
	 * @return the numVertices
	 */
	public int getNumVertices() {
		return numVertices;
	}

	/**
	 * @param numFaces the numFaces to set
	 */
	public void setNumFaces(int numFaces) {
		this.numFaces = numFaces;
		startFaces(numFaces);
	}

	/**
	 * @return the numFaces
	 */
	public int getNumFaces() {
		return numFaces;
	}

	/**
	 * @param numNormais the numNormais to set
	 */
	public void setNumNormais(int numNormais) {
		this.numNormais = numNormais;
		normal = new Vector3f[numNormais];
		
	}

	/**
	 * @return the numNormais
	 */
	public int getNumNormais() {
		return numNormais;
	}

	/**
	 * @param numTexcoords the numTexcoords to set
	 */
	public void setNumTexcoords(int numTexcoords) {
		this.numTexcoords = numTexcoords;
		texCoords = new TextureCoord[numTexcoords];
		
		for(int i=0; i < texCoords.length; i++)
		{
			texCoords[i] = new TextureCoord();
		}
		
	}

	/**
	 * @return the numTexcoords
	 */
	public int getNumTexcoords() {
		return numTexcoords;
	}

	/**
	 * @param normaisPorVertice the normaisPorVertice to set
	 */
	public void setNormaisPorVertice(boolean normaisPorVertice) {
		this.normaisPorVertice = normaisPorVertice;
	}

	/**
	 * @return the normaisPorVertice
	 */
	public boolean isNormaisPorVertice() {
		return normaisPorVertice;
	}

	/**
	 * @param temMateriais the temMateriais to set
	 */
	public void setbHasTexture(boolean bHasTexture) {
		this.bHasTexture = bHasTexture;
	}

	/**
	 * @return the temMateriais
	 */
	public boolean isbHasTexture() {
		return bHasTexture;
	}

	

	/**
	 * @param numDisplayList the numDisplayList to set
	 */
	public void setNumDisplayList(int numDisplayList) {
		this.numDisplayList = numDisplayList;
	}

	/**
	 * @return the numDisplayList
	 */
	public int getNumDisplayList() {
		return numDisplayList;
	}

	/**
	 * @param faces the faces to set
	 */
	public void setFaces(TFace face) {
		this.faces.add(face);
	}
	
	/**
	 * @param faces the faces to set
	 */
	public void setFaces(int index, TFace face) {
		this.faces.set(index, face);
	}
	/**
	 * @param faces the faces to set
	 */
	public void setFaces(ArrayList<TFace> face) {
		this.faces = face;
	}
	
	/**
	 * @return the faces
	 */
	public TFace getFace(int index) {
		return faces.get(index);
	}
	
	public ArrayList<TFace> getFace() {
		return faces;
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
	 * @param dimMin the dimMin to set
	 */
	public void setDimMin(Vector3f dimMin) {
		this.dimMin = dimMin;
	}
	
	/**
	 * @param dimMin the dimMin to set
	 */
	public void setDimMin(float minx, float miny, float minz) {
		this.dimMin.x = minx;
		this.dimMin.y = miny;
		this.dimMin.z = minz;
	}

	/**
	 * @return the dimMin
	 */
	public Vector3f getDimMin() {
		return dimMin;
	}

	/**
	 * @param dimMax the dimMax to set
	 */
	public void setDimMax(Vector3f dimMax) {
		this.dimMax = dimMax;
	}
	
	/**
	 * @param dimMax the dimMax to set
	 */
	public void setDimension() 
	{
		Vector3f dimMax = new Vector3f(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);
		Vector3f dimMin = new Vector3f(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
		
		
		for(int i=0; i < getNumVert(); i++)
		{
			
				dimMin.x = Math.min(dimMin.x, getVertices(i).x);
				dimMin.y = Math.min(dimMin.y, getVertices(i).y);
				dimMin.z = Math.min(dimMin.z, getVertices(i).z);
				
				dimMax.x = Math.max(dimMax.x, getVertices(i).x);
				dimMax.y = Math.max(dimMax.y, getVertices(i).y);
				dimMax.z = Math.max(dimMax.z, getVertices(i).z);
	
		}
		setDimMax(dimMax);
		setDimMin(dimMin);
		setCenter();
		boundingBox = new BoundingBox();
		boundingBox.createBoundingBox(dimMin, dimMax);
	}

	
	public void drawBoundingBox()
	{
		if(boundingBox != null)
		{
			boundingBox.drawBoundingBox();
		}
	}

	
	/**
	 * @param dimMax the dimMax to set
	 */
	public void setDimMax(float maxx, float maxy, float maxz) 
	{
		this.dimMax.x = maxx;
		this.dimMax.y = maxy;
		this.dimMax.z = maxz;
	}

	/**
	 * @return the dimMax
	 */
	public Vector3f getDimMax() {
		return dimMax;
	}

	public String toString()
	{
		return ("Nome " + name + " minx" + dimMin.x + " maxx " + dimMax.x+ " miny " + dimMin.y+ " maxy " + dimMax.y+ " minz " + dimMin.z+ " maxz " + dimMax.z);
	}

	/**
	 * @param center the center to set
	 */
	public void setCenter() {
		this.center = VectorMath.divide(VectorMath.add(getDimMax(), getDimMin()), 2);
	}

	/**
	 * @return the center
	 */
	public Vector3f getCenter() {
		
		return center;
	}

	
	
	// Seta o modo de desenho a ser utilizado para os objetos
	// 'w' - wireframe
	// 's' - sólido
	// 't' - sólido + textura
	public void setDrawMode(String drawMode)
	{
		
			this.drawMode = drawMode;
			
	}
	
	public String getDrawMode()
	{
		return drawMode;
	}
	
	
	public void draw(Model3d world)
	{
		int ult_texid, texid;	// última/atual textura 
		int prim = GL_POLYGON;	// tipo de primitiva
		
				
		
		// Seleciona GL_LINE_LOOP se o objetivo
		// for desenhar o objeto em wireframe
		
		if(drawMode.equalsIgnoreCase("w")) 
			{
				prim = GL_LINE_LOOP;
				
			}
		
		
		// Gera nova display list se for o caso
		if(getNumDisplayList() >= 1000)
		{
			glNewList(getNumDisplayList()-1000,GL_COMPILE_AND_EXECUTE);
			
		}
		// Ou chama a display list já associada...
		
		else if(getNumDisplayList() > -1)
		{
			glCallList(getNumDisplayList());
			
			return;
			
		}
		
		
		// Salva atributos de iluminação e materiais
		glPushAttrib(GL_LIGHTING_BIT);
		glDisable(GL_TEXTURE_2D);
		// Se objeto possui materiais associados a ele,
		// desabilita COLOR_MATERIAL - caso contrário,
		// mantém estado atual, pois usuário pode estar
		// utilizando o recurso para colorizar o objeto
		//if(obj.isTemMateriais())
		//{
		//	glDisable(GL_COLOR_MATERIAL);
		//}
		
		
		// Armazena id da última textura utilizada
		// (por enquanto, nenhuma)
		ult_texid = -1;
		// Varre todas as faces do objeto
		for(int i=0; i < getNumFaces(); i++)
		{
			
			
						
			if(getFace(i).getIndMat() != -1)
			{
				// Sim, envia parâmetros para OpenGL
				//int mat = obj.getFace(i).getIndMat();
				glDisable(GL_COLOR_MATERIAL);
				
				glMaterial(GL_FRONT, GL_AMBIENT, world.getMaterials(getFace(i).getIndMat()).getKd()); 
				
				
				// Se a face tem textura, ignora a cor difusa do material
				// (caso contrário, a textura é colorizada em GL_MODULATE)
				if(getFace(i).getTexId() != -1 && drawMode.equalsIgnoreCase("t"))
				{
					glMaterial(GL_FRONT, GL_DIFFUSE, br);
				}
				else
					{
						glMaterial(GL_FRONT,GL_DIFFUSE, world.getMaterials(getFace(i).getIndMat()).getKd());
						glMaterial(GL_FRONT,GL_SPECULAR, world.getMaterials(getFace(i).getIndMat()).getKs());
						glMaterial(GL_FRONT,GL_EMISSION, world.getMaterials(getFace(i).getIndMat()).getKe());
						glMaterialf(GL_FRONT,GL_SHININESS, world.getMaterials(getFace(i).getIndMat()).getSpec());
					}
			}
			
			// Se o objeto possui uma textura associada, utiliza
			// o seu texid ao invés da informação em cada face
			if(getFace(i).getTexId() != -1)
				{
				// Lê o texid associado à face (-1 se não houver)
					texid = getFace(i).getTexId();
				}else
					{
						texid = -1;
					}
				
			// Se a última face usou textura e esta não,
			// desabilita
			if(texid == -1 && ult_texid != -1)
			{
				glDisable(GL_TEXTURE_2D);
			}
			// Ativa texturas 2D se houver necessidade
			if (texid != -1 && texid != ult_texid && drawMode.equalsIgnoreCase("t"))
			{
			       glEnable(GL_TEXTURE_2D);
			       glBindTexture(GL_TEXTURE_2D,texid);
			}

			
			
			//System.out.println(texid);
			// Inicia a face
			glBegin(prim);
			// Para todos os vértices da face
			
			
			for(int vf=0; vf < 3; ++vf)
			{
				//System.out.println(getNormal(getFace(i).getNormal(vf)).z);
				// Se houver normais definidas para cada vértice,
				// envia a normal correspondente
				if(getFace(i).isPerVertexNormal())
				{
					
					glNormal3f(getNormal(getFace(i).getNormal(vf)).x, 
							getNormal(getFace(i).getNormal(vf)).y,
							getNormal(getFace(i).getNormal(vf)).z);
				}
				// Se houver uma textura associada...
				if(texid!=-1)
				{
					// Envia as coordenadas associadas ao vértice 
					
					glTexCoord2f(getTexcoords(getFace(i).getTexCoords(vf)).s, 
								 getTexcoords(getFace(i).getTexCoords(vf)).t);
				}
					 
		 		// Envia o vértice em si
				//System.out.println(getName());
				glVertex3f(this.getVertices(getFace(i).getVertices(vf)).x,
							this.getVertices(getFace(i).getVertices(vf)).y, 
							this.getVertices(getFace(i).getVertices(vf)).z);
			}
			// Finaliza a face
			glEnd();

			// Salva a última texid utilizada
			ult_texid = texid;
		} // fim da varredura de faces
		
		// Finalmente, desabilita as texturas
		glDisable(GL_TEXTURE_2D);
		// Restaura os atributos de iluminação e materiais
		glPopAttrib();
		// Se for uma nova display list...
		
		if(getNumDisplayList() >= 1000)
		{
			// Finaliza a display list
			glEndList();
			// E armazena a identificação correta
			setNumDisplayList(getNumDisplayList()-1000);
			
		}
		
	}

	/**
	 * @param materialID the materialID to set
	 */
	public void setMaterialID(int materialID) {
		this.materialID = materialID;
		for(int i=0; i < faces.size(); i++)
		{
			faces.get(i).setTexId(materialID);
		}
	}

	/**
	 * @return the materialID
	 */
	public int getMaterialID() {
		return materialID;
	}

	
	public int getNumNorm()
	{
		return numNormais;
	}
	
	public void setNumNorm(int numNormais)
	{
		this.numNormais = numNormais;
		normal = new Vector3f[numNormais];
	}
	
	public int getNumTex()
	{
		return numTexcoords;
	}
	
	public void setNumTex(int numTexcoords)
	{
		this.numTexcoords = numTexcoords;
		texCoords = new TextureCoord[numTexcoords];
		
		for(int i=0; i < texCoords.length; i++)
		{
			texCoords[i] = new TextureCoord();
		}
	}
	
	/**
	 * @param numVertFaces the numVertFaces to set
	 */
	public void setNumVert(int numVertices) {
		this.numVertices = numVertices;
		vertices = new Vector3f[numVertices];
		for (int a=0; a < numVertices; a++)
		{
			vertices[a] = new Vector3f();
		}
	}
	/**
	 * @return the numVertFaces
	 */
	public int getNumVert() {
		return numVertices;
	}

	/**
	 * @param pIndices the pIndices to set
	 */
	public void setIndices(ArrayList<Integer> pIndices) {
		this.indices = pIndices;
	}
	
	public void setIndices(int pIndices) {
		this.indices.add(pIndices);
	}
	
	public void setIndices(int index, int pIndices) {
		this.indices.add(index, pIndices);
	}
	
	public void startIndices(int pIndices) {
		this.indices = new ArrayList<Integer>(pIndices);
	}
	
	

	/**
	 * @return the pIndices
	 */
	public ArrayList<Integer> getIndices() {
		return indices;
	}
	
	public int getIndices(int index) {
		return indices.get(index);
	}

	public void setVertices(Vector3f vertices, int index) {
		this.vertices[index] = vertices;
	}
	
	public void setVertices(Vector3f[] vertices) {
		this.vertices = vertices;
	}
	
	/**
	 * @return the vertices
	 */
	public Vector3f getVertices(int index) {
		return vertices[index];
	}
	
	/**
	 * @return the vertices
	 */
	public Vector3f[] getVertices() {
		return vertices;
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
	
	public TextureCoord[] getTexcoords() {
		return texCoords;
	}
	
}

