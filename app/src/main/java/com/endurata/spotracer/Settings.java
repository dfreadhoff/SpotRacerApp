package com.endurata.spotracer;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

public class Settings extends ActionBarActivity {
    private  String mAthleteId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        readSettings() ;
    }

    public void onBackPressed(){
        commitSettings() ;
        Toast.makeText(getApplicationContext(), "Profile Saved", Toast.LENGTH_SHORT).show();
        this.finish();
    }

    private void readSettings() {
        SharedPreferences prefs = getSharedPreferences("RTA-main", 0);

        mAthleteId = prefs.getString("AthleteId", "");

        String fullName = prefs.getString("firstName", "");
        EditText editName = (EditText) findViewById(R.id.editName);
        editName.setText(fullName);

        String lastName = prefs.getString("lastName", "");
        EditText editLastName = (EditText) findViewById(R.id.editLastName);
        editLastName.setText(lastName);

        String emailAddress = prefs.getString("emailAddress", "");
        EditText editEmail = (EditText) findViewById(R.id.editEmail);
        editEmail.setText(emailAddress);

        String city = prefs.getString("city", "");
        EditText editCity = (EditText) findViewById(R.id.editCity);
        editCity.setText(city);

        String state = prefs.getString("state", "");
        EditText editState = (EditText) findViewById(R.id.editState);
        editState.setText(state);

        String country = prefs.getString("country", "");
        EditText editCountry = (EditText) findViewById(R.id.editCountry);
        editCountry.setText(country);
    }

    private void commitSettings() {
        SharedPreferences prefs = getSharedPreferences("RTA-main", 0);
        SharedPreferences.Editor ed = prefs.edit();

        EditText editName = (EditText) findViewById(R.id.editName);
        String sFirstName = editName.getText().toString();
        ed.putString("firstName", sFirstName);

        EditText editLastName = (EditText) findViewById(R.id.editLastName);
        String sLastName = editLastName.getText().toString();
        ed.putString("lastName", sLastName);

        EditText editEmail = (EditText) findViewById(R.id.editEmail);
        String sEmail = editEmail.getText().toString();
        ed.putString("emailAddress", sEmail);

        EditText editCity = (EditText) findViewById(R.id.editCity);
        String city = editCity.getText().toString();
        ed.putString("city", city);

        EditText editState = (EditText) findViewById(R.id.editState);
        String state = editState.getText().toString();
        ed.putString("state", state);

        EditText editCountry = (EditText) findViewById(R.id.editCountry);
        String country = editCountry.getText().toString();
        ed.putString("country", country);

        ed.commit();

        if (mAthleteId.length() == 0)
            new RetrieveCoursesTask().execute(sFirstName, sLastName, sEmail, city, state, country);
    }

    private class RetrieveCoursesTask extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(Settings.this, "", "Adding User");
        }

        @Override
        protected String doInBackground(String... parms) {
            WSAssistant wa = new WSAssistant("http://engine.endurata.com:8080/axis2/services/RacerTracerService/registerCustomer");
            wa.setParameter("firstName", parms[0]);
            wa.setParameter("lastName", parms[1]);
            wa.setParameter("emailAddress", parms[2]);
            wa.setParameter("city", parms[3]);
            wa.setParameter("state", parms[4]);
            wa.setParameter("country", parms[5]);

            // Invoke the register customer service
            mAthleteId = wa.invokeService();
            Log.d("WS", "Athlete ID is " + mAthleteId) ;
            return mAthleteId ;
        }

        @Override
        protected void onPostExecute(String athleteId) {
            SharedPreferences prefs = getSharedPreferences("RTA-main", 0);
            SharedPreferences.Editor ed = prefs.edit();
            ed.putString("AthleteId", athleteId);
            ed.commit();
            progressDialog.dismiss();
            Toast.makeText(getApplicationContext(), "Profile Saved" + mAthleteId, Toast.LENGTH_SHORT).show();
            Settings.this.finish();
        }
    }
}
