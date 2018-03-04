package navdev.gpstrack.gpsutils.tracker.component;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import navdev.gpstrack.gpsutils.tracker.Tracker;

public class TrackerElevation extends DefaultTrackerComponent implements SensorEventListener {

    private static final String NAME = "Elevation";

    @Override
    public String getName() {
        return NAME;
    }

    private final Tracker tracker;
    private final TrackerGPS trackerGPS;
    private final TrackerPressure trackerPressure;

    private Double mElevationOffset = null;
    private Double mAverageGpsElevation = null;
    private long minEleAverageCutoffTime = Long.MAX_VALUE;
    private boolean mAltitudeAdjust = true;
    private boolean mAltitudeFromGpsAverage = true;
    @SuppressWarnings("unused")
    private boolean isStarted;

    public TrackerElevation(Tracker tracker, TrackerGPS trackerGPS, TrackerPressure trackerPressure){
        this.tracker = tracker;
        this.trackerGPS = trackerGPS;
        this.trackerPressure = trackerPressure;
    }

    @SuppressLint("NewApi")
    public Double getValue() {
        Double val;
        Float pressure = tracker.getCurrentPressure();
        if (pressure != null) {
            //Pressure available - use it for elevation
            //TODO get real sea level pressure (online) or set offset from start/end
            //noinspection InlinedApi
            val = ((Float) SensorManager.getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE, pressure)).doubleValue();
            if (mElevationOffset == null) {
                //"Lock" the offset (can be unlocked in onLocationChanged)
                if (mAltitudeFromGpsAverage && mAverageGpsElevation != null) {
                    //pressure is low-pass filtered, compare to low-pass GPS elevation
                    mElevationOffset = mAverageGpsElevation - val;
                } else {
                    mElevationOffset = 0D;
                }
            }
            val += mElevationOffset;
        } else if (tracker.getLastKnownLocation() != null && tracker.getLastKnownLocation().hasAltitude()) {
            val = tracker.getLastKnownLocation().getAltitude();
        } else {
            val = null;
        }
        return val;
    }

    public void onLocationChanged(android.location.Location arg0) {
        if (arg0.hasAltitude()
                && (mElevationOffset == null || arg0.getTime() < minEleAverageCutoffTime || !isStarted)) {
            //If mElevationOffset is not "used" yet or shortly after first GPS, update the average
            Double ele = arg0.getAltitude();
            final int minElevationStabilizeTime = 60;
            if (minEleAverageCutoffTime == Long.MAX_VALUE) {
                minEleAverageCutoffTime = arg0.getTime() + minElevationStabilizeTime * 1000;
            }
            if (mAverageGpsElevation == null) {
                mAverageGpsElevation = ele;
            } else {
                final float alpha = 0.5f;
                mAverageGpsElevation = mAverageGpsElevation * alpha + (1 - alpha) * ele;
            }
            //Recalculate offset when needed
            mElevationOffset = null;
        }
    }


    @Override
    public void onSensorChanged(SensorEvent event) { }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    /**
     * Sensor is available
     */
    //@SuppressWarnings("unused")
    //public static boolean isAvailable(@SuppressWarnings("UnusedParameters") final Context context) {
    //    //Need trackerGPS or trackerPressure to determine this
    //    //GPS is mandatory
    //    return true;
    //}

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
        if (trackerGPS.isConnected() || trackerPressure.isConnected()) {
            res = ResultCode.RESULT_OK;
        } else {
            res = ResultCode.RESULT_NOT_SUPPORTED;
        }
        return res;
    }

    @Override
    public boolean isConnected() {
        return (trackerGPS.isConnected() || trackerPressure.isConnected());
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
        minEleAverageCutoffTime = Long.MAX_VALUE;
        mElevationOffset = null;
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
        minEleAverageCutoffTime = Long.MAX_VALUE;
        mElevationOffset = null;
        mAverageGpsElevation = null;
    }

    /**
     * Called by tracked after workout has ended
     */
    @Override
    public ResultCode onEnd(Callback callback, Context context) {
        isStarted = false;
        minEleAverageCutoffTime = Long.MAX_VALUE;
        mElevationOffset = null;
        mAverageGpsElevation = null;
        return ResultCode.RESULT_OK;
    }
}
