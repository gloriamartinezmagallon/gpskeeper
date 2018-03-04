package navdev.gpstrack.gpsutils.tracker;

public interface GpsInformation {
    String getGpsAccuracy();

    int getSatellitesAvailable();

    int getSatellitesFixed();
}