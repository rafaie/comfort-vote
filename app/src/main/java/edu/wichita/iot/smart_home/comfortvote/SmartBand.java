package edu.wichita.iot.smart_home.comfortvote;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.j256.ormlite.dao.Dao;
import com.microsoft.band.BandClient;
import com.microsoft.band.BandClientManager;
import com.microsoft.band.BandException;
import com.microsoft.band.BandIOException;
import com.microsoft.band.BandInfo;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import edu.wichita.iot.smart_home.comfortvote.callback.AppendToLogCallback;
import edu.wichita.iot.smart_home.comfortvote.callback.SensorUpdateCallback;
import edu.wichita.iot.smart_home.comfortvote.data.ComfData;
import edu.wichita.iot.smart_home.comfortvote.data.DatabaseHelper;

/**
 * Created by Mostafa on 4/24/2016.
 */


public class SmartBand {

    private ComfData comfData;

    SensorUpdateCallback sensorUpdateCallback;
    AppendToLogCallback appendToLogCallback;
    int type = 0;
    Activity activity;


    private BandClient client = null;



    private DatabaseHelper databaseHelper = null;

    private BandRRIntervalEventListener mBandRRIntervalEventListener = new BandRRIntervalEventListener() {
        @Override
        public void onBandRRIntervalChanged(BandRRIntervalEvent event) {
            comfData.rrInterval = event.getInterval();
        }
    };

    private BandPedometerEventListener mBandPedometerEventListener = new BandPedometerEventListener() {
        @Override
        public void onBandPedometerChanged(BandPedometerEvent event) {
            try {
                comfData.pedometer = event.getStepsToday();
            } catch (InvalidBandVersionException e) {
                e.printStackTrace();
            }
        }
    };

    private BandGyroscopeEventListener mBandGyroscopeEventListener = new BandGyroscopeEventListener() {
        @Override
        public void onBandGyroscopeChanged(BandGyroscopeEvent event) {
            comfData.accelerometerX2 = event.getAccelerationX();
            comfData.accelerometerY2 = event.getAccelerationY();
            comfData.accelerometerZ2 = event.getAccelerationZ();

            comfData.angAccelerometerX = event.getAngularVelocityX();
            comfData.angAccelerometerY = event.getAngularVelocityY();
            comfData.angAccelerometerZ = event.getAngularVelocityZ();
        }
    };

    private BandContactEventListener mBandContactEventListener = new BandContactEventListener() {
        @Override
        public void onBandContactChanged(BandContactEvent event) {
            comfData.bandContactState = String.format(" %s ", event.getContactState());
        }
    };

    private BandAltimeterEventListener mBandAltimeterEventListener = new BandAltimeterEventListener() {
        @Override
        public void onBandAltimeterChanged(BandAltimeterEvent event) {
            comfData.totalGain = event.getTotalGain();
            comfData.totalLoss = event.getTotalLoss();
            comfData.steppingAscended = event.getStepsAscended();
            comfData.steppingDescended = event.getStepsDescended();
            comfData.steppingGain = event.getSteppingGain();
            comfData.steppingLoss = event.getSteppingLoss();
            comfData.rate = event.getRate();
            comfData.flightsStairsAscended = event.getFlightsAscended();
            comfData.flightsStairsDescended = event.getFlightsDescended();
        }
    };


    private BandDistanceEventListener mBandDistanceEventListener = new BandDistanceEventListener() {
        @Override
        public void onBandDistanceChanged(BandDistanceEvent event) {
            try {
                comfData.motionType = String.format(" %s ", event.getMotionType());
                comfData.distance = event.getDistanceToday();
                comfData.pace = event.getPace();
                comfData.speed = event.getSpeed();
            } catch (InvalidBandVersionException e) {
                e.printStackTrace();
            }

        }
    };

    private BandSkinTemperatureEventListener mBandSkinTemperatureEventListener = new BandSkinTemperatureEventListener() {
        @Override
        public void onBandSkinTemperatureChanged(BandSkinTemperatureEvent event) {
            if (event != null){
                comfData.skinTempreture = event.getTemperature();
            }
        }
    };


    private BandUVEventListener mBandUVEventListener = new BandUVEventListener() {
        @Override
        public void onBandUVChanged(BandUVEvent event) {
            if (event != null){
                try {
                    comfData.uVExposureToday = event.getUVExposureToday();
                    comfData.uVIndexLevel = String.format(" %s ", event.getUVIndexLevel());
                } catch (InvalidBandVersionException e) {
                    e.printStackTrace();
                }

            }
        }
    };


    private BandCaloriesEventListener mBandCaloriesEventListener = new BandCaloriesEventListener() {
        @Override
        public void onBandCaloriesChanged(BandCaloriesEvent event) {
            if (event != null){
                try {

                    System.out.print("");
                    long caloriesTodayTemp = event.getCaloriesToday();
                    long caloriesTSTemp = event.getTimestamp();


                        comfData.caloriesInSec = (caloriesTodayTemp - comfData.caloriesToday)/ (comfData.caloriesTS - caloriesTSTemp) * 1000*60;
                        comfData.caloriesToday = caloriesTodayTemp;
                        comfData.caloriesTS = caloriesTSTemp;

                } catch (InvalidBandVersionException e) {
                    e.printStackTrace();
                }

            }
        }
    };

    private BandGsrEventListener mGsrEventListener = new BandGsrEventListener() {
        @Override
        public void onBandGsrChanged(final BandGsrEvent event) {
            if (event != null) {
                comfData.resistance = event.getResistance();
                sensorUpdateCallback.update(comfData);
            }
        }
    };

    private BandBarometerEventListener mBarometerEventListener = new BandBarometerEventListener() {
        @Override
        public void onBandBarometerChanged(final BandBarometerEvent event) {
            if (event != null) {
                comfData.airPressure = event.getAirPressure();
                comfData. temperature = event.getTemperature();
                sensorUpdateCallback.update(comfData);
            }
        }
    };

    private BandAmbientLightEventListener mAmbientLightEventListener = new BandAmbientLightEventListener() {
        @Override
        public void onBandAmbientLightChanged(final BandAmbientLightEvent event) {
            if (event != null) {
                comfData.brightnessVal = event.getBrightness();
                sensorUpdateCallback.update(comfData);
            }
        }
    };


    private BandHeartRateEventListener mHeartRateEventListener = new BandHeartRateEventListener() {
        @Override
        public void onBandHeartRateChanged(final BandHeartRateEvent event) {
            if (event != null) {
                comfData.heartRate = event.getHeartRate();
                comfData.heartRateQuality = String.format(" %s ",event.getQuality());
//                appendToLog("");
                comfData.statusStr = String.format("%s",  comfData.heartRateQuality);
                sensorUpdateCallback.update(comfData);
            }
        }
    };

    private BandAccelerometerEventListener mAccelerometerEventListener = new BandAccelerometerEventListener() {
        @Override
        public void onBandAccelerometerChanged(final BandAccelerometerEvent event) {
            if (event != null) {
                comfData.accelerometerX = event.getAccelerationX();
                comfData.accelerometerY = event.getAccelerationY();
                comfData.accelerometerZ = event.getAccelerationZ();

                sensorUpdateCallback.update(comfData);
            }
        }
    };


    public SmartBand(SensorUpdateCallback sensorUpdateCallback, AppendToLogCallback appendToLogCallback) {
        comfData = new ComfData();
        this.sensorUpdateCallback = sensorUpdateCallback;
        this.appendToLogCallback = appendToLogCallback;
    }


    public void activate(Activity activity){
        type = 1;
        this.activity = activity;
        final WeakReference<Activity> reference = new WeakReference<Activity>(activity);
//        new HeartRateConsentTask().execute(reference);
        new HeartRateSubscriptionTask().execute();
    }


    public void pause() {
        if (client != null) {
            try {
                client.getSensorManager().unregisterHeartRateEventListener(mHeartRateEventListener);
                client.getSensorManager().unregisterAccelerometerEventListener(mAccelerometerEventListener);
                client.getSensorManager().unregisterAmbientLightEventListener(mAmbientLightEventListener);
                client.getSensorManager().unregisterBarometerEventListener(mBarometerEventListener);
                client.getSensorManager().unregisterGsrEventListener(mGsrEventListener);
                client.getSensorManager().unregisterCaloriesEventListener(mBandCaloriesEventListener);
                client.getSensorManager().unregisterUVEventListener(mBandUVEventListener);
                client.getSensorManager().unregisterSkinTemperatureEventListener(mBandSkinTemperatureEventListener);
                client.getSensorManager().unregisterDistanceEventListener(mBandDistanceEventListener);
                client.getSensorManager().unregisterAltimeterEventListener(mBandAltimeterEventListener);
                client.getSensorManager().unregisterContactEventListener(mBandContactEventListener);
                client.getSensorManager().unregisterGyroscopeEventListener(mBandGyroscopeEventListener);
                client.getSensorManager().unregisterPedometerEventListener(mBandPedometerEventListener);
                client.getSensorManager().unregisterRRIntervalEventListener(mBandRRIntervalEventListener);
            } catch (BandIOException e) {
                appendToLogCallback.append(e.getMessage());
            }
        }
    }


    private class HeartRateSubscriptionTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {

                boolean isWarning = false;
                if (getConnectedBandClient()) {
                    if (client.getSensorManager().getCurrentHeartRateConsent() == UserConsent.GRANTED) {
                        client.getSensorManager().registerHeartRateEventListener(mHeartRateEventListener);
                    } else {
                        appendToLogCallback.append("You have not given this application consent to access heart rate data yet."
                                + " Please press the Heart Rate Consent button.\n");
                        isWarning = true;
                    }

                    client.getSensorManager().registerAccelerometerEventListener(mAccelerometerEventListener, SampleRate.MS128);

                    int hardwareVersion = Integer.parseInt(client.getHardwareVersion().await());
                    if (hardwareVersion >= 20) {
                        client.getSensorManager().registerAmbientLightEventListener(mAmbientLightEventListener);
                        client.getSensorManager().registerBarometerEventListener(mBarometerEventListener);
                        client.getSensorManager().registerGsrEventListener(mGsrEventListener);
                        client.getSensorManager().registerCaloriesEventListener(mBandCaloriesEventListener);
                        client.getSensorManager().registerSkinTemperatureEventListener(mBandSkinTemperatureEventListener);
                        client.getSensorManager().registerUVEventListener(mBandUVEventListener);
                        client.getSensorManager().registerDistanceEventListener(mBandDistanceEventListener);
                        client.getSensorManager().registerAltimeterEventListener(mBandAltimeterEventListener);
                        client.getSensorManager().registerContactEventListener(mBandContactEventListener);
                        client.getSensorManager().registerGyroscopeEventListener(mBandGyroscopeEventListener, SampleRate.MS128);
                        client.getSensorManager().registerPedometerEventListener(mBandPedometerEventListener);
                        client.getSensorManager().registerRRIntervalEventListener(mBandRRIntervalEventListener);

                    } else {
                        appendToLogCallback.append("The most of sensors are not supported with your Band version. Microsoft Band 2 is required.\n");
                        isWarning = true;
                    }


                    if (!isWarning){
                        appendToLogCallback.append("");
                    }

                } else {
                    appendToLogCallback.append("Band isn't connected. Please make sure bluetooth is on and the band is in range.\n");
                }
            } catch (BandException e) {
                String exceptionMessage="";
                switch (e.getErrorType()) {
                    case UNSUPPORTED_SDK_VERSION_ERROR:
                        exceptionMessage = "Microsoft Health BandService doesn't support your SDK Version. Please update to latest SDK.\n";
                        break;
                    case SERVICE_ERROR:
                        exceptionMessage = "Microsoft Health BandService is not available. Please make sure Microsoft Health is installed and that you have the correct permissions.\n";
                        break;
                    default:
                        exceptionMessage = "Unknown error occured: " + e.getMessage() + "\n";
                        break;
                }
                appendToLogCallback.append(exceptionMessage);

            } catch (Exception e) {
                appendToLogCallback.append(e.getMessage());
            }
            return null;
        }
    }

    private class HeartRateConsentTask extends AsyncTask<WeakReference<Activity>, Void, Void> {
        @Override
        protected Void doInBackground(WeakReference<Activity>... params) {
            try {
                if (getConnectedBandClient()) {
                    if (params[0].get() != null) {
                        client.getSensorManager().requestHeartRateConsent(params[0].get(), new HeartRateConsentListener() {
                            @Override
                            public void userAccepted(boolean consentGiven) {
                            }
                        });
                    }
                } else {
                    appendToLogCallback.append("Band isn't connected. Please make sure bluetooth is on and the band is in range.\n");
                }
            } catch (BandException e) {
                String exceptionMessage="";
                switch (e.getErrorType()) {
                    case UNSUPPORTED_SDK_VERSION_ERROR:
                        exceptionMessage = "Microsoft Health BandService doesn't support your SDK Version. Please update to latest SDK.\n";
                        break;
                    case SERVICE_ERROR:
                        exceptionMessage = "Microsoft Health BandService is not available. Please make sure Microsoft Health is installed and that you have the correct permissions.\n";
                        break;
                    default:
                        exceptionMessage = "Unknown error occured: " + e.getMessage() + "\n";
                        break;
                }
                appendToLogCallback.append(exceptionMessage);

            } catch (Exception e) {
                appendToLogCallback.append(e.getMessage());
            }
            return null;
        }
    }


    private boolean getConnectedBandClient() throws InterruptedException, BandException {
        if (client == null) {
            BandInfo[] devices = BandClientManager.getInstance().getPairedBands();
            if (devices.length == 0) {
                appendToLogCallback.append("Band isn't paired with your phone.\n");
                return false;
            }
            client = BandClientManager.getInstance().create(getBaseContext(), devices[0]);
        } else if (ConnectionState.CONNECTED == client.getConnectionState()) {
            return true;
        }

        appendToLogCallback.append("Band is connecting...\n");
        return ConnectionState.CONNECTED == client.connect().await();
    }


    public ComfData getComfData(){
        return comfData;
    }
    /**
     * You'll need this in your class to get the helper from the manager once per class.
     */

    private Context getBaseContext(){
        if (type == 1){
            return  activity;
        }
        return null;
    }


}
