package navdev.gpstrack.gpsutils.tracker.workout;

import android.content.SharedPreferences;
import android.location.Location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import navdev.gpstrack.gpsutils.tracker.Tracker;
import navdev.gpstrack.gpsutils.utils.BuildConfig;
import navdev.gpstrack.gpsutils.utils.Constants;

public class Workout implements WorkoutComponent, WorkoutInfo {

    long lap = 0;
    int currentStepNo = -1;
    int workoutType = Constants.WORKOUT_TYPE.BASIC;
    Step currentStep = null;
    boolean paused = false;
    final ArrayList<Step> steps = new ArrayList<Step>();
    final ArrayList<WorkoutStepListener> stepListeners = new ArrayList<WorkoutStepListener>();
    private boolean mute;


    Tracker tracker = null;
    SharedPreferences audioCuePrefs;

    public static final String KEY_COUNTER_VIEW = "CountdownView";
    public static final String KEY_FORMATTER = "Formatter";
    public static final String KEY_MUTE = "mute";
    public static final String KEY_WORKOUT_TYPE = "type";

    public Workout() {
    }

    public void setTracker(Tracker tracker) {
        this.tracker = tracker;
    }

    public void onInit(Workout w) {
        if (BuildConfig.DEBUG && w != this) { throw new AssertionError(); }
        for (Step a : steps) {
            a.onInit(this);
        }
    }

    public void onBind(Workout w, HashMap<String, Object> bindValues) {
        for (Step a : steps) {
            a.onBind(w, bindValues);
        }
    }

    public void onEnd(Workout w) {
        if (BuildConfig.DEBUG && w != this) { throw new AssertionError(); }
        for (Step a : steps) {
            a.onEnd(this);
        }
    }

    @Override
    public void onRepeat(int current, int limit) {
    }

    public void onStart(Scope s, Workout w) {
        if (BuildConfig.DEBUG && w != this) { throw new AssertionError(); }

        for (Step st : steps) {
            st.onRepeat(0, 1);
        }

        currentStepNo = 0;
        if (steps.size() > 0) {
            setCurrentStep(steps.get(currentStepNo));
        }

        if (currentStep != null) {
            currentStep.onStart(Scope.ACTIVITY, this);
            currentStep.onStart(Scope.STEP, this);
            currentStep.onStart(Scope.LAP, this);
        }
    }

    private void setCurrentStep(Step step) {
        Step oldStep = currentStep;
        currentStep = step;

        Step newStep = (step == null) ? null : step.getCurrentStep();
        for (WorkoutStepListener l : stepListeners) {
            l.onStepChanged(oldStep, newStep);
        }
    }

    public void onTick() {

        while (currentStep != null) {
            boolean finished = currentStep.onTick(this);
            if (finished == false)
                break;

            onNextStep();
        }
    }

    public void onNextStep() {
        currentStep.onComplete(Scope.LAP, this);
        currentStep.onComplete(Scope.STEP, this);

        if (currentStep.onNextStep(this))
            currentStepNo++;

        if (currentStepNo < steps.size()) {
            setCurrentStep(steps.get(currentStepNo));
            currentStep.onStart(Scope.STEP, this);
            currentStep.onStart(Scope.LAP, this);
        } else {
            currentStep.onComplete(Scope.ACTIVITY, this);
            setCurrentStep(null);
            tracker.stop();
        }
    }

    public void onPause(Workout w) {

        if (currentStep != null) {
            currentStep.onPause(this);
        }
        paused = true;
    }

    public void onNewLap() {
        if (currentStep != null) {
            currentStep.onComplete(Scope.LAP, this);
            currentStep.onStart(Scope.LAP, this);
        }
    }

    public void onNewLapOrNextStep() {
        if (!isLastStep()) {
            onNextStep();
        } else {
            onNewLap();
        }
    }

    public void onStop(Workout w) {

        if (currentStep != null) {
            currentStep.onStop(this);
        }
    }

    public void onResume(Workout w) {
        if (currentStep != null) {
            currentStep.onResume(this);
        }
        paused = false;
    }

    public void onComplete(Scope s, Workout w) {
        if (currentStep != null) {
            currentStep.onComplete(Scope.LAP, this);
            currentStep.onComplete(Scope.STEP, this);
            currentStep.onComplete(Scope.ACTIVITY, this);
        }
        setCurrentStep(null);
        currentStepNo = -1;
    }

    public void onSave() {
        tracker.completeActivity(true);
    }

    public void onDiscard() {
        tracker.completeActivity(false);
    }

    @Override
    public boolean isPaused() {
        return paused;
    }

    @Override
    public double get(Scope scope, Dimension d) {
        switch (d) {
            case DISTANCE:
                return getDistance(scope);
            case TIME:
                return getTime(scope);
            case SPEED:
                return getSpeed(scope);
            case PACE:
                return getPace(scope);

        }
        return 0;
    }

    @Override
    public double getDistance(Scope scope) {
        switch (scope) {
            case ACTIVITY:
                return tracker.getDistance();
            case STEP:
            case LAP:
                if (currentStep != null)
                    return currentStep.getDistance(this, scope);
                if (BuildConfig.DEBUG) { throw new AssertionError(); }
                break;
            case CURRENT:
                break;
        }
        return 0;
    }

    @Override
    public double getTime(Scope scope) {
        switch (scope) {
            case ACTIVITY:
                return tracker.getTime();
            case STEP:
            case LAP:
                if (currentStep != null)
                    return currentStep.getTime(this, scope);
                if (BuildConfig.DEBUG) { throw new AssertionError(); }
                break;
            case CURRENT:
                return System.currentTimeMillis() / 1000; // now
        }
        return 0;
    }

    @Override
    public double getSpeed(Scope scope) {
        switch (scope) {
            case ACTIVITY:
                double d = getDistance(scope);
                double t = getTime(scope);
                if (t == 0)
                    return (double) 0;
                return d / t;
            case STEP:
            case LAP:
                if (currentStep != null)
                    return currentStep.getSpeed(this, scope);
                break;
            case CURRENT:
                Double s = tracker.getCurrentSpeed();
                if (s != null)
                    return s;
                return 0;
        }
        return 0;
    }

    @Override
    public double getPace(Scope scope) {
        double s = getSpeed(scope);
        if (s != 0)
            return 1.0d / s;
        return 0;
    }

    @Override
    public double getDuration(Scope scope, Dimension dimension) {
        if (scope == Scope.STEP && currentStep != null) {
            return currentStep.getDuration(dimension);
        }
        return 0;
    }

    @Override
    public double getRemaining(Scope scope, Dimension dimension) {
        double curr = this.get(scope, dimension);
        double duration = this.getDuration(scope, dimension);
        if (duration > curr) {
            return duration - curr;
        } else {
            return 0;
        }
    }


    @Override
    public boolean isEnabled(Dimension dim, Scope scope) {
        if ((dim == Dimension.SPEED || dim == Dimension.PACE) &&
                scope == Scope.CURRENT) {
            return tracker.getCurrentSpeed() != null;
        }
        return true;
    }


    public int getStepCount() {
        return steps.size();
    }

    public boolean isLastStep() {
        if (currentStepNo + 1 < steps.size())
            return false;
        if (currentStepNo < steps.size())
            return steps.get(currentStepNo).isLastStep();
        return true;
    }

    /**
     * @return flattened list of all steps in workout
     */
    static public class StepListEntry {
        public StepListEntry(int index, Step step, int level, Step parent) {
            this.index = index;
            this.level = level;
            this.step = step;
            this.parent = parent;
        }

        public final int index;
        public final int level;
        public final Step parent;
        public final Step step;
    }

    public void addStep(Step s) {
        steps.add(s);
    }

    public List<Step> getSteps() {
        return steps;
    }

    public List<StepListEntry> getStepList() {
        ArrayList<StepListEntry> list = new ArrayList<StepListEntry>();
        for (Step s : steps) {
            s.getSteps(null, 0, list);
        }
        return list;
    }

    public Step getCurrentStep() {
        if (currentStepNo >= 0 && currentStepNo < steps.size())
            return steps.get(currentStepNo).getCurrentStep();
        return null;
    }

    public void registerWorkoutStepListener(WorkoutStepListener listener) {
        stepListeners.add(listener);
    }

    public void unregisterWorkoutStepListener(WorkoutStepListener listener) {
        stepListeners.remove(listener);
    }

    private static class FakeWorkout extends Workout {

        FakeWorkout() {
            super();
        }

        @Override
        public boolean isEnabled(Dimension dim, Scope scope) {
            return true;
        }

        public double getDistance(Scope scope) {
            switch (scope) {
                case ACTIVITY:
                    return (3000 + 7000 * Math.random());
                case STEP:
                    return (300 + 700 * Math.random());
                case LAP:
                    return (300 + 700 * Math.random());
                case CURRENT:
                    return 0;
            }
            return 0;
        }

        public double getTime(Scope scope) {
            switch (scope) {
                case ACTIVITY:
                    return (10 * 60 + 50 * 60 * Math.random());
                case STEP:
                    return (1 * 60 + 5 * 60 * Math.random());
                case LAP:
                    return (1 * 60 + 5 * 60 * Math.random());
                case CURRENT:
                    return System.currentTimeMillis() / 1000;
            }
            return 0;
        }

        public double getSpeed(Scope scope) {
            double d = getDistance(scope);
            double t = getTime(scope);
            if (t == 0)
                return 0;
            return d / t;
        }

        public double getHeartRate(Scope scope) {
            return 150 + 25 * Math.random();
        }
    }

    @Override
    public Location getLastKnownLocation() {
        return tracker.getLastKnownLocation();
    }

    public static Workout fakeWorkoutForTestingAudioCue() {
        FakeWorkout w = new FakeWorkout();
        return w;
    }

    public void setMute(boolean mute) {
        this.mute = mute;
    }

    public boolean getMute() {
        return mute;
    }

    public void setWorkoutType(int workoutType) {
        this.workoutType = workoutType;
    }

    public int getWorkoutType() {
        return this.workoutType;
    }

}

