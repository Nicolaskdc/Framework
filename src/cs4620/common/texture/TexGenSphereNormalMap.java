package cs4620.common.texture;

import egl.math.Color;
import egl.math.Colord;
import egl.math.Matrix3d;
import egl.math.Vector2i;
import egl.math.Vector3d;

public class TexGenSphereNormalMap extends ACTextureGenerator {
	// 0.5f means that the discs are tangent to each other
	// For greater values discs intersect
	// For smaller values there is a "planar" area between the discs
	private float bumpRadius;
	// The number of rows and columns
	// There should be one disc in each row and column
	private int resolution;
	
	public TexGenSphereNormalMap() {
		this.bumpRadius = 0.5f;
		this.resolution = 10;
		this.setSize(new Vector2i(256));
	}
	
	public void setBumpRadius(float bumpRadius) {
		this.bumpRadius = bumpRadius;
	}
	
	public void setResolution(int resolution) {
		this.resolution = resolution;
	}
	
	/** applies a transformation to v as defined by the matrix m. */
	public Vector3d multByMatrix(Vector3d v, Matrix3d mat){
		double newX = v.x*mat.m[0] + v.y*mat.m[3] + v.z*mat.m[6];
		double newY = v.x*mat.m[1] + v.y*mat.m[4] + v.z*mat.m[7];
		double newZ = v.x*mat.m[2] + v.y*mat.m[5] + v.z*mat.m[8];
		return new Vector3d(newX, newY, newZ);
	}
	
	/** Returns the position x-y-z determined by the u and v
	 * for a sphere of radius 1 centered at origin */
	public Vector3d getPositionFromUV(float u, float v){
		double phoeta = v*Math.PI; // in radians
		double y = -1*Math.cos(phoeta);
		double r = Math.abs(1*Math.sin(phoeta));
		double phi = u*2*Math.PI; // in radians;
		double x = Math.sin(phi) * r;
		double z = -Math.cos(phi) * r;
		return new Vector3d(x, y, z);
	}
	
	
	@Override
	public void getColor(float u, float v, Color outColor) {
		// TODO A4
		// get our position if sphere is at 0, radius 1 (all that matters is ratios)
		Vector3d pt = getPositionFromUV(u, v);
		// create TBN inverse matrix
		Vector3d normal = (new Vector3d(pt.x, pt.y, pt.z)).normalize();
		Vector3d tangent;
		if(u < .25 || (u > .5 && u < .75)){
			tangent = (new Vector3d(1/pt.x, 0, -1/pt.z)).normalize();
		} else{
			tangent = (new Vector3d(-1/pt.x, 0, 1/pt.z)).normalize();
		}
		Vector3d bitangent = (normal.clone().cross(tangent.clone())).normalize();
		Matrix3d invTBN = (new Matrix3d(tangent.x, bitangent.x, normal.x,
		        					    tangent.y, bitangent.y, normal.y,
		        				  	    tangent.z, bitangent.z, normal.z)).invert();
		
		// ~-- The rest of this function calculates the new normal --~
		Vector3d newNormal = new Vector3d();
		// first determine distance to closest disk center
		float divSize = (float) (1.0/resolution);
		//   -calculate column
		float modU = u%divSize;
		float closestColumn = (u-modU)/divSize;
		if (modU > divSize/2){ closestColumn ++; }
		//   -calculate row
		float modV = v%divSize;
		float closestRow = (v-modV)/divSize;
		if (modV > divSize/2){ closestRow ++; }
		//   -calculate distance
		float centerX = closestColumn*divSize;
		float centerY = closestRow*divSize;
		double distanceToCenter = Math.sqrt(Math.pow((centerX-u), 2) + Math.pow((centerY-v), 2));
		// If we are on disk, use normal from disk center
		//   (the following formula ensures a bumpRadius of 0.5 is tangent circles, and >=1 is full overlap)
		double diskRadius = (divSize/2) * (Math.sqrt(2*bumpRadius));
		if(distanceToCenter <= diskRadius){
			Vector3d diskNormal = getPositionFromUV(centerX, centerY).normalize();
			newNormal = multByMatrix(diskNormal, invTBN).normalize();
		}
		// if not on disk, use position normalized as normal
		else{
			newNormal = multByMatrix(normal, invTBN).normalize();
		}
		
		// Convert from [-1,1] to [0,1] to [0, 255]
		Vector3d converted = new Vector3d();
		converted.setMultiple(0.5, newNormal.clone().normalize());
		converted.add(0.5);
		Colord outColorAccurate = new Colord(converted);
		outColor.set(outColorAccurate);
	}
}
