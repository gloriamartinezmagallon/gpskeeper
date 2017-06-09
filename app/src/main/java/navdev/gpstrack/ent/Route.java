package navdev.gpstrack.ent;

import android.location.Location;
import android.support.v7.widget.RecyclerView;

import com.google.android.gms.maps.model.LatLng;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

    public float getDistance(){
        int distance = 0;

        String[] coor = getTracks().get(0).split(",");
        Location loc1 = new Location("");
        loc1.setLongitude(Double.valueOf(coor[0]));
        loc1.setLatitude(Double.valueOf(coor[1]));

        Location loc2 = null;
        for(int i = 1; i < getTracks().size(); i++){
            if (loc2 != null){
                loc1 = loc2;
            }
            loc2 = new Location("");

            coor = getTracks().get(i).split(",");
            loc2.setLongitude(Double.parseDouble(coor[0]));
            loc2.setLatitude(Double.parseDouble(coor[1]));

            distance += loc1.distanceTo(loc2);
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
