package navdev.gpstrack;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import java.util.Date;

import navdev.gpstrack.dao.GpsBBDD;
import navdev.gpstrack.ent.Route;
import navdev.gpstrack.utils.ActivityService;
import navdev.gpstrack.utils.ActivityServiceReceiver;
import navdev.gpstrack.utils.BroadcastNotifier;
import navdev.gpstrack.utils.MapUtils;
import navdev.gpstrack.utils.PermissionUtils;

public class InitActivity extends AppCompatActivity  implements  GoogleMap.OnMyLocationButtonClickListener,
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback {


    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private boolean mPermissionDenied = false;

    private GoogleMap mMap;

    Route mRoute;

    Intent mActivityServiceIntent;

    Boolean mIsInPause;
    long mTimeWalking;
    double mDistance;

    TextView mDistanceTV;
    TextView mTimeWalkingTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init);

        int idRoute = getIntent().getExtras().getInt(RoutedetailsActivity.ID_ROUTE);

        GpsBBDD gpsBBDD = new GpsBBDD(this);
        mRoute = gpsBBDD.getRouteById(idRoute);
        gpsBBDD.closeDDBB();

        if (mRoute == null){
            Toast.makeText(this,R.string.rutanoencontrada,Toast.LENGTH_LONG).show();
            finish();
        }

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mTimeWalkingTV = (TextView) findViewById(R.id.timeWalking);
        mDistanceTV = (TextView) findViewById(R.id.distance);

        final Button startActivityBtn = (Button)findViewById(R.id.startActivityBtn);
        startActivityBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mActivityServiceIntent == null) {
                    startActivity();
                    startActivityBtn.setText(R.string.stop);
                }else {
                    stopActivity();
                }
            }
        });
    }

    private void stopActivity(){
        new AlertDialog.Builder(this)
                .setIcon(R.mipmap.ic_launcher)
                .setTitle(R.string.app_name)
                .setMessage(R.string.confirmterminaruta)
                .setPositiveButton(R.string.si, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                       Integer route = (mRoute.getId());
                        String tiempotext = ( mTimeWalking+ "");
                        String distance = (mDistance + "");

                        GpsBBDD gpsBBDD = new GpsBBDD(InitActivity.this);
                        gpsBBDD.insertActivity(route, distance, Integer.parseInt(tiempotext), new Date());

                       finish();
                        stopService(mActivityServiceIntent);
                        finish();

                        // ((MainActivity) getActivity()).irActividades();

                    }

                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void startActivity(){

        mActivityServiceIntent = new Intent(this,ActivityService.class);

        // The filter's action is BROADCAST_ACTION
        IntentFilter statusIntentFilter = new IntentFilter(
                BroadcastNotifier.BROADCAST_ACTION);

        // Sets the filter's category to DEFAULT
        statusIntentFilter.addCategory(Intent.CATEGORY_DEFAULT);

        ActivityServiceReceiver activityServiceReceiver = new ActivityServiceReceiver();
        activityServiceReceiver.setOnDataSendStateChange(new ActivityServiceReceiver.OnDataSendStateChange() {
            @Override
            public void run(Boolean inPause, long time, double distance) {
                mTimeWalkingTV.setText(timeTostring(time));
                mDistanceTV.setText(String.format("%.1f km", distance/1000));
                if (inPause){
                    mDistanceTV.setTextColor(getResources().getColor(R.color.bluedefault));
                }else{
                    mDistanceTV.setTextColor(getResources().getColor(R.color.blueaccent));
                }
            }
        });

        LocalBroadcastManager.getInstance(this).registerReceiver(
                activityServiceReceiver,
                statusIntentFilter);

        startService(mActivityServiceIntent);

    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;


        enableMyLocation();
        MapUtils.configMap(mMap,true);

        MapUtils.drawPrimaryLinePath(mRoute.getTracksLatLng(),mMap,getResources().getColor(R.color.bluedefault));
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,android.Manifest.permission.ACCESS_FINE_LOCATION,getString(R.string.permission_rationale_location)
                    , true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mActivityServiceIntent != null){
            stopService(mActivityServiceIntent);
            mActivityServiceIntent = null;
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }

    public String timeTostring(Long tiempo){
        System.out.println(tiempo);

        int hours = (int) ((tiempo / 1000) / 3600);
        int minutes = (int) (((tiempo / 1000) / 60) % 60);
        int seconds = (int) ((tiempo / 1000) % 60);


        String  txminutes, txhours;

        if (minutes<10) txminutes="0"+minutes;
        else txminutes=minutes+"";

        if (hours<10) txhours="0"+hours;
        else txhours=hours+"";

        return txhours+":"+txminutes;
    }
}
