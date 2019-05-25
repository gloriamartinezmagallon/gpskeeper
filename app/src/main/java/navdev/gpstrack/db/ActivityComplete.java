package navdev.gpstrack.db;

import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Relation;

import java.io.Serializable;
import java.util.List;


public class ActivityComplete implements Serializable {

    @Embedded
    public Activity activity;

    @Relation(parentColumn = "route", entityColumn = "id", entity = Route.class)
    public List<Route> route;


    @Relation(parentColumn = "id", entityColumn = "activity", entity = ActivityLocation.class)
    public List<ActivityLocation> locations;



}
