package navdev.gpstrack.gpsutils.tracker.workout;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class Trigger implements TickComponent {

    final ArrayList<TriggerSuppression> triggerSuppression = new ArrayList<TriggerSuppression>();

    @Override
    public void onInit(Workout s) {

    }

    @Override
    public void onBind(Workout s, HashMap<String, Object> bindValues) {

    }

    @Override
    public void onEnd(Workout s) {

    }

    public void fire(Workout w) {
        for (TriggerSuppression s : triggerSuppression) {
            if (s.suppress(this, w)) {
                Log.e(getClass().getName(), "trigger: " + this + "suppressed by: " + s);
                return;
            }
        }
    }
}