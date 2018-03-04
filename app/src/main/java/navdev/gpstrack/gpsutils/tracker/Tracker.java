package navdev.gpstrack.gpsutils.tracker;


import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import navdev.gpstrack.gpsutils.notifications.ForegroundNotificationDisplayStrategy;
import navdev.gpstrack.gpsutils.notifications.NotificationState;
import navdev.gpstrack.gpsutils.notifications.NotificationStateManager;
import navdev.gpstrack.gpsutils.notifications.OngoingState;
import navdev.gpstrack.gpsutils.tracker.component.TrackerComponent;
import navdev.gpstrack.gpsutils.tracker.component.TrackerComponentCollection;
import navdev.gpstrack.gpsutils.tracker.component.TrackerElevation;
import navdev.gpstrack.gpsutils.tracker.component.TrackerGPS;
import navdev.gpstrack.gpsutils.tracker.component.TrackerPressure;
import navdev.gpstrack.gpsutils.tracker.component.TrackerState;
import navdev.gpstrack.gpsutils.tracker.workout.Scope;
import navdev.gpstrack.gpsutils.tracker.workout.Workout;
import navdev.gpstrack.gpsutils.utils.BuildConfig;
import navdev.gpstrack.gpsutils.utils.Constants;
import navdev.gpstrack.gpsutils.utils.Formatter;
import navdev.gpstrack.gpsutils.utils.ValueModel;

public class Tracker extends android.app.Service implements
        LocationListener, Constants {
    public static final int MAX_HR_AGE = 3000; // 3s

    private final Handler handler = new Handler();

    TrackerComponentCollection components = new TrackerComponentCollection();
    //Some trackers may select separate sensors depending on sport, handled in onBind()
    TrackerGPS trackerGPS = (TrackerGPS) components.addComponent(new TrackerGPS(this));

    private TrackerPressure trackerPressure = (TrackerPressure) components.addComponent(new TrackerPressure());
    private TrackerElevation trackerElevation = (TrackerElevation) components.addComponent(new TrackerElevation(this, trackerGPS, trackerPressure));


    long mLapId = 0;
    long mActivityId = 0;
    long mElapsedTimeMillis = 0;
    double mElapsedDistance = 0;
    double mHeartbeats = 0;
    double mHeartbeatMillis = 0; // since we might loose HRM connectivity...
    long mMaxHR = 0;

    final boolean mWithoutGps = false;

    TrackerState nextState; //
    final ValueModel<TrackerState> state = new ValueModel<TrackerState>(TrackerState.INIT);
    int mLocationType = LOCATION.TYPE_START;

    /**
     * Last location given by LocationManager
     */
    Location mLastLocation = null;
    //Second to last location - to get speed
    Location mLast2Location = null;

    /**
     * Last location given by LocationManager when in state STARTED
     */
    Location mActivityLastLocation = null;

    SQLiteDatabase mDB = null;
    PowerManager.WakeLock mWakeLock = null;
    final List<WorkoutObserver> liveLoggers = new ArrayList<WorkoutObserver>();

    private Workout workout = null;

    private NotificationStateManager notificationStateManager;

    private NotificationState activityOngoingState;

    @Override
    public void onCreate() {
        notificationStateManager = new NotificationStateManager(
                new ForegroundNotificationDisplayStrategy(this));

        wakeLock(false);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    @Override
    public void onDestroy() {

        reset();
    }

    public void setup() {
        switch (state.get()) {
            case INIT:
                break;
            case INITIALIZING:
            case INITIALIZED:
                return;
            case CONNECTING:
            case CONNECTED:
            case STARTED:
            case PAUSED:
            case ERROR:
            case STOPPED:
                if (BuildConfig.DEBUG) { throw new AssertionError(); }
                return;
            case CLEANUP:
                /**
                 * if CLEANUP is in progress, setup will continue once complete
                 */
                nextState = TrackerState.INITIALIZING;
                return;
        }

        state.set(TrackerState.INITIALIZING);

        TrackerComponent.ResultCode result = components.onInit(onInitCallback,
                getApplicationContext());
        if (result != TrackerComponent.ResultCode.RESULT_PENDING) {
            onInitCallback.run(components, result);
        }
    }

    private final TrackerComponent.Callback onInitCallback = new TrackerComponent.Callback() {
        @Override
        public void run(TrackerComponent component, TrackerComponent.ResultCode resultCode) {
            if (resultCode == TrackerComponent.ResultCode.RESULT_ERROR_FATAL) {
                state.set(TrackerState.ERROR);
            } else {
                state.set(TrackerState.INITIALIZED);
            }

            Log.e(getClass().getName(), "state.set(" + getState() + ")");
            handleNextState();
        }
    };

    private void handleNextState() {
        if (nextState == null)
            return;

        /* if last phase ended in error,
         * don't continue with a new */
        if (state.get() == TrackerState.ERROR)
            return;

        if (state.get() == nextState) {
            nextState = null;
            return;
        }

        switch(nextState) {
            case INIT:
                reset();
                break;
            case INITIALIZING:
                break;
            case INITIALIZED:
                setup();
                break;
            case CONNECTING:
                break;
            case CONNECTED:
                connect();
                break;
            case STARTED:
                break;
            case PAUSED:
                break;
            case STOPPED:
                break;
            case CLEANUP:
                break;
            case ERROR:
                break;
        }
    }

    public void connect() {
        Log.e(getClass().getName(), "Tracker.connect() - state: " + state.get());
        switch (state.get()) {
            case INIT:
                setup();
            case INITIALIZING:
            case CLEANUP:
                nextState = TrackerState.CONNECTED;
                Log.e(getClass().getName(), " => nextState: " + nextState);
                return;
            case INITIALIZED:
                break;
            case CONNECTING:
            case CONNECTED:
                return;
            case STARTED:
            case PAUSED:
            case ERROR:
            case STOPPED:
                if (BuildConfig.DEBUG) { throw new AssertionError(); }
                return;
        }

        state.set(TrackerState.CONNECTING);

        wakeLock(true);

        TrackerComponent.ResultCode result = components.onConnecting(onConnectCallback,
                getApplicationContext());
        if (result != TrackerComponent.ResultCode.RESULT_PENDING) {
            onConnectCallback.run(components, result);
        }
    }

    private final TrackerComponent.Callback onConnectCallback = new TrackerComponent.Callback() {
        @Override
        public void run(TrackerComponent component, TrackerComponent.ResultCode resultCode) {
            if (resultCode == TrackerComponent.ResultCode.RESULT_ERROR_FATAL) {
                state.set(TrackerState.ERROR);
            } else if (state.get() == TrackerState.CONNECTING) {
                state.set(TrackerState.CONNECTED);
                /* now we're connected */
                components.onConnected();
            }
        }
    };

     private long createActivity() {

        return mActivityId;
    }

    public void setWorkout(Workout workout) {
        this.workout = workout;
    }

    public void start() {
//        Log.e(getClass().getName(), "Tracker.start() state: " + state.get());
        if (BuildConfig.DEBUG && state.get() != TrackerState.CONNECTED) { throw new AssertionError(); }

        // connect workout and tracker
        workout.setTracker(this);

        createActivity();

        // do bindings
        doBind();

        // Let workout do initializations
        workout.onInit(workout);

        // Let components know we're starting
        components.onStart();

        mElapsedTimeMillis = 0;
        mElapsedDistance = 0;
        mHeartbeats = 0;
        mHeartbeatMillis = 0;
        mMaxHR = 0;
        // TODO: check if mLastLocation is recent enough
        mActivityLastLocation = null;

        // New location update will be tagged with START
        setNextLocationType(LOCATION.TYPE_START);

        state.set(TrackerState.STARTED);

        activityOngoingState = new OngoingState(new Formatter(this), workout, this);

        /**
         * And finally let workout know that we started
         */
        workout.onStart(Scope.ACTIVITY, this.workout);
    }

    private void doBind() {
        /**
         * Let components populate bindValues
         */
        HashMap<String, Object> bindValues = new HashMap<String, Object>();
        Context ctx = getApplicationContext();
        bindValues.put(TrackerComponent.KEY_CONTEXT, ctx);
        bindValues.put(Workout.KEY_FORMATTER, new Formatter(ctx));
        bindValues.put(Workout.KEY_MUTE, workout.getMute());
        bindValues.put(Workout.KEY_WORKOUT_TYPE, workout.getWorkoutType());

        components.onBind(bindValues);

        /**
         * and then give them to workout
         */
        workout.onBind(workout, bindValues);
    }

    public void pause() {
        switch (state.get()) {
            case INIT:
            case ERROR:
            case INITIALIZING:
            case INITIALIZED:
            case PAUSED:
            case CONNECTING:
            case CONNECTED:
            case CLEANUP:
            case STOPPED:
                return;
            case STARTED:
                break;
        }
        state.set(TrackerState.PAUSED);
        setNextLocationType(LOCATION.TYPE_PAUSE);
        if (mActivityLastLocation != null) {
            /**
             * This saves mLastLocation as a PAUSE location
             */
            internalOnLocationChanged(mActivityLastLocation);
        }

        components.onPause();
    }

    public void stop() {
        switch (state.get()) {
            case INIT:
            case ERROR:
            case INITIALIZING:
            case INITIALIZED:
            case CONNECTING:
            case CONNECTED:
            case CLEANUP:
            case STOPPED:
                return;
            case PAUSED:
            case STARTED:
                break;
        }
        state.set(TrackerState.STOPPED);
        setNextLocationType(LOCATION.TYPE_PAUSE);
        if (mActivityLastLocation != null) {
            /**
             * This saves mLastLocation as a PAUSE location
             */
            internalOnLocationChanged(mActivityLastLocation);
        }

        components.onPause(); // TODO add new callback for this
    }

    private void internalOnLocationChanged(Location arg0) {
      
        onLocationChangedImpl(arg0, true);
    }

    public void resume() {
        switch (state.get()) {
            case INIT:
            case ERROR:
            case INITIALIZING:
            case CLEANUP:
            case INITIALIZED:
            case CONNECTING:
            case CONNECTED:
                if (BuildConfig.DEBUG) { throw new AssertionError(); }
                return;
            case PAUSED:
            case STOPPED:
                break;
            case STARTED:
                return;
        }

        // TODO: check is mLastLocation is recent enough
        mActivityLastLocation = mLastLocation;
        state.set(TrackerState.STARTED);
        setNextLocationType(LOCATION.TYPE_RESUME);
        if (mActivityLastLocation != null) {
            /**
             * save last know location as resume location
             */
            internalOnLocationChanged(mActivityLastLocation);
        }
    }

    public void reset() {
        switch (state.get()) {
            case INIT:
                return;
            case INITIALIZING:
                // cleanup when INITIALIZE is complete
                nextState = TrackerState.INIT;
                return;
            case INITIALIZED:
            case ERROR:
            case PAUSED:
            case CONNECTING:
            case CONNECTED:
            case STOPPED:
                nextState = TrackerState.INIT;
                // it's ok to "abort" connecting
                break;
            case STARTED:
                if (BuildConfig.DEBUG) { throw new AssertionError(); }
                return;
            case CLEANUP:
                return;
        }

        wakeLock(false);

        if (workout != null) {
            workout.setTracker(null);
            workout = null;
        }

        state.set(TrackerState.CLEANUP);
        liveLoggers.clear();
        TrackerComponent.ResultCode res = components.onEnd(onEndCallback, getApplicationContext());
        if (res != TrackerComponent.ResultCode.RESULT_PENDING)
            onEndCallback.run(components, res);
    }

    private final TrackerComponent.Callback onEndCallback = new TrackerComponent.Callback() {
        @Override
        public void run(TrackerComponent component, TrackerComponent.ResultCode resultCode) {
            if (resultCode == TrackerComponent.ResultCode.RESULT_ERROR_FATAL) {
                state.set(TrackerState.ERROR);
            } else {
                state.set(TrackerState.INIT);
            }

            handleNextState();
        }
    };

    public void completeActivity(boolean save) {
        if (BuildConfig.DEBUG &&
                state.get() != TrackerState.PAUSED &&
                state.get() != TrackerState.STOPPED) {
            throw new AssertionError();
        }

        setNextLocationType(LOCATION.TYPE_END);
        if (mActivityLastLocation != null) {
            internalOnLocationChanged(mActivityLastLocation);
        }

        if (save) {
            liveLog(LOCATION.TYPE_END);
        }
        components.onComplete(!save);
        notificationStateManager.cancelNotification();
        reset();
    }



    void setNextLocationType(int newType) {

        mLocationType = newType;
    }

    public long getTime() {
        return mElapsedTimeMillis / 1000;
    }

    public double getDistance() {
        return mElapsedDistance;
    }

    public Location getLastKnownLocation() {
        return mLastLocation;
    }

    public long getActivityId() {
        return mActivityId;
    }

    @Override
    public void onLocationChanged(Location arg0) {
        //Elevation depends on GPS updates
        trackerElevation.onLocationChanged(arg0);
        onLocationChangedImpl(arg0, false);
    }

    private void onLocationChangedImpl(Location arg0, boolean internal) {
        long now = System.currentTimeMillis();


        //if (internal || state.get() == TrackerState.STARTED) {

            Double eleValue = getCurrentElevation();
            Float pressureValue = getCurrentPressure();
            if (mActivityLastLocation != null) {
                double timeDiff = (double) (arg0.getTime() - mActivityLastLocation
                        .getTime());
                double distDiff = arg0.distanceTo(mActivityLastLocation);
                if (timeDiff < 0) {
                    // time moved backward ??
                    Log.e(getClass().getName(), "lastTime:       " + mActivityLastLocation.getTime());
                    Log.e(getClass().getName(), "arg0.getTime(): " + arg0.getTime());
                    Log.e(getClass().getName(), " => delta time: " + timeDiff);
                    Log.e(getClass().getName(), " => delta dist: " + distDiff);
                    // TODO investigate if this is known...only seems to happen
                    // in emulator
                    timeDiff = 0;
                }
                mElapsedTimeMillis += timeDiff;
                mElapsedDistance += distDiff;

            }
            mActivityLastLocation = arg0;


            switch (mLocationType) {
                case LOCATION.TYPE_START:
                case LOCATION.TYPE_RESUME:
                    liveLog(mLocationType);
                    setNextLocationType(LOCATION.TYPE_GPS);
                    break;
                case LOCATION.TYPE_GPS:
                    break;
                case LOCATION.TYPE_PAUSE:
                    break;
                case LOCATION.TYPE_END:
                    if (!internal && BuildConfig.DEBUG) { throw new AssertionError(); }
                    break;
            }
            liveLog(mLocationType);

            notificationStateManager.displayNotificationState(activityOngoingState);
        //}
        mLast2Location = mLastLocation;
        mLastLocation = arg0;
    }

    private void liveLog(int type) {
        for (WorkoutObserver l : liveLoggers) {
            l.workoutEvent(workout, type);
        }
    }

    @Override
    public void onProviderDisabled(String arg0) {
    }

    @Override
    public void onProviderEnabled(String arg0) {
    }

    @Override
    public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
    }

    public TrackerState getState() {
        return state.get();
    }

    public void registerTrackerStateListener(ValueModel.ChangeListener<TrackerState> listener) {
        state.registerChangeListener(listener);
    }

    public void unregisterTrackerStateListener(ValueModel.ChangeListener<TrackerState> listener) {
        state.unregisterChangeListener(listener);
    }

    /**
     * Service interface stuff...
     */
    public class LocalBinder extends android.os.Binder {
        public Tracker getService() {
            return Tracker.this;
        }
    }

    private final IBinder mBinder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private void wakeLock(boolean get) {
        if (mWakeLock != null) {
            if (mWakeLock.isHeld()) {
                mWakeLock.release();
            }
            mWakeLock = null;
        }
        if (get) {
            PowerManager pm = (PowerManager) this
                    .getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    "RunnerUp");
            if (mWakeLock != null) {
                mWakeLock.acquire();
            }
        }
    }

    public boolean isComponentConfigured(String name) {
        switch (getState()) {
            case INIT:    // before onInit we don't know, so say no
            case CLEANUP: // when cleaning, say no
            case ERROR:   // on error, say no
                return false;
            case INITIALIZING:
                // If we're initializing...say no
                if (components.getResultCode(name) == TrackerComponent.ResultCode.RESULT_PENDING)
                    return false;
            case INITIALIZED:
            case CONNECTING:
            case CONNECTED:
            case STARTED:
            case PAUSED:
            case STOPPED:
                // check component
                break;
        }

        switch (components.getResultCode(name)) {
            case RESULT_OK:
            case RESULT_PENDING:
                return true;
            case RESULT_NOT_SUPPORTED:
            case RESULT_NOT_ENABLED:
            case RESULT_ERROR:
            case RESULT_ERROR_FATAL:
                return false;
        }
        return false;
    }

    public boolean isComponentConnected(String name) {
        TrackerComponent component = components.getComponent(name);
        if (component == null)
            return false;
        return component.isConnected();
    }

    public Float getCurrentPressure() {
        return trackerPressure.getValue();
    }
    public Double getCurrentElevation() {
        return trackerElevation.getValue();
    }

    public Double getCurrentSpeed() {
        return getCurrentSpeed(System.currentTimeMillis(), 3000);
    }

    private Double getCurrentSpeed(long now, long maxAge) {
        if (mLastLocation == null)
            return null;
        if (now > mLastLocation.getTime() + maxAge)
            return null;
        double speed = mLastLocation.getSpeed();
        if ((!mLastLocation.hasSpeed() || speed == 0.0f)
                && mLastLocation != null && mLast2Location != null &&
                mLastLocation.getTime() > mLast2Location.getTime() ) {
            //Some Android (at least emulators) do not implement getSpeed() (even if hasSpeed() is true)
            speed = mLastLocation.distanceTo(mLast2Location) * 1000 / (mLastLocation.getTime() - mLast2Location.getTime());
        }
        return speed;
    }

    public double getHeartbeats() {
        return mHeartbeats;
    }

   /* public Integer getCurrentBatteryLevel() {
        HRProvider hrProvider = trackerHRM.getHrProvider();
        if (hrProvider == null)
            return null;
        return hrProvider.getBatteryLevel();
    }*/

    public Workout getWorkout() {
        return workout;
    }
}
