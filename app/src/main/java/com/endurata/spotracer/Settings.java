package com.endurata.spotracer;

import android.content.SharedPreferences;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;

public class Settings extends ActionBarActivity {

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
        String sFullName = editName.getText().toString();
        ed.putString("firstName", sFullName);

        EditText editLastName = (EditText) findViewById(R.id.editLastName);
        String sLastName = editLastName.getText().toString();
        ed.putString("LastName", sLastName);

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
    }
}
