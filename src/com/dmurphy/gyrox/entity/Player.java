package com.dmurphy.gyrox.entity;

import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL10;

import com.dmurphy.gyrox.game.GameState;
import com.dmurphy.gyrox.model.Model;
import com.dmurphy.gyrox.model.Vector;
import com.dmurphy.gyrox.ui.Camera;
import com.dmurphy.gyrox.ui.HUD;
import com.dmurphy.gyrox.world.Lighting;

public class Player {

	private final Model ship;
	private final int shipColor;

	private int score;
	private final Vector position;

	private Vector playerRotation = new Vector(0.0f, 0.0f, 0.0f);

	private float speed;
	public long turnTime;

	private final float SPEED_OZ_FREQ = 1200.0f;
	private final float SPEED_OZ_FACTOR = 0.09f;
	private final int LOD_DIST[][] = { { 1000, 1000, 1000 }, { 100, 200, 400 },
			{ 30, 100, 200 }, { 10, 30, 150 } };

	public Player(float gridSize, Model mesh, HUD hud) {
		speed = 10.0f;
		ship = mesh;
		score = 0;

		shipColor = GameState.mPrefs.playerColorIndex();

		position = new Vector(gridSize / 2, gridSize / 2, 2.0f);
	}

	public void doTurn(Vector gyroscope, long time) {
		playerRotation = playerRotation.add(gyroscope);
	}

	public void doMovement(long timeDelta, long time) {
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

			/*
			 * position.point[0] += t * DIRS_X[direction]; position.point[1] +=
			 * t * DIRS_Y[direction]; position.point[2] += t *
			 */

			doCrashTestPickup(GameState.pickups, ship);
			collideBoostTest(GameState.boost, ship);
		}
	}

	public void draw(GL10 gl, long time, long timeDelta, Lighting lights) {
		gl.glPushMatrix();
		gl.glTranslatef(getXCoord(), getYCoord(), 0.0f);

		doShipRotation(gl, time);
		gl.glEnable(GL10.GL_LIGHTING);

		gl.glEnable(GL10.GL_DEPTH_TEST);
		gl.glDepthMask(true);
		gl.glEnable(GL10.GL_NORMALIZE);
		gl.glTranslatef(0.0f, 0.0f, ship.getBBoxSize().point[2] / 2.0f);
		gl.glEnable(GL10.GL_CULL_FACE);
		ship.draw(gl, PlayerColor.specular[shipColor],
				PlayerColor.diffuse[shipColor]);
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

			if (s < d - (ship.getBBoxRadius() * 2.0f)) {
				retValue = false;
			} else {
				retValue = true;
			}

		}

		return retValue;
	}

	private void doShipRotation(GL10 gl, long currentTime) {
		gl.glRotatef(playerRotation.point[2], 1.0f, 0.0f, 1.0f);
		gl.glRotatef(playerRotation.point[0], 0.0f, 1.0f, 0.0f);
		gl.glRotatef(playerRotation.point[1], 0.0f, 0.0f, 1.0f);
	}

	public float getXCoord() {
		return position.point[0];
	}

	public float getYCoord() {
		return position.point[1];
	}

	public float getSpeed() {
		return speed;
	}

	public void setSpeed(float sp) {
		speed = sp;
	}

	public void addScore(int val) {
		score += val;
	}

	public int getScore() {
		return score;
	}

	public void doCrashTestPickup(ArrayList<Pickup> pickups, Model mesh) {
		for (int j = 0; j < pickups.size(); j++) {
			float dist = (float) Math
					.sqrt(Math.pow(this.getXCoord()
							- pickups.get(j).getXCoord(), 2)
							+ Math.pow(this.getYCoord()
									- pickups.get(j).getYCoord(), 2));

			if (dist <= (mesh.getBBoxRadius() + ((pickups.get(j).getModel()
					.getBBoxRadius() * 3)))) {
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
