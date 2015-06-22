/* Opponent Notes
 * DetailsFragment.java
 * Rains Jordan
 * 
 * File description: Details fragment. Allows editing of a player's details, and their head-to-head
 * meetings with the user.
 */

package com.pylonsoflight.oppnotes;

import java.util.Calendar;
import java.lang.Character;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class DetailsFragment extends BaseFragment implements OnClickListener, OnTouchListener, OnFocusChangeListener {
	public static final String ID_FRAGMENT = BaseActivity.PACKAGE_NAME + ".ID_FRAGMENT";
	public static final String ID_HIDE_PREV_REMINDER = BaseActivity.PACKAGE_NAME + ".ID_HIDE_PREV_REMINDER";
	public static final String ID_HIDE_NEXT_REMINDER = BaseActivity.PACKAGE_NAME + ".ID_HIDE_NEXT_REMINDER";
	
	private PlayerSQLManager playerSQLManager;
	private H2HSQLManager h2hSQLManager;
	private int fragmentID;
	private int playerID;
	private int prevID;
	private int nextID;
	
	private boolean bHidePrevReminder;
	private boolean bHideNextReminder;

	private int editingDateEncounterID;
	private View editingDateTextView;
	
	private TextView prevPageReminder;
	private TextView nextPageReminder;
	private Button h2hCloseEditorButton, h2hCloseEditorButton2;
	private EditText nameEdit;
	private EditText diffEdit;
	private EditText descEdit;
	private TextView h2hTotal;
	private Button h2hDateButton;
	private Switch h2hResultSwitch;
	private EditText h2hNotesEdit;
	private TableLayout tableLayout;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FragmentActivity activity = getActivity();
        
		playerSQLManager = new PlayerSQLManager(activity);
		playerSQLManager.sortColumn = SQLiteHelper.DB_NAME;   //It's the default option anyway, but whatever.
		
		h2hSQLManager = new H2HSQLManager(activity);
		
		playerID = prevID = nextID = 0;
		
		Bundle bundle = getArguments();
		if (bundle != null) {
		    fragmentID = bundle.getInt(ID_FRAGMENT);
		    
			int gotPlayerID = bundle.getInt(PlayersFragment.ID_MESSAGE, 0);
			if (gotPlayerID != 0) {
				if (playerID == 0)   //If the player ID was already set manually, do nothing.
					setPlayerID(gotPlayerID);
			}
			
			bHidePrevReminder = bundle.getBoolean(DetailsFragment.ID_HIDE_PREV_REMINDER, false);
			bHideNextReminder = bundle.getBoolean(DetailsFragment.ID_HIDE_NEXT_REMINDER, false);
		}
	}
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_details, container, false);
        rootView.setOnTouchListener(this);   //TODO why?
        
        prevPageReminder = (TextView)rootView.findViewById(R.id.prevPageReminder);
        nextPageReminder = (TextView)rootView.findViewById(R.id.nextPageReminder);
        if (bHidePrevReminder)
        	prevPageReminder.setVisibility(View.INVISIBLE);
        if (bHideNextReminder)
        	nextPageReminder.setVisibility(View.INVISIBLE);
        
        h2hCloseEditorButton = (Button)rootView.findViewById(R.id.h2hCloseEditorButton);
        h2hCloseEditorButton2 = (Button)rootView.findViewById(R.id.h2hCloseEditorButton2);
		nameEdit = (EditText)rootView.findViewById(R.id.detailsNameEdit);
		diffEdit = (EditText)rootView.findViewById(R.id.detailsDiffEdit);
		descEdit = (EditText)rootView.findViewById(R.id.detailsDescEdit);
		h2hTotal = (TextView)rootView.findViewById(R.id.h2hTotal);
		h2hDateButton = (Button)rootView.findViewById(R.id.h2hDate);
		h2hResultSwitch = (Switch)rootView.findViewById(R.id.h2hResult);
		h2hNotesEdit = (EditText)rootView.findViewById(R.id.h2hNotesEdit);
		tableLayout = (TableLayout)rootView.findViewById(R.id.detailsTableLayout);
		
		h2hCloseEditorButton.setOnClickListener(this);
		h2hCloseEditorButton2.setOnClickListener(this);
		h2hDateButton.setOnClickListener(this);
		rootView.findViewById(R.id.h2hAddButton).setOnClickListener(this);
		
		nameEdit.setOnFocusChangeListener(this);
		diffEdit.setOnFocusChangeListener(this);
		descEdit.setOnFocusChangeListener(this);
		
		rootView.findViewById(R.id.detailsScrollView).setOnClickListener(this);
		rootView.findViewById(R.id.clickableDetailsLayout1).setOnClickListener(this);
		
		//Store data for a H2H encounter whose date we might be currently editing.
		editingDateEncounterID = 0;   //Invalid default row ID.
		editingDateTextView = null;
		
		//Load player details.
		loadPlayerData();
		
        return rootView;
	}
	
	@Override
	public void onClick(View v) {
        switch (v.getId()) {
        	case R.id.h2hCloseEditorButton:
        	case R.id.h2hCloseEditorButton2:
        		hideKeyboard();
        		break;
	        case R.id.h2hDate:
	        	DialogFragment dialogFragment = new CustomDatePicker();
	        	dialogFragment.show(getActivity().getFragmentManager(), "custom_date_picker");
	        	break;
	        case R.id.h2hAddButton:
	        	addEncounter();
	        	hideKeyboard();
	        	break;
	        default:   //Includes various layouts that might be clicked on.
	        	hideKeyboard();
	        	break;
        }
	}
	
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
	    	case R.id.detailsNameEdit:
	    		//Fall through to next case.
	    	case R.id.detailsDiffEdit:
	    		//Fall through to next case.
	    	case R.id.detailsDescEdit:
		        if (!hasFocus) {
		        	//The user just took focus off the EditText, clicking somewhere else.
		        	
		        	final String origText;
		        	if (viewID == R.id.detailsNameEdit)
		        		origText = playerSQLManager.getName(playerID);
		        	else if (viewID == R.id.detailsDiffEdit)
		        		origText = playerSQLManager.getDifficulty(playerID);
		        	else   //Handle the description, the only remaining possibility.
		        		origText = playerSQLManager.getDescription(playerID);
		        	
		        	final EditText editText = (EditText)v;
		        	if (origText.equals(editText.getText().toString()))
		        		return;   //No change in string data; cancel.
		        	
		        	//Ask if the user wants to save the changes to the EditText.
		        	
					AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
					
					alertDialog.setMessage(getString(R.string.save_changes));
					alertDialog.setTitle(getString(R.string.app_name));
					
					alertDialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
				        public void onClick(DialogInterface dialog, int which) {
				        	try {
				        		switch(viewID) {
					        		case R.id.detailsNameEdit:
					        			playerSQLManager.updateName(playerID, editText.getText().toString());
					        			
					        			//Refresh next and prev pages (assuming sorting by name).
					        			((DetailsActivity)getActivity()).refreshNextAndPrev();
					        			
					        			break;
					        		case R.id.detailsDiffEdit:
					        			try {
					        				String textStr = editText.getText().toString();
					        				Double.parseDouble(textStr);   //See if we get a conversion exception.
					        				playerSQLManager.updateDifficulty(playerID, textStr);
					        			}
					        	        catch (NumberFormatException e) {
					        	        	//Invalid difficulty.
					        	        	showDialog(getString(R.string.difficulty_error));
					        	        	editText.setText(origText);
					        	        }
					        			break;
					        		default:   //Handle the description, the only other possible case.
					        			playerSQLManager.updateDescription(playerID, editText.getText().toString());
					        			break;
				        		}
				        	}
				    		catch (Exception e) {
				    			askRecreateDB();
				    		}
				        }
				    });
					
					alertDialog.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
				        public void onClick(DialogInterface dialog, int which) {
				        	//To prevent user confusion, we'll restore the original text.
				        	editText.setText(origText);
				        }
				    });
					
					alertDialog.create().show();
		        }
		        break;
		    default:
		    	break;
    	}
    }
	
	//Custom date picker which will set the text value of our h2hDateButton widget, or an encounter date
    //TextView that might be currently being edited.
	class CustomDatePicker extends DialogFragment implements DatePickerDialog.OnDateSetListener{
	    @Override
	    public Dialog onCreateDialog(Bundle savedInstanceState) {
    		Calendar c = Calendar.getInstance();
    		int year = c.get(Calendar.YEAR);
    		int month = c.get(Calendar.MONTH);
    		int day = c.get(Calendar.DAY_OF_MONTH);
	        DatePickerDialog dialog = new DatePickerDialog(getActivity(), this, year, month, day);
			return dialog;
	    }
	    
	    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
	    	String date = new StringBuilder().append(monthOfYear + 1).append("/")
	    		.append(dayOfMonth).append("/").append(year).toString();   //Note: Month is 0 based, thus the + 1.
	    	if (editingDateTextView == null)
	    		h2hDateButton.setText(date);
	    	else {
				String convertedDate = convertPrintableDateToDBFormat(date);
				
				h2hSQLManager.updateDate(editingDateEncounterID, convertedDate);
	    		((TextView)editingDateTextView).setText(date);
	    		
	    		rebuildTable();   //The order of table items might have changed as a result of this.
	    		
	    		//Reset values.
	    		editingDateEncounterID = 0;
	    		editingDateTextView = null;
	    	}
	    }
	}
	
    public void setPlayerID(int playerID) {
    	this.playerID = playerID;
    	
    	NextAndPrevIDs ids = playerSQLManager.getNextAndPrevIDs(MainActivity.currentSportID, this.playerID);
    	prevID = ids.prevID;
    	nextID = ids.nextID;
    }
    
    public void setIDs(int playerID, int prevID, int nextID) {
    	//It's useful to call this to set up the fragment, before the onCreate() method runs. onCreate() will
    	//load the player data itself. If calling this after onCreate(), use setIDsAndLoadPlayerData() instead.
    	
    	this.playerID = playerID;
    	this.prevID = prevID;
    	this.nextID = nextID;
    }
    
    public void setIDsAndLoadPlayerData(int playerID, int prevID, int nextID) {
    	this.playerID = playerID;
    	this.prevID = prevID;
    	this.nextID = nextID;
    	loadPlayerData();
    }
    
    public void loadPlayerData() {
    	if (playerID != 0) {
	    	nameEdit.setText(playerSQLManager.getName(playerID));
	    	diffEdit.setText(playerSQLManager.getDifficulty(playerID));
	    	descEdit.setText(playerSQLManager.getDescription(playerID));
	    	
			rebuildTable();
			refreshH2HTotal();
    	}
	}
    
	private void buildTable() {
		try {
			FragmentActivity activity = getActivity();
			
			h2hSQLManager.openDB();
			Cursor cursor = h2hSQLManager.getFirstRowCursor(playerID);
			
			int rows = cursor.getCount();
			int columns = cursor.getColumnCount();
			
			//Final variables, for use in listeners.
			final Context origContext = activity;
			
			for (int r = 0; r < rows; ++r) {
				TableRow tableRow = new TableRow(activity);
				tableLayout.addView(tableRow);   //Add the table row to the table.
				
				int encounterID = 0;   //0 is an unused default row ID.
				
				for (int c = 0; c < columns; ++c) {
					switch(c) {
					case H2HSQLManager.ID_COLUMN:   //The first column, the ID of this row in the database.
						ImageButton removeButton = new ImageButton(activity);
						removeButton.setImageResource(R.drawable.remove_button);
						
						removeButton.setBackgroundResource(0);   //Remove the default background color.
						
						encounterID = cursor.getInt(c);   //We know the encounter ID is the first column of a row.
						final int finalEncounterID = encounterID;   //Allow this to be referred to in listener functions.
						
						//Create an improvised OnClickListener for our Remove button.
						removeButton.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								removeEncounter(finalEncounterID);
							}
						});
						
						tableRow.addView(removeButton);   //Add the button to the table row.
						break;
					
					case H2HSQLManager.PLAYER_ID_COLUMN:
						break;
					
					case H2HSQLManager.NOTES_COLUMN:
						EditText textEdit = new EditText(activity);
						textEdit.setText(cursor.getString(c));
						//textEdit.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_normal));
						
						//Note that we use TableLayout.LayoutParams, since a TableLayout is our EditText's prospective parent.
						TableLayout.LayoutParams layoutParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT);
						layoutParams.setMargins(44, 0, 0, 25);
						textEdit.setLayoutParams(layoutParams);
						textEdit.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
						tableLayout.addView(textEdit);   //Add it to the table directly (no table row object is involved).
						
						//Store a bunch of values as constants, so we can use them in listener functions.
						final int finalEncounterIDAgain = encounterID;   //We already have a constant copy of this, but it's in a different case label.
						final EditText finalTextEdit = textEdit;
						
						textEdit.setOnFocusChangeListener(new OnFocusChangeListener()
						{
						    @Override
						    public void onFocusChange(View v, boolean hasFocus)
						    {
						        if (!hasFocus) {
						        	//The user just took focus off the EditText, clicking somewhere else.

							    	//Note: If the user clicked on the remove button and deleted the encounter, this will
							    	//trigger the focus change event. But we don't want to ask to save changes, in that case.
						        	if (!h2hSQLManager.BIDExists(finalEncounterIDAgain))
						        		return;
						        	
						        	final String origText = h2hSQLManager.getNotes(finalEncounterIDAgain);
						        	
						        	if (origText.equals(finalTextEdit.getText().toString()))
						        		return;   //The strings are equal, let's get out of here.
						        	
						        	//Ask if the user wants to save the changes to the EditText.
						        	
									AlertDialog.Builder alertDialog = new AlertDialog.Builder(origContext);
									
									alertDialog.setMessage(getString(R.string.save_changes));
									alertDialog.setTitle(getString(R.string.app_name));
									
									alertDialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
								        public void onClick(DialogInterface dialog, int which) {
								        	try {
								        		//Update edited notes.
						        				final String textStr = finalTextEdit.getText().toString();
						        				h2hSQLManager.updateNotes(finalEncounterIDAgain, textStr);
						        				
						        				//TODO maybe figure out and implement better fix:
						        				//Rebuilding the table here is usually unnecessary, but on the off
						        				//chance that the user just clicked on the Add encounter button,
						        				//that will have messed up our text box contents due to rebuilding
						        				//the table. For some quite nutty reason, just setting the
						        				//EditText contents here doesn't help with that, but rebuilding the
						        				//table does.
						        				//Of course, since the Add button's rebuildTable() goes through instantly,
						        				//until the user clicks Yes or No to save changes then the EditText will
						        				//show the OLD text contents, regardless of whether you intend to click
						        				//Yes or No eventually. Quite annoying, really.
						        				rebuildTable();
								        	}
								    		catch (Exception e) {
								    			askRecreateDB();
								    		}
								        }
								    });
									
									alertDialog.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
								        public void onClick(DialogInterface dialog, int which) {
								        	//To prevent user confusion, we'll restore the original text.
								        	finalTextEdit.setText(origText);
								        }
								    });
									
									alertDialog.create().show();
						        }
						    }
						});
						
						break;
					
					default:
						TextView text = new TextView(getActivity());
						String textData;
						
						//Set text data.
						
						if (c == H2HSQLManager.RESULT_COLUMN) {
							//Note: For now and maybe ever, results are binary, thus the logic here.
							boolean bWonResult = cursor.getInt(c) == H2HSQLManager.WON_RESULT;
							textData = (bWonResult) ? getString(R.string.won) : getString(R.string.lost);
						}
						else {
							textData = cursor.getString(c);
							if (c == H2HSQLManager.DATE_COLUMN)
								textData = convertDBDateToPrintableFormat(textData);
						}
						
						text.setText(textData);
						
						//text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_normal));
						
						text.setPadding(10, 6, 10, 0);
						
						tableRow.addView(text);
						
						//Constants, for listener functions.
						final int finalEncounterIDYetAgain = encounterID;   //Same deal as always.
						final TextView finalText = text;
						
						if (c == H2HSQLManager.DATE_COLUMN) {
							text.setOnLongClickListener(new OnLongClickListener() {
				                @Override
				                public boolean onLongClick(View v) {
						        	try {
						        		editingDateTextView = finalText;
						        		editingDateEncounterID = finalEncounterIDYetAgain;
							        	DialogFragment dialogFragment = new CustomDatePicker();
							        	dialogFragment.show(getActivity().getFragmentManager(), "custom_date_picker");
						        	}
						    		catch (Exception e) {
						    			askRecreateDB();
						    		}
				                	
				                	return true;
				                }
							});
						}
						else if (c == H2HSQLManager.RESULT_COLUMN) {
							text.setOnLongClickListener(new OnLongClickListener() {
				                @Override
				                public boolean onLongClick(View v) {
						        	try {
							        	//Ask if the user wants to change the result.
							        	
										AlertDialog.Builder alertDialog = new AlertDialog.Builder(origContext);
										
										alertDialog.setMessage(getString(R.string.flip_result));
										alertDialog.setTitle(getString(R.string.app_name));
										
										alertDialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
									        public void onClick(DialogInterface dialog, int which) {
									        	try {
									        		//Update edited result.
							        				boolean bWonResult = h2hSQLManager.getBWonResult(finalEncounterIDYetAgain);
							        				bWonResult = !bWonResult;   //Flip result.
							        				h2hSQLManager.updateResult(finalEncounterIDYetAgain, bWonResult);
							        				finalText.setText((bWonResult) ? getString(R.string.won) : getString(R.string.lost));
							        				refreshH2HTotal();
									        	}
									    		catch (Exception e) {
									    			askRecreateDB();
									    		}
									        }
									    });
										
										alertDialog.setNegativeButton(R.string.cancel, null);
										
										alertDialog.create().show();
						        	}
						    		catch (Exception e) {
						    			askRecreateDB();
						    		}
				                	return true;
				                }
							});
						}
						
						break;
					}
				}
				
				cursor.moveToNext();   //Get the next row from the database query.
			}
			
			h2hSQLManager.closeDB();
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
	
	private void addEncounter() {
		try {
			String date = h2hDateButton.getText().toString();
			if (date.length() == 0 || Character.isLetter(date.charAt(0))) {
	        	//Invalid date.
	        	showDialog(getString(R.string.date_error));
	        	return;
			}
			date = convertPrintableDateToDBFormat(date);
			
			boolean bWonResult = !h2hResultSwitch.isChecked();
			
			String notes = h2hNotesEdit.getText().toString();
        	
			h2hSQLManager.insertRow(playerID, date, bWonResult, notes);
			
			rebuildTable();
			
			//Note: We won't bother resetting the result switch. There's no particular reason why,
			//I just like the idea that whatever result you get, you'll keep on getting.
			h2hDateButton.setText(getString(R.string.date));
			h2hNotesEdit.setText("");
			
			refreshH2HTotal();
		}
		catch (Exception e) {
			askRecreateDB();
		}
	}
	
	private void removeEncounter(final int id) {
		try {
			AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
			alertDialog.setMessage(getString(R.string.remove_encounter));
			alertDialog.setTitle(getString(R.string.app_name));
			
			alertDialog.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int which) {
		        	try {
		        		h2hSQLManager.deleteRow(id);
		        		rebuildTable();
		        		refreshH2HTotal();
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
	
	private void refreshH2HTotal() {
		h2hTotal.setText(h2hSQLManager.getTotalResults(playerID));
	}
	
	public int getFragmentID() {
		return fragmentID;
	}
	
	public int getPrevPlayerID() {
		return prevID;
	}
	
	public int getNextPlayerID() {
		return nextID;
	}
	
	public int getPlayerID() {
		return playerID;
	}
	
	//TODO maybe cache name; this will be necessary if I want to show player titles in tab page headers, instead
	//of just saying "Next" and "Previous".
	public String getPlayerName() {
		return playerSQLManager.getName(playerID);
	}
	
	private String convertPrintableDateToDBFormat(String date) {
		//Convert M[M]/D[D]/YYYY to YYYY-MM-DD
		
		String[] parts = date.split("/");
		try {
			return parts[2] + '-' + leftPadDatePart(parts[0]) + '-' + leftPadDatePart(parts[1]);
		}
		catch (ArrayIndexOutOfBoundsException e) {
			//This shouldn't happen (this would be a database data formatting problem), but if it
			//does, we'll handle it softly.
			return date;
		}
	}
	
	private String convertDBDateToPrintableFormat(String date) {
		//Convert YYYY-MM-DD to M[M]/D[D]/YYYY
		
		String[] parts = date.split("-");
		try {
			return removeDatePadding(parts[1]) + '/' + removeDatePadding(parts[2]) + '/' + parts[0];
		}
		catch (ArrayIndexOutOfBoundsException e) {
			//This shouldn't happen (this would be a database data formatting problem), but if it
			//does, we'll handle it softly.
			return date;
		}
	}
	
	private String leftPadDatePart(String datePart) {
		//Return the string, with a left-padded 0 if necessary.
		return String.format("%02d", Integer.parseInt(datePart));
	}
	
	private String removeDatePadding(String datePart) {
		//Use a regular expression to remove left-trailing zeroes from the string (but leave one 0
		//if there's nothing else... which will never be the case with proper date data anyway).
		return datePart.replaceFirst("^0+(?!$)", "");
	}
}
