package com.dmurphy.gyrox.model;

import javax.microedition.khronos.opengles.GL10;

public class Video {

	float rawHeight, rawWidth;
	int vX, vY;
	public int vH;
	public int vW;

	int _onScreen;

	public Video(int width, int height) {
		setSize(width, height);
		vX = 0;
		vY = 0;
		vW = Float.floatToIntBits(rawWidth);
		vH = Float.floatToIntBits(rawHeight);
	}

	public void setSize(int width, int height) {
		rawHeight = Float.intBitsToFloat(height);
		rawWidth = Float.intBitsToFloat(width);
	}

	public void rasOnly(GL10 gl) {
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadIdentity();
		gl.glOrthof(0.0f, (float) vW, 0.0f, (float) vH, 0.0f, 1.0f);
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();
		gl.glViewport(0, 0, vW, vH);
	}

	public void doPerspective(GL10 gl, float GridSize) {
		int w, h;

		float top;
		float left;
		float ratio = rawWidth / rawHeight;
		float znear = 1.0f;
		float zfar = (float) (GridSize * 6.5f);
		float fov = 105.0f;

		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadIdentity();
		top = (float) Math.tan(fov * Math.PI / 360.0f) * (float) znear;
		left = (float) (((float) -top) * ((float) ratio));
		gl.glFrustumf(left, -left, -top, top, znear, zfar);

		w = Float.floatToIntBits(rawWidth);
		h = Float.floatToIntBits(rawHeight);
		gl.glViewport(0, 0, w, h);

	}

	public int getWidth() {
		return Float.floatToIntBits(rawWidth);
	}

	public int getHeight() {
		return Float.floatToIntBits(rawHeight);
	}

}
