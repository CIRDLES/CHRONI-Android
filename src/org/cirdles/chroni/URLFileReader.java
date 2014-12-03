package org.cirdles.chroni;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;

import android.app.AlertDialog;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class URLFileReader{

//	private ProgressDialog mProgressDialog;
	private String fileName = "";	// generated name of the file
	private String fileType;	// Report Settings or Aliquot File
	private String fileURL;	// the URL of the file
    private String className; // the name of the class
	private String downloadMethod; // the type of download used to get file (igsn or url)
	private Context classContext; // an instance of the activity's context for memory access

	public URLFileReader(Context classContext, String className, String URL, String downloadMethod){
		setFileURL(URL); // Sets the URL for download
		setDownloadMethod(downloadMethod); // sets download type
		setClassContext(classContext);
        setClassName(className);
		// Sets the type of file being accessed for saving purposes
		startFileDownload(classContext, className);
		}

    public URLFileReader(Context classContext, String className, String URL, String downloadMethod, String fileName){
        setFileURL(URL); // Sets the URL for download
        setDownloadMethod(downloadMethod); // sets download type
        setClassContext(classContext);
        setClassName(className);
        setFileName(fileName);
        // Sets the type of file being accessed for saving purposes
        startFileDownload(classContext, className);
    }

	public void startFileDownload(Context classContext, String className){
		if(className.contentEquals("HomeScreen")){
            // Sets the type of file and URL being accessed for saving purposes of the default Report Settings
            setFileType("Report Settings");
            setFileName(createFileName());	// Always downloading Default RS here

            // Sets up the Download thread
            final DownloadTask downloadTask = new DownloadTask(classContext);
            downloadTask.execute(fileURL); // retrieves the file from the specified URL
		}else{
			if(className.contentEquals("AliquotMenu")){
				// Sets the type of file and URL being accessed for saving purposes
				setFileType("Aliquot");
                if(getFileName().isEmpty()){
    				setFileName(createFileName());
                }	// generates file name based on URL
			}
			else if(className.contentEquals("ReportSettingsMenu")){
				// Sets the type of file and URL being accessed for saving purposes
				setFileType("Report Settings");
                if(getFileName().isEmpty()) {
                    setFileName(createFileName());
                }// generates file name based on URL
			}
		
			// Sets up the Download thread 
			final DownloadTask downloadTask = new DownloadTask(classContext);		
			downloadTask.execute(fileURL); // retrieves the file from the specified URL		
		}

	}

	/*
	 * Creates file name based on the file's type and URL
	 */
	protected String createFileName() {
		String name = null;

		if(getFileType().contains("Aliquot")){
			// If downloading based on IGSN URL, just use IGSN for name
			if(downloadMethod.contains("igsn")){
			String[] URL = getFileURL().split("igsn=");
			name = URL[1];
			if(name.contains("&username=")){
				// Makes an additional split to remove the username and password query from the file name
				String[] url2 = name.split("&username=");
				name = url2[0]; 
			}
			}
			
			// if downloading based on URL, makes name from ending of URL
			else if(downloadMethod.contains("url")){
				String[] URL = getFileURL().split("/");
				name = URL[URL.length-1];
				if (name.contains(".xml")){
					// Removes the file name ending from XML files
					String [] newName = name.split(".xml");
					name = newName[0];
				}
			}
		}

        if(getClassName().contentEquals("HomeScreen")) {
            if(fileURL.contentEquals("http://cirdles.org/sites/default/files/Downloads/CIRDLESDefaultReportSettings.xml")){
                name = "Default Report Settings";
            }if(fileURL.contentEquals("http://cirdles.org/sites/default/files/Downloads/Default%20Report%20Settings%202.xml")){
                name = "Default Report Settings 2";
            }
        }

		return name;
	}

	
	public final static InputStream getInputStreamFromURI(String URI){
        InputStream in = null;
        int response = -1;

        	try {
				URL url = new URL(URI); 
				URLConnection conn = url.openConnection();
				HttpURLConnection httpConn = (HttpURLConnection) conn;
				httpConn.setAllowUserInteraction(false);
				httpConn.setInstanceFollowRedirects(true);
				httpConn.setRequestMethod("GET");
				httpConn.connect(); 

				response = httpConn.getResponseCode();                 
				if (response == HttpURLConnection.HTTP_OK) {
				    in = httpConn.getInputStream();                                 
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (ProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
        return in; 
	}

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    // subclass of AsyncTask to handle web request in background
	private class DownloadTask extends AsyncTask<String, Integer, String> {
		private Context context;

		public DownloadTask(Context context) {
			this.context = context;
		}
        String downloadedFilePath; // path where downloaded file is to be written

		@Override
		protected String doInBackground(String... sUrl) {
			// Directories needed to place files in accurate locations
			File chroniDirectory = classContext.getDir("CHRONI", Context.MODE_PRIVATE); //Creating an internal directory for CHRONI files
	    	File aliquotDirectory = new File(chroniDirectory, "Aliquot");
	    	File reportSettingsDirectory = new File(chroniDirectory, "Report Settings");

			// take CPU lock to prevent CPU from going off if the user
			// presses the power button during download
			PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
			PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
			wl.acquire();

			try {
				InputStream input = null;
				OutputStream output = null;
				HttpURLConnection connection = null;
				try {
					URL url = new URL(sUrl[0]);
					connection = (HttpURLConnection) url.openConnection();
					connection.connect();

					if (connection.getResponseCode() != HttpURLConnection.HTTP_OK)
						return "Server returned HTTP "
								+ connection.getResponseCode() + " "
								+ connection.getResponseMessage();

					// might be -1: if server did not report the length
					long fileLength = connection.getContentLength();

					// download the file to the appropriate location
					input = connection.getInputStream();
					if(fileType.contains("Aliquot")){
						if(fileLength == 55){ // Cancels if invalid IGSN file (if file has a length of 0.05 KB)
							AliquotMenuActivity.setInvalidFile(true);	// Sets file as invalid
							cancel(true);
						}else{
                            downloadedFilePath = aliquotDirectory+ "/" + fileName + ".xml";
							output = new FileOutputStream(aliquotDirectory+ "/" + fileName + ".xml");
//							AliquotMenuActivity.setAbsoluteFilePath(aliquotDirectory+ "/" + fileName + ".xml");
						}
					}else if(fileType.contains("Report Settings")){
                        downloadedFilePath = reportSettingsDirectory + "/" + fileName + ".xml";
                        output = new FileOutputStream(reportSettingsDirectory + "/" + fileName + ".xml");
					}
					
					byte data[] = new byte[4096];
					long total = 0;
					int count;
					while ((count = input.read(data)) != -1) {
						// allow canceling with back button
						if (isCancelled()){
							AliquotMenuActivity.setInvalidFile(true);	// Sets file as invalid
							return null;
							}
						total += count;
						// for publishing with progress bar
						if (fileLength > 0) // only if total length is known
							publishProgress((int) (total * 100 / fileLength));
						// Creates the actual downloaded file.
						// Must be bigger than 55 because that is the size of error files
						// if -1, the server is not sending back requested length so, for now, downloading 
							output.write(data, 0, count);
					}

				} catch (Exception e) {
					return e.toString();
				} finally {
					try {
						if (output != null)
							output.close();
						if (input != null)
							input.close();
					} catch (IOException ignored) {
					}

					if (connection != null)
						connection.disconnect();
				}
			} finally {
				wl.release();
			}
			return null;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
//			mProgressDialog.show();
		}

		@Override
		protected void onProgressUpdate(Integer... progress) {
			super.onProgressUpdate(progress);
			// if we get here, length is known, now set indeterminate to false
//			mProgressDialog.setIndeterminate(false);
//			mProgressDialog.setMax(100);
//			mProgressDialog.setProgress(progress[0]);
		}

		@Override
		protected void onPostExecute(String result) {
            boolean erroneousFile = false;
            if(String.valueOf(classContext).contains("AliquotMenuActivity")){
                // Figures out if aliquot file is erroneous
                erroneousFile = parseAliquotFileForError(downloadedFilePath);
                if (result != null || erroneousFile) {
                    Toast.makeText(context, "Download error: " + "You have specified a private IGSN", Toast.LENGTH_LONG).show();
                    Toast.makeText(context, "Enter your profile information to access this private IGSN file.", Toast.LENGTH_LONG).show();
                    File fileToRemove = new File(downloadedFilePath);
                    fileToRemove.delete();
                } else {
                    Toast.makeText(context, "File downloaded!", Toast.LENGTH_SHORT).show();
                }
            }else if(String.valueOf(classContext).contains("ReportSettingsMenuActivity")){
                erroneousFile = parseReportSettingsFileForError(downloadedFilePath);
                if (result != null || erroneousFile) {
                    Toast.makeText(context, "Download error: " + "You have specified an invalid Report Settings file", Toast.LENGTH_LONG).show();
                    File fileToRemove = new File(downloadedFilePath);
                    fileToRemove.delete();
                } else {
                    Toast.makeText(context, "File downloaded!", Toast.LENGTH_SHORT).show();
                }
            }

        }
		
		@Override
		protected void onCancelled(String result) {
			if(downloadMethod.contentEquals("igsn")){
				Toast.makeText(context, "Download error: You have specified an invalid IGSN",
					Toast.LENGTH_LONG).show();
			}else{
				Toast.makeText(context, "Download error: You have specified an invalid URL",
						Toast.LENGTH_LONG).show();
			}
		}

        /*
        Splits report settings file name
        */
        private String splitFileName(String fileName){
            String[] fileNameParts = fileName.split("/");
            String newFileName = fileNameParts[fileNameParts.length-1];
            return newFileName;
        }

        /*
        Parses file for error
         */
        protected boolean parseAliquotFileForError(String downloadedFilePath){
            boolean erroneousFile = false;
            try {
                // Begins the parsing of the file
                File fXmlFile = new File(downloadedFilePath);
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                DomParser parser = new DomParser();
                Document doc = dBuilder.parse(fXmlFile);

                // Get the document's root XML nodes to see if file contains an error
                NodeList root = doc.getChildNodes();
                if(parser.getNode("results", root) != null) {
                    Node rootNode = parser.getNode("results", root);
                    NodeList rootNodes = rootNode.getChildNodes();
                    String errorMessage = parser.getNodeValue("error", rootNodes);
                    erroneousFile = true;
                }
            }catch (Exception e) {
                    e.printStackTrace();
                }

                return erroneousFile;
        }
	}

    /*
        Parses file for error
         */
    protected boolean parseReportSettingsFileForError(String downloadedFilePath){
        boolean erroneousFile = false;
        try {
            // Begins the parsing of the file
            File fXmlFile = new File(downloadedFilePath);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            DomParser parser = new DomParser();
            Document doc = dBuilder.parse(fXmlFile);

            // Get the document's root XML nodes to see if file contains an error
            NodeList root = doc.getChildNodes();
            if(parser.getNode("ReportSettings", root) == null) {
                erroneousFile = true;
            }
        }catch (Exception e) {
            e.printStackTrace();
        }

        return erroneousFile;
    }


	// The accessors and mutators of the outer class
	
	public String getFileType() {
		return fileType;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}
	
	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileURL() {
		return fileURL;
	}

	public void setFileURL(String fileURL) {
		this.fileURL = fileURL;
	}
	
	public String getDownloadMethod() {
		return downloadMethod;
	}

	public void setDownloadMethod(String downloadMethod) {
		this.downloadMethod = downloadMethod;
	}

	public Context getClassContext() {
		return classContext;
	}

	public void setClassContext(Context classContext) {
		this.classContext = classContext;
	}
}
