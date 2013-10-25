package com.dmurphy.gyrox.game;

import java.util.Random;

import javax.microedition.khronos.opengles.GL10;

import com.dmurphy.gyrox.video.GLTexture;
import com.dmurphy.gyrox.video.HUD;
import com.dmurphy.gyrox.video.Lighting;
import com.dmurphy.gyrox.video.Model;
import com.dmurphy.gyrox.video.Segment;
import com.dmurphy.gyrox.video.Vec;

public class Note {

	private Model model;
	
	private float zRot = 0;
	
	private final Random rand = new Random();
	private int trailOffset;
	
	private Segment[] trails = new Segment[1000] ;
	
	private int diffuse;
	private int specular;
	
	private final int LOD_DIST[][] = {
			{ 1000, 1000, 1000 },
			{100, 200, 400},
			{30,100,200},
			{10,30,150}
	};
	
	private final float ColourDiffuse[][] = {
			{ 0.0f, 0.1f, 0.900f, 1.000f},      // Blue
			{ 1.00f, 0.550f, 0.140f, 1.000f},   // Yellow
			{ 0.750f, 0.020f, 0.020f, 1.000f},  // Red
			{ 0.800f, 0.800f, 0.800f, 1.000f},  // Grey
			{ 0.120f, 0.750f, 0.0f, 1.000f},    // Green
			{ 0.750f, 0.0f, 0.35f, 1.000f}      // Purple
			
			
	};
	
	private final float ColourSpecular[][] = {
			{ 0.0f, 0.1f, 0.900f, 1.000f},    // Blue
			{0.500f, 0.500f, 0.000f, 1.000f}, // Yellow
			{0.750f, 0.020f, 0.020f, 1.000f}, // Red
			{1.00f, 1.00f, 1.00f, 1.000f},    // Grey
			{0.050f, 0.500f, 0.00f, 1.00f},   // Green
			{0.500f, 0.000f, 0.500f, 1.00f},  // Purple
	};
	
	public Note(float gridSize, Model mesh) {
		model = mesh;
		
		diffuse = rand.nextInt(6);
		specular = rand.nextInt(6);
		
		trails[0] = new Segment();
		trailOffset = 0;
		trails[trailOffset].vStart.v[0] = rand.nextFloat()* gridSize;
		trails[trailOffset].vStart.v[1] = rand.nextFloat() * gridSize;
		trails[trailOffset].vDirection.v[0] = 0.0f;
		trails[trailOffset].vDirection.v[1] = 0.0f;
	}
	
	public void draw(GL10 gl, Lighting Lights, long delta) {
		zRot += delta / 5.0f;
		gl.glPushMatrix();
		gl.glTranslatef(getXpos(), getYpos(), 0.0f);
		
		gl.glEnable(GL10.GL_LIGHTING);
		gl.glEnable(GL10.GL_DEPTH_TEST);
		gl.glDepthMask(true);
		gl.glEnable(GL10.GL_NORMALIZE);
		gl.glTranslatef(0.0f, 0.0f, model.getBBoxSize().v[2] / 8f);
		gl.glScalef(0.2f, 0.2f, 0.2f);
		gl.glRotatef(zRot, 0, 0, 1);
		gl.glEnable(GL10.GL_CULL_FACE);
		model.draw(gl,ColourSpecular[diffuse],ColourDiffuse[specular]);
		gl.glDisable(GL10.GL_CULL_FACE);
		
		gl.glDisable(GL10.GL_BLEND);
		gl.glDisable(GL10.GL_LIGHTING);
		gl.glPopMatrix();

	}
	
	public boolean isVisible(Camera cam) {
		Vec v1;
		Vec v2;
		Vec tmp = new Vec(getXpos(),getYpos(),0.0f);
		int lod_level = 2;
		float d,s;
		int i;
		int LC_LOD = 3;
		float fov = 120;
		
		boolean retValue;
		
		v1 = cam._target.sub(cam._cam);
		v1.normalize();
		
		v2 = cam._cam.sub(tmp);
		
		d = v2.length();
		
		for(i=0;i<LC_LOD && d >= LOD_DIST[lod_level][i]; i++);
		
		if(i >= LC_LOD) {
			retValue = false;
		}
		else {
			v2 = tmp.sub(cam._cam);
			v2.normalize();
			
			s = v1.dot(v2);
			d = (float)Math.cos((fov/2) * 2 * Math.PI / 360.0f);
			
			if(s < d - (model.getBBoxRadius() * 2.0f)) {
				retValue = false;
			}
			else {
				retValue = true;
			}
			
		}
		
		return retValue;
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
