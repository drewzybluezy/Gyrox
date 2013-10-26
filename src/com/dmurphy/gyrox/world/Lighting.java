

package com.dmurphy.gyrox.world;

import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

import com.dmurphy.gyrox.util.GraphicUtils;

public class Lighting {

	FloatBuffer white_fb;
	FloatBuffer gray66_fb;
	FloatBuffer gray10_fb;
	FloatBuffer black_fb;
	FloatBuffer posWorld0_fb;
	FloatBuffer posWorld1_fb;
	FloatBuffer posCycles_fb;
	
	private static final float white[] = { 1.0f, 1.0f, 1.0f, 1.0f };
	private static final float gray66[] = { 0.66f, 0.66f, 0.66f, 1.0f };
	private static final float gray10[] = { 0.1f, 0.1f, 0.1f, 1.0f };
	private static final float black[] = { 0.0f, 0.0f, 0.0f, 1.0f };
	
	private static final float posWorld0[] = {1.0f, 0.8f, 0.0f, 0.0f};
	private static final float posWorld1[] = {-1.0f, -0.8f, 0.0f, 0.0f};
	private static final float posCycles[] = {0.5f, 0.5f, 1.0f, 0.0f};
	
	public enum LightType {
		E_WORLD_LIGHTS,
		E_CYCLE_LIGHTS,
		E_RECOGNISER_LIGHTS
	}
	
	public Lighting() {
		white_fb = GraphicUtils.convToFloatBuffer(white);
		gray66_fb = GraphicUtils.convToFloatBuffer(gray66);
		gray10_fb = GraphicUtils.convToFloatBuffer(gray10);
		black_fb = GraphicUtils.convToFloatBuffer(black);
		posWorld0_fb = GraphicUtils.convToFloatBuffer(posWorld0);
		posWorld1_fb = GraphicUtils.convToFloatBuffer(posWorld1);
		posCycles_fb = GraphicUtils.convToFloatBuffer(posCycles);
		
	}
	
	public void setupLights(GL10 gl, LightType lightType) {
		// Turn off global ambient lighting
		gl.glLightModelfv(GL10.GL_LIGHT_MODEL_AMBIENT, black_fb);
		gl.glLightModelf(GL10.GL_LIGHT_MODEL_TWO_SIDE, 1.0f);
		
		if(lightType == LightType.E_WORLD_LIGHTS) {
			gl.glEnable(GL10.GL_LIGHTING);
			gl.glMatrixMode(GL10.GL_MODELVIEW);
			gl.glPushMatrix();
			
			gl.glEnable(GL10.GL_LIGHT0);
			gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, posWorld0_fb);
			gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_AMBIENT, gray10_fb);
			gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_SPECULAR, white_fb);
			gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_DIFFUSE, white_fb);
			
			gl.glEnable(GL10.GL_LIGHT1);
			gl.glLightfv(GL10.GL_LIGHT1, GL10.GL_POSITION, posWorld1_fb);
			gl.glLightfv(GL10.GL_LIGHT1, GL10.GL_AMBIENT, black_fb);
			gl.glLightfv(GL10.GL_LIGHT1, GL10.GL_SPECULAR, gray66_fb);
			gl.glLightfv(GL10.GL_LIGHT1, GL10.GL_DIFFUSE, gray66_fb);
			gl.glPopMatrix();
			
		} else {
			gl.glEnable(GL10.GL_LIGHTING);
			gl.glEnable(GL10.GL_LIGHT0);
			
			gl.glMatrixMode(GL10.GL_MODELVIEW);
			gl.glPushMatrix();
			gl.glLoadIdentity();
			
			gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, posCycles_fb);
			gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_AMBIENT, gray10_fb);
			gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_SPECULAR, white_fb);
			gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_DIFFUSE, white_fb);
			
			gl.glDisable(GL10.GL_LIGHT1);
			
			gl.glPopMatrix();
		}
			
	}
	
}
