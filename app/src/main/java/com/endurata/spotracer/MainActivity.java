package com.endurata.spotracer;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import com.endurata.spotracer.RaceList.RaceArrayAdapter ;
import java.util.ArrayList;

public class MainActivity extends ActionBarActivity {


    private String athleteId;
    private String currentlyTransmitting = "false";
    private String lastLatitude = "0";
    private String lastLongitude = "0";
    private LocationManager locationManager;
    private LocationListener locationListener;
    private boolean bSimulatedRacer = false;
    private int indexWayPoints = 0;
    private String sFirstName;
    private String sLastName;
    private String sEmailAddress;
    private String sCity;
    private String sState;
    private String sCountry;
    ListView listView ;
    RaceArrayAdapter mRaceAdapter ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        readSettings() ;

        if (sLastName.length() == 1) {
            Intent settingsIntent = new Intent(this, Settings.class);
            startActivity(settingsIntent);
        }

        athleteId = "24" ;

        listView = (ListView) findViewById(R.id.list);
        mRaceAdapter = new RaceArrayAdapter(MainActivity.this);

        // ListView Item Click Listener
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // ListView Clicked item index
                int itemPosition     = position;
                // ListView Clicked item value
                String  itemValue    = (String) listView.getItemAtPosition(position);
                // Show Alert
                Toast.makeText(getApplicationContext(),
                        "Position :"+itemPosition+"  ListItem : " +itemValue , Toast.LENGTH_LONG)
                        .show();
            }
        });

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
            //sttingsIntent.putExtra("key", value); //Optional parameters
            startActivity(sttingsIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void readSettings() {
        SharedPreferences prefs = getSharedPreferences("RTA-main", 0);

        sFirstName = prefs.getString("firstName", "");
        sLastName = prefs.getString("lastName", "");
        sEmailAddress = prefs.getString("emailAddress", "");
        sCity = prefs.getString("city", "");
        sState = prefs.getString("state", "");
        sCountry = prefs.getString("country", "");
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
            WSAssistant wa = new WSAssistant("http://engine.endurata.com:8080/axis2/services/RacerTracerService/getCourses?customerId=" + athleteId);
            String response = wa.invokeService();
            mRaceValues = response.split(";");
             return 0l;
        }

        @Override
        protected void onPostExecute(Long result) {
            for (int i = 0; i < mRaceValues.length; i++)
               mRaceAdapter.add(mRaceValues[i]);
            mRaceAdapter.notifyDataSetChanged();
            listView.setAdapter(mRaceAdapter);
            progressDialog.dismiss();
        }
    }

    private class RegisterAthleteTask extends AsyncTask<String,Void,String> {

        protected String doInBackground(String... parms) {
            WSAssistant wa = new WSAssistant("http://engine.endurata.com:8080/axis2/services/RacerTracerService/registerCustomer");
            wa.setParameter("firstName", parms[0]);
            wa.setParameter("lastName", parms[1]);
            wa.setParameter("emailAddress", parms[2]);
            wa.setParameter("city", parms[3]);
            wa.setParameter("state", parms[4]);
            wa.setParameter("country", parms[5]);
            // Invoke the register customer service
            athleteId = wa.invokeService();

            WSAssistant wa2 = new WSAssistant("http://engine.endurata.com:8080/axis2/services/RacerTracerService/writeFollowingInfo");
            wa2.setParameter("courseId", "24"); // Illinois Marathon
            wa2.setParameter("spectatorId", "registered");
            wa2.setParameter("athleteId", athleteId);
            wa2.setParameter("isRacing", "true");
            wa2.setParameter("isAdd", "true");
            // Invoke the writeFollowingInfo service
            wa2.invokeService();

            return athleteId;
        }
    }

}
