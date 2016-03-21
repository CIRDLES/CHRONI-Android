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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

	protected File copiedFile = null;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTheme(android.R.style.Theme_Holo);

        // Set the view to be shown if the list is empty
		LayoutInflater inflator = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View emptyView = inflator.inflate(R.layout.file_picker_empty_view, null);
		((ViewGroup) getListView().getParent()).addView(emptyView);
		getListView().setEmptyView(emptyView);

		// adds a RelativeLayout to wrap the listView so that a button can be placed at the bottom
		RelativeLayout outerLayout = (RelativeLayout) inflator.inflate(R.layout.file_picker_regular_view, null);
		((ViewGroup) getListView().getParent()).addView(outerLayout);

		// sets the margin for the listView so that the bottom item isn't covered
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
		params.setMargins(0, 0, 0, 100);
		getListView().setLayoutParams(params);

		// defines the action for the bottom button
		Button previousFolderButton = (Button) findViewById(R.id.filePickerPreviousButton);
		previousFolderButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// moves to the previous directory (if it exists) and refreshes the list of files
				if (mainDirectory.getParentFile() != null) {
					mainDirectory = mainDirectory.getParentFile();
					refreshFilesList();

					// goes through and remo any stubborn delete imageViews
					ViewGroup list = getListView();
					int number = list.getChildCount();

					// gets rid of all the delete images
					for (int i = 0; i < number; i++) {
						View child = list.getChildAt(i);
						View delete = child.findViewById(R.id.deleteButton);
						delete.setVisibility(View.INVISIBLE);
					}
				}

				// if in delete mode, resets back to normal mode
				if (inDeleteMode)
					toggleDelete();

			}
		});

		// defines what happens to a list item on a long press
		getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, final View view, int position, long id) {
				// stores the address of the file/folder that has been chosen
				final File chosenFile = (File) parent.getItemAtPosition(position);

				// only gives options if the chosen file is NOT a directory
				if (!chosenFile.isDirectory()) {
					// brings up a Dialog box and asks the user if they want to copy or delete the file
					CharSequence[] options = {"Copy", "Delete"};    // the user's options
					new AlertDialog.Builder(FilePickerActivity.this).setItems(options, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							if (which == 0) {    // copy has been chosen
								copiedFile = chosenFile;
								// sets the pasteButton's visibility to visible once the file has been copied
								Button pasteButton = (Button) findViewById(R.id.filePickerPasteButton);
								pasteButton.setVisibility(View.VISIBLE);

								dialog.dismiss();
							} else if (which == 1) {    // delete has been chosen
								// shows a new dialog asking if the user would like to delete the file or not
								new AlertDialog.Builder(FilePickerActivity.this).setMessage("Are you sure you want to delete this file?")
										.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface dialog, int which) {
												// if deleting the currently copied file, un-copy it
												if (copiedFile != null && chosenFile.equals(copiedFile)) {
													copiedFile = null;
													Button pasteButton = (Button) findViewById(R.id.filePickerPasteButton);
													pasteButton.setVisibility(View.GONE);
												}

												// deletes the file and updates the adapter
												chosenFile.delete();
												mAdapter.remove(chosenFile);
												dialog.dismiss();
											}
										})
										.setNegativeButton("No", new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface dialog, int which) {
												dialog.dismiss();
											}
										})
										.show();
								dialog.dismiss();
							}
						}
					}).show();

				} else if (chosenFile.list().length == 0) {	// can only delete directory if it's empty

					// brings up a Dialog box and asks the user if they want to copy or delete the file
					CharSequence[] options = {"Delete"};    // the user's options
					new AlertDialog.Builder(FilePickerActivity.this).setItems(options, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// shows a new dialog asking if the user would like to delete the file or not
							new AlertDialog.Builder(FilePickerActivity.this).setMessage("Are you sure you want to delete this file?")
									.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog, int which) {
											// if deleting the currently copied file, un-copy it
											if (copiedFile != null && chosenFile.equals(copiedFile)) {
												copiedFile = null;
												Button pasteButton = (Button) findViewById(R.id.filePickerPasteButton);
												pasteButton.setVisibility(View.GONE);
											}

											// deletes the file and updates the adapter
											chosenFile.delete();
											mAdapter.remove(chosenFile);
											dialog.dismiss();
										}
									})
									.setNegativeButton("No", new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog, int which) {
											dialog.dismiss();
										}
									})
									.show();
						}
					}).show();
				}

				return true;
			}
		});

		// Obtain content from the current intent for later use
		intentContent = getIntent().getStringExtra("Default_Directory");

		// Set initial directory if it hasn't been already defined
		if (mainDirectory == null) {
			mainDirectory = Environment.getExternalStorageDirectory(); // Takes user to root directory folder

			// Sets the initial directory based on what file the user is looking for (Aliquot or Report Settings)
			if (intentContent.contentEquals("Aliquot_Directory"))
				mainDirectory = new File(mainDirectory + "/CHRONI/Aliquot"); // Takes user to the Aliquot folder
			else if (intentContent.contentEquals("Report_Settings_Directory"))    // Report Settings Menu if coming from a Dropdown Menu
				mainDirectory = new File(mainDirectory + "/CHRONI/Report Settings");
			else if (intentContent.contentEquals("From_Report_Directory"))    // Report Settings Menu if coming from a Report Settings Menu
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
	protected void onListItemClick(final ListView l, final View v, final int position, long id) {
		final File newFile = (File) l.getItemAtPosition(position);

		// give a prompt asking if the user wishes to delete the selected file
		if (inDeleteMode) {
			if(newFile.isFile() || newFile.list().length == 0) {
				new AlertDialog.Builder(this).setMessage("Are you sure you wish to delete " +
						newFile.getName() + "?")
						.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialogInterface, int i) {
								// first sets the delete image of the very last item in the list to invisible
								View child = l.getChildAt(mAdapter.getCount() - 1);
								ImageView deleteImage = (ImageView) child.findViewById(R.id.deleteButton);
								deleteImage.setVisibility(View.INVISIBLE);

								// then deletes the file and removes it from the adapter
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
					Toast.makeText(this, "File Name: " + newFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();

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
					Toast.makeText(this, "Please move your selected file to one of the CHRONI directories.", Toast.LENGTH_SHORT).show();
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

	public void onPasteButtonClick(View v) {
		Button button = (Button) v;
		String buttonText = button.getText().toString();

		// the button is acting as a paste button
		if (buttonText.equals("Paste")) {
			// only copy if a file to copy exists
			if (copiedFile != null) {

				// copies the file through the use of Streams
				InputStream input = null;
				OutputStream output = null;

				try {
					// input file is the copied file, and output file is a new file at the current directory
					File newFile = new File(mainDirectory.getAbsolutePath() + "/" + copiedFile.getName());
					input = new FileInputStream(copiedFile);
					output = new FileOutputStream(newFile.getAbsolutePath());

					// copies the file's contents to the new director
					byte[] buf = new byte[1024];
					int bytesRead;
					while ((bytesRead = input.read(buf)) > 0) {
						output.write(buf, 0, bytesRead);
					}

					// adds the file to the adapter immediately after it has been copied
					mAdapter.add(newFile);

				} catch (IOException e) {
					e.printStackTrace();
					Toast.makeText(this, "ERROR: Could not copy file", Toast.LENGTH_SHORT).show();

				} finally {
					try {
						input.close();
						output.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

		} else	// the button is acting as a done button (when deleting)
			toggleDelete();

	}

	public void onNewFolderButtonClicked(View v) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(this).setMessage("Enter the new folder's name below.");

		// creates the layout for the EditText, will be added to the dialog box
		final EditText newFolderText = new EditText(this);

		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		params.setMargins(25, 10, 20, 25);

		LinearLayout dialogLayout = new LinearLayout(this);
		dialogLayout.addView(newFolderText);
		newFolderText.setLayoutParams(params);

		dialog.setView(dialogLayout);	// adds EditText to dialog box

		dialog.setPositiveButton("Create", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Editable newFolderName = newFolderText.getText();
				File newFolder = new File(mainDirectory.getAbsolutePath() + "/" + newFolderName.toString());

				// creates the folder if the length of the text is greater than 0 AND the folder does not already exist
				if (newFolderName.length() > 0 && !newFolder.exists()) {
					// creates the actual folder
					boolean createdSuccessfully = newFolder.mkdir();

					// alerts the user if the folder was not created
					if (!createdSuccessfully)
						Toast.makeText(FilePickerActivity.this, "ERROR: Folder could not be created", Toast.LENGTH_SHORT).show();

					else	// if it succeeded, updates the adapter
						mAdapter.add(newFolder);
				} else
					Toast.makeText(FilePickerActivity.this, "ERROR: Invalid folder name", Toast.LENGTH_SHORT).show();

				dialog.dismiss();
			}
		})
		.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});

		dialog.show();
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

			// changes the bottom button text back to "Paste" and gives it the correct visibility
			Button bottomButton = (Button) findViewById(R.id.filePickerPasteButton);
			bottomButton.setText("Paste");
			if (copiedFile == null)
				bottomButton.setVisibility(View.GONE);
			else
				bottomButton.setVisibility(View.VISIBLE);
		}

		else {
			// displays all of the delete images
			for (int i=0; i<number; i++) {
				View child = list.getChildAt(i);
				View delete = child.findViewById(R.id.deleteButton);
				delete.setVisibility(View.VISIBLE);
			}

			// changes the bottom button text to "Done"
			Button bottomButton = (Button) findViewById(R.id.filePickerPasteButton);
			bottomButton.setText("Done");
			bottomButton.setVisibility(View.VISIBLE);
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
            case R.id.delete:	// Takes user to main menu
                toggleDelete();
                return true;
			case R.id.exitFilePickerMenu:	// Exits the File Picker Activity
				finish();
				return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
