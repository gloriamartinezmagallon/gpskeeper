package navdev.gpstrack.gpsutils.tracker.component;


import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.preference.PreferenceManager;

import java.util.Random;

import navdev.gpstrack.BuildConfig;

public class TrackerPressure extends DefaultTrackerComponent implements SensorEventListener {

    public static final String NAME = "Pressure";

    @Override
    public String getName() {
        return NAME;
    }

    private SensorManager sensorManager = null;

    //For debug builds, use random if sensor is unavailable
    private final static boolean testMode = BuildConfig.DEBUG;
    private static boolean isEmulating = false;

    //The sensor fires continuously, use the last available values (no smoothing)
    @SuppressWarnings("unused")
    private boolean isStarted = true;
    private Float latestVal = null;

    public Float getValue() {
        if (isEmulating) {
            latestVal = (new Random()).nextFloat() * 0.2f + 1013.25f/*SensorManager.PRESSURE_STANDARD_ATMOSPHERE*/;
            //latestTime = SystemClock.elapsedRealtime()*1000000;
        }
        return latestVal;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.values != null && event.values.length > 0) {
            if (latestVal == null) {
                latestVal = event.values[0];
            } else {
                final float alpha = 0.5f;
                latestVal = event.values[0] * alpha + (1 - alpha) * latestVal;
                //latestTime = event.timestamp;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    /**
     * Sensor is available
     */
    public static boolean isAvailable(final Context context) {
        return ((new TrackerPressure()).getSensor(context) != null) || testMode;
    }

    private Sensor getSensor(final Context context) {
        Sensor sensor;
        if (sensorManager == null) {
            sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        }
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        if (sensor == null) {
            sensorManager = null;
        }

        if (testMode && sensor == null) {
            //No real sensor, emulate
            isEmulating = true;
        }

        return sensor;
    }

    /**
     * Called by Tracker during initialization
     */
    @Override
    public ResultCode onInit(Callback callback, Context context) {
        return ResultCode.RESULT_OK;
    }

    @Override
    public ResultCode onConnecting(final Callback callback, final Context context) {
        ResultCode res;
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean enabled = true;

        if (enabled) {
            Sensor sensor = getSensor(context);
            if (sensor != null) {
                sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST);
                res = ResultCode.RESULT_OK;
            } else if (isEmulating) {
                res = ResultCode.RESULT_OK;
            } else {
                res = ResultCode.RESULT_NOT_SUPPORTED;
            }
        } else {
            res = ResultCode.RESULT_NOT_ENABLED;
        }
        return res;
    }

    @Override
    public boolean isConnected() {
        return sensorManager != null;
    }

    @Override
    public void onConnected() {
    }

    /**
     * Called by Tracker before start
     *   Component shall populate bindValues
     *   with objects that will then be passed
     *   to workout
     */
    //public void onBind(HashMap<String, Object> bindValues) {
    //}

    /**
     * Called by Tracker when workout starts
     */
    @Override
    public void onStart() {
        isStarted = true;
    }

    /**
     * Called by Tracker when workout is paused
     */
    @Override
    public void onPause() {
        isStarted = false;
    }

    /**
     * Called by Tracker when workout is resumed
     */
    @Override
    public void onResume() {
        isStarted = true;
    }

    /**
     * Called by Tracker when workout is complete
     */
    @Override
    public void onComplete(boolean discarded) {
        isStarted = false;
    }

    /**
     * Called by tracked after workout has ended
     */
    @Override
    public ResultCode onEnd(Callback callback, Context context) {
        isStarted = false;
        if (sensorManager != null) { sensorManager.unregisterListener(this); }
        sensorManager = null;
        isEmulating = false;
        return ResultCode.RESULT_OK;
    }
}