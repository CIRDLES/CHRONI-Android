package org.cirdles.chroni;

/*
 * This class is used to collect the information from a new user.
 */

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.cirdles.chroni.R.color;
import org.xml.sax.SAXException;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

public class UserProfileActivity extends Activity {

    private EditText geochronUsernameInput, geochronPasswordInput;
    private Button profileSaveButton, profileMenuButton;
    private ToggleButton profileValidateButton;
    
    private String geochronUsername, geochronPassword; // the login values on file
    private boolean isValidated = false; // the current status of user profile credentials

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
		// Stores the login information in shared preferences for a new
		// user
		SharedPreferences settings = getSharedPreferences(USER_PREFS, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.clear(); // Clears previously stored prefs
		editor.putString("Geochron Username", geochronUsernameInput
			.getText().toString());
		editor.putString("Geochron Password", geochronPasswordInput
			.getText().toString());
		editor.commit();
		Toast.makeText(UserProfileActivity.this,
			"Your Geochron Profile information is saved!", 3000)
			.show();
	    }
	});
	
	profileValidateButton = (ToggleButton) findViewById(R.id.profileValidateButton);
	profileValidateButton.setOnClickListener(new View.OnClickListener() {
	    public void onClick(View v) {
//	    	profileValidateButton.setEnabled(false);
	    	retrieveCredentials();
	    	if (!getGeochronUsername().contentEquals("None")&& !getGeochronPassword().contentEquals("None")) {
//	    		Toast.makeText(UserProfileActivity.this, "Username: " + getGeochronUsername() + " and Password: " + getGeochronPassword(), 3000).show();
	    		isValidated = validateGeochronCredentials(getGeochronUsername(), getGeochronPassword());
		    	if(isValidated){
		    		profileValidateButton.setBackgroundColor(Color.GREEN);
		    		profileValidateButton.setText("Validated!");
		    	}else{
		    		profileValidateButton.setText("Unvalidated!");
		    	}
	    	}else{
	    		Toast.makeText(UserProfileActivity.this, "Credentials not stored", 3000).show();
	    	}
	    }
	});

	profileMenuButton = (Button) findViewById(R.id.profileMenuButton);
	profileMenuButton.setOnClickListener(new View.OnClickListener() {
	    public void onClick(View v) {
		Intent openMainMenu = new Intent(
			"android.intent.action.MAINMENU");
		startActivity(openMainMenu);
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

	    // Changes save button to appropriate text and clears input if edits
	    // are made
	    // if((geochronUsernameInput.getText().toString() !=
	    // getGeochronUsername())||
	    // (geochronPasswordInput.getText().toString() !=
	    // getGeochronPassword())){
	    // profileSaveButton.setText("Edit");
	    // geochronPasswordInput.setText("");
	    // }else{
	    // profileSaveButton.setText("Save");
	    // }
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
	public boolean validateGeochronCredentials(String username,
			String password) {

		String geochronCredentialsService = "http://www.geochronportal.org/credentials_service.php";

		boolean valid = false;

		String data = null;
		try {
			// puts login data in appropriate "name=value" query format
			data = //
			URLEncoder.encode("username", "UTF-8") + "="
					+ URLEncoder.encode(username, "UTF-8");
			data += "&" + URLEncoder.encode("password", "UTF-8") + "="
					+ URLEncoder.encode(password, "UTF-8");
		} catch (UnsupportedEncodingException unsupportedEncodingException) {
		}

		File fileOut = HTTP_PostAndResponse(geochronCredentialsService, data);

		if (fileOut != null) {
			org.w3c.dom.Document doc = ConvertXMLTextToDOMdocument(fileOut);

			if (doc != null) {
				if (doc.getElementsByTagName("valid").getLength() > 0) {
					valid = doc.getElementsByTagName("valid").item(0)
							.getTextContent().trim().equalsIgnoreCase("yes");
					System.out.println("valid = " + valid);
				}
			}
			if (valid) {
				Toast.makeText(UserProfileActivity.this,
						"Geochron Credentials are VALID!", 3000).show();
			} else {
				Toast.makeText(UserProfileActivity.this,
						"Credentials NOT valid", 3000).show();
			}
		} else {
			Toast.makeText(
					UserProfileActivity.this,
					"Credentials Server " + geochronCredentialsService
							+ " cannot be located.\n", 3000).show();
		}
		return valid;
	}
    
   /**
    * 
    * @param serviceURI
    * @param data
    * @return
    */
   public static File HTTP_PostAndResponse ( String serviceURI, String data ) {
       File fileOut = new File( "HTTP_PostAndResponse_tempXML.xml" );
//       fileOut.delete();

       try {
           // Send data
           URL url = new URL( serviceURI );
           URLConnection conn = url.openConnection();
           conn.setDoOutput( true ); // Triggers POST
           conn.setDoInput( true );
//           conn.setRequestProperty("Accept-Charset", charset);
//           conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + charset); // second parameter is the standard HTTP POST for web forms
           
           DataOutputStream dstream = new DataOutputStream( conn.getOutputStream() );

           // The POST line
           dstream.writeBytes( data );
           if(dstream != null){
        	   dstream.close();
           }

           // Read Response
           InputStream in = conn.getInputStream();

           BufferedOutputStream streamOut = new BufferedOutputStream(new FileOutputStream( fileOut ));
           int x;
           while ((x = in.read()) != -1) {
               streamOut.write( x );
               //System.out.write(x);
           }
           if(in != null){
        	   in.close();
           }
           if (streamOut != null){
        	   streamOut.close();
           }
       } catch (Exception e) {
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
	case R.id.helpMenu:
	    Intent openHelpBlog = new Intent(Intent.ACTION_VIEW,
		    Uri.parse("http://joyenettles.blogspot.com"));
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