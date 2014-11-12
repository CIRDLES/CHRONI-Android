package org.cirdles.chroni;

import java.io.File;
import java.io.FileNotFoundException;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainMenuActivity extends Activity {

    private Button viewButton, historyButton, profileButton;
    private TextView versionNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	// Sets up layout
	super.onCreate(savedInstanceState);
	setTheme(android.R.style.Theme_Holo);
	setContentView(R.layout.main_menu);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);

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

	    versionNumber = (TextView) findViewById(R.id.versionNumberMainMenu);
	    versionNumber.setText("Version " + versionCode + "." + versionName);
	    versionNumber.setTextColor(getResources().getColor(
		    R.color.button_blue));

	} catch (NameNotFoundException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

	viewButton = (Button) findViewById(R.id.menuOpenButton);
	viewButton.setOnClickListener(new View.OnClickListener() {
	    public void onClick(View v) {
		Intent openDisplay = new Intent(
			"android.intent.action.ALIQUOTMENU");
		startActivity(openDisplay);
	    }
	});

	historyButton = (Button) findViewById(R.id.menuHistoryButton);
	historyButton.setOnClickListener(new View.OnClickListener() {
	    public void onClick(View v) {
		Intent openHistoryTable = new Intent(
			"android.intent.action.HISTORY");
		startActivity(openHistoryTable);
	    }
	});

	profileButton = (Button) findViewById(R.id.menuProfileButton);
	profileButton.setOnClickListener(new View.OnClickListener() {
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
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.returnToMenu:
                Intent openMainMenu = new Intent("android.intent.action.MAINMENU");
                startActivity(openMainMenu);
                return true;
            case R.id.editProfileMenu:
                Intent openUserProfile = new Intent(
                        "android.intent.action.USERPROFILE");
                startActivity(openUserProfile);
                return true;
            case R.id.aboutScreen:
                Intent openAboutScreen = new Intent(
                        "android.intent.action.ABOUT");
                startActivity(openAboutScreen);
                return true;
            case R.id.helpMenu:
                Intent openHelpBlog = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://chronihelpblog.wordpress.com"));
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
