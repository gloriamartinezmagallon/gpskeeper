package navdev.gpstrack;


import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import navdev.gpstrack.db.Converters;
import navdev.gpstrack.db.GpsTrackDB;
import navdev.gpstrack.db.ActivityDao;
import navdev.gpstrack.db.RouteDao;
import navdev.gpstrack.db.Route;
import navdev.gpstrack.gpsutils.utils.Formatter;
import navdev.gpstrack.service.TrackerService;
import navdev.gpstrack.utils.MapUtils;
import navdev.gpstrack.utils.PausableChronometer;
import navdev.gpstrack.utils.PermissionUtils;

import static navdev.gpstrack.service.TrackerService.ASK_IS_RUNNING;
import static navdev.gpstrack.service.TrackerService.LAST_DISTANCE;
import static navdev.gpstrack.service.TrackerService.LAST_LOCATION;
import static navdev.gpstrack.service.TrackerService.SEND_IN_PAUSE;
import static navdev.gpstrack.service.TrackerService.SEND_IS_RUNNING;
import static navdev.gpstrack.service.TrackerService.SEND_LAST_LOCATION;

public class RunActivity extends AppCompatActivity implements  GoogleMap.OnMyLocationButtonClickListener,
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback {
    Intent serviceIntent;
    boolean isSvcRunning = false;
    double distance;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private boolean mPermissionDenied = false;

    private GoogleMap mMap;
    Route mRoute;

    TextView mDistanceTV;
    PausableChronometer mTimeWalkingTV;
    LinearLayout mfueraRutaMsgLV;

    long mActivityId;

    Formatter formatter = null;

    Button mStartBtn;

    int numavisos = 0;
    boolean inPause = false;
    float currentZoom = 17f;

    RouteDao mRouteDao;
    ActivityDao mActivityDao;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_run);

        formatter = new Formatter(this);

        if (!getIntent().getExtras().containsKey(RoutedetailsActivity.ROUTE)
        || !getIntent().getExtras().containsKey(RoutedetailsActivity.ID_ACTIVITY)){
            finish();
        }
        mRoute = (Route) getIntent().getExtras().getSerializable(RoutedetailsActivity.ROUTE);

        mActivityId = getIntent().getExtras().getLong(RoutedetailsActivity.ID_ACTIVITY);

        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(getApplicationContext());
        manager.registerReceiver(receiverTrackerServiceInfo, new IntentFilter(SEND_IS_RUNNING));
        manager.registerReceiver(receiverTrackerServiceInfo, new IntentFilter(SEND_LAST_LOCATION));
        manager.registerReceiver(receiverTrackerServiceInfo, new IntentFilter(SEND_IN_PAUSE));
        manager.sendBroadcast(new Intent(ASK_IS_RUNNING));

        mRouteDao = GpsTrackDB.getDatabase(this).routeDao();
        mActivityDao = GpsTrackDB.getDatabase(this).activityDao();

        if (mRoute == null){
            Toast.makeText(RunActivity.this,R.string.rutanoencontrada,Toast.LENGTH_LONG).show();
            finish();
            return;
        }


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(RunActivity.this);





        mTimeWalkingTV = findViewById(R.id.timeWalking);
        mDistanceTV = findViewById(R.id.distance);
        mfueraRutaMsgLV = findViewById(R.id.fueraRutaMsg);

        mStartBtn = findViewById(R.id.startActivityBtn);

        mStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopActivity();
            }
        });

        if (!isSvcRunning){

            if (BuildConfig.DEBUG){
                new AlertDialog.Builder(this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Activar simulación")
                        .setMessage("¿Seguro?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                serviceIntent = new Intent(RunActivity.this, TrackerService.class);
                                serviceIntent.putExtra(RoutedetailsActivity.ID_ACTIVITY, mActivityId);
                                serviceIntent.putExtra(RoutedetailsActivity.ACTIVAR_SIMULACION, true);
                                startService(serviceIntent);
                            }

                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                serviceIntent = new Intent(RunActivity.this, TrackerService.class);
                                serviceIntent.putExtra(RoutedetailsActivity.ID_ACTIVITY, mActivityId);
                                serviceIntent.putExtra(RoutedetailsActivity.ACTIVAR_SIMULACION, false);
                                startService(serviceIntent);
                            }
                        })
                        .show();
            }


        }
        mTimeWalkingTV.start();
    }


    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiverTrackerServiceInfo);

    }


    private void stopActivity(){
        new AlertDialog.Builder(this)
                .setTitle(R.string.app_name)
                .setMessage(R.string.confirmterminaruta)
                .setPositiveButton(R.string.si, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        cerrarActividad();
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

    private void cerrarActividad(){

        mTimeWalkingTV.stop();
        stopService(serviceIntent);

        Intent intent = new Intent(getApplicationContext(), ActivitiesActivity.class);
        startActivity(intent);
        finish();
    }


    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;


        enableMyLocation();
        MapUtils.configMap(mMap,true,this);

        MapUtils.drawPrimaryLinePathToRun(Converters.stringToLatLngs(mRoute.getTracks()),mMap,getResources().getColor(R.color.blueaccent), RunActivity.this);


        mMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                currentZoom = mMap.getCameraPosition().zoom;
            }
        });
    }

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
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.global, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_settings:
                showSettings();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showSettings(){
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        View layout = inflater.inflate(R.layout.fragment_configuration, null);

        final EditText editTextDistancia = layout.findViewById(R.id.editTextDistancia);
        final EditText editTextAvisos = layout.findViewById(R.id.editTextAvisos);

        editTextDistancia.setText("" + getValueInPreference("UMBRALDISTANCIA",10));
        editTextAvisos.setText(""+ getValueInPreference("NUMAVISOS",4));


        //Building dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.config_fragment);
        builder.setView(layout);
        builder.setPositiveButton(R.string.guardar, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                int umbraldistancia = Integer.parseInt(editTextDistancia.getText().toString());
                int numavisos = Integer.parseInt(editTextAvisos.getText().toString());

                setValueInPreference("UMBRALDISTANCIA",umbraldistancia);
                setValueInPreference("NUMAVISOS",numavisos);

                Toast.makeText(RunActivity.this,R.string.guardarok,Toast.LENGTH_LONG).show();
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void setValueInPreference(String key, int newHighScore){
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(key, newHighScore);
        editor.apply();
    }

    public int getValueInPreference(String key, int defaultValue){
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        return sharedPref.getInt(key, defaultValue);
    }


    private boolean mIsBound = false;


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

    }


    Location currentLocation = null;


    private void checkIfInRoute(){
        double distancia = this.mRoute.getDistanceto(currentLocation);

        if (distancia > getValueInPreference("UMBRALDISTANCIA",10)){
            /*if (numavisos <=  getValueInPreference("NUMAVISOS",4)){
                ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(1000);
            }*/
            mfueraRutaMsgLV.setVisibility(View.VISIBLE);
        }else{
            numavisos = 0;
            mfueraRutaMsgLV.setVisibility(View.GONE);
        }

    }




    @Override
    public void onBackPressed() {
        stopActivity();
    }

    protected BroadcastReceiver receiverTrackerServiceInfo = new BroadcastReceiver()
    {
        @Override
        public void onReceive (Context context, Intent intent)
        {
            if (intent.getAction().equals(SEND_IS_RUNNING)){
                isSvcRunning = true;
            }else if (intent.getAction().equals(SEND_LAST_LOCATION)){
                if (inPause){
                    inPause = false;
                    mTimeWalkingTV.resume();
                }
                currentLocation = (Location) intent.getExtras().get(LAST_LOCATION);
                mMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),
                        currentZoom, 0, currentLocation.getBearing())));

                distance = (double) intent.getExtras().get(LAST_DISTANCE);
                mDistanceTV.setText(formatter.formatDistance(Formatter.Format.TXT_SHORT, Math.round(distance)));

                checkIfInRoute();
            }else if (intent.getAction().equals(SEND_IN_PAUSE)){
                inPause = true;
                mTimeWalkingTV.pause();
            }
        }
    };
}

