package com.dmurphy.gyrox.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.dmurphy.gyrox.ui.Camera;

public class UserPrefs {

	// Pref defaults
	private final int C_PREF_FOLLOW_CAM = 1;
	private final int C_PREF_FOLLOW_CAM_FAR = 2;
	private final int C_PREF_FOLLOW_CAM_CLOSE = 3;
	private final int C_PREF_BIRD_CAM = 4;

	private final String C_DEFAULT_CAM_TYPE = "1";

	private static final float C_GRID_SIZES[] = { 360.0f, 720.0f, 1440.0f };

	private static final float C_SPEED[] = { 5.0f, 10.0f, 15.0f, 20.0f };

	private Context mContext;
	private Camera.CamType mCameraType;

	private boolean mMusic;
	private boolean mSFX;

	private boolean mFPS;

	private float mGridSize;
	private float mSpeed;
	private int mPlayerColorIndex;

	public UserPrefs(Context ctx) {
		mContext = ctx;
		reloadPrefs();
	}

	public void reloadPrefs() {
		int cameraType;
		int gridIndex;
		int speedIndex;

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(mContext);
		cameraType = Integer.valueOf(prefs.getString("cameraPref",
				C_DEFAULT_CAM_TYPE));

		switch (cameraType) {
		case C_PREF_FOLLOW_CAM:
			mCameraType = Camera.CamType.E_CAM_TYPE_FOLLOW;
			break;
		case C_PREF_FOLLOW_CAM_FAR:
			mCameraType = Camera.CamType.E_CAM_TYPE_FOLLOW_FAR;
			break;
		case C_PREF_FOLLOW_CAM_CLOSE:
			mCameraType = Camera.CamType.E_CAM_TYPE_FOLLOW_CLOSE;
			break;
		case C_PREF_BIRD_CAM:
			mCameraType = Camera.CamType.E_CAM_TYPE_BIRD;
			break;
		default:
			mCameraType = Camera.CamType.E_CAM_TYPE_FOLLOW;
			break;
		}

		mMusic = prefs.getBoolean("musicOption", true);
		mSFX = prefs.getBoolean("sfxOption", true);
		mFPS = prefs.getBoolean("fpsOption", false);
		gridIndex = Integer.valueOf(prefs.getString("arenaSize", "1"));
		mGridSize = C_GRID_SIZES[gridIndex];
		speedIndex = Integer.valueOf(prefs.getString("gameSpeed", "1"));
		mSpeed = C_SPEED[speedIndex];
		mPlayerColorIndex = Integer.valueOf(prefs.getString("shipColor", "0"));
	}

	public Camera.CamType CameraType() {
		return mCameraType;
	}

	public boolean playMusic() {
		return mMusic;
	}

	public boolean playSFX() {
		return mSFX;
	}

	public boolean drawFPS() {
		return mFPS;
	}

	public float gridSize() {
		return mGridSize;
	}

	public float speed() {
		return mSpeed;
	}

	public int playerColorIndex() {
		return mPlayerColorIndex;
	}
}
