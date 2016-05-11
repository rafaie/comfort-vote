package edu.wichita.iot.smart_home.comfortvote;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.sql.Array;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import edu.wichita.iot.smart_home.comfortvote.R;
import edu.wichita.iot.smart_home.comfortvote.data.ComfData;

/**
 * Created by Fariba on 4/6/2016.
 */
public class VoteDialogFragment extends DialogFragment{
    private AlertDialog alertDialog = null;

    private TextView myVoteTextView;
    private TextView myClothingTextView;
    private CheckBox inBuildingChk;
    private View rootView;
    private String clothingTxt = "";
    private String clothingId = "";
    private float clothingValue = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_dialog_vote, container, false);
        myVoteTextView = (TextView) rootView.findViewById(R.id.my_vote);
        myClothingTextView = (TextView) rootView.findViewById(R.id.clothing_score);
        inBuildingChk = (CheckBox) rootView.findViewById(R.id.chk_in_building);
        configureButtons(rootView);

        updateClothingBasedOnLatestVote();
        return rootView;
    }


    private void updateClothingBasedOnLatestVote(){
        List<ComfData> comfDatas = MainActivityFragment.getInstance().getDBHelper().getLasComfData(1);
        if (comfDatas.size() > 0){
            clothingValue = comfDatas.get(0).clothingScore;
            clothingTxt = comfDatas.get(0).clothing;

            myClothingTextView.setText(String.valueOf(clothingValue));
        }
    }

    private void  configureButtons(View v){

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myVoteTextView.setText(((Button) v).getText());
            }
        };

        ((Button) v.findViewById(R.id.btn_0)).setOnClickListener(onClickListener);

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

                        int locationType = (inBuildingChk.isChecked()? 0:1);
                        MainActivityFragment.getInstance().storeData(vote,room_tempreture, room_humidity, clothingTxt, clothingValue, locationType);

                        dismiss();
                    } catch (NumberFormatException nfe) {
                        // Not an integer
                        showDialog("Please update room or tempreture humidity! They are not numeric value!");
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        ((Button) v.findViewById(R.id.clothing_update_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showColthingDialog();
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

    private void showColthingDialog(){
        alertDialog = new AlertDialog.Builder(getActivity()).create();
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View clothingView = (View) inflater.inflate(R.layout.alert_dialog_clothing, null);

        alertDialog.setView(clothingView);
        final ListView listView = (ListView) clothingView.findViewById(R.id.clothing_list);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_multiple_choice ,
                getResources().getStringArray(R.array.clothing));

        listView.setAdapter(adapter);
        listView.setItemChecked(2, true);

        String[] checked = clothingTxt.split("\\|");
        for (int id=0; id < listView.getCount(); id ++){
            if (Arrays.asList(checked).contains(getResources().getStringArray(R.array.clothing)[id])){
                listView.setItemChecked(id, true);
            } else{
                listView.setItemChecked(id, false);
            }
        }


        clothingView.findViewById(R.id.btn_clothing_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        clothingView.findViewById(R.id.btn_clothing_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SparseBooleanArray checkedOption = listView.getCheckedItemPositions();
                clothingTxt = "";
                clothingId = "";
                clothingValue = 0;
                for (int id=0; id < listView.getCount(); id ++){
                    if (checkedOption.get(id)){
                        clothingTxt += getResources().getStringArray(R.array.clothing)[id] + "|";
                        clothingId += getResources().getStringArray(R.array.clothing_value)[id] + "|";
                        clothingValue += Float.valueOf(getResources().getStringArray(R.array.clothing_value)[id]);
                    }
                }

                myClothingTextView.setText(String.valueOf(clothingValue));
                alertDialog.dismiss();
            }
        });

        alertDialog.show();
    }
}
