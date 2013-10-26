package com.dmurphy.gyrox.util;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.dmurphy.gyrox.R;

public class Preferences extends PreferenceActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.layout.preferences);
	}
}
