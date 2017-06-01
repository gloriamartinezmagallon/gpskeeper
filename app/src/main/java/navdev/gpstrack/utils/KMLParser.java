package navdev.gpstrack.utils;

import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.io.StringReader;
import java.util.List;

import javax.xml.parsers.SAXParserFactory;

/**
 * Created by gloria on 19/06/2015.
 */
public class KMLParser {


    public static String getName(InputStream is) {
        String  name = null;
        try {
            // create a XMLReader from SAXParser
            XMLReader xmlReader = SAXParserFactory.newInstance().newSAXParser()
                    .getXMLReader();
            // create a SAXCoordsHandler
            SAXNameHandler saxHandler = new SAXNameHandler();
            // store handler in XMLReader
            xmlReader.setContentHandler(saxHandler);
            // the process starts
            xmlReader.parse(new InputSource(is));
            // get the `Employee list`
            name = saxHandler.getName();

        } catch (Exception ex) {
            Log.d("XML", "SAXXMLParser: parse() failed");
        }

        // return coords list
        return name;
    }
    public static String getName(String texto){
        String name = "";
        try{
//            Document doc = Jsoup.parse(texto, "", Parser.xmlParser());
//
//            for(Element e : doc.select("name")) {
//                // the contents
//                name = (e.text());
//            }
            XMLReader xmlReader = SAXParserFactory.newInstance().newSAXParser()
                    .getXMLReader();
            // create a SAXNameHandler
            SAXNameHandler saxHandler = new SAXNameHandler();
            // store handler in XMLReader
            xmlReader.setContentHandler(saxHandler);
            // the process starts

            InputSource inputSource = new InputSource();
            inputSource.setEncoding("ISO-8859-1");
            inputSource.setCharacterStream(new StringReader(texto));
            xmlReader.parse(inputSource);
            // get the `Employee list`
            name = saxHandler.getName();
        }catch (Exception e){
            System.out.println("getName exception " + e.getMessage());
            e.printStackTrace();
        }
        return name;
    }

    public static List getPoints(String texto){
        List points = null;
        try{
//            Document doc = Jsoup.parse(texto, "", Parser.xmlParser());
//
//            for(Element e : doc.select("LineString").select("coordinates")) {
//                // the contents
//                String[] puntos = e.text().split(" ");
//                for (int i = 0; i < puntos.length; i++) {
//                    points.add(puntos[i]);
//                }
//
//            }
            XMLReader xmlReader = SAXParserFactory.newInstance().newSAXParser()
                    .getXMLReader();
            // create a SAXCoordsHandler
            SAXCoordsHandler saxHandler = new SAXCoordsHandler();
            // store handler in XMLReader
            xmlReader.setContentHandler(saxHandler);
            // the process starts
            InputSource inputSource = new InputSource();
            inputSource.setEncoding("ISO-8859-1");
            inputSource.setCharacterStream(new StringReader(texto));
            xmlReader.parse(inputSource);
            // get the `Employee list`
            points = saxHandler.getPoints();
        }catch (Exception e){
            System.out.println("getPoints exception " + e.getMessage());
            e.printStackTrace();
        }
        return points;

    }

    public static List getPoints(StringBuilder stringBuilder){
        List points = null;
        try {
            // create a XMLReader from SAXParser
            XMLReader xmlReader = SAXParserFactory.newInstance().newSAXParser()
                    .getXMLReader();
            // create a SAXCoordsHandler
            SAXCoordsHandler saxHandler = new SAXCoordsHandler();
            // store handler in XMLReader
            xmlReader.setContentHandler(saxHandler);
            // the process starts
            InputSource is = new InputSource(new StringReader(stringBuilder.toString()));
            xmlReader.parse(is);
            // get the `Employee list`
            points = saxHandler.getPoints();

        } catch (Exception ex) {
            Log.d("XML", "SAXXMLParser: parse() failed");
        }

        // return coords list
        return points;
    }

    public static List getPoints(InputStream is) {
        List points = null;
        try {
            // create a XMLReader from SAXParser
            XMLReader xmlReader = SAXParserFactory.newInstance().newSAXParser()
                    .getXMLReader();
            // create a SAXCoordsHandler
            SAXCoordsHandler saxHandler = new SAXCoordsHandler();
            // store handler in XMLReader
            xmlReader.setContentHandler(saxHandler);
            // the process starts
            xmlReader.parse(new InputSource(is));
            // get the `Employee list`
            points = saxHandler.getPoints();

        } catch (Exception ex) {
            Log.d("XML", "SAXXMLParser: parse() failed");
        }

        // return coords list
        return points;
    }


}
