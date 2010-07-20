package fcampos.rawengine3D.model;

public class TAnimationInfo {
	
	private String animName;			// This stores the name of the animation (Jump, Pain, etc..)
	private int startFrame;				// This stores the first frame number for this animation
	private int endFrame;				// This stores the last frame number for this animation

	public TAnimationInfo()
	{
		setAnimName(null);
		setStartFrame(0);
		setEndFrame(0);
	}

	public TAnimationInfo(String animFrame, int startFrame, int endFrame)
	{
		setAnimName(animFrame);
		setStartFrame(startFrame);
		setEndFrame(endFrame);
	}
	/**
	 * @param animName the animName to set
	 */
	public void setAnimName(String animName) {
		this.animName = animName;
	}

	/**
	 * @return the animName
	 */
	public String getAnimName() {
		return animName;
	}

	/**
	 * @param startFrame the startFrame to set
	 */
	public void setStartFrame(int startFrame) {
		this.startFrame = startFrame;
	}

	/**
	 * @return the startFrame
	 */
	public int getStartFrame() {
		return startFrame;
	}

	/**
	 * @param endFrame the endFrame to set
	 */
	public void setEndFrame(int endFrame) {
		this.endFrame = endFrame;
	}

	/**
	 * @return the endFrame
	 */
	public int getEndFrame() {
		return endFrame;
	}
}
