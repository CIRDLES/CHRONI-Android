package org.cirdles.chroni;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.math.BigDecimal;
import android.widget.Toast;

/*
This activity is used for structuring the main menu layout of the application.
 */
public class MainMenuActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	// Sets up layout
	super.onCreate(savedInstanceState);
	setTheme(android.R.style.Theme_Holo);
	setContentView(R.layout.main_menu);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);

     //Places background image on layout due to theme overriding
     RelativeLayout layout =(RelativeLayout)findViewById(R.id.mainMenuBackground);
     layout.setBackground(getResources().getDrawable(R.drawable.background));

	try {
	    // Puts the versioning information on the App
	    Context context = this;
	    int versionCode = context.getPackageManager().getPackageInfo(
		    context.getPackageName(), 0).versionCode;
	    String versionName = context.getPackageManager().getPackageInfo(
		    context.getPackageName(), 0).versionName;

	    TextView versionNumber = (TextView) findViewById(R.id.versionNumberMainMenu);
	    versionNumber.setText("Version " + versionCode + "." + versionName);
	    versionNumber.setTextColor(getResources().getColor(
		    R.color.button_blue));

	} catch (NameNotFoundException e) {
	    e.printStackTrace();
	}

    // Allows user to select an aliquot file for viewing in the display table
    Button openButton = (Button) findViewById(R.id.menuOpenButton);
	openButton.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
            Intent openDisplay = new Intent(
                    "android.intent.action.ALIQUOTMENU");
            startActivity(openDisplay);
        }
    });

    // Allows user to open the history activity
    Button historyButton = (Button) findViewById(R.id.menuHistoryButton);
	historyButton.setOnClickListener(new View.OnClickListener() {
	    public void onClick(View v) {
		Intent openHistoryTable = new Intent(
			"android.intent.action.HISTORY");
		startActivity(openHistoryTable);

//            Toast.makeText(MainMenuActivity.this, "This feature is currently unavailable.", Toast.LENGTH_LONG).show();
        }
	});

    // Allows user to access the profile management feature
	Button credentialsButton = (Button) findViewById(R.id.menuProfileButton);
	credentialsButton.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
            Intent openUserProfile = new Intent(
                    "android.intent.action.USERPROFILE");
            startActivity(openUserProfile);
        }
    });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handles menu item selection
        switch (item.getItemId()) {
            case R.id.returnToMenu: // Takes user to main menu
                Intent openMainMenu = new Intent("android.intent.action.MAINMENU");
                startActivity(openMainMenu);
                return true;
            case R.id.editProfileMenu: //Takes user to credentials screen
                Intent openUserProfile = new Intent(
                        "android.intent.action.USERPROFILE");
                startActivity(openUserProfile);
                return true;
            case R.id.historyMenu: //Takes user to credentials screen
                Intent openHistoryTable = new Intent(
                        "android.intent.action.HISTORY");
                startActivity(openHistoryTable);
                return true;
            case R.id.viewAliquotsMenu: // Takes user to aliquot menu
                Intent openAliquotFiles = new Intent(
                        "android.intent.action.FILEPICKER");
                openAliquotFiles.putExtra("Default_Directory",
                        "Aliquot_Directory");
                startActivity(openAliquotFiles);
                return true;
            case R.id.viewReportSettingsMenu: // Takes user to report settings menu
                Intent openReportSettingsFiles = new Intent(
                        "android.intent.action.FILEPICKER");
                openReportSettingsFiles.putExtra("Default_Directory",
                        "Report_Settings_Directory");
                startActivity(openReportSettingsFiles);
                return true;
            case R.id.viewRootMenu:
                Intent openRootDirectory = new Intent(
                        "android.intent.action.FILEPICKER");
                openRootDirectory.putExtra("Default_Directory",
                        "Root_Directory");
                startActivity(openRootDirectory);
                return true;
            case R.id.aboutScreen: // Takes user to about screen
                Intent openAboutScreen = new Intent(
                        "android.intent.action.ABOUT");
                startActivity(openAboutScreen);
                return true;
            case R.id.helpMenu: // Takes user to help blog
                Intent openHelpBlog = new Intent(Intent.ACTION_VIEW,
                        Uri.parse(getString(R.string.chroni_help_address)));
                startActivity(openHelpBlog);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}