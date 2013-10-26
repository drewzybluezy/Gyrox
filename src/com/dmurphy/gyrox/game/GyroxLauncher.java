package com.dmurphy.gyrox.game;

import com.dmurphy.gyrox.util.Preferences;
import com.dmurphy.gyrox.view.OpenGLView;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;

public class GyroxLauncher extends Activity {
    /** Called when the activity is first created. */
	private OpenGLView view;
	
	private Boolean focusChangeFalseSeen = false;
	private Boolean resume = false;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
		WindowManager w = getWindowManager();
	    Display d = w.getDefaultDisplay();
	    Point p = new Point();
	    d.getSize(p);
	    int width = p.x;
	    int height = p.y;
	   
	    super.onCreate(savedInstanceState);
	    
	    this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN);
	    
        view = new OpenGLView(this, width, height);
        setContentView(view);
    }
    
    
    @Override
    public void onPause() {
    	view.onPause();
    	super.onPause();
    }
    
    @Override
    public void onResume() {
    	if(!focusChangeFalseSeen) {
    		view.onResume();
    	}
    	resume = true;
    	super.onResume();
    }
    
    @Override
    public void onWindowFocusChanged(boolean focus) {
    	if(focus) {
    		if(resume) {
    			view.onResume();
    		}
    		
    		resume = false;
    		focusChangeFalseSeen = false;
    	}
    	else {
    		focusChangeFalseSeen = true;
    	}
    }   
    
    //open menu when key pressed
     public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
        	this.startActivity(new Intent(this, Preferences.class));
        }
        return super.onKeyUp(keyCode, event);
    }
}