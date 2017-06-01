package navdev.gpstrack.utils;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gloria on 19/06/2015.
 */
public class SAXCoordsHandler extends DefaultHandler {

    private List points;
    private String tempVal;
    private String tempPoint;

    public SAXCoordsHandler() {
        points = new ArrayList();
    }

    public List getPoints() {
        return points;
    }

    // Event Handlers
    public void startElement(String uri, String localName, String qName,
                             Attributes attributes) throws SAXException {
        // reset
        tempVal = "";
        if (qName.equalsIgnoreCase("Point")  || qName.equalsIgnoreCase("LineString")) {
            // create a new instance of employee
            tempPoint = null;
        }
    }

    public void characters(char[] ch, int start, int length)
            throws SAXException {
        tempVal = new String(ch, start, length);
    }

    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        if (qName.equalsIgnoreCase("Point") || qName.equalsIgnoreCase("LineString")) {
            // add it to the list
            points.add(tempPoint);
        } else if (qName.equalsIgnoreCase("coordinates")) {
            tempPoint = tempVal;
        }
    }

    public void startDocument() throws SAXException
    {
        System.out.println("start of the document   : ");
    }

    public void endDocument() throws SAXException
    {
        System.out.println("end of the document document     : ");
    }
}
