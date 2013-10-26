package com.dmurphy.gyrox.game;

import java.util.HashMap;

import javax.microedition.khronos.opengles.GL10;

import com.dmurphy.gyrox.model.Video;

import android.content.Context;

public class StateManager {
	
	private static HashMap<StateID, State> stateMap;
	private static State currentState;
	
	public enum StateID {
		MENU_STATE, GAME_STATE, SPLASH_STATE
	}

	public static void init() {
		stateMap = new HashMap<StateID, State>();
		stateMap.put(StateID.MENU_STATE, new MenuState());
		stateMap.put(StateID.GAME_STATE, new GameState());
		stateMap.put(StateID.SPLASH_STATE, new SplashState());
	}
	
	public static void runState(StateID key, Context ctx, GL10 gl, Video video) {
		State s = stateMap.get(key);
		currentState = s;
		
		s.init(ctx, gl, video);		
	}
	
	public static State getState(StateID key) {
		return stateMap.get(key);
	}
	
	public static State getCurrentState() {
		return currentState;
	}
}
