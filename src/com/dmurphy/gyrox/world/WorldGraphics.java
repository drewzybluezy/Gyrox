package com.dmurphy.gyrox.world;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;

import com.dmurphy.gyrox.R;
import com.dmurphy.gyrox.model.GLTexture;
import com.dmurphy.gyrox.util.GraphicUtils;

public class WorldGraphics {

	float gridSize;
	ByteBuffer indexBuffer;
	FloatBuffer textureBuffer;
	FloatBuffer floorTexBuffer;
	FloatBuffer skyboxVertexBuffers[] = new FloatBuffer[6];
	int nIndices;

	// Textures
	GLTexture sbTopTexture;
	GLTexture sbBottomTexture;
	GLTexture sbLeftTexture;
	GLTexture sbRightTexture;
	GLTexture sbFrontTexture;
	GLTexture sbBackTexture;

	GLTexture floorTexture;

	public WorldGraphics(GL10 gl, Context context, float gridSize) {
		// Save Grid Size
		this.gridSize = gridSize;

		initSkyBox();
		loadTextures(gl, context);

		// Setup standard square index and tex buffers
		// Define indices and tex coords
		float t = gridSize / 240.0f;
		byte indices[] = { 0, 1, 3, 0, 3, 2 };
		float texCoords[] = { 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f };

		float l = gridSize / 4;
		t = l / 12;
		float florTexCoords[] = { 0.0f, 0.0f, t, 0.0f, 0.0f, t, t, t };

		floorTexBuffer = GraphicUtils.convToFloatBuffer(florTexCoords);
		textureBuffer = GraphicUtils.convToFloatBuffer(texCoords);
		indexBuffer = GraphicUtils.convToByteBuffer(indices);
		nIndices = indices.length;

	}

	private void loadTextures(GL10 gl, Context context) {
		sbTopTexture = new GLTexture(gl, context, R.drawable.sb_top);
		sbBottomTexture = new GLTexture(gl, context, R.drawable.sb_bottom);
		sbLeftTexture = new GLTexture(gl, context, R.drawable.sb_left);
		sbRightTexture = new GLTexture(gl, context, R.drawable.sb_right);
		sbFrontTexture = new GLTexture(gl, context, R.drawable.sb_front);
		sbBackTexture = new GLTexture(gl, context, R.drawable.sb_back);
		floorTexture = new GLTexture(gl, context, R.drawable.floor);
	}

	private void initSkyBox() {
		float d = (float) gridSize;

		float sides[][] = { { d, -d, d, d, d, d, d, -d, -d, d, d, -d }, /* front */
		{ -d, -d, d, -d, d, d, d, -d, d, d, d, d }, /* top */
		{ d, d, d, -d, d, d, d, d, -d, -d, d, -d }, /* left */
		{ -d, -d, d, d, -d, d, -d, -d, -d, d, -d, -d }, /* right */
		{ d, -d, -d, d, d, -d, -d, -d, -d, -d, d, -d }, /* bottom */
		{ -d, d, d, -d, -d, d, -d, d, -d, -d, -d, -d } };/* back */

		for (int i = 0; i < 6; i++) {
			skyboxVertexBuffers[i] = GraphicUtils.convToFloatBuffer(sides[i]);
		}
	}

	public void drawFloorTextured(GL10 gl) {
		int i, j, l;

		gl.glEnable(GL10.GL_BLEND);
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		gl.glEnable(GL10.GL_TEXTURE_2D);
		gl.glBindTexture(GL10.GL_TEXTURE_2D, floorTexture.getTextureID());

		gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

		l = (int) (gridSize / 4);

		for (i = 0; i < (int) gridSize; i += l) {
			for (j = 0; j < (int) gridSize; j += l) {
				float rawVertices[] = new float[4 * 3];

				rawVertices[0] = (float) i;
				rawVertices[1] = (float) j;
				rawVertices[2] = 0.0f;

				rawVertices[3] = (float) (i + l);
				rawVertices[4] = (float) j;
				rawVertices[5] = 0.0f;

				rawVertices[6] = (float) i;
				rawVertices[7] = (float) (j + l);
				rawVertices[8] = 0.0f;

				rawVertices[9] = (float) (i + l);
				rawVertices[10] = (float) (j + l);
				rawVertices[11] = 0.0f;

				FloatBuffer VertexBuffer = GraphicUtils
						.convToFloatBuffer(rawVertices);

				gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
				gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

				gl.glVertexPointer(3, GL10.GL_FLOAT, 0, VertexBuffer);
				gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, floorTexBuffer);

				// Draw the vertices as triangles, based on the Index Buffer
				// information
				gl.glDrawElements(GL10.GL_TRIANGLE_STRIP, nIndices,
						GL10.GL_UNSIGNED_BYTE, indexBuffer);

				// Disable the client state before leaving
				gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
				gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
			}
		}
	}

	public void drawSkyBox(GL10 gl) {
		gl.glEnable(GL10.GL_TEXTURE_2D);
		gl.glDepthMask(false);
		gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

		for (int i = 0; i < 6; i++) {
			int textureToBind;

			switch (i) {
			case 0:
				textureToBind = sbFrontTexture.getTextureID();
				break;
			case 1:
				textureToBind = sbTopTexture.getTextureID();
				break;
			case 2:
				textureToBind = sbLeftTexture.getTextureID();
				break;
			case 3:
				textureToBind = sbRightTexture.getTextureID();
				break;
			case 4:
				textureToBind = sbBottomTexture.getTextureID();
				break;
			default:
				textureToBind = sbBackTexture.getTextureID();
			}
			gl.glBindTexture(GL10.GL_TEXTURE_2D, textureToBind);

			gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
			gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
			gl.glFrontFace(GL10.GL_CCW);

			gl.glVertexPointer(3, GL10.GL_FLOAT, 0, skyboxVertexBuffers[i]);
			gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer);

			gl.glDisableClientState(GL10.GL_LIGHTING);
			gl.glDrawElements(GL10.GL_TRIANGLES, nIndices,
					GL10.GL_UNSIGNED_BYTE, indexBuffer);
			gl.glEnableClientState(GL10.GL_LIGHTING);
			gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
			gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

		}
		gl.glDisable(GL10.GL_TEXTURE_2D);
		gl.glDepthMask(true);

	}

}
