package unknown.salah;

import android.content.Intent;
import android.content.res.Configuration;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import unknown.salah.menu.DataModel;
import unknown.salah.menu.DrawerItemCustomAdapter;
import unknown.salah.pages.Ayarlar;
import unknown.salah.pages.Namaz;
import unknown.salah.pages.Rapor;

public class MainActivity extends AppCompatActivity {

    private String[] mNavigationDrawerItemTitles;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private Toolbar toolbar;
    private CharSequence mTitle;
    private ActionBarDrawerToggle mDrawerToggle;
    private TextView toolbartext;

    Fragment fragment = null;
    private Bundle dataBundle;

    private static final String SELECTED_DATE = "selectedDate";

    private static final String APP_LINK = "https://play.google.com/store/apps/details?id=unknown.salah";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTitle = getTitle();
        mNavigationDrawerItemTitles = getResources().getStringArray(R.array.navigation_drawer_items_array);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerToggle = setupDrawerToggle();
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        toolbartext = (TextView) findViewById(R.id.toolbar_title);
        setupToolbar();

        DataModel[] drawerItem = new DataModel[mNavigationDrawerItemTitles.length];

        drawerItem[0] = new DataModel(R.drawable.prayer, mNavigationDrawerItemTitles[0]);
        drawerItem[1] = new DataModel(R.drawable.analytics, mNavigationDrawerItemTitles[1]);
        drawerItem[2] = new DataModel(R.drawable.settings, mNavigationDrawerItemTitles[2]);
        drawerItem[3] = new DataModel(R.drawable.community, mNavigationDrawerItemTitles[3]);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        //getSupportActionBar().getDrawerArrowDrawable().setColor(getResources().getColor(R.color.white));

        DrawerItemCustomAdapter adapter = new DrawerItemCustomAdapter(this, R.layout.list_menu_item, drawerItem);
        mDrawerList.setAdapter(adapter);
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        fragment = new Namaz();
        dataBundle = new Bundle();
        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            dataBundle.putBoolean(SELECTED_DATE, false);
            fragment.setArguments(dataBundle);
            fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();

            mDrawerList.setItemChecked(0, true);
            mDrawerList.setSelection(0);
            toolbartext.setText(mNavigationDrawerItemTitles[0]);
            setTitle("");
            mDrawerLayout.closeDrawer(mDrawerList);

        } else {
            Log.e("MainActivity", "Error in creating fragment");
        }

    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }

    }

    private void selectItem(int position) {
        switch (position) {
            case 0:
                fragment = new Namaz();
                toolbartext.setText(mNavigationDrawerItemTitles[0]);
                break;
            case 1:
                fragment = new Rapor();
                toolbartext.setText(mNavigationDrawerItemTitles[1]);
                break;
            case 2:
                Intent intent = new Intent(this, Ayarlar.class);
                startActivity(intent);
                fragment = null;
                toolbartext.setText(mNavigationDrawerItemTitles[2]);
                break;
            case 3:
                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                String shareText = getResources().getString(R.string.app_name) + "\n" +
                        "------------------------------" + "\n" + getResources().getString(R.string.namaz_takip_share) +"\n" + APP_LINK;
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareText);
                startActivity(Intent.createChooser(sharingIntent, "Share via"));
                return;
            default:
                break;
        }

        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            dataBundle.putBoolean(SELECTED_DATE, false);
            fragment.setArguments(dataBundle);
            fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();
            setTitle("");
            mDrawerList.setItemChecked(position, true);
            mDrawerList.setSelection(position);
            mDrawerLayout.closeDrawer(mDrawerList);

        } else {
            Log.e("MainActivity", "Error in creating fragment");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);

    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getSupportActionBar().setTitle(mTitle);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    void setupToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    private ActionBarDrawerToggle setupDrawerToggle() {
        return new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
    }
}
