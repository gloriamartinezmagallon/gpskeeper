package navdev.gpstrack.gpsutils.tracker.workout;

import android.location.Location;

public interface WorkoutInfo {
    double get(Scope scope, Dimension d);

    double getDistance(Scope scope);

    double getTime(Scope scope);

    double getSpeed(Scope scope);

    double getPace(Scope scope);

    double getDuration(Scope scope, Dimension dimension);

    double getRemaining(Scope scope, Dimension dimension);

    //int getSport();
    //Intensity getIntensity();

    /* TODO make better/more elaborate state visible... */
    boolean isPaused();

    boolean isEnabled(Dimension dim, Scope scope);

    Location getLastKnownLocation();
}
