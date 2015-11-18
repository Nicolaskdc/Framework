package cs4620.anim;

import java.util.HashMap;

import cs4620.common.Scene;
import cs4620.common.SceneCamera;
import cs4620.common.SceneObject;
import cs4620.common.event.SceneTransformationEvent;
import egl.math.Matrix4;
import egl.math.Vector3;
import egl.math.Matrix3;
import egl.math.Quat;

/**
 * A Component Resting Upon Scene That Gives
 * Animation Capabilities
 * @author Cristian
 *
 */
public class AnimationEngine {
	/**
	 * The First Frame In The Global Timeline
	 */
	private int frameStart = 0;
	/**
	 * The Last Frame In The Global Timeline
	 */
	private int frameEnd = 100;
	/**
	 * The Current Frame In The Global Timeline
	 */
	private int curFrame = 0;
	/**
	 * Scene Reference
	 */
	private final Scene scene;
	/**
	 * Animation Timelines That Map To Object Names
	 */
	public final HashMap<String, AnimTimeline> timelines = new HashMap<>();

	/**
	 * An Animation Engine That Works Only On A Certain Scene
	 * @param s The Working Scene
	 */
	public AnimationEngine(Scene s) {
		scene = s;
	}
	
	/**
	 * Set The First And Last Frame Of The Global Timeline
	 * @param start First Frame
	 * @param end Last Frame (Must Be Greater Than The First
	 */
	public void setTimelineBounds(int start, int end) {
		// Make Sure Our End Is Greater Than Our Start
		if(end < start) {
			int buf = end;
			end = start;
			start = buf;
		}
		
		frameStart = start;
		frameEnd = end;
		moveToFrame(curFrame);
	}
	/**
	 * Add An Animating Object
	 * @param oName Object Name
	 * @param o Object
	 */
	public void addObject(String oName, SceneObject o) {
		timelines.put(oName, new AnimTimeline(o));
	}
	/**
	 * Remove An Animating Object
	 * @param oName Object Name
	 */
	public void removeObject(String oName) {
		timelines.remove(oName);
	}

	/**
	 * Set The Frame Pointer To A Desired Frame (Will Be Bounded By The Global Timeline)
	 * @param f Desired Frame
	 */
	public void moveToFrame(int f) {
		if(f < frameStart) f = frameStart;
		else if(f > frameEnd) f = frameEnd;
		curFrame = f;
	}
	/**
	 * Looping Forwards Play
	 * @param n Number Of Frames To Move Forwards
	 */
	public void advance(int n) {
		curFrame += n;
		if(curFrame > frameEnd) curFrame = frameStart + (curFrame - frameEnd - 1);
	}
	/**
	 * Looping Backwards Play
	 * @param n Number Of Frames To Move Backwards
	 */
	public void rewind(int n) {
		curFrame -= n;
		if(curFrame < frameStart) curFrame = frameEnd - (frameStart - curFrame - 1);
	}

	public int getCurrentFrame() {
		return curFrame;
	}
	public int getFirstFrame() {
		return frameStart;
	}
	public int getLastFrame() {
		return frameEnd;
	}
	public int getNumFrames() {
		return frameEnd - frameStart + 1;
	}

	/**
	 * Adds A Keyframe For An Object At The Current Frame
	 * Using The Object's Transformation - (CONVENIENCE METHOD)
	 * @param oName Object Name
	 */
	public void addKeyframe(String oName) {
		AnimTimeline tl = timelines.get(oName);
		if(tl == null) return;
		tl.addKeyFrame(getCurrentFrame(), tl.object.transformation);
	}
	/**
	 * Removes A Keyframe For An Object At The Current Frame
	 * Using The Object's Transformation - (CONVENIENCE METHOD)
	 * @param oName Object Name
	 */
	public void removeKeyframe(String oName) {
		AnimTimeline tl = timelines.get(oName);
		if(tl == null) return;
		tl.removeKeyFrame(getCurrentFrame(), tl.object.transformation);
	}
	
	/**
	 * Loops Through All The Animating Objects And Updates Their Transformations To
	 * The Current Frame - For Each Updated Transformation, An Event Has To Be 
	 * Sent Through The Scene Notifying Everyone Of The Change
	 */

	// TODO A6 - Animation

	 public void updateTransformations() {
		// Loop Through All The Timelines
		// And Update Transformations Accordingly
		// (You WILL Need To Use this.scene)
		 
		// Loop through each object
		for(SceneObject object : this.scene.objects) {
			 
			// get object's timeline
			String objectString = object.getID().name;
			AnimTimeline theTimeLine = timelines.get(objectString);

			// only proceed if object can be animated
			// (meaning has a defined timeline in [timelines])
			if(theTimeLine != null){
				
				// extract transformation matrices for surrounding keyframes
				AnimKeyframe[] surroundingFrames = new AnimKeyframe[2];
				theTimeLine.getSurroundingFrames(curFrame, surroundingFrames);
				Matrix4 prevTransf = surroundingFrames[0].transformation.clone();
				Matrix4 nextTransf = surroundingFrames[1].transformation.clone();
				 
				// get progression to next keyframe to use in interpolation
				// (only proceed if have more than one keyframe)
				int prevFrame = surroundingFrames[0].frame;
				int nextFrame = surroundingFrames[1].frame;
				float progress = 1; // progression if not between keyframes
				if(prevFrame != nextFrame){
					progress = ((float) curFrame-prevFrame)/(nextFrame-prevFrame);
				}
					
				// Extract translations and linearly interpolate them
				Vector3 prevTransl =  new Vector3(prevTransf.get(0,3), 
												  prevTransf.get(1,3), 
												  prevTransf.get(2,3));
				Vector3 nextTransl =  new Vector3(nextTransf.get(0,3), 
												  nextTransf.get(1,3), 
												  nextTransf.get(2,3));
				Vector3 newTranslation = prevTransl.clone();
				newTranslation.lerp(nextTransl, progress);
				Matrix4 T = Matrix4.createTranslation(newTranslation);
					
				// Extract scaling and rotations
				Matrix3 prevRS = new Matrix3(prevTransf);
				Matrix3 nextRS = new Matrix3(nextTransf);
					
				Matrix3 prevRot = new Matrix3();
				Matrix3 prevScale = new Matrix3();
				prevRS.polar_decomp(prevRot,  prevScale);
				Matrix3 nextRot = new Matrix3();
				Matrix3 nextScale = new Matrix3();
				nextRS.polar_decomp(nextRot,  nextScale);
					
				// interpolate scales linearly
				Matrix3 newScale = new Matrix3();
				newScale.interpolate(prevScale, nextScale, progress);
				Matrix4 S = new Matrix4(newScale);

				// slerp rotation matrix
				Quat prevQ = new Quat(prevRot);
				Quat nextQ = new Quat(nextRot);
				Quat newQ = Quat.slerp(prevQ, nextQ, progress);
				Matrix4 newR = new Matrix4();
				newQ.toRotationMatrix(newR);
				Matrix4 R = new Matrix4(newR);
					
				// combine interpolated R,S,and T
				Matrix4 finalTransform = T.mulBefore(R.mulBefore(S));
				
				// send the event
				object.transformation.set(finalTransform);
				scene.sendEvent(new SceneTransformationEvent(object));
			}
		}
	 }
}