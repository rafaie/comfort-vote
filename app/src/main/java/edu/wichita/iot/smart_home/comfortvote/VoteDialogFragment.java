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

import org.w3c.dom.Text;

import java.sql.SQLException;

import edu.wichita.iot.smart_home.comfortvote.R;

/**
 * Created by Fariba on 4/6/2016.
 */
public class VoteDialogFragment extends DialogFragment{
    int mNum;
    TextView myVoteTextView;
    View rootView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_dialog_vote, container, false);
        myVoteTextView = (TextView) rootView.findViewById(R.id.my_vote);
        configureButtons(rootView);


        return rootView;
    }


    private void  configureButtons(View v){

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myVoteTextView.setText(((Button) v).getText());
            }
        };

        ((Button) v.findViewById(R.id.btn_1)).setOnClickListener(onClickListener);
        ((Button) v.findViewById(R.id.btn_2)).setOnClickListener(onClickListener);
        ((Button) v.findViewById(R.id.btn_3)).setOnClickListener(onClickListener);
        ((Button) v.findViewById(R.id.btn_4)).setOnClickListener(onClickListener);
        ((Button) v.findViewById(R.id.btn_5)).setOnClickListener(onClickListener);
        ((Button) v.findViewById(R.id.btn_6)).setOnClickListener(onClickListener);

        ((Button) v.findViewById(R.id.btn_11)).setOnClickListener(onClickListener);
        ((Button) v.findViewById(R.id.btn_12)).setOnClickListener(onClickListener);
        ((Button) v.findViewById(R.id.btn_13)).setOnClickListener(onClickListener);
        ((Button) v.findViewById(R.id.btn_14)).setOnClickListener(onClickListener);
        ((Button) v.findViewById(R.id.btn_15)).setOnClickListener(onClickListener);
        ((Button) v.findViewById(R.id.btn_16)).setOnClickListener(onClickListener);

        ((Button) v.findViewById(R.id.btn_cancel)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        ((Button) v.findViewById(R.id.btn_submit)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((EditText) rootView.findViewById(R.id.room_tempreture)).getText().toString().length() < 1){
                    showDialog("Please update room temperature!");
                } else if (((EditText) rootView.findViewById(R.id.room_humidity)).getText().toString().length() < 1){
                    showDialog("Please update room humidity!");
                } else{
                    try {
                        int room_tempreture = Integer.parseInt(((EditText) rootView.findViewById(R.id.room_tempreture)).getText().toString());
                        int room_humidity = Integer.parseInt(((EditText) rootView.findViewById(R.id.room_humidity)).getText().toString());
                        int vote = Integer.parseInt(myVoteTextView.getText().toString());


                        MainActivityFragment.getInstance().storeData(vote,room_tempreture, room_humidity);

                        dismiss();
                        // Yes!  An integer.
                    } catch (NumberFormatException nfe) {
                        // Not an integer
                        showDialog("Please update room or tempreture humidity! They are not numeric value!");
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
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
