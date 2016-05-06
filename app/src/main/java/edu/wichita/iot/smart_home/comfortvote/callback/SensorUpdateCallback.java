package edu.wichita.iot.smart_home.comfortvote.callback;

import edu.wichita.iot.smart_home.comfortvote.data.ComfData;

/**
 * Created by Mostafa on 5/5/2016.
 */
public interface SensorUpdateCallback {
    void update(ComfData comfData);
}
