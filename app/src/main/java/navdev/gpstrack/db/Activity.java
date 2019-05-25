package navdev.gpstrack.db;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Date;


@Entity( tableName = "activities" )
public class Activity  implements Serializable {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private Integer id;

    @ColumnInfo(name = "route")
    @NonNull
    private Integer route;

    @ColumnInfo(name = "distance")
    @NonNull
    private Integer distance;

    @ColumnInfo(name = "time")
    @NonNull
    private Integer time;

    @ColumnInfo(name = "adddate")
    @NonNull
    private Date adddate;


    public Activity(Integer id, Integer route, Integer distance, Integer time, Date adddate) {
        this.id = id;
        this.route = route;
        this.distance = distance;
        this.time = time;
        this.adddate = adddate;
    }

    @Ignore
    public Activity(Integer route, Integer distance, Integer time, Date adddate) {
        this.route = route;
        this.distance = distance;
        this.time = time;
        this.adddate = adddate;
    }

    public Integer getId() {
        return id;
    }

    @NonNull
    public Integer getRoute() {
        return route;
    }

    @NonNull
    public Integer getDistance() {
        return distance;
    }

    @NonNull
    public Integer getTime() {
        return time;
    }

    @NonNull
    public Date getAdddate() {
        return adddate;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setRoute(@NonNull Integer route) {
        this.route = route;
    }

    public void setAdddate(@NonNull Date adddate) {
        this.adddate = adddate;
    }

    public void setDistance(@NonNull Integer distance) {
        this.distance = distance;
    }

    public void setTime(@NonNull Integer time) {
        this.time = time;
    }
}
