package org.cirdles.chroni;

/*
 * This class is used to collect the information from a new user.
 */

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.Header;
import org.apache.http.client.HttpResponseException;
import org.xml.sax.SAXException;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class UserProfileActivity extends Activity {

    private EditText geochronUsernameInput, geochronPasswordInput;
    private TextView validationText;
    private Button profileMenuButton, profileValidateButton, profileEraseButton;
    
    private String geochronUsername, geochronPassword; // the login values on file
    private boolean isValidated = false; // the current status of user profile credentials

	public static final String USER_REGISTERED = "My CIRDLES Application";
    public static final String USER_PREFS = "My CIRDLES Settings";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
	setTheme(android.R.style.Theme_Holo);
	setContentView(R.layout.user_profile);

    //Places background image on layout due to theme overriding
    RelativeLayout layout =(RelativeLayout)findViewById(R.id.userProfileBackground);
    layout.setBackground(getResources().getDrawable(R.drawable.background));

	validationText = (TextView) findViewById(R.id.validationText);
	
	profileValidateButton = (Button) findViewById(R.id.profileValidateButton);
	profileValidateButton.setOnClickListener(new View.OnClickListener() {
	    public void onClick(View v) {
            // Displays error message if username is missing
            if (geochronUsernameInput.getText().length() == 0) {
                Toast.makeText(UserProfileActivity.this, "Please enter your Geochron username.", Toast.LENGTH_LONG).show();
            }
            // Displays error message if password is missing
            if (geochronPasswordInput.getText().length() == 0) {
                Toast.makeText(UserProfileActivity.this, "Please enter your Geochron password.", Toast.LENGTH_LONG).show();
            }
            // Displays error message if all input is missing
            if (geochronUsernameInput.getText().length() == 0 && geochronPasswordInput.getText().length() == 0) {
                Toast.makeText(UserProfileActivity.this, "Please enter your Geochron credentials.", Toast.LENGTH_LONG).show();
            }

            if (geochronUsernameInput.getText().length() != 0 && geochronPasswordInput.getText().length() != 0) {
                // Stores the login information in shared preferences for a new user if both fields contain input
                SharedPreferences settings = getSharedPreferences(USER_PREFS, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.clear(); // Clears previously stored prefs
                editor.putString("Geochron Username", geochronUsernameInput
                        .getText().toString());
                editor.putString("Geochron Password", geochronPasswordInput
                        .getText().toString());
                editor.commit();

                // Provides feedback that credentials have been saved
                Toast.makeText(UserProfileActivity.this, "Your Geochron Profile information is saved!", Toast.LENGTH_SHORT).show();

                // Checks internet connection before getting credential input
                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo mobileWifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                if (mobileWifi.isConnected()) {
                    // Attempts to validate GeoChron credentials if input is stored
                    Toast.makeText(UserProfileActivity.this, "Validating Credentials...", Toast.LENGTH_SHORT).show();
                    retrieveCredentials(); // Fetches the credentials

                    if (!getGeochronUsername().contentEquals("None") && !getGeochronPassword().contentEquals("None")) {
                        try {
                            // validates credentials if not empty
                            validateGeochronCredentials(getGeochronUsername(), getGeochronPassword());
                        } catch (HttpResponseException e) {
                            e.printStackTrace();
                            Toast.makeText(UserProfileActivity.this, "Connection error", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(UserProfileActivity.this, "Credentials not stored", Toast.LENGTH_LONG).show();
                    }
                } else {
                    //Handles lack of wifi connection
                    Toast.makeText(UserProfileActivity.this, "Please check your internet connection before attempting to validate.", Toast.LENGTH_LONG).show();
                }
            }
        }
	});

	profileMenuButton = (Button) findViewById(R.id.fileBrowserHomeButton);
	profileMenuButton.setOnClickListener(new View.OnClickListener() {
	    public void onClick(View v) {
		Intent openMainMenu = new Intent(
			"android.intent.action.MAINMENU");
		startActivity(openMainMenu);
	    }
	});
	
	/*
	 * Clears all profile information
	 */
	profileEraseButton = (Button) findViewById(R.id.profileEraseButton);
	profileEraseButton.setOnClickListener(new View.OnClickListener() {
	    public void onClick(View v) {
	    	if(geochronUsernameInput.getText().length() != 0 || geochronPasswordInput.getText().length() != 0){

			SharedPreferences settings = getSharedPreferences(USER_PREFS, 0);
			SharedPreferences.Editor editor = settings.edit();
			editor.clear(); // Clears previously stored prefs
			
			geochronUsernameInput.setText("");
			geochronPasswordInput.setText("");
			
    		Toast.makeText(UserProfileActivity.this, "Credentials erased!", Toast.LENGTH_LONG).show();
	    	}
	    }
	 	});
			
	// Initializes profile information with currently stored profile or, if
	// no one has been registered, sets as empty
	retrieveCredentials(); // Checks to see if there is a profile stored
	geochronUsernameInput = (EditText) findViewById(R.id.geochronUsername);
	geochronPasswordInput = (EditText) findViewById(R.id.geochronPassword);

	if (!getGeochronUsername().contentEquals("None")
		&& !getGeochronPassword().contentEquals("None")) {
	    geochronUsernameInput.setText(getGeochronUsername());
	    geochronPasswordInput.setText(getGeochronPassword());
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
    
    /**
    * Validates currently stored geochron credentials 
    * From U-Pb Redux's ReduxPersistantState.class
    * http://www.geochronportal.org/post_to_credentials_service.html
    * 
    * @param username
    * @param password
    * @return
    */
	public void validateGeochronCredentials(String username, String password) throws HttpResponseException {
	    boolean isValid = false; // Geochron Credential boolean
		String geochronCredentialsService = "http://www.geochronportal.org/credentials_service.php";

		// Specify the information to be sent with the AsyncHttpClient
		RequestParams params = new RequestParams(); 
		params.put("username", username);
		params.put("password", password);

		UserVerificationClient.post(geochronCredentialsService, params,
				new AsyncHttpResponseHandler() {
					@Override
					public void onSuccess(int statusCode, Header[] headers,
							byte[] responseBody) {
						File fileOut = HTTP_PostAndResponse(responseBody);
						boolean valid = false;

						if (fileOut != null) {
							org.w3c.dom.Document doc = ConvertXMLTextToDOMdocument(fileOut);

							if (doc != null) {
								if (doc.getElementsByTagName("valid").getLength() > 0) {
									valid = doc.getElementsByTagName("valid").item(0)
											.getTextContent().trim().equalsIgnoreCase("yes");
									setValidated(valid);
								}
							}
							if (valid) {
					    		validationText.setText("Your Geochron Portal credentials are valid!");

							} else {
					    		validationText.setText("Your Geochron Portal credentials are not valid!");

							}
						} else {
							Toast.makeText(
									UserProfileActivity.this,
									"Credentials Server cannot be located.\n", Toast.LENGTH_LONG).show();
						}
					}

					@Override
					public void onFailure(int statusCode, Header[] headers,
							byte[] responseBody, Throwable error) {
						Toast.makeText(UserProfileActivity.this,
								"Error " + statusCode, Toast.LENGTH_LONG)
								.show();
					}
				});
	}

    
   /*
    * 
    * @param serviceURI
    * @param data
    * @return
    */
   public static File HTTP_PostAndResponse ( byte[] data ) {
       File fileOut = new File(Environment.getExternalStorageDirectory() + "/CHRONI/Profile Information.xml");
//       fileOut.delete();

       try {
           // Read Response
    	   ByteArrayInputStream in = new ByteArrayInputStream(data);

           FileOutputStream streamOut = new FileOutputStream( fileOut );
           int x;
           while ((x = in.read()) != -1) {
               char ch = (char)x;
        	   streamOut.write( ch );
           }
           if(in != null){
        	   in.close();
           }
           if (streamOut != null){
        	   streamOut.flush();
        	   streamOut.close();
           }
           
       } catch (Exception e) {
    	   String err="Error: " + e.getMessage();
    	   Log.e("UserProfileActivity.this", err);
    	   return null;
       }

       return fileOut;
   }

   /**
    * 
    * @param XMLfile
    * @return
    */
   public static org.w3c.dom.Document ConvertXMLTextToDOMdocument ( File XMLfile ) {
       org.w3c.dom.Document doc = null;

       // Parses an XML file and returns a DOM document.
       // If validating is true, the contents is validated against the DTD
       // specified in the file.
       try {
           // Create a builder factory
           DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
           factory.setValidating( false );

           // Create the builder and parse the file
           doc = factory.newDocumentBuilder().parse( XMLfile );
       } catch (SAXException e) {
//           JOptionPane.showMessageDialog( null,
//                   new String[]{"Document error: "//
//                       + e.getMessage()} );
       } catch (ParserConfigurationException e) {
//           JOptionPane.showMessageDialog( null,
//                   new String[]{"Parsing error: "//
//                       + e.getMessage()} );
       } catch (IOException e) {
//           JOptionPane.showMessageDialog( null,
//                   new String[]{"File error: "//
//                       + e.getMessage()} );
       }


       return doc;
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

    public boolean isValidated() {
		return isValidated;
	}

	public void setValidated(boolean isValidated) {
		this.isValidated = isValidated;
	}
    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onStop()
     */
    @Override
    protected void onStop() {
	super.onStop();

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

}