package navdev.gpstrack.service;

import android.Manifest;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import navdev.gpstrack.R;
import navdev.gpstrack.RoutedetailsActivity;
import navdev.gpstrack.RunActivity;
import navdev.gpstrack.dao.GpsBBDD;
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
    private long mActivityId;

    @Override
    public IBinder onBind(Intent intent) {return null;}

    private BroadcastReceiver receiverAskIsRunning = new BroadcastReceiver(){
        public void onReceive (Context context, Intent intent) {
            LocalBroadcastManager manager = LocalBroadcastManager.getInstance(getApplicationContext());
            manager.sendBroadcast(new Intent(SEND_IS_RUNNING));
        }
    };


    private void buildNotification() {
        String stop = "stop";
        Intent notificationIntent = new Intent(this, RunActivity.class);
        registerReceiver(stopReceiver, new IntentFilter(stop));
        PendingIntent broadcastIntent = PendingIntent.getBroadcast(
                this, 0, notificationIntent, 0);
        // Create the persistent notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.service_msg))
                .setOngoing(true)
                .setContentIntent(broadcastIntent)
                .setSmallIcon(R.mipmap.ic_launcher);
        startForeground(1, builder.build());
    }

    protected BroadcastReceiver stopReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "received stop broadcast");
            // Stop the service when the notification is tapped
            unregisterReceiver(stopReceiver);
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
                    Location location = locationResult.getLastLocation();
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
                            if (lastLocation != null)
                                distance += MapUtils.distanceBetween(location,lastLocation);
                            Intent intent = new Intent(SEND_LAST_LOCATION);
                            intent.putExtra(LAST_LOCATION,location);
                            intent.putExtra(LAST_DISTANCE,distance);
                            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

                            lastLocation = location;
                        }


                        GpsBBDD gpsBBDD = new GpsBBDD(TrackerService.this);
                        gpsBBDD.insertPositionActivity(
                                mActivityId,
                                location.getLatitude(),
                                location.getLongitude(),
                                location.getAltitude(),
                                location.getSpeed(),
                                location.getTime());
                        gpsBBDD.closeDDBB();

                    }
                }
            }, null);
        }
    }

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiverAskIsRunning);
        super.onDestroy();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent.getExtras().containsKey(RoutedetailsActivity.ID_ACTIVITY)){
            mActivityId = intent.getLongExtra(RoutedetailsActivity.ID_ACTIVITY,0);
        }

        buildNotification();

        requestLocationUpdates();

        LocalBroadcastManager.getInstance(this).registerReceiver(receiverAskIsRunning, new IntentFilter(ASK_IS_RUNNING));
        //LocalBroadcastManager.getInstance(this).sendBroadcastSync(new Intent(SEND_IS_RUNNING));
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }
}
