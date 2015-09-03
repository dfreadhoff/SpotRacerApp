package com.endurata.spotracer;

import android.app.Activity;
import android.app.ListFragment;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.endurata.spotracer.ListAdapter.RaceArrayAdapter;
import com.endurata.spotracer.utils.WSAssistant;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnRaceInteractionListener}
 * interface.
 */
public class RaceFragment extends ListFragment implements AdapterView.OnItemClickListener {

    private RaceArrayAdapter mRaceAdapter ;

    public static final String ARG_ATHLETE = "athlete";

    private OnRaceInteractionListener mListener;

    /**
     * The fragment's ListView/GridView.
     */
    private ListView mListView;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private ListAdapter mAdapter;

    private String mAthleteId;


    public static RaceFragment newInstance(String param1) {
        RaceFragment fragment = new RaceFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ATHLETE, param1);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public RaceFragment() {
    }

    public void setAthleteId(String athleteId) {
        this.mAthleteId = mAthleteId;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mAthleteId = getArguments().getString(ARG_ATHLETE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item_list, container, false);

        // Set the adapter
        mListView = (ListView) view.findViewById(android.R.id.list);
        mRaceAdapter = new RaceArrayAdapter(getActivity());

        // Populate the list from web service
        new RetrieveCoursesTask().execute();

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnRaceInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        int c = 4 ;
        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            mListener.onRaceInteraction(mRaceAdapter.getItem(position).toString().split(","));
        }
    }

    /**
     * The default content for this Fragment has a TextView that is shown when
     * the list is empty. If you would like to change the text, call this method
     * to supply the text it should use.
     */
    public void setEmptyText(CharSequence emptyText) {
        View emptyView = mListView.getEmptyView();

        if (emptyView instanceof TextView) {
            ((TextView) emptyView).setText(emptyText);
        }
    }

    public interface OnRaceInteractionListener {
        public void onRaceInteraction(String raceData[]);
    }

    private class RetrieveCoursesTask extends AsyncTask<String, Integer, Long> {
        ProgressDialog progressDialog;
        String mRaceValues[] ;

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(getActivity(), "", "Downloading Races");
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


}
