package navdev.gpstrack.utils;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.maps.model.LatLng;

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



    @Override
    public boolean stopService(Intent name) {
        if (mLocationManager != null){
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                mLocationManager.removeUpdates(mLocationListener);
        }
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

        initLocationListener();

        initBroadcastNotifier();

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
        if (location.getSpeed() == 0){
            if (!isInPause){
                long currentTime = System.currentTimeMillis();
                if (lastTime != 0)
                    timeWalking += currentTime - lastTime;
                lastTime = currentTime;
                isInPause = true;
            }
        }else{
            if (isInPause){
                long currentTime = System.currentTimeMillis();
                if (lastTime != 0)
                    timeInPause += currentTime - lastTime;
                lastTime = currentTime;
                isInPause = false;
            }
        }


        LatLng currentLocation = new LatLng(location.getLatitude(),location.getLongitude());
        if (lastLocation != null){
            distance += MapUtils.distanceBetween(lastLocation, currentLocation);
        }
        lastLocation = currentLocation;

        mBrodcastNotifier.broadcastIntentSendinfoState(isInPause,timeWalking,distance);
    }

}
