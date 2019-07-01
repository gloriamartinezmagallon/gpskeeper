package navdev.gpstrack;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import java.util.List;

import navdev.gpstrack.db.ActivitiesViewModel;
import navdev.gpstrack.db.GpsTrackDB;
import navdev.gpstrack.db.ActivityDao;
import navdev.gpstrack.adapter.ActivitiesAdapter;
import navdev.gpstrack.db.ActivityComplete;

public class ActivitiesActivity extends AppCompatActivity{

    private RecyclerView mListView;

    private ActivitiesAdapter mAdapter;
    private TextView mEmptyView;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_activity_list);


        mListView = (RecyclerView) findViewById(R.id.recycler_view);
        mEmptyView = (TextView) findViewById(android.R.id.empty);


    }

    private void initListView(){


        final ActivitiesViewModel viewModel = ViewModelProviders.of(this).get(ActivitiesViewModel.class);

        final LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);

        viewModel.count().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer numOfActivities) {
                if (numOfActivities == 0){
                    setEmptyText(getResources().getString(R.string.emptylistactivities));
                }else{
                    viewModel.getAllActivities().observe(ActivitiesActivity.this, new Observer<List<ActivityComplete>>() {
                        @Override
                        public void onChanged(@Nullable List<ActivityComplete> activityCompletes) {
                            mAdapter = new ActivitiesAdapter(ActivitiesActivity.this,activityCompletes);
                            mListView.setLayoutManager(llm);
                            mListView.setAdapter(mAdapter);
                        }
                    });
                }
            }
        });
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
