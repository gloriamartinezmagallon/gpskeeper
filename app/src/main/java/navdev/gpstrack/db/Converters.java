package navdev.gpstrack.db;

import android.arch.persistence.room.TypeConverter;

import com.google.android.gms.maps.model.LatLng;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Converters {
    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }

    public static String dateToString(Date date) {
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        return  dateFormat.format(date);
    }

    public static String distanceToString(Integer distance) {
        return NumberFormat.getNumberInstance(Locale.getDefault()).format(distance/1000);
    }

    public static String numToString(Integer number) {
        return NumberFormat.getNumberInstance(Locale.getDefault()).format(number);
    }

    public static String timeToString(Integer time) {
        int hours = time / 3600; //segundos a horas
        int minutes = (time/60) % 60;
        return String.format("%dh %02dm", hours, minutes);
    }

    public static ArrayList<LatLng> stringToLatLngs(String coords) {
        String[] coordsArray = coords.split(" ");
        ArrayList<LatLng> latLngs = new ArrayList<>();
        for (String c: coordsArray){
            String[] latLng = c.split(",");
            if (latLng.length >= 2)
                latLngs.add(new LatLng(Double.parseDouble(latLng[1]),Double.parseDouble(latLng[0])));
        }
        return latLngs;
    }

    public static ArrayList<Double> stringToAlts(String coords) {
        String[] coordsArray = coords.split(" ");
        ArrayList<Double> alts = new ArrayList<>();
        for (String c: coordsArray){
            String[] latLng = c.split(",");
            if (latLng.length >= 3)
                alts.add(Double.parseDouble(latLng[2]));
        }
        return alts;
    }

    public static ArrayList<LatLng> activityLocationsToLatLngs(List<ActivityLocation> latlngs) {
        ArrayList<LatLng> latLngs = new ArrayList<>();

        for (ActivityLocation al: latlngs){
            latLngs.add(new LatLng(al.getLatitud(), al.getLongitud()));
        }
        return latLngs;
    }

    public static Date getFirstDateOfCurrentMonth() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY,0);
        cal.set(Calendar.MINUTE,0);
        cal.set(Calendar.SECOND,0);
        return cal.getTime();
    }
}
