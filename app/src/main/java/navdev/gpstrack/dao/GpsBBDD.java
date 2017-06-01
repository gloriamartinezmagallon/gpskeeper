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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import navdev.gpstrack.ent.Activity;
import navdev.gpstrack.ent.Route;

public class GpsBBDD extends SQLiteOpenHelper {

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
    private HashMap hp;

    public GpsBBDD(Context context)
    {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        System.out.println("Creamos las tablas");
        db.execSQL(
                "create table " + ROUTES_TABLE_NAME + " " +
                        "( " + ROUTES_COLUMN_ID + " integer primary key AUTOINCREMENT, " +
                        "" + ROUTES_COLUMN_NAME + " text," +
                        "" + ROUTES_COLUMN_TRACKS + " text," +
                        "" + ROUTES_COLUMN_IMPORTED + " integer," +
                        "" + ROUTES_COLUMN_USES + " integer," +
                        "" + ROUTES_COLUMN_ADDDATE + " string)"
        );

        db.execSQL(
                "create table " + ACTIVITIES_TABLE_NAME + " " +
                        "( " + ACTIVITIES_COLUMN_ID + " integer primary key AUTOINCREMENT, " +
                        "" + ACTIVITIES_COLUMN_ROUTE + " integer," +
                        "" + ACTIVITIES_COLUMN_TIME + " text," +
                        "" + ACTIVITIES_COLUMN_DISTANCE + " integer," +
                        "" + ROUTES_COLUMN_ADDDATE + " string)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS " + ROUTES_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS "+ACTIVITIES_TABLE_NAME);
        onCreate(db);
    }

    public void closeDDBB(){
        this.close();
    }

    public long insertRoute (String name, List<String> tracks, Integer imported, Integer uses,Date adddate)
    {
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

    public boolean updateRoute (Integer id, String name, List<String> tracks, Integer imported, Integer uses, Date adddate)
    {
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


    public Integer deleteRoute (Integer id)
    {
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

    public ArrayList<Route> getAllRoutes()
    {
        ArrayList<Route> array_list = new ArrayList<Route>();


        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from "+ROUTES_TABLE_NAME, null );
        res.moveToFirst();

        while(res.isAfterLast() == false){
            Route route = new Route();

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

            array_list.add(route);
            res.moveToNext();
        }
        return array_list;
    }

    public long insertActivity (Integer route,String distance, String time,Date adddate)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(ACTIVITIES_COLUMN_ROUTE, route);
        contentValues.put(ACTIVITIES_COLUMN_DISTANCE, distance);
        contentValues.put(ACTIVITIES_COLUMN_TIME, time);
        SimpleDateFormat simpledateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        contentValues.put(ACTIVITIES_COLUMN_ADDDATE, simpledateformat.format(adddate));
        return db.insert(ACTIVITIES_TABLE_NAME, null, contentValues);
    }

    public int numberOfActivities(){
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, ACTIVITIES_TABLE_NAME);
        db.close();
        return numRows;
    }

    public boolean updateActivity (Integer id, Integer route, String distance, String time, Date adddate)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(ACTIVITIES_COLUMN_ROUTE, route);

        contentValues.put(ACTIVITIES_COLUMN_DISTANCE, distance);
        contentValues.put(ACTIVITIES_COLUMN_TIME, time);
        SimpleDateFormat simpledateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        contentValues.put(ACTIVITIES_COLUMN_ADDDATE, simpledateformat.format(adddate));
        db.update(ACTIVITIES_TABLE_NAME, contentValues, ACTIVITIES_COLUMN_ID+" = ? ", new String[]{Integer.toString(id)});
        return true;
    }


    public Integer deleteActivity (Integer id)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(ACTIVITIES_TABLE_NAME,
                ACTIVITIES_COLUMN_ID+" = ? ",
                new String[] { Integer.toString(id) });
    }

    public ArrayList<Activity> getAllActivities()
    {
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
            activity.setTime(res.getString(res.getColumnIndex(ACTIVITIES_COLUMN_TIME)));
            activity.setAdddate(res.getString(res.getColumnIndex(ACTIVITIES_COLUMN_ADDDATE)));

            array_list.add(activity);
            res.moveToNext();
        }
        return array_list;
    }
}
