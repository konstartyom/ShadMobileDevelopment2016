package com.konstartyom.photos;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mTitle, mDrawerTitle;
    private String mCurrentTheme;

    private static final String TAG = "PhotosMain";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mCurrentTheme = ThemeLoader.loadAppropriateTheme(this, ThemeLoader.MAIN_THEME);
        super.onCreate(savedInstanceState);
        ImageCacher.init();
        setContentView(R.layout.activity_main);
        TabLayout tabs = (TabLayout) findViewById(R.id.tabs);
        ViewPager pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(new TPAdapter(getSupportFragmentManager(),
                getResources().getString(R.string.tab_word)));
        tabs.setupWithViewPager(pager);
        ((NavigationView)findViewById(R.id.navigation_view))
                .setNavigationItemSelectedListener(this);
        mTitle = mDrawerTitle = getTitle();
        initDrawerToggle();

    }

    private void initDrawerToggle(){
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                drawer,         /* DrawerLayout object */
                R.drawable.ic_open_menu_drawer,  /* nav drawer icon to replace 'Up' caret */
                R.string.open_menu_drawer,  /* "open drawer" description */
                R.string.close_menu_drawer  /* "close drawer" description */
        ) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getSupportActionBar().setTitle(mTitle);
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle(mDrawerTitle);
            }
        };

        // Set the drawer toggle as the DrawerListener
        drawer.setDrawerListener(mDrawerToggle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    protected void onResume(){
        super.onResume();
        if(!mCurrentTheme.equals(ThemeLoader.getTheme(this))){
            finish();
            startActivity(new Intent(this, this.getClass()));
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle your other action bar items...

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {


            //Checking if the item is in checked state or not, if not make it in checked state
        if(menuItem.isChecked()) menuItem.setChecked(false);
        else menuItem.setChecked(true);

            //Closing drawer on item click
        ((DrawerLayout)findViewById(R.id.drawer_layout)).closeDrawers();
            //drawerLayout.closeDrawers();

            //Check to see which item was being clicked and perform appropriate action
        menuItem.setChecked(false);
        switch (menuItem.getItemId()){


                //Replacing the main content with ContentFragment Which is our Inbox View;
            case R.id.menu_settings:
                Intent settingsIntent = new Intent(this, Prefs.class);
                settingsIntent.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT,
                        Prefs.Prefs1Fragment.class.getName());
                //settingsIntent.putExtra(PreferenceActivity.EXTRA_NO_HEADERS, true);
                startActivity(settingsIntent);
                return true;

            case R.id.menu_goTo1:
                ((TabLayout) findViewById(R.id.tabs)).getTabAt(0).select();;
                return true;
            case R.id.menu_goTo2:
                ((TabLayout) findViewById(R.id.tabs)).getTabAt(1).select();
                return true;
            case R.id.menu_goTo3:
                ((TabLayout) findViewById(R.id.tabs)).getTabAt(2).select();
                return true;
            default:
                return true;



        }
    }

}
