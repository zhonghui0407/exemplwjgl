package fcampos.rawengine3D.model;

import java.io.IOException;
import java.util.Vector;
import fcampos.rawengine3D.loader.*;
import fcampos.rawengine3D.loader.LoaderMD3.TagMD3;

public class Model3d {

	
	private int numOfObjects;					// The number of objects in the model
	private int numOfMaterials;					// The number of materials for the model
	private int numOfAnimations;				// The number of animations in this model 
	private int currentAnim;					// The current index into pAnimations list (NEW)
	private int currentFrame;					// The current frame of the current animation (NEW)
	
	private int numOfTags;						// This stores the number of tags in the model
	
	private Vector<MaterialInfo> materials;	// The list of material information (Textures and colors)
	private Vector<Object3d> object;			// The object list for our model	
	private Vector<AnimationInfo> animations; // The list of animations 
	
	
	private Model3d[]	links;				// This stores a list of pointers that are linked to this model
	private TagMD3[]	tags;			// This stores all the tags for the model animations

	
	private TObjectLoader loader;
	private Loader3DS dsloader;
	
	public Model3d()
	{
		numOfObjects = 0;
		numOfMaterials = 0;
		numOfAnimations = 0;
		materials = new Vector<MaterialInfo>();
		object = new Vector<Object3d>();
		animations = new Vector<AnimationInfo>();
		
	}

	
	public void load(String arqName, boolean mipmap, boolean useAnisotropicFilter, Model3d world) throws IOException
	{
		loader = new TObjectLoader();
		loader.carregaObjeto(arqName, mipmap, useAnisotropicFilter, world);
		//System.out.println(pObject.get(0).getName());
		
	}
	
	public void load(String arqName) throws IOException
	{
		dsloader = new Loader3DS();
		dsloader.import3DS(this, arqName);
		//System.out.println(pObject.get(0).getName());
		
	}
	
	public void draw(Model3d world)
	{
		//System.out.println(pObject.get(0).getName());
		for(int i=0; i< object.size(); i++)
		{
		Object3d obj = object.get(i);
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
	public void setLinks(Model3d[] pLinks) {
		this.links = pLinks;
	}
	
	/**
	 * @param pLinks the pLinks to set
	 */
	public void setLinks(Model3d pLinks, int index) {
		this.links[index] = pLinks;
	}
	
	/**
	 * @param links the pLinks to set
	 */
	public void setNumLinks(int total) {
		links = new Model3d[total];
		for(int i = 0; i < total; i++)
			links[i] = null;
	}


	/**
	 * @return the pLinks
	 */
	public Model3d[] getLinks() {
		return links;
	}
	
	/**
	 * @return the pLinks
	 */
	public Model3d getLinks(int index) {
		return links[index];
	}


	/**
	 * @param pTags the pTags to set
	 */
	public void setTags(TagMD3[] pTags) {
		this.tags = pTags;
	}
	
	/**
	 * @param pTags the pTags to set
	 */
	public void setTags(TagMD3 pTags, int index) {
		this.tags[index] = pTags;
	}
	
	/**
	 * @param tags the pTags to set
	 */
	public void setNumTags(int total) {
		tags = new TagMD3[total];
				
	}


	/**
	 * @return the pTags
	 */
	public TagMD3[] getTags() {
		return tags;
	}
	
	/**
	 * @return the pTags
	 */
	public TagMD3 getTags(int index) {
		return tags[index];
	}
	
	
	

	
	
	
}
