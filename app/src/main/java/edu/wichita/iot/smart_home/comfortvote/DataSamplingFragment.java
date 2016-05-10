package edu.wichita.iot.smart_home.comfortvote;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.sql.SQLException;

/**
 * Created by Fariba on 4/6/2016.
 */
public class DataSamplingFragment extends DialogFragment{
    int mNum;
    TextView myVoteTextView;
    View rootView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_data_sampling, container, false);
        myVoteTextView = (TextView) rootView.findViewById(R.id.my_vote);
        configureButtons(rootView);


        return rootView;
    }


    private void  configureButtons(View v){
    }

    private void showDialog(String msgStr){
        new AlertDialog.Builder(getActivity())
                .setTitle("Alert")
                .setMessage(msgStr)
                .setCancelable(false)
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Whatever...
                    }
                }).create().show();
    }
}
