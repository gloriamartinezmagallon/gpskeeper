package navdev.gpstrack.fragment;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import navdev.gpstrack.MainActivity;
import navdev.gpstrack.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class ConfigurationFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    EditText editTextDistancia,editTextAvisos;
    Button guardarButton;
    ImageView navdev;

    public static ConfigurationFragment newInstance(String param1, String param2) {
        ConfigurationFragment fragment = new ConfigurationFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */

    public ConfigurationFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_configuration, container, false);

        guardarButton = (Button) v.findViewById(R.id.guardarButton);
        guardarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int umbraldistancia = Integer.parseInt(editTextDistancia.getText().toString());
                int numavisos = Integer.parseInt(editTextAvisos.getText().toString());

                ((MainActivity) getActivity()).setValueInPreference("UMBRALDISTANCIA",umbraldistancia);
                ((MainActivity) getActivity()).setValueInPreference("NUMAVISOS",numavisos);

                Toast.makeText(getActivity(),R.string.guardarok,Toast.LENGTH_LONG).show();
            }
        });

        navdev = (ImageView) v.findViewById(R.id.navdev);
        navdev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "http://www.navarradeveloper.com";

                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });

        editTextDistancia = (EditText) v.findViewById(R.id.editTextDistancia);
        editTextAvisos = (EditText) v.findViewById(R.id.editTextAvisos);

        editTextDistancia.setText("" + ((MainActivity) getActivity()).getValueInPreference("UMBRALDISTANCIA", ((MainActivity) getActivity()).UMBRALDISTANCIA));
        editTextAvisos.setText(""+((MainActivity) getActivity()).getValueInPreference("NUMAVISOS", ((MainActivity) getActivity()).NUMAVISOS));

        return v;
    }


}
