package navdev.gpstrack;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import navdev.gpstrack.dao.GpsBBDD;
import navdev.gpstrack.ent.Route;
import navdev.gpstrack.gpsutils.tracker.Tracker;
import navdev.gpstrack.gpsutils.tracker.workout.Scope;
import navdev.gpstrack.gpsutils.tracker.workout.Workout;
import navdev.gpstrack.gpsutils.utils.Formatter;
import navdev.gpstrack.gpsutils.utils.TickListener;
import navdev.gpstrack.utils.MapUtils;
import navdev.gpstrack.utils.PermissionUtils;

import static navdev.gpstrack.R.id.startActivityBtn;

public class RunActivity extends AppCompatActivity implements  GoogleMap.OnMyLocationButtonClickListener,
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback, TickListener {
    Workout workout = null;
    Tracker mTracker = null;
    final Handler handler = new Handler();

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private boolean mPermissionDenied = false;

    private GoogleMap mMap;

    Route mRoute;


    TextView mDistanceTV;
    TextView mTimeWalkingTV;

    long mActivityId;


    Formatter formatter = null;

    Button mStartBtn;

    int numavisos = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run);


        formatter = new Formatter(this);

        int idRoute  = getIntent().getExtras().getInt(RoutedetailsActivity.ID_ROUTE);

        mActivityId = getIntent().getExtras().getLong(RoutedetailsActivity.ID_ACTIVITY);


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

        mStartBtn = (Button)findViewById(startActivityBtn);

        mStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopActivity();
            }
        });

        bindGpsTracker();


    }


    private final ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service. Because we have bound to a explicit
            // service that we know is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            if (mTracker == null) {
                mTracker = ((Tracker.LocalBinder) service).getService();
                // Tell the user about this for our demo.
                RunActivity.this.onGpsTrackerBound();
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            // Because it is running in our same process, we should never
            // see this happen.
            mIsBound = false;
            mTracker = null;
        }
    };

    void bindGpsTracker() {
        // Establish a connection with the service. We use an explicit
        // class name because we want a specific service implementation that
        // we know will be running in our own process (and thus won't be
        // supporting component replacement by other applications).
        getApplicationContext().bindService(new Intent(this, Tracker.class),
                mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    void unbindGpsTracker() {
        if (mIsBound) {
            // Detach our existing connection.
            getApplicationContext().unbindService(mConnection);
            mIsBound = false;
        }
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
        unbindGpsTracker();
        stopTimer();

    }

    void onGpsTrackerBound() {
        if (mTracker == null) {
            // should not happen
            return;
        }

        if (mTracker.getWorkout() == null) {
            // should not happen
            return;
        }

        workout = mTracker.getWorkout();

        {
            /**
             * Countdown view can't be bound until RunActivity is started
             *   since it's not created until then
             */
            HashMap<String, Object> bindValues = new HashMap<>();
            workout.onBind(workout, bindValues);
        }

        startTimer();
    }


    private void stopActivity(){
        new AlertDialog.Builder(this)
                .setTitle(R.string.app_name)
                .setMessage(R.string.confirmterminaruta)
                .setPositiveButton(R.string.si, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int tiempotext = (int)mTracker.getTime();
                        String distance = mTracker.getDistance()+"";

                        if (timer != null) {
                            workout.onStop(workout);
                            stopTimer(); // set timer=null;

                        }
                        GpsBBDD gpsBBDD = new GpsBBDD(RunActivity.this);
                        gpsBBDD.updateActivity(mActivityId, distance, tiempotext);
                        ;

                        Intent intent = new Intent(getApplicationContext(), ActivitiesActivity.class);
                        startActivity(intent);
                        finish();
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


    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;


        enableMyLocation();
        MapUtils.configMap(mMap,true);

        MapUtils.drawPrimaryLinePath(mRoute.getTracksLatLng(),mMap,getResources().getColor(R.color.bluedefault));
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
        editor.commit();
    }

    public int getValueInPreference(String key, int defaultValue){
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        return sharedPref.getInt(key, defaultValue);
    }


    private boolean mIsBound = false;


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (workout == null) {
            // "should not happen"
            finish();
            return;
        }
        if (resultCode == Activity.RESULT_OK) {
            /**
             * they saved
             */
            workout.onComplete(Scope.ACTIVITY, workout);
            workout.onSave();
            mTracker = null;
            finish();
            return;
        } else if (resultCode == Activity.RESULT_CANCELED) {
            /**
             * they discarded
             */
            workout.onComplete(Scope.ACTIVITY, workout);
            workout.onDiscard();
            mTracker = null;
            finish();
            return;
        } else if (resultCode == Activity.RESULT_FIRST_USER) {
            startTimer();
            if (requestCode == 0) {
                workout.onResume(workout);
            } else {
                // we were paused before stopButtonClick...don't resume
            }
        } else {
            if (BuildConfig.DEBUG) { throw new AssertionError(); }
        }
    }

    Timer timer = null;

    void startTimer() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                RunActivity.this.handler.post(new Runnable() {
                    public void run() {
                        RunActivity.this.onTick();
                    }
                });
            }
        }, 0, 500);
    }

    void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }
    }

    Location currentLocation = null;

    public void onTick() {
        if (workout != null) {
            workout.onTick();
            updateView();

            if (mTracker != null) {
                Location l2 = mTracker.getLastKnownLocation();
                if (l2 != null && !l2.equals(currentLocation)) {
                    currentLocation = l2;
                    checkIfInRoute();
                }
            }
        }
    }

    private void checkIfInRoute(){
        double distancia = this.mRoute.getDistanceto(currentLocation);

        if (distancia > getValueInPreference("UMBRALDISTANCIA",10)){
            if (numavisos <=  getValueInPreference("NUMAVISOS",4)){
                ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(1000);
            }
        }else{
            numavisos = 0;
        }

    }

    private void updateView() {

        double ad = workout.getDistance(Scope.ACTIVITY);
        double at = workout.getTime(Scope.ACTIVITY);

        mTimeWalkingTV.setText(formatter.formatElapsedTime(Formatter.Format.TXT_LONG, Math.round(at)));
        mDistanceTV.setText(formatter.formatDistance(Formatter.Format.TXT_SHORT, Math.round(ad)));


    }

    @Override
    public void onBackPressed() {
        stopActivity();
    }
}

