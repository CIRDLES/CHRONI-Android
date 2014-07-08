package org.cirdles.chroni;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;

import android.os.AsyncTask;
import android.os.Environment;
import android.os.PowerManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

public class URLFileReader{

//	private ProgressDialog mProgressDialog;
	private String fileName = "file_name";	// generated name of the file
	private String fileType;	// Report Settings or Aliquot File
	private String fileURL;	// the URL of the file
	private String downloadMethod; // the type of download used to get file (igsn or url)

	/*
	 * Sets up the Progress Bar and retrieves default Report Settings File from CIRDLES.org
	 */
	public URLFileReader(Context classContext, String className, String URL, String downloadMethod){
		setFileURL(URL); // Sets the URL for download
		setDownloadMethod(downloadMethod); // sets download type
		
		// Sets the type of file being accessed for saving purposes
		startFileDownload(classContext, className);
		}

	public void startFileDownload(Context classContext, String className){
		if(className.contentEquals("HomeScreen")){
		// Sets the type of file and URL being accessed for saving purposes
		setFileType("Report Settings");
		setFileName("Default Report Settings");	// Always downloading Default RS here
		}
		else if(className.contentEquals("AliquotMenu")){
			// Sets the type of file and URL being accessed for saving purposes
			setFileType("Aliquot");
			setFileName(createFileName());	// generates file name based on URL
		}
		else if(className.contentEquals("ReportSettingsMenu")){
			// Sets the type of file and URL being accessed for saving purposes
			setFileType("Report Settings");
			setFileName(createFileName());	// generates file name based on URL
		}
				
		// Sets up the Download thread
		final DownloadTask downloadTask = new DownloadTask(classContext);		
		downloadTask.execute(fileURL); // retrieves the file from the specified URL
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
		}else if(getFileType().contains("Report Settings")){
//			if(!getFileName().contains("Default Report Settings")){
				String[] URL = getFileURL().split("/");
				name = URL[URL.length-1];
				if (name.contains(".xml")){
					// Removes the file name ending from XML files
					String [] newName = name.split(".xml");
					name = newName[0];
//				}			
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
	
	
	// subclass of AsyncTask to handle web request in background
	private class DownloadTask extends AsyncTask<String, Integer, String> {
		private Context context;

		public DownloadTask(Context context) {
			this.context = context;
		}

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
					if(fileType.contains("Aliquot")){
						if(fileLength == 55){ // Cancels if invalid IGSN file (if file has a length of 0.05 KB)
							AliquotMenuActivity.setInvalidFile(true);	// Sets file as invalid
							cancel(true);
						}else{
							output = new FileOutputStream(Environment.getExternalStorageDirectory() + "/CHRONI/Aliquot/" + fileName + ".xml");
						}
					}else if(fileType.contains("Report Settings")){
						output = new FileOutputStream(Environment.getExternalStorageDirectory() + "/CHRONI/Report Settings/" + fileName + ".xml");
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
//			mProgressDialog.dismiss();
			if (result != null)
				Toast.makeText(context, "Download error: " + result,
						Toast.LENGTH_LONG).show();
			else
				Toast.makeText(context, "File downloaded!", Toast.LENGTH_SHORT)
						.show();
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
}
