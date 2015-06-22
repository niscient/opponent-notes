/* Opponent Notes
 * BackupFragment.java
 * Rains Jordan
 * 
 * File description: Allows backing up and restoration of database.
 * 
 * TODO maybe make not inherit from BaseFragment, thus not having to do some unnecessary setup stuff.
 */

package com.pylonsoflight.oppnotes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;

public class BackupFragment extends BaseFragment implements OnClickListener, OnTouchListener {
	private Button saveTostorageButton, loadFromstorageButton;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //TODO: Pointless... so maybe make a new fragment class, without this stuff?
        /*FragmentActivity activity = getActivity();
		playerSQLManager = new PlayerSQLManager(activity);
		h2hSQLManager = new H2HSQLManager(activity);*/
	}
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	View rootView = inflater.inflate(R.layout.fragment_backup, container, false);
    	rootView.setOnTouchListener(this);   //TODO why?
        
    	saveTostorageButton = (Button)rootView.findViewById(R.id.saveToStorageButton);
    	loadFromstorageButton = (Button)rootView.findViewById(R.id.loadFromStorageButton);
		
    	saveTostorageButton.setOnClickListener(this);
    	loadFromstorageButton.setOnClickListener(this);
		
		rootView.findViewById(R.id.clickableBackupLayout1).setOnClickListener(this);
		
        return rootView;
    }
	
	@Override
	public void onClick(View v) {
        switch (v.getId()) {
	        case R.id.saveToStorageButton:
	        	saveTostorage();
	        	hideKeyboard();
	        	break;
	        case R.id.loadFromStorageButton:
	        	loadFromstorage();
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
	
	protected void rebuildTable() {
		//TODO: Pointless...
		//Do nothing; a relic of the class hierarchy.
	}
	
	private void saveTostorage() {
		copyDBFile(true);
	}
	
	private void loadFromstorage() {
		copyDBFile(false);
	}
	
	private void copyDBFile(final boolean bSaving) {
		//Copy the DB file either to or from storage.
		
		try {
	        File device = Environment.getDataDirectory();
	        File storage = Environment.getExternalStorageDirectory();
	        
	        if (bSaving) {
	        	if (!storage.canWrite()) {
	    			AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
	    			
	    			alertDialog.setMessage(getString(R.string.cannot_access_storage_write));
	    	    	
	    			alertDialog.setTitle(getString(R.string.app_name));
	    			alertDialog.setPositiveButton(getString(R.string.ok), null);
	    			alertDialog.setCancelable(false);
	    			
	    			alertDialog.create().show();
	    			return;
	            }
	        }
	        else {
	        	if (!storage.canRead()) {
	    			AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
	    			
	    			alertDialog.setMessage(getString(R.string.cannot_access_storage_read));
	    	    	
	    			alertDialog.setTitle(getString(R.string.app_name));
	    			alertDialog.setPositiveButton(getString(R.string.ok), null);
	    			alertDialog.setCancelable(false);
	    			
	    			alertDialog.create().show();
	    			return;
	            }
            }
            
	        String devicePath = "//data//" + BaseActivity.PACKAGE_NAME + "//databases//" + SQLManager.DB_FILE;
	        String storagePath = SQLManager.DB_FILE;
	        
            final File sourceDB = (bSaving) ? new File(device, devicePath) : new File(storage, storagePath);
            final File destDB = (bSaving) ? new File(storage, storagePath) : new File(device, devicePath);
            
            if (bSaving) {
            	if (destDB.exists()) {
	    			AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
	    			
	    			alertDialog.setMessage(getString(R.string.already_exists_start) + SQLManager.DB_FILE +
	    			    getString(R.string.already_exists_end));
	    	    	
	    			alertDialog.setTitle(getString(R.string.app_name));
	    			
	    			alertDialog.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
	    		        public void onClick(DialogInterface dialog, int which) {
	    		        	try {
	    		        		copyDBFilePart2(bSaving, sourceDB, destDB);
	    		        	}
	    		    		catch (Exception e) {
	    		    			askRecreateDB();
	    		    		}
	    		        }
	    		    });
	    			
	    			alertDialog.setNegativeButton(getString(R.string.cancel), null);
	    			
	    			alertDialog.create().show();
            	}
                else
                	copyDBFilePart2(bSaving, sourceDB, destDB);
            }
            else {
    			AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
    			
    			alertDialog.setMessage(getString(R.string.load_backup_prompt));
    	    	
    			alertDialog.setTitle(getString(R.string.app_name));
    			
    			alertDialog.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
    		        public void onClick(DialogInterface dialog, int which) {
    		        	try {
    		        		copyDBFilePart2(bSaving, sourceDB, destDB);
    		        	}
    		    		catch (Exception e) {
    		    			askRecreateDB();
    		    		}
    		        }
    		    });
    			
    			alertDialog.setNegativeButton(getString(R.string.cancel), null);
    			
    			alertDialog.create().show();
            }
		}
		catch (Exception e) {
			askRecreateDB();
		}
	}
	
	private void copyDBFilePart2(boolean bSaving, File sourceDB, File destDB) throws IOException {
        if (sourceDB.exists()) {
        	FileInputStream source = new FileInputStream(sourceDB);
        	FileOutputStream dest = new FileOutputStream(destDB);
        	
            byte[] buf = new byte[1024];
            int len;
            while ((len = source.read(buf)) > 0) {
                dest.write(buf, 0, len);
            }
            
            source.close();
            dest.close();
        }
        else {
			AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
			
			alertDialog.setMessage(getString(R.string.does_not_exist_start) + SQLManager.DB_FILE +
			    getString(R.string.does_not_exist_end));
	    	
			alertDialog.setTitle(getString(R.string.app_name));
			alertDialog.setPositiveButton(getString(R.string.ok), null);
			alertDialog.setCancelable(false);
			
			alertDialog.create().show();
        }
        
        if(!bSaving) {
        	SportsFragment sportsFragment = ((MainActivity)getActivity()).sportsFragment;
			if (sportsFragment != null) {
	        	sportsFragment.bSettingUpSpinner = true;
	        	sportsFragment.rebuildTable();
			}
			
    		//Note that we should only do this if there are no sports; otherwise, the act of
    		//recreating the sports spinner and having onItemSelected called will automatically
			//rebuild the players table. However, we'll just refresh it anyway, whatever.
			if (((MainActivity)getActivity()).playersFragment != null)
				((MainActivity)getActivity()).playersFragment.rebuildTable();
        }
	}
}