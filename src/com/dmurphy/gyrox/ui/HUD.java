package com.dmurphy.gyrox.ui;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;

import com.dmurphy.gyrox.R;
import com.dmurphy.gyrox.game.GameState;
import com.dmurphy.gyrox.model.Font;
import com.dmurphy.gyrox.model.Video;

public class HUD {
	
	private Font font;
	private GL10 gl;
	
	// fps members
	private final int FPS_HSIZE = 20;
	private int fps_h[] = new int[FPS_HSIZE];
	private int pos = -FPS_HSIZE;
	private int fps_avg = 0;
	
	// console members
	private final int CONSOLE_DEPTH = 100;
	private String consoleBuff[] = new String[CONSOLE_DEPTH];
	private int position;
	private int offset;
	
	// win lose
	private boolean dispInst = true;
	
	public HUD(GL10 gl1, Context ctx) {
		gl = gl1;
	    // Load font
	    font = new Font(gl,ctx,R.drawable.space_1, R.drawable.space_2);
	    // Hard code these values for now allow loadable fonts later...
	    font.textureWidth = 256;
	    font.width = 32;
	    font.lower = 32;
	    font.upper = 126;
	    
	    resetConsole();
	    
	}
	
	public void draw(Video Visual, long dt, int plyrScore, float timeLeft, int notesLeft) {
		// Draw fps
		gl.glDisable(GL10.GL_DEPTH_TEST);
		Visual.rasOnly(gl);
		
		if(GameState.mPrefs.drawFPS())
			drawFPS(Visual,dt);
		
		drawConsole(Visual);
		if (GameState.gameOver) drawFinalScore(plyrScore, Visual);
		if (GameState.gameOver && (GameState.endDelay <= 0)) drawNewGame(Visual);
		drawScore(plyrScore);
		drawInstructions(Visual);
		drawTimeLeft(timeLeft, Visual);
		drawPickupsLeft(notesLeft, Visual);
		drawMultiplier(GameState.getMultiplier(), Visual);
	}
	
	public void resetConsole() {
		int i;
		
		position = 0;
	    offset = 0;
	    
	    for(i = 0; i < CONSOLE_DEPTH; i++) {
	    	consoleBuff[i] = null;
	    }
	}
	
	public void displayInstr(Boolean value) {
		dispInst = value;
	}
	
	
	
	private void drawTimeLeft(float timeLeft, Video v) {
		StringBuilder sb = new StringBuilder();
		
		sb.append("Time Left: ");
		sb.append(timeLeft);
		
		gl.glColor4f(1.0f, 1.0f, 0.2f, 1.0f);
		font.drawText(5, v.vH - 30, 24, sb.toString());
	}
	
	private void drawMultiplier(int mult, Video v) {
		StringBuilder sb = new StringBuilder();
		
		sb.append("x");
		sb.append(mult);
		
		gl.glColor4f(1.0f, 0f, 0f, 1.0f);
		font.drawText(5, v.vH - 90, 24, sb.toString());
	}
	
	private void drawPickupsLeft(int pickupsLeft, Video v) {
		StringBuilder sb = new StringBuilder();
		
		sb.append("Notes Remaining: ");
		sb.append(pickupsLeft);
		
		gl.glColor4f(1.0f, 1.0f, 0.2f, 1.0f);
		font.drawText(5, v.vH - 60, 24, sb.toString());
	}
	
	private void drawScore(int score) {
		StringBuilder sb = new StringBuilder();
		
		sb.append("Score: ");
		sb.append(score);
		
		gl.glColor4f(1.0f, 1.0f, 0.2f, 1.0f);
		font.drawText(5, 5, 32, sb.toString());
	}
	
	private void drawNewGame(Video Visual) {
		StringBuilder sb = new StringBuilder();
		
		sb.append("Press any key to continue");
			
		font.drawText(5, 
					(Visual.vH / 2) - 35, 
					(Visual.vW / (6 / 4 * sb.length())), 
					sb.toString());
		
	}
	
	private void drawFinalScore(int score, Video Visual) {
		StringBuilder sb = new StringBuilder();
		
		sb.append("FINAL SCORE: ");
		sb.append(score);
			
		font.drawText(5, 
					Visual.vH / 2, 
					(Visual.vW / (6 / 4 * sb.length())), 
					sb.toString());
		
	}
	
	private void drawInstructions(Video Visual) {
		String str1 = null;
		String str2 = null;
		
		if(dispInst) {
			str1 = "Tap screen to Start";
			str2 = "Press device menu key for settings";
			
			gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
			
			font.drawText(
					5,
					Visual.vH / 4,
					(Visual.vW / (6 / 4 * str1.length())),
					str1);
			
			font.drawText(
					5,
					Visual.vH / 8,
					(Visual.vW / (6/4 * str2.length())),
					str2);
					
		}
	}
	
	private void drawConsole(Video Visual) {
		int lines = 3; // lines of console to display
		int i;
		int index;
		
		for(i = 0; i < lines; i++) {
			index = (position + i - lines - offset + CONSOLE_DEPTH) % CONSOLE_DEPTH;
			if(consoleBuff[index] != null) {
				int size = 30;
				int length = consoleBuff[index].length();
				
				while(length * size > Visual.vW / 2 - 25)
					size--;
				
				gl.glColor4f(1.0f, 0.4f, 0.2f, 1.0f);
				font.drawText(25, Visual.getHeight() - 20 * (i + 1), size, consoleBuff[index]);
			}
		}
	}
	
	private void drawFPS(Video Visual, long dt) {
		int diff;
		StringBuilder sb = new StringBuilder();
		
		diff = (dt > 0) ? (int)dt : 1;
		
		if(pos < 0) {
			fps_avg = 1000 / diff;
			fps_h[pos + FPS_HSIZE] = 1000 / diff;
			pos++;
		}
		else {
			fps_h[pos] = 1000 / diff;
			pos = (pos + 1) % FPS_HSIZE;
			
			if(pos % 10 == 0) {
				int i;
				int sum = 0;
				int min = 1000;
				
				for(i=0;i<FPS_HSIZE;i++) {
					sum += fps_h[i];
					if(fps_h[i] < min)
						min = fps_h[i];
				}
				fps_avg = sum / FPS_HSIZE;
			}
		}
		
		sb.append("FPS: ");
		sb.append(fps_avg);
		
		gl.glColor4f(1.0f, 0.4f, 0.2f, 1.0f);
		font.drawText(Visual.getWidth() - 280, Visual.getHeight() - 30, 30, sb.toString());
	}
}
