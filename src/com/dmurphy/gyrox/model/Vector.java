package com.dmurphy.gyrox.model;

public class Vector {

	public float point[] = new float[3];

	public Vector() {
		point[0] = 0.0f;
		point[1] = 0.0f;
		point[2] = 0.0f;
	}

	public Vector(float x, float y, float z) {
		point[0] = x;
		point[1] = y;
		point[2] = z;
	}

	public void copy(Vector V1) {
		point[0] = V1.point[0];
		point[1] = V1.point[1];
		point[2] = V1.point[2];
	}

	public Vector add(Vector V1) {
		Vector ReturnResult = new Vector();

		ReturnResult.point[0] = point[0] + V1.point[0];
		ReturnResult.point[1] = point[1] + V1.point[1];
		ReturnResult.point[2] = point[2] + V1.point[2];

		return ReturnResult;
	}

	public Vector subtract(Vector V1) {
		Vector ReturnResult = new Vector();

		ReturnResult.point[0] = point[0] - V1.point[0];
		ReturnResult.point[1] = point[1] - V1.point[1];
		ReturnResult.point[2] = point[2] - V1.point[2];

		return ReturnResult;
	}

	public void multiply(float Mul) {
		point[0] *= Mul;
		point[1] *= Mul;
		point[2] *= Mul;
	}

	public void scale(float fScale) {
		point[0] *= fScale;
		point[1] *= fScale;
		point[2] *= fScale;
	}

	public Vector cross(Vector V1) {
		Vector ReturnResult = new Vector();

		ReturnResult.point[0] = point[1] * V1.point[2] - point[2] * V1.point[1];
		ReturnResult.point[1] = point[2] * V1.point[0] - point[0] * V1.point[2];
		ReturnResult.point[2] = point[0] * V1.point[1] - point[1] * V1.point[0];

		return ReturnResult;
	}

	public float dot(Vector V1) {
		return (point[0] * V1.point[0] + point[1] * V1.point[1] + point[2] * V1.point[2]);
	}

	public float length() {
		return (float) (Math.sqrt(point[0] * point[0] + point[1] * point[1] + point[2] * point[2]));
	}

	public float length2() {
		return (float) (Math.sqrt(point[0] * point[0] + point[1] * point[1]));
	}

	public void normalize() {
		float d = length();

		if (d != 0) {
			point[0] /= d;
			point[1] /= d;
			point[2] /= d;
		}
	}

	public void normalize2() {
		float d = length2();

		if (d != 0) {
			point[0] /= d;
			point[1] /= d;
		}
	}

	public Vector ortho() {
		Vector ReturnResult = new Vector();

		ReturnResult.point[0] = point[1];
		ReturnResult.point[1] = -point[0];

		return ReturnResult;
	}

}
