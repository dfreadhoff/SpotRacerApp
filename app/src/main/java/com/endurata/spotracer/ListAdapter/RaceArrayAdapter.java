package com.endurata.spotracer.ListAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.endurata.spotracer.R;
import java.util.ArrayList;

/**
 * Created by dfreadhoff on 8/21/2015.
 */
public class RaceArrayAdapter extends ArrayAdapter<String> {
    private Context context ;
    private ArrayList<String> values = new ArrayList() ;

    public RaceArrayAdapter(Context context, int resource, int textViewResourceId, String[] objects) {
        super(context, resource, textViewResourceId, objects);
    }

    public RaceArrayAdapter(Context context) {
        super(context, R.layout.item);
        this.context = context;
    }

    @Override
    public void add(String object) {
        values.add(object);
    }

    @Override
    public int getCount() {
        return values.size();
    }

    @Override
    public String getItem(int position) {
        return values.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.item, parent, false);

        TextView textView = (TextView) rowView.findViewById(R.id.list_item);
        textView.setText(values.get(position).split(",")[1]);

        TextView detailView = (TextView) rowView.findViewById(R.id.detail);
        detailView.setText("2015-03-12");

        return rowView ;
    }
}
