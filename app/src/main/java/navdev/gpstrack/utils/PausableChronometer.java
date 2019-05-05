package navdev.gpstrack.utils;

import android.content.Context;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Chronometer;


public class PausableChronometer  extends Chronometer {
    private static final String TAG = PausableChronometer.class.getSimpleName();

    private long mTimeWhenPaused = 0;

    public PausableChronometer(Context context) {
        super(context);
    }

    public PausableChronometer(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    public void restart() {
        reset();
        start();
    }

    @Override
    public void start() {
        setBase(SystemClock.elapsedRealtime()+mTimeWhenPaused);
        super.start();
    }

    @Override
    public void stop() {
        super.stop();
        mTimeWhenPaused = getBase() - SystemClock.elapsedRealtime();
    }

    public void reset() {
        stop();
        setBase(SystemClock.elapsedRealtime());
        mTimeWhenPaused = 0;
    }

    public void resume() {
        setBase(SystemClock.elapsedRealtime() + mTimeWhenPaused);
        Log.d(TAG,"RESUME "+mTimeWhenPaused);
        start();
    }

    public void pause() {
        mTimeWhenPaused = getBase() - SystemClock.elapsedRealtime();
        Log.d(TAG,"pause "+SystemClock.elapsedRealtime());
        Log.d(TAG,"pause "+getBase());
        Log.d(TAG,"pause "+mTimeWhenPaused);
        stop();
    }

    public int getTime(){
        String[] texto = ((String)getText()).split(":");
        int time= 0;
        for (int i = 0; i < texto.length; i++){
            for (int j = i+1; j < texto.length; j++){
                time += Integer.parseInt(texto[i])*60;
            }
        }
        return time;
    }
}
