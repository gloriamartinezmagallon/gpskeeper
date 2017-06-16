package navdev.gpstrack.utils;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

/**
 * Created by glori on 16/06/2017.
 */

public class BroadcastNotifier {


    // Defines a custom Intent action
    public static final String BROADCAST_ACTION = "navdev.gpstrack.BROADCAST";
    public static final String SEND_DATA_STATUS = "nafarco.gpstrack.SEND_DATA_STATUS";


    private LocalBroadcastManager mBroadcaster;

    public BroadcastNotifier(Context context) {

        // Gets an instance of the support library local broadcastmanager
        mBroadcaster = LocalBroadcastManager.getInstance(context);

    }


    public void broadcastIntentSendinfoState(boolean inPause, long time, double distance) {

        Intent localIntent = new Intent();

        // The Intent contains the custom broadcast action for this app
        localIntent.setAction(BROADCAST_ACTION);

        // Puts the status into the Intent
        localIntent.putExtra(SEND_DATA_STATUS, 2);
        localIntent.putExtra("inPause", inPause);
        localIntent.putExtra("time", time);
        localIntent.putExtra("distance", distance);
        localIntent.addCategory(Intent.CATEGORY_DEFAULT);

        // Broadcasts the Intent
        mBroadcaster.sendBroadcast(localIntent);

    }
}