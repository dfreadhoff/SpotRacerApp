package com.endurata.spotracer;

import android.app.Activity;
import android.app.ListFragment;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.endurata.spotracer.DataStructs.FollowAthleteStruct;
import com.endurata.spotracer.ListAdapter.FollowArrayAdapter;
import com.endurata.spotracer.utils.WSAssistant;

import java.util.ArrayList;


/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class FollowFragment extends ListFragment implements ListView.OnItemClickListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    public static final String ARG_ATHLETE = "AthleteID";
    public static final String ARG_COURSE = "CourseID";

    public void setCourseID(String mCourseID) {
        this.mCourseID = mCourseID;
    }

    public void setAthleteId(String mAthleteId) {
        this.mAthleteId = mAthleteId;
    }

    private String mCourseID;
    private String mAthleteId;

    private OnFragmentInteractionListener mListener;

    private ListView mListView ;
    private FollowArrayAdapter mAdapter ;
    private ArrayList<FollowAthleteStruct> mTransactions = new ArrayList() ;

    public static FollowFragment newInstance(String param1, String param2) {
        FollowFragment fragment = new FollowFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ATHLETE, param1);
        args.putString(ARG_COURSE, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public void setArguements(String athleteId, String courseId) {
        mAthleteId = athleteId ;
        mCourseID = courseId ;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public FollowFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);
     }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_follow_list, container, false);
        mListView = (ListView) view.findViewById(android.R.id.list);
        mAdapter = new FollowArrayAdapter(getActivity());

        if (getArguments() != null) {
            mAthleteId = getArguments().getString(ARG_ATHLETE);
            mCourseID = getArguments().getString(ARG_COURSE);
        }
        new RetrieveFollowTask().execute("", mAthleteId);

        final EditText lastNameEditText = (EditText) view.findViewById(R.id.textViewLastName);
        final TextWatcher TextChange = new TextWatcher() {
            public void afterTextChanged(Editable s) {
                new RetrieveFollowTask().execute(lastNameEditText.getText().toString(), "");
            }
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
        };

        lastNameEditText.addTextChangedListener(TextChange);

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        //mListener = (OnFragmentInteractionListener) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
    @Override
    public void onStop() {
        super.onStop();

        // OnStop is called when the fragment is detached
        new RegisterFollowTask().execute();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        CheckBox cb = (CheckBox) view.findViewById(R.id.checkBox);
        cb.setChecked(!cb.isChecked());
        FollowAthleteStruct person = mAdapter.getFollowAthleteStructItem(position);
        person.setIsFollowing(cb.isChecked() ? "1" : "0");
        mTransactions.add(person);
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
        public void onFragmentInteraction(String id);
    }

    private class RetrieveFollowTask extends AsyncTask<String, Integer, Long> {
        ProgressDialog progressDialog;
        String mValues[] ;

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(getActivity(), "", "Downloading Followers");
        }

        @Override
        protected Long doInBackground(String... parms) {
            // 0e66bb55-cd24-4835-a0e9-9e1d28bb7256,David,Erickson,Naperville,Il,USA,0;
            WSAssistant wa = new WSAssistant("http://engine.endurata.com:8080/axis2/services/RacerTracerService/getAllAthletes?spectatorId="+parms[1]+"&courseId="+mCourseID+"&lastNameFilter=" + parms[0]);
            String response = wa.invokeService();
            mValues = null ;
            if (response.length() > 0)
                mValues = response.split(";");
            return 0l;
        }

        @Override
        protected void onPostExecute(Long result) {
            mAdapter.setData(mValues);
            mAdapter.notifyDataSetChanged();
            mListView.setAdapter(mAdapter);
            progressDialog.dismiss();
        }
    }

    private class RegisterFollowTask extends AsyncTask<String,Void,String> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            //progressDialog = ProgressDialog.show(getActivity(), "", "Follow");
        }

        protected String doInBackground(String... parms) {
            for (FollowAthleteStruct person : mTransactions) {
                WSAssistant wa2 = new WSAssistant("http://engine.endurata.com:8080/axis2/services/RacerTracerService/writeFollowingInfo");// ?courseId=+mCourseID+ "&spectatorId=" +person.getAthleteId()+ "&athleteId=" +mAthleteId+ "&isRacing=" + "&isAdd="+person.getIsFollowing());
                wa2.setParameter("courseId", mCourseID); // Illinois Marathon
                wa2.setParameter("spectatorId", person.getAthleteId());
                wa2.setParameter("athleteId", mAthleteId);
                wa2.setParameter("isRacing", "false");
                wa2.setParameter("isAdd", person.getIsFollowing().equals("1") ? "true" : "false");
                // Invoke the writeFollowingInfo service
                wa2.invokeService();
            }
            mTransactions.clear();
            return mAthleteId;
        }

        protected void onPostExecute(Long result) {
            //progressDialog.dismiss();
        }
    }
}
