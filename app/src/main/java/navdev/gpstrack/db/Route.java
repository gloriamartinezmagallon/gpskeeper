package navdev.gpstrack.db;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.location.Location;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity( tableName = "routes" )
public class Route  implements Serializable {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    Integer id;

    @ColumnInfo(name = "name")
    String name;

    @ColumnInfo(name = "tracks")
    String tracks;

    @ColumnInfo(name = "imported")
    Integer imported;

    @ColumnInfo(name = "uses")
    Integer uses;

    @ColumnInfo(name = "adddate")
    Date adddate;

    @Ignore
    public Route(String name, Date adddate, String tracks, Integer imported){
        this.name = name;
        this.adddate = adddate;
        this.tracks = tracks;
        this.imported = imported;
        this.uses = 0;

    }

    public Route(Integer id, String name,String tracks, Integer imported, Integer uses, Date adddate) {
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

    public String getName() {
        return name;
    }

    public String getTracks() {
        return tracks;
    }

    public Integer getImported() {
        return imported;
    }

    public Integer getUses() {
        return uses;
    }

    public Date getAdddate() {
        return adddate;
    }

    @Ignore
    public int getDistance(){
        int distance = 0;

        List<String> tracks = this.getTracksList();

        String[] coor = tracks.get(0).split(",");
        Location loc1 = new Location("");
        loc1.setLongitude(Double.valueOf(coor[0]));
        loc1.setLatitude(Double.valueOf(coor[1]));

        Location loc2 = null;
        for(int i = 1; i < tracks.size(); i++){
            if (loc2 != null){
                loc1 = loc2;
            }
            loc2 = new Location("");

            coor = tracks.get(i).split(",");
            loc2.setLongitude(Double.parseDouble(coor[0]));
            loc2.setLatitude(Double.parseDouble(coor[1]));

            distance += loc1.distanceTo(loc2);
        }

        return distance;
    }

    public Integer getDistanceto(Location loc){
        //TODO
        return 0;
    }

    @Ignore
    public List<String> getTracksList() {
        ArrayList rutas = new ArrayList();
        String[] rutasstring = this.tracks.split(" ");
        for (int i = 0; i < rutasstring.length; i++){
            rutas.add(rutasstring[i]);
        }

        return rutas;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setTracks(String tracks) {
        this.tracks = tracks;
    }

    public void setImported(Integer imported) {
        this.imported = imported;
    }

    public void setUses(Integer uses) {
        this.uses = uses;
    }

    public void setAdddate(Date adddate) {
        this.adddate = adddate;
    }


}
