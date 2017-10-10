package navdev.gpstrack.dao;

/**
 * Created by gloria on 13/08/2015.
 */
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import navdev.gpstrack.ent.Activity;
import navdev.gpstrack.ent.ActivityLocation;
import navdev.gpstrack.ent.Route;

public class GpsBBDD extends SQLiteOpenHelper {

    static String LOGTAG = "GpsBBDD";

    public static final String DATABASE_NAME = "GpsBBDD.db";
    public static final String ROUTES_TABLE_NAME = "routes";
    public static final String ROUTES_COLUMN_ID = "id";
    public static final String ROUTES_COLUMN_NAME = "name";
    public static final String ROUTES_COLUMN_TRACKS= "tracks";
    public static final String ROUTES_COLUMN_IMPORTED = "imported";
    public static final String ROUTES_COLUMN_USES = "uses";
    public static final String ROUTES_COLUMN_ADDDATE = "adddate";

    public static final String ACTIVITIES_TABLE_NAME = "activities";
    public static final String ACTIVITIES_COLUMN_ID = "id";
    public static final String ACTIVITIES_COLUMN_ROUTE = "route";
    public static final String ACTIVITIES_COLUMN_TIME = "timecount";
    public static final String ACTIVITIES_COLUMN_DISTANCE = "distancecount";
    public static final String ACTIVITIES_COLUMN_ADDDATE = "adddate";


    public static final String POSITIONSACTIVITY_TABLE_NAME = "positionactivity";
    public static final String POSITIONSACTIVITY_COLUMN_ID = "id";
    public static final String POSITIONSACTIVITY_COLUMN_ACTIVTY = "activity";
    public static final String POSITIONSACTIVITY_COLUMN_LAT = "lat";
    public static final String POSITIONSACTIVITY_COLUMN_LNG = "lng";

    private HashMap hp;

    static int VERSIONDB = 3;

    public GpsBBDD(Context context)
    {
        super(context, DATABASE_NAME, null, VERSIONDB);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(LOGTAG, "Creamos las tablas");
        db.execSQL(createRoutes());
        db.execSQL(createActivities());
        db.execSQL(createPositionsActivities());
    }

    private String createPositionsActivities(){
        return "create table " + POSITIONSACTIVITY_TABLE_NAME + " " +
                "( " + POSITIONSACTIVITY_COLUMN_ID + " integer primary key AUTOINCREMENT, " +
                "" + POSITIONSACTIVITY_COLUMN_ACTIVTY + " integer," +
                "" + POSITIONSACTIVITY_COLUMN_LAT + " double," +
                "" + POSITIONSACTIVITY_COLUMN_LNG + " double)";
    }

    private String createActivities(){
        return "create table " + ACTIVITIES_TABLE_NAME + " " +
                "( " + ACTIVITIES_COLUMN_ID + " integer primary key AUTOINCREMENT, " +
                "" + ACTIVITIES_COLUMN_ROUTE + " integer," +
                "" + ACTIVITIES_COLUMN_TIME + " integer," +
                "" + ACTIVITIES_COLUMN_DISTANCE + " integer," +
                "" + ACTIVITIES_COLUMN_ADDDATE + " string)";
    }

    private String createRoutes(){
        return "create table " + ROUTES_TABLE_NAME + " " +
                "( " + ROUTES_COLUMN_ID + " integer primary key AUTOINCREMENT, " +
                "" + ROUTES_COLUMN_NAME + " text," +
                "" + ROUTES_COLUMN_TRACKS + " text," +
                "" + ROUTES_COLUMN_IMPORTED + " integer," +
                "" + ROUTES_COLUMN_USES + " integer," +
                "" + ROUTES_COLUMN_ADDDATE + " string)";
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 3){
            db.execSQL("DROP TABLE IF EXISTS " + ROUTES_TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS "+ACTIVITIES_TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS "+ POSITIONSACTIVITY_TABLE_NAME);

            onCreate(db);
        }
    }

    public void closeDDBB(){
        this.close();
    }

    public long insertRoute (String name, List<String> tracks, Integer imported, Integer uses,Date adddate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(ROUTES_COLUMN_NAME, name);
        String track = "";
        for (int i = 0; i< tracks.size(); i++){
            if (track.length() != 0) track+=" ";
            track +=tracks.get(i);
        }
        contentValues.put(ROUTES_COLUMN_TRACKS, track);
        contentValues.put(ROUTES_COLUMN_IMPORTED, imported);
        contentValues.put(ROUTES_COLUMN_USES, uses);
        SimpleDateFormat simpledateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        contentValues.put(ROUTES_COLUMN_ADDDATE, simpledateformat.format(adddate));
        return db.insert(ROUTES_TABLE_NAME, null, contentValues);
    }

    public int numberOfRoutes(){
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, ROUTES_TABLE_NAME);
        db.close();
        return numRows;
    }

    public boolean updateRoute (Integer id, String name, List<String> tracks, Integer imported, Integer uses, Date adddate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(ROUTES_COLUMN_NAME, name);
        String track = "";
        for (int i = 0; i< tracks.size(); i++){
            if (track.length() != 0) track+=" ";
            track +=tracks.get(i);
        }
        contentValues.put(ROUTES_COLUMN_TRACKS, track);
        contentValues.put(ROUTES_COLUMN_IMPORTED, imported);
        contentValues.put(ROUTES_COLUMN_USES, uses);
        SimpleDateFormat simpledateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        contentValues.put(ROUTES_COLUMN_ADDDATE, simpledateformat.format(adddate));
        db.update(ROUTES_TABLE_NAME, contentValues, ROUTES_COLUMN_ID + " = ? ", new String[]{Integer.toString(id)});
        return true;
    }

    public Integer deleteRoute (Integer id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(ROUTES_TABLE_NAME,
                ROUTES_COLUMN_ID+" = ? ",
                new String[] { Integer.toString(id) });
    }

    public Route getRouteById(Integer id){
        Route route = null;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from "+ROUTES_TABLE_NAME+" where "+ROUTES_COLUMN_ID+" = ? ",
                new String[] { Integer.toString(id) });
        res.moveToFirst();

        while(res.isAfterLast() == false){
            route = new Route();

            route.setId(res.getInt(res.getColumnIndex(ROUTES_COLUMN_ID)));
            route.setName(res.getString(res.getColumnIndex(ROUTES_COLUMN_NAME)));
            ArrayList rutas = new ArrayList();
            String[] rutasstring = res.getString(res.getColumnIndex(ROUTES_COLUMN_TRACKS)).split(" ");
            for (int i = 0; i < rutasstring.length; i++){
                rutas.add(rutasstring[i]);
            }
            route.setTracks(rutas);
            route.setImported(res.getInt(res.getColumnIndex(ROUTES_COLUMN_IMPORTED)));
            route.setUses(res.getInt(res.getColumnIndex(ROUTES_COLUMN_USES)));
            route.setAdddate(res.getString(res.getColumnIndex(ROUTES_COLUMN_ADDDATE)));

            res.moveToNext();
        }
        return route;
    }

    public ArrayList<Route> getAllRoutes() {
        ArrayList<Route> array_list = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from " + ROUTES_TABLE_NAME, null);
        if (res.moveToFirst()){
            do {
                Route route = new Route();

                route.setId(res.getInt(res.getColumnIndex(ROUTES_COLUMN_ID)));
                route.setName(res.getString(res.getColumnIndex(ROUTES_COLUMN_NAME)));
                ArrayList rutas = new ArrayList();
                String[] rutasstring = res.getString(res.getColumnIndex(ROUTES_COLUMN_TRACKS)).split(" ");
                for (int i = 0; i < rutasstring.length; i++) {
                    rutas.add(rutasstring[i]);
                }
                route.setTracks(rutas);
                route.setImported(res.getInt(res.getColumnIndex(ROUTES_COLUMN_IMPORTED)));
                route.setUses(res.getInt(res.getColumnIndex(ROUTES_COLUMN_USES)));
                route.setAdddate(res.getString(res.getColumnIndex(ROUTES_COLUMN_ADDDATE)));

                array_list.add(route);
            } while (res.moveToNext());
        }
        return array_list;
    }

    public long insertActivity (Integer route,String distance, Integer time,Date adddate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(ACTIVITIES_COLUMN_ROUTE, route);
        contentValues.put(ACTIVITIES_COLUMN_DISTANCE, distance);
        contentValues.put(ACTIVITIES_COLUMN_TIME, time);
        SimpleDateFormat simpledateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        contentValues.put(ACTIVITIES_COLUMN_ADDDATE, simpledateformat.format(adddate));
        return db.insert(ACTIVITIES_TABLE_NAME, null, contentValues);
    }

    public boolean updateActivity (long activity,String distance, Integer time) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(ACTIVITIES_COLUMN_DISTANCE, distance);
        contentValues.put(ACTIVITIES_COLUMN_TIME, time);
        return (db.update(ACTIVITIES_TABLE_NAME, contentValues,ACTIVITIES_COLUMN_ID+"= ?",new String[]{Long.toString(activity)})>0);
    }

    public Integer deleteActivity (Integer id) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(POSITIONSACTIVITY_TABLE_NAME, POSITIONSACTIVITY_COLUMN_ACTIVTY+" = ? ",
                new String[] { Integer.toString(id) });

        return db.delete(ACTIVITIES_TABLE_NAME,
                ACTIVITIES_COLUMN_ID+" = ? ",
                new String[] { Integer.toString(id) });
    }

    public Activity getActivityById(Integer id){
        Activity activity = null;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from "+ACTIVITIES_TABLE_NAME+" where "+ACTIVITIES_COLUMN_ID+" = ? ",
                new String[] { Integer.toString(id) });

        if (res.moveToFirst()){
            activity = new Activity();

            activity.setId(res.getInt(res.getColumnIndex(ACTIVITIES_COLUMN_ID)));
            activity.setRoute(res.getInt(res.getColumnIndex(ACTIVITIES_COLUMN_ROUTE)));
            activity.setDistance(res.getString(res.getColumnIndex(ACTIVITIES_COLUMN_DISTANCE)));
            activity.setTime(res.getInt(res.getColumnIndex(ACTIVITIES_COLUMN_TIME)));
            activity.setAdddate(res.getString(res.getColumnIndex(ACTIVITIES_COLUMN_ADDDATE)));
        }

        return activity;
    }

    public int numberOfActivities(){
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, ACTIVITIES_TABLE_NAME);
        db.close();
        return numRows;
    }

    public int numberOfActivitiesthismonth(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select count(*) from "+ACTIVITIES_TABLE_NAME+" WHERE "+ACTIVITIES_COLUMN_ADDDATE+" LIKE ?", new String[]{new SimpleDateFormat("yyyy-MM-%").format(new Date())});
        if(res.moveToFirst()){
            return res.getInt(0);
        }

        return 0;
    }

    public ArrayList<Activity> getAllActivities() {
        ArrayList<Activity> array_list = new ArrayList<>();


        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from "+ACTIVITIES_TABLE_NAME, null );
        res.moveToFirst();

        while(res.isAfterLast() == false){
            Activity activity = new Activity();

            activity.setId(res.getInt(res.getColumnIndex(ROUTES_COLUMN_ID)));
            activity.setRoute(res.getInt(res.getColumnIndex(ACTIVITIES_COLUMN_ROUTE)));

            activity.setDistance(res.getString(res.getColumnIndex(ACTIVITIES_COLUMN_DISTANCE)));
            activity.setTime(res.getInt(res.getColumnIndex(ACTIVITIES_COLUMN_TIME)));
            activity.setAdddate(res.getString(res.getColumnIndex(ACTIVITIES_COLUMN_ADDDATE)));

            array_list.add(activity);
            res.moveToNext();
        }
        return array_list;
    }


    public long insertPositionActivity (long activity,double lat, double lng) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(POSITIONSACTIVITY_COLUMN_ACTIVTY, activity);
        contentValues.put(POSITIONSACTIVITY_COLUMN_LAT, lat);
        contentValues.put(POSITIONSACTIVITY_COLUMN_LNG, lng);
        return db.insert(POSITIONSACTIVITY_TABLE_NAME, null, contentValues);
    }


    public ArrayList<ActivityLocation> getAllPositionActivity(int activity) {
        ArrayList<ActivityLocation> array_list = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from " + POSITIONSACTIVITY_TABLE_NAME+" WHERE "+POSITIONSACTIVITY_COLUMN_ACTIVTY+"=?", new String[]{Integer.toString(activity)});
        if (res.moveToFirst()){
            do {
                ActivityLocation activityLocation = new ActivityLocation(
                        res.getInt(res.getColumnIndex(POSITIONSACTIVITY_COLUMN_ACTIVTY)),
                        res.getDouble(res.getColumnIndex(POSITIONSACTIVITY_COLUMN_LAT)),
                        res.getDouble(res.getColumnIndex(POSITIONSACTIVITY_COLUMN_LNG))
                );

                array_list.add(activityLocation);
            } while (res.moveToNext());
        }
        return array_list;
    }

    //ESTADISTICAS
    public int numberOfkm(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select sum("+ACTIVITIES_COLUMN_DISTANCE+") from "+ACTIVITIES_TABLE_NAME, null);
        if(res.moveToFirst()){
            return res.getInt(0);
        }

        return 0;
    }

    public int numberOfkmthismonth(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select sum("+ACTIVITIES_COLUMN_DISTANCE+") from "+ACTIVITIES_TABLE_NAME+" WHERE "+ACTIVITIES_COLUMN_ADDDATE+" LIKE ?", new String[]{new SimpleDateFormat("yyyy-MM-%").format(new Date())});
        if(res.moveToFirst()){
            return res.getInt(0);
        }

        return 0;
    }

    public int numberOftime(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select sum("+ACTIVITIES_COLUMN_TIME+") from "+ACTIVITIES_TABLE_NAME, null);
        if(res.moveToFirst()){
            return res.getInt(0);
        }

        return 0;
    }

    public int numberOftimethismonth(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select sum("+ACTIVITIES_COLUMN_TIME+") from "+ACTIVITIES_TABLE_NAME+" WHERE "+ACTIVITIES_COLUMN_ADDDATE+" LIKE ?", new String[]{new SimpleDateFormat("yyyy-MM-%").format(new Date())});
        if(res.moveToFirst()){
            return res.getInt(0);
        }

        return 0;
    }

}
