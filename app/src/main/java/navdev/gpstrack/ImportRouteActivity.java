package navdev.gpstrack;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;


import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import navdev.gpstrack.ent.Route;

public class ImportRouteActivity extends AppCompatActivity {

    static String LOGTAG = "ImportRouteActivity";
    public int FILESELECTCODE = 99;

    ProgressDialog barProgressDialog;
    Route rutaimportada;

    FileFilter filter = new FileFilter() {

        @Override
        public boolean accept(File arg0) {
            return arg0.getName().endsWith(".kml") || arg0.isDirectory();
        }

    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_import_route);

        FloatingActionButton first_fab = (FloatingActionButton) findViewById(R.id.first_fab);
        FloatingActionButton second_fab = (FloatingActionButton) findViewById(R.id.second_fab);

        final EditText urledittext = (EditText) findViewById(R.id.urleditext);

        first_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    System.out.println("Buscando rutas");
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("file/*.kml");
                startActivityForResult(intent, FILESELECTCODE);

            }
        });

        second_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (urledittext.getText().toString().length() == 0) {
                    Toast.makeText(ImportRouteActivity.this, getResources().getString(R.string.indiqueurl), Toast.LENGTH_LONG).show();
                    return;
                }

                barProgressDialog = new ProgressDialog(ImportRouteActivity.this);
                barProgressDialog.setTitle(getResources().getString(R.string.descargando));
                barProgressDialog.setMessage(getResources().getString(R.string.espere));
                barProgressDialog.setIndeterminate(true);
                barProgressDialog.show();

                final DownloadFile downloadFile = new DownloadFile(ImportRouteActivity.this);
                downloadFile.execute(urledittext.getText().toString());

                barProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        downloadFile.cancel(true);
                    }
                });
            }
        });

        Intent intent = getIntent();

        if(Intent.ACTION_VIEW.equals(intent.getAction()) || Intent.ACTION_PICK.equals(intent.getAction())){
            try{
                Route rutaimportada = importKMLFromFile(new File(intent.getData().getPath()));
                //irDetallesruta(rutaimportada);
            }catch (Exception e){
                Log.e(LOGTAG,e.getMessage());
                Toast.makeText(this,"No se pudo importar la ruta",Toast.LENGTH_LONG).show();
            }
        }
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
               // (ImportRouteActivity.this).irDetallesruta(rutaimportada);

            }

        }
    }



    public Route parserKMLfromurl(String urls){
        try {
            URL url = new URL(urls);
            return parseKML(new InputSource(url.openStream()));

        }catch (Exception e){
            System.out.println(e.getMessage());
        }
        return null;
    }


    ///IMPORTAR KML FROM FILE
    public Route importKMLFromFile(File fl){
        try {
            if (fl.isFile() == false){

                Log.d(LOGTAG,"No es fichero");
            }else{
                Log.d(LOGTAG,fl.canRead()+" "+fl.getName());
            }
            FileInputStream fin = new FileInputStream(fl);

            Route ruta = parseKML(new InputSource(fin));

            fin.close();

            return ruta;
        }catch (Exception e){
            Log.d(LOGTAG,e.getMessage());
        }
        return null;
    }

    public Route parseKML(InputSource inputSource) throws Exception{
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(inputSource);
        doc.getDocumentElement().normalize();

        NodeList nodeList = doc.getElementsByTagName("name");
        String name = "";
        for (int i = 0; i < nodeList.getLength(); i++) {
            name = nodeList.item(i).getTextContent();
            break;
        }

        nodeList = doc.getElementsByTagName("coordinates");
        ArrayList<String> coords = new ArrayList();

        for (int i = 0; i < nodeList.getLength(); i++) {
            String[] coordenadas = nodeList.item(i).getTextContent().split(" ");
            for(int j = 0; j<coordenadas.length; j++){
                if (coordenadas[j].split(",").length <= 2) continue;
                coords.add(coordenadas[j]);
            }
        }

        Route route = new Route();
        route.setName(name);
        route.setAdddate(new Date());
        route.setUses(0);
        route.setImported(1);
        route.setTracks(coords);

        return route;
    }


    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = getIntent();
        if(Intent.ACTION_VIEW.equals(intent.getAction())){
            try{
                Route rutaimportada = importKMLFromFile(new File(intent.getData().getPath()));
                //irDetallesruta(rutaimportada);
            }catch (Exception e){
                Toast.makeText(this,"No se pudo importar la ruta",Toast.LENGTH_LONG).show();
            }

        }
    }
}
