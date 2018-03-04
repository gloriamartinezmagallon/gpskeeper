package navdev.gpstrack.gpsutils.notifications;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import navdev.gpstrack.InitActivity;
import navdev.gpstrack.R;
import navdev.gpstrack.gpsutils.tracker.workout.Scope;
import navdev.gpstrack.gpsutils.tracker.workout.WorkoutInfo;
import navdev.gpstrack.gpsutils.utils.Constants;
import navdev.gpstrack.gpsutils.utils.Formatter;

public class OngoingState implements NotificationState {
    private final Formatter formatter;
    private final WorkoutInfo workoutInfo;
    private final Context context;
    private final NotificationCompat.Builder builder;

    public OngoingState(Formatter formatter, WorkoutInfo workoutInfo, Context context) {
        this.formatter = formatter;
        this.workoutInfo = workoutInfo;
        this.context = context;

        builder = new NotificationCompat.Builder(context);
        Intent i = new Intent(context, InitActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        i.putExtra(Constants.Intents.FROM_NOTIFICATION, true);
        PendingIntent pi = PendingIntent.getActivity(context, 0, i, 0);

        builder.setTicker("RunnerUp activity started");
        builder.setContentIntent(pi);
        builder.setContentTitle("Activity ongoing");
       // builder.setSmallIcon(R.drawable.ic_stat_notify);
        builder.setOngoing(true);
        builder.setOnlyAlertOnce(true);
        navdev.gpstrack.gpsutils.utils.NotificationCompat.setLocalOnly(builder);
        navdev.gpstrack.gpsutils.utils.NotificationCompat.setVisibility(builder);
        navdev.gpstrack.gpsutils.utils.NotificationCompat.setCategory(builder);
    }

    @Override
    public Notification createNotification() {
        String distance = formatter.formatDistance(Formatter.Format.TXT_SHORT,
                Math.round(workoutInfo.getDistance(Scope.ACTIVITY)));
        String time = formatter.formatElapsedTime(Formatter.Format.TXT_LONG,
                Math.round(workoutInfo.getTime(Scope.ACTIVITY)));
        String pace = formatter.formatPace(Formatter.Format.TXT_SHORT,
                workoutInfo.getPace(Scope.ACTIVITY));

        String content = String.format("%s: %s %s: %s %s: %s",
                context.getString(R.string.distancia), distance,
                context.getString(R.string.tiempo), time,
                context.getString(R.string.paso), pace);
        builder.setContentText(content);

        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle(builder);
        bigTextStyle.setBigContentTitle("Activity ongoing");
        bigTextStyle.bigText(String.format("%s: %s,\n%s: %s\n%s: %s",
                context.getString(R.string.distancia), distance,
                context.getString(R.string.tiempo), time,
                context.getString(R.string.paso), pace));
        builder.setStyle(bigTextStyle);

        return builder.build();
    }
}

