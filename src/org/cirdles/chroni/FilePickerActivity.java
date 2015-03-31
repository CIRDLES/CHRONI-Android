/*
 * Copyright 2011 Anders Kalr
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cirdles.chroni;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class FilePickerActivity extends ListActivity {
	
	/**
	 * The file path
	 */
	public final static String EXTRA_FILE_PATH = "file_path";
	
	/**
	 * Sets whether hidden files should be visible in the list or not
	 */
	public final static String EXTRA_SHOW_HIDDEN_FILES = "show_hidden_files";

	/**
	 * The allowed file extensions in an ArrayList of Strings
	 */
	public final static String EXTRA_ACCEPTED_FILE_EXTENSIONS = "accepted_file_extensions";
	
	/**
	 * The initial directory which will be used if no directory has been sent with the intent 
	 */
//	public final static String DEFAULT_INITIAL_DIRECTORY = Environment.getExternalStorageDirectory()+ "/CIRDLES/";
	
	protected File mainDirectory;
	protected ArrayList<File> mFiles;
	protected FilePickerListAdapter mAdapter;
	protected boolean mShowHiddenFiles = false;
	protected String[] acceptedFileExtensions;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTheme(android.R.style.Theme_Holo);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);

        // Set the view to be shown if the list is empty
		LayoutInflater inflator = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View emptyView = inflator.inflate(R.layout.file_picker_empty_view, null);
		((ViewGroup)getListView().getParent()).addView(emptyView);
		getListView().setEmptyView(emptyView);
		
		// Set initial directory
        mainDirectory = Environment.getExternalStorageDirectory(); // Takes user to root directory folder

        //Sets the initial directory based on what file user is looking for (Aliquot or Report Settings)
		if(getIntent().hasExtra("Default_Directory")){
            if (getIntent().getStringExtra("Default_Directory").contentEquals("Aliquot_Directory"))
            {
                mainDirectory = new File(Environment.getExternalStorageDirectory() + "/CHRONI/Aliquot"); // Takes user to the Aliquot folder
		    }else if(getIntent().getStringExtra("Default_Directory").contentEquals("Report_Settings_Directory")) {
                mainDirectory = new File(Environment.getExternalStorageDirectory() + "/CHRONI/Report Settings");
            }

        }
		
		// Initialize the ArrayList
		mFiles = new ArrayList<File>();
		
		// Set the ListAdapter
		mAdapter = new FilePickerListAdapter(this, mFiles);
		setListAdapter(mAdapter);
		
		// Initialize the extensions array to allow any file extensions
		acceptedFileExtensions = new String[] {};
		
		// Get intent extras
		if(getIntent().hasExtra(EXTRA_SHOW_HIDDEN_FILES)) {
			mShowHiddenFiles = getIntent().getBooleanExtra(EXTRA_SHOW_HIDDEN_FILES, false);
		}
		if(getIntent().hasExtra(EXTRA_ACCEPTED_FILE_EXTENSIONS)) {
			ArrayList<String> collection = getIntent().getStringArrayListExtra(EXTRA_ACCEPTED_FILE_EXTENSIONS);
			acceptedFileExtensions = (String[]) collection.toArray(new String[collection.size()]);
		}
	}
	
	@Override
	protected void onResume() {
		refreshFilesList();
		super.onResume();
	}
	
	/**
	 * Updates the list view to the current directory
	 */
	protected void refreshFilesList() {
		// Clear the files ArrayList
		mFiles.clear();
		
		// Set the extension file filter
		ExtensionFilenameFilter filter = new ExtensionFilenameFilter(acceptedFileExtensions);
		
		// Get the files in the directory
		File[] files = mainDirectory.listFiles(filter);
		if(files != null && files.length > 0) {
			for(File f : files) {
				if(f.isHidden() && !mShowHiddenFiles) {
					// Don't add the file
					continue;
				}
				
				// Add the file the ArrayAdapter
				mFiles.add(f);
			}
			
			Collections.sort(mFiles, new FileComparator());
		}
		mAdapter.notifyDataSetChanged();
	}
	
	@Override
	public void onBackPressed() {
		Intent openMainMenu = null;
        if(getIntent().hasExtra("Default_Directory")){
            if(getIntent().getStringExtra("Default_Directory").contentEquals("Aliquot_Directory")){
                openMainMenu = new Intent("android.intent.action.ALIQUOTMENU");
            }else if(getIntent().getStringExtra("Default_Directory").contentEquals("Report_Settings_Directory")){
                openMainMenu =new Intent("android.intent.action.REPORTSETTINGSMENU");
            }
        }
        startActivity(openMainMenu);
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		File newFile = (File)l.getItemAtPosition(position);

        // If item selected is a file (and not a directory), allows user to select file
		if(newFile.isFile()) {		
			// Sends back selected file name
			if(getIntent().getStringExtra("Default_Directory").contentEquals("Aliquot_Directory") || getIntent().getStringExtra("Default_Directory").contentEquals("Aliquot_CHRONI_Directory")){
		    	Intent openAliquotMenu = new Intent("android.intent.action.ALIQUOTMENU");
		    	openAliquotMenu.putExtra("AliquotXMLFileName", newFile.getAbsolutePath());
		    	startActivity(openAliquotMenu);
//                Toast.makeText(FilePickerActivity.this, "File Name: " + newFile.getAbsolutePath(), Toast.LENGTH_LONG).show();

            }else if(getIntent().getStringExtra("Default_Directory").contentEquals("Report_Settings_Directory") || getIntent().getStringExtra("Default_Directory").contentEquals("Report_Settings_CHRONI_Directory")){
				Intent openRSMenu = new Intent("android.intent.action.REPORTSETTINGSMENU");
				openRSMenu.putExtra("ReportSettingsXMLFileName", newFile.getAbsolutePath());
		    	startActivity(openRSMenu);
//                Toast.makeText(FilePickerActivity.this, "File Name: " + newFile.getAbsolutePath(), Toast.LENGTH_LONG).show();

            }
//			setResult(RESULT_OK, extra);
			
			// Finish the activity
			finish();
		} else {
			mainDirectory = newFile;
			// Update the files list
			refreshFilesList();
		}
		
		super.onListItemClick(l, v, position, id);
	}
	
	private class FilePickerListAdapter extends ArrayAdapter<File> {
		
		private List<File> mObjects;
		
		public FilePickerListAdapter(Context context, List<File> objects) {
			super(context, R.layout.file_picker_list_item, android.R.id.text1, objects);
			mObjects = objects;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			View row = null;
			
			if(convertView == null) { 
				LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				row = inflater.inflate(R.layout.file_picker_list_item, parent, false);
			} else {
				row = convertView;
			}

			File object = mObjects.get(position);

			ImageView imageView = (ImageView)row.findViewById(R.id.file_picker_image);
			TextView textView = (TextView)row.findViewById(R.id.file_picker_text);
			// Set single line
			textView.setSingleLine(true);
			
			textView.setText(object.getName());
			if(object.isFile()) {
				// Show the file icon
				imageView.setImageResource(R.drawable.cirdles_logo);
			} else {
				// Show the folder icon
				imageView.setImageResource(R.drawable.cirdles_logo);
			}
			
			return row;
		}

	}
	
	private class FileComparator implements Comparator<File> {
	    @Override
	    public int compare(File f1, File f2) {
	    	if(f1 == f2) {
	    		return 0;
	    	}
	    	if(f1.isDirectory() && f2.isFile()) {
	        	// Show directories above files
	        	return -1;
	        }
	    	if(f1.isFile() && f2.isDirectory()) {
	        	// Show files below directories
	        	return 1;
	        }
	    	// Sort the directories alphabetically
	        return f1.getName().compareToIgnoreCase(f2.getName());
	    }
	}
	
	private class ExtensionFilenameFilter implements FilenameFilter {
		private String[] mExtensions;
		
		public ExtensionFilenameFilter(String[] extensions) {
			super();
			mExtensions = extensions;
		}
		
		@Override
		public boolean accept(File dir, String filename) {
			if(new File(dir, filename).isDirectory()) {
				// Accept all directory names
				return true;
			}
			if(mExtensions != null && mExtensions.length > 0) {
				for(int i = 0; i < mExtensions.length; i++) {
					if(filename.endsWith(mExtensions[i])) {
						// The filename ends with the extension
						return true;
					}
				}
				// The filename did not match any of the extensions
				return false;
			}
			// No extensions has been set. Accept all file extensions.
			return true;
		}
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
            case R.id.aboutScreen: // Takes user to about screen
                Intent openAboutScreen = new Intent(
                        "android.intent.action.ABOUT");
                startActivity(openAboutScreen);
                return true;
            case R.id.helpMenu: // Takes user to help blog
                Intent openHelpBlog = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://chronihelpblog.wordpress.com"));
                startActivity(openHelpBlog);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
