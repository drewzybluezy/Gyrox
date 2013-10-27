package com.dmurphy.gyrox.entity;

import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

import com.dmurphy.gyrox.model.Model;
import com.dmurphy.gyrox.model.Vector;
import com.dmurphy.gyrox.util.GraphicUtils;
import com.dmurphy.gyrox.world.Lighting;

public class SpeedBoost {

	private Model model;
	private Vector position;

	FloatBuffer whiteBuffer;
	FloatBuffer worldBuffer;

	private static final float white[] = { 1.0f, 1.0f, 1.0f, 1.0f };
	private static final float world[] = { 0.5f, 0.5f, 1.0f, 0.0f };

	public SpeedBoost(float gridSize, Model mesh, float x, float y) {
		model = mesh;

		whiteBuffer = GraphicUtils.convToFloatBuffer(white);
		worldBuffer = GraphicUtils.convToFloatBuffer(world);
		
		position = new Vector(x*gridSize, y*gridSize, 0);
	}

	public void draw(GL10 gl, Lighting lights) {
		gl.glPushMatrix();
		gl.glTranslatef(getXpos(), getYpos(), 0.0f);

		gl.glEnable(GL10.GL_LIGHTING);
		gl.glEnable(GL10.GL_LIGHT2);

		gl.glLightfv(GL10.GL_LIGHT2, GL10.GL_POSITION, worldBuffer);
		gl.glLightfv(GL10.GL_LIGHT2, GL10.GL_AMBIENT, whiteBuffer);
		gl.glLightfv(GL10.GL_LIGHT2, GL10.GL_SPECULAR, whiteBuffer);
		gl.glLightfv(GL10.GL_LIGHT2, GL10.GL_DIFFUSE, whiteBuffer);

		gl.glEnable(GL10.GL_DEPTH_TEST);
		gl.glDepthMask(true);
		gl.glEnable(GL10.GL_NORMALIZE);
		gl.glTranslatef(0.0f, 0.0f, model.getBBoxSize().point[2]);
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
		return position.point[0];
	}

	public float getYpos() {
		return position.point[1];
	}

	public Model getModel() {
		return model;
	}
}
