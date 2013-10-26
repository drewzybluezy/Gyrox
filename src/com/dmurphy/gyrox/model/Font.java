package com.dmurphy.gyrox.model;

import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

import com.dmurphy.gyrox.util.GraphicUtils;

import android.content.Context;

public class Font {

	// private int _nTextures;
	public int textureWidth;
	public int width;
	public int lower;
	public int upper;

	private GL10 gl;

	private GLTexture t1;
	private GLTexture t2;

	public Font(GL10 gl1, Context context, int tex1, int tex2) {
		gl = gl1;
		t1 = new GLTexture(gl, context, tex1, GL10.GL_CLAMP_TO_EDGE,
				GL10.GL_CLAMP_TO_EDGE, false);
		t2 = new GLTexture(gl, context, tex2, GL10.GL_CLAMP_TO_EDGE,
				GL10.GL_CLAMP_TO_EDGE, false);
	}

	public void drawText(int x, int y, int size, String text) {
		gl.glEnable(GL10.GL_BLEND);
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		gl.glEnable(GL10.GL_TEXTURE_2D);

		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

		gl.glPushMatrix();
		gl.glTranslatef(x, y, 0);
		gl.glScalef(size, size, size);

		renderString(text);

		gl.glPopMatrix();
		gl.glDisable(GL10.GL_TEXTURE_2D);
		gl.glDisable(GL10.GL_BLEND);
	}

	private void renderString(String str) {
		int w = textureWidth / width;
		float cw = (float) width / (float) textureWidth;
		float cx, cy;
		int i, index;
		int tex;
		int bound = -1;

		float vertex[] = new float[6 * 2];
		float texture[] = new float[6 * 2];
		FloatBuffer vertexBuffer;
		FloatBuffer textureBuffer;

		for (i = 0; i < str.length(); i++) {
			index = str.charAt(i) - lower + 1;

			if (index >= upper)
				return; // index out of bounds

			tex = index / (w * w);

			// Bind texture
			if (tex != bound) {
				if (tex == 0)
					gl.glBindTexture(GL10.GL_TEXTURE_2D, t1.getTextureID());
				else
					gl.glBindTexture(GL10.GL_TEXTURE_2D, t2.getTextureID());

				gl.glTexEnvf(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE,
						GL10.GL_MODULATE);
				bound = tex;
			}

			// find texture coordinates
			index = index % (w * w);
			cx = (float) (index % w) / (float) w;
			cy = (float) (index / w) / (float) w;

			// draw character
			texture[0] = cx;
			texture[1] = 1.0f - cy - cw;
			vertex[0] = (float) i;
			vertex[1] = 0.0f;

			texture[2] = cx + cw;
			texture[3] = 1.0f - cy - cw;
			vertex[2] = i + 1.0f;
			vertex[3] = 0.0f;

			texture[4] = cx + cw;
			texture[5] = 1.0f - cy;
			vertex[4] = i + 1.0f;
			vertex[5] = 1.0f;

			texture[6] = cx + cw;
			texture[7] = 1.0f - cy;
			vertex[6] = i + 1.0f;
			vertex[7] = 1.0f;

			texture[8] = cx;
			texture[9] = 1.0f - cy;
			vertex[8] = i;
			vertex[9] = 1.0f;

			texture[10] = cx;
			texture[11] = 1.0f - cy - cw;
			vertex[10] = i;
			vertex[11] = 0.0f;

			vertexBuffer = GraphicUtils.convToFloatBuffer(vertex);
			textureBuffer = GraphicUtils.convToFloatBuffer(texture);

			gl.glVertexPointer(2, GL10.GL_FLOAT, 0, vertexBuffer);
			gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer);
			gl.glDrawArrays(GL10.GL_TRIANGLES, 0, 6);

		}
	}
}
