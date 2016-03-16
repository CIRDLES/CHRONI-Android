/**
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

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class FilePickerActivity extends ListActivity {

	// Sets whether hidden files should be visible in the list or not
	public final static String EXTRA_SHOW_HIDDEN_FILES = "show_hidden_files";

	// The allowed file extensions in an ArrayList of Strings
	public final static String EXTRA_ACCEPTED_FILE_EXTENSIONS = "accepted_file_extensions";

	protected File mainDirectory;
	protected ArrayList<File> mFiles;
	protected FilePickerListAdapter mAdapter;
	protected boolean mShowHiddenFiles = false;
	protected String[] acceptedFileExtensions;
    protected String intentContent;
	protected boolean inDeleteMode = false;


    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTheme(android.R.style.Theme_Holo);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);

        // Set the view to be shown if the list is empty
		LayoutInflater inflator = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View emptyView = inflator.inflate(R.layout.file_picker_empty_view, null);
		((ViewGroup) getListView().getParent()).addView(emptyView);
		getListView().setEmptyView(emptyView);

		// adds a RelativeLayout to wrap the listView so that a button can be placed at the bottom
		RelativeLayout outerLayout = (RelativeLayout) inflator.inflate(R.layout.file_picker_regular_view, null);
		((ViewGroup) getListView().getParent()).addView(outerLayout);

		// defines the action for the bottom button
		Button bottomButton = (Button) findViewById(R.id.filePickerButton);
		bottomButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Button button = (Button) v;
				String buttonText = button.getText().toString();

				if (buttonText.equals("Previous Directory")) {
					// moves to the previous directory (if it exists) and refreshes the list of files
					if (mainDirectory.getParentFile() != null) {
						mainDirectory = mainDirectory.getParentFile();
						refreshFilesList();
					}

				} else if (buttonText.equals("Done"))
					// finished deleting files
					toggleDelete();
			}
		});

		// Obtain content from the current intent for later use
		intentContent = getIntent().getStringExtra("Default_Directory");

		// Set initial directory
        mainDirectory = Environment.getExternalStorageDirectory(); // Takes user to root directory folder

		// Sets the initial directory based on what file the user is looking for (Aliquot or Report Settings)
		if (intentContent.contentEquals("Aliquot_Directory")){
			mainDirectory = new File(mainDirectory + "/CHRONI/Aliquot"); // Takes user to the Aliquot folder
		} else if (intentContent.contentEquals("Report_Settings_Directory")) {	// Report Settings Menu if coming from a Dropdown Menu
            mainDirectory = new File(mainDirectory + "/CHRONI/Report Settings");
        } else if (intentContent.contentEquals("From_Report_Directory")) {  // Report Settings Menu if coming from a Report Settings Menu
            mainDirectory = new File(mainDirectory + "/CHRONI/Report Settings");
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
			acceptedFileExtensions = collection.toArray(new String[collection.size()]);
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
		if (files != null && files.length > 0) {
			for (File f : files) {
				if (f.isHidden() && !mShowHiddenFiles)
					// Don't add the file
					continue;

				// Add the file the ArrayAdapter
				mFiles.add(f);
			}

			Collections.sort(mFiles, new FileComparator());
		}
		mAdapter.notifyDataSetChanged();
	}

	@Override
	protected void onListItemClick(ListView l, View v, final int position, long id) {
		final File newFile = (File) l.getItemAtPosition(position);

		// give a prompt asking if the user wishes to delete the selected file
		if (inDeleteMode) {
			if(newFile.isFile()) {
				new AlertDialog.Builder(this).setMessage("Are you sure you wish to delete " +
						newFile.getName() + "?")
						.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialogInterface, int i) {
								newFile.delete();
								mAdapter.remove(newFile);
								dialogInterface.dismiss();
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
		}

		else {

			// If item selected is a file (and not a directory), allows user to select file
			if (newFile.isFile()) {
				// Sends back selected file name

				if (intentContent.contentEquals("Aliquot_Directory")) {
					Intent returnAliquotIntent = new Intent("android.intent.action.ALIQUOTMENU");
					returnAliquotIntent.putExtra("AliquotXMLFileName", newFile.getAbsolutePath());
					setResult(RESULT_OK, returnAliquotIntent);    // Returns Extra to AliquotMenuActivity
					Toast.makeText(FilePickerActivity.this, "File Name: " + newFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();

					// If coming from any menu (by using the dropdown menu)
				} else if (intentContent.contentEquals("Report_Settings_Directory")) {
					Intent openRSMenu = new Intent("android.intent.action.REPORTSETTINGSMENU");
					openRSMenu.putExtra("ReportSettingsXMLFileName", newFile.getAbsolutePath());
					startActivity(openRSMenu);  // Start a new Report Settings Menu with the selected Report Settings

					// If coming FROM a Report Settings Menu (by pressing the + button)
				} else if (intentContent.contentEquals("From_Report_Directory")) {
					Intent returnRSIntent = new Intent("android.intent.action.REPORTSETTINGSMENU");
					returnRSIntent.putExtra("NewReportSettingsXMLFileName", newFile.getAbsolutePath());
					setResult(RESULT_OK, returnRSIntent);   // Return to previous Report Settings Menu with the new Report Settings

				} else if (intentContent.contentEquals("Root_Directory")) {
					Intent openRSMenu = new Intent("android.intent.action.MAINMENU");
					openRSMenu.putExtra("XMLFileName", newFile.getAbsolutePath());
					Toast.makeText(FilePickerActivity.this, "Please move your selected file to one of the CHRONI directories.", Toast.LENGTH_SHORT).show();
					startActivity(openRSMenu);
				}

				// Finish the activity
				finish();
			} else {
				mainDirectory = newFile;
				// Update the files list
				refreshFilesList();
			}

			super.onListItemClick(l, v, position, id);
		}
	}

	private class FilePickerListAdapter extends ArrayAdapter<File> {

		private List<File> mObjects;

		public FilePickerListAdapter(Context context, List<File> objects) {
			super(context, R.layout.file_picker_list_item, android.R.id.text1, objects);
			mObjects = objects;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View row;

			if(convertView == null) {
				LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				row = inflater.inflate(R.layout.file_picker_list_item, parent, false);
			} else
				row = convertView;

			File object = mObjects.get(position);

			ImageView imageView = (ImageView)row.findViewById(R.id.file_picker_image);
			TextView textView = (TextView)row.findViewById(R.id.file_picker_text);
			// Set single line
			textView.setSingleLine(true);
			textView.setTextSize(24);

			textView.setText(object.getName());
			if(object.isFile())
				// Show the file icon
				imageView.setImageResource(R.drawable.chroni_logo);
			else
				// Show the folder icon
				imageView.setImageResource(R.drawable.chroni_logo);

			return row;
		}

	}

	private class FileComparator implements Comparator<File> {
	    @Override
	    public int compare(File f1, File f2) {
	    	if(f1 == f2)
	    		return 0;

	    	if(f1.isDirectory() && f2.isFile())
	        	// Show directories above files
	        	return -1;

	    	if(f1.isFile() && f2.isDirectory())
	        	// Show files below directories
	        	return 1;

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
				for(String extension : mExtensions)
					if(filename.endsWith(extension))	// The filename ends with the extension
						return true;

				// The filename did not match any of the extensions
				return false;
			}
			// No extensions has been set. Accept all file extensions.
			return true;
		}
	}

	/**
	 * This method either displays an X on the right hand side of each list item or
	 * gets rid of it.
	 */
	public void toggleDelete() {

		ViewGroup list = getListView();
		int number = list.getChildCount();

		if (inDeleteMode) {
			// gets rid of all the delete images
			for (int i=0; i<number; i++) {
				View child = list.getChildAt(i);
				View delete = child.findViewById(R.id.deleteButton);
				delete.setVisibility(View.INVISIBLE);
			}

			// changes the bottom button text back to "Previous Directory"
			Button bottomButton = (Button) findViewById(R.id.filePickerButton);
			bottomButton.setText("Previous Directory");
		}

		else {
			// displays all of the delte images
			for (int i=0; i<number; i++) {
				View child = list.getChildAt(i);
				View delete = child.findViewById(R.id.deleteButton);
				delete.setVisibility(View.VISIBLE);
			}

			// changes the bottom button text to "Done"
			Button bottomButton = (Button) findViewById(R.id.filePickerButton);
			bottomButton.setText("Done");
		}

		inDeleteMode = !inDeleteMode;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.file_delete, menu);
		return true;
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handles menu item selection
        switch (item.getItemId()) {
            case R.id.delete: // Takes user to main menu
                toggleDelete();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
