package navdev.gpstrack.fragment;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.melnykov.fab.FloatingActionButton;

import org.xml.sax.InputSource;

import java.io.File;
import java.io.FileFilter;
import java.net.URL;
import java.util.ArrayList;

import navdev.gpstrack.MainActivity;
import navdev.gpstrack.R;
import navdev.gpstrack.ent.Route;

/**
 * A fragment with a Google +1 button.
 * Use the {@link ImportRouteFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ImportRouteFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    ProgressDialog barProgressDialog;

    Route rutaimportada;

    FileFilter filter = new FileFilter() {

        @Override
        public boolean accept(File arg0) {
            return arg0.getName().endsWith(".kml") || arg0.isDirectory();
        }

    };


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ImportRouteFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ImportRouteFragment newInstance(String param1, String param2) {
        ImportRouteFragment fragment = new ImportRouteFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public ImportRouteFragment() {
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
        View view = inflater.inflate(R.layout.fragment_import_route, container, false);

        FloatingActionButton first_fab = (FloatingActionButton) view.findViewById(R.id.first_fab);
        FloatingActionButton second_fab = (FloatingActionButton) view.findViewById(R.id.second_fab);

        final EditText urledittext = (EditText) view.findViewById(R.id.urleditext);

        first_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    System.out.println("Buscando rutas");
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("file/*.kml");
                ((MainActivity) getActivity()).startActivityForResult(intent, ((MainActivity) getActivity()).FILESELECTCODE);
                    /*barProgressDialog= ProgressDialog.show(getActivity(),getResources().getString(R.string.app_name),getResources().getString(R.string.buscandoruas),true,false);

                    ArrayList<File> externalfiles = listFiles(Environment.getExternalStorageDirectory().getAbsolutePath());
                    ArrayList<File> internalfiles = listFiles(Environment.getDataDirectory().getAbsolutePath());

                    if (externalfiles.size() >0){
                        for(int i = 0; i < externalfiles.size(); i++){
                            System.out.println(externalfiles.get(i).getAbsolutePath());
                        }
                    }

                    System.out.println("Files: " + externalfiles.size() + " " + internalfiles.size());

                    barProgressDialog.hide();

                    final ArrayList<String> filesname = new ArrayList<String>();
                    final ArrayList<File> files = new ArrayList<File>();
                    for (int i = 0; i < externalfiles.size(); i++){
                        filesname.add(externalfiles.get(i).getName());
                        files.add(externalfiles.get(i));
                    }
                    for (int i = 0; i < internalfiles.size(); i++){
                        filesname.add(internalfiles.get(i).getName());
                        files.add(internalfiles.get(i));
                    }
                    if (filesname.size() > 0){

                        final String[] filenombres = new String[filesname.size()];
                        for (int i = 0; i<filesname.size();i++){

                            filenombres[i]= filesname.get(i);
                        }


                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                        builder.setTitle(R.string.rutasencontradas)
                                .setItems(filenombres, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        rutaimportada = importKMLFromFile(files.get(which));
                                        if (rutaimportada == null){
                                            Toast.makeText(getActivity(),"No se pudo importar la ruta",Toast.LENGTH_LONG).show();
                                        }else{
                                            ((MainActivity) getActivity()).irDetallesruta(rutaimportada);
                                        }
                                    }
                                })
                                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });

                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }else{
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                        builder.setMessage(getResources().getString(R.string.rutasnoencontradas))
                                .setTitle(R.string.buscandoruas)
                                .setPositiveButton(R.string.si, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        dialog.dismiss();
                                    }
                                });

                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }*/


            }
        });

        second_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (urledittext.getText().toString().length() == 0) {
                    Toast.makeText(getActivity(), getResources().getString(R.string.indiqueurl), Toast.LENGTH_LONG).show();
                    return;
                }

                barProgressDialog = new ProgressDialog(getActivity());
                barProgressDialog.setTitle(getResources().getString(R.string.descargando));
                barProgressDialog.setMessage(getResources().getString(R.string.espere));
                barProgressDialog.setIndeterminate(true);
                barProgressDialog.show();

                final DownloadFile downloadFile = new DownloadFile(getActivity());
                downloadFile.execute(urledittext.getText().toString());

                barProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        downloadFile.cancel(true);
                    }
                });
            }
        });

        return view;
    }
    private ArrayList<File> filter(ArrayList<File> files) {
        ArrayList<File> result = new ArrayList<File>();

        for (int i = 0; i < files.size(); i++) {
            if (files.get(i).isDirectory()) {
                File file = files.get(i);
                files.remove(i);
                File[] files2 = file.listFiles(filter);
                if (files2 == null)
                    continue;
                ArrayList<File> f = new ArrayList<File>();
                for (int j = 0; j < files2.length; j++) {
                    f.add(files2[j]);
                }
                result.addAll(listFiles(f));
            } else {
                result.add(files.get(i));
            }
        }

        return result;
    }
    private ArrayList<File> listFiles(String folder) {
        ArrayList<File> files = new ArrayList<File>();
        files.add(new File(folder));
        return listFiles(files);
    }
    private ArrayList<File> listFiles(ArrayList<File> files) {
        ArrayList<File> result = new ArrayList<File>();

        for (int i = 0; i < files.size(); i++) {
            if (files.get(i).isDirectory()) {
                File file = files.get(i);
                files.remove(i);
                File[] files2 = file.listFiles(filter);
                if (files2 == null)
                    continue;
                ArrayList<File> f = new ArrayList<File>();
                for (int j = 0; j < files2.length; j++) {
                    f.add(files2[j]);
                }
                result.addAll(listFiles(f));
            } else {
                result.add(files.get(i));
            }
        }

        return result;
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    private class DownloadFile extends AsyncTask<String, Integer, String> {

        private Context context;

        public DownloadFile(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(String... sUrl) {

            try {
                String web = sUrl[0];
                if (!web.contains("http://") && !web.contains("https://")) web = "http://"+web;

                rutaimportada = parserKMLfromurl(web);
                return null;

            } catch (Exception e) {
                return e.getMessage();
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // take CPU lock to prevent CPU from going off if the user
            // presses the power button during download
            barProgressDialog.show();
            rutaimportada = null;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            // if we get here, length is known, now set indeterminate to false
            barProgressDialog.setIndeterminate(true);
        }

        @Override
        protected void onPostExecute(String result) {
            barProgressDialog.dismiss();
            if (rutaimportada == null){
                Toast.makeText(context,"Download error: "+result, Toast.LENGTH_LONG).show();
                //System.out.println(result);
            }
            else{
                ((MainActivity) getActivity()).irDetallesruta(rutaimportada);

            }

        }
    }



    public Route parserKMLfromurl(String urls){
        try {
            URL url = new URL(urls);
            return ((MainActivity)getActivity()).parseKML(new InputSource(url.openStream()));

        }catch (Exception e){
            System.out.println(e.getMessage());
        }
        return null;
    }


}
