

package com.dmurphy.gyrox.entity;

import java.util.ArrayList;
import java.util.Random;

import javax.microedition.khronos.opengles.GL10;

import com.dmurphy.gyrox.game.GyroxMain;
import com.dmurphy.gyrox.model.GLTexture;
import com.dmurphy.gyrox.model.Model;
import com.dmurphy.gyrox.model.Segment;
import com.dmurphy.gyrox.model.Vec;
import com.dmurphy.gyrox.sound.SoundManager;
import com.dmurphy.gyrox.ui.Camera;
import com.dmurphy.gyrox.ui.HUD;
import com.dmurphy.gyrox.world.Lighting;

public class Player {


	private Model Cycle;
	private int Player_num;
	private int Direction;
	private int LastDirection;
	private Explosion Explode;
	
	private int Score;
	
	GLTexture _ExplodeTex;
	
	private Segment[] Trails = new Segment[1000] ;
	
	private HUD hud; // Allow messages to be added to console
	
	
	private int trailOffset;
	private float trailHeight;
	
	private float Speed;
	public long TurnTime;
	public final float DIRS_X[] = {0.0f, -1.0f, 0.0f, 1.0f};
	public final float DIRS_Y[] = {-1.0f, 0.0f, 1.0f, 0.0f};
	private final float SPEED_OZ_FREQ = 1200.0f;
	private final float SPEED_OZ_FACTOR = 0.09f;
	
	private final float dirangles[] = { 0.0f, -90.0f, -180.0f, 90.0f, 180.0f, -270.0f };
	public final int TURN_LEFT = 3;
	public final int TURN_RIGHT = 1;
	public final int TURN_LENGTH = 200;
	public final float TRAIL_HEIGHT = 3.5f;
	private final float EXP_RADIUS_MAX = 30.0f;
	private final float EXP_RADIUS_DELTA = 0.01f;
	
	private float exp_radius;
	
	private final float START_POS[][] = {
			{ 0.5f, 0.25f},
			{0.75f, 0.5f},
			{0.5f, 0.4f},
			{0.25f, 0.5f},
			{0.25f, 0.25f},
			{0.65f, 0.35f}
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
	
	private final float ColourAlpha[][] = {
			{0.0f, 0.1f, 0.900f, 0.600f},      // Blue
			 {1.000f, 0.850f, 0.140f, 0.600f}, // Yellow
			 {0.750f, 0.020f, 0.020f, 0.600f}, // Red
			 {0.700f, 0.700f, 0.700f, 0.600f}, // Grey
			 {0.120f, 0.700f, 0.000f, 0.600f}, // Green
			 {0.720f, 0.000f, 0.300f, 0.600f}  // Purple
	};

	private int mPlayerColorIndex;
	private final int LOD_DIST[][] = {
			{ 1000, 1000, 1000 },
			{100, 200, 400},
			{30,100,200},
			{10,30,150}
	};
	
	public Player(float gridSize, Model mesh, HUD hud) {
		
		Random rand = new Random();
		Direction = rand.nextInt(3); // accepts values 0..3;
		LastDirection = Direction;
		
		Trails[0] = new Segment();
		trailOffset = 0;
		Trails[trailOffset].vStart.v[0] = START_POS[0][0] * gridSize;
		Trails[trailOffset].vStart.v[1] = START_POS[0][1] * gridSize;
		Trails[trailOffset].vDirection.v[0] = 0.0f;
		Trails[trailOffset].vDirection.v[1] = 0.0f;
		
		trailHeight = TRAIL_HEIGHT;
	
		this.hud = hud;
		
		Speed = 10.0f;
		exp_radius = 0.0f;
		
		Cycle = mesh;
		Score = 0;

		mPlayerColorIndex = GyroxMain.mPrefs.playerColorIndex();
		
	}
	

	public void doTurn(int direction, long current_time) {
		float x = getXpos();
		float y = getYpos();
		
		trailOffset++;
		Trails[trailOffset] = new Segment();
		Trails[trailOffset].vStart.v[0] = x;
		Trails[trailOffset].vStart.v[1] = y;
		Trails[trailOffset].vDirection.v[0] = 0.0f;
		Trails[trailOffset].vDirection.v[1] = 0.0f;
		
		LastDirection = Direction;
		Direction = (Direction + direction) % 4;
		TurnTime = current_time;
	}
	
	
	public void doMovement(long dt, long current_time, Segment walls[]) {
		float fs;
		float t;
		
		if(Speed > 0.0f) {
			fs = (float) (1.0f - SPEED_OZ_FACTOR + SPEED_OZ_FACTOR *
				Math.cos(0.0f * (float)Math.PI / 4.0f + 
						   (current_time % SPEED_OZ_FREQ) *
						   2.0f * Math.PI / SPEED_OZ_FREQ));
			
			if (GyroxMain.getBoostRemaining() > 0)
				t = dt / 100.0f * (Speed * 3f) * fs;
			else
				t = dt / 100.0f * Speed * fs;
			
			Trails[trailOffset].vDirection.v[0] += t * DIRS_X[Direction];
			Trails[trailOffset].vDirection.v[1] += t * DIRS_Y[Direction];
			
			doCrashTestWalls(walls);
			doCrashTestPickup(GyroxMain.notes, Cycle);
			collideBoostTest(GyroxMain.boost, Cycle);
		}
		else {
			if(trailHeight > 0.0f) {
				trailHeight -= (dt * TRAIL_HEIGHT) / 1000.0f;
			}
			if(exp_radius < EXP_RADIUS_MAX) {
				exp_radius += (dt * EXP_RADIUS_DELTA);
			}
		}
	}
	
	public void drawCycle(GL10 gl, long curr_time, long time_dt, Lighting Lights, GLTexture ExplodeTex) {
		gl.glPushMatrix();
		gl.glTranslatef(getXpos(), getYpos(), 0.0f);

		doCycleRotation(gl,curr_time);
		gl.glEnable(GL10.GL_LIGHTING);
		
		gl.glEnable(GL10.GL_DEPTH_TEST);
		gl.glDepthMask(true);
		if(exp_radius == 0.0f) {
			gl.glEnable(GL10.GL_NORMALIZE);
			gl.glTranslatef(0.0f, 0.0f, Cycle.getBBoxSize().v[2] / 2.0f);
			gl.glEnable(GL10.GL_CULL_FACE);
			Cycle.draw(gl,ColourSpecular[Player_num],ColourDiffuse[mPlayerColorIndex]);
			gl.glDisable(GL10.GL_CULL_FACE);
		}
		
		else if(exp_radius < EXP_RADIUS_MAX) {
			if(getExplode() != null) {
				if(getExplode().runExplode()) {
					gl.glEnable(GL10.GL_BLEND);
	
					Explode.Draw(gl, time_dt, ExplodeTex);
				
					gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
					gl.glTranslatef(0.0f, 0.0f, Cycle.getBBoxSize().v[2] / 2.0f);
				}
			}
		}
		
		gl.glDisable(GL10.GL_BLEND);
		gl.glDisable(GL10.GL_LIGHTING);
		gl.glPopMatrix();

	}
	
	public void doCrashTestWalls(Segment Walls[]) {
		Segment Current = Trails[trailOffset];
		Vec V;
		
		for(int j=0; j < 4; j++) {
			V = Current.intersect(Walls[j]);
			
			if(V != null) {
				if(Current.t1 >= 0.0f && Current.t1 < 1.0f && Current.t2 >= 0.0f && Current.t2 < 1.0f) {
					
					Current.vDirection.v[0] = V.v[0] - Current.vStart.v[0];
					Current.vDirection.v[1] = V.v[1] - Current.vStart.v[1];
					Speed = 0.0f;
					Explode = new Explosion(0.0f);
					
					GyroxMain.endGame();
					
					if(GyroxMain.mPrefs.playSFX())
						SoundManager.playSound(GyroxMain.CRASH_SOUND, 1.0f);
					break;
				}
			}
		}
		
	}

	private void doCycleRotation(GL10 gl, long CurrentTime) {
		  long time = CurrentTime - TurnTime;
		  float dirAngle;
		  float axis = 1.0f;
		  float Angle;
		  
		  dirAngle = getDirAngle(time);
		  
		  gl.glRotatef(dirAngle, 0.0f, 0.0f, 1.0f);
		
		  if((time < TURN_LENGTH) && (LastDirection != Direction)) {
			  if( (Direction < LastDirection) && (LastDirection != 3)) {
				  axis = -1.0f;
			  }
			  else if( ((LastDirection == 3) && (Direction == 2)) ||
					          ((LastDirection == 0) && (Direction == 3))) {
				  axis = -1.0f;
			  }
			  Angle = (float)Math.sin((Math.PI * time / TURN_LENGTH)) * 25.0f;
			  gl.glRotatef(Angle, 0.0f, (axis * -1.0f), 0.0f);
		  }
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
		
		v1 = cam.getTarget().sub(cam.getCam());
		v1.normalize();
		
		v2 = cam.getCam().sub(tmp);
		
		d = v2.length();
		
		for(i=0;i<LC_LOD && d >= LOD_DIST[lod_level][i]; i++);
		
		if(i >= LC_LOD) {
			retValue = false;
		}
		else {
			v2 = tmp.sub(cam.getCam());
			v2.normalize();
			
			s = v1.dot(v2);
			d = (float)Math.cos((fov/2) * 2 * Math.PI / 360.0f);
			
			if(s < d - (Cycle.getBBoxRadius() * 2.0f)) {
				retValue = false;
			}
			else {
				retValue = true;
			}
			
		}
		
		return retValue;
	}
	
	private float getDirAngle(long time){
		int last_dir;
		float dir_angle;
		
		if(time < TURN_LENGTH) {
			last_dir = LastDirection;
			if(Direction == 3 && last_dir ==2) {
				last_dir = 4;
			}
			if(Direction == 2 && last_dir == 3) {
				last_dir = 5;
			}
			dir_angle = ((TURN_LENGTH - time) * dirangles[last_dir] + 
					time * dirangles[Direction]) / TURN_LENGTH;
		}
		else {
			dir_angle = dirangles[Direction];
		}
		return dir_angle;
	}
	
	public Explosion getExplode() {
		return Explode;
	}
	
	public void setExplodeTex(GLTexture tex) {
		_ExplodeTex = tex;
	}
	
	public float getXpos() {
		return Trails[trailOffset].vStart.v[0] + Trails[trailOffset].vDirection.v[0];
	}
	
	public float getYpos() {
		return Trails[trailOffset].vStart.v[1] + Trails[trailOffset].vDirection.v[1];
	}

	public int getDirection() {
		return Direction;
	}
	
	public int getLastDirection() {
		return LastDirection;
	}

	public float getSpeed() {
		return Speed;
	}
	
	public void setSpeed(float sp) {
		Speed = sp;
	}
	
	public float[] getColorAlpha() {
		return ColourAlpha[mPlayerColorIndex];
	}
	
	public float[] getColorDiffuse() {
		return ColourDiffuse[mPlayerColorIndex];
	}
	
	public void addScore(int val) {
			Score += val;
	}
	
	public int getScore() {
		return Score;
	}
	
	public void doCrashTestPickup(ArrayList<Note> notes, Model mesh) {
		for(int j = 0; j < notes.size(); j++) {
			float dist = (float)Math.sqrt(Math.pow(this.getXpos() - notes.get(j).getXpos(), 2) +
					Math.pow(this.getYpos() - notes.get(j).getYpos(), 2));
			
			if (dist <= (mesh.getBBoxRadius() + (notes.get(j).getModel().getBBoxRadius()))) {
				GyroxMain.removePickup(j);
				this.addScore(10*GyroxMain.getMultiplier());
				GyroxMain.playPickup();
				GyroxMain.incrementTime(GyroxMain.PICKUP_TIME_INCREMENT*GyroxMain.DIMINISHING_RETURNS);
				GyroxMain.pickedUp();
			}
		}
	}
	
	public void collideBoostTest(ArrayList<SpeedBoost> boost, Model mesh) {
		for(int j = 0; j < boost.size(); j++) {
			float dist = (float)Math.sqrt(Math.pow(this.getXpos() - boost.get(j).getXpos(), 2) +
					Math.pow(this.getYpos() - boost.get(j).getYpos(), 2));
				
			if (dist <= (mesh.getBBoxRadius() + (boost.get(j).getModel().getBBoxRadius()* 5f))) {
				GyroxMain.boost();
				GyroxMain.playBoost();
			}
		}
	}
}
