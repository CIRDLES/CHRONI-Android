package org.cirdles.chroni;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;

import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.view.Menu;
import android.widget.TextView;

public class HomeScreen extends Activity implements FilenameFilter {

	// Maintains whether app is initalizing for the first time or not 
	private static final String PREF_FIRST_LAUNCH = "First Launch";
	
	// Version number
	private TextView versionNumber;
	
	CirdlesDatabaseHelper preloadedAliquots; // Database

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTheme(android.R.style.Theme_Holo);
		setContentView(R.layout.home_screen);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        
        // Checks to see if this is launch
//		checkCredentials();        
        
        try {
	        // Puts the versioning information on the App
	        Context context = this;
	        int versionCode = context.getPackageManager().getPackageInfo (context.getPackageName(), 0).versionCode;
	        String versionName = context.getPackageManager().getPackageInfo (context.getPackageName(), 0).versionName;
		
	        versionNumber = (TextView) findViewById(R.id.versionNumber);
	        versionNumber.setText("Version " + versionCode + "." + versionName);
	        versionNumber.setTextColor(getResources().getColor(R.color.button_blue));
	        
			//Creates the necessary application directories
			createDirectories();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        // Puts demo items in the history database
        preloadedAliquots = new CirdlesDatabaseHelper(this);
        if(!preloadedAliquots.isPresent("Demo Aliquot")){
        	preloadedAliquots.createEntry("01/11/1111", "Demo Aliquot");      
        }
        
		Thread timer = new Thread(){
			public void run(){
				try{
					sleep(3500); // The home screen is shown for two seconds
				}catch(InterruptedException e){
					e.printStackTrace();
				}finally{
					// Tests to see whether the user has completed a profile yet.
					// If not, proceeds to the User Profile login screen, else moves to Main Menu.
//					SharedPreferences appSettings = getSharedPreferences(USER_REGISTERED, 0);
//					if(!appSettings.getBoolean("User Registered", false)){
//						Intent openLoginScreen = new Intent("android.intent.action.LOGINSCREEN");
//						startActivity(openLoginScreen);
//					}else{
						Intent openMainMenu = new Intent("android.intent.action.MAINMENU");
						openMainMenu.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(openMainMenu);
//					}
				}
			}
		};
		timer.start();
	}
        
	/*
	 * Creates the necessary application directories: CIRDLES, Aliquot and Report Settings folders
	 */
	protected void createDirectories() throws FileNotFoundException {
		// Establishes the CIRDLES root folder
		File cirdlesDirectory = new File(Environment.getExternalStorageDirectory()+ "/CHRONI/");
		File aliquotDirectory = new File(Environment.getExternalStorageDirectory()+ "/CHRONI/Aliquot");
		File reportSettingsDirectory = new File(Environment.getExternalStorageDirectory()+ "/CHRONI/Report Settings");
		
		// Creates the directories if they are not there
		if(cirdlesDirectory.exists()){
			cirdlesDirectory.mkdirs();
		}
		if(aliquotDirectory.exists()){
			aliquotDirectory.mkdirs();
		}
		if(reportSettingsDirectory.exists()){
			reportSettingsDirectory.mkdirs();
		}
		// Downloads Default Report Settings file
		if(accept(reportSettingsDirectory, "Default Report Settings")){
			URLFileReader downloader = new URLFileReader(HomeScreen.this, "HomeScreen", "http://cirdles.org/sites/default/files/Downloads/CIRDLESDefaultReportSettings.xml", "url");	
		}
		// Notes that files have been downloaded and application has been properly initialized
		storeSharedPrefs();
	}

	@Override
	/*
	 * Checks to see if file is already on device
	 */
	public boolean accept(File dir, String fileName) {
		return fileName.contains("Default Report Settings");
	}
	
    /*
     * Storing in Shared Preferences
     */
	 protected void storeSharedPrefs() {
		SharedPreferences settings = getSharedPreferences(PREF_FIRST_LAUNCH, 0);
		SharedPreferences.Editor editor = settings.edit();
		 editor.putBoolean(PREF_FIRST_LAUNCH, false);
		 editor.commit();  //Commiting changes
	    } 
	
     /* 
      * Checking Shared Preferences if the user had pressed 
      * the remember me button last time he logged in
      * */
	 private boolean isFirstTime() {
		 return PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean(PREF_FIRST_LAUNCH, true);
	    }
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
