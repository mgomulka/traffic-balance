package pl.edu.agh.simulator;

import java.io.File;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class FileParser {

	public static final String VEHICLE_ELEM = "vehicle";
	public static final String TIMESTEP_ELEM = "timestep";
	
	public static final String ID_ATTR = "id";
	public static final String LAT_ATTR = "lat";
	public static final String LON_ATTR = "lon";
	public static final String TIME_ATTR = "time";
	
	
	private final LocationDataSender sender;
	private final File file;
	
	public FileParser(File file, LocationDataSender sender) {
		this.file = file;
		this.sender = sender;
			
	}
	
	public void start() {
		
		SAXParserFactory factory = SAXParserFactory.newInstance();
		  try {

		        SAXParser saxParser =  factory.newSAXParser();
		        saxParser.parse(file, new DefaultHandler() {
		        	
		        	private int currTimestep = 0;
		        
		        	public void startElement(String namespaceURI, String localName,
                            String qName, Attributes attrs) throws SAXException {
		        		
		        		
		        		if(qName.equals(VEHICLE_ELEM)) {
		        		
		        			String id = attrs.getValue(ID_ATTR);
		        			double lon = Double.parseDouble(attrs.getValue(LON_ATTR));
		        			double lat = Double.parseDouble(attrs.getValue(LAT_ATTR));
		        			
		        			sender.put(id, lon, lat, currTimestep);
		        			
		        		} else if(qName.equals(TIMESTEP_ELEM)) {
		        			
		        			int time = Double.valueOf(attrs.getValue(TIME_ATTR)).intValue();
		        			currTimestep = time;
		        		}
		        		
		        	}
		        	public void endElement(String namespaceURI, String localName, String qName) {
		        		
		        		if(qName.equals(TIMESTEP_ELEM)) {
		        			sender.tick();
		        		}
		        	}
		        	
		        });

		  } catch (Throwable err) {
		        err.printStackTrace ();
		  }
	}
}
