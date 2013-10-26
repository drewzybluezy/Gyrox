package com.dmurphy.gyrox.model;

import javax.microedition.khronos.opengles.GL10;

public class Video {

	float height, width;
	int x, y;
	public int h, w;

	public int rawHeight, rawWidth;

	int _onScreen;

	public Video(int width, int height) {
		setSize(width, height);
		x = 0;
		y = 0;
		w = Float.floatToIntBits(width);
		h = Float.floatToIntBits(height);
	}

	public void setSize(int width, int height) {
		this.height = Float.intBitsToFloat(height);
		this.width = Float.intBitsToFloat(width);

		this.rawHeight = height;
		this.rawWidth = width;
	}

	public void rasOnly(GL10 gl) {
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadIdentity();
		gl.glOrthof(0.0f, (float) w, 0.0f, (float) h, 0.0f, 1.0f);
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();
		gl.glViewport(0, 0, w, h);
	}

	public void doPerspective(GL10 gl, float GridSize) {
		int w, h;

		float top;
		float left;
		float ratio = width / height;
		float znear = 1.0f;
		float zfar = (float) (GridSize * 6.5f);
		float fov = 105.0f;

		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadIdentity();
		top = (float) Math.tan(fov * Math.PI / 360.0f) * (float) znear;
		left = (float) (((float) -top) * ((float) ratio));
		gl.glFrustumf(left, -left, -top, top, znear, zfar);

		w = Float.floatToIntBits(width);
		h = Float.floatToIntBits(height);
		gl.glViewport(0, 0, w, h);

	}

	public int getWidth() {
		return Float.floatToIntBits(width);
	}

	public int getHeight() {
		return Float.floatToIntBits(height);
	}

	public int getRawHeight() {
		return rawHeight;
	}

	public int getRawWidth() {
		return rawWidth;
	}

}
