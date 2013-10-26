
package com.dmurphy.gyrox.util;

import com.dmurphy.gyrox.R;
import com.dmurphy.gyrox.R.layout;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class Preferences extends PreferenceActivity {
	 @Override
	    protected void onCreate(Bundle savedInstanceState) {
	        // TODO Auto-generated method stub
	        super.onCreate(savedInstanceState);
	        addPreferencesFromResource(R.layout.preferences);
	    }
}
