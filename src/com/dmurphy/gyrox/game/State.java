package com.dmurphy.gyrox.game;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;

import com.dmurphy.gyrox.game.StateManager.StateID;
import com.dmurphy.gyrox.model.Video;

public interface State {
	
	public void init(Context ctx, GL10 gl, Video video);
	
	public void nextState(StateID state);
	
	public void addTouchEvent(float x, float y);

}
