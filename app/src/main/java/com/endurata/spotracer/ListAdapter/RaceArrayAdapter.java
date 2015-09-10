package com.endurata.spotracer.ListAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.endurata.spotracer.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

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

        String parts[] = values.get(position).split(",") ;
        TextView textView = (TextView) rowView.findViewById(R.id.list_item);
        textView.setText(parts[1]);

        String raceDate = (parts[3].equals("null")) ? new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime()) : parts[3] ;
        if (parts[8].equals("null"))parts[8] = "0" ;
        if (parts[9].equals("null"))parts[9] = "0" ;
        String registered = getContext().getResources().getString(parts[8].equals("0") ? R.string.register_not :
                (parts[9].equals("0") ? R.string.register_spectator : R.string.register_racer)) ;

        TextView detailView = (TextView) rowView.findViewById(R.id.detail);
        detailView.setText(raceDate + ", " + registered);

        return rowView ;
    }
}
