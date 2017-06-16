package navdev.gpstrack;


import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import com.google.maps.android.kml.KmlContainer;
import com.google.maps.android.kml.KmlGeometry;
import com.google.maps.android.kml.KmlGroundOverlay;
import com.google.maps.android.kml.KmlLayer;
import com.google.maps.android.kml.KmlMultiGeometry;
import com.google.maps.android.kml.KmlPlacemark;
import com.vincent.filepicker.Constant;
import com.vincent.filepicker.activity.NormalFilePickActivity;
import com.vincent.filepicker.filter.entity.NormalFile;

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

import navdev.gpstrack.dao.GpsBBDD;
import navdev.gpstrack.ent.Route;
import navdev.gpstrack.utils.KMLParser;
import navdev.gpstrack.utils.MapUtils;
import navdev.gpstrack.utils.PermissionUtils;

public class ImportRouteActivity extends AppCompatActivity {

    static String LOGTAG = "ImportRouteActivity";

    private static final int ACCESSFILE_PERMISSION_REQUEST_CODE = 23;

    Route mRutaimportada;


    FloatingActionButton mSavefile;
    TextView mTextfile;
    Button mSavebtn;

    GoogleMap mMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_import_route);

        mSavefile = (FloatingActionButton) findViewById(R.id.first_fab);

        mSavefile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 searchFile();
            }
        });

        mTextfile = (TextView) findViewById(R.id.textFile);
        mSavebtn = (Button) findViewById(R.id.btnSave);
        mSavebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guardarRuta();
            }
        });

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
                MapUtils.configMap(mMap,false);

                if (mRutaimportada != null)
                    MapUtils.drawPrimaryLinePath(mRutaimportada.getTracksLatLng(),mMap,getResources().getColor(R.color.bluedefault));
            }
        });
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
                mRutaimportada = importKMLFromFile(new File(intent.getData().getPath()));
                mostrarRuta();
            }catch (Exception e){
                Toast.makeText(this,"No se pudo importar la ruta",Toast.LENGTH_LONG).show();
            }

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constant.REQUEST_CODE_PICK_FILE){
            if (resultCode == RESULT_OK){
                ArrayList<NormalFile> list = data.getParcelableArrayListExtra(Constant.RESULT_PICK_FILE);
                try{
                    mRutaimportada =importKMLFromFile(new File(list.get(0).getPath()));
                    mostrarRuta();
                }catch (Exception e){e.printStackTrace();}
            }

        }
    }

    private void mostrarRuta(){

        mTextfile.setText(mRutaimportada.getName()+"\n"+mRutaimportada.getDistanceKm());
        mSavebtn.setVisibility(View.VISIBLE);
        if (mMap != null)
            MapUtils.drawPrimaryLinePath(mRutaimportada.getTracksLatLng(),mMap,getResources().getColor(R.color.bluedefault));
    }

    private void guardarRuta(){
        if (mRutaimportada == null) return;
        GpsBBDD gpsBBDD = new GpsBBDD(this);
        long idresult = gpsBBDD.insertRoute(mRutaimportada.getName(), mRutaimportada.getTracks(), mRutaimportada.getImported(), mRutaimportada.getUses(), mRutaimportada.getAdddate());        gpsBBDD.closeDDBB();
        if (idresult > -1){
            mRutaimportada.setId(Integer.parseInt(idresult+""));
            Toast.makeText(this, getResources().getString(R.string.rutaguardada), Toast.LENGTH_LONG).show();

            Intent newintent = new Intent(ImportRouteActivity.this, RoutedetailsActivity.class);
            newintent.putExtra(RoutedetailsActivity.ID_ROUTE,mRutaimportada.getId());
            startActivity(newintent);
            finish();
        }
    }


    private void searchFile(){
        Log.d(LOGTAG,"Buscando  fichero kml");

        if (ContextCompat.checkSelfPermission(ImportRouteActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            PermissionUtils.requestPermission(ImportRouteActivity.this, ACCESSFILE_PERMISSION_REQUEST_CODE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,getString(R.string.permission_rationale_accessfiles), true);
        }else{
            Intent intent = new Intent(ImportRouteActivity.this, NormalFilePickActivity.class);
            intent.putExtra(Constant.MAX_NUMBER, 1);
            intent.putExtra(NormalFilePickActivity.SUFFIX, new String[] {"kml", "KML"});
            startActivityForResult(intent, Constant.REQUEST_CODE_PICK_FILE);
        }
    }


}
