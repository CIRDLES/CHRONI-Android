package com.example.cirdles;

import android.os.Bundle;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.view.Menu;

public class ReportSettingsMenu extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTheme(android.R.style.Theme_DeviceDefault_NoActionBar_Fullscreen);
		setContentView(R.layout.report_settings_select);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}

}
