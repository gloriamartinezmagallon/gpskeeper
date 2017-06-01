package navdev.gpstrack.utils;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Created by gloria on 19/06/2015.
 */
public class SAXNameHandler extends DefaultHandler {

    private String name;
    private String tempVal;
    private String tempName;

    public SAXNameHandler() {
        name = "";
    }

    public String getName() {
        return name;
    }

    // Event Handlers
    public void startElement(String uri, String localName, String qName,
                             Attributes attributes) throws SAXException {
        // reset
        tempVal = "";
        if (qName.equalsIgnoreCase("name")) {
            // create a new instance of employee
            tempName = null;
        }
    }

    public void characters(char[] ch, int start, int length)
            throws SAXException {
        tempVal = new String(ch, start, length);
    }

    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        if (qName.equalsIgnoreCase("name")) {
            // add it to the list
            name = tempVal;
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
