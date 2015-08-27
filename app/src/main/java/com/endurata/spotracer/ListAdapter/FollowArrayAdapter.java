package com.endurata.spotracer.ListAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.endurata.spotracer.DataStructs.FollowAthleteStruct;
import com.endurata.spotracer.R;

import java.util.ArrayList;

/**
 * Created by dfreadhoff on 8/21/2015.
 */
public class FollowArrayAdapter extends ArrayAdapter<String> {
    private Context context ;
    private ArrayList<FollowAthleteStruct> mValues = new ArrayList() ;

    public FollowArrayAdapter(Context context, int resource, int textViewResourceId, String[] objects) {
        super(context, resource, textViewResourceId, objects);
    }

    public FollowArrayAdapter(Context context) {
        super(context, R.layout.item);
        this.context = context;
    }

    public void setData(String object[]) {
        mValues.clear();
        if (object == null) return ;

        for (int i = 0; i < object.length; i++) {
            FollowAthleteStruct person = new FollowAthleteStruct(object[i]);
            mValues.add(person);
        }
    }

    @Override
    public int getCount() {
        return mValues.size() ;
    }

    public FollowAthleteStruct getFollowAthleteStructItem(int position) {
        return mValues.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.item_follow, parent, false);

        TextView textView = (TextView) rowView.findViewById(R.id.list_item);
        textView.setText(mValues.get(position).getFirstName() + " " + mValues.get(position).getLastName());

        TextView detailView = (TextView) rowView.findViewById(R.id.detail);
        detailView.setText("2015-03-12");

        CheckBox cb = (CheckBox) rowView.findViewById(R.id.checkBox);
        cb.setChecked(mValues.get(position).getIsFollowing().equals("1"));

        return rowView ;
    }
}
