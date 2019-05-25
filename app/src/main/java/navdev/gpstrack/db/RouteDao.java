package navdev.gpstrack.db;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;
import android.arch.persistence.room.Update;

import java.util.List;

import navdev.gpstrack.db.Route;

@Dao
public interface RouteDao {


    @Query("SELECT * FROM routes WHERE id = :id LIMIT 1")
    @Transaction
    Route findRouteById(long id);


    @Query("SELECT count(*) FROM routes")
    @Transaction
    LiveData<Integer> numOfRoutes();



    @Query("SELECT * FROM routes")
    @Transaction
    LiveData<List<Route>> getAllRoutes();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Route route);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Route... route);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(Route route);

    @Delete()
    void delete(Route route);
}
