package navdev.gpstrack.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class ActivityServiceReceiver  extends BroadcastReceiver {

    public int mSendDataState;

    public ActivityServiceReceiver() {
    }

    private OnDataSendStateChange onDataSendStateChange = null;

    public interface OnDataSendStateChange {
        void run(Boolean inPause, long time, double distance);
    }



    public void setOnDataSendStateChange(OnDataSendStateChange onDataSendStateChange) {
        this.onDataSendStateChange = onDataSendStateChange;
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        mSendDataState = intent.getIntExtra(BroadcastNotifier.SEND_DATA_STATUS, -1);


        if(mSendDataState > 0){//HA CAMBIADO EL ESTADO DEL ENVIO DE DATOS
            if(onDataSendStateChange != null){
                Boolean inPause = intent.getBooleanExtra("inPause",false);
                long time = intent.getLongExtra("time",0);
                double distance = intent.getDoubleExtra("distance",0);
                onDataSendStateChange.run(inPause,time,distance);
            }
        }


    }


}