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
import android.widget.CheckBox;
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
	protected boolean inMovePickMode = false;

	protected File copiedFile = null;
	protected ArrayList<File> cutFiles = null;
	protected Menu mOptionsMenu = null;	// stores the options menu for the Activity

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
						View checkbox = child.findViewById(R.id.checkBoxFilePicker);
						checkbox.setVisibility(View.GONE);
					}
				}

				// if in delete mode or move mode, resets back to normal mode
				if (inDeleteMode)
					toggleDelete();
				if (inMovePickMode)
					toggleMove(false);	// pass false, not actually executing the move

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
					CharSequence[] options = {"Copy", "Move", "Delete"};    // the user's options
					new AlertDialog.Builder(FilePickerActivity.this).setTitle(chosenFile.getName())
							.setItems(options, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							if (which == 0) {    // copy has been chosen
								// resets cutFiles if it contains files
								if (cutFiles != null)
									cutFiles = null;

								copiedFile = chosenFile;
								Toast.makeText(FilePickerActivity.this, "File Copied!", Toast.LENGTH_SHORT).show();

								// sets the pasteButton's visibility to visible once the file has been copied
								Button pasteButton = (Button) findViewById(R.id.filePickerPasteButton);
								pasteButton.setVisibility(View.VISIBLE);

								dialog.dismiss();

							} else if (which == 1) {	// move has been chosen
								// resets copiedFile if it has a file in it
								if (copiedFile != null)
									copiedFile = null;

								cutFiles = new ArrayList<File>();
								cutFiles.add(chosenFile);
								Toast.makeText(FilePickerActivity.this, "File Copied!", Toast.LENGTH_SHORT).show();

								// sets the pasteButton's visibility to visible once the file has been copied
								Button pasteButton = (Button) findViewById(R.id.filePickerPasteButton);
								pasteButton.setVisibility(View.VISIBLE);

								dialog.dismiss();

							} else if (which == 2) {    // delete has been chosen
								// shows a new dialog asking if the user would like to delete the file or not
								new AlertDialog.Builder(FilePickerActivity.this).setMessage("Are you sure you want to delete this file?")
										.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface dialog, int which) {
												// if deleting a currently copied or cut file, un-copy it
												if ((copiedFile != null && chosenFile.equals(copiedFile))
														|| (cutFiles != null && cutFiles.contains(chosenFile))) {

													// resets the copied/cut file(s)
													if (copiedFile != null)
														copiedFile = null;
													if (cutFiles != null)
														cutFiles = null;

													// and gets rid of the paste button on the bottom
													Button pasteButton = (Button) findViewById(R.id.filePickerPasteButton);
													pasteButton.setVisibility(View.GONE);
												}

												// deletes the file and updates the adapter
												chosenFile.delete();
												mAdapter.remove(chosenFile);
												Toast.makeText(FilePickerActivity.this, "File Deleted!", Toast.LENGTH_SHORT).show();

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

		// checks off the view's checkbox if in either delete or move mode
		if (inDeleteMode || inMovePickMode) {
			if(newFile.isFile() || newFile.list().length == 0) {
				CheckBox checkBox = (CheckBox) v.findViewById(R.id.checkBoxFilePicker);

				// checks the box if not already checked
				if (!checkBox.isChecked())
					checkBox.setChecked(true);

				// un-checks the box if already checked
				else
					checkBox.setChecked(false);
			}

		} else {
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

				// Finishes the activity
				finish();
			} else {
				mainDirectory = newFile;
				// Updates the files list
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

			// cutting files instead of copying
			if (cutFiles != null) {
				boolean success = true;	// tells whether all files have been copied or not
				boolean allDeleted = true;	// tells whether all files have been deleted or not
				boolean filesMoved = false; // tells if any files have been moved at all

				// pastes each file to the new directory one at a time
				for (File file : cutFiles) {
					// only move if NOT in the same directory
					if (!file.getAbsolutePath().equals(mainDirectory.getAbsolutePath() + "/" + file.getName())) {
						// once success becomes false, it will aways be false
						success = success && copyFileToCurrentDirectory(file);

						// same with allDeleted
						allDeleted = allDeleted && file.delete();	// deletes the file after copying

						filesMoved = true;	// files have now been moved
					}
				}

				// inform the user of any errors (deleting or copying)
				if (!success || !allDeleted)
					Toast.makeText(this, "ERROR: Could not successfully move all files", Toast.LENGTH_SHORT).show();
				else if (filesMoved)
					Toast.makeText(this,"File(s) Moved!", Toast.LENGTH_SHORT).show();

				// reset cutFiles ArrayList and Paste Button
				cutFiles = null;
				button.setVisibility(View.GONE);
			}

			// copying a file instead of cutting, only copies if it is NOT the same directory
			else if (copiedFile != null
					&& !copiedFile.getAbsolutePath().equals(mainDirectory.getAbsolutePath() + "/" + copiedFile.getName())) {

				// copies the file to the current directory
				boolean success = copyFileToCurrentDirectory(copiedFile);

				if (!success)	// if file could not be copied, alert the user
					Toast.makeText(this, "ERROR: Could not copy file", Toast.LENGTH_SHORT).show();

			}

		} else {    // the button is acting as a done button (in either move or delete)
			if (inDeleteMode) {
				toggleDelete();
			}

			if (inMovePickMode)
				toggleMove(true);	// pass true, actually executing the cut
		}
	}

	/**
	 * Copies the file given to the mainDirectory, which is the current directory that the user is in.
	 *
	 * @param fileCopying the file that is being copied
	 * @return a boolean telling whether the copy was successful or not
	 */
	public boolean copyFileToCurrentDirectory(File fileCopying) {
		boolean result = true;	// result is true until proven false

		// copies the file through the use of Streams
		InputStream input = null;
		OutputStream output = null;

		try {
			// input file is the copied file, and output file is a new file at the current directory
			File newFile = new File(mainDirectory.getAbsolutePath() + "/" + fileCopying.getName());
			input = new FileInputStream(fileCopying);
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
			result = false;	// error occurred, could not copy the file

		} finally {
			try {
				input.close();
				output.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return result;
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

					else {    // if it succeeded, updates the adapter
						mAdapter.add(newFolder);
						Toast.makeText(FilePickerActivity.this, "Folder Created!",Toast.LENGTH_SHORT).show();
					}

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
	 * This method either shows all of the checkboxes on the list items, or adds every file that has been checked off
	 *
	 * @param executeMove a boolean that specifies whether to add the files to the ArrayList or not
	 */
	public void toggleMove(boolean executeMove) {
		ViewGroup list = getListView();
		int number = list.getChildCount();

		if (inMovePickMode) {
			mOptionsMenu.findItem(R.id.deleteFilePickerMenu).setVisible(true);	// puts the delete items option back

			cutFiles = new ArrayList<File>();	// initializes cutFiles to add files to

			// only adds files IF the user has pressed done (executeMove will be true)
			if (executeMove) {
				// gets rid of all of the move checkboxes and add files to cutFiles
				for (int i = 0; i < number; i++) {
					View child = list.getChildAt(i);
					CheckBox checkbox = (CheckBox) child.findViewById(R.id.checkBoxFilePicker);

					// adds the file to cutFiles IF the checkbox is checked
					if (checkbox.isChecked()) {
						File fileToCut = mAdapter.getItem(i);
						cutFiles.add(fileToCut);

						// un-checks the checkbox
						checkbox.setChecked(false);
					}

					checkbox.setVisibility(View.GONE);
				}
			}

			// resets cutFiles if no files have been added
			if (cutFiles.size() == 0)
				cutFiles = null;
			else
				Toast.makeText(this, "File(s) Copied!", Toast.LENGTH_SHORT).show();

			// changes the bottom button text back to "Paste" and gives it the correct visibility
			Button bottomButton = (Button) findViewById(R.id.filePickerPasteButton);
			bottomButton.setText("Paste");
			if (copiedFile == null && cutFiles == null)
				bottomButton.setVisibility(View.GONE);
			else
				bottomButton.setVisibility(View.VISIBLE);

		} else {
			mOptionsMenu.findItem(R.id.deleteFilePickerMenu).setVisible(false);	// removes the delete items menu option

			// displays all of the move checkboxes
			for (int i=0; i<number; i++) {
				View child = list.getChildAt(i);
				View checkbox = child.findViewById(R.id.checkBoxFilePicker);
				checkbox.setVisibility(View.VISIBLE);
			}

			// changes the bottom button text to "Done"
			Button bottomButton = (Button) findViewById(R.id.filePickerPasteButton);
			bottomButton.setText("Done");
			bottomButton.setVisibility(View.VISIBLE);
		}

		inMovePickMode = !inMovePickMode;

	}

	/**
	 * This method either displays an X on the right hand side of each list item or
	 * gets rid of it.
	 */
	public void toggleDelete() {
		final ViewGroup list = getListView();
		final int number = list.getChildCount();

		if (inDeleteMode) {
			mOptionsMenu.findItem(R.id.moveFilePickerMenu).setVisible(true);	// puts the move items menu option back

			// first makes sure that there are checked off items
			boolean hasCheckedItems = false;
			for (int i=0; i<number; i++) {
				View child = list.getChildAt(i);
				CheckBox checkbox = (CheckBox) child.findViewById(R.id.checkBoxFilePicker);
				hasCheckedItems = hasCheckedItems || checkbox.isChecked();	// once this turns true, will always be true
			}

			// only deletes items IF there are any checked off
			if (hasCheckedItems) {

				final ArrayList<File> filesToDelete = new ArrayList<File>();	// stores the files that will be deleted

				// gets rid of all the checkboxes and adds the checked off files to the delete list
				for (int i = 0; i < number; i++) {
					View child = list.getChildAt(i);
					CheckBox checkbox = (CheckBox) child.findViewById(R.id.checkBoxFilePicker);

					if (checkbox.isChecked()) {
						// adds the file to the list of files that will be deleted
						filesToDelete.add(mAdapter.getItem(i));
						// un-checks the checkbox if checked
						checkbox.setChecked(false);
					}

					// gets rid of the checkbox
					checkbox.setVisibility(View.GONE);
				}

				new AlertDialog.Builder(this).setMessage("File(s) will be deleted, do you wish to continue?")
						.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								boolean allDeleted = true;    // tells whether all files have been deleted or not

								// goes through and deletes the files in the list
								for (File file : filesToDelete) {
									// once allDeleted becomes false, it will aways be false
									allDeleted = allDeleted && file.delete();
									mAdapter.remove(file);
								}

								if (!allDeleted)    // alerts the user if all the files could not be deleted
									Toast.makeText(FilePickerActivity.this, "ERROR: Could not delete all files", Toast.LENGTH_SHORT).show();
								else
									Toast.makeText(FilePickerActivity.this, "File(s) Deleted!", Toast.LENGTH_SHORT).show();

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

			} else {
				// gets rid of all the checkboxes but doesn't add any files to delete
				for (int i = 0; i < number; i++) {
					View child = list.getChildAt(i);
					CheckBox checkbox = (CheckBox) child.findViewById(R.id.checkBoxFilePicker);

					if (checkbox.isChecked())
						// un-checks the checkbox if checked
						checkbox.setChecked(false);

					// gets rid of the checkbox
					checkbox.setVisibility(View.GONE);
				}
			}

			// changes the bottom button text back to "Paste"
			Button bottomButton = (Button) findViewById(R.id.filePickerPasteButton);
			bottomButton.setText("Paste");

			// gives it the correct visibility
			if (copiedFile == null && cutFiles == null)
				bottomButton.setVisibility(View.GONE);
			else
				bottomButton.setVisibility(View.VISIBLE);

		} else {
			mOptionsMenu.findItem(R.id.moveFilePickerMenu).setVisible(false);	// removes the move items menu option

			// displays all of the delete images
			for (int i=0; i<number; i++) {
				View child = list.getChildAt(i);
				View checkbox = child.findViewById(R.id.checkBoxFilePicker);
				checkbox.setVisibility(View.VISIBLE);
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
		mOptionsMenu = menu;	// saves the menu for later use
		return true;
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handles menu item selection
        switch (item.getItemId()) {
			case R.id.moveFilePickerMenu:
				toggleMove(true);	// pass true, actually chose to execute the cut
				return true;
            case R.id.deleteFilePickerMenu:	// Takes user to main menu
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
