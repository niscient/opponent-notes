/* Opponent Notes
 * SettingsSQLManager.java
 * Rains Jordan
 * 
 * File description: Class to manage Settings table SQL database queries. Note that it is always
 * assumed that one, and only one, row in this table exists.
 */

package com.pylonsoflight.oppnotes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class SettingsSQLManager extends SQLManager {
	//Settings table column constants.
	static final int DEFAULT_SPORTS_SPINNER_ITEM = 0;
	
	public SettingsSQLManager(Context context) {
		super(context);
	}
	
	public String getColumn(String columnName) {
		openDB();
		
		Cursor cursor = db.rawQuery("SELECT " + columnName + " FROM " +
			SQLiteHelper.DB_SETTINGS_TABLE, null);
		
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
	
	public int getDefaultSportsSpinnerItem() {
		//recreateDB();   //TODO remove.
		return Integer.parseInt(getColumn(SQLiteHelper.DB_DEFAULT_SPORTS_SPINNER_ITEM));
	}
	
	public void setDefaultSportsSpinnerItem(int defaultSportsSpinnerItem) {
		openDB();
		
		ContentValues contentValues = new ContentValues();
		contentValues.put(SQLiteHelper.DB_DEFAULT_SPORTS_SPINNER_ITEM, defaultSportsSpinnerItem);
		db.update(SQLiteHelper.DB_SETTINGS_TABLE, contentValues, null, null);
		
		closeDB();
	}
}
