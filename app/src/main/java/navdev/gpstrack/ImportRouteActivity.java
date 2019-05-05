package navdev.gpstrack;


import android.Manifest;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import navdev.gpstrack.dao.GpsBBDD;
import navdev.gpstrack.ent.Route;
import navdev.gpstrack.utils.MapUtils;
import navdev.gpstrack.utils.PermissionUtils;

public class ImportRouteActivity extends AppCompatActivity {

    static String LOGTAG = "ImportRouteActivity";

    private static final int ACCESSFILE_PERMISSION_REQUEST_CODE = 23;
    private static final int REQUEST_CODE_PICK_FILE = 1024;

    Route mRutaimportada;


    FloatingActionButton mSavefile;
    TextView mTextfile;
    Button mSavebtn;

    GoogleMap mMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_import_route);

        mSavefile = findViewById(R.id.first_fab);

        mSavefile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 searchFile();
            }
        });

        mTextfile = findViewById(R.id.textFile);
        mSavebtn = findViewById(R.id.btnSave);
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
                MapUtils.configMap(mMap,false,ImportRouteActivity.this);

                if (mRutaimportada != null)
                    MapUtils.drawPrimaryLinePath(mRutaimportada.getTracksLatLng(),mMap,getResources().getColor(R.color.bluedefault));
            }
        });
    }


        ///IMPORTAR KML FROM FILE
    public Route importKMLFromFile(Uri uri){
        try {
            InputStreamReader fin = new InputStreamReader(getContentResolver().openInputStream(uri));

            /*if (!fl.isFile()){
                Log.d(LOGTAG,"No es fichero");
            }else{
                Log.d(LOGTAG,fl.canRead()+" "+fl.getName());
            }
            FileInputStream fin = new FileInputStream(fl);*/


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
        String name = nodeList.item(0).getTextContent();

        nodeList = doc.getElementsByTagName("coordinates");
        ArrayList coords = new ArrayList();

        for (int i = 0; i < nodeList.getLength(); i++) {
            String[] coordenadas = nodeList.item(i).getTextContent().split(" ");
            for (String coordenada : coordenadas) {
                if (coordenada.split(",").length <= 2) continue;
                coords.add(coordenada);
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


    /*@Override
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
*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_PICK_FILE && resultCode == RESULT_OK){
            try{
                mRutaimportada = importKMLFromFile(data.getData());
                mostrarRuta();
            }catch (Exception e){
                e.printStackTrace();
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
        long idresult = gpsBBDD.insertRoute(mRutaimportada.getName(), mRutaimportada.getTracks(), mRutaimportada.getImported(), mRutaimportada.getUses(), mRutaimportada.getAdddate());
        gpsBBDD.closeDDBB();
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
           Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
           intent.addCategory(Intent.CATEGORY_OPENABLE);
           intent.setType("*/*");
           startActivityForResult(Intent.createChooser(intent, "Select a File to Upload"), REQUEST_CODE_PICK_FILE);
        }
    }


    private String getPathFile(Context context, Uri uri) {
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        return uri.getPath();
        // DocumentProvider
        /*if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else
            if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {split[1]};
                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
        */
    }

    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = { column };
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }
}
