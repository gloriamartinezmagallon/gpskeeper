package navdev.gpstrack.db;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import java.util.Date;
import java.util.List;

public class ActivitiesViewModel extends AndroidViewModel {

    ActivityDao activityDao;

    public ActivitiesViewModel(@NonNull Application application) {
        super(application);
        activityDao = GpsTrackDB.getDatabase(application).activityDao();
    }

    public LiveData<ActivitiesStatistics> getStatitics(Date date){
        return activityDao.getStatitics(date);
    }

    public LiveData<Integer> count(){
        return activityDao.numOfActivities();
    }

    public LiveData<List<ActivityComplete>> getAllActivities(){
        return activityDao.getAllActivities();
    }

}