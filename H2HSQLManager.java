/* Opponent Notes
 * H2HSQLManager.java
 * Rains Jordan
 * 
 * File description: Class to manage H2H (Head-to-Head encounter) table SQL database queries.
 */

package com.pylonsoflight.oppnotes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class H2HSQLManager extends SQLManager {
	//H2H table column constants.
	static final int ID_COLUMN = 0;
	static final int PLAYER_ID_COLUMN = 1;
	static final int DATE_COLUMN = 2;
	static final int RESULT_COLUMN = 3;
	static final int NOTES_COLUMN = 4;
	
	//This is stored SQL data.
	static final int WON_RESULT = 1;
	static final int LOST_RESULT = 0;
	
	public H2HSQLManager(Context context) {
		super(context);
	}
	
	public int insertRow(int playerID, String date, boolean bWonResult, String notes) {
		openDB();
		
		ContentValues contentValues = new ContentValues();
		contentValues.put(SQLiteHelper.DB_PLAYER_ID, playerID);
		contentValues.put(SQLiteHelper.DB_DATE, date);
		contentValues.put(SQLiteHelper.DB_RESULT, bWonResult);
		contentValues.put(SQLiteHelper.DB_NOTES, notes);
		long rowID = db.insert(SQLiteHelper.DB_H2H_TABLE, null, contentValues);
		
		closeDB();
		return (int)rowID;
	}
	
	public boolean deleteRow(int id) {
		openDB();
		
		boolean bDeleted = db.delete(SQLiteHelper.DB_H2H_TABLE, SQLiteHelper.DB_ID + "=" + id, null) > 0;
		
		closeDB();
		return bDeleted;
	}
	
	public boolean deleteRowsWithPlayerID(int playerID) {
		openDB();
		
		boolean bDeleted = db.delete(SQLiteHelper.DB_H2H_TABLE, SQLiteHelper.DB_PLAYER_ID + "=" + playerID, null) > 0;
		
		closeDB();
		return bDeleted;
	}
	
	public String getColumn(int id, String columnName) {
		return super.getColumn(SQLiteHelper.DB_H2H_TABLE, id, columnName);
	}
	
	public String getPlayerID(int id) {
		return getColumn(id, SQLiteHelper.DB_PLAYER_ID);
	}
	
	public String getDate(int id) {
		return getColumn(id, SQLiteHelper.DB_DATE);
	}
	
	public boolean getBWonResult(int id) {
		int value = Integer.parseInt(getColumn(id, SQLiteHelper.DB_RESULT));
		return value == WON_RESULT;
	}
	
	public String getNotes(int id) {
		return getColumn(id, SQLiteHelper.DB_NOTES);
	}
	
	public void updateDate(int id, String date) {
		openDB();
		
		ContentValues contentValues = new ContentValues();
		contentValues.put(SQLiteHelper.DB_DATE, date);
		db.update(SQLiteHelper.DB_H2H_TABLE, contentValues, SQLiteHelper.DB_ID + "=" + id, null);
		
		closeDB();
	}
	
	public void updateResult(int id, boolean bWonResult) {
		openDB();
		
		ContentValues contentValues = new ContentValues();
		contentValues.put(SQLiteHelper.DB_RESULT, bWonResult);
		db.update(SQLiteHelper.DB_H2H_TABLE, contentValues, SQLiteHelper.DB_ID + "=" + id, null);
		
		closeDB();
	}
	
	public void updateNotes(int id, String notes) {
		openDB();
		
		ContentValues contentValues = new ContentValues();
		contentValues.put(SQLiteHelper.DB_NOTES, notes);
		db.update(SQLiteHelper.DB_H2H_TABLE, contentValues, SQLiteHelper.DB_ID + "=" + id, null);
		
		closeDB();
	}
	
	public Cursor getFirstRowCursor(int playerID) {
		//Order the results by date.
		String sortStr = " ORDER BY date(" + SQLiteHelper.DB_DATE + ") DESC";
		
		return super.getFirstRowCursor("SELECT * FROM " + SQLiteHelper.DB_H2H_TABLE +
			" WHERE " + SQLiteHelper.DB_PLAYER_ID + "=" + playerID + sortStr);
	}
	
	public boolean BIDExists(int id) {
		openDB();
		
		boolean bExists = false;
		
		Cursor cursor = db.rawQuery("SELECT * FROM " + SQLiteHelper.DB_H2H_TABLE + " WHERE "
			+ SQLiteHelper.DB_ID + "=" + id, null);
		if (cursor != null && cursor.getCount() != 0) {
			bExists = true;
		}
		
		cursor.close();
		closeDB();
		return bExists;
	}
	
	public String getTotalResults(int playerID) {
		//Return the total head-to-head against this player, in string format.
		
		//Note: I'm sort of leaving the door open for future non-binary results, which is why I use quotes
		//around the result in this query.
		
		openDB();
		
		Cursor wins = db.rawQuery("SELECT COUNT(*) FROM " + SQLiteHelper.DB_H2H_TABLE +
		    " WHERE " + SQLiteHelper.DB_PLAYER_ID + "=" + playerID + " AND " +
		    SQLiteHelper.DB_RESULT + "='" + WON_RESULT + "'", null);
		wins.moveToFirst();
		int winCount = wins.getInt(0);
		wins.close();
		
		Cursor losses = db.rawQuery("SELECT COUNT(*) FROM " + SQLiteHelper.DB_H2H_TABLE +
		    " WHERE " + SQLiteHelper.DB_PLAYER_ID + "=" + playerID + " AND " +
			SQLiteHelper.DB_RESULT + "='" + LOST_RESULT + "'", null);
		losses.moveToFirst();
		int lossCount = losses.getInt(0);
		losses.close();
		
		closeDB();
		
		return String.format("%d-%d", winCount, lossCount);
	}
}
