package navdev.gpstrack.db;

import android.arch.persistence.room.ColumnInfo;

import java.util.Date;

public class ActivitiesStatistics {

    @ColumnInfo(name = "numKms")
    public int numKms;

    @ColumnInfo(name = "numKmFromDate")
    public int numKmFromDate;

    @ColumnInfo(name = "numTime")
    public int numTime;

    @ColumnInfo(name = "numTimeFromDate")
    public int numTimeFromDate;

    @ColumnInfo(name = "numActivities")
    public int numActivities;

    @ColumnInfo(name = "numActivitiesFromDate")
    public int numActivitiesFromDate;

    @ColumnInfo(name = "minDate")
    public Date minDate;
    @ColumnInfo(name = "maxDate")
    public Date maxDate;
}
