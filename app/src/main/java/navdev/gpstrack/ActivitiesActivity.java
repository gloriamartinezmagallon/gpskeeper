package navdev.gpstrack;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import navdev.gpstrack.dao.GpsBBDD;
import navdev.gpstrack.ent.Activity;
import navdev.gpstrack.adapter.ActivitiesAdapter;

public class ActivitiesActivity extends AppCompatActivity implements AbsListView.OnItemClickListener {

    private AbsListView mListView;

    private ListAdapter mAdapter;

    private ArrayList<Activity> mActivities;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_activity_list);


        mListView = (AbsListView) findViewById(android.R.id.list);
        mListView.setEmptyView(findViewById(android.R.id.empty));



        GpsBBDD bbdd = new GpsBBDD(this);
        if (bbdd.numberOfActivities() == 0){
            setEmptyText(getResources().getString(R.string.emptylistactivities));
        }else{

            mActivities = bbdd.getAllActivities();

            mAdapter = new ActivitiesAdapter(this,R.layout.fragment_activity_list,mActivities);

            // Set the adapter
            mListView.setAdapter(mAdapter);

            // Set OnItemClickListener so we can be notified on item clicks
            mListView.setOnItemClickListener(this);
        }

        bbdd.closeDDBB();

    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        return;
    }

    public void setEmptyText(CharSequence emptyText) {
        View emptyView = mListView.getEmptyView();

        if (emptyView instanceof TextView) {
            ((TextView) emptyView).setText(emptyText);
        }
    }



}
