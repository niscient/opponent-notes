/* Opponent Notes
 * SportSQLManager.java
 * Rains Jordan
 * 
 * File description: Class to manage Sport table SQL database queries.
 */

package com.pylonsoflight.oppnotes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class SportSQLManager extends SQLManager {
	//Sport table column constants.
	static final int ID_COLUMN = 0;
	static final int SPORT_COLUMN = 1;
	
	public SportSQLManager(Context context) {
		super(context);
	}
	
	public int insertRow(String sport) {
		openDB();
		
		ContentValues contentValues = new ContentValues();
		contentValues.put(SQLiteHelper.DB_SPORT, sport);
		long rowID = db.insert(SQLiteHelper.DB_SPORT_TABLE, null, contentValues);
		
		closeDB();
		return (int)rowID;
	}
	
	public boolean deleteRow(int id, PlayerSQLManager playerSQLManager, H2HSQLManager h2hSQLManager) {
		openDB();
		
		boolean bDeleted = db.delete(SQLiteHelper.DB_SPORT_TABLE, SQLiteHelper.DB_ID + "=" + id, null) > 0;
		
		closeDB();
		
		//Delete all players (and associated head-to-head encounters) associated with the sport.
		if (bDeleted)
			playerSQLManager.deleteRowsWithSportID(id, h2hSQLManager);
		
		return bDeleted;
	}
	
	public String getColumn(int id, String columnName) {
		return super.getColumn(SQLiteHelper.DB_SPORT_TABLE, id, columnName);
	}
	
	public String getSport(int id) {
		return getColumn(id, SQLiteHelper.DB_SPORT);
	}
	
	public String getTemplate(int id) {
		return getColumn(id, SQLiteHelper.DB_SPORT_TEMPLATE);
	}
	
	public void updateSport(int id, String sport) {
		openDB();
		
		ContentValues contentValues = new ContentValues();
		contentValues.put(SQLiteHelper.DB_SPORT, sport);
		db.update(SQLiteHelper.DB_SPORT_TABLE, contentValues, SQLiteHelper.DB_ID + "=" + id, null);
		
		closeDB();
	}
	
	public void updateTemplate(int id, String template) {
		openDB();
		
		ContentValues contentValues = new ContentValues();
		contentValues.put(SQLiteHelper.DB_SPORT_TEMPLATE, template);
		db.update(SQLiteHelper.DB_SPORT_TABLE, contentValues, SQLiteHelper.DB_ID + "=" + id, null);
		
		closeDB();
	}
	
	public Cursor getFirstRowCursor() {
		return super.getFirstRowCursor("SELECT * FROM " + SQLiteHelper.DB_SPORT_TABLE);
	}
	
	public int getFirstID() {
		openDB();
		
		Cursor cursor = db.rawQuery("SELECT " + SQLiteHelper.DB_ID + " FROM " +
			SQLiteHelper.DB_SPORT_TABLE, null);
		
		if (cursor == null || cursor.getCount() == 0) {
			closeDB();
			return 0;   //Invalid row ID, indicating that no first ID exists.
		}
		else {
			cursor.moveToFirst();
			int value = cursor.getInt(0);   //TODO: If I ever turn IDs into longs, translate this too (use getLong()).
			closeDB();
			return value;
		}
	}
	
	public int getNthID(int n) {
		//Note that 0 counts as a valid n index (the first row ID).
		
		int moveCount = n;
		
		openDB();
		
		Cursor cursor = db.rawQuery("SELECT " + SQLiteHelper.DB_ID + " FROM " +
			SQLiteHelper.DB_SPORT_TABLE, null);
		
		if (cursor == null || cursor.getCount() == 0) {
			closeDB();
			return 0;   //Invalid row ID, indicating that no first ID exists.
		}
		else {
			cursor.moveToFirst();
			int rows = cursor.getCount();
			
			if (moveCount > rows)
				throw new RuntimeException("Invalid sport row position.");
			
			for (int r = 0; r < moveCount; ++r)
				cursor.moveToNext();
			
			int value = cursor.getInt(0);   //TODO: If I ever turn IDs into longs, translate this too (use getLong()).
			closeDB();
			return value;
		}
	}
	
	/*public int getCount() {
		openDB();
		
		Cursor cursor = db.rawQuery("SELECT count(*) FROM " + SQLiteHelper.DB_SPORT_TABLE, null);
		cursor.moveToFirst();
		int count = cursor.getInt(0);
		cursor.close();
		
		closeDB();
		return count;
	}*/
}
