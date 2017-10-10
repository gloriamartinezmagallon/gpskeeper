package navdev.gpstrack;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import navdev.gpstrack.adapter.RoutesAdapter;
import navdev.gpstrack.dao.GpsBBDD;
import navdev.gpstrack.ent.Activity;
import navdev.gpstrack.adapter.ActivitiesAdapter;

public class ActivitiesActivity extends AppCompatActivity{

    private RecyclerView mListView;

    private ActivitiesAdapter mAdapter;
    private TextView mEmptyView;

    private ArrayList<Activity> mActivities;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_activity_list);


        mListView = (RecyclerView) findViewById(R.id.recycler_view);
        mEmptyView = (TextView) findViewById(android.R.id.empty);


    }

    private void initListView(){



        GpsBBDD bbdd = new GpsBBDD(this);
        if (bbdd.numberOfActivities() == 0){
            setEmptyText(getResources().getString(R.string.emptylistactivities));
        }else{

            mActivities = bbdd.getAllActivities();

            mAdapter = new ActivitiesAdapter(this,mActivities);
            LinearLayoutManager llm = new LinearLayoutManager(this);
            llm.setOrientation(LinearLayoutManager.VERTICAL);
            mListView.setLayoutManager(llm);
            mListView.setAdapter(mAdapter);
        }

        bbdd.closeDDBB();
    }



    public void setEmptyText(CharSequence emptyText) {
        mEmptyView.setText(emptyText);
        mListView.setVisibility(View.GONE);
        mEmptyView.setVisibility(View.VISIBLE);
    }


    @Override
    protected void onResume() {
        super.onResume();
        initListView();
    }
}
