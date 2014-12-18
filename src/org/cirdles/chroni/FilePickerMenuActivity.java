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

import java.io.File;

public class FilePickerMenuActivity extends Activity {

    private Button chroniDirectoryButton, deviceDirectoryButtonButton;
    private String directoryType; // the type of directory to be initialized based on the menu activity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	// Sets up layout
	super.onCreate(savedInstanceState);
	setTheme(android.R.style.Theme_Holo);
	setContentView(R.layout.file_browser_menu);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);

        //Sets the initial directory based on what file user is looking for (Aliquot or Report Settings)
        if(getIntent().hasExtra("Directory_Type")){
            if(getIntent().getStringExtra("Directory_Type").contentEquals("Aliquot")){
                setDirectoryType("Aliquot");
            }else if(getIntent().getStringExtra("Directory_Type").contentEquals("Report Settings")){
                setDirectoryType("Report_Settings");
            }
        }

    chroniDirectoryButton = (Button) findViewById(R.id.chroniDirectoryButton);
    chroniDirectoryButton.setOnClickListener(new View.OnClickListener() {
	    public void onClick(View v) {
            Intent openFilePicker = new Intent(
                    "android.intent.action.FILEPICKER");
            openFilePicker.putExtra("Default_Directory", getDirectoryType() + "_CHRONI_Directory");
            startActivity(openFilePicker);
	    }
	});

        deviceDirectoryButtonButton = (Button) findViewById(R.id.deviceDirectoryButton);
        deviceDirectoryButtonButton.setOnClickListener(new View.OnClickListener() {
	    public void onClick(View v) {
            Intent openFilePicker = new Intent(
                    "android.intent.action.FILEPICKER");
            openFilePicker.putExtra("Default_Directory", getDirectoryType() + "_Device_Directory");
            startActivity(openFilePicker);
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
            case R.id.viewAliquotsMenu:
                Intent openAliquotFiles = new Intent(
                        "android.intent.action.FILEPICKER");
                openAliquotFiles.putExtra("Default_Directory",
                        "Aliquot");
                startActivity(openAliquotFiles);
                return true;
            case R.id.viewReportSettingsMenu:
                Intent openReportSettingsFiles = new Intent(
                        "android.intent.action.FILEPICKER");
                openReportSettingsFiles.putExtra("Default_Directory",
                        "Report Settings");
                startActivity(openReportSettingsFiles);
                return true;
//            case R.id.aboutScreen:
//                Intent openAboutScreen = new Intent(
//                        "android.intent.action.ABOUT");
//                startActivity(openAboutScreen);
//                return true;
//            case R.id.helpMenu:
//                Intent openHelpBlog = new Intent(Intent.ACTION_VIEW,
//                        Uri.parse("http://chronihelpblog.wordpress.com"));
//                startActivity(openHelpBlog);
//                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public String getDirectoryType() {
        return directoryType;
    }

    public void setDirectoryType(String directoryType) {
        this.directoryType = directoryType;
    }
}