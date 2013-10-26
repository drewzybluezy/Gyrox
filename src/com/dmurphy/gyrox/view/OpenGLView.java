package com.dmurphy.gyrox.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

@SuppressLint("ViewConstructor")
public class OpenGLView extends GLSurfaceView {
	private OpenGLRenderer renderer;

	private float x = 0;
	private float y = 0;

	public OpenGLView(Context context, int width, int height) {
		super(context);
		renderer = new OpenGLRenderer(context, width, height);
		setRenderer(renderer);
	}

	public void onPause() {
		renderer.onPause();
	}

	public void onResume() {
		renderer.onResume();
	}

	public boolean onTouchEvent(final MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			x = event.getX();
			y = event.getY();
			renderer.onTouch(x, y);

		}

		return true;
	}
}
