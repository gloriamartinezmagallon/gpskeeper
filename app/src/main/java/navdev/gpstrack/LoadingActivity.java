package navdev.gpstrack;

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

    }

    @Override
    protected void onResume() {
        super.onResume();
        getInfoBBDD();
        bgImg.setVisibility(View.GONE);
        mInfoLl.setVisibility(View.VISIBLE);
    }

    private void getInfoBBDD(){
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

        int numTimethismonth = gpsBBDD.numberOftimethismonth();
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

    public String timeTostring(int tiempo) {
        int seconds = (int) (tiempo % 60);
        int minutes = (int) (tiempo / 60) % 60;
        int hours = (int) ((tiempo / (60 * 60)) % 24);

        String txseconds, txminutes, txhours;
        if (seconds < 10) txseconds = "0" + seconds;
        else txseconds = seconds + "";

        if (minutes < 10) txminutes = "0" + minutes;
        else txminutes = minutes + "";

        if (hours < 10) txhours = "0" + hours;
        else txhours = hours + "";

        return txhours + ":" + txminutes;
    }

}
