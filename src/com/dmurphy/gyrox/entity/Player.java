package com.dmurphy.gyrox.entity;

import java.util.ArrayList;
import java.util.Random;

import javax.microedition.khronos.opengles.GL10;

import com.dmurphy.gyrox.game.GameState;
import com.dmurphy.gyrox.model.GLTexture;
import com.dmurphy.gyrox.model.Model;
import com.dmurphy.gyrox.model.Segment;
import com.dmurphy.gyrox.model.Vec;
import com.dmurphy.gyrox.sound.SoundManager;
import com.dmurphy.gyrox.ui.Camera;
import com.dmurphy.gyrox.ui.HUD;
import com.dmurphy.gyrox.world.Lighting;

public class Player {

	private Model ship;
	private int shipColor;
	private int direction;
	private int prevDirection;
	private Explosion explosion;

	private int score;

	GLTexture explosionTexture;

	private Segment[] trails = new Segment[1000];

	private int trailOffset;
	private float trailHeight;

	private float speed;
	public long turnTime;
	public final float DIRS_X[] = { 0.0f, -1.0f, 0.0f, 1.0f };
	public final float DIRS_Y[] = { -1.0f, 0.0f, 1.0f, 0.0f };
	private final float SPEED_OZ_FREQ = 1200.0f;
	private final float SPEED_OZ_FACTOR = 0.09f;

	private final float dirAngles[] = { 0.0f, -90.0f, -180.0f, 90.0f, 180.0f,
			-270.0f };
	public final int TURN_LEFT = 3;
	public final int TURN_RIGHT = 1;
	public final int TURN_LENGTH = 200;
	public final float TRAIL_HEIGHT = 3.5f;
	private final float EXP_RADIUS_MAX = 30.0f;
	private final float EXP_RADIUS_DELTA = 0.01f;

	private float explosionRadius;

	private final float START_POS[][] = { { 0.5f, 0.25f }, { 0.75f, 0.5f },
			{ 0.5f, 0.4f }, { 0.25f, 0.5f }, { 0.25f, 0.25f }, { 0.65f, 0.35f } };

	private final float diffuse[][] = { { 0.0f, 0.1f, 0.900f, 1.000f }, // Blue
			{ 1.00f, 0.550f, 0.140f, 1.000f }, // Yellow
			{ 0.750f, 0.020f, 0.020f, 1.000f }, // Red
			{ 0.800f, 0.800f, 0.800f, 1.000f }, // Grey
			{ 0.120f, 0.750f, 0.0f, 1.000f }, // Green
			{ 0.750f, 0.0f, 0.35f, 1.000f } // Purple
	};

	private final float specular[][] = { { 0.0f, 0.1f, 0.900f, 1.000f }, // Blue
			{ 0.500f, 0.500f, 0.000f, 1.000f }, // Yellow
			{ 0.750f, 0.020f, 0.020f, 1.000f }, // Red
			{ 1.00f, 1.00f, 1.00f, 1.000f }, // Grey
			{ 0.050f, 0.500f, 0.00f, 1.00f }, // Green
			{ 0.500f, 0.000f, 0.500f, 1.00f }, // Purple
	};

	private final float alpha[][] = { { 0.0f, 0.1f, 0.900f, 0.600f }, // Blue
			{ 1.000f, 0.850f, 0.140f, 0.600f }, // Yellow
			{ 0.750f, 0.020f, 0.020f, 0.600f }, // Red
			{ 0.700f, 0.700f, 0.700f, 0.600f }, // Grey
			{ 0.120f, 0.700f, 0.000f, 0.600f }, // Green
			{ 0.720f, 0.000f, 0.300f, 0.600f } // Purple
	};

	private int mPlayerColorIndex;
	private final int LOD_DIST[][] = { { 1000, 1000, 1000 }, { 100, 200, 400 },
			{ 30, 100, 200 }, { 10, 30, 150 } };

	public Player(float gridSize, Model mesh, HUD hud) {
		Random rand = new Random();
		direction = rand.nextInt(3); // accepts values 0..3;
		prevDirection = direction;

		trails[0] = new Segment();
		trailOffset = 0;
		trails[trailOffset].vStart.v[0] = START_POS[0][0] * gridSize;
		trails[trailOffset].vStart.v[1] = START_POS[0][1] * gridSize;
		trails[trailOffset].vDirection.v[0] = 0.0f;
		trails[trailOffset].vDirection.v[1] = 0.0f;

		trailHeight = TRAIL_HEIGHT;

		speed = 10.0f;
		explosionRadius = 0.0f;

		ship = mesh;
		score = 0;

		mPlayerColorIndex = GameState.mPrefs.playerColorIndex();
	}

	public void doTurn(int direction, long time) {
		float x = getXCoord();
		float y = getYCoord();

		trailOffset++;
		trails[trailOffset] = new Segment();
		trails[trailOffset].vStart.v[0] = x;
		trails[trailOffset].vStart.v[1] = y;
		trails[trailOffset].vDirection.v[0] = 0.0f;
		trails[trailOffset].vDirection.v[1] = 0.0f;

		prevDirection = this.direction;
		this.direction = (this.direction + direction) % 4;
		turnTime = time;
	}

	public void doMovement(long timeDelta, long time, Segment walls[]) {
		float fs;
		float t;

		if (speed > 0.0f) {
			fs = (float) (1.0f - SPEED_OZ_FACTOR + SPEED_OZ_FACTOR
					* Math.cos(0.0f * (float) Math.PI / 4.0f
							+ (time % SPEED_OZ_FREQ) * 2.0f * Math.PI
							/ SPEED_OZ_FREQ));

			if (GameState.getBoostRemaining() > 0)
				t = timeDelta / 100.0f * (speed * 3f) * fs;
			else
				t = timeDelta / 100.0f * speed * fs;

			trails[trailOffset].vDirection.v[0] += t * DIRS_X[direction];
			trails[trailOffset].vDirection.v[1] += t * DIRS_Y[direction];

			doCrashTestWalls(walls);
			doCrashTestPickup(GameState.notes, ship);
			collideBoostTest(GameState.boost, ship);
		} else {
			if (trailHeight > 0.0f) {
				trailHeight -= (timeDelta * TRAIL_HEIGHT) / 1000.0f;
			}
			if (explosionRadius < EXP_RADIUS_MAX) {
				explosionRadius += (timeDelta * EXP_RADIUS_DELTA);
			}
		}
	}

	public void drawCycle(GL10 gl, long time, long timeDelta,
			Lighting lights, GLTexture explosionTexture) {
		gl.glPushMatrix();
		gl.glTranslatef(getXCoord(), getYCoord(), 0.0f);

		doCycleRotation(gl, time);
		gl.glEnable(GL10.GL_LIGHTING);

		gl.glEnable(GL10.GL_DEPTH_TEST);
		gl.glDepthMask(true);
		if (explosionRadius == 0.0f) {
			gl.glEnable(GL10.GL_NORMALIZE);
			gl.glTranslatef(0.0f, 0.0f, ship.getBBoxSize().v[2] / 2.0f);
			gl.glEnable(GL10.GL_CULL_FACE);
			ship.draw(gl, specular[shipColor], diffuse[mPlayerColorIndex]);
			gl.glDisable(GL10.GL_CULL_FACE);
		}

		else if (explosionRadius < EXP_RADIUS_MAX) {
			if (getExplode() != null) {
				if (getExplode().runExplode()) {
					gl.glEnable(GL10.GL_BLEND);

					explosion.Draw(gl, timeDelta, explosionTexture);

					gl.glBlendFunc(GL10.GL_SRC_ALPHA,
							GL10.GL_ONE_MINUS_SRC_ALPHA);
					gl.glTranslatef(0.0f, 0.0f, ship.getBBoxSize().v[2] / 2.0f);
				}
			}
		}

		gl.glDisable(GL10.GL_BLEND);
		gl.glDisable(GL10.GL_LIGHTING);
		gl.glPopMatrix();

	}

	public void doCrashTestWalls(Segment walls[]) {
		Segment currentSegment = trails[trailOffset];
		Vec V;

		for (int j = 0; j < 4; j++) {
			V = currentSegment.intersect(walls[j]);

			if (V != null) {
				if (currentSegment.t1 >= 0.0f && currentSegment.t1 < 1.0f
						&& currentSegment.t2 >= 0.0f && currentSegment.t2 < 1.0f) {

					currentSegment.vDirection.v[0] = V.v[0] - currentSegment.vStart.v[0];
					currentSegment.vDirection.v[1] = V.v[1] - currentSegment.vStart.v[1];
					speed = 0.0f;
					explosion = new Explosion(0.0f);

					GameState.endGame();

					if (GameState.mPrefs.playSFX())
						SoundManager.playSound(GameState.CRASH_SOUND, 1.0f);
					break;
				}
			}
		}

	}

	private void doCycleRotation(GL10 gl, long currentTime) {
		long time = currentTime - turnTime;
		float dirAngle;
		float axis = 1.0f;
		float angle;

		dirAngle = getDirAngle(time);

		gl.glRotatef(dirAngle, 0.0f, 0.0f, 1.0f);

		if ((time < TURN_LENGTH) && (prevDirection != direction)) {
			if ((direction < prevDirection) && (prevDirection != 3)) {
				axis = -1.0f;
			} else if (((prevDirection == 3) && (direction == 2))
					|| ((prevDirection == 0) && (direction == 3))) {
				axis = -1.0f;
			}
			angle = (float) Math.sin((Math.PI * time / TURN_LENGTH)) * 25.0f;
			gl.glRotatef(angle, 0.0f, (axis * -1.0f), 0.0f);
		}
	}

	public boolean isVisible(Camera cam) {
		Vec v1;
		Vec v2;
		Vec tmp = new Vec(getXCoord(), getYCoord(), 0.0f);
		int levelOfDetail = 2;
		float d, s;
		int i;
		int LC_LOD = 3;
		float fov = 120;

		boolean retValue;

		v1 = cam.getTarget().sub(cam.getCam());
		v1.normalize();

		v2 = cam.getCam().sub(tmp);

		d = v2.length();

		for (i = 0; i < LC_LOD && d >= LOD_DIST[levelOfDetail][i]; i++)
			;

		if (i >= LC_LOD) {
			retValue = false;
		} else {
			v2 = tmp.sub(cam.getCam());
			v2.normalize();

			s = v1.dot(v2);
			d = (float) Math.cos((fov / 2) * 2 * Math.PI / 360.0f);

			if (s < d - (ship.getBBoxRadius() * 2.0f)) {
				retValue = false;
			} else {
				retValue = true;
			}

		}

		return retValue;
	}

	private float getDirAngle(long time) {
		int prevDirection;
		float angleDirection;

		if (time < TURN_LENGTH) {
			prevDirection = this.prevDirection;
			if (direction == 3 && prevDirection == 2) {
				prevDirection = 4;
			}
			if (direction == 2 && prevDirection == 3) {
				prevDirection = 5;
			}
			angleDirection = ((TURN_LENGTH - time) * dirAngles[prevDirection] + time
					* dirAngles[direction])
					/ TURN_LENGTH;
		} else {
			angleDirection = dirAngles[direction];
		}
		return angleDirection;
	}

	public Explosion getExplode() {
		return explosion;
	}

	public void setExplodeTex(GLTexture tex) {
		explosionTexture = tex;
	}

	public float getXCoord() {
		return trails[trailOffset].vStart.v[0]
				+ trails[trailOffset].vDirection.v[0];
	}

	public float getYCoord() {
		return trails[trailOffset].vStart.v[1]
				+ trails[trailOffset].vDirection.v[1];
	}

	public int getDirection() {
		return direction;
	}

	public int getLastDirection() {
		return prevDirection;
	}

	public float getSpeed() {
		return speed;
	}

	public void setSpeed(float sp) {
		speed = sp;
	}

	public float[] getColorAlpha() {
		return alpha[mPlayerColorIndex];
	}

	public float[] getColorDiffuse() {
		return diffuse[mPlayerColorIndex];
	}

	public void addScore(int val) {
		score += val;
	}

	public int getScore() {
		return score;
	}

	public void doCrashTestPickup(ArrayList<Note> notes, Model mesh) {
		for (int j = 0; j < notes.size(); j++) {
			float dist = (float) Math.sqrt(Math.pow(this.getXCoord()
					- notes.get(j).getXpos(), 2)
					+ Math.pow(this.getYCoord() - notes.get(j).getYpos(), 2));

			if (dist <= (mesh.getBBoxRadius() + (notes.get(j).getModel()
					.getBBoxRadius()))) {
				GameState.removePickup(j);
				this.addScore(10 * GameState.getMultiplier());
				GameState.playPickup();
				GameState.incrementTime(GameState.PICKUP_TIME_INCREMENT
						* GameState.DIMINISHING_RETURNS);
				GameState.pickedUp();
			}
		}
	}

	public void collideBoostTest(ArrayList<SpeedBoost> boost, Model mesh) {
		for (int j = 0; j < boost.size(); j++) {
			float dist = (float) Math.sqrt(Math.pow(this.getXCoord()
					- boost.get(j).getXpos(), 2)
					+ Math.pow(this.getYCoord() - boost.get(j).getYpos(), 2));

			if (dist <= (mesh.getBBoxRadius() + (boost.get(j).getModel()
					.getBBoxRadius() * 5f))) {
				GameState.boost();
				GameState.playBoost();
			}
		}
	}
}
