package navdev.gpstrack.ent;

import android.content.Context;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

import navdev.gpstrack.dao.GpsBBDD;

/**
 * Created by gloria on 09/10/2015.
 */
public class Activity {

    Integer id;
    Integer route;
    String distance;
    String time;
    Date adddate;


    public Activity(Integer id, Integer route, String distance, String time, Date adddate) {
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

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
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
}
