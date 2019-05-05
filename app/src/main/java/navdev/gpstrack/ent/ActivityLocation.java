package navdev.gpstrack.ent;


import java.text.SimpleDateFormat;
import java.util.Date;

public class ActivityLocation {
    int idActivity;
    double latitud;
    double longitud;
    double altitud;
    double speed;
    long registerTime;

    public ActivityLocation(int idActivity, double latitud, double longitud, double alt, double speed, long registerTime) {
        this.idActivity = idActivity;
        this.latitud = latitud;
        this.longitud = longitud;
        this.altitud = alt;
        this.speed = speed;
        this.registerTime = registerTime;
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
