package com.dmurphy.gyrox.game;

import java.nio.FloatBuffer;
import java.util.Random;

import javax.microedition.khronos.opengles.GL10;

import com.dmurphy.gyrox.video.GraphicUtils;
import com.dmurphy.gyrox.video.Lighting;
import com.dmurphy.gyrox.video.Model;
import com.dmurphy.gyrox.video.Segment;
import com.dmurphy.gyrox.video.Vec;
import com.dmurphy.gyrox.video.Lighting.LightType;

public class SpeedBoost {

private Model model;
	
	private final Random rand = new Random();
	private int trailOffset;
	
	private Segment[] trails = new Segment[1000] ;
	
	FloatBuffer white_fb;
	FloatBuffer posWorld0_fb;
	
	private static final float white[] = { 1.0f, 1.0f, 1.0f, 1.0f };
	private static final float posWorld0[] = {0.5f, 0.5f, 1.0f, 0.0f};
	
	private final int LOD_DIST[][] = {
			{ 1000, 1000, 1000 },
			{100, 200, 400},
			{30,100,200},
			{10,30,150}
	};
	
	public SpeedBoost(float gridSize, Model mesh, float x, float y) {
		model = mesh;
		
		white_fb = GraphicUtils.ConvToFloatBuffer(white);
		posWorld0_fb = GraphicUtils.ConvToFloatBuffer(posWorld0);
		
		trails[0] = new Segment();
		trailOffset = 0;
		trails[trailOffset].vStart.v[0] = x * gridSize;
		trails[trailOffset].vStart.v[1] = y * gridSize;
		trails[trailOffset].vDirection.v[0] = 0.0f;
		trails[trailOffset].vDirection.v[1] = 0.0f;
	}
	
	public void draw(GL10 gl, Lighting lights) {
		gl.glPushMatrix();
		gl.glTranslatef(getXpos(), getYpos(), 0.0f);
		
		gl.glEnable(GL10.GL_LIGHTING);
		gl.glEnable(GL10.GL_LIGHT2);
		
		gl.glLightfv(GL10.GL_LIGHT2, GL10.GL_POSITION, posWorld0_fb);
		gl.glLightfv(GL10.GL_LIGHT2, GL10.GL_AMBIENT, white_fb);
		gl.glLightfv(GL10.GL_LIGHT2, GL10.GL_SPECULAR, white_fb);
		gl.glLightfv(GL10.GL_LIGHT2, GL10.GL_DIFFUSE, white_fb);
		
		gl.glEnable(GL10.GL_DEPTH_TEST);
		gl.glDepthMask(true);
		gl.glEnable(GL10.GL_NORMALIZE);
		gl.glTranslatef(0.0f, 0.0f, model.getBBoxSize().v[2]);
		gl.glScalef(1f, 1f, 1f);
		gl.glRotatef(180, 1.0f, 0, 0);
		gl.glEnable(GL10.GL_CULL_FACE);
		model.draw(gl);
		gl.glDisable(GL10.GL_CULL_FACE);
		
		gl.glDisable(GL10.GL_BLEND);
		gl.glDisable(GL10.GL_LIGHTING);
		gl.glDisable(GL10.GL_LIGHT2);
		gl.glPopMatrix();

	}
	
	public float getXpos() {
		return trails[trailOffset].vStart.v[0] + trails[trailOffset].vDirection.v[0];
	}
	
	public float getYpos() {
		return trails[trailOffset].vStart.v[1] + trails[trailOffset].vDirection.v[1];
	}
	
	public Model getModel() {
		return model;
	}
}
