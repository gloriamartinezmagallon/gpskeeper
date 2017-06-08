package navdev.gpstrack;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import navdev.gpstrack.dao.GpsBBDD;

public class LoadingActivity extends AppCompatActivity {

    LinearLayout mInfoLl;
    ImageView   bgImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        bgImg = (ImageView) findViewById(R.id.imageView);
        mInfoLl = (LinearLayout) findViewById(R.id.infoLl);

        bgImg.setVisibility(View.VISIBLE);
        mInfoLl.setVisibility(View.GONE);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        bgImg.setVisibility(View.GONE);
                        mInfoLl.setVisibility(View.VISIBLE);
                    }
                });
            }
        }).start();

        GpsBBDD gpsBBDD = new GpsBBDD(this);

        int numKms = gpsBBDD.numberOfkm();
        TextView kmsalltimeTV = (TextView) findViewById(R.id.kmsalltime);
        kmsalltimeTV.setText(numKms+" "+getResources().getString(R.string.kmsalltime));

        int numKmsthismonth = gpsBBDD.numberOfkmthismonth();
        TextView numkmthismonthTV = (TextView) findViewById(R.id.numkmthismonth);
        numkmthismonthTV.setText(numKmsthismonth+" ");


        int numTime = gpsBBDD.numberOftime();
        TextView timealltimeTV = (TextView) findViewById(R.id.timealltime);
        timealltimeTV.setText(timeTostring(numTime)+" "+getResources().getString(R.string.timealltime));

        int numTimethismonth = gpsBBDD.numberOftime();
        TextView numtimethismonthTV = (TextView) findViewById(R.id.numtimethismonth);
        numtimethismonthTV.setText(timeTostring(numTimethismonth));

        int numRoutes = gpsBBDD.numberOfRoutes();
        TextView numroutesTV = (TextView) findViewById(R.id.numroutes);
        numroutesTV.setText(numRoutes+"");


        int numActivities = gpsBBDD.numberOfActivities();
        TextView numactivitiesTV = (TextView) findViewById(R.id.numactivities);
        numactivitiesTV.setText(" "+getResources().getString(R.string.activitiesthismonth)+" "+numActivities);

        int numActivitiesthismonth = gpsBBDD.numberOfActivitiesthismonth();
        TextView numactivitiesthismonthTV = (TextView) findViewById(R.id.numactivitiesthismonth);
        numactivitiesthismonthTV.setText((numActivitiesthismonth+""));

        gpsBBDD.closeDDBB();


    }


    public void showActivities(View v){
        Intent newintent = new Intent(LoadingActivity.this, ActivitiesActivity.class);
        startActivity(newintent);
    }

    public void showRoutes(View v){
        Intent newintent = new Intent(LoadingActivity.this, RoutesActivity.class);
        startActivity(newintent);
    }

    public String timeTostring(int tiempo){
        int seconds = (int) ((tiempo / (1000)) % 60);
        int minutes = (int) ((tiempo / (1000 * 60)) % 60);
        int hours = (int) ((tiempo / (1000 * 60 * 60)) % 24);

        String txseconds, txminutes, txhours;
        if (seconds<10) txseconds="0"+seconds;
        else txseconds=seconds+"";

        if (minutes<10) txminutes="0"+minutes;
        else txminutes=minutes+"";

        if (hours<10) txhours="0"+hours;
        else txhours=hours+"";

        return txhours+":"+txminutes;
    }

    private void goToMain(){

        Intent intent = getIntent();
        System.out.println("Intent loading "+intent.getAction());

        Intent newintent = new Intent(LoadingActivity.this, InitActivity.class);
        if(Intent.ACTION_VIEW.equals(intent.getAction()) || Intent.ACTION_PICK.equals(intent.getAction())){
            newintent.setAction(intent.getAction());
            newintent.setData(intent.getData());
       }
        startActivity(newintent);
        finish();
    }



}
