package navdev.gpstrack;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import java.util.Date;
import java.util.Locale;

import navdev.gpstrack.db.Converters;
import navdev.gpstrack.db.GpsTrackDB;
import navdev.gpstrack.db.ActivityDao;
import navdev.gpstrack.db.RouteDao;
import navdev.gpstrack.db.Activity;
import navdev.gpstrack.db.Route;
import navdev.gpstrack.gpsutils.tracker.GpsInformation;
import navdev.gpstrack.gpsutils.tracker.GpsStatus;
import navdev.gpstrack.gpsutils.tracker.Tracker;
import navdev.gpstrack.gpsutils.tracker.component.TrackerState;
import navdev.gpstrack.gpsutils.tracker.workout.Dimension;
import navdev.gpstrack.gpsutils.tracker.workout.Workout;
import navdev.gpstrack.gpsutils.tracker.workout.WorkoutBuilder;
import navdev.gpstrack.gpsutils.utils.Constants;
import navdev.gpstrack.gpsutils.utils.TickListener;
import navdev.gpstrack.utils.MapUtils;
import navdev.gpstrack.utils.PermissionUtils;

import static navdev.gpstrack.R.id.startActivityBtn;

public class InitActivity extends AppCompatActivity  implements  GoogleMap.OnMyLocationButtonClickListener,
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback, TickListener, GpsInformation {


    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private boolean mPermissionDenied = false;

    private GoogleMap mMap;

    Route mRoute;

    long mActivityId;

    boolean skipStopGps = false;
    GpsStatus mGpsStatus = null;
    Tracker mTracker = null;

    Button mStartBtn;

    RouteDao mRouteDao;
    ActivityDao mActivityDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init);

        mRoute  = (Route) getIntent().getExtras().getSerializable(RoutedetailsActivity.ROUTE);


        if (mRoute == null) {
            Toast.makeText(InitActivity.this, R.string.rutanoencontrada, Toast.LENGTH_LONG).show();
            finish();
        }

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        mStartBtn = (Button)findViewById(startActivityBtn);
        mStartBtn.setOnClickListener(startButtonClick);


        mGpsStatus = new GpsStatus(this);

    }

    final View.OnClickListener startButtonClick = new View.OnClickListener() {
        public void onClick(View v) {
            if (mGpsStatus.isEnabled() == false) {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            } else {

                new AsyncTask() {
                    @Override
                    protected Object doInBackground(Object[] objects) {
                        Activity activity = new Activity(mRoute.getId(),0,0,new Date());
                        mActivityId = GpsTrackDB.getDatabase(InitActivity.this).activityDao().insert(activity);

                        return null;
                    }

                    @Override
                    protected void onPostExecute(Object o) {

                        Intent intent = new Intent(InitActivity.this,
                                RunActivity.class);

                        intent.putExtra(RoutedetailsActivity.ROUTE, mRoute);
                        intent.putExtra(RoutedetailsActivity.ID_ACTIVITY, mActivityId);
                        InitActivity.this.startActivityForResult(intent, 112);
                        finish();
                    }
                }.execute();
                return;
            }
            updateView();
        }
    };

    Workout prepareWorkout() {
        Context ctx = getApplicationContext();
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences audioPref = null;
        Workout w = null;
        Dimension target = Dimension.valueOf(Constants.DIMENSION.DISTANCE);
        w = WorkoutBuilder.createDefaultWorkout(getResources(), pref, target);
        w.setWorkoutType(Constants.WORKOUT_TYPE.BASIC);
        WorkoutBuilder.prepareWorkout(getResources(), pref, w);
        return w;
    }

    private void unregisterStartEventListener() {
        try {
            unregisterReceiver(startEventBroadcastReceiver);
        } catch (Exception e) {
        }

    }

    private final BroadcastReceiver startEventBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mTracker == null)
                        return;

                    if (!mStartBtn.isEnabled())
                        return;

                    if (mTracker.getState() == TrackerState.INIT /* this will start gps */||
                            mTracker.getState() == TrackerState.INITIALIZED /* ...start a workout*/ ||
                            mTracker.getState() == TrackerState.CONNECTED) {
                        mStartBtn.performClick();
                    }
                }
            });
        }
    };



    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;


        enableMyLocation();
        MapUtils.configMap(mMap,true,this);

        MapUtils.drawPrimaryLinePath(Converters.stringToLatLngs(mRoute.getTracks()),mMap,getResources().getColor(R.color.bluedefault),InitActivity.this);
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

    public String timeTostring(Long tiempo){

        int hours = (int) ((tiempo / 1000) / 3600);
        int minutes = (int) (((tiempo / 1000) / 60) % 60);
        int seconds = (int) ((tiempo / 1000) % 60);


        String  txtseconds,txminutes, txhours;

        if (seconds<10) txtseconds="0"+seconds;
        else txtseconds=seconds+"";

        if (minutes<10) txminutes="0"+minutes;
        else txminutes=minutes+"";

        if (hours<10) txhours="0"+hours;
        else txhours=hours+"";

        return txhours+":"+txminutes+":"+txtseconds;
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
        View layout = inflater.inflate(R.layout.fragment_configuration, null);

        final EditText editTextDistancia = (EditText) layout.findViewById(R.id.editTextDistancia);
        final EditText editTextAvisos = (EditText) layout.findViewById(R.id.editTextAvisos);

        editTextDistancia.setText("" + getValueInPreference("UMBRALDISTANCIA", getValueInPreference("UMBRALDISTANCIA",10)));
        editTextAvisos.setText(""+ getValueInPreference("NUMAVISOS", getValueInPreference("NUMAVISOS",4)));


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

                Toast.makeText(InitActivity.this,R.string.guardarok,Toast.LENGTH_LONG).show();
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
        editor.commit();
    }

    public int getValueInPreference(String key, int defaultValue){
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        return sharedPref.getInt(key, defaultValue);
    }


    private boolean mIsBound = false;
    private final ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service. Because we have bound to a explicit
            // service that we know is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            mTracker = ((Tracker.LocalBinder) service).getService();

            // Tell the user about this for our demo.
            InitActivity.this.onGpsTrackerBound();
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            // Because it is running in our same process, we should never
            // see this happen.
            mTracker = null;
        }
    };

    void onGpsTrackerBound() {
        startGps();
        updateView();
    }


    private void startGps() {
        Log.e(getClass().getName(), "InitActivity.startGps()");
        if (mGpsStatus != null && !mGpsStatus.isLogging())
            mGpsStatus.start(this);

    }

    @Override
    public String getGpsAccuracy() {
        String s = "";
        if (mTracker != null) {
            Location l = mTracker.getLastKnownLocation();

            if (l != null && l.getAccuracy() > 0) {
                s = String.format(Locale.getDefault(), ", %s m", l.getAccuracy());
            }
            if (mTracker.getCurrentElevation() != null) {
                s += String.format(Locale.getDefault(), " (%.1f m)", mTracker.getCurrentElevation());
            }
        }

        return s;
    }

    @Override
    public int getSatellitesAvailable()  {
        return mGpsStatus.getSatellitesAvailable();
    }

    @Override
    public int getSatellitesFixed() {
        return mGpsStatus.getSatellitesFixed();
    }

    @Override
    public void onTick() {
        updateView();
    }

    private void updateView(){}


    @Override
    public void onStart() {
        super.onStart();
        registerStartEventListener();
    }

    private void registerStartEventListener() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.Intents.START_WORKOUT);
        registerReceiver(startEventBroadcastReceiver, intentFilter);

    }

    @Override
    public void onResume() {
        super.onResume();

        onGpsTrackerBound();
        this.updateView();
    }

    @Override
    public void onPause() {
        super.onPause();

        stopGps();
    }

    @Override
    public void onStop() {
        super.onStop();
        unregisterStartEventListener();
    }

    private void stopGps() {
        Log.e(getClass().getName(), "StartActivity.stopGps() skipStop: " + this.skipStopGps);
        if (skipStopGps == true)
            return;

        if (mGpsStatus != null)
            mGpsStatus.stop(this);

        if (mTracker != null)
            mTracker.reset();

        //notificationStateManager.cancelNotification();
    }

    void unbindGpsTracker() {
        if (mIsBound) {
            // Detach our existing connection.
            getApplicationContext().unbindService(mConnection);
            mIsBound = false;
        }
    }

    @Override
    public void onDestroy() {
        stopGps();
        unbindGpsTracker();
        mGpsStatus = null;
        mTracker = null;

        super.onDestroy();
    }

    @Override
    public void onBackPressed () {
        if (mGpsStatus.isLogging()) {
            stopGps();
            updateView();
        } else {
            super.onBackPressed();
        }
    }
}
