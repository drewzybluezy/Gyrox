package com.dmurphy.gyrox.entity;

import java.util.Random;

import javax.microedition.khronos.opengles.GL10;

import com.dmurphy.gyrox.model.Model;
import com.dmurphy.gyrox.model.Vector;
import com.dmurphy.gyrox.ui.Camera;
import com.dmurphy.gyrox.world.Lighting;

public class Pickup {

	private Model model;

	private float zRot = 0;
	private Vector position;

	private final Random rand = new Random();

	private int diffuse;
	private int specular;

	private final int LOD_DIST[][] = { { 1000, 1000, 1000 }, { 100, 200, 400 },
			{ 30, 100, 200 }, { 10, 30, 150 } };

	private final float cDiffuse[][] = { { 0.0f, 0.1f, 0.900f, 1.000f }, // Blue
			{ 1.00f, 0.550f, 0.140f, 1.000f }, // Yellow
			{ 0.750f, 0.020f, 0.020f, 1.000f }, // Red
			{ 0.800f, 0.800f, 0.800f, 1.000f }, // Grey
			{ 0.120f, 0.750f, 0.0f, 1.000f }, // Green
			{ 0.750f, 0.0f, 0.35f, 1.000f } // Purple

	};

	private final float cSpecular[][] = { { 0.0f, 0.1f, 0.900f, 1.000f }, // Blue
			{ 0.500f, 0.500f, 0.000f, 1.000f }, // Yellow
			{ 0.750f, 0.020f, 0.020f, 1.000f }, // Red
			{ 1.00f, 1.00f, 1.00f, 1.000f }, // Grey
			{ 0.050f, 0.500f, 0.00f, 1.00f }, // Green
			{ 0.500f, 0.000f, 0.500f, 1.00f }, // Purple
	};

	public Pickup(float gridSize, Model mesh) {
		model = mesh;

		diffuse = rand.nextInt(6);
		specular = rand.nextInt(6);

		position = new Vector(rand.nextFloat() * gridSize, rand.nextFloat()
				* gridSize, 0);

	}

	public void draw(GL10 gl, Lighting Lights, long delta) {
		zRot += delta / 5.0f;
		gl.glPushMatrix();
		gl.glTranslatef(getXCoord(), getYCoord(), 0.0f);

		gl.glEnable(GL10.GL_LIGHTING);
		gl.glEnable(GL10.GL_DEPTH_TEST);
		gl.glDepthMask(true);
		gl.glEnable(GL10.GL_NORMALIZE);
		gl.glTranslatef(0.0f, 0.0f, model.getBBoxSize().point[2] / 8f);
		gl.glScalef(0.2f, 0.2f, 0.2f);
		gl.glRotatef(zRot, 0, 0, 1);
		gl.glEnable(GL10.GL_CULL_FACE);
		model.draw(gl, cSpecular[diffuse], cDiffuse[specular]);
		gl.glDisable(GL10.GL_CULL_FACE);

		gl.glDisable(GL10.GL_BLEND);
		gl.glDisable(GL10.GL_LIGHTING);
		gl.glPopMatrix();

	}

	public boolean isVisible(Camera cam) {
		Vector v1;
		Vector v2;
		Vector tmp = new Vector(getXCoord(), getYCoord(), 0.0f);
		int levelOfDetail = 2;
		float d, s;
		int i;
		int LC_LOD = 3;
		float fov = 120;

		boolean retValue;

		v1 = cam.getTarget().subtract(cam.getCam());
		v1.normalize();

		v2 = cam.getCam().subtract(tmp);

		d = v2.length();

		for (i = 0; i < LC_LOD && d >= LOD_DIST[levelOfDetail][i]; i++)
			;

		if (i >= LC_LOD) {
			retValue = false;
		} else {
			v2 = tmp.subtract(cam.getCam());
			v2.normalize();

			s = v1.dot(v2);
			d = (float) Math.cos((fov / 2) * 2 * Math.PI / 360.0f);

			if (s < d - (model.getBBoxRadius() * 2.0f)) {
				retValue = false;
			} else {
				retValue = true;
			}

		}

		return retValue;
	}

	public float getXCoord() {
		return position.point[0];
	}

	public float getYCoord() {
		return position.point[1];
	}

	public Model getModel() {
		return model;
	}

}
