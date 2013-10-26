package com.dmurphy.gyrox.view;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.dmurphy.gyrox.game.GyroxMain;

public class OpenGLRenderer implements GLSurfaceView.Renderer {

	GyroxMain game = new GyroxMain();

	Context mContext;

	String debug;
	StringBuffer sb = new StringBuffer(40);

	private int frameCount = 0;

	public OpenGLRenderer(Context context, int win_width, int win_height) {
		mContext = context;
		Log.e("GyroxLauncher", "Renderer Constructor: Create Video Object");
		debug = sb.append("Screen size = ").append(win_width).append(",")
				.append(win_height).toString();
		Log.e("GyroxLauncher", debug);
		game.updateScreenSize(win_width, win_height);
	}

	public void onTouch(float x, float y) {
		game.addTouchEvent(x, y);
	}

	public void onPause() {
		game.pauseGame();
	}

	public void onResume() {
		game.resumeGame();
	}

	public void onSurfaceCreated(GL10 gl, EGLConfig config) {

		Log.e("GyroxLauncher", "Renderer: Surface Created Do perspective");
		game.drawSplash(mContext, gl);
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int w, int h) {
		Log.e("GyroxLauncher", "Renderer: Surface changed");
		sb = null;
		sb = new StringBuffer(40);
		debug = sb.append("Screen size = ").append(w).append(",").append(h)
				.toString();
		Log.e("GyroxLauncher", debug);
		game.updateScreenSize(w, h);
	}

	@Override
	public void onDrawFrame(GL10 gl) {

		if (frameCount == 1) {
			game.initGame();
		}

		else if (frameCount > 1) {
			game.runGame();
		}

		frameCount++;
	}

}
