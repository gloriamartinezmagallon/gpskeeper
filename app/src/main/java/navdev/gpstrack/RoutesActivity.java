package navdev.gpstrack;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.TextView;


import java.util.ArrayList;

import navdev.gpstrack.dao.GpsBBDD;
import navdev.gpstrack.ent.Route;
import navdev.gpstrack.fragment.adapter.RoutesAdapter;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 * interface.
 */
public class RoutesActivity extends AppCompatActivity implements AbsListView.OnItemClickListener {

    private AbsListView mListView;
    private FloatingActionButton fab_addroute;

    ArrayList<Route> routes;

    private ListAdapter mAdapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.fragment_route);


        mListView = (AbsListView) findViewById(android.R.id.list);
        mListView.setEmptyView(findViewById(android.R.id.empty));

        fab_addroute = (FloatingActionButton) findViewById(R.id.fab);
        fab_addroute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newintent = new Intent(RoutesActivity.this, ImportRouteActivity.class);
                startActivity(newintent);
            }
        });

        GpsBBDD bbdd = new GpsBBDD(this);
        if (bbdd.numberOfRoutes() == 0){
            setEmptyText(getResources().getString(R.string.emptylistroutes));
        }else{

             routes = bbdd.getAllRoutes();

            mAdapter = new RoutesAdapter(this,R.layout.fragment_route_list,routes);

            // Set the adapter
            ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);

            // Set OnItemClickListener so we can be notified on item clicks
            mListView.setOnItemClickListener(this);
        }

        bbdd.closeDDBB();

    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //((MainActivity)getActivity()).irDetallesruta(routes.get(position));
    }


    public void setEmptyText(CharSequence emptyText) {
        View emptyView = mListView.getEmptyView();

        if (emptyView instanceof TextView) {
            ((TextView) emptyView).setText(emptyText);
        }
    }



}
