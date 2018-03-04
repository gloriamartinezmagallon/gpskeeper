package navdev.gpstrack.gpsutils.tracker.component;

import android.content.Context;

import java.util.HashMap;

/**
 * Created by glori on 10/10/2017.
 */

public abstract class DefaultTrackerComponent implements TrackerComponent {

    /**
     * Component name
     */
    public abstract String getName();

    /**
     * Called by Tracker during initialization
     */
    @Override
    public ResultCode onInit(Callback callback, Context context) {
        return ResultCode.RESULT_UNKNOWN;
    }

    @Override
    public ResultCode onConnecting(Callback callback, Context context) {
        return ResultCode.RESULT_OK;
    }

    @Override
    public boolean isConnected() {
        return true;
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
    public void onBind(HashMap<String, Object> bindValues) {
    }

    /**
     * Called by Tracker when workout starts
     */
    @Override
    public void onStart() {
    }

    /**
     * Called by Tracker when workout is paused
     */
    @Override
    public void onPause() {
    }

    /**
     * Called by Tracker when workout is resumed
     */
    @Override
    public void onResume() {
    }

    /**
     * Called by Tracker when workout is complete
     */
    @Override
    public void onComplete(boolean discarded) {
    }

    /**
     * Called by tracked after workout has ended
     */
    @Override
    public ResultCode onEnd(Callback callback, Context context) {
        return ResultCode.RESULT_OK;
    }
}

