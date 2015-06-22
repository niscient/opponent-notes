//TODO:
//(2) that blank Player Details page...
//(3) i think the edge of Rating will go off the edge on 2.7" screens or when the font size is Huge.
//    to fix this, I've used layout_weight... improperly. 100 to 1 makes no sense.
//(4) throughout the code, i use int instead of long for row IDs... that should really change.
//(5) i don't know what's with that little gray or blue line on the right edge of the details fragments
//    much of the time... it's a viewpager thing, been happening for a while before this version of the
//    code. the gray line always appears on the first page of the ViewPager, for like 1/2 second when it's
//    first created. this is likely to never be fixed.
//(6) remove commented-out toasts and printlns throughout program.
//(7) i should probably remove Settings in favor of SharedPreferences... maybe.
//(8) change "players.db" to "oppnotes.db"

/* Opponent Notes
 * MainActivity.java
 * Rains Jordan
 * 
 * Project description: A program which keeps track of the opponents you face in a sport or some other
 *  kind of competition. Stores each opponent's name, a numeric difficulty rating, and a short
 *  description of the player. Also stores head-to-head records of all the meetings you've had with
 *  a player, and who won which meetings.
 * 
 * File description: Main activity. Shows a ViewPager containing main tabs.
 * 
 * Notes:
 *  Although difficulty is expected to be a number, no boundaries are set. This is so that you can use
 *   the program for various sports, which all use different metrics. Also, in the database, the
 *   difficulty ratings are actually stored as strings. This is to allow the user to specify the
 *   difficulty format of their choice -- if they enter in 4.0, I want to keep that. If they enter 4, I
 *   want to keep that. I don't want to convert it arbitrarily. However, if the user enters a
 *   non-numeric difficulty rating, the program complains.
 *  Ignore the XML message that a ScrollView or its LinearLayout is possibly useless. Make the
 *   ScrollView the topmost element breaks everything.
 *  
 * To do:
 *  I have commented out all references to setTextSize() and avoided XML use of dimens.text_normal and
 *   text_label unless I decide to set font sizes manually. (Note that whatever settings I do use, as
 *   long as they're in sp, will incorporate the device user's chosen font size.) I do use manual sizes
 *   for the Next and Previous headers.
 *  
 *  Remove PagerTitleStrip entirely.
 */

package com.pylonsoflight.oppnotes;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.ViewGroup;

public class MainActivity extends BaseActivity {
    private static final int NUM_PAGES = 3;
    
    private static final int PLAYERS_PAGE = 0;
    private static final int SPORTS_PAGE = 1;
    //private static final int BACKUP_PAGE = 2;   //Doesn't need to be stored.
    
    static public int currentSportID;
    
    public PlayersFragment playersFragment;
    public SportsFragment sportsFragment;
    
    private ViewPager pager;
    private PagerAdapter pagerAdapter;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        //TODO remove; outdated
        /*
        SportSQLManager sportSQLManager = new SportSQLManager(this);
        currentSportID = sportSQLManager.getFirstID();   //Will be 0 (no ID) if there are no sports.
        */
        
        //This will be overridden by the sports spinner's OnItemSelectedListener (which Android calls
        //automatically at the start, after the sports spinner "table" is built and setAdapter() is
        //called). (Note that we'll actually override this behavior to use our own custom default
        //spinner item position, rather than Android's default 0-index.)
        currentSportID = 0;
        
        //To prevent redrawing the main table at the start, get this now.
        //TODO remove this, since putting in instantiateitem means that all references to the players
        //fragment remain up-to-date (a necessity since viewpager has this habit of never calling
        //getitem() on screen orientation changes).
        //TODO countermand this, it's better visually not to have to reload the table on start even if
        //it isn't really necessary. so keep this, after all.
        SettingsSQLManager settingsSQLManager = new SettingsSQLManager(this);
        int sportsSpinnerItem = settingsSQLManager.getDefaultSportsSpinnerItem();
        SportSQLManager sportSQLManager = new SportSQLManager(this);
        currentSportID = sportSQLManager.getNthID(sportsSpinnerItem);
        
        playersFragment = null;
        sportsFragment = null;
        
        pager = (ViewPager)findViewById(R.id.mainPager);
        pager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        getActionBar().setSelectedNavigationItem(position);
                    }
                });
        
        pagerAdapter = new MainPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(pagerAdapter);
        
        //Indirectly, make it so onCreateView() isn't called each time the user switches between
        //fragments... not that it matters much.
        pager.setOffscreenPageLimit(NUM_PAGES - 1);
        
        final ActionBar actionBar = getActionBar();
        
        //Display tabs in the action bar.
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        
        ActionBar.TabListener tabListener = new ActionBar.TabListener() {
            @Override
			public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
				pager.setCurrentItem(tab.getPosition());
			}
            
            public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
                //Normally here we would hide the given tab, but we'll do nothing.
            }
            
            public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
                //Do nothing.
            }
        };
        
        for (int i = 0; i < NUM_PAGES; i++) {
        	String tabTitle;
        	switch(i) {
        		case PLAYERS_PAGE:
        			tabTitle = getString(R.string.players_tab);
        			break;
        		case SPORTS_PAGE:
        			tabTitle = getString(R.string.sports_tab);
        			break;
        		default:   //Must be BACKUP_PAGE.
        			tabTitle = getString(R.string.backup_tab);
        			break;
        	}
            actionBar.addTab(actionBar.newTab().setText(tabTitle).setTabListener(tabListener));
        }
        
        //TODO REMOVE THIS STUFF, IT'S FOR QUICK TESTING.
        /*
        int testPlayerID = 6;   //happens to be delpo
		Intent intent = new Intent(this, DetailsActivity.class);
		intent.putExtra(PlayersFragment.ID_MESSAGE, Integer.toString(testPlayerID));
		intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		startActivityForResult(intent, BaseActivity.REQUEST_CODE);
		*/
    }
    
    //TODO delete?
    /*@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}*/
	
    //TODO delete?
	/*@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}*/
	
    @Override
    public void onBackPressed() {
        if (pager.getCurrentItem() == 0) {
        	//If the user is looking at the first step and hits Back, this calls finish() on this
        	//activity and pops the back stack.
            super.onBackPressed();
        }
        else {
            pager.setCurrentItem(pager.getCurrentItem() - 1);
        }
    }
    
    private class MainPagerAdapter extends FragmentPagerAdapter {
        public MainPagerAdapter(FragmentManager fm) {
            super(fm);
        }
        
        @Override
        public Fragment getItem(int position) {
        	switch(position) {
        		case PLAYERS_PAGE:
        			return new PlayersFragment();
        		case SPORTS_PAGE:
        			return new SportsFragment();
        		default:   //Must be BACKUP_PAGE.
        			return new BackupFragment();
        	}
        }
        
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = (Fragment)super.instantiateItem(container, position);
            
        	switch(position) {
	    		case PLAYERS_PAGE:
	    			playersFragment = (PlayersFragment)fragment;
	    			break;
	    		case SPORTS_PAGE:
	    			sportsFragment = (SportsFragment)fragment;
	    			break;
	    		default:   //Must be BACKUP_PAGE.
	    			//Note: We don't care about storing the backup fragment.
	    			break;
	    	}
            
            return fragment;
        }
        
        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }
}