package com.endurata.spotracer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A simple {@link Fragment} subclass.
 */
public class RoleAlert extends DialogFragment implements DialogInterface.OnClickListener {

    private RoleAlertListener mListener = null ;

    public RoleAlert() {
    }

    public void addListener(RoleAlertListener listener){
        mListener = listener ;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity()) ;
        builder.setTitle("Role") ;
        String items[] = {"Spectator","Racer"} ;
        builder.setItems(items, this) ;
        builder.setNegativeButton("Cancel", this);
        return builder.create() ;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        mListener.onAlertClick(which);
    }

    public interface RoleAlertListener {
        public void onAlertClick(int which) ;
    }
}
