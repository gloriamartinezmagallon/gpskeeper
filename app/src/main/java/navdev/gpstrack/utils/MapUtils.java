package navdev.gpstrack.utils;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.DrawFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.content.res.AppCompatResources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;

import navdev.gpstrack.R;

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

    public static void  drawPrimaryLinePath(ArrayList<LatLng> listLocsToDraw, final GoogleMap mMap, int color, Context context){
        if ( mMap == null || listLocsToDraw.size() < 2 ) return;
        LatLngBounds.Builder b = new LatLngBounds.Builder();
        PolylineOptions options = new PolylineOptions();

        options.color(color);
        options.visible(true);

        ArrayList<LatLng> marker1km = new ArrayList<>();
        float distance = 0;
        LatLng lastLatLng = null;
        for ( LatLng locRecorded : listLocsToDraw ){
            options.add( locRecorded );
            b.include(locRecorded);
            if (lastLatLng != null){
                distance += distanceBetween(locRecorded,lastLatLng);
                if (distance > 1000){
                    marker1km.add(locRecorded);
                    distance = 0;
                }
            }
            lastLatLng = locRecorded;
        }

        distance = 0;
        for (LatLng locMarker: marker1km){
            distance += 1f;
            mMap.addMarker(createMarker(context,locMarker,distance,color));
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

    public static MarkerOptions createMarker(Context context, LatLng point, float metros, int color) {
        MarkerOptions marker = new MarkerOptions();
        marker.position(point);
        int pxW = context.getResources().getDimensionPixelSize(R.dimen.map_marker_width);
        int pxH = context.getResources().getDimensionPixelSize(R.dimen.map_marker_height);
        View markerView = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.map_circle_text, null);
        markerView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        markerView.layout(0, 0, pxW, pxH);
        markerView.buildDrawingCache();
        TextView km_text_view =  markerView.findViewById(R.id.km_text_view);

        Drawable unwrappedDrawable = AppCompatResources.getDrawable(context, R.drawable.marker);
        Drawable wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
        DrawableCompat.setTint(wrappedDrawable, color);

        km_text_view.setBackground(wrappedDrawable);
        Bitmap mDotMarkerBitmap = Bitmap.createBitmap(pxW, pxH, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(mDotMarkerBitmap);
        km_text_view.setText(String.format("%.00f", metros).replace(".",","));
        markerView.draw(canvas);



        marker.icon(BitmapDescriptorFactory.fromBitmap(mDotMarkerBitmap));
        return marker;
    }

    public static void  drawPrimaryLinePathToRun(ArrayList<LatLng> listLocsToDraw, final GoogleMap mMap, int color, Context context){
        if ( mMap == null || listLocsToDraw.size() < 2 ) return;
        LatLngBounds.Builder b = new LatLngBounds.Builder();
        PolylineOptions options = new PolylineOptions();

        options.color(color);
        options.width(8);
        options.visible(true);

        ArrayList<LatLng> marker1km = new ArrayList<>();
        float distance = 0;
        LatLng lastLatLng = null;
        for ( LatLng locRecorded : listLocsToDraw ){
            options.add( locRecorded );
            b.include(locRecorded);
            if (lastLatLng != null){
                distance += distanceBetween(locRecorded,lastLatLng);
                if (distance > 1000){
                    marker1km.add(locRecorded);
                    distance = 0;
                }
            }
            lastLatLng = locRecorded;
        }

        mMap.addPolyline(options);

        distance = 0;
        for (LatLng locMarker: marker1km){
            distance += 1f;
            mMap.addMarker(createMarker(context,locMarker,distance,color));
        }
    }

    public static void  drawPoint(LatLng point, final GoogleMap mMap, int color){

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(point);
        mMap.addMarker(markerOptions);

    }
}
