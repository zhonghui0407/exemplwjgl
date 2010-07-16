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
	
	private Vector<TMaterialInfo> pMaterials;	// The list of material information (Textures and colors)
	private Vector<T3dObject> pObject;			// The object list for our model	
	private Vector<TAnimationInfo> pAnimations; // The list of animations 
	
	
	private T3dModel[]	pLinks;				// This stores a list of pointers that are linked to this model
	private TMD3Tag[]	pTags;			// This stores all the tags for the model animations

	
	private TObjectLoader loader;
	private T3dsLoader dsloader;
	
	public T3dModel()
	{
		numOfObjects = 0;
		numOfMaterials = 0;
		numOfAnimations = 0;
		pMaterials = new Vector<TMaterialInfo>();
		pObject = new Vector<T3dObject>();
		pAnimations = new Vector<TAnimationInfo>();
		
	}

	
	public void carregaObjeto(String arqName, boolean mipmap, boolean useAnisotropicFilter, T3dModel world) throws IOException
	{
		loader = new TObjectLoader();
		loader.carregaObjeto(arqName, mipmap, useAnisotropicFilter, world);
		//System.out.println(pObject.get(0).getName());
		
	}
	
	public void carregaObjeto(T3dModel world, String arqName) throws IOException
	{
		dsloader = new T3dsLoader();
		dsloader.import3DS(world, arqName);
		//System.out.println(pObject.get(0).getName());
		
	}
	
	public void draw(T3dModel world)
	{
		//System.out.println(pObject.get(0).getName());
		for(int i=0; i< pObject.size(); i++)
		{
		T3dObject obj = pObject.get(i);
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
	public void setPMaterials(Vector<TMaterialInfo> pMaterials) {
		this.pMaterials = pMaterials;
	}
	
	public void addPMaterials(TMaterialInfo pMaterials) {
		this.pMaterials.add(pMaterials);
	}

	/**
	 * @return the pMaterials
	 */
	public Vector<TMaterialInfo> getPMaterials() {
		return pMaterials;
	}
	
	public TMaterialInfo getPMaterials(int index) {
		return pMaterials.get(index);
	}

	/**
	 * @param pObject the pObject to set
	 */
	public void setPObject(Vector<T3dObject> pObject) {
		this.pObject = pObject;
	}
	
	public void addPObject(T3dObject pObject) {
		this.pObject.add(pObject);
		
	}

	/**
	 * @return the pObject
	 */
	public Vector<T3dObject> getPObject() {
		return pObject;
	}
	
	public T3dObject getPObject(int index) {
		return pObject.get(index);
	}
	
	public int findMaterial(String nome) 
	{
		for (int i=0; i < pMaterials.size(); i++) 
		{
			if (nome.equalsIgnoreCase(getPMaterials(i).getName()))
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
	public void setPAnimations(Vector<TAnimationInfo> pAnimations) {
		this.pAnimations = pAnimations;
	}
	
	public void addAnimations(TAnimationInfo animation)
	{
		pAnimations.add(animation);
	}

	public TAnimationInfo getPAnimations(int index)
	{
		return pAnimations.get(index);
	}

	/**
	 * @return the pAnimations
	 */
	public Vector<TAnimationInfo> getPAnimations() {
		return pAnimations;
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
	public void setPLinks(T3dModel[] pLinks) {
		this.pLinks = pLinks;
	}
	
	/**
	 * @param pLinks the pLinks to set
	 */
	public void setPLinks(T3dModel pLinks, int index) {
		this.pLinks[index] = pLinks;
	}
	
	/**
	 * @param pLinks the pLinks to set
	 */
	public void setNumLinks(int total) {
		pLinks = new T3dModel[total];
		for(int i = 0; i < total; i++)
			pLinks[i] = null;
	}


	/**
	 * @return the pLinks
	 */
	public T3dModel[] getPLinks() {
		return pLinks;
	}
	
	/**
	 * @return the pLinks
	 */
	public T3dModel getPLinks(int index) {
		return pLinks[index];
	}


	/**
	 * @param pTags the pTags to set
	 */
	public void setPTags(TMD3Tag[] pTags) {
		this.pTags = pTags;
	}
	
	/**
	 * @param pTags the pTags to set
	 */
	public void setPTags(TMD3Tag pTags, int index) {
		this.pTags[index] = pTags;
	}
	
	/**
	 * @param pTags the pTags to set
	 */
	public void setNumPTags(int total) {
		pTags = new TMD3Tag[total];
				
	}


	/**
	 * @return the pTags
	 */
	public TMD3Tag[] getPTags() {
		return pTags;
	}
	
	/**
	 * @return the pTags
	 */
	public TMD3Tag getPTags(int index) {
		return pTags[index];
	}
	
	
	

	
	
	
}
