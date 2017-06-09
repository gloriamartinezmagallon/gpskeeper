package navdev.gpstrack.utils;


import android.util.DisplayMetrics;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

import navdev.gpstrack.R;
import navdev.gpstrack.ent.Activity;

public class MapUtils {

    public static void configMap( final GoogleMap mMap, boolean showMyLocationButton){

        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        try{
            mMap.setMyLocationEnabled(showMyLocationButton);
        }catch (Exception e){e.printStackTrace();}

    }

    public static void  drawPrimaryLinePath(ArrayList<LatLng> listLocsToDraw, final GoogleMap mMap, int color){
        if ( mMap == null )
        {
            return;
        }

        if ( listLocsToDraw.size() < 2 )
        {
            return;
        }
        LatLngBounds.Builder b = new LatLngBounds.Builder();
        PolylineOptions options = new PolylineOptions();

        options.color(color);

        DisplayMetrics dm = new DisplayMetrics();
        options.visible(true);

        for ( LatLng locRecorded : listLocsToDraw )
        {
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
}
