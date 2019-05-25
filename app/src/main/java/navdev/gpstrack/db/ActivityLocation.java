package navdev.gpstrack.db;


import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Date;

@Entity( tableName = "positionactivity" )
public class ActivityLocation  implements Serializable {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    Integer id;

    @ColumnInfo(name = "activity")
    @NonNull
    Integer activity;

    @ColumnInfo(name = "lat")
    @NonNull
    double latitud;

    @ColumnInfo(name = "lng")
    @NonNull
    double longitud;

    @ColumnInfo(name = "alt")
    @NonNull
    double altitud;

    @ColumnInfo(name = "speed")
    @NonNull
    double speed;

    @ColumnInfo(name = "registerTime")
    @NonNull
    Date registerTime;

    public ActivityLocation(Integer id, Integer activity, double latitud, double longitud, double altitud, double speed, Date registerTime) {
        this.id = id;
        this.activity = activity;
        this.latitud = latitud;
        this.longitud = longitud;
        this.altitud = altitud;
        this.speed = speed;
        this.registerTime = registerTime;
    }

    @Ignore
    public ActivityLocation(Integer activity, double latitud, double longitud, double alt, double speed, Date registerTime) {
        this.activity = activity;
        this.latitud = latitud;
        this.longitud = longitud;
        this.altitud = alt;
        this.speed = speed;
        this.registerTime = registerTime;
    }

    public double getLatitud() {
        return latitud;
    }

    public double getLongitud() {
        return longitud;
    }

    public double getAltitud() {
        return altitud;
    }

    public double getSpeed() {
        return speed;
    }

    @NonNull
    public Date getRegisterTime() {
        return registerTime;
    }
}
