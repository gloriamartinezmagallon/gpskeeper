package navdev.gpstrack;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import navdev.gpstrack.ent.Route;
import navdev.gpstrack.fragment.MapFragment;

public class MainActivity extends AppCompatActivity {

    static String LOGTAG ="MainActivity";
    int FRAGMENTMAP = 0;
    int FRAGMENTROUTE = 1;
    int FRAGMENTACTIVITY = 2;
    int FRAGMENTCONFIGURATION = 3;

    public int UMBRALDISTANCIA = 10;
    public int NUMAVISOS = 4;

    public int FILESELECTCODE = 99;

    public Route ruta = null;

    public boolean rutainiciada = false;
    public long distanciarecorrida = 0;
    public Timer timermovimiento;
    public Location lastlocation = null;
    public boolean fueraderuta = false;
    public int numavisos = 0;

    public boolean rutapausada = false;

    public long tiempomovimiento;
    public long tiempoenpausa;

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
        Intent intent = getIntent();

        if(Intent.ACTION_VIEW.equals(intent.getAction()) || Intent.ACTION_PICK.equals(intent.getAction())){
            try{
                Route rutaimportada = importKMLFromFile(new File(intent.getData().getPath()));
                irDetallesruta(rutaimportada);
            }catch (Exception e){
                Log.e(LOGTAG,e.getMessage());
                Toast.makeText(this,"No se pudo importar la ruta",Toast.LENGTH_LONG).show();
            }
        }
    }


    @Override
    public void startActivityForResult(Intent intent, int requestCode, Bundle options) {
        super.startActivityForResult(intent, requestCode, options);

    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = getIntent();
        if(Intent.ACTION_VIEW.equals(intent.getAction())){
            try{
                Route rutaimportada = importKMLFromFile(new File(intent.getData().getPath()));
                irDetallesruta(rutaimportada);
            }catch (Exception e){
                Toast.makeText(this,"No se pudo importar la ruta",Toast.LENGTH_LONG).show();
            }

        }
    }

    public String timeTostring(Long tiempo){
        int seconds = (int) ((tiempo / (1000)) % 60);
        int minutes = (int) ((tiempo / (1000 * 60)) % 60);
        int hours = (int) ((tiempo / (1000 * 60 * 60)) % 24);

        String txseconds, txminutes, txhours;
        if (seconds<10) txseconds="0"+seconds;
        else txseconds=seconds+"";

        if (minutes<10) txminutes="0"+minutes;
        else txminutes=minutes+"";

        if (hours<10) txhours="0"+hours;
        else txhours=hours+"";

        return txhours+":"+txminutes+":"+txseconds;
    }



    public void initFragmentImportRoute(){
        /*FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .add(R.id.container, ImportRouteFragment.newInstance("importroutefragment", "importroutefragment"))
                .addToBackStack("ImportRouteFragment")
                .commit();*/
    }

    public void irDetallesruta(Route rutaseleccionada){

    }

    public void irListadoRutas(){

       /* FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .add(R.id.container, RoutesFragment.newInstance("routes", "routes"))
                .addToBackStack("RoutesFragment")
                .commit();*/
    }



    public void comenzarRuta(Route rutaseleccionada){
        ruta = rutaseleccionada;
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .add(R.id.container, MapFragment.newInstance("mapa", ruta.getName()))
                .addToBackStack("MapFragment")
                .commit();
    }

    public void iniciarRuta(){
        rutainiciada = true;
        timermovimiento = new Timer();
        timermovimiento.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (rutapausada){
                    tiempoenpausa++;
                }else{
                    tiempomovimiento++;
                }
            }
        }, 1000, 1000);
        distanciarecorrida = 0;
        lastlocation = null;
        fueraderuta = false;
        numavisos = 0;
    }

    public void terminarRuta(){
        rutainiciada = false;
        timermovimiento.cancel();
        distanciarecorrida = 0;
        lastlocation = null;
        fueraderuta = false;
        numavisos = 0;
    }


    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section2);
                break;
            case 4:
                mTitle = getString(R.string.title_section3);
                break;
        }
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


    public void setValueInPreference(String key, int newHighScore){
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(key, newHighScore);
        editor.commit();
    }

    public int getValueInPreference(String key, int defaultValue){
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        return sharedPref.getInt(key, defaultValue);
    }



}
