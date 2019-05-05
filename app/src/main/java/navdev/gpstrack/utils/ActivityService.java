package navdev.gpstrack.utils;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.maps.model.LatLng;

import navdev.gpstrack.InitActivity;
import navdev.gpstrack.R;
import navdev.gpstrack.dao.GpsBBDD;
import navdev.gpstrack.ent.Activity;
import navdev.gpstrack.ent.Route;

public class ActivityService extends Service {

   @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    long timeInPause;
    long timeWalking;

    boolean isInPause;

    double distance;
    LatLng lastLocation;
    long lastTime;

    LocationManager mLocationManager;
    LocationListener mLocationListener;

    BroadcastNotifier mBrodcastNotifier;

    long mActivityId;

    Activity mActivity;
    Route mRoute;

    int mUmbraldistancia;
    int mNumavisos;
    int mNumavisosrealizados;

    Vibrator mVibrator;

    @Override
    public boolean stopService(Intent name) {
        if (mLocationManager != null){
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                mLocationManager.removeUpdates(mLocationListener);
        }
        removeNotificacion();
        return super.stopService(name);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mLocationManager != null){
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                mLocationManager.removeUpdates(mLocationListener);
        }
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        timeInPause = 0;
        timeWalking = 0;
        distance = 0;

        mNumavisosrealizados = 0;

        mActivityId = intent.getLongExtra("activityId",0);
        mUmbraldistancia = intent.getIntExtra("UMBRALDISTANCIA",0);
        mNumavisos = intent.getIntExtra("NUMAVISOS",0);

        GpsBBDD gpsBBDD = new GpsBBDD(this);
        mActivity = gpsBBDD.getActivityById((int)mActivityId);
        mRoute = mActivity.getRoute(this);
        gpsBBDD.closeDDBB();

        initLocationListener();

        initBroadcastNotifier();

        mVibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);

        return super.onStartCommand(intent, flags, startId);
    }

    private void initBroadcastNotifier(){
        mBrodcastNotifier = new BroadcastNotifier(getApplicationContext());
    }

    private void initLocationListener(){
        // Acquire a reference to the system Location Manager
        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // Define a listener that responds to location updates
        mLocationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                makeUseOfNewLocation(location);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
        };

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocationListener);
        }
    }

    private void makeUseOfNewLocation(Location location){

        //COMPROBAMOS SI CAMBIA DE MOVIMIENTO A PAUSA O AL REVÉS PARA ACTUALIZAR EL TIEMPO

        long currentTime = System.currentTimeMillis();
        if (location.getSpeed() == 0){
            if (!isInPause){
                if (lastTime != 0)
                    timeWalking += currentTime - lastTime;
                lastTime = currentTime;
                isInPause = true;
            }
        }else{
            if (isInPause){
                isInPause = false;
            }

            if (lastTime != 0)
                timeWalking += currentTime - lastTime;

            lastTime = currentTime;

            LatLng currentLocation = new LatLng(location.getLatitude(),location.getLongitude());
            if (lastLocation != null){
                distance += MapUtils.distanceBetween(lastLocation, currentLocation);
            }
            lastLocation = currentLocation;

            /*GpsBBDD gpsBBDD = new GpsBBDD(this);
            gpsBBDD.insertPositionActivity(mActivityId, currentLocation.latitude, currentLocation.longitude);
            gpsBBDD.closeDDBB();*/

            double distanciaARuta = mRoute.getDistanceto(location);
            if (distanciaARuta >= (double)mUmbraldistancia){
                if (mNumavisos >= mNumavisosrealizados){
                    notifyUser();
                    mVibrator.vibrate(1000);
                    mNumavisosrealizados++;
                }
            }else{
                if (mNumavisosrealizados > 0){
                    removeNotificacion();
                    mNumavisosrealizados = 0;
                }
            }
        }

        mBrodcastNotifier.broadcastIntentSendinfoState(isInPause,timeWalking,distance);
    }

    public void removeNotificacion(){
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);

        notificationManager.cancel((int)mActivityId);
    }

    public void notifyUser() {
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);

        Intent intent = new Intent(this, InitActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, (int) mActivityId, intent, 0);

        Notification.Builder builder = new Notification.Builder(this);
        builder.setSmallIcon(R.drawable.ic_notification);
        if (Build.VERSION.SDK_INT > 21)
            builder.setColor(getResources().getColor(R.color.bluedefault));
        builder.setContentTitle(getString(R.string.fueraruta));
        builder.setContentText(getString(R.string.app_name));
        builder.setPriority(Notification.PRIORITY_MAX);
        builder.setAutoCancel(true);
        Notification n = builder.build();


        notificationManager.notify((int)mActivityId, n);
    }

}
