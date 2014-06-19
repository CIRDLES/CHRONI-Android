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
import android.widget.Toast;

public class UserProfile extends Activity {

	private EditText geochronUsernameInput, geochronPasswordInput;
	private Button profileSaveButton, profileCancelButton;

	private String geochronUsername, geochronPassword;	// the login values on file
	
	public static final String USER_REGISTERED = "My CIRDLES Application";
	public static final String USER_PREFS = "My CIRDLES Settings";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setTheme(android.R.style.Theme_Holo);
		setContentView(R.layout.user_profile);
				
		profileSaveButton = (Button) findViewById(R.id.profileSaveButton);
		profileSaveButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				//	Stores the login information in shared preferences for a new user
				SharedPreferences settings = getSharedPreferences(USER_PREFS, 0);
				SharedPreferences.Editor editor = settings.edit();
				editor.clear(); // Clears previously stored prefs
				editor.putString("Geochron Username", geochronUsernameInput.getText().toString());
				editor.putString("Geochron Password", geochronPasswordInput.getText().toString());
				editor.commit();
				Toast.makeText(UserProfile.this, "Your Geochron Profile information is saved!", 3000).show();
				
		    	Intent openMainMenu = new Intent("android.intent.action.MAINMENU");
		    	startActivity(openMainMenu);		    		
		    	}
			});	

		profileCancelButton = (Button) findViewById(R.id.profileCancelButton);
		profileCancelButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent openMainMenu = new Intent("android.intent.action.MAINMENU");
		    	startActivity(openMainMenu);		    		
		    	}
		});	
		
		// Initializes profile information with currently stored profile or, if no one has been registered, sets as empty 
		checkCredentials(); // Checks to see if there is a profile stored 
		geochronUsernameInput = (EditText) findViewById(R.id.geochronUsername);
		geochronPasswordInput = (EditText) findViewById(R.id.geochronPassword);
		
		if(!geochronUsername.contentEquals("None") && !geochronPassword.contentEquals("None")){
			geochronUsernameInput.setText(getGeochronUsername());
			geochronPasswordInput.setText(getGeochronPassword());
		
//			// Changes save button to appropriate text and clears input if edits are made
//			if(geochronUsernameInput.getText().toString() != getGeochronUsername()){
//				profileSaveButton.setText("Save");
//			}else{
//				profileSaveButton.setText("Edit");
//				geochronPasswordInput.setText("");
//			}
//		
//			if(geochronPasswordInput.getText().toString() != getGeochronPassword()){
//				profileSaveButton.setText("Save");
//			}else{
//				profileSaveButton.setText("Edit");
//			}
		
		}	
		
	}
	
	/*
	 * Retrieves the Shared Preferences
	 */
	private void checkCredentials(){
		SharedPreferences settings = getSharedPreferences(USER_PREFS, 0);
		setGeochronUsername(settings.getString("Geochron Username", "None"));
		setGeochronPassword(settings.getString("Geochron Password", "None"));
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
	
	public String getGeochronUsername() {
		return geochronUsername;
	}

	public void setGeochronUsername(String geochronUsername) {
		this.geochronUsername = geochronUsername;
	}

	public String getGeochronPassword() {
		return geochronPassword;
	}

	public void setGeochronPassword(String geochronPassword) {
		this.geochronPassword = geochronPassword;
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
               
             default:
                return super.onOptionsItemSelected(item);
        }
    }

}