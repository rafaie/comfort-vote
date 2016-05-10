package edu.wichita.iot.smart_home.comfortvote;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import edu.wichita.iot.smart_home.comfortvote.callback.AppendToLogCallback;
import edu.wichita.iot.smart_home.comfortvote.callback.SensorUpdateCallback;
import edu.wichita.iot.smart_home.comfortvote.data.ComfData;
import edu.wichita.iot.smart_home.comfortvote.data.DatabaseHelper;
import edu.wichita.iot.smart_home.comfortvote.data.SensorData;

/**
 * Created by Fariba on 4/21/2016.
 */
public class ComfService extends Service {

    private static final String TAG = ComfService.class.getCanonicalName();


    // Database Helper
    private DatabaseHelper databaseHelper = null;

    // constant
    public static final long NOTIFY_INTERVAL = 1000 * 900; // 900 seconds

    // run on another Thread to avoid crash
    private Handler mHandler = new Handler();
    // timer handling
    private Timer mTimer = null;

    private SmartBand smartBand;
    private long activeTime = 0;
    private long storeTime = 0;
    private static final long SENSOR_WAIT_TIME = 20 * 1000;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        // cancel if already existed
        if(mTimer != null) {
            mTimer.cancel();
        } else {
            // recreate new
            mTimer = new Timer();
        }

        smartBand = new SmartBand(new SensorUpdateCallback() {
            @Override
            public void update(ComfData comfData) {
                saveSampple(comfData);
            }
        }, new AppendToLogCallback() {
            @Override
            public void append(String str) {

            }
        });

        // schedule task
        mTimer.scheduleAtFixedRate(new TimeDisplayTimerTask(), 0, NOTIFY_INTERVAL);
    }

    class TimeDisplayTimerTask extends TimerTask {

        @Override
        public void run() {
            // run on another thread
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    activateSmartband();
                    activeTime = System.currentTimeMillis();
                    Toast.makeText(getApplicationContext(), getDateTime() + " Smartband is activated!",
                            Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Timer working now !!!");
                }

            });
        }

    }

    private void activateSmartband(){
        smartBand.activateInBackground(this.getBaseContext());
    }

    private void saveSampple(ComfData comfData) {

        if (System.currentTimeMillis() - activeTime > SENSOR_WAIT_TIME) {
            if (System.currentTimeMillis() - storeTime > SENSOR_WAIT_TIME) {
                storeTime = System.currentTimeMillis();
                storeSensorData(comfData);
                smartBand.pause();
                Toast.makeText(getApplicationContext(), getDateTime() + " Data is Stored!",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * You'll need this in your class to get the helper from the manager once per class.
     */
    private DatabaseHelper getDBHelper() {
        if (databaseHelper == null) {
            databaseHelper = DatabaseHelper.getHelper(getBaseContext());
        }
        return databaseHelper;
    }


    private void storeSensorData(ComfData comfData) {
        try{

            Dao<SensorData, Integer> dao = getDBHelper().getSendorDataDao();
            SensorData sensorData = SensorData.newInstance(comfData);
            dao.create(sensorData);
        } catch (SQLException e){
            System.out.print(e);
        }

    }

    private String getDateTime() {
        // get date time in custom format
        SimpleDateFormat sdf = new SimpleDateFormat("[yyyy/MM/dd - HH:mm:ss]");
        return sdf.format(new Date());
    }
}
