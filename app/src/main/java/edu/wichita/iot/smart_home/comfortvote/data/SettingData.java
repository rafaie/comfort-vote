package edu.wichita.iot.smart_home.comfortvote.data;

import com.j256.ormlite.field.DatabaseField;

/**
 * Created by Fariba on 4/22/2016.
 */
public class SettingData {
    // id is generated by the database and set on the object automagically
    @DatabaseField(generatedId = true)
    int id;

    @DatabaseField
    public String currentTime;

    @DatabaseField
    public String heartRateQuality;

    @DatabaseField
    public int heartRate;

    @DatabaseField
    public float accelerometerX;

}
