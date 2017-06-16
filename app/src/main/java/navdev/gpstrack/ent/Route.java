package navdev.gpstrack.ent;

import android.location.Location;
import android.support.v7.widget.RecyclerView;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.SphericalUtil;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import navdev.gpstrack.utils.MapUtils;

public class Route {

    Integer id;
    String name;
    List tracks;
    Integer imported;
    Integer uses;
    Date adddate;

    public Route(){
        this.id = 0;

    }

    public Route(Integer id, String name,List tracks, Integer imported, Integer uses, Date adddate) {
        this.id = id;
        this.name = name;
        this.tracks = tracks;
        this.imported = imported;
        this.uses = uses;
        this.adddate = adddate;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getTracks() {
        return tracks;
    }

    public void setTracks(List tracks) {
        this.tracks = tracks;
    }

    public Integer getImported() {
        return imported;
    }

    public void setImported(Integer imported) {
        this.imported = imported;
    }

    public Integer getUses() {
        return uses;
    }

    public void setUses(Integer uses) {
        this.uses = uses;
    }

    public Date getAdddate() {
        return adddate;
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

    public float getDistanceto(Location posicion){
        float distance = 0;

        for(int i = 1; i < getTracks().size(); i++){
            Location loc = new Location("");

            String[] coor = getTracks().get(i).split(",");
            loc.setLongitude(Double.parseDouble(coor[0]));
            loc.setLatitude(Double.parseDouble(coor[1]));

            if (distance == 0 || distance > loc.distanceTo(posicion)){
                distance = loc.distanceTo(posicion);
            }
        }

        return distance;
    }

    public String getDistanceKm(){
        float distance = 0;

        for(int i = 1; i < getTracks().size(); i++) {

            String[] aux = this.getTracks().get(i).split(",");
            double lng = Double.parseDouble(aux[0]);
            double lat = Double.parseDouble(aux[1]);
            LatLng currLocation = new LatLng(lat,lng);

            aux = this.getTracks().get(i-1).split(",");
            lng = Double.parseDouble(aux[0]);
            lat = Double.parseDouble(aux[1]);
            LatLng lastLocation = new LatLng(lat,lng);

            distance += MapUtils.distanceBetween(lastLocation,currLocation);


        }

        return String.format("%.1f km", distance/1000);
    }

    public float getDistance(){
        float distance = 0;

        for(int i = 1; i < getTracks().size(); i++) {

            String[] aux = this.getTracks().get(i).split(",");
            double lng = Double.parseDouble(aux[0]);
            double lat = Double.parseDouble(aux[1]);
            LatLng currLocation = new LatLng(lat,lng);

            aux = this.getTracks().get(i-1).split(",");
            lng = Double.parseDouble(aux[0]);
            lat = Double.parseDouble(aux[1]);
            LatLng lastLocation = new LatLng(lat,lng);

            distance += SphericalUtil.computeDistanceBetween(lastLocation,currLocation);


        }

        return distance;
    }

    @Override
    public String toString() {
        return name;
    }


    public ArrayList<LatLng> getTracksLatLng(){
        ArrayList<LatLng> locs = new ArrayList<LatLng>();
        for (int i = 0; i < this.getTracks().size(); i++){
            String[] aux = this.getTracks().get(i).split(",");
            double lng = Double.parseDouble(aux[0]);
            double lat = Double.parseDouble(aux[1]);
            System.out.println("Coords "+lat+"/"+lng);
            locs.add(new LatLng(lat,lng));
        }

        return locs;
    }


}
