package navdev.gpstrack.fragment;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;


import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import navdev.gpstrack.MainActivity;
import navdev.gpstrack.R;
import navdev.gpstrack.dao.GpsBBDD;
import navdev.gpstrack.ent.Route;
import navdev.gpstrack.utils.PermissionUtils;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link MapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MapFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    static String LOGTAG = "MapFragment";


    LinearLayout ll_configruta, ll_playruta;
    MapView mapView;
    GoogleMap mMap;
    TextView btncomenzar, tv_tiempo, tv_distancia, tv_velocidad, tv_labeltiempo;

    FloatingActionButton fab;
    FloatingActionButton fabpause;

    Timer updatetimer;

    long tiempo;


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MapFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MapFragment newInstance(String param1, String param2) {
        MapFragment fragment = new MapFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public MapFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        btncomenzar = (TextView) view.findViewById(R.id.btncomenzar);
        btncomenzar.setVisibility(View.GONE);


        tv_distancia = (TextView) view.findViewById(R.id.tv_distancia);
        tv_tiempo = (TextView) view.findViewById(R.id.tv_tiempo);
        tv_velocidad = (TextView) view.findViewById(R.id.tv_velocidad);

        tv_labeltiempo = (TextView) view.findViewById(R.id.labeltiempo);


        ll_configruta = (LinearLayout) view.findViewById(R.id.ll_configruta);
        ll_playruta = (LinearLayout) view.findViewById(R.id.ll_playruta);


        fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fabpause = (FloatingActionButton) view.findViewById(R.id.fabpause);


        mapView = (MapView) view.findViewById(R.id.mapView);

        mapView.onCreate(savedInstanceState);
        /*map.setMyLocationEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);




        map.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {

                if (mParam2.length() == 0 || ((MainActivity) getActivity()).rutainiciada)
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 17));

                if (((MainActivity) getActivity()).rutainiciada) {
                    if (location.getSpeed() == 0)
                        ((MainActivity) getActivity()).rutapausada = true;
                    else ((MainActivity) getActivity()).rutapausada = false;


                    if (((MainActivity) getActivity()).rutapausada) {

                        tv_labeltiempo.setText(R.string.tiempopausa);

                        tv_distancia.setText(String.format("%.3f", (double) ((MainActivity) getActivity()).distanciarecorrida / 1000) + " km");
                        tv_velocidad.setText("0 km/h");
                    } else {
                        tv_labeltiempo.setText(R.string.tiempo);

                        tv_velocidad.setText(String.format("%.2f", (location.getSpeed() * 3.6)) + " km/h");

                        if (((MainActivity) getActivity()).lastlocation != null) {
                            ((MainActivity) getActivity()).distanciarecorrida += location.distanceTo(((MainActivity) getActivity()).lastlocation);
                        }
                        ((MainActivity) getActivity()).lastlocation = location;
                        tv_distancia.setText(String.format("%.3f", (double) ((MainActivity) getActivity()).distanciarecorrida / 1000) + " km");

                        if (((MainActivity) getActivity()).ruta.getDistanceto(location) > ((MainActivity) getActivity()).getValueInPreference("UMBRALDISTANCIA",((MainActivity) getActivity()).UMBRALDISTANCIA)) {
                            if (((MainActivity) getActivity()).fueraderuta && ((MainActivity) getActivity()).numavisos <= ((MainActivity) getActivity()).getValueInPreference("NUMAVISOS",((MainActivity) getActivity()).NUMAVISOS))
                                ((Vibrator) getActivity().getSystemService(getActivity().VIBRATOR_SERVICE)).vibrate(1000);

                            ((MainActivity) getActivity()).fueraderuta = true;
                            ((MainActivity) getActivity()).numavisos++;
                        } else {
                            ((MainActivity) getActivity()).fueraderuta = false;
                            ((MainActivity) getActivity()).numavisos = 0;
                        }
                    }

                }
            }
        });*/
        if (mMap == null) mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                Log.d(LOGTAG, "Map Ready");
                mMap = googleMap;
                mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
                    @Override
                    public boolean onMyLocationButtonClick() {
                        return false;
                    }
                });
                enableMyLocation();
            }
        });



        fabpause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getActivity())
                        .setIcon(R.mipmap.ic_launcher)
                        .setTitle(R.string.app_name)
                        .setMessage(R.string.confirmterminaruta)
                        .setPositiveButton(R.string.si, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Integer route = (((MainActivity) getActivity()).ruta.getId());
                                String tiempotext = (tiempo + "");
                                String distance = (((MainActivity) getActivity()).distanciarecorrida + "");

                                GpsBBDD gpsBBDD = new GpsBBDD(getActivity());
                                gpsBBDD.insertActivity(route, distance, Integer.parseInt(tiempotext), new Date());

                                mMap.clear();

                                ((MainActivity) getActivity()).ruta = null;

                                ((MainActivity) getActivity()).terminarRuta();

                               // ((MainActivity) getActivity()).irActividades();

                            }

                        })
                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                fabpause.setImageDrawable(getResources().getDrawable(R.drawable.ic_stop_white_24dp));
                            }
                        })
                        .show();
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).irListadoRutas();
            }
        });

        if (((MainActivity) getActivity()).rutainiciada){
            ll_configruta.setVisibility(View.GONE);
            ll_playruta.setVisibility(View.VISIBLE);
            fab.setVisibility(View.GONE);
            fabpause.setVisibility(View.VISIBLE);
        }else{
            ll_configruta.setVisibility(View.VISIBLE);
            ll_playruta.setVisibility(View.GONE);
            fabpause.setVisibility(View.GONE);
        }



        if (mParam2.length() > 0 || ((MainActivity) getActivity()).rutainiciada){//Comenzar ruta
            TextView route_name = (TextView) view.findViewById(R.id.route_name);
            route_name.setText(mParam2);
            ArrayList<LatLng> locs = new ArrayList<LatLng>();
            Route ruta = ((MainActivity)getActivity()).ruta;
            for (int i = 0; i < ruta.getTracks().size(); i++){
                String[] aux = ruta.getTracks().get(i).split(",");
                double lng = Double.parseDouble(aux[0]);
                double lat = Double.parseDouble(aux[1]);
                System.out.println("Coords "+lat+"/"+lng);
                locs.add(new LatLng(lat,lng));
            }
            drawPrimaryLinePath(locs);
            if (!((MainActivity) getActivity()).rutainiciada)
                btncomenzar.setVisibility(View.VISIBLE);
        }

        btncomenzar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMap.getMyLocation() != null) {
                    if (((MainActivity) getActivity()).ruta.getDistanceto(mMap.getMyLocation()) > 1000) {
                        Toast.makeText(getActivity(), getResources().getString(R.string.mindistanciaaruta), Toast.LENGTH_LONG).show();
                        return;
                    }

                    ((MainActivity) getActivity()).iniciarRuta();

                    updatetimer = new Timer();
                    updatetimer.scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {
                            if (((MainActivity) getActivity()).rutapausada) {
                                updateTimer(((MainActivity) getActivity()).tiempoenpausa);
                                //tv_tiempo.setText(((MainActivity) getActivity()).timeTostring(((MainActivity) getActivity()).tiempoenpausa*1000));
                            } else {
                                updateTimer(((MainActivity) getActivity()).tiempomovimiento);
                                //tv_tiempo.setText(((MainActivity) getActivity()).timeTostring(((MainActivity) getActivity()).tiempomovimiento*1000));
                            }
                        }
                    }, 1000, 1000);


                    ll_configruta.setVisibility(View.GONE);
                    ll_playruta.setVisibility(View.VISIBLE);

                    fab.setVisibility(View.GONE);
                    btncomenzar.setVisibility(View.GONE);

                    fabpause.setVisibility(View.VISIBLE);
                }
            }
        });



        return view;
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission((AppCompatActivity) getActivity(), LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
        }
    }
    private void updateTimer(final long tiempo){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try{
                    tv_tiempo.setText(((MainActivity) getActivity()).timeTostring(tiempo * 1000));
                }catch (Exception e){}


            }
        });

    }

    private void drawPrimaryLinePath(ArrayList<LatLng> listLocsToDraw)
    {
        if ( mMap == null )
        {
            return;
        }

        if ( listLocsToDraw.size() < 2 )
        {
            return;
        }
        LatLngBounds.Builder b = new LatLngBounds.Builder();
        PolylineOptions options = new PolylineOptions();

        options.color(getResources().getColor(R.color.bluedefault));

        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width=dm.widthPixels;
        System.out.println("DensidityDPI " + dm.densityDpi + " " + dm.density);
        int dpAsPixels = (int) (5*dm.density + 0.5f);
        options.width(dpAsPixels);
        options.visible(true);

        for ( LatLng locRecorded : listLocsToDraw )
        {
            options.add( locRecorded );
            b.include(locRecorded);
        }

        mMap.addPolyline(options);
        System.out.println("Centrar mapa en ruta");
        final LatLngBounds bounds = b.build();


        dpAsPixels = (int) (70*dm.density + 0.5f);
        final int dpAsPixels_f = dpAsPixels;

        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, Math.round(dpAsPixels_f)));
            }
        });



    }



}
