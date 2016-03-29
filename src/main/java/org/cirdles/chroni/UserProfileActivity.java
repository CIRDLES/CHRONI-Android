package org.cirdles.chroni;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.Header;
import org.xml.sax.SAXException;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This class is used to collect the information from a new user.
 */
public class UserProfileActivity extends Activity {

    private EditText geochronUsernameInput, geochronPasswordInput;
    private TextView validationText;

    private String geochronUsername, geochronPassword; // the login values on file

    public static final String USER_PREFS = "My CIRDLES Settings";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(android.R.style.Theme_Holo);
        setContentView(R.layout.user_profile);

        //Places background image on layout due to theme overriding
        RelativeLayout layout =(RelativeLayout)findViewById(R.id.userProfileBackground);
        layout.setBackground(getResources().getDrawable(R.drawable.background));

        validationText = (TextView) findViewById(R.id.validationText);

        Button profileValidateButton = (Button) findViewById(R.id.profileValidateButton);
        profileValidateButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // gets the text that has been entered
                final String usernameEntered = geochronUsernameInput.getText().toString();
                final String passwordEntered = geochronPasswordInput.getText().toString();

                // Displays error message if username is missing
                if (usernameEntered.length() == 0) {
                    Toast.makeText(UserProfileActivity.this, "Please enter your Geochron username.", Toast.LENGTH_LONG).show();
                }
                // Displays error message if password is missing
                else if (passwordEntered.length() == 0) {
                    Toast.makeText(UserProfileActivity.this, "Please enter your Geochron password.", Toast.LENGTH_LONG).show();
                }

                else {  // both fields have been entered, are NOT empty

                    // Checks internet connection before getting credential input
                    ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo mobileWifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                    if (mobileWifi.isConnected()) {

                        // Attempts to validate GeoChron credentials if input is stored
                        try {
                            validateGeochronCredentials(usernameEntered, passwordEntered);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(UserProfileActivity.this, "Connection error", Toast.LENGTH_LONG).show();
                        }

                    } else {  // if not on WiFi, alert user and ask to continue

                        new AlertDialog.Builder(UserProfileActivity.this).setMessage("You are not connected to WiFi, mobile data rates may apply. " +
                                "Do you wish to continue?")
                                // if user selects yes, continue with validation
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                        // Attempts to validate GeoChron credentials if input is stored
                                        try {
                                            // validates credentials if not empty
                                            validateGeochronCredentials(usernameEntered, passwordEntered);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            Toast.makeText(UserProfileActivity.this, "Connection error", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                })
                                // if user selects no, just go back
                                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                    }
                                })
                                .show();

                    }
                }

            }
        });

        Button profileMenuButton = (Button) findViewById(R.id.fileBrowserHomeButton);
        profileMenuButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();   // Exits out of Profile/Credentials Page
            }
        });

        // Clears all profile information
        Button profileEraseButton = (Button) findViewById(R.id.profileEraseButton);
        profileEraseButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(geochronUsernameInput.getText().length() != 0 || geochronPasswordInput.getText().length() != 0){

                    SharedPreferences settings = getSharedPreferences(USER_PREFS, 0);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.clear(); // Clears previously stored prefs
                    editor.apply();

                    geochronUsernameInput.setText("");
                    geochronPasswordInput.setText("");
                    validationText.setText("");

                    Toast.makeText(UserProfileActivity.this, "Credentials erased!", Toast.LENGTH_LONG).show();
                }
            }
        });

        // Initializes profile information with currently stored profile or, if
        // no one has been registered, sets as empty
        retrieveCredentials(); // Checks to see if there is a profile stored
        geochronUsernameInput = (EditText) findViewById(R.id.geochronUsername);
        geochronPasswordInput = (EditText) findViewById(R.id.geochronPassword);

        if (!getGeochronUsername().contentEquals("None") && !getGeochronPassword().contentEquals("None")) {
            geochronUsernameInput.setText(getGeochronUsername());
            geochronPasswordInput.setText(getGeochronPassword());
        }

    }

    /**
     * Retrieves the currently stored username and password
     * If none present, returns None
     */
    private void retrieveCredentials() {
        SharedPreferences settings = getSharedPreferences(USER_PREFS, 0);
        setGeochronUsername(settings.getString("Geochron Username", "None"));
        setGeochronPassword(settings.getString("Geochron Password", "None"));
    }
    
    /**
    * Validates currently stored geochron credentials 
    * From U-Pb Redux's ReduxPersistantState.class
    * http://www.geochronportal.org/post_to_credentials_service.html
    */
	public void validateGeochronCredentials(final String username, final String password) {

        validationText.setText("");
		String geochronCredentialsService = "https://app.geosamples.org/webservices/credentials_service.php";

		// Specify the information to be sent with the AsyncHttpClient
		RequestParams params = new RequestParams(); 
		params.put("username", username);
		params.put("password", password);

		UserVerificationClient.post(geochronCredentialsService, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                File fileOut = HTTP_PostAndResponse(responseBody);
                boolean valid = false;

                if (fileOut != null) {
                    org.w3c.dom.Document doc = ConvertXMLTextToDOMdocument(fileOut);

                    if (doc != null) {
                        if (doc.getElementsByTagName("valid").getLength() > 0) {
                            valid = doc.getElementsByTagName("valid").item(0)
                                    .getTextContent().trim().equalsIgnoreCase("yes");
                        }
                    }
                    if (valid) {
                        validationText.setText("Your Geochron Portal credentials are VALID!");
                        validationText.setTextColor(Color.parseColor("#00D000"));

                        // stores the credentials in settings
                        SharedPreferences settings = getSharedPreferences(USER_PREFS, 0);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString("Geochron Username", username);
                        editor.putString("Geochron Password", password);
                        editor.apply();
                        geochronUsername = username;
                        geochronPassword = password;

                    } else {
                        validationText.setText("Your Geochron Portal credentials are INVALID!");
                        validationText.setTextColor(Color.RED);
                    }
                } else {
                    Toast.makeText(
                            UserProfileActivity.this,
                            "Credentials Server cannot be located.\n", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(UserProfileActivity.this,
                        "Error " + statusCode, Toast.LENGTH_LONG)
                        .show();
                error.printStackTrace();
            }

        });

	}


   public static File HTTP_PostAndResponse (byte[] data) {
       File fileOut = new File(Environment.getExternalStorageDirectory() + "/CHRONI/Profile Information.xml");

       try {
           // Read Response
    	   ByteArrayInputStream in = new ByteArrayInputStream(data);

           FileOutputStream streamOut = new FileOutputStream( fileOut );
           int x;
           while ((x = in.read()) != -1) {
               char ch = (char)x;
        	   streamOut.write( ch );
           }
           in.close();

           streamOut.flush();
           streamOut.close();

       } catch (Exception e) {
           e.printStackTrace();
       }

       return fileOut;
   }

   public static org.w3c.dom.Document ConvertXMLTextToDOMdocument (File XMLfile) {
       org.w3c.dom.Document doc = null;

       // Parses an XML file and returns a DOM document.
       // If validating is true, the contents is validated against the DTD
       // specified in the file.
       try {
           // Create a builder factory
           DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
           factory.setValidating(false);

           // Create the builder and parse the file
           doc = factory.newDocumentBuilder().parse(XMLfile);
       } catch (SAXException e) {
           e.printStackTrace();
       } catch (ParserConfigurationException e) {
           e.printStackTrace();
       } catch (IOException e) {
           e.printStackTrace();
       }

       return doc;
   }

    public String getGeochronUsername() {
	    return geochronUsername;
    }

    public void setGeochronUsername(String name) {
	    this.geochronUsername = name;
    }

    public String getGeochronPassword() {
	    return geochronPassword;
    }

    public void setGeochronPassword(String pass) {
	    this.geochronPassword = pass;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    /**
     * The purpose of overriding this method is to alter/delete some of the menu items from the default
     * menu, as they are not wanted in this Activity. Doing so prevents the unnecessary stacking of
     * Activities by making the user follow the intended flow of Activities in the application.
     *
     * @param menu the menu that has been inflated in the Activity
     * @return true so that the menu is displayed
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // removes the History item from the menu
        MenuItem historyItem = menu.findItem(R.id.historyMenu);
        historyItem.setVisible(false);

        // removes the Edit Credentials item from the menu
        MenuItem credentialsItem = menu.findItem(R.id.editProfileMenu);
        credentialsItem.setVisible(false);

        MenuItem viewFiles = menu.findItem(R.id.viewFilesMenu);
        viewFiles.setVisible(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handles menu item selection
        switch (item.getItemId()) {
            case R.id.returnToMenu: // Takes user to main menu by finishing the Activity
                finish();
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
            case R.id.importFilesMenu:  // Takes user to import files menu
                Intent importFiles = new Intent(
                        "android.intent.action.IMPORTFILES");
                startActivity(importFiles);
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
                // Checks internet connection before downloading files
                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo mobileWifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

                if (mobileWifi.isConnected()) {
                    Intent openHelpBlog = new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://cirdles.org/projects/et_redux/#what-is-geopass"));
                    startActivity(openHelpBlog);

                } else {
                    new AlertDialog.Builder(this).setMessage("You are not connected to WiFi, mobile data rates may apply. " +
                            "Do you wish to continue?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Intent openHelpBlog = new Intent(Intent.ACTION_VIEW,
                                            Uri.parse(getString(R.string.chroni_help_address)));
                                    startActivity(openHelpBlog);
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            })
                            .show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}