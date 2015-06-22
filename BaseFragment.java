/* Opponent Notes
 * BaseFragment.java
 * Rains Jordan
 * 
 * File description: Abstract class which contains some code shared by other fragments.
 */

package com.pylonsoflight.oppnotes;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Point;
import android.support.v4.app.Fragment;
import android.view.Display;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public abstract class BaseFragment extends Fragment {
	protected PlayerSQLManager playerSQLManager;
	protected H2HSQLManager h2hSQLManager;
	
	//Removed for now; never tested. Probably fine.
	/*
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        FragmentActivity activity = getActivity();
		playerSQLManager = new PlayerSQLManager(activity);
		h2hSQLManager = new H2HSQLManager(activity);
	}
	*/
	
	abstract protected void rebuildTable();
	
	protected void hideKeyboard() {
	    InputMethodManager inputManager = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
	    View view = getActivity().getCurrentFocus();
	    if (view != null) {
	        inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
	        view.clearFocus();
	    }
	}
	
	protected int getScreenWidth() {
    	Display display = getActivity().getWindowManager().getDefaultDisplay();
    	Point size = new Point();
    	display.getSize(size);
    	return size.x;
	}
	
	protected void showDialog(String message) {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
		
		alertDialog.setMessage(message);
		alertDialog.setTitle(getString(R.string.error));
		alertDialog.setPositiveButton(R.string.ok, null);
		alertDialog.create().show();
	}
	
	protected void askRecreateDB() {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
		alertDialog.setMessage(getString(R.string.recreate_message));
		alertDialog.setTitle(getString(R.string.app_name));
		
		alertDialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) {
	        	//Note: It makes little difference which SQLManager object we use, as they use the
	        	//same database.
	        	playerSQLManager.recreateDB();
	        	rebuildTable();
	        }
	    });
		
		alertDialog.setNegativeButton(R.string.cancel, null);
		alertDialog.create().show();
	}
}
