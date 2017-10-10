package navdev.gpstrack.ent;


public class ActivityLocation {
    int idActivity;
    double latitud;
    double longitud;

    public ActivityLocation(int idActivity, double latitud, double longitud) {
        this.idActivity = idActivity;
        this.latitud = latitud;
        this.longitud = longitud;
    }

    public int getIdActivity() {
        return idActivity;
    }

    public double getLatitud() {
        return latitud;
    }

    public double getLongitud() {
        return longitud;
    }
}
