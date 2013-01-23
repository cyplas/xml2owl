package si.uni_lj.fri.xml2owl.map;

/** Stand-alone XMl2OWL application. */
public class MapApplication {

    /** Process the XML2OWL mappings, using a
     * MapApplicationDataManager to help with data input and output
     * from the filesystem. mapping work using the data. */ 
    public static void run (String commonPath, String owlFile, String rulesFile, String dataFile) throws Exception {
     
	System.out.println("[XML2OWL] Beginning of XML2OWL mapping program output." );
	
	MapService service = new MapService();
	MapApplicationDataManager manager = 
            new MapApplicationDataManager(commonPath, owlFile, rulesFile, dataFile);

	// rig the request, call the service, and save the owl
	MapRequest request = manager.makeRequest();
	MapResponse response = service.map(request);
	manager.processResponse(response);

	System.out.println("[XML2OWL] End of XML2OWL mapping program output." );

    }

    /** Application entry point, which just calls run(). */
    public static void main (String[] args) throws Exception {
	run(args[0], args[1], args[2], args[3]);
    }

}
