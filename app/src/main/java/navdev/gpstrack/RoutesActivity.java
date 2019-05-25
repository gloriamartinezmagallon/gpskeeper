package navdev.gpstrack;

import android.arch.lifecycle.Observer;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.TextView;


import java.util.List;

import navdev.gpstrack.db.GpsTrackDB;
import navdev.gpstrack.db.RouteDao;
import navdev.gpstrack.db.Route;
import navdev.gpstrack.adapter.RoutesAdapter;

public class RoutesActivity extends AppCompatActivity implements AbsListView.OnItemClickListener {

    private RecyclerView mListView;
    private TextView mEmptyView;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.fragment_route);


        mListView = findViewById(R.id.recycler_view);
        mEmptyView =  findViewById(android.R.id.empty);

        FloatingActionButton mFabaddroute =  findViewById(R.id.fab);
        mFabaddroute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newintent = new Intent(RoutesActivity.this, ImportRouteActivity.class);
                startActivity(newintent);
            }
        });

    }

    private void initListView(){

        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        mListView.setLayoutManager(llm);
        GpsTrackDB.getDatabase(this).routeDao().numOfRoutes().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer numOfRoutes) {
                if (numOfRoutes == 0){
                    setEmptyText(getResources().getString(R.string.emptylistroutes));
                }else {
                    GpsTrackDB.getDatabase(RoutesActivity.this).routeDao().getAllRoutes().observe(RoutesActivity.this, new Observer<List<Route>>() {
                        @Override
                        public void onChanged(@Nullable List<Route> routes) {
                            RoutesAdapter mAdapter = new RoutesAdapter(RoutesActivity.this, routes);
                            mListView.setAdapter(mAdapter);
                        }
                    });

                }
            }
        });



    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

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
