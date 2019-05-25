package navdev.gpstrack.db;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.arch.persistence.room.migration.Migration;
import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

@Database(entities = {Activity.class,Route.class, ActivityLocation.class}, version = 6, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class GpsTrackDB extends RoomDatabase {

    private static GpsTrackDB INSTANCE;
    private static final String DB_NAME = "GpsBBDD2.db";

    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {}
    };
    private static final Migration MIGRATION_2_3 = new Migration(2,3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {}
    };
    private static final Migration MIGRATION_3_4 = new Migration(3,4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {}
    };
    private static final Migration MIGRATION_4_5 = new Migration(4,5) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {}
    };
    private static final Migration MIGRATION_5_6= new Migration(5,6) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            Cursor db = database.query("SELECT * FROM routes");
            db.moveToFirst();
            ArrayList<Route> routes = new ArrayList<>();
            do{
                Integer id = db.getInt(0);
                String name = db.getString(1);
                String tracks = db.getString(2);
                Integer imported = db.getInt(3);
                Integer uses = db.getInt(4);
                String adddate = db.getString(5);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date d = new Date();
                try {
                    d = sdf.parse(adddate);
                } catch (Exception ex) {
                    Log.v("Exception", ex.getLocalizedMessage());
                }

                routes.add(new Route(id, name, tracks, imported, uses, d));

            }while(db.moveToNext());

            database.execSQL("CREATE TABLE IF NOT EXISTS `routes_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT, `name` TEXT, `tracks` TEXT, `imported` INTEGER, `uses` INTEGER, `adddate` INTEGER)");

            String queryInsert = "INSERT OR REPLACE INTO `routes_new`(`id`,`name`,`tracks`,`imported`,`uses`,`adddate`) VALUES (?,?,?,?,?,?)";
            for (Route r:routes){
                database.execSQL(queryInsert, new Object[]{r.id,r.name,r.tracks,r.imported,r.uses,r.adddate.getTime()});
            }
            database.execSQL("DROP TABLE IF EXISTS `routes`");
            database.execSQL("ALTER TABLE `routes_new` RENAME TO `routes`;");


            db = database.query("SELECT * FROM activities");
            db.moveToFirst();
            ArrayList<Activity> activities = new ArrayList<>();
            do{
                Integer id = db.getInt(0);
                Integer route = db.getInt(1);
                Integer distance = db.getInt(2);
                Integer time = db.getInt(3);
                String adddate = db.getString(4);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date d = new Date();
                try {
                    d = sdf.parse(adddate);
                } catch (Exception ex) {
                    Log.v("Exception", ex.getLocalizedMessage());
                }

                activities.add(new Activity(id, route, distance, time, d));

            }while(db.moveToNext());

            database.execSQL("CREATE TABLE IF NOT EXISTS `activities_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT, `route` INTEGER NOT NULL, `distance` INTEGER NOT NULL, `time` INTEGER NOT NULL, `adddate` INTEGER NOT NULL)");

            String queryInsertA = "INSERT OR REPLACE INTO `activities_new`(`id`,`route`,`distance`,`time`,`adddate`) VALUES (?,?,?,?,?)";
            for (Activity a:activities){
                database.execSQL(queryInsertA, new Object[]{a.getId(),a.getRoute(),a.getDistance(),a.getTime(),a.getAdddate().getTime()});
            }
            database.execSQL("DROP TABLE IF EXISTS `activities`");
            database.execSQL("ALTER TABLE `activities_new` RENAME TO `activities`;");

            database.execSQL("CREATE TABLE IF NOT EXISTS `positionactivity_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT, `activity` INTEGER NOT NULL, `lat` REAL NOT NULL, `lng` REAL NOT NULL, `alt` REAL NOT NULL, `speed` REAL NOT NULL, `registerTime` INTEGER NOT NULL)");
            database.execSQL("INSERT INTO `positionactivity_new` SELECT id, activity, lat, lng, IFNULL(alt,0), IFNULL(speed,0), IFNULL(registerTime,0) FROM positionactivity");

            database.execSQL("DROP TABLE IF EXISTS `positionactivity`");
            database.execSQL("ALTER TABLE `positionactivity_new` RENAME TO `positionactivity`;");

        }
    };

    public static GpsTrackDB getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (GpsTrackDB.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            GpsTrackDB.class, DB_NAME)
                            .addCallback(new RoomDatabase.Callback() {
                                @Override
                                public void onCreate(@NonNull SupportSQLiteDatabase db) {
                                    super.onCreate(db);
                                }
                            })
                            //.allowMainThreadQueries()
                            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5,MIGRATION_5_6)
                            .build();
                }
            }
        }
        return INSTANCE;
    }


    public abstract RouteDao routeDao();
    public abstract ActivityDao activityDao();



}
