package navdev.gpstrack.gpsutils.tracker.workout;

public interface WorkoutStepListener {
    public void onStepChanged(Step oldStep, Step newStep);
}
