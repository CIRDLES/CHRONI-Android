package org.cirdles.chroni;

import com.example.cirdles.R;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class ReportSettingsMenu extends Activity {
	private Button reportSettingsFileSelectButton, reportSettingsOpenButton, reportSettingsUrlButton;
	private EditText reportSettingsFileSelectText, reportSettingsUrlText;
	private String selectedReportSettings; // name of Report Settings file that has been chosen for viewing
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTheme(android.R.style.Theme_Holo);
		setContentView(R.layout.report_settings_select);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Information about Report Settings file
        reportSettingsFileSelectButton = (Button) findViewById(R.id.reportSettingsFileSelectButton);
        reportSettingsFileSelectButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				Intent openFilePicker = new Intent("android.intent.action.FILEPICKER");
 				openFilePicker.putExtra("Default_Directory", "Report Settings");
 				startActivity(openFilePicker);
 		    	}
 			});	

        reportSettingsOpenButton = (Button) findViewById(R.id.reportSettingsFileOpenButton);
        reportSettingsOpenButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 		    	Intent openMainMenu = new Intent("android.intent.action.DISPLAY");
 		    	startActivity(openMainMenu);		    		
 		    	}
 			});	
 		
        reportSettingsFileSelectText = (EditText) findViewById(R.id.reportSettingsFileSelectText);		
 		if(getIntent().hasExtra("ReportSettingsXMLFileName")){
 			selectedReportSettings = getIntent().getStringExtra("ReportSettingsXMLFileName");
			String[] absoluteFileName = selectedReportSettings.split("/");
			String fileName = absoluteFileName[absoluteFileName.length - 1];
 			reportSettingsFileSelectText.setText(fileName);
 		}

 	// Information about Report Settings URL
 		reportSettingsUrlText = (EditText) findViewById(R.id.reportSettingsUrlText);
 		
 		reportSettingsUrlButton = (Button) findViewById(R.id.reportSettingsUrlButton);
 		reportSettingsUrlButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
// 				aliquotURL = aliquotURLText.getText().toString();

 				// Downloads Aliquot file from URL
// 				URLFileReader downloader = new URLFileReader(AliquotMenu.this, "AliquotMenu", aliquotURL, "url");	
 				Intent openMainMenu = new Intent("android.intent.action.DISPLAY");
// 				openMainMenu.putExtra("Url", aliquotURL);
 		    	startActivity(openMainMenu);	
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
