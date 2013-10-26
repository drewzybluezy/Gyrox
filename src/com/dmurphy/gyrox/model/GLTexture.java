

package com.dmurphy.gyrox.model;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.opengl.GLUtils;

public class GLTexture {

	int textureID[];
	boolean boGenMipMap = true;
	
	int wrapS;
	int wrapT;
	
	public int debugType;
	
	public GLTexture(GL10 gl, Context context, int resource) {
		wrapS = GL10.GL_REPEAT;
		wrapT = GL10.GL_REPEAT;
		textureID = new int[1];
		loadTexture(gl,context,resource);
	}

	public GLTexture(GL10 gl, Context context, int resource, int wrapS, int wrapT) {
		this.wrapS = wrapS;
		this.wrapT = wrapT;
		textureID = new int[1];
		loadTexture(gl,context,resource);
	}
	
	public GLTexture(GL10 gl, Context context, int resource, int wrapS, int wrapT, boolean mipMap) {
		this.wrapS = wrapS;
		this.wrapT = wrapT;
		boGenMipMap = mipMap;
		textureID = new int[1];
		loadTexture(gl,context,resource);
	}

	// Will load a texture out of a drawable resource file, and return an OpenGL texture ID:
	private void loadTexture(GL10 gl, Context context, int resource) {
	    
	    // In which ID will we be storing this texture?
		gl.glGenTextures(1, textureID, 0);
	    
	    // We need to flip the textures vertically:
	    Matrix flip = new Matrix();
	    flip.postScale(1f, -1f);
	    
	    // This will tell the BitmapFactory to not scale based on the device's pixel density:
	    // (Thanks to Matthew Marshall for this bit)
	    BitmapFactory.Options opts = new BitmapFactory.Options();
	    opts.inScaled = false;
	    
	    // Load up, and flip the texture:
	    Bitmap temp = BitmapFactory.decodeResource(context.getResources(), resource, opts);
	    Bitmap bmp = Bitmap.createBitmap(temp, 0, 0, temp.getWidth(), temp.getHeight(), flip, true);
	    temp.recycle();
	    
	    gl.glBindTexture(GL10.GL_TEXTURE_2D, textureID[0]);
	    
	    // Set all of our texture parameters:
	    if(boGenMipMap) {
	    	gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR_MIPMAP_NEAREST);
	    	gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR_MIPMAP_NEAREST);
	    }
	    else {
	    	gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
	    	gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
	    }
	    gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, wrapS);
	    gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, wrapT);

	    debugType = GLUtils.getInternalFormat(bmp);
	    
	    // Generate, and load up all of the mipmaps:
	    if(boGenMipMap) {
		    for(int level=0, height = bmp.getHeight(), width = bmp.getWidth(); true; level++) {
		        // Push the bitmap onto the GPU:
		        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, level, bmp, 0);
		        
		        // We need to stop when the texture is 1x1:
		        if(height==1 && width==1) break;
		        
		        // Resize, and let's go again:
		        width >>= 1; height >>= 1;
		        if(width<1)  width = 1;
		        if(height<1) height = 1;
		        
		        Bitmap bmp2 = Bitmap.createScaledBitmap(bmp, width, height, true);
		        bmp.recycle();
		        bmp = bmp2;
		    }
	    }
	    else {
	    	GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bmp, 0);
	    }
	    
	    bmp.recycle();
	    
	}

	public int getTextureID() {
		return textureID[0];
	}
	
}
