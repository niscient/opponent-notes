/* Opponent Notes
 * DetailsActivity.java
 * Rains Jordan
 * 
 * File description: Details activity. Shows details for a single player. Allows navigation between
 * fragments.
 * 
 * Notes:
 *  Using instantiateItem() fixed the problems that happened on screen orientation change, although that's
 *   irrelevant since I'm keeping my earlier fix of not reloading XML data on screen orientation change,
 *   so that any in-progress encounter notes won't be disrupted. This means that only one XML file will
 *   ever be used for this activity, for the duration of that activity's lifetime. The alternative would
 *   be trying to save and restore the contents (including text, cursor position, and whether something is
 *   currently highlighted) of any currently-open EditTexts on a screen orientation change.
 *  Note that if I were to remove
 *   android:configChanges="orientation|screenSize"
 *   from the manifest, on a screen orientation change, the current page would become the original page
 *   that was opened to. (If there are only two pages, the original page will be opened.) This is something
 *   I would want to fix if I did ever put XML reloading back in. 
 *  If there are only two players the query, only two fragments are created, and you can't loop infinitely
 *   between them. If only one player is in the query, only one fragment is created. This is mostly all
 *   done to prevent multiple fragments with slightly different states but the same basic information,
 *   on the same player.
 */

package com.pylonsoflight.oppnotes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;

public class DetailsActivity extends BaseActivity implements OnClickListener {
	private static final int THREE_PAGES = 3;
	private static final int TWO_PAGES = 2;
	private static final int ONE_PAGE = 1;   //Unused.
	
    //Generally applies only when three pages are used. We'll determine the behavior to use based on
    //the player table row position of the player that was clicked on in the players table.
    private static final int FIRST_ROW = 0;
    private static final int SECOND_ROW = 1;
    
    //These only apply when three pages are used (although they may be used otherwise anyway, their
    //MEANINGS apply to three-page usage).
    private static final int PREV_PAGE = 0;
    private static final int MIDDLE_PAGE = 1;
    private static final int NEXT_PAGE = 2;
    
    private int NUM_PAGES;
    
	private PlayerSQLManager playerSQLManager;
    
    private int currentPlayerID;
    private int prevPlayerID;
    private int nextPlayerID;
    
    private int playerTableRow;
    
    private ArrayList<Integer> fragmentPositions;   //Contains fragment IDs.
    private Hashtable<Integer,Fragment> fragments;
	
    private ViewPager pager;
    private PagerAdapter pagerAdapter;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        
        //TODO: Remove.
		//findViewById(R.id.detailsPagerTitleStrip).setOnClickListener(this);
		
        //Set up data needed for the fragments.
        
    	/* Note that DetailFragment can't call getActivity() until this activity finishes setting up,
    	 * but we want to set the player IDs for the prev and next fragments ASAP so we can set the
    	 * fragments up. Each fragment's onCreate() is called after the tab pages are set up, so we'll
    	 * set things up here instead. For each fragment, we will pass its respective player ID (the
    	 * prev, current, and next player IDs from the starting point) in a bundle to that fragment.
    	 * We'll avoid using the fragment's setPlayerID() since that is actually a somewhat expensive
    	 * operation; instead, we'll minimize our SQL queries, and continue fiddling with all the
    	 * fragments' IDs from this activity as the user navigates between the tab pages.
    	 * 
		 * Note that we always end up giving the next and prev fragments 0 values for their own next and
		 * prev player ID values. This works fine, since we update a fragment's next and prev player
		 * IDs when it becomes the current fragment.
		 */
        
    	playerSQLManager = new PlayerSQLManager(this);
    	
        int playerCount = playerSQLManager.getCount(MainActivity.currentSportID);
        NUM_PAGES = THREE_PAGES;
        if (NUM_PAGES > playerCount)
        	NUM_PAGES = playerCount;
        
        //TODO remove: This was an important println for a while.
        //System.out.println("wut PAGES " + playerCount + " " + NUM_PAGES);   //TODO remove
    	
		//Get player ID data from the main activity.
		Intent intent = getIntent();
		currentPlayerID = Integer.parseInt(intent.getStringExtra(PlayersFragment.ID_MESSAGE));
		NextAndPrevIDs ids = playerSQLManager.getNextAndPrevIDs(MainActivity.currentSportID, currentPlayerID);
		prevPlayerID = ids.prevID;
		nextPlayerID = ids.nextID;
		
		playerTableRow = playerSQLManager.getQueryPosition(MainActivity.currentSportID, currentPlayerID);
		
		fragmentPositions = new ArrayList<Integer>();
		fragments = new Hashtable<Integer,Fragment>();
        
		//Set up the ViewPager.
		
        pager = (ViewPager)findViewById(R.id.detailsPager);
        pager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                    	if (NUM_PAGES == THREE_PAGES) {
	                        if (position == PREV_PAGE)
	                        	prevPlayer();
	                        else if (position == NEXT_PAGE)
	                        	nextPlayer();
                    	}
                    }
                });
        
        pagerAdapter = new DetailsPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(pagerAdapter);
        pager.setOffscreenPageLimit(NUM_PAGES - 1);   //Force the immediate creation of all offscreen pages.
        
        switch (NUM_PAGES) {
        	case THREE_PAGES:
        		pager.setCurrentItem(MIDDLE_PAGE);
            	break;
        	case TWO_PAGES:
        		if (playerTableRow == SECOND_ROW)
        			pager.setCurrentItem(playerTableRow);
        		break;
        	default:   //One page.
        		break;
        }
    }
	
	@Override
	public void onClick(View v) {
        switch (v.getId()) {
        	//TODO: Remove.
        	/*case R.id.detailsPagerTitleStrip:
	        	hideKeyboard();
	        	break;
	        */
        	default:
        		break;
        }
	}
    
    @Override
    protected void onSaveInstanceState(final Bundle outState) {
    	/* Prevent the data from being saved. This is intended to prevent problems that happen when
    	 * screen orientation causes the activity to be recreated. When there's a screen reorientation,
    	 * ViewPager has strange problems recovering, so we'll use workarounds to avoid having to
    	 * save the old instance state.
    	 * 
    	 * Note that this function is called when the device is locked via the power button or when
    	 * the home key is pressed (or you navigate to another app). Neither of these actions disrupts
    	 * ViewPager the way reorientating the screen does, since they don't result in recreating the
    	 * activity. Also, neither of these actions seems to require saving the instance state, so there
    	 * seems to be no harm in not doing it in those cases, even though doing it is the default
    	 * behavior.
    	 */
    	
    	//As noted, disabling this doesn't seem to cause problems when restoring after device locking
    	//or leaving the app via the home key.
    	
    	//TODO: This has currently been UNCOMMENTED since without it the contents of any open text
    	//fields vanishes when the screen orientation is changed. Currently, screen orientation works
    	//but of course it also causes the strange page problems that have been described.
    	//This means that the program currently has a small bug.
    	//TODO this is mostly useless anyway though since this only helps, anyway, with the name,
    	//rating, and description fields -- it doesn't help restore table text fields!
    	
    	//super.onSaveInstanceState(outState);
    	
    	
    	
    	//TODO resume maybe; decide on whether to split up interface first
    	//Bundle customState = new Bundle();
    	//customState.putString("name", detailsName.getText().toString());
    	
    	//TODO if an edit Notes box is open, save it with an ID, then restore it next time.
    	//http://stackoverflow.com/questions/19234653/edittext-not-automatically-saved-on-screen-orientation-change
    	
    	//in fact, when restoring the table data, i'll have to say something like
    	//"make this text field active, THEN call setText()"
    	//so that the user then has the option of clicking away or not and resulting in the SQL comparison
    	
    	//TODO ignore everything I just said. Now that I have a ScrollView for the entire window,
    	//I don't care whether a separate XML file is loaded for landscape, so I just used
    	//android:configChanges="orientation|screenSize"
    	//in the manifest. By the way, the alternative to having a single large ScrollView is to
    	//section things off into separate fragments, and again in that case I don't care about having
    	//separate XML files. So no matter what, doing this is a lot easier than trying to cobble
    	//together a working system for saving and restoring the (1) text, (2) cursor position,
    	//(3) possibly currently highlighted word for a currently-edited encounter note, which is
    	//what I would want to do if I didn't have this handy cheat available.
    }
    
    //TODO remove.
	/*
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//Inflate the menu; this adds items to the action bar.
		getMenuInflater().inflate(R.menu.details, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	*/
    
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        
        Intent intent = new Intent();
        setResult(REQUEST_CODE, intent);
    }
    
    private class DetailsPagerAdapter extends FragmentStatePagerAdapter  {
        public DetailsPagerAdapter(FragmentManager fm) {
            super(fm);
        }
        
        @Override
        public int getCount() {
            return NUM_PAGES;
        }
        
        @Override
        public Fragment getItem(int position) {
        	return new DetailsFragment();
        }
        
        @Override
        public Fragment instantiateItem(ViewGroup container, int position) {
        	DetailsFragment fragment = (DetailsFragment)super.instantiateItem(container, position);
        	
        	//We'll use initial positions for IDs, since the items will be created out of order (specifically, the
        	//order of creation seems to be "current, prev, next"). We can then easily sort this list, once
        	//we've finished creating the fragments. From that point on, what the fragment IDs are won't matter,
        	//as long as they're all unique.
        	int fragmentID = position;
        	
        	//We got a creation request, even after the list is complete. This shouldn't happen, but just in case.
        	if (fragments.size() >= NUM_PAGES)
        		return fragments.get(fragmentPositions.get(position));
        	
        	Bundle bundle = new Bundle();
        	bundle.putInt(DetailsFragment.ID_FRAGMENT, fragmentID);
        	
        	//Note: Because the fragment's onCreate() won't be called until later and we need it to process
        	//player IDs right now so we can get title information based on those IDs, we'll jump the gun and set
        	//the player IDs preemptively. (Title information is something that I've added and removed in alternate
        	//phases, but this allows it to be used, at least. In any case, there's other setup information
        	//that needs to be sent to the fragments, at creation time.)
        	if (NUM_PAGES == TWO_PAGES && playerTableRow == FIRST_ROW) {
        		//Instead of using what would normally be the "prev and middle" model, use "middle and next".
        		switch (position) {
	        		case 0:
	        			bundle.putInt(PlayersFragment.ID_MESSAGE, currentPlayerID);
	        			bundle.putBoolean(DetailsFragment.ID_HIDE_PREV_REMINDER, true);
	        			fragment.setIDs(currentPlayerID, prevPlayerID, nextPlayerID);
	        			break;
	        		default:   //1
	        			bundle.putInt(PlayersFragment.ID_MESSAGE, nextPlayerID);
	        			bundle.putBoolean(DetailsFragment.ID_HIDE_NEXT_REMINDER, true);
	        			fragment.setIDs(nextPlayerID, 0, 0);
	        			break;
        		}
        	}
        	else {
	        	switch (position) {
	        		case 0:   //aka PREV_PAGE, if using three pages
	        			bundle.putInt(PlayersFragment.ID_MESSAGE, prevPlayerID);
	        			fragment.setIDs(prevPlayerID, 0, 0);
	        			if (NUM_PAGES == TWO_PAGES || NUM_PAGES == ONE_PAGE)
	        				bundle.putBoolean(DetailsFragment.ID_HIDE_PREV_REMINDER, true);
	        			if (NUM_PAGES == ONE_PAGE)
	        				bundle.putBoolean(DetailsFragment.ID_HIDE_NEXT_REMINDER, true);
	        			break;
	        		case 1:   //aka MIDDLE_PAGE, if using three pages
	        			bundle.putInt(PlayersFragment.ID_MESSAGE, currentPlayerID);
	        			fragment.setIDs(currentPlayerID, prevPlayerID, nextPlayerID);
	        			if (NUM_PAGES == TWO_PAGES)
	        				bundle.putBoolean(DetailsFragment.ID_HIDE_NEXT_REMINDER, true);
	        			break;
	        		default:   //2, aka NEXT_PAGE, if using three pages
	        			bundle.putInt(PlayersFragment.ID_MESSAGE, nextPlayerID);
	        			fragment.setIDs(nextPlayerID, 0, 0);
	        			break;
	        	}
        	}
        	
        	fragment.setArguments(bundle);
        	
        	fragmentPositions.add(fragmentID);
        	fragments.put(fragmentID, fragment);
        	
        	//If we're still constructing the list (which we should be, but just in case), make sure it's
        	//organized by initial position.
        	if (fragmentPositions.size() == NUM_PAGES)
        		Collections.sort(fragmentPositions);
        	
        	return fragment;
        }
        
        @Override
        public int getItemPosition(Object item) {
            DetailsFragment fragment = (DetailsFragment)item;
            int fragmentID = fragment.getFragmentID();
            int listPosition = fragmentPositions.indexOf(fragmentID);
            
            if (listPosition == -1)
                return POSITION_NONE;
            else
                return listPosition;
        }
        
        //TODO: Remove entirely.
        /*
        @Override
        public CharSequence getPageTitle(int position) {
        	//Note: getPageTitle() runs before getItem(), strangely, so we can't access the fragment data here.
        	//Titles have been removed for now, for efficiency reasons (expensive SQL lookups). These lookups
        	//would only be necessary if direct fragment.getPlayerName() access failed, but it will fail at first
        	//because of the getItem() problem. So I could modify this to work better, but it will always be
        	//slow when loading the first set of data (before the user navigates between tabs).
        	
        	if (NUM_PAGES == THREE_PAGES || (NUM_PAGES == TWO_PAGES && playerTableRow == SECOND_ROW)) {
	            switch(position) {
	            	case PREV_PAGE:
	            		return getString(R.string.previous);
	            	case MIDDLE_PAGE:
	            		return "";
	            	case NEXT_PAGE:
	            		return getString(R.string.next);
	            	default:
	            		break;
	            }
        	}
        	else if (NUM_PAGES == TWO_PAGES) {
	            switch(position) {
	            	case 0:
	            		return "";
	            	default:   //1
	            		return getString(R.string.next);
	            }
        	}
        	return "";
        }
        */
        
        //TODO this is now out of date
        //TODO maybe remove, although it does work and is the acceptable solution to the complaint in the other
        //version of this function.
        //Also, this function is a bit slow anyway. To fix that, DetailsFragment's getPlayerName() would have
        //to be rewritten to store a copy of the player name, rather than looking it up in real-time.
        //TODO maybe do this (say via a new DetailsFragment's function called getCachedPlayerName()) if I want to
        //reinstate tab page titles that are player names.
        //TODO also, after a device turn, the titles become invalid.
        /*@Override
        public CharSequence getPageTitle(int position) {
    		if (position == MIDDLE_PAGE)
    			return "";
    		
        	if (position < fragmentPositions.size()) {
        		int fragmentID = fragmentPositions.get(position);
            	
        		DetailsFragment fragment = (DetailsFragment)fragments.get(fragmentID);
        		//return fragment.getPlayerName();
        		return fragment.getPlayerName();
        	}
        	else {
	            switch(position) {
	            	case PREV_PAGE:
	            		return playerSQLManager.getName(prevPlayerID);
	            		//return getString(R.string.previous);
	            	case NEXT_PAGE:
	            		return playerSQLManager.getName(nextPlayerID);
	            		//return getString(R.string.next);
	            	default:
	            		break;
	            }
        	}
        	return "";
        }*/
    }
    
    public void prevPlayer() {
    	//Translate elements in the order 0 1 2 to 2 0 1, etc. (the next call would give 1 2 0).
    	
    	if (prevPlayerID == 0)   //Should never happen, since our queries loop.
    		return;
    	
    	if (fragmentPositions.size() == THREE_PAGES) {   //Just to make sure.
    		int temp0 = fragmentPositions.get(0);
    		fragmentPositions.set(0, fragmentPositions.get(2));
    		fragmentPositions.set(2, fragmentPositions.get(1));
    		fragmentPositions.set(1, temp0);
    		
    		nextPlayerID = currentPlayerID;
    		currentPlayerID = prevPlayerID;
    		prevPlayerID = playerSQLManager.getNextAndPrevIDs(MainActivity.currentSportID, currentPlayerID).prevID;
    		
    		//Since we don't save the instance state for this activity, we need to set the current
    		//player ID so that if the activity is asked to recreate itself, it'll use the up-to-date
    		//current player ID.
    		getIntent().putExtra(PlayersFragment.ID_MESSAGE, Integer.toString(currentPlayerID));
    		
    		((DetailsFragment)fragments.get(fragmentPositions.get(MIDDLE_PAGE))).setIDs(currentPlayerID, prevPlayerID, nextPlayerID);
    		((DetailsFragment)fragments.get(fragmentPositions.get(NEXT_PAGE))).setIDs(nextPlayerID, 0, 0);
    		
    		final DetailsFragment prevFragment = ((DetailsFragment)fragments.get(fragmentPositions.get(PREV_PAGE)));
    		prevFragment.setIDs(prevPlayerID, 0, 0);
    		
    		//Run this in a new thread to prevent visual slowdowns.
    		Handler handler = new Handler();
    		handler.postDelayed(new Runnable() {
    		    @Override
    		    public void run() {
    		    	prevFragment.loadPlayerData();
    		    }
    		}, 0);
    		
    		pagerAdapter.notifyDataSetChanged();
    	}
    }
    
    public void nextPlayer() {
    	//Translate elements in the order 0 1 2 to 1 2 0, etc. (the next call would give 2 0 1).
    	
    	if (nextPlayerID == 0)   //Should never happen, since our queries loop.
    		return;
    	
    	if (fragmentPositions.size() == THREE_PAGES) {
	    	int temp0 = fragmentPositions.get(0);
	    	fragmentPositions.set(0, fragmentPositions.get(1));
	    	fragmentPositions.set(1, fragmentPositions.get(2));
	    	fragmentPositions.set(2, temp0);
	    	
    		prevPlayerID = currentPlayerID;
    		currentPlayerID = nextPlayerID;
    		nextPlayerID = playerSQLManager.getNextAndPrevIDs(MainActivity.currentSportID, currentPlayerID).nextID;
    		
    		//Since we don't save the instance state for this activity, we need to set the current
    		//player ID so that if the activity is asked to recreate itself, it'll use the up-to-date
    		//current player ID.
    		getIntent().putExtra(PlayersFragment.ID_MESSAGE, Integer.toString(currentPlayerID));
    		
    		((DetailsFragment)fragments.get(fragmentPositions.get(PREV_PAGE))).setIDs(prevPlayerID, 0, 0);
    		((DetailsFragment)fragments.get(fragmentPositions.get(MIDDLE_PAGE))).setIDs(currentPlayerID, prevPlayerID, nextPlayerID);
    		
    		final DetailsFragment nextFragment = ((DetailsFragment)fragments.get(fragmentPositions.get(NEXT_PAGE)));
    		nextFragment.setIDs(nextPlayerID, 0, 0);
    		
    		//Run this in a new thread to prevent visual slowdowns.
    		Handler handler = new Handler();
    		handler.postDelayed(new Runnable() {
    		    @Override
    		    public void run() {
    		    	nextFragment.loadPlayerData();
    		    }
    		}, 0);
    		
	    	pagerAdapter.notifyDataSetChanged();
    	}
    }
    
    public void refreshNextAndPrev() {
    	if (fragmentPositions.size() == THREE_PAGES) {
    		NextAndPrevIDs ids = playerSQLManager.getNextAndPrevIDs(MainActivity.currentSportID, currentPlayerID);
    		prevPlayerID = ids.prevID;
    		nextPlayerID = ids.nextID;
    		
    		((DetailsFragment)fragments.get(fragmentPositions.get(MIDDLE_PAGE))).setIDs(currentPlayerID, prevPlayerID, nextPlayerID);
    		
    		final DetailsFragment prevFragment = ((DetailsFragment)fragments.get(fragmentPositions.get(PREV_PAGE)));
    		prevFragment.setIDs(prevPlayerID, 0, 0);
    		final DetailsFragment nextFragment = ((DetailsFragment)fragments.get(fragmentPositions.get(NEXT_PAGE)));
    		nextFragment.setIDs(nextPlayerID, 0, 0);
    		
    		//Run this in a new thread to prevent visual slowdowns.
    		Handler handler = new Handler();
    		handler.postDelayed(new Runnable() {
    		    @Override
    		    public void run() {
    		    	prevFragment.loadPlayerData();
    		    	nextFragment.loadPlayerData();
    		    }
    		}, 0);
    		
	    	pagerAdapter.notifyDataSetChanged();
    	}
    }
}