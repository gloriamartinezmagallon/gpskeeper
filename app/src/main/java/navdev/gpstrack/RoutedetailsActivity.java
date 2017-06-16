package navdev.gpstrack;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;


import java.util.ArrayList;

import navdev.gpstrack.dao.GpsBBDD;
import navdev.gpstrack.ent.Route;
import navdev.gpstrack.utils.MapUtils;

public class RoutedetailsActivity extends AppCompatActivity {

    public static String ID_ROUTE = "routeId";
    private Route mRoute = null;
    GoogleMap mMap;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_routedetails);

        int idRoute = getIntent().getExtras().getInt(ID_ROUTE);

        GpsBBDD gpsBBDD = new GpsBBDD(this);
        mRoute = gpsBBDD.getRouteById(idRoute);
        gpsBBDD.closeDDBB();

        if (mRoute == null){
            Toast.makeText(this,R.string.rutanoencontrada,Toast.LENGTH_LONG).show();
            finish();
        }
        final EditText route_name = (EditText) findViewById(R.id.route_name);
        route_name.setText(mRoute.getName());

        TextView route_distancia = (TextView) findViewById(R.id.route_distancia);
        route_distancia.setText(mRoute.getDistanceKm());

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
                MapUtils.drawPrimaryLinePath(mRoute.getTracksLatLng(),mMap,getResources().getColor(R.color.bluedefault));
            }
        });



        final TextView btncomenzar = (TextView) findViewById(R.id.btncomenzar);
        btncomenzar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newintent = new Intent(RoutedetailsActivity.this, InitActivity.class);
                newintent.putExtra(RoutedetailsActivity.ID_ROUTE,mRoute.getId());
                startActivity(newintent);
                finish();
            }
        });

        FloatingActionButton fabsave = (FloatingActionButton) findViewById(R.id.fabsave);
        fabsave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRoute.setName(route_name.getText().toString());
                GpsBBDD gpsBBDD = new GpsBBDD(RoutedetailsActivity.this);
                gpsBBDD.updateRoute(mRoute.getId(), mRoute.getName(), mRoute.getTracks(), mRoute.getImported(), mRoute.getUses(), mRoute.getAdddate());
                gpsBBDD.closeDDBB();
                Toast.makeText(RoutedetailsActivity.this, getResources().getString(R.string.rutaguardada), Toast.LENGTH_LONG).show();
            }
        });

        FloatingActionButton fabtrash = (FloatingActionButton) findViewById(R.id.fabtrash);
        fabtrash.setVisibility(View.VISIBLE);
        fabtrash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(RoutedetailsActivity.this)
                        .setIcon(R.drawable.ic_delete_black_24dp)
                        .setTitle(R.string.borrarruta)
                        .setMessage(R.string.borrarrutaconfirm)
                        .setPositiveButton(R.string.si, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                GpsBBDD gpsBBDD = new GpsBBDD(RoutedetailsActivity.this);

                                gpsBBDD.deleteRoute(mRoute.getId());

                                Toast.makeText(RoutedetailsActivity.this, getResources().getString(R.string.rutaborrada), Toast.LENGTH_LONG).show();
                                gpsBBDD.closeDDBB();
                                irListadoRutas();
                            }

                        })
                        .setNegativeButton(R.string.no, null)
                        .show();

            }
        });
    }

    private void irListadoRutas(){
        Intent newintent = new Intent(RoutedetailsActivity.this, RoutesActivity.class);
        startActivity(newintent);
        finish();
    }
}
