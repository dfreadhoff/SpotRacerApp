package com.endurata.spotracer;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TabHost;

import com.endurata.spotracer.utils.WSAssistant;

public class MainActivity extends FragmentActivity implements RaceFragment.OnRaceInteractionListener {

    private String mAthleteId;
    private String mCourseId;
    private TabHost mTabHost;
    private boolean mIsAthlete;
    private String mCourseGPX;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        readSettings() ;

        if (mAthleteId.length() == 0) {
            Intent settingsIntent = new Intent(this, Settings.class);
            startActivity(settingsIntent);
        }
//9974250f-d2ae-41de-aa9c-563315b08e6a
        mTabHost = (TabHost)findViewById(android.R.id.tabhost);
        mTabHost.setup();

        mTabHost.setOnTabChangedListener(tabChangeListener);

        TabHost.TabSpec raceTab = mTabHost.newTabSpec("race");
        raceTab.setIndicator("Course");//",getResources().getDrawable(R.drawable.android
        raceTab.setContent(new DummyTabContent(getBaseContext()));
        mTabHost.addTab(raceTab);

        TabHost.TabSpec followTab = mTabHost.newTabSpec("follow");
        followTab.setIndicator("Follow");
        followTab.setContent(new DummyTabContent(getBaseContext()));
        mTabHost.addTab(followTab);

        TabHost.TabSpec mapTab = mTabHost.newTabSpec("map");
        mapTab.setIndicator("Map");
        mapTab.setContent(new DummyTabContent(getBaseContext()));
        mTabHost.addTab(mapTab);
    }

    @Override
    public void onRaceInteraction(String raceData[]) {
        mCourseId = raceData[0] ;
        mCourseGPX = raceData[7] ;

        RoleAlert roleAlert = new RoleAlert();
        roleAlert.show(getFragmentManager(), "roleAlert");
        roleAlert.addListener(new RoleAlert.RoleAlertListener(){
            @Override
            public void onAlertClick(int which) {
                //Toast.makeText(MainActivity.this, "which is " + which, Toast.LENGTH_SHORT).show();
                if (which != -2) {  // negative 2 is cancel
                    mIsAthlete = (which == 1) ;  // index 0 is spectator, index 1 is athlete
                    mTabHost.setCurrentTab(1);
                }
            }
        });
    }

    public class DummyTabContent implements TabHost.TabContentFactory {
        private Context mContext;

        public DummyTabContent(Context context){
            mContext = context;
        }

        @Override
        public View createTabContent(String tag) {
            View v = new View(mContext);
            return v;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent sttingsIntent = new Intent(this, Settings.class);
            startActivity(sttingsIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void readSettings() {
        SharedPreferences prefs = getSharedPreferences("RTA-main", 0);
        mAthleteId = prefs.getString("AthleteId", "");
    }

    private class RegisterAthleteTask extends AsyncTask<String,Void,String> {

        protected String doInBackground(String... parms) {
            WSAssistant wa2 = new WSAssistant("http://engine.endurata.com:8080/axis2/services/RacerTracerService/writeFollowingInfo");
            wa2.setParameter("courseId", "24"); // Illinois Marathon
            wa2.setParameter("spectatorId", "registered");
            wa2.setParameter("athleteId", mAthleteId);
            wa2.setParameter("isRacing", "true");
            wa2.setParameter("isAdd", "true");
            // Invoke the writeFollowingInfo service
            wa2.invokeService();

            return mAthleteId;
        }
    }

    private String mLastTab="0";
    TabHost.OnTabChangeListener tabChangeListener = new TabHost.OnTabChangeListener() {

        @Override
        public void onTabChanged(String tabId) {
            mLastTab = tabId ;
            FragmentManager fm = getFragmentManager();
            RaceFragment raceFragment = (RaceFragment) fm.findFragmentByTag("race");
            FollowFragment followFragment = (FollowFragment) fm.findFragmentByTag("follow");
            MapFragment mapFragment = (MapFragment) fm.findFragmentByTag("map");
            FragmentTransaction ft = fm.beginTransaction();

            if(raceFragment!=null)
                ft.detach(raceFragment);
            if(followFragment!=null) {
                ft.detach(followFragment);
                if (mLastTab.equals("1"))
                    followFragment.onFinish();
            }
            if(mapFragment!=null)
                ft.detach(mapFragment);

            if(tabId.equalsIgnoreCase("race")) {
                if (raceFragment == null) {
                    raceFragment = new RaceFragment() ;
                    ft.add(android.R.id.tabcontent, raceFragment, "race");
                } else {
                    ft.attach(raceFragment);
                }
                raceFragment.setAthleteId(mAthleteId);
            }else if(tabId.equalsIgnoreCase("follow")){
                if(followFragment==null){
                    followFragment = new FollowFragment() ;
                    ft.add(android.R.id.tabcontent, followFragment, "follow");
                }else{
                    ft.attach(followFragment);
                }
                followFragment.setAthleteId(mAthleteId);
                followFragment.setCourseID(mCourseId);
            }else{
                if(mapFragment==null){
                    mapFragment = new MapFragment() ;
                    ft.add(android.R.id.tabcontent, mapFragment, "map");
                }else{
                    ft.attach(mapFragment);
                }
                mapFragment.setCourse(mCourseGPX) ;
            }
            ft.commit();
        }
    };
}
