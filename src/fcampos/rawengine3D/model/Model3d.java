package fcampos.rawengine3D.model;

import java.io.IOException;
import java.util.Vector;
import fcampos.rawengine3D.loader.*;
import fcampos.rawengine3D.loader.TMD3Loader.TMD3Tag;

public class T3dModel {

	
	private int numOfObjects;					// The number of objects in the model
	private int numOfMaterials;					// The number of materials for the model
	private int numOfAnimations;				// The number of animations in this model 
	private int currentAnim;					// The current index into pAnimations list (NEW)
	private int currentFrame;					// The current frame of the current animation (NEW)
	
	private int numOfTags;						// This stores the number of tags in the model
	
	private Vector<TMaterialInfo> materials;	// The list of material information (Textures and colors)
	private Vector<T3dObject> object;			// The object list for our model	
	private Vector<TAnimationInfo> animations; // The list of animations 
	
	
	private T3dModel[]	links;				// This stores a list of pointers that are linked to this model
	private TMD3Tag[]	tags;			// This stores all the tags for the model animations

	
	private TObjectLoader loader;
	private T3dsLoader dsloader;
	
	public T3dModel()
	{
		numOfObjects = 0;
		numOfMaterials = 0;
		numOfAnimations = 0;
		materials = new Vector<TMaterialInfo>();
		object = new Vector<T3dObject>();
		animations = new Vector<TAnimationInfo>();
		
	}

	
	public void load(String arqName, boolean mipmap, boolean useAnisotropicFilter, T3dModel world) throws IOException
	{
		loader = new TObjectLoader();
		loader.carregaObjeto(arqName, mipmap, useAnisotropicFilter, world);
		//System.out.println(pObject.get(0).getName());
		
	}
	
	public void load(String arqName) throws IOException
	{
		dsloader = new T3dsLoader();
		dsloader.import3DS(this, arqName);
		//System.out.println(pObject.get(0).getName());
		
	}
	
	public void draw(T3dModel world)
	{
		//System.out.println(pObject.get(0).getName());
		for(int i=0; i< object.size(); i++)
		{
		T3dObject obj = object.get(i);
		obj.draw(world);
		}
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
	public void setMaterials(Vector<TMaterialInfo> pMaterials) {
		this.materials = pMaterials;
	}
	
	public void addMaterials(TMaterialInfo pMaterials) {
		this.materials.add(pMaterials);
	}

	/**
	 * @return the pMaterials
	 */
	public Vector<TMaterialInfo> getMaterials() {
		return materials;
	}
	
	public TMaterialInfo getMaterials(int index) {
		return materials.get(index);
	}

	/**
	 * @param pObject the pObject to set
	 */
	public void setObject(Vector<T3dObject> pObject) {
		this.object = pObject;
	}
	
	public void addObject(T3dObject pObject) {
		this.object.add(pObject);
		
	}

	/**
	 * @return the pObject
	 */
	public Vector<T3dObject> getObject() {
		return object;
	}
	
	public T3dObject getObject(int index) {
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


	/**
	 * @param numOfAnimations the numOfAnimations to set
	 */
	public void setNumOfAnimations(int numOfAnimations) {
		this.numOfAnimations = numOfAnimations;
	}


	/**
	 * @return the numOfAnimations
	 */
	public int getNumOfAnimations() {
		return numOfAnimations;
	}


	/**
	 * @param pAnimations the pAnimations to set
	 */
	public void setAnimations(Vector<TAnimationInfo> pAnimations) {
		this.animations = pAnimations;
	}
	
	public void addAnimations(TAnimationInfo animation)
	{
		animations.add(animation);
	}

	public TAnimationInfo getAnimations(int index)
	{
		return animations.get(index);
	}

	/**
	 * @return the pAnimations
	 */
	public Vector<TAnimationInfo> getAnimations() {
		return animations;
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


	/**
	 * @param numOfTags the numOfTags to set
	 */
	public void setNumOfTags(int numOfTags) {
		this.numOfTags = numOfTags;
	}


	/**
	 * @return the numOfTags
	 */
	public int getNumOfTags() {
		return numOfTags;
	}


	/**
	 * @param pLinks the pLinks to set
	 */
	public void setLinks(T3dModel[] pLinks) {
		this.links = pLinks;
	}
	
	/**
	 * @param pLinks the pLinks to set
	 */
	public void setLinks(T3dModel pLinks, int index) {
		this.links[index] = pLinks;
	}
	
	/**
	 * @param links the pLinks to set
	 */
	public void setNumLinks(int total) {
		links = new T3dModel[total];
		for(int i = 0; i < total; i++)
			links[i] = null;
	}


	/**
	 * @return the pLinks
	 */
	public T3dModel[] getLinks() {
		return links;
	}
	
	/**
	 * @return the pLinks
	 */
	public T3dModel getLinks(int index) {
		return links[index];
	}


	/**
	 * @param pTags the pTags to set
	 */
	public void setTags(TMD3Tag[] pTags) {
		this.tags = pTags;
	}
	
	/**
	 * @param pTags the pTags to set
	 */
	public void setTags(TMD3Tag pTags, int index) {
		this.tags[index] = pTags;
	}
	
	/**
	 * @param tags the pTags to set
	 */
	public void setNumTags(int total) {
		tags = new TMD3Tag[total];
				
	}


	/**
	 * @return the pTags
	 */
	public TMD3Tag[] getTags() {
		return tags;
	}
	
	/**
	 * @return the pTags
	 */
	public TMD3Tag getTags(int index) {
		return tags[index];
	}
	
	
	

	
	
	
}
