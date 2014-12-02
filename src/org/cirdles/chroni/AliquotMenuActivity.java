package org.cirdles.chroni;

import java.io.File;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class AliquotMenuActivity extends Activity {

    private Button aliquotFileSelectButton, aliquotFileSubmitButton,
	    aliquotIGSNSubmitButton, aliquotURLButton;
    private EditText aliquotFileSelectText, aliquotIGSNText, aliquotURLText;

    private String selectedAliquot, aliquotIGSN, aliquotURL, aliquotLocation, aliquot; // the Aliquot values
	private static String absoluteFilePath; // the path of the Aliquot file
    public static boolean aliquotFound;
    private String finalAliquotFileName; // the user specififed filename
    private String[][] finalTable; // the table created from parsing the two files
    private boolean invalidFile = false; // true if file attempted to be downloaded is invalid
    private static boolean privateFile = false;
    private static String geochronUsername, geochronPassword; // the geochron information on file for the user
    public static final String USER_PREFS = "My CIRDLES Settings"; // code to access stored preferences

    // Base URLs for IGSN downloads
    public static String BASE_ALIQUOT_URI = "http://www.geochronportal.org/getxml.php?igsn=";
    // public static String BASE_ALIQUOT_URI = "http://picasso.kgs.ku.edu/geochron/getxml.php?igsn=";
    public static String BASE_SAMPLE_URI = "http://www.geosamples.org/display.php?igsn=";

    private static final int REQUEST_PICK_FILE = 1;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setTheme(android.R.style.Theme_Holo);
	setContentView(R.layout.aliquot_select);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);

    //Places background image on layout due to theme overriding
    RelativeLayout layout =(RelativeLayout)findViewById(R.id.aliquotSelectBackground);
    layout.setBackground(getResources().getDrawable(R.drawable.background));

	// Directories needed for file locations
	final File chroniDirectory = getDir("CHRONI", Context.MODE_PRIVATE);
	final File aliquotDirectory = new File(chroniDirectory, "Aliquot");

	// Information about Aliquot File
	aliquotFileSelectButton = (Button) findViewById(R.id.aliquotFileSelectButton);
	aliquotFileSelectButton.setOnClickListener(new View.OnClickListener() {
	    public void onClick(View v) {
		Intent openFilePicker = new Intent(
			"android.intent.action.FILEPICKER");
		openFilePicker.putExtra("Default_Directory", "Aliquot");
		startActivity(openFilePicker);
	    }
	});

   aliquotFileSelectText = (EditText) findViewById(R.id.aliquotFileSelectText);
    /*
    Will get selected file and place the file name on the file select input line
     */
	if (getIntent().hasExtra("AliquotXMLFileName")) {
	    selectedAliquot = getIntent().getStringExtra("AliquotXMLFileName");
	    String[] filePath = selectedAliquot.split("/");
	    String fileName = filePath[filePath.length - 1];
	    aliquotFileSelectText.setText(fileName);
	}

	aliquotFileSubmitButton = (Button) findViewById(R.id.aliquotFileSubmitButton);
	aliquotFileSubmitButton.setOnClickListener(new View.OnClickListener() {
	    public void onClick(View v) {
		if (aliquotFileSelectText.getText().length() != 0) {
            Toast.makeText(AliquotMenuActivity.this, "Opening table...", Toast.LENGTH_LONG).show();
		    Intent openMainMenu = new Intent(
			    "android.intent.action.DISPLAY");
		    openMainMenu.putExtra("AliquotXML", getIntent()
			    .getStringExtra("AliquotXMLFileName"));
		    startActivity(openMainMenu);
		}
	    }
	});

	// Information about Aliquot IGSN
	aliquotIGSNText = (EditText) findViewById(R.id.aliquotIGSNText);
    // Checks to see if user profile information has been authenticated for private file access
    retrieveCredentials();
	if (!getGeochronUsername().contentEquals("None")&& !getGeochronPassword().contentEquals("None")) {
		aliquotIGSNText.setHint("Profile information stored. Private files enabled!");
	}else{
		aliquotIGSNText.setHint("No profile information stored. Private files disabled.");
	}

	aliquotIGSNSubmitButton = (Button) findViewById(R.id.aliquotIGSNSubmitButton);
	aliquotIGSNSubmitButton.setText("Download");
	aliquotIGSNSubmitButton.setOnClickListener(new View.OnClickListener() {
	    public void onClick(View v) {
            // Checks internet connection before downloading files
            ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

            if (mWifi.isConnected()) {
		if (aliquotIGSNText.getText().length() != 0) {
            aliquotIGSN = aliquotIGSNText.getText().toString().toUpperCase().trim();
            // Downloads Aliquot file
            final String aliquotURL = makeURI(BASE_ALIQUOT_URI, aliquotIGSN);
            Toast.makeText(AliquotMenuActivity.this, "Downloading Aliquot...", Toast.LENGTH_LONG).show();
            URLFileReader downloader = new URLFileReader(AliquotMenuActivity.this, "AliquotMenu", makeURI(BASE_ALIQUOT_URI, aliquotIGSN), "igsn");

            setAbsoluteFilePath(aliquotDirectory + "/" + aliquotIGSN + ".xml");

        	}
		}else{
                //Handles lack of wifi connection
                Toast.makeText(AliquotMenuActivity.this, "Please check your internet connection before performing this action.", Toast.LENGTH_LONG).show();
            }
        }

	    });

	// Information about Aliquot URL
	aliquotURLText = (EditText) findViewById(R.id.aliquotURLText);

	aliquotURLButton = (Button) findViewById(R.id.aliquotURLButton);
	aliquotURLButton.setText("Download");
	aliquotURLButton.setOnClickListener(new View.OnClickListener() {
	    public void onClick(View v) {
            // Checks internet connection before downloading files
            ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

            if (mWifi.isConnected()) {
		        if (aliquotURLText.getText().length() != 0) {
                    aliquotURL = aliquotURLText.getText().toString().trim();
                    // Downloads Aliquot file from URL

                    requestFileName(); // Prompts the user for a file name and sets it as the final file name and begins download

                     }
            }else{
                //Handles lack of wifi connection
                Toast.makeText(AliquotMenuActivity.this, "Please check your internet connection before performing this action.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /*
    Currently requests file name from user and then proceeds to download based on input
     */
        public void requestFileName(){

            // Directories needed for file locations
            final File chroniDirectory = getDir("CHRONI", Context.MODE_PRIVATE);
            final File aliquotDirectory = new File(chroniDirectory, "Aliquot");

            AlertDialog.Builder userFileNameAlert = new AlertDialog.Builder(AliquotMenuActivity.this);

                userFileNameAlert.setTitle("Choose a file name");
                userFileNameAlert.setMessage("Enter the desired name of your Aliquot URL file:");

                // Set an EditText view to get user input
                final EditText input = new EditText(AliquotMenuActivity.this);
                userFileNameAlert.setView(input);

                userFileNameAlert.setPositiveButton("Start Download!", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if (input.getText().toString().length() != 0) {
                            setFinalAliquotFileName(input.getText().toString()); // sets the user file name in the class
                            URLFileReader downloader = new URLFileReader(
                                    AliquotMenuActivity.this, "AliquotMenu",
                                    aliquotURL, "url", getFinalAliquotFileName()); // Downloads the file and sets user name
                            setAbsoluteFilePath(aliquotDirectory + "/" + getFinalAliquotFileName() + ".xml");
                            Toast.makeText(AliquotMenuActivity.this, "Downloading Aliquot...", Toast.LENGTH_LONG).show();
                        }
                    }
                });

                userFileNameAlert.setNegativeButton("Cancel Download", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Canceled.
                    }
                });

                userFileNameAlert.show();
            }

    /*
	 * Creates file name based on the file's type and URL
	 */
	protected String createFileName(String downloadMethod, String fileUrl) {
		String name = null;
			// If downloading based on IGSN URL, just use IGSN for name
			if(downloadMethod.contains("igsn")){
			String[] URL = fileUrl.split("igsn=");
			name = URL[1];
			if(name.contains("&username=")){
				// Makes an additional split to remove the username and password query from the file name
				String[] url2 = name.split("&username=");
				name = url2[0];
			}

			}
		return name;
	}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	if (resultCode == RESULT_OK) {
	    switch (requestCode) {
	    case REQUEST_PICK_FILE:
		if (data.hasExtra(FilePickerActivity.EXTRA_FILE_PATH)) {
		    // Get the file path
		    File f = new File(
			    data.getStringExtra(FilePickerActivity.EXTRA_FILE_PATH));

		    aliquotLocation = f.getPath();
		    aliquotFound = true;

		    String[] aliquotName = aliquotLocation.toString()
			    .split("/");
		    aliquot = aliquotName[aliquotName.length - 1];

		    // Set the file path text view
		    aliquotFileSelectText.setText(aliquot);
		}
	    }
	}
    }
    
    /*
     * Retrieves the currently stored username and password
     * If none present, returns None
     */
    private void retrieveCredentials() {
	SharedPreferences settings = getSharedPreferences(USER_PREFS, 0);
	setGeochronUsername(settings.getString("Geochron Username", "None"));
	setGeochronPassword(settings.getString("Geochron Password", "None"));
    }
    
    /*
     * Creates URL from the constant geochron URL and IGSN
     */
    public final static String makeURI(String baseURL, String IGSN) {
	String URI = baseURL + IGSN;
	// Will create unique URL and password if credentials stored
//    retrieveCredentials();
	if (!getGeochronUsername().contentEquals("None")&& !getGeochronPassword().contentEquals("None")) {
		URI += "&username="+ getGeochronUsername()+"&password="+ getGeochronPassword();
	 }
	return URI;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
	// Inflate the menu; this adds items to the action bar if it is present.
	getMenuInflater().inflate(R.menu.menu, menu);
	return true;
    }

    public boolean isInvalidFile() {
	return invalidFile;
    }

    public static void setInvalidFile(boolean invalidFile) {
	invalidFile = invalidFile;
    }

    public static String getGeochronUsername() {
		return geochronUsername;
	}

	public void setGeochronUsername(String geochronUsername) {
		this.geochronUsername = geochronUsername;
	}

	public static String getGeochronPassword() {
		return geochronPassword;
	}

	public void setGeochronPassword(String geochronPassword) {
		this.geochronPassword = geochronPassword;
	}

	public String getAbsoluteFilePath() {
		return absoluteFilePath;
	}

	public void setAbsoluteFilePath(String absoluteFileName) {
		this.absoluteFilePath = absoluteFileName;
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public String getFinalAliquotFileName() {
        return finalAliquotFileName;
    }

    public void setFinalAliquotFileName(String finalAliquotFileName) {
        this.finalAliquotFileName = finalAliquotFileName;
    }
}
