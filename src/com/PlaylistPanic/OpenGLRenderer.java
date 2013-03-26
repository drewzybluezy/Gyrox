
package com.PlaylistPanic;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.util.Log;

import com.PlaylistPanic.Game.PlaylistPanicGame;

public class OpenGLRenderer implements GLSurfaceView.Renderer {

	PlaylistPanicGame Game = new PlaylistPanicGame();
	
	Context mContext;
	
	String Debug;
	StringBuffer sb = new StringBuffer(40);

	private int frameCount = 0;
	
	public OpenGLRenderer(Context context, int win_width, int win_height) {
		mContext = context;
		Log.e("PlaylistPanic", "Renderer Constructor: Create Video Object");
		Debug = sb.append("Screen size = ").append(win_width).append(",").append(win_height).toString();
		Log.e("PlaylistPanic", Debug);
		Game.updateScreenSize(win_width, win_height);
	}
	
	public void onTouch(float x, float y) {
		Game.addTouchEvent(x, y);
	}
	
	public void onPause() {
		Game.pauseGame();
	}
	
	public void onResume() {
		Game.resumeGame();
	}
	
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {

	    Log.e("PlaylistPanic", "Renderer: Surface Created Do perspective");
	    Game.drawSplash(mContext, gl);
	}

	
	@Override
	public void onSurfaceChanged(GL10 gl, int w, int h) {
		Log.e("PlaylistPanic", "Renderer: Surface changed");
		sb=null;
		sb = new StringBuffer(40);
		Debug = sb.append("Screen size = ").append(w).append(",").append(h).toString();
		Log.e("PlaylistPanic", Debug);
		Game.updateScreenSize(w, h);
	}
	
	@Override
	public void onDrawFrame(GL10 gl) {

		if(frameCount == 1) {
			Game.initGame();
		}
		
		else if(frameCount > 1){
			Game.runGame();
		}

		frameCount++;	
	}

	
}
