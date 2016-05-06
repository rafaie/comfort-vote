package edu.wichita.iot.smart_home.comfortvote;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Mostafa on 4/21/2016.
 */
public class AutoStart extends BroadcastReceiver
{
    public void onReceive(Context arg0, Intent arg1)
    {
        Intent intent = new Intent(arg0,ComfService.class);
        arg0.startService(intent);
        Log.i("Autostart", "started");
    }
}
