package navdev.gpstrack;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.TextView;


import java.util.ArrayList;

import navdev.gpstrack.dao.GpsBBDD;
import navdev.gpstrack.ent.Route;
import navdev.gpstrack.adapter.RoutesAdapter;

public class RoutesActivity extends AppCompatActivity implements AbsListView.OnItemClickListener {

    private RecyclerView mListView;
    private FloatingActionButton mFabaddroute;
    private TextView mEmptyView;

    ArrayList<Route> mRoutes;

    private RoutesAdapter mAdapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.fragment_route);


        mListView = (RecyclerView) findViewById(R.id.recycler_view);
        mEmptyView = (TextView) findViewById(android.R.id.empty);

        mFabaddroute = (FloatingActionButton) findViewById(R.id.fab);
        mFabaddroute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newintent = new Intent(RoutesActivity.this, ImportRouteActivity.class);
                startActivity(newintent);
            }
        });

        initListView();
    }

    private void initListView(){
        GpsBBDD bbdd = new GpsBBDD(this);
        if (bbdd.numberOfRoutes() == 0){
            setEmptyText(getResources().getString(R.string.emptylistroutes));
        }else{
            mRoutes = bbdd.getAllRoutes();

            mAdapter = new RoutesAdapter(this,mRoutes);
            LinearLayoutManager llm = new LinearLayoutManager(this);
            llm.setOrientation(LinearLayoutManager.VERTICAL);
            mListView.setLayoutManager(llm);
            mListView.setAdapter(mAdapter);

        }

        bbdd.closeDDBB();
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
