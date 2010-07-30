package fcampos.rawengine3D.model;

import java.io.IOException;
import java.util.Vector;

public class Model3d {

	
	protected int numOfObjects;					// The number of objects in the model
	protected int numOfMaterials;				// The number of materials for the model

	protected Vector<MaterialInfo> materials;	// The list of material information (Textures and colors)
	protected Vector<Object3d> object;			// The object list for our model
	protected Vector<AnimationInfo> animations; // The list of animations 
	
	private int currentAnim;					// The current index into pAnimations list (NEW)
	private int currentFrame;					// The current frame of the current animation (NEW)
	private int nextFrame;						// The next frame of animation to interpolate too
	private float t;							// The ratio of 0.0f to 1.0f between each key frame
	private float lastTime;						// This stores the last time that was stored


	public Model3d()
	{
		numOfObjects = 0;
		numOfMaterials = 0;
		
		materials = new Vector<MaterialInfo>();
		object = new Vector<Object3d>();
		animations = new Vector<AnimationInfo>();
		setNextFrame(0);
		setT(0f);
		setLastTime(0);
		
		
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
	
	/**
	 * @param pAnimations the pAnimations to set
	 */
	public void setAnimations(Vector<AnimationInfo> pAnimations) {
		this.animations = pAnimations;
	}
	
	public void addAnimations(AnimationInfo animation)
	{
		animations.add(animation);
	}

	public AnimationInfo getAnimations(int index)
	{
		return animations.get(index);
	}

	/**
	 * @return the pAnimations
	 */
	public Vector<AnimationInfo> getAnimations() {
		return animations;
	}
	
	/**
	 * @return the numOfAnimations
	 */
	public int getNumOfAnimations() 
	{
		return animations.size();
	}
	
	/**
	 * @param currentAnim the currentAnim to set
	 */
	public void setCurrentAnim(int currentAnim) {
		this.currentAnim = currentAnim;
	}


	/**
	 * @return the currentAnim
	 */
	public int getCurrentAnim() {
		return currentAnim;
	}


	/**
	 * @param currentFrame the currentFrame to set
	 */
	public void setCurrentFrame(int currentFrame) {
		this.currentFrame = currentFrame;
	}


	/**
	 * @return the currentFrame
	 */
	public int getCurrentFrame() {
		return currentFrame;
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


	/**
	 * @param nextFrame the nextFrame to set
	 */
	public void setNextFrame(int nextFrame) {
		this.nextFrame = nextFrame;
	}


	/**
	 * @return the nextFrame
	 */
	public int getNextFrame() {
		return nextFrame;
	}


	/**
	 * @param t the t to set
	 */
	public void setT(float t) {
		this.t = t;
	}


	/**
	 * @return the t
	 */
	public float getT() {
		return t;
	}


	/**
	 * @param lastTime the lastTime to set
	 */
	public void setLastTime(float lastTime) {
		this.lastTime = lastTime;
	}


	/**
	 * @return the lastTime
	 */
	public float getLastTime() {
		return lastTime;
	}





	
}
