package fcampos.rawengine3D.model;

import static org.lwjgl.opengl.GL11.*;
import fcampos.rawengine3D.MathUtil.*;
import fcampos.rawengine3D.resource.Conversion;

import java.nio.FloatBuffer;
import java.util.ArrayList;

public class Obj {
	
	private float[] transl;
	private float[] rot;
	private int mesa;
	
	private int numVertices;
	private int numFaces;
	private int numNormais;
	private int numTexcoords;
	
	private boolean normaisPorVertice;	// true se houver normais por vértice
	private boolean temMateriais;			// true se houver materiais
	
	private int numDisplayList;				// display list, se houver
	
	private ArrayList<Face> faces;
	private ArrayList<Material> material;
	
	private String name;
	
	private Vector3f dimMin;
	private Vector3f dimMax;
	private Vector3f center;
	
	private String drawMode;
	
	public final static float branco[] = { 1.0f, 1.0f, 1.0f, 1.0f };	// constante para cor branca
	public final static FloatBuffer br = Conversion.allocFloats(branco);
	
	
	
	public Obj()
	{
		setTransl(new float[3]);
		setRot(new float[4]);
		setMesa(0);
		setNormaisPorVertice(false);
		setTemMateriais(false);
		setNumDisplayList(-1);
		dimMin = new Vector3f();
		dimMax = new Vector3f();
		center = new Vector3f();
		faces = new ArrayList<Face>();
		setTextura(-1);
		material = new ArrayList<Material>();
		drawMode = "t";
		
			
	}
	
	public Obj(Obj obj)
	{
		setTransl(new float[3]);
		setRot(new float[4]);
		this.faces = obj.faces;
		this.mesa = obj.mesa;
		this.name = obj.name;
		this.normaisPorVertice = obj.normaisPorVertice;
		this.numDisplayList = obj.numDisplayList;
		this.numFaces = obj.numFaces;
		this.numNormais = obj.numNormais;
		this.numTexcoords = obj.numTexcoords;
		this.numVertices = obj.numVertices;
		setRot(0f,0f,0f,0f);
		this.temMateriais = obj.temMateriais;
		setTransl(0f,0f,0f);
		this.dimMin = obj.dimMin;
		this.dimMax = obj.dimMax;
		this.material = obj.material;
		this.center = obj.center;
		this.drawMode = obj.drawMode;
	}
	
	private void startFaces(int total)
	{
		
		for (int i = 0; i < total; i++)
		{
			Face face = new Face();
			faces.add(face);
		}
		
	}

	/**
	 * @param numVertices the numVertices to set
	 */
	public void setNumVertices(int numVertices) {
		this.numVertices = numVertices;
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
	public void setTemMateriais(boolean temMateriais) {
		this.temMateriais = temMateriais;
	}

	/**
	 * @return the temMateriais
	 */
	public boolean isTemMateriais() {
		return temMateriais;
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
	public void setFaces(Face face) {
		this.faces.add(face);
	}
	
	/**
	 * @return the faces
	 */
	public Face getFace(int index) {
		return faces.get(index);
	}
	
		
	/**
	 * @param transl the transl to set
	 */
	public void setTransl(float[] transl) {
		this.transl = transl;
	}
	
	public void setTransl(float x, float y, float z) {
		transl[0] = x;
		transl[1] = y;
		transl[2] = z;
	}
	/**
	 * @return the transl
	 */
	public float[] getTransl() {
		return transl;
	}
	
	public float getTransl(int index) {
		return transl[index];
	}
	/**
	 * @param rot the rot to set
	 */
	public void setRot(float[] rot) {
		this.rot = rot;
	}
	
	public void setRot(float rot, float x, float y, float z) {
		this.rot[0] = rot;
		this.rot[1] = x;
		this.rot[2] = y;
		this.rot[3] = z;
	}
	/**
	 * @return the rot
	 */
	public float[] getRot() {
		return rot;
	}
	
	public float getRot(int index) {
		return rot[index];
	}
	/**
	 * @param mesa the mesa to set
	 */
	public void setMesa(int mesa) {
		this.mesa = mesa;
	}
	/**
	 * @return the mesa
	 */
	public int getMesa() {
		return mesa;
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
	public void setDimMax(float maxx, float maxy, float maxz) {
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
	public void setCenter(Vector3f center) {
		this.center.setTo(center);
	}

	/**
	 * @return the center
	 */
	public Vector3f getCenter() {
		
		Vector3f center = VectorMath.divide(VectorMath.add(getDimMax(), getDimMin()), 2);
		return center;
	}

	/**
	 * @param textura the textura to set
	 */
	public void setTextura(int textura) {
		for(int i=0; i < faces.size(); i++)
		{
			faces.get(i).setTexId(textura);
		}
	}

	/**
	 * @param material the material to set
	 */
	public void setMaterial(ArrayList<Material> material) {
		this.material = material;
	}
	
	public void setMaterial(Material material) {
		this.material.add(material);
	}

	/**
	 * @return the material
	 */
	public ArrayList<Material> getMaterial() {
		return material;
	}
	
	/**
	 * @return the material
	 */
	public Material getMaterial(int index) {
		return material.get(index);
	}
	
	public int findMaterial(String nome) 
	{
		for (int i=0; i < material.size(); i++) 
		{
			if (nome.equalsIgnoreCase(getMaterial(i).getName()))
			{
				return i;
			}
		}
		return -1;
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
	
	
	public void draw()
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
			
			
			// Usa normais calculadas por face (flat shading) se
			// o objeto não possui normais por vértice
			if(!getFace(i).isPerVertexNormal())
			{
				
				glNormal3f(getFace(i).getNormal(i).x, getFace(i).getNormal(i).y, getFace(i).getNormal(i).z);
			}
			// Existe um material associado à face ?
			
			
			if(getFace(i).getIndMat() != -1)
			{
				// Sim, envia parâmetros para OpenGL
				//int mat = obj.getFace(i).getIndMat();
				glDisable(GL_COLOR_MATERIAL);
				
				glMaterial(GL_FRONT, GL_AMBIENT, getFace(i).getMaterial().getKd()); 
				
				
				// Se a face tem textura, ignora a cor difusa do material
				// (caso contrário, a textura é colorizada em GL_MODULATE)
				if(getFace(i).getTexId() != -1 && drawMode.equalsIgnoreCase("t"))
				{
					glMaterial(GL_FRONT, GL_DIFFUSE, br);
				}
				else
					{
						glMaterial(GL_FRONT,GL_DIFFUSE, getFace(i).getMaterial().getKd());
						glMaterial(GL_FRONT,GL_SPECULAR, getFace(i).getMaterial().getKs());
						glMaterial(GL_FRONT,GL_EMISSION, getFace(i).getMaterial().getKe());
						glMaterialf(GL_FRONT,GL_SHININESS, getFace(i).getMaterial().getSpec());
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

			
			
			
			// Inicia a face
			glBegin(prim);
			// Para todos os vértices da face
			
			for(int vf=0; vf < getFace(i).getNumVert(); ++vf)
			{

				// Se houver normais definidas para cada vértice,
				// envia a normal correspondente
				if(getFace(i).isPerVertexNormal())
				{
					
					glNormal3f(getFace(i).getNormal(vf).x, 
							   getFace(i).getNormal(vf).y,
							   getFace(i).getNormal(vf).z);
				}
				// Se houver uma textura associada...
				if(texid!=-1)
				{
					// Envia as coordenadas associadas ao vértice 
					
					glTexCoord2f(getFace(i).getTexcoords(vf).s, 
							     getFace(i).getTexcoords(vf).t);
				}
					 
		 		// Envia o vértice em si
				
				glVertex3f(getFace(i).getVertices(vf).x,
						   getFace(i).getVertices(vf).y, 
						   getFace(i).getVertices(vf).z);
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

}
