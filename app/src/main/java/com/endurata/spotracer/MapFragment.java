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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

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

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class MapFragment extends Fragment implements View.OnClickListener, SystemTimerAndroid.SystemTimerListener {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    private OnFragmentInteractionListener mListener;
    private SystemTimerAndroid mUpdateTimer;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Marker mMyMarker;
    private GPX mGPX;

    public void setAthleteId(String mAthleteId) {
        this.mAthleteId = mAthleteId;
    }

    private String mAthleteId;

    public void setCourse(String mCourse) {
        this.mCourse = mCourse;
    }

    private String mCourse;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */

    // TODO: Rename and change types of parameters
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

        if (getArguments() != null) {
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_maps, container, false);
        Button transmitButton = (Button) view.findViewById(R.id.transmitButton);
        transmitButton.setOnClickListener(this);

        // Get GPS location
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        setUpMapIfNeeded();
        return view;
    }
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            FragmentActivity app = (FragmentActivity) getActivity();
            mMap = ((SupportMapFragment) app.getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
//        try {
//            mListener = (OnFragmentInteractionListener) activity;
//        } catch (ClassCastException e) {
//            throw new ClassCastException(activity.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void setUpMap() {
        new RetrieveCourseGPXTask().execute();

        mMyMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
    }

    @Override
    public void onClick(View v) {
        String transOff = getResources().getString(R.string.trans_button_off);
        String transOn = getResources().getString(R.string.trans_button_on);

        Button b = (Button) v;
        if (b.getText().equals(transOn)) {
            b.setText(transOff);
            //transmitLocation() ;
            mUpdateTimer = new SystemTimerAndroid(3000, this) ;
        } else {
            b.setText(transOn);
            //stopTransmitting() ;
            mUpdateTimer.killTimer();
            mUpdateTimer = null ;
        }
    }

    @Override
    public void onSystemTimeSignal() {
        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);;
        if (location != null) {
            LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude()) ;
            mMyMarker.setPosition(currentLocation);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
            String lastLatitude = Location.convert(location.getLatitude(), Location.FORMAT_DEGREES);
            String lastLongitude = Location.convert(location.getLongitude(), Location.FORMAT_DEGREES);
            //new SendAthletePositionTask().execute(lastLatitude, lastLongitude, "true");
        }
    }

    public void transmitLocation() {
        // Define a listener that responds to location updates
        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the location provider
                String lastLatitude = Location.convert(location.getLatitude(), Location.FORMAT_DEGREES);
                String lastLongitude = Location.convert(location.getLongitude(), Location.FORMAT_DEGREES);
                // Send location to web service
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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(String id);
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
            PolylineOptions lineOptions = new PolylineOptions().width(6).color(Color.BLUE);
            Polyline lineRoute = mMap.addPolyline(lineOptions);
            lineRoute.setPoints(points);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(points.get(0), 13));

            progressDialog.dismiss();
        }
    }

}
