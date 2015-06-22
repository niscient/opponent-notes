/* Opponent Notes
 * SportsFragment.java
 * Rains Jordan
 * 
 * File description: Allows creation and deletion of sports.
 */

package com.pylonsoflight.oppnotes;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class SportsFragment extends BaseFragment implements OnClickListener, OnTouchListener, OnFocusChangeListener {
	private SettingsSQLManager settingsSQLManager;
	private SportSQLManager sportSQLManager;
	
	private Spinner sportsSpinner;
	private EditText templateEdit;
	private EditText sportEdit;
	private Button closeEditorButton, removeSportButton, addSportButton;
	
	public boolean bSettingUpSpinner;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        FragmentActivity activity = getActivity();
        settingsSQLManager = new SettingsSQLManager(activity);
		sportSQLManager = new SportSQLManager(activity);
		playerSQLManager = new PlayerSQLManager(activity);
		h2hSQLManager = new H2HSQLManager(activity);
	}
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	View rootView = inflater.inflate(R.layout.fragment_sports, container, false);
    	rootView.setOnTouchListener(this);   //TODO why?
        
        sportsSpinner = (Spinner)rootView.findViewById(R.id.sportsSpinner);
        templateEdit = (EditText)rootView.findViewById(R.id.sportsTemplateEdit);
		sportEdit = (EditText)rootView.findViewById(R.id.sportsSportEdit);
		closeEditorButton = (Button)rootView.findViewById(R.id.sportsCloseEditorButton);
		removeSportButton = (Button)rootView.findViewById(R.id.removeSportButton);
		addSportButton = (Button)rootView.findViewById(R.id.addSportButton);
		
		closeEditorButton.setOnClickListener(this);
		removeSportButton.setOnClickListener(this);
		addSportButton.setOnClickListener(this);
		
		rootView.findViewById(R.id.clickableSportsLayout1).setOnClickListener(this);
		
		bSettingUpSpinner = true;
		
		sportsSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				MainActivity.currentSportID = sportSQLManager.getNthID(position);
				
				if (bSettingUpSpinner) {
					bSettingUpSpinner = false;
				}
				else
					settingsSQLManager.setDefaultSportsSpinnerItem(position);
				
				templateEdit.setText(sportSQLManager.getTemplate(MainActivity.currentSportID));
				
				if (((MainActivity)getActivity()).playersFragment != null)
					((MainActivity)getActivity()).playersFragment.rebuildTable();
			}
			
			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				//Do nothing. I don't think this will ever run, anyway. When the last existing
				//spinner item is deleted, neither this nor onItemSelected() runs.
			}
		});
		
		templateEdit.setOnFocusChangeListener(this);
		
		buildTable();
		
        return rootView;
    }
	
	@Override
	public void onClick(View v) {
        switch (v.getId()) {
        	case R.id.sportsCloseEditorButton:
        		hideKeyboard();
        		break;
	        case R.id.removeSportButton:
	        	removeSport();
	        	hideKeyboard();
	        	break;
	        case R.id.addSportButton:
	        	addSport();
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
	    	case R.id.sportsTemplateEdit:
		        if (!hasFocus) {
		        	//The user just took focus off the EditText, clicking somewhere else.
		        	
		        	//Note: If the user tries to add a new sport while still editing the template for
		        	//another sport, we need to stop that from happening, as the current sport ID will be
		        	//immediately set, messing up any chance of saving changes to the old template here.
		        	//Using a final ID doesn't actually help since addSport() sets the current sport ID
		        	//before this code gets a chance to execute.

		        	final int id = MainActivity.currentSportID;
		        	final String origText;
		        	origText = sportSQLManager.getTemplate(id);
		        	
		        	//Note that we need to get the string here so we use the string for later reference;
		        	//if we were to just use the EditText, we could run into problems later when we try to
		        	//add a new sport while still editing the template for another sport.
		        	//Well... to be honest, due to the problems mentioned earlier, this won't really help
		        	//with that; we still need to prevent the addition of new sports while editing old
		        	//ones. But it's still not a bad idea to cache the text value, here.
		        	final EditText textEdit = (EditText)v;
		        	final String newText = textEdit.getText().toString();
		        	
		        	if (origText.equals(newText))
		        		return;   //No change in string data; cancel.
		        	
		        	//Ask if the user wants to save the changes to the EditText.
		        	
					AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
					
					alertDialog.setMessage(getString(R.string.save_changes));
					alertDialog.setTitle(getString(R.string.app_name));
					
					alertDialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
				        public void onClick(DialogInterface dialog, int which) {
				        	try {
				        		sportSQLManager.updateTemplate(id, newText);
				        	}
				    		catch (Exception e) {
				    			askRecreateDB();
				    		}
				        }
				    });
					
					alertDialog.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
				        public void onClick(DialogInterface dialog, int which) {
				        	//To prevent user confusion, we'll restore the original text.
				        	textEdit.setText(origText);
				        }
				    });
					
					alertDialog.create().show();
		        }
		        break;
		    default:
		    	break;
    	}
    }
	
	protected void buildTable() {
		try {
			//In this case, the "table" refers to our sport list.
			
			sportSQLManager.openDB();
			Cursor cursor = sportSQLManager.getFirstRowCursor();
			
			int rows = cursor.getCount();
			int columns = cursor.getColumnCount();
			
			ArrayList<String> items = new ArrayList<String>();
			int selectedItem = -1;
			
			for (int r = 0; r < rows; ++r) {
				int sportID = 0;   //0 is an unused default row ID, according to SQLite and all other databases. We will never generate it automatically.
				
				for (int c = 0; c < columns; ++c) {
					switch(c) {
					case SportSQLManager.ID_COLUMN:   //The first column, the ID of this row in the database.
						sportID = cursor.getInt(c);   //We know the sport ID is the first column of a row. Note that this is standard database practice.
						
						if (sportID == MainActivity.currentSportID)
							selectedItem = r;   //Convert from row to spinner item index.
						
						break;
					
					case SportSQLManager.SPORT_COLUMN:
						items.add(cursor.getString(c));
						break;
					
					default:
						break;
					}
				}
				
				cursor.moveToNext();   //Get the next row from the database query.
			}
			
			sportSQLManager.closeDB();
			
		    ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
		    	android.R.layout.simple_spinner_item, items);
		    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		    sportsSpinner.setAdapter(adapter);
			
		    //Note: This should not run if we just deleted the current item and set the current
		    //sport ID to 0.
			if (selectedItem != -1)
				sportsSpinner.setSelection(selectedItem);
			
			/* Note that, if we just deleted the current item then recreated the spinner, the topmost
			 * (0-index) spinner item will be selected automatically, due to OnItemSelectedListener
			 * being activated automatically (due to our just recreating the spinner's contents).
			 * 
			 * If we're still setting up the spinner (when the fragment is first being created), we
			 * can prevent this behavior (due to how Android works) by making a setSelection() call of
			 * our own. However, we will let Android proceed with its automatic 0-index selecting
			 * behavior if we AREN'T setting up the spinner, but the current sport ID is 0 (in other
			 * words, if we just set the current sport ID to 0 due to removing a sport).
			 */
			
			if (bSettingUpSpinner) {
				//Must happen after calling setAdapter().
				//Note that calling this is harmless if the spinner is empty, so we'll just do that.
				sportsSpinner.setSelection(settingsSQLManager.getDefaultSportsSpinnerItem());
			}
			
			//Each time we build the table, enable or disable the widgets that allow sport creation.
			if (items.size() > 0) {
				removeSportButton.setEnabled(true);
				removeSportButton.setClickable(true);
				
				templateEdit.setEnabled(true);
				templateEdit.setClickable(true);
				//Note that the actual template contents will be set by onItemSelected() in a moment.
			}
			else {
				removeSportButton.setEnabled(false);
				removeSportButton.setClickable(false);
				
				/* We have an empty spinner. Whenever we get around to adding items again, let's make
				 * 0 the default selected spinner item position.
				 * Only bother doing this if it will result in a value change, though. (This basically
				 * means, did we just delete something, or are we still setting up the spinner, in
				 * which case we haven't changed the default value? Those are the two situations we
				 * could be in.)
				 */
				if (!bSettingUpSpinner)
					settingsSQLManager.setDefaultSportsSpinnerItem(0);
				
				templateEdit.setEnabled(false);
				templateEdit.setClickable(false);
				templateEdit.setText("");
			}
		}
		catch (Exception e) {   //No matter what, if the program is crashing, I want the user to have the option to reset the database.
			askRecreateDB();
		}
	}
	
	protected void rebuildTable() {
		//Note: There's no need to empty the contents of the spinner.
		//playerSQLManager.printSQLCounts(getActivity());   //TODO remove
		buildTable();
	}
	
	private void addSport() {
		try {
			//First, let's make sure we're not currently editing another sport's template.
			if (MainActivity.currentSportID != 0) {
	        	String origTemplate = sportSQLManager.getTemplate(MainActivity.currentSportID);
	        	String newTemplate = templateEdit.getText().toString();
	        	
	        	if (!origTemplate.equals(newTemplate)) {
	        		showDialog(getString(R.string.sport_add_wait_edit_error));
	        		return;   //No change in string data.
	        	}
			}
        	
        	//Okay, now proceed to actually make a new sport.
        	
			String sport = sportEdit.getText().toString();
			if (sport.length() == 0) {
	        	showDialog(getString(R.string.sport_error));
	        	return;
			}
			
			int id = sportSQLManager.insertRow(sport);
			MainActivity.currentSportID = id;
			
			rebuildTable();
			
			sportEdit.setText("");
		}
		catch (Exception e) {
			askRecreateDB();
		}
	}
	
	private void removeSport() {
		try {
			final int id = MainActivity.currentSportID;
			
			if (id == 0)   //Invalid sport.
				return;
			
			AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
			
			alertDialog.setMessage(getString(R.string.remove_sport_message_start) + sportSQLManager.getSport(id) +
			    getString(R.string.remove_sport_message_end));
	    	
			alertDialog.setTitle(getString(R.string.app_name));
			
			alertDialog.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int which) {
		        	try {
		        		sportSQLManager.deleteRow(id, playerSQLManager, h2hSQLManager);
						
						//Indirectly prevent a spinner item from being selected when the spinner
						//is recreated while rebuilding the "table". Note that recreating the spinner
						//will cause OnItemSelectedListener to activate, automatically selecting
						//the current sport ID to the first sport ID.
		        		MainActivity.currentSportID = 0;
		        		
		        		rebuildTable();
		        		
		        		//Note that we should only do this if there are no sports; otherwise, the act of
		        		//recreating the sports spinner and having onItemSelected called will automatically
						//rebuild the players table. However, we'll just refresh it anyway, whatever.
		        		if (((MainActivity)getActivity()).playersFragment != null)
		        			((MainActivity)getActivity()).playersFragment.rebuildTable();
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