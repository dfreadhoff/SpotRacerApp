package com.endurata.spotracer;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TabHost;

import com.endurata.spotracer.utils.WSAssistant;

import java.util.HashMap;

public class MainActivity extends FragmentActivity implements RaceFragment.OnRaceInteractionListener {

    private String mAthleteId;
    private String mCourseId;
    private TabHost mTabHost;
    private boolean mIsAthlete;
    private boolean mIsRegistered ;
    private String mCourseGPX;
    public static HashMap FOLLOWEES = new HashMap();

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
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        Bundle args = new Bundle();
        args.putString(RaceFragment.ARG_ATHLETE, mAthleteId);
        Fragment frags[] = { new RaceFragment(), new FollowFragment(), new MapFragment() };
        String fragNames[] = {"race", "follow", "map"} ;
        String tabNames[] = {"Course", "Follow", "Map"} ;

        mTabHost = (TabHost)findViewById(android.R.id.tabhost);
        mTabHost.setup();

        for (int i = 0; i < frags.length; i++) {
            frags[i].setArguments(args) ;
            ft.add(android.R.id.tabcontent, frags[i], fragNames[i]);
            if (i > 0) ft.detach(frags[i]) ;

            TabHost.TabSpec tab = mTabHost.newTabSpec(fragNames[i]);
            tab.setIndicator(tabNames[i]);
            tab.setContent(new DummyTabContent(getBaseContext()));
            mTabHost.addTab(tab);
        }
        ft.commit();

        mTabHost.setOnTabChangedListener(tabChangeListener);

    }

    @Override
    public void onRaceInteraction(String raceData[]) {
        mCourseId = raceData[0] ;
        mCourseGPX = raceData[7] ;
        mIsRegistered = (raceData[8].equals("1")) ;

        RoleAlert roleAlert = new RoleAlert();
        if (mIsRegistered) roleAlert.setUnregisterFlag();
        roleAlert.show(getFragmentManager(), "roleAlert");
        roleAlert.addListener(new RoleAlert.RoleAlertListener(){
            @Override
            public void onAlertClick(int which) {
                //Toast.makeText(MainActivity.this, "which is " + which, Toast.LENGTH_SHORT).show();
                if (which != -2) {  // negative 2 is cancel
                    mIsAthlete = (which == 1) ;  // index 0 is spectator, index 1 is athlete
                    new RegisterAthleteTask().execute(which==2 ? "false" : "true");  // if unregister, pass isAdd flag as false
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

    private String mLastTab="race";
    TabHost.OnTabChangeListener tabChangeListener = new TabHost.OnTabChangeListener() {

        @Override
        public void onTabChanged(String tabId) {
            FragmentManager fm = getFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();

            Fragment last = fm.findFragmentByTag(mLastTab);
            if(last != null) ft.detach(last);
            mLastTab = tabId ;

            Bundle args = new Bundle();
            args.putString(RaceFragment.ARG_ATHLETE, mAthleteId);
            Fragment frag = fm.findFragmentByTag(tabId) ;
            frag.getArguments().putString(RaceFragment.ARG_ATHLETE, mAthleteId);
            frag.getArguments().putString(FollowFragment.ARG_COURSE,  mCourseId);
            frag.getArguments().putString(MapFragment.ARG_COURSE_GFX, mCourseGPX);
            ft.attach(frag);
            ft.commit();
        }
    };


    private class RegisterAthleteTask extends AsyncTask<String, Integer, Long> {

        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(MainActivity.this, "", "Updating Participant");
        }

        protected Long doInBackground(String... parms) {
            WSAssistant wa2 = new WSAssistant("http://engine.endurata.com:8080/axis2/services/RacerTracerService/" + (mIsRegistered  ? "updateRacingFlag" : "writeFollowingInfo"));
            wa2.setParameter("courseId", mCourseId);
            wa2.setParameter("spectatorId", "registered");
            wa2.setParameter("athleteId", mAthleteId);
            wa2.setParameter("isRacing", mIsAthlete ? "true" : "false");
            wa2.setParameter("isAdd", parms[0]);
            // Invoke the writeFollowingInfo service
            String ret = wa2.invokeService();
            Log.d("WS", ret) ;
            return 0l;
        }

        protected void onPostExecute(Long result) {
            progressDialog.dismiss();
        }
    }}
