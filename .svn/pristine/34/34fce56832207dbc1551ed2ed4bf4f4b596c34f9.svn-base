package com.example.cirdles;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class Display extends Activity {
	private TextView testUrl;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTheme(android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
		setContentView(R.layout.display);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }
	
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        	case R.id.returnToMenu:
        		Intent openMainMenu = new Intent("android.intent.action.MAINMENU");
        		startActivity(openMainMenu);
        		return true;
            case R.id.editProfileMenu:
	        	Intent openUserProfile = new Intent("android.intent.action.MAIN");
	        	startActivity(openUserProfile);
	            return true;
	        case R.id.helpMenu:
				Intent openHelpBlog = new Intent(Intent.ACTION_VIEW, Uri.parse("http://joyenettles.blogspot.com"));
				startActivity(openHelpBlog);
	        	return true;
	        case R.id.exitProgram:
                finish();
                System.exit(0);
                
//            case R.id.deleteFileMenu:
//            	Intent openReportSettingsMenu = new Intent("android.intent.action.REPORTSETTINGSMENU");
//				startActivity(openReportSettingsMenu);
//                return true;
//            case R.id.renameFileMenu:
//            	Intent openAliquotMenu = new Intent("android.intent.action.ALIQUOTMENU");
//				startActivity(openAliquotMenu);
//                return true;
//            case R.id.defaultFileMenu:
//            	Intent openAliquotMenu = new Intent("android.intent.action.ALIQUOTMENU");
//				startActivity(openAliquotMenu);
//                return true;

            case R.id.selectAliquotMenu:
            	Intent openAliquotMenu = new Intent("android.intent.action.ALIQUOTMENU");
				startActivity(openAliquotMenu);
                return true;
            case R.id.selectReportSettingsMenu:
            	Intent openReportSettingsMenu = new Intent("android.intent.action.REPORTSETTINGSMENU");
				startActivity(openReportSettingsMenu);
                return true;
                
             default:
                return super.onOptionsItemSelected(item);
        }
    }


}
