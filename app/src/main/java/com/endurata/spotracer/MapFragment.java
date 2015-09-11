package com.endurata.spotracer;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.endurata.spotracer.DataStructs.FollowAthleteStruct;
import com.endurata.spotracer.javagpx.GPXParser;
import com.endurata.spotracer.javagpx.types.GPX;
import com.endurata.spotracer.javagpx.types.Track;
import com.endurata.spotracer.javagpx.types.TrackSegment;
import com.endurata.spotracer.javagpx.types.Waypoint;
import com.endurata.spotracer.utils.SystemTimerAndroid;
import com.endurata.spotracer.utils.WSAssistant;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MapFragment extends Fragment implements View.OnClickListener, SystemTimerAndroid.SystemTimerListener {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    private OnFragmentInteractionListener mListener;
    private SystemTimerAndroid mUpdateTimer;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Marker mMyMarker;
    private GPX mGPX;
    private View mView;
    private boolean mMapIsDirty;
    private Marker mAthleteMarker;

    public void setAthleteId(String mAthleteId) {
        this.mAthleteId = mAthleteId;
    }

    private String mAthleteId;
    public static final String ARG_COURSE_GFX = "course";

    public void setCourse(String course) {
        if (!this.mCourse.equals(course))
            mMapIsDirty = true ;
        this.mCourse = course;
    }

    private String mCourse ;

    public static MapFragment newInstance(String param1, String param2) {
        MapFragment fragment = new MapFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MapFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get GPS location
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (mView == null) mView = inflater.inflate(R.layout.activity_maps, container, false);

        Button transmitButton = (Button) mView.findViewById(R.id.transmitButton);
        transmitButton.setOnClickListener(this);

        if (getArguments() != null) {
            setCourse(this.getArguments().getString(ARG_COURSE_GFX)) ;
            mAthleteId = this.getArguments().getString(RaceFragment.ARG_ATHLETE) ;
        }

        setUpMapIfNeeded();
        return mView;
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            FragmentActivity app = (FragmentActivity) getActivity();
            mMap = ((SupportMapFragment) app.getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
            // Check if we were successful in obtaining the map.
            }
        if (mMapIsDirty && mMap != null && !mCourse.equals("Training.gpx")) {
            new RetrieveCourseGPXTask().execute();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View v) {
        String transOff = getResources().getString(R.string.trans_button_off);
        String transOn = getResources().getString(R.string.trans_button_on);

        Button b = (Button) v;
        if (b.getText().equals(transOn)) {
            b.setText(transOff);
            transmitLocation() ;
            mUpdateTimer = new SystemTimerAndroid(3000, this) ;
        } else {
            new GetAthletePositionTask().execute() ;
            b.setText(transOn);
            stopTransmitting() ;
            mUpdateTimer.killTimer();
            mUpdateTimer = null ;
        }
    }

    public interface OnFragmentInteractionListener {
        public void onFragmentInteraction(String id);
    }

    @Override
    public void onSystemTimeSignal() {
        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);;
        if (location != null) {
            String lastLatitude = Location.convert(location.getLatitude(), Location.FORMAT_DEGREES);
            String lastLongitude = Location.convert(location.getLongitude(), Location.FORMAT_DEGREES);
            new SendAthletePositionTask().execute(lastLatitude, lastLongitude, "true");
            new GetAthletePositionTask().execute() ;
        }
    }

    public void transmitLocation() {
        // Define a listener that responds to location updates
        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the location provider
                LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude()) ;
                mAthleteMarker.setPosition(currentLocation);

                // Send location to web service
                String lastLatitude = Location.convert(location.getLatitude(), Location.FORMAT_DEGREES);
                String lastLongitude = Location.convert(location.getLongitude(), Location.FORMAT_DEGREES);
                new SendAthletePositionTask().execute(lastLatitude, lastLongitude, "true");
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}
            public void onProviderEnabled(String provider) {}
            public void onProviderDisabled(String provider) {}
        };

        // Register the listener with the Location Manager to receive location updates every 15 seconds
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 15000, 0, locationListener);
    }

    /** Called when the user clicks the Stop Transmitting button */
    public void stopTransmitting() {
        // Stop sending athlete position to server by removing the LocationListener
        locationManager.removeUpdates(locationListener);
        // Update currentlyTransmitting status and location position
        new SendAthletePositionTask().execute("0", "0", "false");
    }

    private class SendAthletePositionTask extends AsyncTask<String,Void,String> {

        protected String doInBackground(String... parms) {
            WSAssistant wa = new WSAssistant("http://engine.endurata.com:8080/axis2/services/RacerTracerService/sendAthletePosition");
            wa.setParameter("athleteId", mAthleteId);
            wa.setParameter("lastLatitude", parms[0]);
            wa.setParameter("lastLongitude", parms[1]);
            wa.setParameter("currentlyTransmitting", parms[2]);
            // Invoke the sendAthletePosition service
            String response = wa.invokeService();

            return response;
        }
    }

    private class RetrieveCourseGPXTask extends AsyncTask<String, Integer, String> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(getActivity(), "", "Downloading Course");
        }

        @Override
        protected String doInBackground(String... parms) {
            try {
                InputStream input = new URL("http://engine.endurata.com/GPX/" + mCourse.replaceAll(" ", "%20")).openStream();
                mGPX = GPXParser.parse(input) ;
                input.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "" ;
        }

        @Override
        protected void onPostExecute(String result) {
            List<LatLng> points = new ArrayList() ;
            for (Track track : mGPX.getTracks()) {
                for (TrackSegment seg : track.getTrackSegments()){
                    for (Waypoint pt : seg.getWaypoints()) {
                        LatLng latlon = new LatLng(pt.getLat(), pt.getLon()) ;
                        points.add(latlon) ;
                    }
                }
            }
            mMap.clear();
            PolylineOptions lineOptions = new PolylineOptions().width(6).color(Color.BLUE);
            Polyline lineRoute = mMap.addPolyline(lineOptions);
            lineRoute.setPoints(points);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(points.get(0), 13));
            mMapIsDirty = false ;
            progressDialog.dismiss();
        }
    }

    private class GetAthletePositionTask extends AsyncTask<String,Void,String> {

        protected String doInBackground(String... parms) {
            WSAssistant wa = new WSAssistant("http://engine.endurata.com:8080/axis2/services/RacerTracerService/getAthletesPosition?followingList=9974250f-d2ae-41de-aa9c-563315b08e6a");
            // Invoke the sendAthletePosition service
            String response = wa.invokeService();

            Iterator i = MainActivity.FOLLOWEES.entrySet().iterator();
            while (i.hasNext()) {
                Map.Entry obj = (Map.Entry) i.next();
                FollowAthleteStruct athlete = (FollowAthleteStruct) obj.getValue();
                athlete.setIsTransmitting(false);
            }

            String follows[] = response.split(";");
            for (String person : follows) {
                String athlete = person.split(",")[0];
                Double latitude = Double.parseDouble(person.split(",")[1]);
                Double longitude = Double.parseDouble(person.split(",")[2]);
                LatLng currentPos = new LatLng(latitude, longitude);

                FollowAthleteStruct followee = (FollowAthleteStruct) MainActivity.FOLLOWEES.get(athlete);
                followee.setIsTransmitting(true);
                ;
                Marker athleteMarker = followee.getMarker();
                if (athleteMarker == null) {
                    followee.setMarker(mMap.addMarker(new MarkerOptions().position(currentPos).title(followee.getLastName())));
                } else {
                    athleteMarker.setPosition(currentPos);
                }

            }
            Iterator j = MainActivity.FOLLOWEES.entrySet().iterator();
            while (j.hasNext()) {
                Map.Entry obj = (Map.Entry) j.next();
                FollowAthleteStruct athlete = (FollowAthleteStruct) obj.getValue();
                if (!athlete.isTransmitting() && athlete.getMarker() != null) {
                    athlete.getMarker().remove();
                    athlete.setMarker(null);
                }
            }
            return "" ;
        }
    }
}
