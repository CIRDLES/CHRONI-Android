package org.cirdles.chroni;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.os.AsyncTask;
import android.os.Environment;
import android.os.PowerManager;
import android.content.Context;
import android.widget.Toast;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class URLFileReader{

	private String fileName = "";	// generated name of the file
	private String fileType;	// Report Settings or Aliquot File
	private String fileURL;		// the URL of the file
    private String className;	// the name of the class
	private String downloadMethod;	// the type of download used to get file (igsn or url)
	private Context classContext;	// an instance of the activity's context for memory access

	public URLFileReader(Context classContext, String className, String URL, String downloadMethod) {
		setFileURL(URL); // Sets the URL for download
		setDownloadMethod(downloadMethod); // sets download type
		setClassContext(classContext);
        setClassName(className);

	}

	public void startFileDownload() {
		if(className.contentEquals("HomeScreen")) {
            // Sets the type of file and URL being accessed for saving purposes of the default Report Settings
            setFileType("Report Settings");
            setFileName(createFileName());	// Always downloading Default RS here


            // Sets up the Download thread
            final DownloadTask downloadTask = new DownloadTask(classContext);
            downloadTask.execute(fileURL); // retrieves the file from the specified URL

		} else {

			if(className.contentEquals("AliquotMenu")){
				// Sets the type of file and URL being accessed for saving purposes
				setFileType("Aliquot");
                if(getFileName().isEmpty()){
    				setFileName(createFileName());
                }	// generates file name based on URL

			} else if(className.contentEquals("ReportSettingsMenu")){
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

	/**
	 * Creates file name based on the file's type and URL
	 */
	protected String createFileName() {
		String name = null;

		if(getFileType().contains("Aliquot")){
			// If downloading based on IGSN URL, just use IGSN for name
			if(downloadMethod.contains("igsn")){
				String[] url = getFileURL().split("igsn=");
				name = url[1];

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
            if (fileURL.contentEquals("https://raw.githubusercontent.com/CIRDLES/cirdles.github.com/master/assets/Default%20Report%20Settings%20XML/Default%20Report%20Settings.xml")) {
                name = "Default Report Settings";
            } else if (fileURL.contentEquals("https://raw.githubusercontent.com/CIRDLES/cirdles.github.com/master/assets/Default%20Report%20Settings%20XML/Default%20Report%20Settings%202.xml")) {
                name = "Default Report Settings 2";
            }
        }

		return name;
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
        String downloadedFilePath = "";	// path where downloaded file is to be written

		@Override
		protected String doInBackground(String... sUrl) {

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
					if(fileType.contains("Aliquot")) {
						if(fileLength >= 55){ // Cancels if invalid IGSN file (if file has a length of 0.05 KB)
							cancel(true);

						} else {
                            downloadedFilePath = Environment.getExternalStorageDirectory()
									+ "/CHRONI/Aliquot/" + fileName + ".xml"; // Stores name of path

                            output = new FileOutputStream(Environment.getExternalStorageDirectory()
									+ "/CHRONI/Aliquot/" + fileName + ".xml");
						}

					} else if (fileType.contains("Report Settings")) {
                        downloadedFilePath = Environment.getExternalStorageDirectory()
								+ "/CHRONI/Report Settings/" + fileName + ".xml";

                        output = new FileOutputStream(Environment.getExternalStorageDirectory()
								+ "/CHRONI/Report Settings/" + fileName + ".xml");
					}
					
					byte data[] = new byte[4096];
					long total = 0;
					int count;
					while ((count = input.read(data)) != -1) {
						// allow canceling with back button
						if (isCancelled()){
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
		protected void onPostExecute(String result) {
            boolean erroneousFile;
            if(String.valueOf(classContext).contains("AliquotMenuActivity")){
                // Figures out if aliquot file is erroneous
                erroneousFile = parseAliquotFileForError(downloadedFilePath);
                if (result != null || erroneousFile) {
                    Toast.makeText(context, "Download error: " + "You have specified a private IGSN", Toast.LENGTH_LONG).show();
                    Toast.makeText(context, "Enter your profile information to access this private IGSN file.", Toast.LENGTH_LONG).show();
                    File fileToRemove = new File(downloadedFilePath);
                    fileToRemove.delete();
                } else {

                    Toast.makeText(context, getActualFileName(downloadedFilePath) + " downloaded!", Toast.LENGTH_LONG).show();
                }

            } else if(String.valueOf(classContext).contains("ReportSettingsMenuActivity")){
                erroneousFile = parseReportSettingsFileForError(downloadedFilePath);
                if (result != null || erroneousFile) {
                    Toast.makeText(context, "Download error: " + "You have specified an invalid Report Settings file", Toast.LENGTH_LONG).show();
                    File fileToRemove = new File(downloadedFilePath);
                    fileToRemove.delete();
                } else {
                    Toast.makeText(context, getActualFileName(downloadedFilePath) + " downloaded!", Toast.LENGTH_LONG).show();
                }
            }

        }
		
		@Override
		protected void onCancelled(String result) {
			if (downloadMethod.contentEquals("igsn")) {
				Toast.makeText(context, "Download error: You have specified an invalid IGSN",
						Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(context, "Download error: You have specified an invalid URL",
						Toast.LENGTH_LONG).show();
			}
		}

		/**
		 * Parses file for error
		 */
        protected boolean parseAliquotFileForError(String downloadedFilePath){
            boolean erroneousFile = false;

			if (downloadedFilePath.isEmpty())
				erroneousFile = true;

			else {
				try {
					// Begins the parsing of the file
					File fXmlFile = new File(downloadedFilePath);
					DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
					DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
					DomParser parser = new DomParser();
					Document doc = dBuilder.parse(fXmlFile);

					// Get the document's root XML nodes to see if file contains an error
					NodeList root = doc.getChildNodes();
					if (parser.getNode("results", root) != null)
						erroneousFile = true;

				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			return erroneousFile;
        }
	}

	/**
	 * Obtains the actual name of the file that is being downloaded (i.e. what the file is named).
	 *
	 * @param filePath the path to the file
	 * @return the name of the file
	 */
	private String getActualFileName(String filePath) {
		String[] splitParts = filePath.split("/");
		return splitParts[splitParts.length - 1];
	}

	/**
	 * Parses file for error
	 *
	 * @param downloadedFilePath
	 * @return
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
            if(parser.getNode("ReportSettings", root) == null)
                erroneousFile = true;

        }catch (Exception e) {
            e.printStackTrace();
        }

        return erroneousFile;
    }

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

	public void setDownloadMethod(String downloadMethod) {
		this.downloadMethod = downloadMethod;
	}

	public void setClassContext(Context classContext) {
		this.classContext = classContext;
	}

}
