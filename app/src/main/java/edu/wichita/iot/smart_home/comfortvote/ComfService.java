package edu.wichita.iot.smart_home.comfortvote;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.NotificationCompat;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Toast;

import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import edu.wichita.iot.smart_home.comfortvote.callback.AppendToLogCallback;
import edu.wichita.iot.smart_home.comfortvote.callback.SensorUpdateCallback;
import edu.wichita.iot.smart_home.comfortvote.data.ComfData;
import edu.wichita.iot.smart_home.comfortvote.data.DatabaseHelper;
import edu.wichita.iot.smart_home.comfortvote.data.SettingData;

/**
 * Created by Fariba on 4/21/2016.
 */
public class ComfService extends Service {

    private static final String TAG = ComfService.class.getCanonicalName();

    // Database Helper
    private DatabaseHelper databaseHelper = null;

    // constant
    private static long samplingInterval = 30; // 300 seconds
    private static long notificationInterval = 1800; // 1800 seconds
    private static long sampling_wait_time = 10;

    
    // run on another Thread to avoid crash
    private Handler mHandler = new Handler();
    // timer handling
    private Timer mTimer = null;

    private Timer mTimer2 = null;
    private SmartBand smartBand;
    private long activeTime = 0;
    private long storeTime = 0;

    private static ComfService comfService;

    private final IMyAidlInterface.Stub mBinder = new IMyAidlInterface.Stub(){
        public void setTimer(long samplingInterval, long notificationInterval, long sampleing_wait_time){
            comfService.setTimer(samplingInterval, notificationInterval, sampleing_wait_time);
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        comfService = this;
        SettingData settingData = getDBHelper().getSettignData();
        setTimer(settingData.samplingInterval, settingData.notificationInterval, settingData.samplingWaitTime);
    }

    public void setTimer(long samplingInterval, long notificationInterval, long sampleing_wait_time) {
        this.samplingInterval = samplingInterval;
        this.notificationInterval = notificationInterval;
        this.sampling_wait_time = sampleing_wait_time;

        // schedule task
        Date date1 = new Date();
        date1.setTime(0);

        if (samplingInterval >  3 && sampleing_wait_time > 3){
            if(mTimer != null) {
                mTimer.cancel();
            }
            mTimer = new Timer();
            mTimer.schedule(new SampelingTimerTask(), date1, this.samplingInterval * 1000);
        } else if (mTimer != null) {
            mTimer.cancel();
        }

        if (notificationInterval > 59 ){
            if(mTimer2 != null) {
                mTimer2.cancel();
            }

            mTimer2 = new Timer();
            mTimer2.schedule(new NotificationTimerTask(), date1, this.notificationInterval * 1000);
        } else if ( mTimer2 != null){
            mTimer2.cancel();
        }
    }

    class SampelingTimerTask extends TimerTask {

        @Override
        public void run() {
            // run on another thread
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    activeTime = System.currentTimeMillis();
                    activateSmartband();
                }

            });
        }
    }

    class NotificationTimerTask extends TimerTask {

        @Override
        public void run() {
            // run on another thread
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                    if (hour > 8 && hour < 23){
                        raiseNotification();
                    }
                }
            });
        }
    }

    //Thanks from http://www.tutorialspoint.com/android/android_notifications.htm
    private void raiseNotification(){
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setSmallIcon(R.mipmap.hvac_flat_icons);
        mBuilder.setContentTitle("Comfort Vote, Click Me!");
        mBuilder.setContentText("Hi, It's time to make a new vote!");
        //Ton
        mBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

        Intent resultIntent = new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);

        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

// notificationID allows you to update the notification later on.
        mNotificationManager.notify(0, mBuilder.build());

    }

    private void activateSmartband() {

        smartBand = new SmartBand(new SensorUpdateCallback() {
            @Override
            public void update(ComfData comfData, int sensorType) {
                saveSampple(comfData, sensorType);
            }
        }, new AppendToLogCallback() {
            @Override
            public void append(String str) {

            }
        });

        activeTime = System.currentTimeMillis();
        smartBand.activateInBackground(this.getBaseContext());
    }

    private void saveSampple(ComfData comfData, int sensorType) {

        if (System.currentTimeMillis() - activeTime > sampling_wait_time * 1000) {
            if (System.currentTimeMillis() - storeTime > sampling_wait_time * 1000) {
                storeTime = System.currentTimeMillis();
                storeSensorData(comfData);
                if (smartBand != null) {
                    smartBand.pause();
                }
                smartBand = null;
                Toast.makeText(getApplicationContext(), getDateTime() + " Data is Stored!",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private DatabaseHelper getDBHelper() {
        if (databaseHelper == null) {
            databaseHelper = DatabaseHelper.getHelper(getBaseContext());
        }
        return databaseHelper;
    }


    private void storeSensorData(ComfData comfData) {
        try{
            String currentTime = DateFormat.format("MM/dd/yyyy HH:mm:ss", System.currentTimeMillis()).toString();
            Dao<ComfData, Integer> dao = getDBHelper().getComfDataDao();
            ComfData comfDataNew = ComfData.newInstance(comfData);

            // Store last votes and tempreture and humidity!
            List<ComfData> comfDatas = getDBHelper().getLasComfDataVote(1);
            if (comfDatas.size() > 0) {
                comfDataNew.vote = comfDatas.get(0).vote;
                comfDataNew.roomHumidity = comfDatas.get(0).roomHumidity;
                comfDataNew.temperature = comfDatas.get(0).temperature;
                comfDataNew.clothingScore = comfDatas.get(0).clothingScore;
                comfDataNew.clothing = comfDatas.get(0).clothing;
            }
            comfDataNew.dataType = 10;
            comfDataNew.currentTime = currentTime;
            dao.create(comfDataNew);

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
