package navdev.gpstrack.ent;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import navdev.gpstrack.dao.GpsBBDD;
import navdev.gpstrack.utils.MapUtils;

/**
 * Created by gloria on 09/10/2015.
 */
public class Activity {

    Integer id;
    Integer route;
    String distance;
    Integer time;
    Date adddate;


    public Activity(Integer id, Integer route, String distance, Integer time, Date adddate) {
        this.id = id;
        this.route = route;
        this.distance = distance;
        this.time = time;
        this.adddate = adddate;
    }

    public Activity() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Route getRoute(Context context) {
        GpsBBDD gpsBBDD = new GpsBBDD(context);
        Route routeob = gpsBBDD.getRouteById(this.route);
        gpsBBDD.closeDDBB();
        return routeob;
    }

    public void setRoute(Integer route) {
        this.route = route;
    }

    public String getDistance() {
        return distance;
    }

    public String getDistanceKm(){
        try{
            float distance = Float.parseFloat(this.distance);

            return String.format("%.1f km", distance/1000);
        }catch (Exception e){e.printStackTrace();}
        return "";
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public Integer getTime() {
        return time;
    }

    public void setTime(Integer time) {
        this.time = time;
    }

    public Date getAdddate() {
        return adddate;
    }

    public String getAdddatetoformat(){

        SimpleDateFormat simpledateformat = new SimpleDateFormat("dd/MM/yyyy");
        return simpledateformat.format(this.getAdddate());
    }

    public void setAdddate(Date adddate) {
        this.adddate = adddate;
    }
    public void setAdddate(String adddate) {

        ParsePosition pos = new ParsePosition(0);
        SimpleDateFormat simpledateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date stringDate = simpledateformat.parse(adddate, pos);
        this.adddate = stringDate;

    }

    public String timeTostring(){
        int minutes = ((time / (1000 * 60)) % 60);
        int hours = ((time / (1000 * 60 * 60)) % 24);

        String txminutes, txhours;

        if (minutes<10) txminutes="0"+minutes;
        else txminutes=minutes+"";

        txhours=hours+"";

        return txhours+"h "+txminutes+" min";
    }


    public ArrayList<LatLng> getLocations(Context context){
        GpsBBDD gpsBBDD = new GpsBBDD(context);
        ArrayList<ActivityLocation> locations = gpsBBDD.getAllPositionActivity(id);
        gpsBBDD.closeDDBB();

        ArrayList<LatLng> latLngs = new ArrayList<>();
        for (ActivityLocation al: locations){
            latLngs.add(new LatLng(al.getLatitud(),al.getLongitud()));
        }
        return latLngs;
    }
}
