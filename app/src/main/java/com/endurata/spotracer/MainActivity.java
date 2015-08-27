package com.endurata.spotracer;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.endurata.spotracer.ListAdapter.RaceArrayAdapter ;

public class MainActivity extends ActionBarActivity implements AdapterView.OnItemClickListener {

    private String mAthleteId;
    private ListView mListView ;
    private RaceArrayAdapter mRaceAdapter ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        readSettings() ;
Log.d("AthleteID", "Id is:" + mAthleteId) ;
        if (mAthleteId.length() == 0) {
            Intent settingsIntent = new Intent(this, Settings.class);
            startActivity(settingsIntent);
        }
//9974250f-d2ae-41de-aa9c-563315b08e6a
        mListView = (ListView) findViewById(R.id.list);
        mRaceAdapter = new RaceArrayAdapter(MainActivity.this);

        mListView.setOnItemClickListener(this) ;
        new RetrieveCoursesTask().execute();
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

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent sttingsIntent = new Intent(MainActivity.this, FollowActivity.class);

        int itemPosition  = position;
        sttingsIntent.putExtra("key", mRaceAdapter.getItem(itemPosition).split(",")[0]);

        startActivity(sttingsIntent);
    }

    private class RetrieveCoursesTask extends AsyncTask<String, Integer, Long> {
        ProgressDialog progressDialog;
        String mRaceValues[] ;

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(MainActivity.this, "", "Downloading Races");
        }

        @Override
        protected Long doInBackground(String... parms) {
            WSAssistant wa = new WSAssistant("http://engine.endurata.com:8080/axis2/services/RacerTracerService/getCourses?customerId=" + mAthleteId);
            String response = wa.invokeService();
            mRaceValues = response.split(";");
             return 0l;
        }

        @Override
        protected void onPostExecute(Long result) {
            for (int i = 0; i < mRaceValues.length; i++)
               mRaceAdapter.add(mRaceValues[i]);
            mRaceAdapter.notifyDataSetChanged();
            mListView.setAdapter(mRaceAdapter);
            progressDialog.dismiss();
        }
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

}
