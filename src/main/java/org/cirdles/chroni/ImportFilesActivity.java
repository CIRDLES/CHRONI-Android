/*
 * Copyright 2016 CIRDLES.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cirdles.chroni;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.w3c.dom.Document;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class ImportFilesActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(android.R.style.Theme_Holo);
        setContentView(R.layout.import_files);
    }

    /**
     * When the + button is clicked, the File Picker will be opened at the default directory
     *
     * @param v the Button that was pressed
     */
    public void filePickerButtonPressed(View v) {
        Intent openFilePicker = new Intent("android.intent.action.FILEPICKER");
        openFilePicker.putExtra("Default_Directory", "Parent_Directory");   // specifying CHRONI's parent directory (default)

        // Opens FilePicker and waits for it to return an Extra (SEE onActivityResult())
        startActivityForResult(openFilePicker, 1);
    }

    /**
     * This method goes to the specified directory, finds all XML files that adhere to both Aliquot and Report Settings
     * specifications, and imports each one to their correct directory (CHRONI/Aliquot or CHRONI/Report Settings).
     *
     * @param v the Button that was pressed
     */
    public void importButtonPressed(View v) {
        if (getIntent().hasExtra("ImportDirectory")) {
            String externalDirectory = Environment.getExternalStorageDirectory().getAbsolutePath();

            // obtains the file at the specified path
            File importingDirectory = new File(getIntent().getStringExtra("ImportDirectory"));

            // makes sure the specified directory IS indeed a directory AND it isn't the Aliquot or Report Settings directory
            if (importingDirectory.isDirectory()
                    && !importingDirectory.getAbsolutePath().equals(externalDirectory + "/CHRONI/Aliquot")
                    && !importingDirectory.getAbsolutePath().equals(externalDirectory + "/CHRONI/Report Settings")) {

                boolean allFilesMoved = true;   // will tell if all valid files have been successfully moved

                String successToastText = "";   // will be displayed upon success

                // remembers the number of each XML file type to be used in the successToastText later
                int numAliquots = 0;
                int numReportSettings = 0;

                // iterates through all files in the directory
                for (File file : importingDirectory.listFiles()) {
                    if (file.isFile()) {    // the file found is a file, not a directory

                        // moves the file IF it is a valid Aliquot or Report Settings file
                        String fileType = validateFile(file);
                        if (fileType.equals("Aliquot")) {
                            // moves the file to the Aliquot directory
                            boolean success = cutAndPasteFile(file,
                                    new File(Environment.getExternalStorageDirectory() + "/CHRONI/Aliquot"));

                            allFilesMoved = allFilesMoved && success;   // remembers if it is successful

                            if (success)    // accounts for the moved Aliquot file if successful
                                numAliquots++;

                        } else if (fileType.equals("ReportSettings")) {
                            // moves the file to the Aliquot directory
                            boolean success = cutAndPasteFile(file,
                                    new File(Environment.getExternalStorageDirectory() + "/CHRONI/Report Settings"));

                            allFilesMoved = allFilesMoved && success;   // remembers if it is successful

                            if (success)    // accounts for the moved Report Settings file if successful
                                numReportSettings++;
                        }

                    }
                }

                if (allFilesMoved) {
                    // creates the correct Toast statement
                    if (numAliquots <= 0 && numReportSettings <= 0)
                        successToastText += "No Aliquots or Report Settings files were moved.";
                    else {
                        successToastText += "SUCCESS! ";
                        boolean onlyOneTypeFound = numAliquots == 0 || numReportSettings == 0;  // one of the types was not found

                        if (numAliquots > 0) {
                            successToastText += numAliquots + " Aliquot";
                            if (numAliquots > 1)    // accounts for more than 1 being found
                                successToastText += "s";
                        }

                        if (numReportSettings > 0) {
                            if (numAliquots > 0)
                                successToastText += " and";

                            successToastText += " " + numReportSettings + " Report Settings file";
                            if (numReportSettings > 1)  // accounts for more than 1 being found
                                successToastText += "s";
                        }

                        // uses the appropriate verb ("were" or "was")
                        if (!onlyOneTypeFound || numAliquots > 1 || numReportSettings > 1)
                            successToastText += " were successfully moved.";
                        else
                            successToastText += " was successfully moved.";
                    }

                    Toast.makeText(this, successToastText, Toast.LENGTH_LONG).show();

                } else
                    Toast.makeText(this, "ERROR: Some files could not be moved", Toast.LENGTH_SHORT).show();

            } else
                Toast.makeText(this, "ERROR: Please choose a valid directory", Toast.LENGTH_SHORT).show();

        } else
            Toast.makeText(this, "ERROR: Please choose a valid directory", Toast.LENGTH_SHORT).show();

        // gets the EditText view so that the text can be altered later
        EditText view = (EditText) findViewById(R.id.directoryChosenText);

        view.setText("");   // resets the EditText's text to nothing
    }

    /**
     * Copies the file given to the directory given and deletes the old file, returning a boolean stating whether
     * it was successful or not.
     *
     * @param fileCopying the file that is being copied
     * @param toDirectory the directory that the file will be copied to
     * @return a boolean telling if the copy was successful
     */
    public boolean cutAndPasteFile(File fileCopying, File toDirectory) {
        boolean result = true;  // result is true until proved false

        // first makes sure that a file is being copied to a directory
        if (fileCopying.isFile() && toDirectory.isDirectory()) {

            // copies the file through the use of Streams
            InputStream input = null;
            OutputStream output = null;

            try {
                // input file is the copied file, and output file is a new file at the specified directory
                File newFile = new File(toDirectory.getAbsolutePath() + "/" + fileCopying.getName());
                input = new FileInputStream(fileCopying);
                output = new FileOutputStream(newFile.getAbsolutePath());

                // copies the file's contents to the new directory
                byte[] buf = new byte[1024];
                int bytesRead;
                while ((bytesRead = input.read(buf)) > 0) {
                    output.write(buf, 0, bytesRead);
                }

            } catch (IOException e) {
                e.printStackTrace();
                result = false; // error occurred, could not copy the file

            } finally {
                try {
                    input.close();
                    output.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } else
            // either the file copying is not a file or toDirectory is not a directory
            result = false;

        // after file is copied successfully, deletes the old file
        if (result)
            fileCopying.delete();

        return result;
    }

    /**
     * Checks to make sure that the given file is either an Aliquot or a Report Settings file.
     *
     * @param file the file that is being validated
     * @return a String that specifies which type the file is ("Aliquot" or "Report Settings")
     * or "none" if neither
     */
    private String validateFile(File file) {
        String result = "none"; // default value is "none" until proven otherwise

        try {
            // parses the XML file
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(file);

            // gets the first node in the XML
            String nodeName = doc.getDocumentElement().getNodeName();
            System.out.println(nodeName);

            // sets result if the file is an Aliquot or Report Settings file
            if (nodeName.equals("Aliquot") || nodeName.equals("ReportSettings"))
                result = nodeName;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    @Override
    // Gets the filename that the FilePicker returns and puts it into this Intent's Extra
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if(resultCode == RESULT_OK){
                if (data.hasExtra("ImportDirectory")) {
                    String result = data.getStringExtra("ImportDirectory"); // Specified file name from file browser
                    getIntent().putExtra("ImportDirectory", result);    // put the DATA into THIS INTENT

                    String[] selectedAliquotFilePath = result.split("/");   // Splits selected file name into relevant parts
                    String directoryName = selectedAliquotFilePath[selectedAliquotFilePath.length - 1]; // Creates displayable file name from split file path
                    EditText directoryText = (EditText) findViewById(R.id.directoryChosenText);
                    directoryText.setText(directoryName);   // sets the EditText in the Activity equal to the directory's name
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.table_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // changes the Exit item to say "Back" instead
        MenuItem exitItem = menu.findItem(R.id.exitMenu);
        exitItem.setTitle("Back");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handles menu item selection
        switch (item.getItemId()) {
            case R.id.exitMenu:
                finish();
                return true;

            case R.id.helpMenu: // Takes user to help blog
                // Checks internet connection before downloading files
                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo mobileWifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

                if (mobileWifi.isConnected()) {
                    Intent openHelpBlog = new Intent(Intent.ACTION_VIEW,
                            Uri.parse(getString(R.string.chroni_help_address)));
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
