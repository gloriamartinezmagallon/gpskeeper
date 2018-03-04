package navdev.gpstrack.gpsutils.tracker.component;

import android.content.Context;

import java.util.HashMap;

/**
 * Created by glori on 10/10/2017.
 */

public interface TrackerComponent {

    public static final String KEY_CONTEXT = "KEY_CONTEXT";

    public enum ResultCode {
        RESULT_OK,
        RESULT_UNKNOWN,       // we don't know if hw is present
        RESULT_NOT_SUPPORTED, // hw not present or not configured
        RESULT_NOT_ENABLED,   // hw is disabled (e.g bluetooth off)
        RESULT_ERROR,         // Component failed to initialize
        RESULT_ERROR_FATAL,   // Component failed, Tracker shouldn't start
        RESULT_PENDING        // will call callback
    }

    public interface Callback {
        void run(TrackerComponent component, ResultCode resultCode);
    }

    /**
     * Component name
     */
    public String getName();

    /**
     * Called by Tracker during initialization
     */
    ResultCode onInit(Callback callback, Context context);

    /**
     * Called by Tracker when connecting
     */
    ResultCode onConnecting(Callback callback, Context context);

    /**
     * is component connected (for some definition of connected)
     *   the value returned here is used to show connected/not connected icons
     */
    boolean isConnected();

    /**
     * Called by Tracker when all "mandatory" components are connected, i.e
     *   we're ready to start
     */
    void onConnected();

    /**
     * Called by Tracker before start
     *   Component shall populate bindValues
     *   with objects that will then be passed
     *   to workout
     */
    void onBind(HashMap<String, Object> bindValues);

    /**
     * Called by Tracker just before workout starts
     */
    void onStart();

    /**
     * Called by Tracker when workout is paused
     */
    void onPause();

    /**
     * Called by Tracker when workout is resumed
     */
    void onResume();

    /**
     * Called by Tracker when workout is complete
     */
    void onComplete(boolean discarded);

    /**
     * Called by tracker when shutting down
     * can be called while CONNECTING (but not while INITIALIZING)
     */
    ResultCode onEnd(Callback callback, Context context);
}
