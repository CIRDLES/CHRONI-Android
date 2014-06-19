package org.cirdles.chroni;

/*
 * This class is used to collect the information from a new user.
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

public class UserProfile extends Activity {

	private EditText geochronUsernameInput, geochronPasswordInput;
	private Button loginSubmitButton;

	private String geochronUsername, geochronPassword;	// the login values on file
	
	public static final String USER_REGISTERED = "My CIRDLES Application";
	public static final String USER_PREFS = "My CIRDLES Settings";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setTheme(android.R.style.Theme_Holo);
		setContentView(R.layout.user_profile);

		geochronUsernameInput = (EditText) findViewById(R.id.geochronUsername);
		geochronUsername = geochronUsernameInput.getText().toString();

		geochronPasswordInput = (EditText) findViewById(R.id.geochronPassword);
		geochronPassword = geochronPasswordInput.getText().toString();
		
		loginSubmitButton = (Button) findViewById(R.id.profileSaveButton);
		loginSubmitButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				//	Stores the login information in shared preferences for a new user
				SharedPreferences settings = getSharedPreferences(USER_PREFS, 0);
				SharedPreferences.Editor editor = settings.edit();
				editor.putString("Geochron Username", geochronUsername);
				editor.putString("Geochron Password", geochronPassword);
				editor.commit();

				SharedPreferences applicationSettings = getSharedPreferences(USER_REGISTERED, 0);
				SharedPreferences.Editor applicationEditor = applicationSettings.edit();
				applicationEditor.putBoolean("User Registered", true);
				applicationEditor.commit();
				
		    	Intent openMainMenu = new Intent("android.intent.action.MAINMENU");
		    	startActivity(openMainMenu);		    		
		    	}
			});	
	}
	
	/*
	 * This method gets the current time.
	 */
	public String getTime(){
		java.text.DateFormat dateFormat = new SimpleDateFormat("KK:mm:ss a MM/dd/yy");
		Date date = new Date();
		String time = dateFormat.format(date);
		return time;
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onStop()
	 */
	@Override
    protected void onStop(){
       super.onStop();
       
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
	        	Intent openUserProfile = new Intent("android.intent.action.USERPROFILE");
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

//            case R.id.selectAliquotMenu:
//            	Intent openAliquotMenu = new Intent("android.intent.action.ALIQUOTMENU");
//				startActivity(openAliquotMenu);
//                return true;
//            case R.id.selectReportSettingsMenu:
//            	Intent openReportSettingsMenu = new Intent("android.intent.action.REPORTSETTINGSMENU");
//				startActivity(openReportSettingsMenu);
//                return true;
                
             default:
                return super.onOptionsItemSelected(item);
        }
    }

}