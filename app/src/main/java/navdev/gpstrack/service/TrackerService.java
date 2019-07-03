package navdev.gpstrack.service;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Date;

import navdev.gpstrack.BuildConfig;
import navdev.gpstrack.R;
import navdev.gpstrack.RoutedetailsActivity;
import navdev.gpstrack.RunActivity;
import navdev.gpstrack.db.ActivityComplete;
import navdev.gpstrack.db.Converters;
import navdev.gpstrack.db.GpsTrackDB;
import navdev.gpstrack.db.ActivityDao;
import navdev.gpstrack.db.Activity;
import navdev.gpstrack.db.ActivityLocation;
import navdev.gpstrack.db.Route;
import navdev.gpstrack.utils.MapUtils;

public class TrackerService extends Service {

    private static final String TAG = TrackerService.class.getSimpleName();


    public static final String ASK_IS_RUNNING = "AskIsRunning";

    public static final String SEND_IS_RUNNING = "sendIsRunning";
    public static final String SEND_LAST_LOCATION = "sendLastLocation";
    public static final String LAST_LOCATION = "lastLocation";
    public static final String LAST_DISTANCE = "lastDistance";
    public static final String SEND_IN_PAUSE = "sendInPause";

    private Location lastLocation;
    private boolean inPause = false;
    private double distance = 0;
    private int time = 0;
    private int mActivityId;

    private boolean activarSimulacion = false;

    private ActivityDao mActivityDao;
    private Activity mActivity;

    @Override
    public IBinder onBind(Intent intent) {return null;}

    private BroadcastReceiver receiverAskIsRunning = new BroadcastReceiver(){
        public void onReceive (Context context, Intent intent) {
            LocalBroadcastManager manager = LocalBroadcastManager.getInstance(getApplicationContext());
            manager.sendBroadcast(new Intent(SEND_IS_RUNNING));
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.O)
    private Notification getNotification() {
        NotificationChannel channel = new NotificationChannel("gpskeeper_01",
                getString(R.string.app_name),
                NotificationManager.IMPORTANCE_DEFAULT
        );
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
        Notification.Builder builder = new Notification.Builder(getApplicationContext(), "gpskeeper_01");
        builder.setSmallIcon(R.mipmap.ic_notification);
        return builder.build();
    }

    private void buildNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(1, getNotification());
        }
    }

    protected BroadcastReceiver stopReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "received stop broadcast");
            // Stop the service when the notification is tapped
            unregisterReceiver(stopReceiver);
            stopForeground(true);
            stopSelf();
        }
    };


    private void requestLocationUpdates() {
        LocationRequest request = new LocationRequest();
        request.setInterval(10*1000); //10 segundos
        request.setFastestInterval(5*1000); //5 segundos
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(this);

        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (permission == PackageManager.PERMISSION_GRANTED) {
            client.requestLocationUpdates(request, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    final Location location = locationResult.getLastLocation();
                    checkLocation(location);
                }
            }, null);
        }
    }

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiverAskIsRunning);
        super.onDestroy();
    }

    private void checkLocation(final Location location){
        if (location != null) {
            if (lastLocation != null && lastLocation.getLatitude() == location.getLatitude() &&
                    lastLocation.getLongitude() == location.getLongitude()){
                Log.d(TAG, "location NO update " + location);
                if (!inPause){
                    Intent intent = new Intent(SEND_IN_PAUSE);
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                    inPause = true;
                }
            }else{
                if (inPause){
                    inPause = false;
                }
                Log.d(TAG, "location update " + location);
                if (lastLocation != null) {
                    distance += MapUtils.distanceBetween(location, lastLocation);
                    time += (location.getTime() - lastLocation.getTime()) / 1000;
                }
                Intent intent = new Intent(SEND_LAST_LOCATION);
                intent.putExtra(LAST_LOCATION,location);
                intent.putExtra(LAST_DISTANCE,distance);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

                mActivity.setDistance((int) Math.round(distance));
                mActivity.setTime(time);

                new AsyncTask() {
                    @Override
                    protected Object doInBackground(Object[] objects) {
                        mActivityDao.update(mActivity);
                        return null;
                    }
                }.execute();

                lastLocation = location;
            }

            new AsyncTask() {
                @Override
                protected Object doInBackground(Object[] objects) {
                    mActivityDao.insertLocation(new ActivityLocation(mActivityId,
                            location.getLatitude(),
                            location.getLongitude(),
                            location.getAltitude(),
                            (double) location.getSpeed(),
                            new Date(location.getTime())));
                    return null;
                }
            }.execute();



        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent.getExtras().containsKey(RoutedetailsActivity.ID_ACTIVITY)){
            mActivityId = (int) intent.getLongExtra(RoutedetailsActivity.ID_ACTIVITY,0l);
        }

        if (intent.getExtras().containsKey(RoutedetailsActivity.ACTIVAR_SIMULACION)){
            activarSimulacion = intent.getBooleanExtra(RoutedetailsActivity.ACTIVAR_SIMULACION,false);
        }


        mActivityDao = GpsTrackDB.getDatabase(this).activityDao();
        new AsyncTask() {

            ActivityComplete activityComplete;
            @Override
            protected Object doInBackground(Object[] objects) {
                activityComplete = mActivityDao.findActivyById(mActivityId);
                mActivity = activityComplete.activity;
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {

                buildNotification();

                if (BuildConfig.DEBUG && activarSimulacion) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            simulateLocationUpdates(activityComplete.route.get(0));
                        }
                    }).start();

                }else{
                    requestLocationUpdates();
                }

                LocalBroadcastManager.getInstance(TrackerService.this).registerReceiver(receiverAskIsRunning, new IntentFilter(ASK_IS_RUNNING));
                super.onPostExecute(o);
            }
        }.execute();


        //LocalBroadcastManager.getInstance(this).sendBroadcastSync(new Intent(SEND_IS_RUNNING));
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    private void simulateLocationUpdates(Route route){

        ArrayList<LatLng> latLngs = Converters.stringToLatLngs(route.getTracks());
        ArrayList<Double> alts = Converters.stringToAlts(route.getTracks());

        for (int i = 0; i < latLngs.size(); i++){
            try{
                Thread.sleep(1000*3);

                Location location = new Location("");
                location.setLatitude(latLngs.get(i).latitude);
                location.setLongitude(latLngs.get(i).longitude);
                location.setAltitude(alts.get(i));
                location.setTime(new Date().getTime());
                location.setSpeed(25);

                checkLocation(location);

            }catch (Exception e){e.printStackTrace();}

        }
    }
}
