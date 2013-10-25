

package com.dmurphy.gyrox.video;

public class Vec {

	public float v[] = new float[3];
	
	public Vec() {
		v[0] = 0.0f;
		v[1] = 0.0f;
		v[2] = 0.0f;
	}
	
	public Vec(float x, float y, float z) {
		v[0] = x;
		v[1] = y;
		v[2] = z;
	}
	
	public void copy(Vec V1)
	{
		v[0] = V1.v[0];
		v[1] = V1.v[1];
		v[2] = V1.v[2];
	}
	
	public Vec add(Vec V1) {
		Vec ReturnResult = new Vec();
		
		ReturnResult.v[0] = v[0] + V1.v[0];
		ReturnResult.v[1] = v[1] + V1.v[1];
		ReturnResult.v[2] = v[2] + V1.v[2];
		
		return ReturnResult;
	}
	
	public Vec sub(Vec V1) {
		Vec ReturnResult = new Vec();
		
		ReturnResult.v[0] = v[0] - V1.v[0];
		ReturnResult.v[1] = v[1] - V1.v[1];
		ReturnResult.v[2] = v[2] - V1.v[2];
		
		return ReturnResult;
	}
	
	public void mul(float Mul) {
		v[0] *= Mul;
		v[1] *= Mul;
		v[2] *= Mul;
	}
	
	public void scale(float fScale) {
		v[0] *= fScale;
		v[1] *= fScale;
		v[2] *= fScale;
	}
	
	public Vec cross(Vec V1) {
		Vec ReturnResult = new Vec();
		
		ReturnResult.v[0] = v[1] * V1.v[2] - v[2] * V1.v[1];
		ReturnResult.v[1] = v[2] * V1.v[0] - v[0] * V1.v[2];
		ReturnResult.v[2] = v[0] * V1.v[1] - v[1] * V1.v[0];
		
		return ReturnResult;
	}
	
	public float dot(Vec V1) {
		return (v[0] * V1.v[0] + v[1] * V1.v[1] + v[2] * V1.v[2]);
	}
	
	public float length() {
		return (float) (Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]));
	}
	
	public float length2() {
		return (float) (Math.sqrt(v[0] * v[0] + v[1] * v[1]));
	}

	public void normalize(){
		float d = length();
		
		if(d != 0) {
			v[0] /= d;
			v[1] /= d;
			v[2] /= d;
		}
	}
	
	public void normalize2() {
		float d = length2();
		
		if(d != 0)
		{
			v[0] /= d;
			v[1] /= d;
		}
	}
	
	public Vec ortho() {
		Vec ReturnResult = new Vec();
		
		ReturnResult.v[0] = v[1];
		ReturnResult.v[1] = -v[0];
		
		return ReturnResult;
	}
	
}
