package edu.wichita.iot.smart_home.comfortvote;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.j256.ormlite.dao.Dao;
import com.microsoft.band.BandClient;
import com.microsoft.band.BandClientManager;
import com.microsoft.band.BandException;
import com.microsoft.band.BandInfo;
import com.microsoft.band.BandIOException;
import com.microsoft.band.ConnectionState;
import com.microsoft.band.InvalidBandVersionException;
import com.microsoft.band.UserConsent;
import com.microsoft.band.sensors.BandAccelerometerEvent;
import com.microsoft.band.sensors.BandAccelerometerEventListener;
import com.microsoft.band.sensors.BandAltimeterEvent;
import com.microsoft.band.sensors.BandAltimeterEventListener;
import com.microsoft.band.sensors.BandAmbientLightEvent;
import com.microsoft.band.sensors.BandAmbientLightEventListener;
import com.microsoft.band.sensors.BandBarometerEvent;
import com.microsoft.band.sensors.BandBarometerEventListener;
import com.microsoft.band.sensors.BandCaloriesEvent;
import com.microsoft.band.sensors.BandCaloriesEventListener;
import com.microsoft.band.sensors.BandContactEvent;
import com.microsoft.band.sensors.BandContactEventListener;
import com.microsoft.band.sensors.BandContactState;
import com.microsoft.band.sensors.BandDistanceEvent;
import com.microsoft.band.sensors.BandDistanceEventListener;
import com.microsoft.band.sensors.BandGsrEvent;
import com.microsoft.band.sensors.BandGsrEventListener;
import com.microsoft.band.sensors.BandGyroscopeEvent;
import com.microsoft.band.sensors.BandGyroscopeEventListener;
import com.microsoft.band.sensors.BandHeartRateEvent;
import com.microsoft.band.sensors.BandHeartRateEventListener;
import com.microsoft.band.sensors.BandPedometerEvent;
import com.microsoft.band.sensors.BandPedometerEventListener;
import com.microsoft.band.sensors.BandRRIntervalEvent;
import com.microsoft.band.sensors.BandRRIntervalEventListener;
import com.microsoft.band.sensors.BandSkinTemperatureEvent;
import com.microsoft.band.sensors.BandSkinTemperatureEventListener;
import com.microsoft.band.sensors.BandUVEvent;
import com.microsoft.band.sensors.BandUVEventListener;
import com.microsoft.band.sensors.HeartRateConsentListener;
import com.microsoft.band.sensors.HeartRateQuality;
import com.microsoft.band.sensors.MotionType;
import com.microsoft.band.sensors.SampleRate;
import com.microsoft.band.sensors.UVIndexLevel;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import edu.wichita.iot.smart_home.comfortvote.callback.AppendToLogCallback;
import edu.wichita.iot.smart_home.comfortvote.callback.SensorUpdateCallback;
import edu.wichita.iot.smart_home.comfortvote.data.ComfData;
import edu.wichita.iot.smart_home.comfortvote.data.DatabaseHelper;
import edu.wichita.iot.smart_home.comfortvote.data.SensorData;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    private static MainActivityFragment instance;
    TextView appLogTextView;
    SmartBand smartBand;

    public static  MainActivityFragment getInstance(){
        return instance;
    }


    private DatabaseHelper databaseHelper = null;


    public MainActivityFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        smartBand = new SmartBand(new SensorUpdateCallback() {
            @Override
            public void update(ComfData comfData, int sensorType) {
                updateSensorInfo(comfData, sensorType);
            }
        }, new AppendToLogCallback() {
            @Override
            public void append(String str) {
                appendToLog(str);
            }
        });
        instance = this;
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        appLogTextView = (TextView) rootView.findViewById(R.id.app_log);
        ((Button) rootView.findViewById(R.id.connect)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                smartBand.activate(getActivity());
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        appLogTextView.setText("");
    }


    @Override
    public void onPause() {
        super.onPause();
        smartBand.pause();

    }

    @Override
    public void onDestroy() {

        if (databaseHelper != null) {
            databaseHelper.close();
            databaseHelper = null;
        }

        super.onDestroy();
    }


    private void appendToLog(final String string) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView) getActivity().findViewById(R.id.app_log)).setText(string);
            }
        });
    }


    private void updateSensorInfo(final ComfData comfData, int sensorType) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String heartRateStr = String.format("%d beats per minute,"
                        + "%s", comfData.heartRate, comfData.heartRateQuality);
                ((TextView) getActivity().findViewById(R.id.heart_rate)).setText(heartRateStr);


                String accelerometerSTr = String.format(" X = %.3f \n Y = %.3f\n Z = %.3f",
                        comfData.accelerometerX, comfData.accelerometerY, comfData.accelerometerZ);
                ((TextView) getActivity().findViewById(R.id.accelerometer)).setText(accelerometerSTr);

                String gyroscopeSTr = String.format(" X = %.3f, X = %.3f \n Y = %.3f, Y = %.3f\n Z = %.3f, Z = %.3f",
                        comfData.accelerometerX2, comfData.angAccelerometerX, comfData.accelerometerY2,
                        comfData.angAccelerometerY, comfData.accelerometerZ2, comfData.angAccelerometerZ);

                ((TextView) getActivity().findViewById(R.id.gyroscope)).setText(gyroscopeSTr);

                String barometrStr = String.format("Air Pressure = %.3f hPa \n"
                        + "Temperature = %.2f degrees Celsius", comfData.airPressure, comfData.temperature);
                ((TextView) getActivity().findViewById(R.id.barometer)).setText(barometrStr);


                ((TextView) getActivity().findViewById(R.id.brightnessVal)).setText(
                        String.format(" %d lux", comfData.brightnessVal));
                ((TextView) getActivity().findViewById(R.id.status)).setText("Status : " + comfData.statusStr);

                ((TextView) getActivity().findViewById(R.id.resistance)).setText(
                        String.format(" %d kOhms", comfData.resistance));


                ((TextView) getActivity().findViewById(R.id.calories_today)).setText(
                        String.format(" %d ", comfData.caloriesToday));

                ((TextView) getActivity().findViewById(R.id.skin_temperature)).setText(
                        String.format(" %.2f degrees Celsius", comfData.skinTemperature));

                String uVInfoStr = String.format( "VV Exposure Today = %d\n uVIndexLevel = %s",
                        comfData.uVExposureToday, comfData.uVIndexLevel);
                ((TextView) getActivity().findViewById(R.id.uv_info)).setText(uVInfoStr);

                String distanceInfo = String.format("MotionType = %s, Distance Today = %d, " +
                        "Pace = %f ms/m,Speed = %f cm/s", comfData.motionType, comfData.distance,
                        comfData.pace, comfData.speed);
                ((TextView) getActivity().findViewById(R.id.distance_info)).setText(distanceInfo);

                String altimeterInfo = new StringBuilder().append(String.format("Total Gain = %d cm\n", comfData.totalGain))
                        .append(String.format("Total Loss = %d cm\n", comfData.totalLoss))
                        .append(String.format("Stepping Gain = %d cm\n", comfData.steppingGain))
                        .append(String.format("Stepping Loss = %d cm\n", comfData.steppingLoss))
                        .append(String.format("Steps Ascended = %d\n", comfData.steppingAscended))
                        .append(String.format("Steps Descended = %d\n", comfData.steppingDescended))
                        .append(String.format("Rate = %f cm/s\n", comfData.rate))
                        .append(String.format("Flights of Stairs Ascended = %d\n", comfData.flightsStairsAscended))
                        .append(String.format("Flights of Stairs Descended = %d\n", comfData.flightsStairsDescended)).toString();
                ((TextView) getActivity().findViewById(R.id.altimeter_info)).setText(altimeterInfo);

                ((TextView) getActivity().findViewById(R.id.band_contact_state)).setText(String.format(" %s ", comfData.bandContactState));

                ((TextView) getActivity().findViewById(R.id.pedometer)).setText(
                        String.format(" %d in Today", comfData.pedometer));
                ((TextView) getActivity().findViewById(R.id.rr_interval)).setText(
                        String.format(" %f ", comfData.rrInterval));
            }
        });
    }


    /**
     * You'll need this in your class to get the helper from the manager once per class.
     */
    private DatabaseHelper getDBHelper() {
        if (databaseHelper == null) {
            databaseHelper = DatabaseHelper.getHelper(getActivity().getBaseContext());
        }
        return databaseHelper;
    }

    public void storeData(int vote, float roomTempreture, float roomHumidity) throws SQLException {
        Dao<ComfData, Integer> dao = getDBHelper().getComfDataDao();

        String currentTime = DateFormat.format("MM/dd/yyyy HH:mm:ss", System.currentTimeMillis()).toString();

        final ComfData deviceComfData = smartBand.getComfData();
        ComfData comfData = new ComfData(deviceComfData);
        comfData.currentTime = currentTime;
        comfData.vote = vote;
        comfData.roomTempreture = roomTempreture;
        comfData.roomHumidity = roomHumidity;

        List<SensorData> sensorDatas = getDBHelper().getLasSensorData(2);
        if (sensorDatas.size() >= 2){
            comfData.caloriesToday2 = sensorDatas.get(0).caloriesToday;
            comfData.caloriesTS2 = sensorDatas.get(0).caloriesTS;
            comfData.pedometer2 = sensorDatas.get(0).pedometer;
            comfData.pedometerTS2 = sensorDatas.get(0).pedometerTS;
            comfData.caloriesToday3 = sensorDatas.get(1).caloriesToday;
            comfData.caloriesTS3 = sensorDatas.get(1).caloriesTS;
            comfData.pedometer3 = sensorDatas.get(1).pedometer;
            comfData.pedometerTS3 = sensorDatas.get(1).pedometerTS;
        } else if (sensorDatas.size() == 1) {
            comfData.caloriesToday2 = sensorDatas.get(0).caloriesToday;
            comfData.caloriesTS2 = sensorDatas.get(0).caloriesTS;
            comfData.pedometer2 = sensorDatas.get(0).pedometer;
            comfData.pedometerTS2 = sensorDatas.get(0).pedometerTS;
            comfData.caloriesToday3 =0;
            comfData.caloriesTS3 = 0;
            comfData.pedometer3 = 0;
            comfData.pedometerTS3 = 0;
        } else{
            comfData.caloriesToday2 = 0;
            comfData.caloriesTS2 = 0;
            comfData.pedometer2 = 0;
            comfData.pedometerTS2 = 0;
            comfData.caloriesToday3 = 0;
            comfData.caloriesTS3 = 0;
            comfData.pedometer3 = 0;
            comfData.pedometerTS3 = 0;
        }

        dao.create(comfData);
    }

    public void shareDB(){
        try {

            Dao<ComfData, Integer> dao = getDBHelper().getComfDataDao();
            List<ComfData> comfDataList = dao.queryForAll();

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
            File rootFile = new File(baseFolder + "comf_data_"+ h +".csv");
            FileOutputStream  fileOutputStream = new FileOutputStream (rootFile);


            if (comfDataList.size() > 0){
                fileOutputStream.write(comfDataList.get(0).getCsvFormatHeader().getBytes());
                for (ComfData comfData : comfDataList){
                    fileOutputStream.write(comfData.getCsvFormat().getBytes());
                }
                fileOutputStream.close();

                shareFile(rootFile);
            } else {
                showDialog("There is No data in DB!");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    public void shareFile(File file1){
        Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
        emailIntent.setType("*/*");
//        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] {"me@gmail.com"});
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
                "Comfort Vote DB data");
        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT,
                "Go on the attachment! you can find data there");
        emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file1));
        startActivity(Intent.createChooser(emailIntent, "Send mail..."));
    }


    public void clearDB(){

        new AlertDialog.Builder(getActivity())
                .setTitle("Alert")
                .setMessage("Do you want to Clear the DB ?")
                .setCancelable(true)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            getDBHelper().clearComfDaoTable();
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
