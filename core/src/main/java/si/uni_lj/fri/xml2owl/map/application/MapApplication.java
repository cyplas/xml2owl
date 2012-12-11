package si.uni_lj.fri.xml2owl.map.application;

import si.uni_lj.fri.xml2owl.map.service.MapService;
import si.uni_lj.fri.xml2owl.map.service.types.*;

/** Wraps MapService so that it can be run and tested as a stand-alone
 *  application, without relying on web service infrastructure. */
public class MapApplication {

    /** Process the XML2OWL mappings, using an ApplicationDataManager to help
     * with data input and output, and a TranslateD2OImpl service to do the
     * mapping work using the data. */ 
    public static void run (String directory, String source) throws Exception {
     
	System.out.println
	    ("                      *** Xml2owl: begin output ***\n\n" );
	
	MapService service = new MapService();
	MapApplicationDataManager manager = new MapApplicationDataManager(directory, source);

	// rig the request, call the service, and possibly save the owl
	Request request = manager.makeRequest();
	Response response = service.map(request);
	manager.processResponse(response);

	System.out.println
	    ("\n\n                      *** Xml2owl:  end output  ***" );

    }

    /** Application entry point, which just calls run(). */
    public static void main (String[] args) throws Exception {
	run(args[0], args[1]);
    }

}
