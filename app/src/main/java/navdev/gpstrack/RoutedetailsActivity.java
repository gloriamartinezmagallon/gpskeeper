package navdev.gpstrack;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import navdev.gpstrack.db.Converters;
import navdev.gpstrack.db.GpsTrackDB;
import navdev.gpstrack.db.RouteDao;
import navdev.gpstrack.db.Route;
import navdev.gpstrack.utils.MapUtils;

public class RoutedetailsActivity extends AppCompatActivity {

    public static String ROUTE = "route";
    public static String ID_ACTIVITY = "activityID";
    public static String ACTIVAR_SIMULACION = "activarSimulacion";
    private Route mRoute = null;
    GoogleMap mMap;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_routedetails);

        mRoute = (Route) getIntent().getExtras().getSerializable(ROUTE);

        if (mRoute == null) {
            Toast.makeText(RoutedetailsActivity.this, R.string.rutanoencontrada, Toast.LENGTH_LONG).show();
            finish();
        }

        final EditText route_name = findViewById(R.id.route_name);
        route_name.setText(mRoute.getName());

        TextView route_distancia = findViewById(R.id.route_distancia);
        route_distancia.setText(Converters.distanceToString(mRoute.getDistance())+"km");

        final SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
                MapUtils.configMap(mMap, false, RoutedetailsActivity.this);
                MapUtils.drawPrimaryLinePath(Converters.stringToLatLngs(mRoute.getTracks()), mMap, getResources().getColor(R.color.bluedefault),RoutedetailsActivity.this);
            }
        });



        final TextView btncomenzar = (TextView) findViewById(R.id.btncomenzar);
        btncomenzar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newintent = new Intent(RoutedetailsActivity.this, InitActivity.class);
                newintent.putExtra(RoutedetailsActivity.ROUTE,mRoute);
                startActivity(newintent);
                finish();
            }
        });

        FloatingActionButton fabsave = findViewById(R.id.fabsave);
        fabsave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String routeName = route_name.getText().toString();
                new AsyncTask() {
                    @Override
                    protected Object doInBackground(Object[] objects) {
                        mRoute.setName(routeName);
                        GpsTrackDB.getDatabase(RoutedetailsActivity.this).routeDao().update(mRoute);
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Object o) {
                        Toast.makeText(RoutedetailsActivity.this, getResources().getString(R.string.rutaguardada), Toast.LENGTH_LONG).show();
                        super.onPostExecute(o);
                    }
                }.execute();

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

                                new AsyncTask() {
                                    @Override
                                    protected Object doInBackground(Object[] objects) {
                                        GpsTrackDB.getDatabase(RoutedetailsActivity.this).routeDao().delete(mRoute);
                                        return null;
                                    }

                                    @Override
                                    protected void onPostExecute(Object o) {
                                        Toast.makeText(RoutedetailsActivity.this, getResources().getString(R.string.rutaborrada), Toast.LENGTH_LONG).show();
                                        irListadoRutas();
                                        super.onPostExecute(o);
                                    }
                                }.execute();
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
