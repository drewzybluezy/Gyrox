

package com.PlaylistPanic;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;

public class PlaylistPanic extends Activity {
    /** Called when the activity is first created. */
	private OpenGLView _View;
	
	private Boolean _FocusChangeFalseSeen = false;
	private Boolean _Resume = false;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
		WindowManager w = getWindowManager();
	    Display d = w.getDefaultDisplay();
	    int width = d.getWidth();
	    int height = d.getHeight();
	   
	    super.onCreate(savedInstanceState);
	    
	    this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN);
	    
        _View = new OpenGLView(this, width, height);
        setContentView(_View);

    }
    
    
    @Override
    public void onPause() {
    	_View.onPause();
    	super.onPause();
    }
    
    @Override
    public void onResume() {
    	if(!_FocusChangeFalseSeen) {
    		_View.onResume();
    	}
    	_Resume = true;
    	super.onResume();
    }
    
    @Override
    public void onWindowFocusChanged(boolean focus) {
    	if(focus) {
    		if(_Resume) {
    			_View.onResume();
    		}
    		
    		_Resume = false;
    		_FocusChangeFalseSeen = false;
    	}
    	else {
    		_FocusChangeFalseSeen = true;
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