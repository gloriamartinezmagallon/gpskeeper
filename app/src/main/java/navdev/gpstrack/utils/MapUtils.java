package navdev.gpstrack.utils;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.util.DisplayMetrics;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;

import navdev.gpstrack.R;
import navdev.gpstrack.ent.Activity;

public class MapUtils {

    public static void configMap(final GoogleMap mMap, boolean showMyLocationButton, Context context) {

        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(showMyLocationButton);
        }catch (Exception e){e.printStackTrace();}

    }

    public static double distanceBetween(LatLng latLng1, LatLng latLng2){
        return SphericalUtil.computeDistanceBetween(latLng1,latLng2);
    }
    public static double distanceBetween(Location loc1, Location loc2){
        LatLng latLng1 = new LatLng(loc1.getLatitude(), loc1.getLongitude());
        LatLng latLng2 = new LatLng(loc2.getLatitude(), loc2.getLongitude());
        return SphericalUtil.computeDistanceBetween(latLng1,latLng2);
    }

    public static void  drawPrimaryLinePath(ArrayList<LatLng> listLocsToDraw, final GoogleMap mMap, int color){
        if ( mMap == null || listLocsToDraw.size() < 2 ) return;
        LatLngBounds.Builder b = new LatLngBounds.Builder();
        PolylineOptions options = new PolylineOptions();

        options.color(color);
        options.visible(true);

        for ( LatLng locRecorded : listLocsToDraw ){
            options.add( locRecorded );
            b.include(locRecorded);
        }

        mMap.addPolyline(options);
        final LatLngBounds bounds = b.build();

        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
            }
        });
    }

    public static void  drawPrimaryLinePathToRun(ArrayList<LatLng> listLocsToDraw, final GoogleMap mMap, int color){
        if ( mMap == null || listLocsToDraw.size() < 2 ) return;
        LatLngBounds.Builder b = new LatLngBounds.Builder();
        PolylineOptions options = new PolylineOptions();

        options.color(color);
        options.width(8);
        options.visible(true);

        for ( LatLng locRecorded : listLocsToDraw ){
            options.add( locRecorded );
            b.include(locRecorded);
        }

        mMap.addPolyline(options);
        final LatLngBounds bounds = b.build();

    }
}
