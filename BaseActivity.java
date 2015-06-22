/* Opponent Notes
 * BaseActivity.java
 * Rains Jordan
 * 
 * File description: Abstract class which contains some code shared by other activities.
 */

package com.pylonsoflight.oppnotes;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public abstract class BaseActivity extends FragmentActivity {
	public static final String PACKAGE_NAME = "com.pylonsoflight.oppnotes";
	public static final int REQUEST_CODE = 0;   //Used to help signal the main activity that the detail activity has closed.
	
	protected void hideKeyboard() {
	    InputMethodManager inputManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
	    View view = getCurrentFocus();
	    if (view != null) {
	        inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
	        view.clearFocus();
	    }
	}
}
