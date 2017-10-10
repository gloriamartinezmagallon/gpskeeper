package navdev.gpstrack;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.Activity;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import java.util.ArrayList;

import navdev.gpstrack.dao.GpsBBDD;
import navdev.gpstrack.ent.ActivityLocation;
import navdev.gpstrack.ent.Route;
import navdev.gpstrack.utils.MapUtils;

public class ActivityDetailsActivity extends AppCompatActivity {

    navdev.gpstrack.ent.Activity mActivity;
    Route mRoute;
    ArrayList<ActivityLocation> mActivityLocations;

    GoogleMap mMap;

    public static String ID_ACTIVITY ="idActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        TextView activityDate = (TextView) findViewById(R.id.activiy_date);
        TextView activityName = (TextView) findViewById(R.id.activity_name);
        TextView activityDistance= (TextView) findViewById(R.id.activity_distance);
        TextView activityTime= (TextView) findViewById(R.id.activity_time);

        FloatingActionButton fabtrash = (FloatingActionButton) findViewById(R.id.fabtrash);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);


        int idActivity = getIntent().getExtras().getInt(ID_ACTIVITY);

        GpsBBDD gpsBBDD = new GpsBBDD(this);
        mActivity = gpsBBDD.getActivityById(idActivity);

        if (mActivity == null){
            Toast.makeText(this,R.string.rutanoencontrada,Toast.LENGTH_LONG).show();
            gpsBBDD.closeDDBB();
            finish();
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
                                GpsBBDD gpsBBDD = new GpsBBDD(ActivityDetailsActivity.this);

                                gpsBBDD.deleteActivity(mActivity.getId());

                                Toast.makeText(ActivityDetailsActivity.this, getResources().getString(R.string.actividadborrada), Toast.LENGTH_LONG).show();
                                gpsBBDD.closeDDBB();
                                finish();
                            }

                        })
                        .setNegativeButton(R.string.no, null)
                        .show();
            }
        });

        mRoute = mActivity.getRoute(this);


        activityDate.setText(mActivity.getAdddatetoformat());
        activityName.setText(mRoute.getName());
        activityDistance.setText(mActivity.getDistanceKm());
        activityTime.setText(mActivity.timeTostring());



        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
                MapUtils.configMap(mMap,false);
                MapUtils.drawPrimaryLinePath(mRoute.getTracksLatLng(),mMap,getResources().getColor(R.color.bluedefault));
                MapUtils.drawPrimaryLinePath(mActivity.getLocations(ActivityDetailsActivity.this),mMap,getResources().getColor(R.color.colorAccent));
            }
        });




    }

}
