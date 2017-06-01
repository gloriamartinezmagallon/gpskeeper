package navdev.gpstrack.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;
import com.melnykov.fab.FloatingActionButton;

import java.util.ArrayList;

import navdev.gpstrack.MainActivity;
import navdev.gpstrack.R;
import navdev.gpstrack.dao.GpsBBDD;
import navdev.gpstrack.ent.Route;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link RoutedetailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RoutedetailsFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private Route ruta = null;
    GoogleMap map;


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RoutedetailsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RoutedetailsFragment newInstance(String param1, String param2) {
        RoutedetailsFragment fragment = new RoutedetailsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public RoutedetailsFragment() {
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
        View view =  inflater.inflate(R.layout.fragment_routedetails, container, false);

        ruta = ((MainActivity) getActivity()).ruta;
        if (ruta == null){
            Toast.makeText(getActivity(),"No se puede mostrar la ruta",Toast.LENGTH_LONG).show();
            getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
        }
        final EditText route_name = (EditText) view.findViewById(R.id.route_name);
        route_name.setText(ruta.getName());

        TextView route_distancia = (TextView) view.findViewById(R.id.route_distancia);
        route_distancia.setText(ruta.getDistance()+" m");

        SupportMapFragment mapView = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);

        map = mapView.getMap();

        ArrayList<LatLng> locs = new ArrayList<LatLng>();
        for (int i = 0; i < ruta.getTracks().size(); i++){
            String[] aux = ruta.getTracks().get(i).split(",");
            double lng = Double.parseDouble(aux[0]);
            double lat = Double.parseDouble(aux[1]);
            System.out.println("Coords "+lat+"/"+lng);
            locs.add(new LatLng(lat,lng));
        }
        drawPrimaryLinePath(locs);

        final TextView btncomenzar = (TextView) view.findViewById(R.id.btncomenzar);
        if (ruta.getId() == 0) btncomenzar.setVisibility(View.GONE);
        btncomenzar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).comenzarRuta(ruta);
            }
        });

        FloatingActionButton fabsave = (FloatingActionButton) view.findViewById(R.id.fabsave);
        fabsave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ruta.setName(route_name.getText().toString());
                GpsBBDD gpsBBDD = new GpsBBDD(getActivity());
                if (ruta.getId() > 0) {
                    gpsBBDD.updateRoute(ruta.getId(), ruta.getName(), ruta.getTracks(), ruta.getImported(), ruta.getUses(), ruta.getAdddate());
                } else {
                    long idresult = gpsBBDD.insertRoute(ruta.getName(), ruta.getTracks(), ruta.getImported(), ruta.getUses(), ruta.getAdddate());;
                    if (idresult > -1){
                        ruta.setId(Integer.parseInt(idresult+""));
                        Toast.makeText(getActivity(), getResources().getString(R.string.rutaguardada), Toast.LENGTH_LONG).show();
                        btncomenzar.setVisibility(View.VISIBLE);
                    }

                }

                gpsBBDD.closeDDBB();
            }
        });

        FloatingActionButton fabtrash = (FloatingActionButton) view.findViewById(R.id.fabtrash);
        if (ruta.getId() > 0) fabtrash.setVisibility(View.VISIBLE);
        fabtrash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getActivity())
                        .setIcon(R.drawable.ic_delete_black_24dp)
                        .setTitle(R.string.borrarruta)
                        .setMessage(R.string.borrarrutaconfirm)
                        .setPositiveButton(R.string.si, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                GpsBBDD gpsBBDD = new GpsBBDD(getActivity());

                                gpsBBDD.deleteRoute(ruta.getId());

                                Toast.makeText(getActivity(), getResources().getString(R.string.rutaborrada), Toast.LENGTH_LONG).show();
                                gpsBBDD.closeDDBB();
                                ((MainActivity) getActivity()).irListadoRutas();
                            }

                        })
                        .setNegativeButton(R.string.no, null)
                        .show();

            }
        });



        return view;
    }

    private void drawPrimaryLinePath(ArrayList<LatLng> listLocsToDraw)
    {
        if ( map == null )
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

        map.addPolyline(options);
        System.out.println("Centrar mapa en ruta");
        final LatLngBounds bounds = b.build();


        dpAsPixels = (int) (70*dm.density + 0.5f);
        final int dpAsPixels_f = dpAsPixels;

        map.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, Math.round(dpAsPixels_f)));
            }
        });



    }


}
