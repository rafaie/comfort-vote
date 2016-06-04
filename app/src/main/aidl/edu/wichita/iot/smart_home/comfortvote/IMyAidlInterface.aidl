// IMyAidlInterface.aidl
package edu.wichita.iot.smart_home.comfortvote;

// Declare any non-default types here with import statements

interface IMyAidlInterface {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
   void setTimer(long samplingInterval, long notificationInterval, long sampleing_wait_time);

}
