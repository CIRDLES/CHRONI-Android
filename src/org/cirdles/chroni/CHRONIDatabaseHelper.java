package org.cirdles.chroni;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/*
 * This class defines the functionality behind the database.
 */
public class CHRONIDatabaseHelper extends SQLiteOpenHelper {
	public static final String KEY_ROWID = "_id";	// keeps track of row count
	public static final String KEY_DATE = "_date";
	public static final String ALIQUOT_NAME = "aliquot_name";
    public static final String REPORT_SETTINGS_NAME = "report_settings_name";

    private static final String DATABASE_NAME = "MyCHRONIDB";	// name of database
	private static final String DATABASE_TABLE = "viewedAliquotTable";	// name of table used
	private static final int DATABASE_VERSION = 1;

	private int rowNumber = 1;
	
	public CHRONIDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	/*
	 * Only called when database is created
	 * (non-Javadoc)
	 * @see android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite.SQLiteDatabase)
	 */
	public void onCreate(SQLiteDatabase db) {
		// Creates table and adds appropriate rows
		db.execSQL("CREATE TABLE " + DATABASE_TABLE + " (" + 
				KEY_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + 
				KEY_DATE + " TEXT NOT NULL, " +
                        ALIQUOT_NAME + " TEXT NOT NULL, " +
                        REPORT_SETTINGS_NAME + " TEXT NOT NULL);"
		);
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
		// Called to update the created table
		db.execSQL("DROP TABLE IF EXISTS " + DATABASE_NAME);
		
		// creates table again
		onCreate(db);
	}
	
	
	/*
	 * Method that will create an entry in the database.
	 * @params the column values that will be added in the database
	 */
	public void createEntry(String date, String aliquotName, String reportSettingsName){
		SQLiteDatabase db = this.getWritableDatabase();
		
		ContentValues cv = new ContentValues();
		cv.put(KEY_DATE, date);
		cv.put(ALIQUOT_NAME, aliquotName);
        cv.put(REPORT_SETTINGS_NAME, reportSettingsName);

        db.insert(DATABASE_TABLE, null, cv);
		db.close();
	}

	/*
	 * Determines if an Aliquot is present in the database.
	 * @param aliquotName the name of the aliquot that is being searched
	 */
	public boolean isPresent(String aliquotName){
		boolean present = false;
		SQLiteDatabase db = this.getReadableDatabase();
		
		String[] columns = new String[]{KEY_ROWID, KEY_DATE, ALIQUOT_NAME, REPORT_SETTINGS_NAME}; //column names
		Cursor c = db.query(DATABASE_TABLE, columns, ALIQUOT_NAME + " = '" + aliquotName + "'", null, null, null, null);
		
		if(c.toString().contentEquals(aliquotName)){
			present = true;
		}
		c.close();
		return present;
	}
	
//	/*
//	 *  Returns an aliquot name from the database based on the maltPin.
//	 *  For use on My Experiments screen when a certain row is clicked to go to the Add Info screen.
//	 *  @param preloadedString - the string that determines if its preloaded
//	 */
//	public String getEquipmentName(String equipmentName){
//		SQLiteDatabase db = this.getReadableDatabase();
//		
//		String[] columns = new String[]{KEY_ROWID, KEY_EQUIPMENT, ALIQUOT_NAME, KEY_MALTPIN, KEY_TIME, KEY_COMMENTS, KEY_PICTURE, KEY_URL}; //column names
//		Cursor c = db.query(DATABASE_TABLE, columns, KEY_EQUIPMENT + " = '" + equipmentName + "'", null, null, null, null);
//		String result = "";
//		
//		if(c != null){
//			// gets the data from the row requested
//			c.moveToFirst();
//			result = c.getString(1);
//		}
//		c.close();
//		return result;
//	}
//	
//	/*
//	 *  Returns a specific entry from the database based on the rowNumber.
//	 *  For use on History screen when a certain row is clicked to view that table
//	 *  @param rowNumber - the row from the table whose information to return
//	 */
//	public String getData(long rowNumber){
//		SQLiteDatabase db = this.getReadableDatabase();
//		
//		String[] columns = new String[]{KEY_ROWID, KEY_DATE, ALIQUOT_NAME, KEY_SOURCE}; //column names
//		Cursor c = db.query(DATABASE_TABLE, columns, KEY_ROWID + " = " + rowNumber, null, null, null, null);
//		String result = "";
//		
//		if(c != null){
//			// gets the data from the row requested
//			c.moveToFirst();
//			result = result + c.getString(0) + " " + c.getString(1) + " " + c.getString(2) +  c.getString(3) + " "  + "\n" ;
//		}
//		c.close();
//		return result;
//	}
//	
//	/*
//	 *  Returns a sample name from the database based on the maltPin.
//	 *  For use on My Experiments screen when a certain row is clicked to go to the Add Info screen.
//	 *  @param rowNumber - the row from the table whose information to return
//	 */
//	public String getSampleName(String maltPin){
//		SQLiteDatabase db = this.getReadableDatabase();
//		
//		String[] columns = new String[]{KEY_ROWID, KEY_EQUIPMENT, ALIQUOT_NAME, KEY_MALTPIN, KEY_TIME, KEY_COMMENTS, KEY_PICTURE, KEY_URL}; //column names
//		Cursor c = db.query(DATABASE_TABLE, columns, KEY_MALTPIN + " = '" + maltPin + "'", null, null, null, null);
//		String result = "";
//		
//		if(c != null){
//			// gets the data from the row requested
//			c.moveToFirst();
//			result = c.getString(2);
//		}
//		c.close();
//		return result;
//	}
//	
//	
//	/*
//	 * Method returns all data from the Experiment Table of the database.
//	 * 
//	 */
//	public String getAllData() {
//		SQLiteDatabase ourDatabase = this.getReadableDatabase();
//		
//		String[] columns = new String[]{KEY_ROWID, KEY_EQUIPMENT, ALIQUOT_NAME, KEY_MALTPIN, KEY_TIME, KEY_COMMENTS, KEY_PICTURE, KEY_URL}; //column names
//		Cursor c = ourDatabase.query(DATABASE_TABLE, columns, null, null, null, null, null);
//		String result = "";
//
//		// Setting up indices for each column
//		int iRow = c.getColumnIndex(KEY_ROWID);
//		int iEquipment = c.getColumnIndex(KEY_EQUIPMENT);
//		int iName = c.getColumnIndex(ALIQUOT_NAME);
//		int iPin = c.getColumnIndex(KEY_MALTPIN);
//		int iTime = c.getColumnIndex(KEY_TIME);
//		int iComments = c.getColumnIndex(KEY_COMMENTS);
//		int iPicture = c.getColumnIndex(KEY_PICTURE);
//		int iURL = c.getColumnIndex(KEY_URL);
//		
//		// reads all the database data
//		for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext()){
//			result = result + c.getString(iRow) + " " + c.getString(iEquipment) + " " + c.getString(iName) + " " + c.getString(iPin) + " " + c.getString(iTime) + " " 
//					+ c.getString(iComments) + " " +  c.getString(iPicture) + " " +  c.getString(iURL) + "\n" ;
//		}
//		c.close();
////		ourDatabase.close();
//		return result;
//	}
//	
//	/*
//	 * Updates an entry in the database table.
//	 */
//	public void updateEntry(long rowNumber, String machine, String sample, String pin, String time, String comments, String picture, String url){
//		SQLiteDatabase ourDatabase = this.getWritableDatabase();
//		
//		ContentValues cv = new ContentValues();
//		cv.put(KEY_EQUIPMENT, machine);
//		cv.put(ALIQUOT_NAME, sample);
//		cv.put(KEY_MALTPIN, pin);
//		cv.put(KEY_TIME, time);
//		cv.put(KEY_COMMENTS, comments);
//		cv.put(KEY_PICTURE, picture);
//		cv.put(KEY_URL, url); 
//		ourDatabase.update(DATABASE_TABLE, cv, KEY_ROWID + " = " + rowNumber, null);
//	
//		ourDatabase.close();
//	}	

	/*
	 * Method creates a 2D array of all the data found in the database
	 * @return the 2d array generated by the method
	 * 
	 */
	public String[][] fillTableData() {
		SQLiteDatabase ourDatabase = this.getReadableDatabase();

		String[] columns = new String[]{KEY_ROWID, KEY_DATE, ALIQUOT_NAME, REPORT_SETTINGS_NAME}; //column names
		Cursor c = ourDatabase.query(DATABASE_TABLE, columns, null, null, null, null, null);
		
		// Setting up indices for each column
		int iDate = c.getColumnIndex(KEY_DATE);
		int iAliquot = c.getColumnIndex(ALIQUOT_NAME);
        int iReportSettings = c.getColumnIndex(REPORT_SETTINGS_NAME);

        // Sets us the 2d array
		final int ROWS = (int) getTotalEntryCount() + 1; //extra row reserved for header
		final int COLS = 5;
		String[][] databaseTable = new String[ROWS][COLS];
		
		// Fills in the header row
		databaseTable[0][0] = "Last Opened";
		databaseTable[0][1] = "Aliquot";
        databaseTable[0][2] = "Report Settings";
        databaseTable[0][3] = "VIEW";	// empty header for buttons
        databaseTable[0][4] = "DELETE";	// empty header for buttons

        // inserts the data into the 2D array
		rowNumber = 1;
		for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext()){
			int columnNumber = 0;
			databaseTable[rowNumber][columnNumber] = c.getString(iDate);
			columnNumber++;
			databaseTable[rowNumber][columnNumber] = c.getString(iAliquot);
			columnNumber++;
            databaseTable[rowNumber][columnNumber] = c.getString(iReportSettings);
            columnNumber++;
            databaseTable[rowNumber][columnNumber] = " ";
            columnNumber++;
            databaseTable[rowNumber][columnNumber] = " ";
			rowNumber++;
		}		
		c.close();
		return databaseTable;
	}


	/*
	 * Deletes a specific entry from a table
	 */
	public void deleteEntry(long rowNumber)
	{
	    SQLiteDatabase db = this.getWritableDatabase();
	    try
	    {
	        db.delete(DATABASE_TABLE, KEY_ROWID + " = '" + rowNumber + "'", null);
	    }
	    catch(Exception e)
	    {
	        e.printStackTrace();
	    }
	    finally
	    {
	        db.close();
	    }
	}
	
	/*
	 * Returns the number of entries currently in the database.
	 */
	public int getEntryCount(){
		return rowNumber + 1;
	}
	
	/*
	 * Method figures out the number of entries in the entire database.
	 */
	public long getTotalEntryCount(){
		String countQuery = "SELECT * FROM " + DATABASE_TABLE;
		SQLiteDatabase ourDatabase = this.getReadableDatabase();
		Cursor cursor = ourDatabase.rawQuery(countQuery, null);
//		cursor.close();
		return cursor.getCount(); 
	}
	
	/*
	 * Checks to see if information is in the database.
	 */
	public boolean isEmpty(){
		if(getTotalEntryCount() == 0){
			return true;
		}
		return false;
	}
	
}
