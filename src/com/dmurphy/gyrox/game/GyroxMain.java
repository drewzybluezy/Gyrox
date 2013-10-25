

package com.dmurphy.gyrox.game;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.os.Handler;
import android.os.SystemClock;

import com.dmurphy.gyrox.R;
import com.dmurphy.gyrox.game.Camera.CamType;
import com.dmurphy.gyrox.sound.SoundManager;
import com.dmurphy.gyrox.video.*;
import com.dmurphy.gyrox.video.Lighting.LightType;


public class GyroxMain {

	// Define Time data
	public long TimeLastFrame;
	public long TimeCurrent;
	public long TimeDt;
	
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
	
	// Define game textures
	private GLTexture explodeTex;
	private GLTexture SplashScreen;
	
	private Model playerModel;
	private Model noteModel;
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
	public static ArrayList<Note> notes = new ArrayList<Note>();
	public static ArrayList<SpeedBoost> boost = new ArrayList<SpeedBoost>();
	
	// Camera data
	private Camera cam;
	
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
	
	public Segment walls[] = {
			new Segment(),
			new Segment(),
			new Segment(),
			new Segment()
	};
	
	// Preferences
	public static UserPrefs mPrefs;
	
	public GyroxMain() {
		initWalls();
	}
	
	public void initGame() {
		
		// Load sounds
	    SoundManager.getInstance();
	    SoundManager.initSounds(mContext);
	    SoundManager.addSound(CRASH_SOUND, R.raw.game_crash);
	    SoundManager.addSound(PICKUP_SOUND, R.raw.pickup);
	    SoundManager.addSound(BOOST_SOUND, R.raw.speed);
	    SoundManager.addMusic(R.raw.contact);
	    
	    // Load HUD
	    hud = new HUD(gl,mContext);
	    
		// Load Models
		playerModel = new Model(mContext,R.raw.justicar);
		noteModel = new Model(mContext, R.raw.noteobj);
		boostModel = new Model(mContext, R.raw.arrow);
		
		explodeTex = new  GLTexture(gl,mContext, R.drawable.impact);
		
		newGame();
		
	    // Initialize sounds
	    if(mPrefs.playMusic())
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

		if(mPrefs != null) {
			mPrefs.reloadPrefs();

			// Update options
			if(!initialState) {
				cam.updateType(mPrefs.CameraType());
			}
			else {
				reset = true;
			}
			
			if(mPrefs.playMusic())
				SoundManager.playMusic(true);
			else
				SoundManager.stopMusic();			
			
			resetTime();
		}
	}
	
	public void drawSplash(Context ctx, GL10 gl1) {
		float verts[] = {
			-1.0f, 1.0f, 0.0f,
			1.0f,  1.0f, 0.0f,
			-1.0f, -1.0f,0.0f,
			1.0f,  -1.0f, 0.0f
		};
		
		float texture[] = {
			0.0f, 1.0f, 
			1.0f, 1.0f,
			0.0f, 0.0f, 
			1.0f, 0.0f
		};

		gl = gl1;
		mContext = ctx;

		FloatBuffer vertfb = GraphicUtils.ConvToFloatBuffer(verts);
		FloatBuffer texfb = GraphicUtils.ConvToFloatBuffer(texture);

		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

		gl.glLoadIdentity();
		
		gl.glEnable(GL10.GL_TEXTURE_2D);
		if(SplashScreen == null)
			SplashScreen = new GLTexture(gl,mContext,R.drawable.splash);

		gl.glBindTexture(GL10.GL_TEXTURE_2D, SplashScreen.getTextureID());

		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertfb);
		gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, texfb);
		gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);
		
		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL10.GL_NORMAL_ARRAY);
		gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		
	}
	
	public void updateScreenSize(int width, int height) {
		if(video == null) {
			video = new Video(width, height);
		} else {
			video.setSize(width, height);
		}
		

	}
	
	public void addTouchEvent(float x, float y) {
		if(loading)
			return;
		
		if(player.getSpeed() > 0.0f) {
			if(initialState) {
				// Change the camera and start movement.
				cam = new Camera(player, mPrefs.CameraType());
				playing = true;
				
				if(mPrefs.playMusic() && !SoundManager.isPlaying())
					SoundManager.playMusic(true);
				
				hud.displayInstr(false);
				initialState = false;
			}
			else {
				if(x <= (video.getWidth() / 2)) {
					inputDirection = player.TURN_LEFT;
				}
				else {
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
		
		if(input) {
			player.doTurn(inputDirection, TimeCurrent);
			input = false;
		}
		
		if(reset) {
			
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
	private long DtHist[] = new long[MAX_SAMPLES];
	private int DtHead = 0;
	private int DtElements = 0;
	
	private void resetTime() {
		TimeLastFrame = SystemClock.uptimeMillis();
		TimeCurrent = TimeLastFrame;
		timeLeft =  TIME_LIMIT;
		TimeDt = 0;
		DtHead = 0;
		DtElements = 0;
	}
	
	private void updateTime() {	
		long RealDt;
		int i;
		
		if (boostRemaining <= 0) boosted = false;
		
		TimeLastFrame = TimeCurrent;
		TimeCurrent = SystemClock.uptimeMillis();
		RealDt = TimeCurrent - TimeLastFrame;
		
		if (endDelay > 0) endDelay -= RealDt;
		
		if (playing) timeLeft -= RealDt;
		if (boosted) boostRemaining -= RealDt;
		multLeft -= RealDt;
		
		if (mCurrentPickups <= 0) newLevel();	
		if (timeLeft <= 0) endGame();
		if (multLeft <= 0) {
			multiplier = 1;
			numNotesPickedUp = 0;
		}
		
		DtHist[DtHead] = RealDt;	
		DtHead++;
		
		if(DtHead >= MAX_SAMPLES) {
			DtHead = 0;
		}
		
		if(DtElements == MAX_SAMPLES){
			// Average the last MAX_SAMPLE DT's
			TimeDt = 0;
			for(i = 0; i < MAX_SAMPLES; i++) {
				TimeDt += DtHist[i];
			}
			TimeDt /= MAX_SAMPLES;
		}
		else {
			TimeDt = RealDt;
			DtElements++;
		}
	}
	
	public static void endGame() {
		if (gameOver == false) endDelay = DELAY_LENGTH;
		gameOver = true;
		player.setSpeed(0);
		notes.clear();
		boost.clear();
	}

	private void initWalls() {
		float raw[][] = {
				{0.0f, 0.0f, 1.0f, 0.0f },
				{ 1.0f, 0.0f, 0.0f, 1.0f },
				{ 1.0f, 1.0f, -1.0f, 0.0f },
				{ 0.0f, 1.0f, 0.0f, -1.0f }
		};
		
		float width = mCurrentGridSize;
		float height = mCurrentGridSize;
		
		int j;
		
		for(j = 0; j < 4; j++) {
			walls[j].vStart.v[0] = raw[j][0] * width;
			walls[j].vStart.v[1] = raw[j][1] * height;
			walls[j].vDirection.v[0] = raw[j][2] * width;
			walls[j].vDirection.v[1] = raw[j][3] * height;
		}
	}

	
	private void render() {
		
		if(!initialState) {
			player.doMovement(TimeDt,TimeCurrent,walls);
		}
		
		cam.doCameraMovement(player,TimeCurrent, TimeDt);

		gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		
		// Load identity
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadIdentity();
		video.doPerspective(gl, mCurrentGridSize);
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();
		gl.glLightfv(GL10.GL_LIGHT1, GL10.GL_POSITION, cam.ReturnCamBuffer());

		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT | GL10.GL_STENCIL_BUFFER_BIT);
		gl.glEnable(GL10.GL_BLEND);

		cam.doLookAt(gl);
		
		gl.glDisable(GL10.GL_LIGHTING);
		gl.glDisable(GL10.GL_BLEND);
		gl.glDepthMask(false);
		gl.glDisable(GL10.GL_DEPTH_TEST);
		
		world.drawSkyBox(gl);
		world.drawFloorTextured(gl);
		
		gl.glDepthMask(true);
		gl.glEnable(GL10.GL_DEPTH_TEST);
		
		world.drawWalls(gl);
		
		lights.setupLights(gl, LightType.E_CYCLE_LIGHTS);


		if(player.isVisible(cam))
			player.drawCycle(gl, TimeCurrent, TimeDt, lights, explodeTex);
		
		if (!gameOver) {
			for(int i = 0; i < mCurrentPickups; i++) {
				notes.get(i).draw(gl, lights, TimeDt);
			}
			for (int i = 0; i < boost.size(); i++) {
				boost.get(i).draw(gl, lights);
			}
		}

		hud.draw(video,TimeDt,player.getScore(), timeLeft, mCurrentPickups);
	}
	
	public static void removePickup(int i) {
		notes.remove(i);
		mCurrentPickups--;
	}
	
	public static void incrementTime(float delta) {
		timeLeft += delta;
	}
	
	public void newLevel() {
		player.addScore((int)timeLeft / 10);
		numberToSpawn += INCREMENT_PICKUPS;
		timeLeft += TIME_LIMIT;
		
		for (int i = 0; i < numberToSpawn; i++) {
			notes.add(new Note(mCurrentGridSize, noteModel));
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
		}
		else {
		    mPrefs = new UserPrefs(mContext);
		    mCurrentGridSize = mPrefs.gridSize();
		}
		
		mCurrentGridSize = mPrefs.gridSize();
			
		// re-init the world
		initWalls();
			
		world = new WorldGraphics(gl, mContext, mCurrentGridSize);

		// Setup perspective
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		video.doPerspective(gl, mCurrentGridSize);
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		
		player = new Player(mCurrentGridSize, playerModel, hud);
		player.setSpeed(mPrefs.speed());
		
		hud.resetConsole();
		cam = new Camera(player, CamType.E_CAM_TYPE_CIRCLING);
		
		hud.displayInstr(true);
		
		for (int i = 0; i < numberToSpawn; i++) {
			notes.add(new Note(mCurrentGridSize, noteModel));
		}
		
		boost.add(new SpeedBoost(mCurrentGridSize, boostModel, 0.5f, 0.5f));
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
		switch(numNotesPickedUp) {
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
	
}
