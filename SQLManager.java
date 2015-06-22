/* Opponent Notes
 * SQLManager.java
 * Rains Jordan
 * 
 * File description: Abstract class to manage SQL database queries, including insertions
 * and deletions. Interfaces with our SQLiteHelper subclass.
 */

package com.pylonsoflight.oppnotes;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

public abstract class SQLManager {
	protected Context context;
	protected SQLiteHelper helper;
	protected SQLiteDatabase db;
	
	//Store a few details about the database itself.
	static final String DB_FILE = "oppnotes.db";
	static final int DB_VERSION = 2;
	
	public SQLManager(Context context) {
		this.context = context;
	}
	
	public void openDB() throws SQLException {
		//Use our SQLiteHelper class to enable reading from and writing to the database.
		helper = new SQLiteHelper(context, DB_FILE, null, DB_VERSION);
		db = helper.getWritableDatabase();
	}
	
	public void closeDB() {
		//Close the database for reading/writing.
		helper.close();
	}
	
	public void recreateDB() {
		openDB();
		helper.recreateTables(db);
		closeDB();
	}
	
	public String getColumn(String table, int id, String columnName) {
		openDB();
		
		Cursor cursor = db.rawQuery("SELECT " + columnName + " FROM " + table +
		    " WHERE " + SQLiteHelper.DB_ID + "=" + id, null);
		
		if (cursor == null || cursor.getCount() == 0) {
			closeDB();
			return "";
		}
		else {
			cursor.moveToFirst();
			String value = cursor.getString(0);
			closeDB();
			return value;
		}
	}
	
	public Cursor getFirstRowCursor(String query) {
		/* Note that this function, unlike some others here, does not open and close the database
		 * directly, since there's the expectation that further reading will be done. So it's up to
		 * the calling function to manage opening and closing the database. Note that if
		 * getFirstRowCursor() is called on a non-open database, an error will result. */
		
		//Make a query that gets all relevant columns from all rows of the table.
		//Note that the ID is the first column.
		Cursor cursor = db.rawQuery(query, null);
		
		if (cursor != null)
			cursor.moveToFirst();   //Got some results? Move the cursor to the first row of results.
		return cursor;
	}
	
	//TODO remove
	protected void printSQLCounts(Activity activity) {
		openDB();
		
		Cursor cursor1 = db.rawQuery("SELECT count(*) FROM " + SQLiteHelper.DB_SPORT_TABLE, null);
		cursor1.moveToFirst();
		int count1 = cursor1.getInt(0);
		cursor1.close();
		
		Cursor cursor2 = db.rawQuery("SELECT count(*) FROM " + SQLiteHelper.DB_PLAYER_TABLE, null);
		cursor2.moveToFirst();
		int count2 = cursor2.getInt(0);
		cursor2.close();
		
		Cursor cursor3 = db.rawQuery("SELECT count(*) FROM " + SQLiteHelper.DB_H2H_TABLE, null);
		cursor3.moveToFirst();
		int count3 = cursor3.getInt(0);
		cursor3.close();
		
		closeDB();
		
		//TODO remove this whole function, and this too
		Toast.makeText(activity.getApplicationContext(), "Counts: " + count1 + ", " + count2 + ", " + count3, Toast.LENGTH_SHORT).show();
	}
}
