package si.uni_lj.fri.xml2owl.map.application;

import si.uni_lj.fri.xml2owl.map.service.MapService;
import si.uni_lj.fri.xml2owl.map.service.types.*;

/** Stand-alone XMl2OWL application. */
public class MapApplication {

    /** Process the XML2OWL mappings, using an ApplicationDataManager
     * to help with data input and output from the filesystem. *
     * mapping work using the data. */ 
    public static void run (String directory, String source) throws Exception {
     
	System.out.println("[XML2OWL] Beginning of XML2OWL program output." );
	
	MapService service = new MapService();
	MapApplicationDataManager manager = new MapApplicationDataManager(directory, source);

	// rig the request, call the service, and save the owl
	Request request = manager.makeRequest();
	Response response = service.map(request);
	manager.processResponse(response);

	System.out.println("[XML2OWL] End of XML2OWL program output." );

    }

    /** Application entry point, which just calls run(). */
    public static void main (String[] args) throws Exception {
	run(args[0], args[1]);
    }

}
