package navdev.gpstrack;

import android.annotation.SuppressLint;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import navdev.gpstrack.db.ActivitiesStatistics;
import navdev.gpstrack.db.ActivitiesViewModel;
import navdev.gpstrack.db.Activity;
import navdev.gpstrack.db.ActivityComplete;
import navdev.gpstrack.db.ActivityLocation;
import navdev.gpstrack.db.Converters;
import navdev.gpstrack.db.GpsTrackDB;
import navdev.gpstrack.db.ActivityDao;
import navdev.gpstrack.db.RouteDao;
import navdev.gpstrack.utils.MapUtils;

import static navdev.gpstrack.service.TrackerService.SEND_IS_RUNNING;

public class LoadingActivity extends AppCompatActivity {

    LinearLayout mInfoLl;
    ImageView   bgImg;

    ActivityDao mActivityDao;
    RouteDao mRouteDao;

    protected BroadcastReceiver receiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive (Context context, Intent intent)
        {
            if (intent.getAction().equals(SEND_IS_RUNNING)){
               //TODO
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        mActivityDao = GpsTrackDB.getDatabase(this).activityDao();
        mRouteDao = GpsTrackDB.getDatabase(this).routeDao();

        bgImg = findViewById(R.id.imageView);
        mInfoLl = findViewById(R.id.infoLl);

        bgImg.setVisibility(View.VISIBLE);
        mInfoLl.setVisibility(View.GONE);

    }

    @Override
    protected void onResume() {
        super.onResume();
        getInfoBBDD();
    }

    private void checkAllActivities(ActivitiesViewModel viewModel ){
        viewModel.getAllActivities().observe(this, new Observer<List<ActivityComplete>>() {
            @Override
            public void onChanged(@Nullable List<ActivityComplete> activityCompletes) {
                for(ActivityComplete ac: activityCompletes){
                    if (ac.activity.getDistance() == 0 && ac.locations.size() > 0 && ac.locations.get(0).getRegisterTime().getTime() == 0){
                        int distance = 0;
                        LatLng antLoc = null;
                        for (ActivityLocation loc: ac.locations){
                            LatLng location = new LatLng(loc.getLatitud(), loc.getLongitud());
                            if (antLoc != null){
                                distance += MapUtils.distanceBetween(location,antLoc);
                            }
                            antLoc = location;
                        }

                        ac.activity.setDistance(distance);

                        final Activity activity = ac.activity;
                        new Thread(new Runnable() {
                            public void run() {
                                mActivityDao.update(activity);
                            }
                        }).start();
                    }
                }
            }
        });
    }

    @SuppressLint("CheckResult")
    private void getInfoBBDD(){

        ActivitiesViewModel viewModel = ViewModelProviders.of(this).get(ActivitiesViewModel.class);


        checkAllActivities(viewModel);

        final TextView kmsalltimeTV = findViewById(R.id.kmsalltime);
        final TextView numkmthismonthTV = findViewById(R.id.numkmthismonth);
        final TextView timealltimeTV = findViewById(R.id.timealltime);
        final TextView numtimethismonthTV = findViewById(R.id.numtimethismonth);
        final TextView numactivitiesTV = findViewById(R.id.numactivities);
        final TextView numactivitiesthismonthTV = findViewById(R.id.numactivitiesthismonth);
        viewModel.getStatitics(Converters.getFirstDateOfCurrentMonth()).observe(this, new Observer<ActivitiesStatistics>() {
            @Override
            public void onChanged(@Nullable ActivitiesStatistics statistics) {
                kmsalltimeTV.setText(Converters.distanceToString(statistics.numKms) + " " + getResources().getString(R.string.kmsalltime));
                numkmthismonthTV.setText(Converters.distanceToString(statistics.numKmFromDate));

                timealltimeTV.setText(timeTostring(statistics.numTime)+" "+getResources().getString(R.string.timealltime));
                numtimethismonthTV.setText(timeTostring(statistics.numTimeFromDate));

                numactivitiesthismonthTV.setText(Converters.numToString(statistics.numActivitiesFromDate));
                numactivitiesTV.setText(getResources().getString(R.string.activitiesthismonth)+" "+Converters.numToString(statistics.numActivities));

                bgImg.setVisibility(View.GONE);
                mInfoLl.setVisibility(View.VISIBLE);
            }
        });
        final TextView numroutesTV = findViewById(R.id.numroutes);
        mRouteDao.numOfRoutes().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer numOfRoutes) {
                numroutesTV.setText(Converters.numToString(numOfRoutes));
            }
        });
    }

    public void showActivities(View v){
        Intent newintent = new Intent(LoadingActivity.this, ActivitiesActivity.class);
        startActivity(newintent);
    }

    public void showRoutes(View v){
        Intent newintent = new Intent(LoadingActivity.this, RoutesActivity.class);
        startActivity(newintent);
    }

    public String timeTostring(int tiempo) {
        int seconds = (int) (tiempo % 60);
        int minutes = (int) (tiempo / 60) % 60;
        int hours = (int) ((tiempo / (60 * 60)) % 24);

        String txseconds, txminutes, txhours;
        if (seconds < 10) txseconds = "0" + seconds;
        else txseconds = seconds + "";

        if (minutes < 10) txminutes = "0" + minutes;
        else txminutes = minutes + "";

        if (hours < 10) txhours = "0" + hours;
        else txhours = hours + "";

        return txhours + ":" + txminutes;
    }

    @Override
    protected void onStart() {
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(getApplicationContext());
        manager.registerReceiver(receiver, new IntentFilter(Intent.ACTION_VIEW));
        super.onStart();
    }

}
