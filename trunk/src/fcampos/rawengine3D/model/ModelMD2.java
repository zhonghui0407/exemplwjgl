package fcampos.rawengine3D.model;

import java.util.Vector;

import fcampos.rawengine3D.loader.LoaderMD2;

public class ModelMD2 extends Model3d{
	
	private int numOfAnimations;				// The number of animations in this model 
	private int currentAnim;					// The current index into pAnimations list (NEW)
	private int currentFrame;					// The current frame of the current animation (NEW)
	private String textureName;
		
	private Vector<AnimationInfo> animations; // The list of animations 
	
	private LoaderMD2 loaderMD2;
	
	public ModelMD2(String textureName)
	{
		super();
		
		numOfAnimations = 0;
		animations = new Vector<AnimationInfo>();
		this.textureName = textureName;
		
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


	@Override
	public boolean load(String fileName)
	{
		
		loaderMD2 = new LoaderMD2();
		try{
			loaderMD2.importMD2(this, fileName, textureName);
			return true;
		}catch (Exception e) {
			System.out.println(e.getMessage());
			return false;
		}
		
	}



}
