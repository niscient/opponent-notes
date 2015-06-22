/* Opponent Notes
 * PlayerSQLManager.java
 * Rains Jordan
 * 
 * File description: Class to manage Player table SQL database queries.
 */

package com.pylonsoflight.oppnotes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class PlayerSQLManager extends SQLManager {
	public String sortColumn;
	public boolean bSortAscending;
	
	//Player table column constants.
	static final int ID_COLUMN = 0;   //This is the index of the ID column, as specified by the order used when we create our database table.
	static final int SPORT_ID_COLUMN = 1;
	static final int NAME_COLUMN = 2;
	static final int DIFFICULTY_COLUMN = 3;
	static final int H2H_COLUMN = 4;
	
	//Visible position of particular columns, for printing purposes.
	static final int VISIBLE_NAME_COLUMN = 1;
	
	public PlayerSQLManager(Context context) {
		super(context);
		
		/* Set these as the default sorting options, to match the starting RadioButton and Checkbox
		 * values set in the XML file. */
		sortColumn = SQLiteHelper.DB_NAME;
		bSortAscending = true;
	}
	
	public int insertRow(int sportID, String name, String difficulty, String description) {
		openDB();
		
		ContentValues contentValues = new ContentValues();
		contentValues.put(SQLiteHelper.DB_SPORT_ID, sportID);
		contentValues.put(SQLiteHelper.DB_NAME, name);
		contentValues.put(SQLiteHelper.DB_DIFFICULTY, difficulty);
		contentValues.put(SQLiteHelper.DB_DESCRIPTION, description);
		long rowID = db.insert(SQLiteHelper.DB_PLAYER_TABLE, null, contentValues);
		
		closeDB();
		return (int)rowID;
	}
	
	public boolean deleteRow(int id, H2HSQLManager h2hSQLManager) {
		//Note: There's no real reason not to just create new SQL managers, rather than passing them
		//in, but it helps remind us that these tables will be modified.
		
		openDB();
		
		boolean bDeleted = db.delete(SQLiteHelper.DB_PLAYER_TABLE, SQLiteHelper.DB_ID + "=" + id, null) > 0;
		
		closeDB();
		
		//Delete all head-to-head encounters associated with the player.
		if (bDeleted)
			h2hSQLManager.deleteRowsWithPlayerID(id);
		
		return bDeleted;
	}
	
	public boolean deleteRowsWithSportID(int sportID, H2HSQLManager h2hSQLManager) {
		//Note: There's no real reason not to just create new SQL managers, rather than passing them
		//in, but it helps remind us that these tables will be modified.
		//Indeed, in this case, we're not actually even going to use the reference to
		//H2HSQLManager at all, due to implementation details!
		
		openDB();
		
		db.execSQL("DELETE FROM " + SQLiteHelper.DB_H2H_TABLE + " WHERE " + SQLiteHelper.DB_PLAYER_ID +
			" IN (SELECT " + SQLiteHelper.DB_ID + " FROM " + SQLiteHelper.DB_PLAYER_TABLE + " WHERE " +
			SQLiteHelper.DB_SPORT_ID + "=" + sportID + ")");
		
		boolean bDeleted = db.delete(SQLiteHelper.DB_PLAYER_TABLE, SQLiteHelper.DB_SPORT_ID + "=" + sportID, null) > 0;
		
		closeDB();
		
		return bDeleted;
	}

	public String getColumn(int id, String columnName) {
		return super.getColumn(SQLiteHelper.DB_PLAYER_TABLE, id, columnName);
	}
	
	public String getSportID(int id) {
		return getColumn(id, SQLiteHelper.DB_SPORT_ID);
	}
	
	public String getName(int id) {
		return getColumn(id, SQLiteHelper.DB_NAME);
	}
	
	public String getDifficulty(int id) {
		return getColumn(id, SQLiteHelper.DB_DIFFICULTY);
	}
	
	public String getDescription(int id) {
		return getColumn(id, SQLiteHelper.DB_DESCRIPTION);
	}
	
	public void updateSportID(int id, int sportID) {
		openDB();
		
		ContentValues contentValues = new ContentValues();
		contentValues.put(SQLiteHelper.DB_SPORT_ID, sportID);
		db.update(SQLiteHelper.DB_PLAYER_TABLE, contentValues, SQLiteHelper.DB_ID + "=" + id, null);
		
		closeDB();
	}
	
	public void updateName(int id, String name) {
		openDB();
		
		ContentValues contentValues = new ContentValues();
		contentValues.put(SQLiteHelper.DB_NAME, name);
		db.update(SQLiteHelper.DB_PLAYER_TABLE, contentValues, SQLiteHelper.DB_ID + "=" + id, null);
		
		closeDB();
	}
	
	public void updateDifficulty(int id, String difficulty) {
		openDB();
		
		ContentValues contentValues = new ContentValues();
		contentValues.put(SQLiteHelper.DB_DIFFICULTY, difficulty);
		db.update(SQLiteHelper.DB_PLAYER_TABLE, contentValues, SQLiteHelper.DB_ID + "=" + id, null);
		
		closeDB();
	}
	
	public void updateDescription(int id, String description) {
		openDB();
		
		ContentValues contentValues = new ContentValues();
		contentValues.put(SQLiteHelper.DB_DESCRIPTION, description);
		db.update(SQLiteHelper.DB_PLAYER_TABLE, contentValues, SQLiteHelper.DB_ID + "=" + id, null);
		
		closeDB();
	}
	
	public Cursor getFirstRowCursor(int sportID) {
		String sortStr = " ORDER BY ";
		
		if (sortColumn.equals(SQLiteHelper.DB_DIFFICULTY)) {
			sortStr += "CAST(" + sortColumn + " AS int)";   //Sort results properly, as integers.
		}
		else {
			sortStr += sortColumn;
			
			if (sortColumn.equals(SQLiteHelper.DB_NAME))
				sortStr += " COLLATE NOCASE";   //Ignore upper/lower case when ordering results.
		}
		
		if (bSortAscending)
			sortStr += " ASC";
		else
			sortStr += " DESC";
		
		return super.getFirstRowCursor("SELECT * FROM " + SQLiteHelper.DB_PLAYER_TABLE +
			" WHERE " + SQLiteHelper.DB_SPORT_ID + "=" + sportID + sortStr);
	}
    
    /*
    //TODO remove, this works fine but we always want the looping version these days.
    public NextAndPrevIDs getNextAndPrevIDs(int playerID, int sportID) {
    	//Returns 0 at endpoints. Assumes that the player and sport IDs are valid.
    	
    	NextAndPrevIDs ids = new NextAndPrevIDs();
    	
		//Get the previous and next IDs that are adjacent to this ID in the query results (and
		//remember that we're sorting by name, in the PlayerSQLManager's query).
		
    	openDB();
		Cursor cursor = playerSQLManager.getFirstRowCursor(sportID);
		int rows = cursor.getCount();
		
		//Note: 0 is an unused default row ID, according to SQLite and all other databases.
		//We will never generate it automatically.
		ids.prevID = 0;
		ids.nextID = 0;
		
		//Go through the query until we hit our chosen player ID.
		//(The reason for jumping through hoops like this is to use any means necessary not to have
		//multiple cursors or connections to the same database open simultaneously.)
		for (int r = 0; r < rows; ++r) {
			int tempID = cursor.getInt(PlayerSQLManager.ID_COLUMN);
			if (tempID == playerID) {
				//Get next row ID.
				if (r < rows - 1) {
					cursor.moveToNext();
					ids.nextID = cursor.getInt(PlayerSQLManager.ID_COLUMN);
				}
				break;
			}
			ids.prevID = tempID;
			cursor.moveToNext();
		}
		
		closeDB();
		
		return ids;
    }
    */
    
    public NextAndPrevIDs getNextAndPrevIDs(int sportID, int playerID) {
    	//Loops around to the other end of the table at endpoints. Assumes that the player ID and
    	//sport IDs are valid.
    	
    	NextAndPrevIDs ids = new NextAndPrevIDs();
    	
		//Get the previous and next IDs that are adjacent to this ID in the query results (and
		//remember that we're sorting by name, in the PlayerSQLManager's query).
		
    	openDB();
		Cursor cursor = getFirstRowCursor(sportID);
		int rows = cursor.getCount();
		
		//Note: 0 is an unused default row ID, according to SQLite and all other databases.
		//We will never generate it automatically.
		ids.prevID = 0;
		ids.nextID = 0;
		
		//Note that we'll only bother computing the last row ID if we never find a previous ID (in other words,
		//if the row we want is the first row).
		int firstRowID = 0, lastRowID = 0;
		
		boolean bFound = false;
		
		//Go through the query until we hit our chosen player ID.
		//(The reason for jumping through hoops like this is to use any means necessary not to have
		//multiple cursors or connections to the same database open simultaneously.)
		for (int r = 0; r < rows; ++r) {
			int tempID = cursor.getInt(PlayerSQLManager.ID_COLUMN);
			if (r == 0)
				firstRowID = tempID;
			if (r == rows - 1)   //Note that this is not in an "else if", in case there's only 1 player total.
				lastRowID = tempID;
			
			if (!bFound) {
				if (tempID == playerID) {
					bFound = true;
					//Get next row ID.
					if (r < rows - 1) {
						cursor.moveToNext();
						ids.nextID = cursor.getInt(PlayerSQLManager.ID_COLUMN);
					}
					if (ids.prevID != 0)
						break;
					
					//If we didn't just break, we found a match, but no previous ID. So we'll wait until the end
					//of the loop and grab the last row ID.
					continue;
				}
				else
					ids.prevID = tempID;
			}
			cursor.moveToNext();
		}
		
		if (ids.prevID == 0)
			ids.prevID = lastRowID;
		if (ids.nextID == 0)
			ids.nextID = firstRowID;
		
		closeDB();
		
		return ids;
    }
	
	public int getCount(int sportID) {
		openDB();
		
		Cursor cursor = db.rawQuery("SELECT count(*) FROM " + SQLiteHelper.DB_PLAYER_TABLE + " WHERE "
			+ SQLiteHelper.DB_SPORT_ID + "=" + sportID, null);
		cursor.moveToFirst();
		int count = cursor.getInt(0);
		cursor.close();
		
		closeDB();
		return count;
	}
	
	public int getQueryPosition(int sportID, int playerID) {
		//Return the position of the player ID, in the query results.
		
		String sortStr = " ORDER BY ";
		
		if (sortColumn.equals(SQLiteHelper.DB_DIFFICULTY)) {
			sortStr += "CAST(" + sortColumn + " AS int)";   //Sort results properly, as integers.
		}
		else {
			sortStr += sortColumn;
			
			if (sortColumn.equals(SQLiteHelper.DB_NAME))
				sortStr += " COLLATE NOCASE";   //Ignore upper/lower case when ordering results.
		}
		
		if (bSortAscending)
			sortStr += " ASC";
		else
			sortStr += " DESC";

		openDB();
		
		Cursor cursor = super.getFirstRowCursor("SELECT " + SQLiteHelper.DB_ID + " FROM " +
			SQLiteHelper.DB_PLAYER_TABLE + " WHERE " + SQLiteHelper.DB_SPORT_ID + "=" + sportID + sortStr);
		
		int rows = cursor.getCount();
		
		for (int r = 0; r < rows; ++r) {
			if (cursor.getInt(0) == playerID) {
				closeDB();
				return r;
			}
			
			cursor.moveToNext();
		}
		
		closeDB();
		return -1;   //No match.
	}
}
