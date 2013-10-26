package com.dmurphy.gyrox.game;

import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;

import com.dmurphy.gyrox.R;
import com.dmurphy.gyrox.game.StateManager.StateID;
import com.dmurphy.gyrox.model.GLTexture;
import com.dmurphy.gyrox.model.Video;
import com.dmurphy.gyrox.util.GraphicUtils;

public class SplashState implements State {

	GL10 gl;
	Context mContext;
	private GLTexture splashScreen;
	private Video video;

	@Override
	public void init(Context ctx, GL10 gl, Video video) {
		float verts[] = { -1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, -1.0f, -1.0f,
				0.0f, 1.0f, -1.0f, 0.0f };

		float texture[] = { 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f };

		this.gl = gl;
		this.video = video;
		mContext = ctx;

		FloatBuffer vertfb = GraphicUtils.convToFloatBuffer(verts);
		FloatBuffer texfb = GraphicUtils.convToFloatBuffer(texture);

		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

		gl.glLoadIdentity();

		gl.glEnable(GL10.GL_TEXTURE_2D);
		if (splashScreen == null)
			splashScreen = new GLTexture(gl, mContext, R.drawable.splash);

		gl.glBindTexture(GL10.GL_TEXTURE_2D, splashScreen.getTextureID());

		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertfb);
		gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, texfb);
		gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);

		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL10.GL_NORMAL_ARRAY);
		gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
	}

	@Override
	public void nextState(StateID state) {
		StateManager.runState(state, mContext, gl, video);
	}

	@Override
	public void addTouchEvent(float x, float y) {
	}

}
