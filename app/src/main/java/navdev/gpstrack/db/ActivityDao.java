package navdev.gpstrack.db;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Relation;
import android.arch.persistence.room.Transaction;
import android.arch.persistence.room.Update;

import java.util.Date;
import java.util.List;

import navdev.gpstrack.db.Activity;
import navdev.gpstrack.db.ActivityComplete;
import navdev.gpstrack.db.ActivityLocation;

@Dao
public interface ActivityDao {

    @Query("SELECT * FROM activities WHERE id = :id LIMIT 1")
    @Transaction
    ActivityComplete findActivyById(Integer id);

    @Query("SELECT SUM(distance) as numKms, " +
            "      SUM(CASE WHEN adddate >= :date THEN distance ELSE 0 END) as numKmFromDate, " +
            "      SUM(time) as numTime, " +
            "      SUM(CASE WHEN adddate >= :date THEN time ELSE 0 END) as numTimeFromDate, " +
            "      COUNT(*) as numActivities, " +
            "      SUM(CASE WHEN adddate >= :date THEN 1 ELSE 0 END) as numActivitiesFromDate, " +
            "      MIN(adddate) as minDate, " +
            "      MAX(adddate) as maxDate " +
            "FROM activities ")
    LiveData<ActivitiesStatistics> getStatitics(Date date);

    @Query("SELECT * FROM activities")
    @Transaction
    LiveData<List<ActivityComplete>> getAllActivities();



    @Query("SELECT count(*) FROM activities")
    @Transaction
    LiveData<Integer> numOfActivities();


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Activity activity);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Activity... activity);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(Activity activity);

    @Delete()
    void delete(Activity activity);


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertLocation(ActivityLocation activityLocation);

}
