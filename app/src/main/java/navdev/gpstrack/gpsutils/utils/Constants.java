package navdev.gpstrack.gpsutils.utils;


public interface Constants {

    String LOG = "navdev.gpstrack";

    public interface Intents {
        final String PAUSE_RESUME = "org.runnerup.PAUSE_RESUME";
        final String NEW_LAP = "org.runnerup.NEW_LAP";
        final String FROM_NOTIFICATION = "org.runnerup.FROM_NOTIFICATION";
        final String START_WORKOUT = "org.runnerup.START_WORKOUT";
        final String PAUSE_WORKOUT = "org.runnerup.PAUSE_WORKOUT";
        final String RESUME_WORKOUT = "org.runnerup.RESUME_WORKOUT";
    }

    public interface LOCATION {
        public static final String TABLE = "location";
        public static final String ACTIVITY = "activity_id";
        public static final String LAP = "lap";
        public static final String TYPE = "type";
        public static final String TIME = "time"; // in milliseconds since epoch
        public static final String ELAPSED = "elapsed";
        public static final String DISTANCE = "distance";
        public static final String LATITUDE = "latitude";
        public static final String LONGITUDE = "longitude";
        public static final String ACCURANCY = "accurancy";
        public static final String ALTITUDE = "altitude";
        public static final String GPS_ALTITUDE = "gps_altitude";
        public static final String SPEED = "speed";
        public static final String BEARING = "bearing";
        public static final String SATELLITES = "satellites";
        public static final String HR = "hr";
        public static final String CADENCE = "cadence";
        public static final String TEMPERATURE = "temperature";
        public static final String PRESSURE = "pressure";

        public static final int TYPE_START = 1;
        public static final int TYPE_END = 2;
        public static final int TYPE_GPS = 3;
        public static final int TYPE_PAUSE = 4;
        public static final int TYPE_RESUME = 5;
        public static final int TYPE_DISCARD = 6;
    }

    public interface TRACKER_STATE {
        public static final int INIT = 0;         // initial state
        public static final int INITIALIZING = 1; // initializing components
        public static final int INITIALIZED = 2;  // initialized
        public static final int STARTED = 3;      // Workout started
        public static final int PAUSED = 4;       // Workout paused
        public static final int CLEANUP = 5;      // Cleaning up components
        public static final int ERROR = 6;        // Components failed to initialize ;
        public static final int CONNECTING = 7;
        public static final int CONNECTED = 8;
        public static final int STOPPED = 9;
    }

    public interface WORKOUT_TYPE {
        public static final int BASIC = 0;
        public static final int INTERVAL = 1;
        public static final int ADVANCED = 2;
    }

    public interface DIMENSION {
        public static final int TIME = 1;
        public static final int DISTANCE = 2;
        public static final int SPEED = 3;
        public static final int PACE = 4;
        public static final int HR = 5;
        public static final int HRZ = 6;
        public static final int CAD = 7;
        public static final int TEMPERATURE = 8;
        public static final int PRESSURE = 9;
    }

    public interface LAP {
        public static final String TABLE = "lap";
        public static final String ACTIVITY = "activity_id";
        public static final String LAP = "lap";
        public static final String INTENSITY = "type";
        public static final String TIME = "time";
        public static final String DISTANCE = "distance";
        public static final String PLANNED_TIME = "planned_time";
        public static final String PLANNED_DISTANCE = "planned_distance";
        public static final String PLANNED_PACE = "planned_pace";
        public static final String AVG_HR = "avg_hr";
        public static final String MAX_HR = "max_hr";
        public static final String AVG_CADENCE = "avg_cadence";
    }
}
