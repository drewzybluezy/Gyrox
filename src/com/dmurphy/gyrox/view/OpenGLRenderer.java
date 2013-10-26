package com.dmurphy.gyrox.view;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.dmurphy.gyrox.game.GameState;
import com.dmurphy.gyrox.game.StateManager;
import com.dmurphy.gyrox.game.StateManager.StateID;
import com.dmurphy.gyrox.model.Video;

public class OpenGLRenderer implements GLSurfaceView.Renderer {

	Context mContext;

	String debug;
	StringBuffer sb = new StringBuffer(40);
	GL10 gl;
	Video video;

	private int frameCount = 0;

	public OpenGLRenderer(Context context, int win_width, int win_height) {
		mContext = context;
		updateScreenSize(win_width, win_height);
		
		Log.e("GyroxLauncher", "Renderer Constructor: Create Video Object");
		debug = sb.append("Screen size = ").append(win_width).append(",")
				.append(win_height).toString();
		Log.e("GyroxLauncher", debug);

		StateManager.init();
		
	}

	public void onTouch(float x, float y) {
		StateManager.getCurrentState().addTouchEvent(x, y);
	}

	public void onPause() {
		if (StateManager.getCurrentState() instanceof GameState) {
			GameState game = (GameState) StateManager.getCurrentState();
			game.pauseGame();
		}
	}

	public void onResume() {
		if (StateManager.getCurrentState() instanceof GameState) {
			GameState game = (GameState) StateManager.getCurrentState();
			game.resumeGame();
		}
	}

	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		Log.e("GyroxLauncher", "Renderer: Surface Created Do perspective");
		this.gl = gl;
		StateManager.runState(StateID.SPLASH_STATE, mContext, gl, video);
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int w, int h) {
		Log.e("GyroxLauncher", "Renderer: Surface changed");
		sb = null;
		sb = new StringBuffer(40);
		debug = sb.append("Screen size = ").append(w).append(",").append(h)
				.toString();
		Log.e("GyroxLauncher", debug);
		updateScreenSize(w, h);
	}

	@Override
	public void onDrawFrame(GL10 gl) {
		if (frameCount == 1) {
			StateManager.getCurrentState().nextState(StateID.GAME_STATE);
		} else if (frameCount > 1 && StateManager.getCurrentState() instanceof GameState) {
			GameState s = (GameState)StateManager.getCurrentState();
			s.runGame();
		}

		frameCount++;
	}
	
	public void updateScreenSize(int width, int height) {
		if (video == null) {
			video = new Video(width, height);
		} else {
			video.setSize(width, height);
		}

	}

}
