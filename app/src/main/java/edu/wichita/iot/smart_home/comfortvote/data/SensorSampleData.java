package edu.wichita.iot.smart_home.comfortvote.data;

import android.text.format.DateFormat;

import com.j256.ormlite.field.DatabaseField;

/**
 * Created by Mostafa on 4/1/2016.
 *
 */
public class SensorSampleData extends SensorData{

    public static SensorSampleData newInstance(final ComfData comfData){
        SensorSampleData sensorSampleData = new SensorSampleData();

        sensorSampleData.currentTime = DateFormat.format("MM/dd/yyyy HH:mm:ss", System.currentTimeMillis()).toString();
        sensorSampleData.heartRateQuality = comfData.heartRateQuality;
        sensorSampleData.heartRate = comfData.heartRate;
        sensorSampleData.accelerometerX = comfData.accelerometerX;
        sensorSampleData.accelerometerY = comfData.accelerometerY;
        sensorSampleData.accelerometerZ = comfData.accelerometerZ;
        sensorSampleData.accelerometerX2 = comfData.accelerometerX2;
        sensorSampleData.accelerometerY2 = comfData.accelerometerY2;
        sensorSampleData.accelerometerZ2 = comfData.accelerometerZ2;
        sensorSampleData.angAccelerometerX = comfData.angAccelerometerX;
        sensorSampleData.angAccelerometerY = comfData.angAccelerometerY;
        sensorSampleData.angAccelerometerZ = comfData.angAccelerometerZ;
        sensorSampleData.brightnessVal = comfData.brightnessVal;
        sensorSampleData.airPressure = comfData.airPressure;
        sensorSampleData.temperature = comfData.temperature;
        sensorSampleData.resistance = comfData.resistance;
        sensorSampleData.caloriesToday = comfData.caloriesToday;
        sensorSampleData.caloriesTS = comfData.caloriesTS;
        sensorSampleData.skinTemperature = comfData.skinTemperature;
        sensorSampleData.uVExposureToday = comfData.uVExposureToday;
        sensorSampleData.uVIndexLevel = comfData.uVIndexLevel;
        sensorSampleData.motionType = comfData.motionType;
        sensorSampleData.distance = comfData.distance;
        sensorSampleData.pace = comfData.pace;
        sensorSampleData.speed = comfData.speed;
        sensorSampleData.totalLoss = comfData.totalLoss;
        sensorSampleData.totalGain = comfData.totalGain;
        sensorSampleData.steppingGain = comfData.steppingGain;
        sensorSampleData.steppingLoss = comfData.steppingLoss;
        sensorSampleData.steppingAscended = comfData.steppingAscended;
        sensorSampleData.steppingDescended = comfData.steppingDescended;
        sensorSampleData.rate = comfData.rate;
        sensorSampleData.flightsStairsAscended = comfData.flightsStairsAscended;
        sensorSampleData.flightsStairsDescended = comfData.flightsStairsDescended;
        sensorSampleData.bandContactState = comfData.bandContactState;
        sensorSampleData.pedometer = comfData.pedometer;
        sensorSampleData.pedometerTS = comfData.pedometerTS;
        sensorSampleData.rrInterval = comfData.rrInterval;
        sensorSampleData.statusStr = comfData.statusStr;

        return sensorSampleData;
    }

}
