package navdev.gpstrack.gpsutils.tracker;

import navdev.gpstrack.gpsutils.tracker.workout.WorkoutInfo;

public interface WorkoutObserver {
    // @note: type is in Constants.DB.LOCATION.TYPE
    public void workoutEvent(WorkoutInfo workoutInfo, int type);
}
