package edu.wichita.iot.smart_home.comfortvote;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.j256.ormlite.dao.Dao;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import edu.wichita.iot.smart_home.comfortvote.callback.AppendToLogCallback;
import edu.wichita.iot.smart_home.comfortvote.callback.SensorUpdateCallback;
import edu.wichita.iot.smart_home.comfortvote.data.ComfData;
import edu.wichita.iot.smart_home.comfortvote.data.DatabaseHelper;
import edu.wichita.iot.smart_home.comfortvote.data.SensorData;
import edu.wichita.iot.smart_home.comfortvote.data.SensorSampleData;

/**
 * Created by Fariba on 4/6/2016.
 */
public class DataSamplingFragment extends DialogFragment{

    private static final int STATUS_STOP = 0;
    private static final int STATUS_RUN = 1;

    private int sampleingStatus = STATUS_STOP;

    TextView dbSizeTextView;
    View rootView;
    SmartBand smartBand;

    // timer handling
    private Timer mTimer = null;

    // constant
    public static final long DB_INFO_PRESENTATION_INTERVAL = 1000 * 1; // 1 seconds


    // Database Helper
    private DatabaseHelper databaseHelper = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_data_sampling, container, false);
        dbSizeTextView = (TextView) rootView.findViewById(R.id.db_size);

        // Update the service status
        sampleingStatus = STATUS_STOP;

        configureButtons(rootView);

        smartBand = new SmartBand(new SensorUpdateCallback() {
            @Override
            public void update(ComfData comfData, int sensorType) {
                SaveSampleData(comfData, sensorType);
            }
        }, new AppendToLogCallback() {
            @Override
            public void append(String str) {
                // Do Nothing
            }
        });

        // cancel if already existed
        if(mTimer != null) {
            mTimer.cancel();
        } else {
            // recreate new
            mTimer = new Timer();
        }

        // schedule task
        mTimer.scheduleAtFixedRate(new TimeDisplayTimerTask(), 0, DB_INFO_PRESENTATION_INTERVAL);

        return rootView;
    }

    class TimeDisplayTimerTask extends TimerTask {

        @Override
        public void run() {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        long dbSize= getDBHelper().getSendorSampleDataDao().countOf();
                        dbSizeTextView.setText(Long.toString(dbSize));
                    } catch (SQLException e){
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    private void  configureButtons(View v){
        ((Button) v.findViewById(R.id.btn_sample)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sampleingStatus == STATUS_STOP){
                    ((Button) v.findViewById(R.id.btn_sample)).setText("Stop");
                    smartBand.activateForSampling(getActivity());
                    sampleingStatus = STATUS_RUN;
                } else if (sampleingStatus == STATUS_RUN){
                    ((Button) v.findViewById(R.id.btn_sample)).setText("Start");
                    smartBand.pause();
                    sampleingStatus = STATUS_STOP;
                }
            }
        });

        ((Button) v.findViewById(R.id.btn_sample_clear)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearDB();
            }
        });

        ((Button) v.findViewById(R.id.btn_sample_share)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareDB();
            }
        });


    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        mTimer.cancel();
        smartBand.pause();
        super.onDismiss(dialog);
    }

    private DatabaseHelper getDBHelper() {
        if (databaseHelper == null) {
            databaseHelper = DatabaseHelper.getHelper(getActivity());
        }
        return databaseHelper;
    }

    private void SaveSampleData(ComfData comfData, int sensorType)      {
        if (sensorType != SmartBand.ACCELEROMETER_SENSOR){
            return;
        }

        try{
            Dao<SensorSampleData, Integer> dao = getDBHelper().getSendorSampleDataDao();
            SensorSampleData sensorSampleData = (SensorSampleData) SensorSampleData.newInstance(comfData);
            dao.create(sensorSampleData);
        } catch (SQLException e){
            System.out.print(e);
        }
    }


    public void clearDB(){

        new AlertDialog.Builder(getActivity())
                .setTitle("Alert")
                .setMessage("Do you want to Clear the Sampling Sensor Table ?")
                .setCancelable(true)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            getDBHelper().clearSensorSampleDataTable();
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


    public void shareDB(){
        try {

            Dao<SensorSampleData, Integer> dao = getDBHelper().getSendorSampleDataDao();
            List<SensorSampleData> sensorSampleDataList = dao.queryForAll();

            String h = DateFormat.format("yyyyy_mm_dd_hhmmssaa", System.currentTimeMillis()).toString();

            String baseFolder = "";
            // check if external storage is available
            if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                baseFolder = getActivity().getExternalFilesDir(null).getAbsolutePath();
            }
            // revert to using internal storage
            else {
                baseFolder = getActivity().getFilesDir().getAbsolutePath();
            }
            File rootFile = new File(baseFolder + "sansor_sample_data_"+ h +".csv");
            FileOutputStream fileOutputStream = new FileOutputStream (rootFile);

            if (sensorSampleDataList.size() > 0){
                fileOutputStream.write(sensorSampleDataList.get(0).getCsvFormatHeader().getBytes());
                for (SensorSampleData sensorSampleData : sensorSampleDataList){
                    fileOutputStream.write(sensorSampleData.getCsvFormat().getBytes());
                }
                fileOutputStream.close();

                MainActivityFragment.getInstance().shareFile(rootFile);
            } else {
                MainActivityFragment.getInstance().showDialog("There is No data in DB!");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

}
