package fcampos.rawengine3D.model;

import java.io.IOException;
import java.util.Vector;

public class Model3d {

	
	protected int numOfObjects;					// The number of objects in the model
	protected int numOfMaterials;					// The number of materials for the model


	protected Vector<MaterialInfo> materials;	// The list of material information (Textures and colors)
	protected Vector<Object3d> object;			// The object list for our model	
	

	
	public Model3d()
	{
		numOfObjects = 0;
		numOfMaterials = 0;
		
		materials = new Vector<MaterialInfo>();
		object = new Vector<Object3d>();
		
		
	}

	
	public boolean load(String fileName) throws IOException {
		return false;
	}
	
	
	
	
	/**
	 * @param numOfObjects the numOfObjects to set
	 */
	public void setNumOfObjects(int numOfObjects) {
		this.numOfObjects = numOfObjects;
		}
	
	/**
	 * @param numOfObjects the numOfObjects to set
	 */
	public void addNumOfObjects(int numOfObjects) {
		this.numOfObjects += numOfObjects;
		}

	/**
	 * @return the numOfObjects
	 */
	public int getNumOfObjects() {
		return numOfObjects;
	}
	
	
	/**
	 * @param numOfMaterials the numOfMaterials to set
	 */
	public void setNumOfMaterials(int numOfMaterials) {
		this.numOfMaterials = numOfMaterials;
				
	}
	
	public void addNumOfMaterials(int numOfMaterials) {
		this.numOfMaterials += numOfMaterials;
				
	}

	/**
	 * @return the numOfMaterials
	 */
	public int getNumOfMaterials() {
		return numOfMaterials;
	}

	/**
	 * @param pMaterials the pMaterials to set
	 */
	public void setMaterials(Vector<MaterialInfo> pMaterials) {
		this.materials = pMaterials;
	}
	
	public void addMaterials(MaterialInfo pMaterials) {
		this.materials.add(pMaterials);
	}

	/**
	 * @return the pMaterials
	 */
	public Vector<MaterialInfo> getMaterials() {
		return materials;
	}
	
	public MaterialInfo getMaterials(int index) {
		return materials.get(index);
	}

	/**
	 * @param pObject the pObject to set
	 */
	public void setObject(Vector<Object3d> pObject) {
		this.object = pObject;
	}
	
	public void addObject(Object3d pObject) {
		this.object.add(pObject);
		
	}

	/**
	 * @return the pObject
	 */
	public Vector<Object3d> getObject() {
		return object;
	}
	
	public Object3d getObject(int index) {
		return object.get(index);
	}
	
	public int findMaterial(String nome) 
	{
		for (int i=0; i < materials.size(); i++) 
		{
			if (nome.equalsIgnoreCase(getMaterials(i).getName()))
			{
				return i;
			}
		}
		return -1;
	}





	
}
