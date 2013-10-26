package com.dmurphy.gyrox.entity;

import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

import com.dmurphy.gyrox.model.GLTexture;
import com.dmurphy.gyrox.model.Vec;
import com.dmurphy.gyrox.util.GraphicUtils;

public class Explosion {

	private float radius;

	private final float IMPACT_RADIUS_DELTA = 0.025f;
	private final float IMPACT_MAX_RADIUS = 25.0f;

	// Shockwave behaviour constants
	private final float SHOCKWAVE_MIN_RADIUS = 0.0f;
	private final float SHOCKWAVE_MAX_RADIUS = 45.0f;
	private final float SHOCKWAVE_WIDTH = 0.2f;
	private final float SHOCKWAVE_SPACING = 6.0f;
	private final float SHOCKWAVE_SPEED = 1.2f; // relative to impact radius
												// delta
	private final int SHOCKWAVE_SEGMENTS = 25;
	private final int NUM_SHOCKWAVES = 3;

	// Glow contants
	private final float GLOW_START_OPACITY = 1.2f;
	private final float GLOW_INTENSITY = 1.0f;

	// Spire contants
	private final float SPIRE_WIDTH = 0.40f;
	private final int NUM_SPIRES = 21;

	private GLTexture explosionTexture;

	public Explosion(float radius) {
		this.radius = radius;
	}

	public float getRadius() {
		return radius;
	}

	public boolean runExplode() {
		boolean retVal = true;
		if (radius > IMPACT_MAX_RADIUS) {
			retVal = false;
		}
		return retVal;
	}

	public void Draw(GL10 gl, long timeDelta, GLTexture tex) {
		gl.glDisable(GL10.GL_LIGHTING);
		gl.glPushMatrix();
		gl.glRotatef(90, 90, 0, 1);
		gl.glTranslatef(0.0f, -0.5f, -0.5f);
		gl.glColor4f(0.68f, 0.0f, 0.0f, 1.0f);

		explosionTexture = tex;

		drawShockwaves(gl);

		if (radius < IMPACT_MAX_RADIUS) {
			drawImpactGlow(gl);
			drawSpires(gl);
		}

		radius += (timeDelta * IMPACT_RADIUS_DELTA);

		gl.glPopMatrix();
		gl.glEnable(GL10.GL_LIGHTING);

	}

	private void drawSpires(GL10 gl) {
		int i;

		Vec zunit = new Vec(0.0f, 0.0f, 1.0f);
		Vec right, left;

		Vec vectors[] = { new Vec(1.00f, 0.20f, 0.00f),
				new Vec(0.80f, 0.25f, 0.00f), new Vec(0.90f, 0.50f, 0.00f),
				new Vec(0.70f, 0.50f, 0.00f), new Vec(0.52f, 0.45f, 0.00f),
				new Vec(0.65f, 0.75f, 0.00f), new Vec(0.42f, 0.68f, 0.00f),
				new Vec(0.40f, 1.02f, 0.00f), new Vec(0.20f, 0.90f, 0.00f),
				new Vec(0.08f, 0.65f, 0.00f), new Vec(0.00f, 1.00f, 0.00f),
				new Vec(-0.08f, 0.65f, 0.00f), new Vec(-0.20f, 0.90f, 0.00f),
				new Vec(-0.40f, 1.02f, 0.00f), new Vec(-0.42f, 0.68f, 0.00f),
				new Vec(-0.65f, 0.75f, 0.00f), new Vec(-0.52f, 0.45f, 0.00f),
				new Vec(-0.70f, 0.50f, 0.00f), new Vec(-0.90f, 0.50f, 0.00f),
				new Vec(-0.80f, 0.30f, 0.00f), new Vec(-1.00f, 0.20f, 0.00f) };

		float triangleList[] = new float[3 * 3];
		FloatBuffer spireBuffer;

		gl.glColor4f(1.0f, 1.0f, 1.0f, 0.0f);
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glBlendFunc(GL10.GL_ONE, GL10.GL_ONE);

		for (i = 0; i < NUM_SPIRES; i++) {
			right = vectors[i].cross(zunit);
			right.normalize();
			right.mul(SPIRE_WIDTH);

			left = zunit.cross(vectors[i]);
			left.normalize();
			left.mul(SPIRE_WIDTH);

			triangleList[0] = right.v[0];
			triangleList[1] = right.v[1];
			triangleList[2] = right.v[2];
			triangleList[3] = (radius * vectors[i].v[0]);
			triangleList[4] = (radius * vectors[i].v[1]);
			triangleList[5] = 0.0f;
			triangleList[6] = left.v[0];
			triangleList[7] = left.v[1];
			triangleList[8] = left.v[2];

			spireBuffer = GraphicUtils.convToFloatBuffer(triangleList);
			gl.glVertexPointer(3, GL10.GL_FLOAT, 0, spireBuffer);
			gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 3);
		}

		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);

	}

	private void drawImpactGlow(GL10 gl) {
		float opacity;
		float impactVertex[] = { -1.0f, -1.0f, 0.0f, 1.0f, -1.0f, 0.0f, 1.0f,
				1.0f, 0.0f, 1.0f, 1.0f, 0.0f, -1.0f, 1.0f, 0.0f, -1.0f, -1.0f,
				0.0f };
		float textureVertex[] = { 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 1.0f,
				1.0f, 0.0f, 1.0f, 0.0f, 0.0f };
		FloatBuffer impactBuffer;
		FloatBuffer textureBuffer;

		opacity = GLOW_START_OPACITY - (radius / IMPACT_MAX_RADIUS);

		gl.glPushMatrix();
		gl.glScalef(radius, radius, 1.0f);
		gl.glBindTexture(GL10.GL_TEXTURE_2D, explosionTexture.getTextureID());
		gl.glEnable(GL10.GL_TEXTURE_2D);

		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

		gl.glColor4f(GLOW_INTENSITY, GLOW_INTENSITY, GLOW_INTENSITY, opacity);
		gl.glDepthMask(false);

		impactBuffer = GraphicUtils.convToFloatBuffer(impactVertex);
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, impactBuffer);
		textureBuffer = GraphicUtils.convToFloatBuffer(textureVertex);
		gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer);
		gl.glDrawArrays(GL10.GL_TRIANGLES, 0, 6);

		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		gl.glDepthMask(true);
		gl.glDisable(GL10.GL_TEXTURE_2D);
		gl.glPopMatrix();
	}

	private void drawShockwaves(GL10 gl) {
		int waves;
		float currentRadius = (radius * SHOCKWAVE_SPEED);

		gl.glColor4f(1.0f, 0.0f, 0.0f, 1.0f);

		for (waves = 0; waves < NUM_SHOCKWAVES; waves++) {
			if (currentRadius > SHOCKWAVE_MIN_RADIUS
					&& currentRadius < SHOCKWAVE_MAX_RADIUS) {
				drawWave(gl, currentRadius);
			}
			currentRadius -= SHOCKWAVE_SPACING;
		}
	}

	private void drawWave(GL10 gl, float adj_radius) {
		int i, j, vertex;
		double angle;
		double deltaRadius = SHOCKWAVE_WIDTH / SHOCKWAVE_SEGMENTS;
		double deltaAngle = (180.0 / SHOCKWAVE_SEGMENTS) * (Math.PI / 180);
		double startAngle = (270.0 * (Math.PI / 180));
		int numIndices = (2 * (SHOCKWAVE_SEGMENTS + 1));

		float waveVertex[] = new float[(3 * (2 * (SHOCKWAVE_SEGMENTS + 1)))];
		FloatBuffer waveBuffer;

		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);

		for (i = 0; i < SHOCKWAVE_SEGMENTS; i++) {
			angle = startAngle;
			vertex = 0;
			for (j = 0; j <= SHOCKWAVE_SEGMENTS; j++) {
				waveVertex[vertex] = (float) ((adj_radius + deltaRadius) * Math
						.sin(angle));
				vertex++;
				waveVertex[vertex] = (float) ((adj_radius + deltaRadius) * Math
						.cos(angle));
				vertex++;
				waveVertex[vertex] = 0.0f;
				vertex++;

				waveVertex[vertex] = (float) (adj_radius * Math.sin(angle));
				vertex++;
				waveVertex[vertex] = (float) (adj_radius * Math.cos(angle));
				vertex++;
				waveVertex[vertex] = 0.0f;
				vertex++;

				angle += deltaAngle;
			}

			waveBuffer = GraphicUtils.convToFloatBuffer(waveVertex);
			gl.glVertexPointer(3, GL10.GL_FLOAT, 0, waveBuffer);
			gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, numIndices);
			adj_radius += deltaRadius;
		}

		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
	}
}
