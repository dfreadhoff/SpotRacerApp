package com.endurata.spotracer.RaceList;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.endurata.spotracer.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by dfreadhoff on 8/21/2015.
 */
public class RaceArrayAdapter extends ArrayAdapter<String> {
    private Context context ;
    //private String[] values ;
    private ArrayList<String> values = new ArrayList() ;

    public RaceArrayAdapter(Context context, int resource, int textViewResourceId, String[] objects) {
        super(context, resource, textViewResourceId, objects);

    }

    public RaceArrayAdapter(Context context) {
        super(context, R.layout.item);
        this.context = context;
        this.values = values;
    }

    public void add(String object) {
        values.add(object);
    }

    @Override
    public int getCount() {
        return values.size();
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.item, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.list_item);
        if (values != null)
            textView.setText(values.get(position).split(",")[1]);
        TextView detailView = (TextView) rowView.findViewById(R.id.detail);
        detailView.setText("2015-03-12");

        return rowView ;
    }
}
