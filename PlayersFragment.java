/* Opponent Notes
 * PlayersFragment.java
 * Rains Jordan
 * 
 * File description: Allows entry and deletion of info for players. Displays retrieved data in a table.
 */

package com.pylonsoflight.oppnotes;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class PlayersFragment extends BaseFragment implements OnClickListener, OnTouchListener, OnFocusChangeListener {
	public static final String ID_MESSAGE = BaseActivity.PACKAGE_NAME + ".ID_MESSAGE";

	SportSQLManager sportSQLManager;
	
	private EditText nameEdit, diffEdit, descEdit;
	private Button addPlayerButton;
	private TableLayout tableLayout;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setHasOptionsMenu(true);
        
        FragmentActivity activity = getActivity();
		playerSQLManager = new PlayerSQLManager(activity);
		h2hSQLManager = new H2HSQLManager(activity);
		sportSQLManager = new SportSQLManager(activity);
		
		//playerSQLManager.recreateDB();   //TODO remove
		//askRecreateDB();   //TODO remove; for testing
		//playerSQLManager.printSQLCounts(getActivity());   //TODO remove
	}
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_players, container, false);
        rootView.setOnTouchListener(this);   //TODO why?
        
		nameEdit = (EditText)rootView.findViewById(R.id.playerNameEdit);
		diffEdit = (EditText)rootView.findViewById(R.id.playerDiffEdit);
		descEdit = (EditText)rootView.findViewById(R.id.playerDescEdit);
		addPlayerButton = (Button)rootView.findViewById(R.id.addPlayerButton);
		tableLayout = (TableLayout)rootView.findViewById(R.id.tableLayout);
		
		addPlayerButton.setOnClickListener(this);
		tableLayout.setOnClickListener(this);
		
		descEdit.setOnFocusChangeListener(this);
		
		rootView.findViewById(R.id.scrollView).setOnClickListener(this);
		rootView.findViewById(R.id.clickableLayout1).setOnClickListener(this);
		rootView.findViewById(R.id.clickableLayout2).setOnClickListener(this);
		
		buildTable();
		
        return rootView;
    }
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.players, menu);
		
	    menu.findItem(R.id.action_sort_ascending).setChecked(true);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		    case R.id.action_sort_name:
		    	if (!playerSQLManager.sortColumn.equals(SQLiteHelper.DB_NAME)) {
		    		playerSQLManager.sortColumn = SQLiteHelper.DB_NAME;
		    		rebuildTable();
		    	}
		    	return true;
		    case R.id.action_sort_difficulty:
		    	if (!playerSQLManager.sortColumn.equals(SQLiteHelper.DB_DIFFICULTY)) {
		    		playerSQLManager.sortColumn = SQLiteHelper.DB_DIFFICULTY;
		    		rebuildTable();
				}
		    	return true;
		    case R.id.action_sort_date_added:
		    	if (!playerSQLManager.sortColumn.equals(SQLiteHelper.DB_ID)) {
		    		playerSQLManager.sortColumn = SQLiteHelper.DB_ID;
		    		rebuildTable();
				}
		    	return true;
		    case R.id.action_sort_ascending:
		    	boolean bChecked = !item.isChecked();
		    	item.setChecked(bChecked);
		    	if (playerSQLManager.bSortAscending != bChecked) {
		    		playerSQLManager.bSortAscending = bChecked;
		    		rebuildTable();
				}
		    	return true;
		    default:
		    	return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	public void onClick(View v) {
        switch (v.getId()) {
	        case R.id.addPlayerButton:
	        	addPlayer();
	        	hideKeyboard();
	        	break;
	        case R.id.tableLayout:
	        	hideKeyboard();
	        	break;
	        case R.id.scrollView:
	        	hideKeyboard();
	        	break;
	        default:   //Includes various layouts that might be clicked on.
	        	hideKeyboard();
	        	break;
        }
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
	    int eventAction = event.getAction();
	    switch (eventAction) {
	        case MotionEvent.ACTION_DOWN:   //We clicked the "background" of the activity.
	        	hideKeyboard();
	            break;
	        default:
	            break;
	    }
	    
	    return true;
	}
	
	@Override
    public void onFocusChange(View v, boolean hasFocus)
    {
    	final int viewID = v.getId();
    	switch (viewID) {
	    	case R.id.playerDescEdit:
		        if (hasFocus && descEdit.getText().length() == 0) {
		        	final String text = sportSQLManager.getTemplate(MainActivity.currentSportID);
		        	if (text.length() != 0)
		        	{
						AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
						
						alertDialog.setMessage(getString(R.string.ask_load_template));
				    	
						alertDialog.setTitle(getString(R.string.app_name));
						
						alertDialog.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
					        public void onClick(DialogInterface dialog, int which) {
					        	try {
					        		descEdit.setText(text);
					        	}
					    		catch (Exception e) {
					    			askRecreateDB();
					    		}
					        }
					    });
						
						alertDialog.setNegativeButton(getString(R.string.no), null);
						
						alertDialog.create().show();
		        	}
		        }
		    default:
		    	break;
    	}
	}
	
	private void buildTable() {
		try {
			FragmentActivity activity = getActivity();
			
			//Each time we build the table, enable or disable the widgets that allow player creation.
			
			if (MainActivity.currentSportID == 0) {   //Invalid sport.
				//TODO remove this whole section
				//SettingsSQLManager settingsSQLManager = new SettingsSQLManager(activity);
				//Toast.makeText(activity.getApplicationContext(), "NO CURRENT, but maybe should load spinner " + settingsSQLManager.getDefaultSportsSpinnerItem(), Toast.LENGTH_SHORT).show();
				
				nameEdit.setEnabled(false);
				nameEdit.setClickable(false);
				diffEdit.setEnabled(false);
				diffEdit.setClickable(false);
				descEdit.setEnabled(false);
				descEdit.setClickable(false);
				addPlayerButton.setEnabled(false);
				addPlayerButton.setClickable(false);
				
				//Add a temporary error message to the table.
				
				TableRow fakeTableRow = new TableRow(activity);
				TextView messageText = new TextView(activity);
				messageText.setText(getString(R.string.select_sport_error));
				//messageText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_normal));
				messageText.setTypeface(null, Typeface.ITALIC);
				messageText.setPadding(10, 6, 10, 0);
				
				//Make text wrap if it exceeds screen width.
				TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
				messageText.setLayoutParams(layoutParams);
				
				fakeTableRow.addView(messageText);
				
				tableLayout.addView(fakeTableRow);
				
				return;
			}
			else {
				nameEdit.setEnabled(true);
				nameEdit.setClickable(true);
				diffEdit.setEnabled(true);
				diffEdit.setClickable(true);
				descEdit.setEnabled(true);
				descEdit.setClickable(true);
				addPlayerButton.setEnabled(true);
				addPlayerButton.setClickable(true);
			}
			
			
			//Prepare to build the table.
			
			playerSQLManager.openDB();
			Cursor cursor = playerSQLManager.getFirstRowCursor(MainActivity.currentSportID);
			
			int rows = cursor.getCount();
			int columns = cursor.getColumnCount();
			
			final Context origContext = activity;
			
			tableLayout.setColumnShrinkable(PlayerSQLManager.VISIBLE_NAME_COLUMN, true);
			
			
			//Add table headers, in the top row.
			
			TableRow firstTableRow = new TableRow(activity);
			
			//Add an empty cell, to match the remove buttons below.
			TextView text1 = new TextView(activity);
			firstTableRow.addView(text1);
			
			TextView text2 = new TextView(activity);
			text2.setText(getString(R.string.name));
			//text2.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_normal));
			text2.setTypeface(null, Typeface.BOLD);
			text2.setPadding(10, 6, 10, 0);
			firstTableRow.addView(text2);
			
			TextView text3 = new TextView(activity);
			text3.setText(getString(R.string.difficulty));
			//text3.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_normal));
			text3.setTypeface(null, Typeface.BOLD);
			text3.setPadding(10, 6, 10, 0);
			firstTableRow.addView(text3);
			
			TextView text4 = new TextView(activity);
			text4.setText(getString(R.string.h2h_table_header));
			//text4.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_normal));
			text4.setTypeface(null, Typeface.BOLD);
			text4.setPadding(10, 6, 10, 0);
			firstTableRow.addView(text4);
			
			tableLayout.addView(firstTableRow);
			
			
			//Add the rest of the rows.
			
			for (int r = 0; r < rows; ++r) {
				TableRow tableRow = new TableRow(activity);
				tableLayout.addView(tableRow);
				
				int playerID = 0;   //0 is an unused default row ID, according to SQLite and all other databases. We will never generate it automatically.
				
				for (int c = 0; c < columns; ++c) {
					switch(c) {
					case PlayerSQLManager.ID_COLUMN:   //The first column, the ID of this row in the database.
						ImageButton removeButton = new ImageButton(activity);
						removeButton.setImageResource(R.drawable.remove_button);
						
						removeButton.setBackgroundResource(0);   //Remove the default background color.
						
						playerID = cursor.getInt(c);   //We know the player ID is the first column of a row. Note that this is standard database practice.
						final int finalPlayerID = playerID;   //Allow this to be referred to in listener functions.
						
						//Create an improvised OnClickListener for our Remove button.
						removeButton.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								removePlayer(finalPlayerID);
							}
						});
						
						tableRow.addView(removeButton);
						break;
					
					case PlayerSQLManager.SPORT_ID_COLUMN:
						break;
					
					default:
						TextView text = new TextView(activity);
						
						if (c == PlayerSQLManager.DIFFICULTY_COLUMN) {
							TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
							layoutParams.setMargins(44, 0, 0, 25);
							text.setLayoutParams(layoutParams);
						}
						
						if (c == PlayerSQLManager.H2H_COLUMN)
							text.setText(h2hSQLManager.getTotalResults(playerID));
						else
							text.setText(cursor.getString(c));
						
						//text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_normal));
						
						text.setPadding(10, 6, 10, 0);
						
						tableRow.addView(text);
						
						break;
					}
				}

				//Wire the table row to show an intent displaying player data.
				
				final int finalPlayerIDAgain = playerID;   //We need a final copy, different from the one in the other case statement.
				
				tableRow.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(origContext, DetailsActivity.class);
						intent.putExtra(ID_MESSAGE, Integer.toString(finalPlayerIDAgain));
						
						//We need to a way to find out when the activity has finished, so we'll make it so we get a result from it.
						startActivityForResult(intent, BaseActivity.REQUEST_CODE);
					}
				});
				
				cursor.moveToNext();   //Get the next row from the database query.
			}
			
			playerSQLManager.closeDB();
		}
		catch (Exception e) {   //No matter what, if the program is crashing, I want the user to have the option to reset the database.
			askRecreateDB();
		}
	}
	
	protected void rebuildTable() {
		//playerSQLManager.printSQLCounts(getActivity());   //TODO remove
		tableLayout.removeAllViews();   //Delete contents of table.
		buildTable();
	}
	
	//TODO: Remove, unnecessary.
	/*
	public void refreshDescription() {
		if (MainActivity.currentSportID != 0) {
			if (descEdit.getText().length() == 0) {
				String text = sportSQLManager.getTemplate(MainActivity.currentSportID);
				descEdit.setText(text);
			}
		}
	}
	
	public void refreshAndRebuild() {
		refreshDescription();
		rebuildTable();
	}
	*/
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		//We just got a result back from a newly-closed DetailsActivity. In other words, it was just closed.
		//We don't care what the result was, but we do want to refresh the table, in case edits were made.
	    super.onActivityResult(requestCode, resultCode, data);
	    rebuildTable();
	}
	
	private void addPlayer() {
		try {
			String name = nameEdit.getText().toString();
			if (name.length() == 0) {
	        	showDialog(getString(R.string.name_error));
	        	return;
			}
			
			Double.parseDouble(diffEdit.getText().toString());   //See if we get a conversion exception.
			String difficulty = diffEdit.getText().toString();
			
			String description = descEdit.getText().toString();
        	
			playerSQLManager.insertRow(MainActivity.currentSportID, name, difficulty, description);
			
			rebuildTable();
			
			nameEdit.setText("");
			diffEdit.setText("");
			descEdit.setText("");
		}
        catch (NumberFormatException e) {
        	//Invalid difficulty.
        	showDialog(getString(R.string.difficulty_error));
        }
		catch (Exception e) {
			askRecreateDB();
		}
	}
	
	private void removePlayer(final int id) {
		try {
			AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
			
			alertDialog.setMessage(getString(R.string.remove_player_message_start) + playerSQLManager.getName(id) +
			    getString(R.string.remove_player_message_end));
	    	
			alertDialog.setTitle(getString(R.string.app_name));
			
			alertDialog.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int which) {
		        	try {
		        		playerSQLManager.deleteRow(id, h2hSQLManager);
		        		
		        		rebuildTable();
		        	}
		    		catch (Exception e) {
		    			askRecreateDB();
		    		}
		        }
		    });
			
			alertDialog.setNegativeButton(getString(R.string.cancel), null);
			
			alertDialog.create().show();
		}
		catch (Exception e) {
			askRecreateDB();
		}
	}
}