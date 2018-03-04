package navdev.gpstrack.gpsutils.tracker.workout;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;

import java.util.List;

import navdev.gpstrack.R;
import navdev.gpstrack.gpsutils.utils.Formatter;
import navdev.gpstrack.gpsutils.utils.SafeParse;

public class WorkoutBuilder {

    /**
     * @return workout based on SharedPreferences
     */
    public static Workout createDefaultWorkout(Resources res, SharedPreferences prefs,
                                               Dimension target) {
        Workout w = new Workout();


        Step step = new Step();


        addAutoPauseTrigger(res, step, prefs);
        w.steps.add(step);

        if (target == Dimension.PACE) {
            double unitMeters = Formatter.getUnitMeters(res, prefs);
            double seconds_per_unit = (double) SafeParse.parseSeconds(
                    prefs.getString(res.getString(R.string.pref_basic_target_pace_max), "00:05:00"), 5 * 60);
            int targetPaceRange = prefs.getInt(res.getString(R.string.pref_basic_target_pace_min_range), 15);
            double targetPaceMax = seconds_per_unit / unitMeters;
            double targetPaceMin = (targetPaceMax * unitMeters - targetPaceRange) / unitMeters;
            Range range = new Range(targetPaceMin, targetPaceMax);
            step.targetType = Dimension.PACE;
            step.targetValue = range;
        }
        /**
         *
         */
        return w;
    }

    private static void addAutoPauseTrigger(Resources res, Step step, SharedPreferences prefs) {
        boolean enableAutoPause = prefs.getBoolean(res.getString(R.string.pref_autopause_active), true);
        if (!enableAutoPause)
            return;

        float autoPauseMinSpeed = 0;
        float autoPauseAfterSeconds = 4f;

        String val = prefs.getString(res.getString(R.string.pref_autopause_minpace), "60");
        try {
            float autoPauseMinPace = Float.parseFloat(val);
            if (autoPauseMinPace > 0)
                autoPauseMinSpeed = 1000 / (autoPauseMinPace * 60);
        } catch (NumberFormatException e) {
        }
        val = prefs.getString(res.getString(R.string.pref_autopause_afterseconds), "4");
        try {
            autoPauseAfterSeconds = Float.parseFloat(val);
        } catch (NumberFormatException e) {
        }
        AutoPauseTrigger tr = new AutoPauseTrigger(autoPauseAfterSeconds, autoPauseMinSpeed);
        step.triggers.add(tr);
    }




    public static SharedPreferences getSubPreferences(Context ctx, SharedPreferences pref,
                                                      String key, String defaultVal, String suffix) {
        String name = pref.getString(key, null);
        if (name == null || name.contentEquals(defaultVal)) {
            return pref;
        }
        return ctx.getSharedPreferences(name + suffix, Context.MODE_PRIVATE);
    }




    interface TriggerFilter {
        boolean match(Trigger trigger);
    }

    private static Trigger hasTrigger(List<Trigger> triggers, TriggerFilter filter) {
        for (Trigger t : triggers) {
            if (filter.match(t))
                return t;
        }
        return null;
    }





    public static void prepareWorkout(Resources res, SharedPreferences prefs, Workout w) {

    }
}

