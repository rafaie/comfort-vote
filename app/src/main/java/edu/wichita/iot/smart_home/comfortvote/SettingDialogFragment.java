package edu.wichita.iot.smart_home.comfortvote;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.renderscript.ScriptGroup;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import edu.wichita.iot.smart_home.comfortvote.data.ComfData;
import edu.wichita.iot.smart_home.comfortvote.data.DatabaseHelper;
import edu.wichita.iot.smart_home.comfortvote.data.SettingData;

/**
 * Created by Fariba on 4/6/2016.
 */
public class SettingDialogFragment extends DialogFragment{
    private static int DB_MAIN = 1;
    private static int DB_COUNTINUES = 2;

    private View rootView;

    private IMyAidlInterface internalService;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            internalService = IMyAidlInterface.Stub.asInterface((IBinder) service);
            Toast.makeText(getActivity(), "Service connected", Toast.LENGTH_LONG)
                    .show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            internalService = null;
        }
    };

    // Database Helper
    private DatabaseHelper databaseHelper = null;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_dialog_setting, container, false);
        loadData();
        configureButtons(rootView);
        initService();
        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releaseService();
    }

    private void loadData(){
        SettingData settingData = getDBHelper().getSettignData();
        ((TextView) rootView.findViewById(R.id.notification_interval)).setText(String.valueOf(settingData.notificationInterval));
        ((TextView) rootView.findViewById(R.id.sampling_interval)).setText(String.valueOf(settingData.samplingInterval));
        ((TextView) rootView.findViewById(R.id.sampling_wait_time)).setText(String.valueOf(settingData.samplingWaitTime));
    }

    private void  configureButtons(View v){

        ((Button) v.findViewById(R.id.setting_update_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int samplingInterval = Integer.valueOf(String.valueOf(((TextView) rootView.findViewById(R.id.sampling_interval)).getText()));
                int notificationInterval = Integer.valueOf(String.valueOf(((TextView) rootView.findViewById(R.id.notification_interval)).getText()));
                int samplingWaitTime = Integer.valueOf(String.valueOf(((TextView) rootView.findViewById(R.id.sampling_wait_time)).getText()));

                System.out.println(samplingInterval);
                System.out.println(samplingWaitTime);
                System.out.println(notificationInterval);

                getDBHelper().saveSettignData(samplingInterval, samplingWaitTime, notificationInterval);

                // Apply change on service
                try {
                    internalService.setTimer(samplingInterval, notificationInterval, samplingWaitTime);
                    showDialog("The Setting is applied and stored");
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });

        ((Button) v.findViewById(R.id.continues_db_clean_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearDB(DB_COUNTINUES);
            }
        });

        ((Button) v.findViewById(R.id.main_db_clean_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearDB(DB_MAIN);
            }
        });

    }


    /** Binds this activity to the service. */
    private void initService() {
        Intent intent = new Intent();
        intent.setClassName("edu.wichita.iot.smart_home.comfortvote", ComfService.class.getName());
        boolean ret = getActivity().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

//    /** Unbinds this activity from the service. */
    private void releaseService() {
        getActivity().unbindService(serviceConnection);
    }




    private DatabaseHelper getDBHelper() {
        if (databaseHelper == null) {
            databaseHelper = DatabaseHelper.getHelper(getActivity());
        }
        return databaseHelper;
    }


    private int db_type;
    public void clearDB(int type){

        db_type = type;
        String name = "Main DB";
        if (db_type == DB_COUNTINUES){
            name = "Continues Sampling DB";
        }
        new AlertDialog.Builder(getActivity())
                .setTitle("Alert")
                .setMessage("Do you want to clear " + name + " ?")
                .setCancelable(true)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            if (db_type == DB_MAIN) {
                                getDBHelper().clearComfDaoTable();
                            } else {
                                getDBHelper().clearSensorSampleDataTable();
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Nothing
            }
        }).create().show();
    }


    public void showDialog(String msgStr){
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
