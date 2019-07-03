package navdev.gpstrack;

import android.app.AlertDialog;
import android.arch.lifecycle.Observer;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import java.util.ArrayList;

import navdev.gpstrack.db.Converters;
import navdev.gpstrack.db.GpsTrackDB;
import navdev.gpstrack.db.ActivityDao;
import navdev.gpstrack.db.ActivityComplete;
import navdev.gpstrack.db.ActivityLocation;
import navdev.gpstrack.db.Route;
import navdev.gpstrack.utils.MapUtils;

public class ActivityDetailsActivity extends AppCompatActivity {

    ActivityComplete mActivity;
    Route mRoute;


    ActivityLocation maxSpeed = null;
    ActivityLocation minSpeed = null;

    GoogleMap mMap = null;

    public static String ACTIVITY ="Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        final TextView activityDate = findViewById(R.id.activiy_date);
        TextView activityName =  findViewById(R.id.activity_name);
        TextView activityDistance=  findViewById(R.id.activity_distance);
        TextView activityTime=  findViewById(R.id.activity_time);


        final TextView activityMaxSpeed=  findViewById(R.id.activity_maxSpeed);
        final TextView activityMinSpeed=  findViewById(R.id.activity_minSpeed);
        final TextView activityAvgSpeed=  findViewById(R.id.activity_avgSpeed);

        FloatingActionButton fabtrash = findViewById(R.id.fabtrash);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);


        mActivity = (ActivityComplete) getIntent().getExtras().getSerializable(ACTIVITY);


        if (mActivity == null){
            Toast.makeText(this,R.string.rutanoencontrada,Toast.LENGTH_LONG).show();
            finish();
        }

        double avgSpeedSum = 0;
        double avgSpeedCount = 0;
        double avgSpeed = 0;
        for(ActivityLocation activityLocation: mActivity.locations){
            if (activityLocation.getSpeed() <= 0)
                continue;

            if (maxSpeed == null){
                maxSpeed = activityLocation;
                minSpeed = activityLocation;
            }else{
                if (maxSpeed.getSpeed() < activityLocation.getSpeed()){
                    maxSpeed = activityLocation;
                }else if (activityLocation.getSpeed() > 0 && minSpeed.getSpeed() > activityLocation.getSpeed()){
                    minSpeed = activityLocation;
                }
            }

            avgSpeedCount++;
            avgSpeedSum += activityLocation.getSpeed();
        }

        avgSpeed = avgSpeedSum/avgSpeedCount;

        if (minSpeed != null)
            activityMinSpeed.setText(Converters.speedToString(minSpeed.getSpeed()));
        if (maxSpeed != null)
            activityMaxSpeed.setText(Converters.speedToString(maxSpeed.getSpeed()));
        if (avgSpeed > 0)
            activityAvgSpeed.setText(Converters.speedToString(avgSpeed));

        if (mMap != null){
            MapUtils.drawPoint(Converters.activityLocationToLatLng(maxSpeed),mMap,getResources().getColor(R.color.bluedefault));
            MapUtils.drawPoint(Converters.activityLocationToLatLng(minSpeed),mMap,getResources().getColor(R.color.blueaccent));
        }

        fabtrash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(ActivityDetailsActivity.this)
                        .setIcon(R.drawable.ic_delete_black_24dp)
                        .setTitle(R.string.borraractividad)
                        .setMessage(R.string.confirmarborraractividad)
                        .setPositiveButton(R.string.si, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {


                                new AsyncTask() {
                                    @Override
                                    protected Object doInBackground(Object[] objects) {
                                        GpsTrackDB.getDatabase(ActivityDetailsActivity.this).activityDao()
                                                .delete(mActivity.activity);
                                        return null;
                                    }

                                    @Override
                                    protected void onPostExecute(Object o) {
                                        Toast.makeText(ActivityDetailsActivity.this, getResources().getString(R.string.actividadborrada), Toast.LENGTH_LONG).show();
                                        finish();
                                        super.onPostExecute(o);
                                    }
                                }.execute();


                            }

                        })
                        .setNegativeButton(R.string.no, null)
                        .show();
            }
        });

        mRoute = mActivity.route.get(0);


        activityDate.setText(Converters.dateToString(mActivity.activity.getAdddate()));
        activityName.setText(mRoute.getName());
        activityDistance.setText(Converters.distanceToString(mActivity.activity.getDistance())+" km");
        if (mActivity.activity.getTime() == 0){
            activityTime.setText("No registrado");
        }else{
            activityTime.setText(Converters.timeToString(mActivity.activity.getTime()));
        }



        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
                MapUtils.configMap(mMap,false, ActivityDetailsActivity.this);
                MapUtils.drawPrimaryLinePath(Converters.stringToLatLngs(mRoute.getTracks()),mMap,getResources().getColor(R.color.bluedefault), ActivityDetailsActivity.this);
                MapUtils.drawPrimaryLinePath(Converters.activityLocationsToLatLngs(mActivity.locations),mMap,getResources().getColor(R.color.blueaccent),ActivityDetailsActivity.this);

                if (maxSpeed != null && minSpeed != null){
                    MapUtils.drawPoint(Converters.activityLocationToLatLng(maxSpeed),mMap,getResources().getColor(R.color.bluedefault));
                    MapUtils.drawPoint(Converters.activityLocationToLatLng(minSpeed),mMap,getResources().getColor(R.color.blueaccent));
                }
            }
        });




    }

}
