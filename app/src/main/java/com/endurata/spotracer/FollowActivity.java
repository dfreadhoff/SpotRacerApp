package com.endurata.spotracer;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.endurata.spotracer.RaceList.RaceArrayAdapter;

public class FollowActivity extends ActionBarActivity {
    ListView listView ;
    RaceArrayAdapter mRaceAdapter ;
    private String athleteId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow);

        listView = (ListView) findViewById(R.id.list);
        mRaceAdapter = new RaceArrayAdapter(this);

        // ListView Item Click Listener
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // ListView Clicked item index
                int itemPosition = position;
                // ListView Clicked item value
                String itemValue = (String) listView.getItemAtPosition(position);
                // Show Alert
                Toast.makeText(getApplicationContext(),
                        "Position :" + itemPosition + "  ListItem : " + itemValue, Toast.LENGTH_LONG)
                        .show();
            }
        });

        new RetrieveFollowTask().execute();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_follow, menu);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class RetrieveFollowTask extends AsyncTask<String, Integer, Long> {
        ProgressDialog progressDialog;
        String mRaceValues[] ;

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(FollowActivity.this, "", "Downloading Followers");
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


}
