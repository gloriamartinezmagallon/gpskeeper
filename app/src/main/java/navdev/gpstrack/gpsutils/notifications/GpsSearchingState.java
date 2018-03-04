package navdev.gpstrack.gpsutils.notifications;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import navdev.gpstrack.InitActivity;
import navdev.gpstrack.gpsutils.tracker.GpsInformation;
import navdev.gpstrack.gpsutils.utils.Constants;


public class GpsSearchingState implements NotificationState {
    private final Context context;
    private final GpsInformation gpsInformation;
    private final NotificationCompat.Builder builder;

    public GpsSearchingState(Context context, GpsInformation gpsInformation) {
        this.context = context;
        this.gpsInformation = gpsInformation;

        builder = new NotificationCompat.Builder(context);
        Intent i = new Intent(context, InitActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        i.putExtra(Constants.Intents.FROM_NOTIFICATION, true);
        PendingIntent pi = PendingIntent.getActivity(context, 0, i, 0);

        builder.setContentIntent(pi);
        builder.setContentTitle("Buscando GPS");
        //builder.setSmallIcon(R.drawable.ic_stat_notify);
        builder.setOnlyAlertOnce(true);
        navdev.gpstrack.gpsutils.utils.NotificationCompat.setLocalOnly(builder);
        navdev.gpstrack.gpsutils.utils.NotificationCompat.setVisibility(builder);
        navdev.gpstrack.gpsutils.utils.NotificationCompat.setCategory(builder);
    }

    @Override
    public Notification createNotification() {
        builder.setContentText(String.format("%s: %d/%d%s",
               "Satelites GPS",
                gpsInformation.getSatellitesFixed(), gpsInformation.getSatellitesAvailable(),
                gpsInformation.getGpsAccuracy()));

        return builder.build();
    }
}
