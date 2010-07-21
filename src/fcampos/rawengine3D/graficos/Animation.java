package fcampos.rawengine3D.graficos;

import java.util.ArrayList;

/**
The Animation class manages a series of images (frames) and
the amount of time to display each frame.
*/

public class Animation {
	
	private ArrayList<AnimFrame> frames;
	private int currFrameIndex;
	private long animTime;
	private long totalDuration;
	
	public Animation() {
        this(new ArrayList<AnimFrame>(), 0);
    }


    private Animation(ArrayList<AnimFrame> frames, long totalDuration) {
        this.frames = frames;
        this.totalDuration = totalDuration;
        start();
    }


    /**
        Creates a duplicate of this animation. The list of frames
        are shared between the two Animations, but each Animation
        can be animated independently.
    */
    public Object clone() {
        return new Animation(frames, totalDuration);
    }
	
	public synchronized void addFrame(Texture texture, long duration)
	{
		totalDuration += duration;
		frames.add(new AnimFrame(texture, totalDuration));
	}
	
	public synchronized void start() {
		animTime = 0;
		currFrameIndex = 0;
	}
	
	public synchronized void update(float elapsedTime) {
		if (frames.size() > 1) {
			animTime += elapsedTime;
			
			if (animTime >= totalDuration) {
				animTime = animTime % totalDuration;
				currFrameIndex = 0;
			}
			
			while (animTime > getFrame(currFrameIndex).endTime) {
				currFrameIndex++;
			}
		}
	}
	
	public synchronized Texture getImage() {
		if (frames.size() == 0) {
			return null;
		} 
		  else {
		  	return getFrame(currFrameIndex).image;
		  	
		  }
	}
	
	private AnimFrame getFrame(int i) 
	{
		return (AnimFrame)frames.get(i);
	}
	
	
	private class AnimFrame {
		
		Texture image;
		long endTime;
		
		public AnimFrame(Texture image, long endTime) {
			this.image = image;
			this.endTime = endTime;
		}
	}
		
}