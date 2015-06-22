/* Opponent Notes
 * SQLiteHelper.java
 * Rains Jordan
 * 
 * File description: SQLite helper class. The standard method of dealing with SQLite databases is to
 *  create a subclass of SQLiteOpenHelper and overwrite onCreate() and onUpgrade().
 *  
 * Notes:
 *  Note that 'difficulty' is stored as text. This is so that, in future versions of the program, if I
 *  wanted to, I could extend it to allow "Very Hard" and things of that nature as difficulty settings,
 *  without destroying the existing database.
 */

package com.pylonsoflight.oppnotes;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLiteHelper extends SQLiteOpenHelper {
	//Store table and field names (keeping them in static constants like this is standard practice).
	
	public static final String DB_SETTINGS_TABLE = "Settings";
	public static final String DB_DEFAULT_SPORTS_SPINNER_ITEM = "default_sports_spinner_item";
	
	public static final String DB_SPORT_TABLE = "Sport";
	public static final String DB_ID = "id";
	public static final String DB_SPORT = "sport";
	public static final String DB_SPORT_TEMPLATE = "template";
	
	public static final String DB_PLAYER_TABLE = "Player";
	public static final String DB_SPORT_ID = "sportid";
	public static final String DB_NAME = "name";
	public static final String DB_DIFFICULTY = "difficulty";
	public static final String DB_DESCRIPTION = "description";
	
	public static final String DB_H2H_TABLE = "H2H";
	public static final String DB_PLAYER_ID = "playerid";
	public static final String DB_DATE = "date";
	public static final String DB_RESULT = "result";
	public static final String DB_NOTES = "notes";
	
	public SQLiteHelper(Context context, String name, CursorFactory factory, int version) {
		super(context, name, factory, version);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		/* Note: onCreate() only runs when we first create the database file, but I'm still calling
		 * createTables() rather than recreateTables() here since I think splitting up the two functions
		 * is logical, and the more logical of the functions to call here is createTables(). */
		createTables(db);
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		//TODO: Remove: Recreate the tables when upgrading to a new version of the database.
		//recreateTables(db);
		
		switch(oldVersion) {
			case 1:
				db.execSQL("ALTER TABLE " + DB_SPORT_TABLE + " ADD COLUMN " + DB_SPORT_TEMPLATE +
					" TEXT DEFAULT ''");
				//Fall through to next case, if there is one.
			default:
				break;
		}
	}
	
	public void createTables(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE IF NOT EXISTS " + DB_SETTINGS_TABLE + " (" +
			DB_DEFAULT_SPORTS_SPINNER_ITEM + " INTEGER)");
		db.execSQL("INSERT INTO " + DB_SETTINGS_TABLE + " VALUES(" + 0 + ")");
		//Note: I won't bother enforcing that there only (or always) be one Settings table row.
		//I'll trust the app not to tinker with it.
		
		db.execSQL("CREATE TABLE IF NOT EXISTS " + DB_SPORT_TABLE + " (" + DB_ID +
			" INTEGER PRIMARY KEY AUTOINCREMENT, " + DB_SPORT + " TEXT NOT NULL, " + DB_SPORT_TEMPLATE +
			" TEXT DEFAULT '')");
		
		db.execSQL("CREATE TABLE IF NOT EXISTS " + DB_PLAYER_TABLE + " (" + DB_ID +
		    " INTEGER PRIMARY KEY AUTOINCREMENT, " + DB_SPORT_ID + " INTEGER, " + DB_NAME +
		    " TEXT NOT NULL, " + DB_DIFFICULTY + " TEXT NOT NULL, " + DB_DESCRIPTION + " TEXT, " +
		    "FOREIGN KEY(" + DB_SPORT_ID + ") REFERENCES " + DB_SPORT_TABLE + "(" +
		    DB_ID + "))");
		
		db.execSQL("CREATE TABLE IF NOT EXISTS " + DB_H2H_TABLE + " (" + DB_ID +
			    " INTEGER PRIMARY KEY AUTOINCREMENT, " + DB_PLAYER_ID + " INTEGER, " +
			    DB_DATE + " TEXT NOT NULL, " + DB_RESULT + " BOOLEAN, " + DB_NOTES + " TEXT, " +
			    "FOREIGN KEY(" + DB_PLAYER_ID + ") REFERENCES " + DB_PLAYER_TABLE + "(" +
			    DB_ID + "))");
	}
	
	public void recreateTables(SQLiteDatabase db) {
		//Remove the old tables, if they exist.
		db.execSQL("DROP TABLE IF EXISTS " + DB_SETTINGS_TABLE);
		db.execSQL("DROP TABLE IF EXISTS " + DB_SPORT_TABLE);
		db.execSQL("DROP TABLE IF EXISTS " + DB_PLAYER_TABLE);
		db.execSQL("DROP TABLE IF EXISTS " + DB_H2H_TABLE);
		createTables(db);
	}
}
