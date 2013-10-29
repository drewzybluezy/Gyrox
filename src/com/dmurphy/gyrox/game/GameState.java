package com.dmurphy.gyrox.game;

import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.os.SystemClock;

import com.dmurphy.gyrox.R;
import com.dmurphy.gyrox.entity.Pickup;
import com.dmurphy.gyrox.entity.Player;
import com.dmurphy.gyrox.entity.SpeedBoost;
import com.dmurphy.gyrox.game.StateManager.StateID;
import com.dmurphy.gyrox.model.Model;
import com.dmurphy.gyrox.model.Video;
import com.dmurphy.gyrox.sound.SoundManager;
import com.dmurphy.gyrox.ui.Camera;
import com.dmurphy.gyrox.ui.Camera.CamType;
import com.dmurphy.gyrox.ui.HUD;
import com.dmurphy.gyrox.util.UserPrefs;
import com.dmurphy.gyrox.world.Lighting;
import com.dmurphy.gyrox.world.Lighting.LightType;
import com.dmurphy.gyrox.world.WorldGraphics;

public class GameState implements State {

	// Define Time data
	public long timeLastFrame;
	public long timeCurrent;
	public long timeDelta;

	private static float boostRemaining = 0;
	private static final float BOOST_LENGTH = 500f;

	public static long endDelay = 0;
	private static final long DELAY_LENGTH = 4000;

	private static boolean boosted = false;

	public static float timeLeft;

	public static int multiplier = 1;
	public static int numNotesPickedUp = 0;
	public static long multLeft = 0;

	public static final int x2 = 4;
	public static final int x3 = 12;
	public static final int x4 = 28;
	public static final long MULT_LENGTH = 5000;

	private static float mCurrentGridSize;
	public static int mCurrentPickups;
	public static int numberToSpawn;

	public static boolean gameOver = false;

	private Model playerModel;
	private Model pickupModel;
	private Model boostModel;

	private Video video;
	private WorldGraphics world;
	private Lighting lights = new Lighting();

	public static final int INITIAL_NUM_PICKUPS = 10;
	public static final int INCREMENT_PICKUPS = 3;

	public static final float TIME_LIMIT = 30000f;
	public static final float PICKUP_TIME_INCREMENT = 1000f;
	public static final float DIMINISHING_RETURNS = 0.75f;

	private static Player player;
	public static ArrayList<Pickup> pickups = new ArrayList<Pickup>();
	public static ArrayList<SpeedBoost> boost = new ArrayList<SpeedBoost>();

	// Camera data
	private Camera camera;

	private boolean playing = false;

	boolean input = false;
	boolean reset = false;
	int inputDirection;

	boolean initialState = true;
	private boolean loading = true;

	public static int CRASH_SOUND = 1;
	public static int MUSIC_SOUND = 3;
	public static int PICKUP_SOUND = 2;
	public static int BOOST_SOUND = 4;

	HUD hud;
	Context mContext;
	GL10 gl;

	// Preferences
	public static UserPrefs mPrefs;

	public void init(Context ctx, GL10 gl, Video video) {
		this.gl = gl;
		this.mContext = ctx;
		this.video = video;

		// Load sounds
		SoundManager.getInstance();
		SoundManager.initSounds(mContext);
		SoundManager.addSound(CRASH_SOUND, R.raw.game_crash);
		SoundManager.addSound(PICKUP_SOUND, R.raw.pickup);
		SoundManager.addSound(BOOST_SOUND, R.raw.speed);
		SoundManager.addMusic(R.raw.motherboard);

		// Load HUD
		hud = new HUD(gl, mContext);

		// Load Models
		playerModel = new Model(mContext, R.raw.justicar);
		pickupModel = new Model(mContext, R.raw.star);
		boostModel = new Model(mContext, R.raw.arrow);

		newGame();

		// Initialize sounds
		if (mPrefs.playMusic())
			SoundManager.playMusic(true);

		resetTime();

		loading = false;
	}

	// hooks for android pausing thread
	public void pauseGame() {
		SoundManager.getInstance();
		SoundManager.globalPauseSound();
	}

	// hooks for android resuming thread
	public void resumeGame() {
		SoundManager.getInstance();
		SoundManager.globalResumeSound();

		if (mPrefs != null) {
			mPrefs.reloadPrefs();

			// Update options
			if (!initialState) {
				camera.updateType(mPrefs.CameraType());
			} else {
				reset = true;
			}

			if (mPrefs.playMusic())
				SoundManager.playMusic(true);
			else
				SoundManager.stopMusic();

			resetTime();
		}
	}

	public void addTouchEvent(float x, float y) {
		if (loading)
			return;

		if (player.getSpeed() > 0.0f) {
			if (initialState) {
				// Change the camera and start movement.
				camera = new Camera(player, mPrefs.CameraType());
				playing = true;

				if (mPrefs.playMusic() && !SoundManager.isPlaying())
					SoundManager.playMusic(true);

				hud.displayInstr(false);
				initialState = false;
			} else {
				if (x <= (video.getWidth() / 2)) {
					inputDirection = player.TURN_LEFT;
				} else {
					inputDirection = player.TURN_RIGHT;
				}
				input = true;
			}
		}

		else if (gameOver && (endDelay <= 0)) {
			reset = true;
		}
	}

	public void runGame() {
		updateTime();

		if (input) {
			player.doTurn(inputDirection, timeCurrent);
			input = false;
		}

		if (reset) {

			gameOver = false;
			playing = false;
			timeLeft = TIME_LIMIT;

			newGame();

			initialState = true;
			reset = false;
		}

		render();
	}

	// DT smoothing experiment
	private final int MAX_SAMPLES = 20;
	private long dtHist[] = new long[MAX_SAMPLES];
	private int dtHead = 0;
	private int dtElements = 0;

	private void resetTime() {
		timeLastFrame = SystemClock.uptimeMillis();
		timeCurrent = timeLastFrame;
		timeLeft = TIME_LIMIT;
		timeDelta = 0;
		dtHead = 0;
		dtElements = 0;
	}

	private void updateTime() {
		long realDt;
		int i;

		if (boostRemaining <= 0)
			boosted = false;

		timeLastFrame = timeCurrent;
		timeCurrent = SystemClock.uptimeMillis();
		realDt = timeCurrent - timeLastFrame;

		if (endDelay > 0)
			endDelay -= realDt;

		if (playing)
			timeLeft -= realDt;
		if (boosted)
			boostRemaining -= realDt;
		multLeft -= realDt;

		if (mCurrentPickups <= 0)
			newLevel();
		if (timeLeft <= 0)
			endGame();
		if (multLeft <= 0) {
			multiplier = 1;
			numNotesPickedUp = 0;
		}

		dtHist[dtHead] = realDt;
		dtHead++;

		if (dtHead >= MAX_SAMPLES) {
			dtHead = 0;
		}

		if (dtElements == MAX_SAMPLES) {
			// Average the last MAX_SAMPLE DT's
			timeDelta = 0;
			for (i = 0; i < MAX_SAMPLES; i++) {
				timeDelta += dtHist[i];
			}
			timeDelta /= MAX_SAMPLES;
		} else {
			timeDelta = realDt;
			dtElements++;
		}
	}

	public static void endGame() {
		if (gameOver == false)
			endDelay = DELAY_LENGTH;
		gameOver = true;
		player.setSpeed(0);
		pickups.clear();
		boost.clear();
	}

	private void render() {
		if (!initialState) {
			player.doMovement(timeDelta, timeCurrent);
		}

		camera.doCameraMovement(player, timeCurrent, timeDelta);

		gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

		// Load identity
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadIdentity();
		video.doPerspective(gl, mCurrentGridSize);
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();
		gl.glLightfv(GL10.GL_LIGHT1, GL10.GL_POSITION, camera.ReturnCamBuffer());

		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT
				| GL10.GL_STENCIL_BUFFER_BIT);
		gl.glEnable(GL10.GL_BLEND);

		camera.doLookAt(gl);

		gl.glDisable(GL10.GL_LIGHTING);
		gl.glDisable(GL10.GL_BLEND);
		gl.glDepthMask(false);
		gl.glDisable(GL10.GL_DEPTH_TEST);

		world.drawSkyBox(gl);
		//world.drawFloorTextured(gl);

		gl.glDepthMask(true);
		gl.glEnable(GL10.GL_DEPTH_TEST);

		lights.setupLights(gl, LightType.E_CYCLE_LIGHTS);

		if (player.isVisible(camera))
			player.draw(gl, timeCurrent, timeDelta, lights);

		if (!gameOver) {
			for (int i = 0; i < mCurrentPickups; i++) {
				pickups.get(i).draw(gl, lights, timeDelta);
			}
			for (int i = 0; i < boost.size(); i++) {
				boost.get(i).draw(gl, lights);
			}
		}

		hud.draw(video, timeDelta, player.getScore(), timeLeft, mCurrentPickups);
	}

	public static void removePickup(int i) {
		pickups.remove(i);
		mCurrentPickups--;
	}

	public static void incrementTime(float delta) {
		timeLeft += delta;
	}

	public void newLevel() {
		player.addScore((int) timeLeft / 10);
		numberToSpawn += INCREMENT_PICKUPS;
		timeLeft += TIME_LIMIT;

		for (int i = 0; i < numberToSpawn; i++) {
			pickups.add(new Pickup(mCurrentGridSize, pickupModel));
		}

		mCurrentPickups = numberToSpawn;
	}

	public static void playPickup() {
		if (mPrefs.playSFX())
			SoundManager.playSound(PICKUP_SOUND, 1.0f);
	}

	public static void playBoost() {
		if (mPrefs.playSFX())
			SoundManager.playSound(BOOST_SOUND, 1.0f);
	}

	public void newGame() {
		multiplier = 1;
		numNotesPickedUp = 0;
		endDelay = 0;

		mCurrentPickups = INITIAL_NUM_PICKUPS;
		numberToSpawn = INITIAL_NUM_PICKUPS;

		if (mPrefs != null) {
			mPrefs.reloadPrefs();
		} else {
			mPrefs = new UserPrefs(mContext);
			mCurrentGridSize = mPrefs.gridSize();
		}

		mCurrentGridSize = mPrefs.gridSize();
		world = new WorldGraphics(gl, mContext, mCurrentGridSize);

		// Setup perspective
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		video.doPerspective(gl, mCurrentGridSize);
		gl.glMatrixMode(GL10.GL_MODELVIEW);

		player = new Player(mCurrentGridSize, playerModel, hud);
		player.setSpeed(mPrefs.speed());

		hud.resetConsole();
		camera = new Camera(player, CamType.E_CAM_TYPE_CIRCLING);

		hud.displayInstr(true);

		for (int i = 0; i < numberToSpawn; i++) {
			pickups.add(new Pickup(mCurrentGridSize, pickupModel));
		}
		boost.add(new SpeedBoost(mCurrentGridSize, boostModel, 0.25f, 0.25f));
		boost.add(new SpeedBoost(mCurrentGridSize, boostModel, 0.25f, 0.75f));
		boost.add(new SpeedBoost(mCurrentGridSize, boostModel, 0.75f, 0.25f));
		boost.add(new SpeedBoost(mCurrentGridSize, boostModel, 0.75f, 0.75f));
	}

	public static void boost() {
		boosted = true;
		boostRemaining = BOOST_LENGTH;
	}

	public static float getBoostRemaining() {
		return boostRemaining;
	}

	public static int getMultiplier() {
		return multiplier;
	}

	public static void pickedUp() {
		numNotesPickedUp++;
		multLeft = MULT_LENGTH;
		switch (numNotesPickedUp) {
		case x2:
			multiplier = 2;
			break;
		case x3:
			multiplier = 3;
			break;
		case x4:
			multiplier = 4;
			break;
		}
	}

	@Override
	public void nextState(StateID state) {
		// TODO Auto-generated method stub

	}

}
