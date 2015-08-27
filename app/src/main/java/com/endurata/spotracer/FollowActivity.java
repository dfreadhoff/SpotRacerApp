package com.endurata.spotracer;

import android.app.Fragment;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;

public class FollowActivity extends ActionBarActivity implements Button.OnClickListener, FollowFragment.OnFragmentInteractionListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow);

        Button btn = (Button) findViewById(R.id.button) ;
        btn.setOnClickListener(this);

        Bundle bundl = new Bundle();
        bundl.putString("AthleteID", "9974250f-d2ae-41de-aa9c-563315b08e6a");
        bundl.putString("CourseID", "94");

        FollowFragment f = (FollowFragment) getFragmentManager().findFragmentById(R.id.follow_fragment) ;
        f.setArguements("9974250f-d2ae-41de-aa9c-563315b08e6a", "94") ;
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

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button) {
            Intent mapIntent = new Intent(FollowActivity.this, MapsActivity.class);
            startActivity(mapIntent);
            FollowFragment f = (FollowFragment) getFragmentManager().findFragmentById(R.id.follow_fragment) ;
            f.onFinish();
        }
    }

    @Override
    public void onFragmentInteraction(String id) {

    }
}
