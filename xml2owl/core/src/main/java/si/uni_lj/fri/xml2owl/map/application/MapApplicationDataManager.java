package si.uni_lj.fri.xml2owl.map.application;

import org.apache.commons.io.FileUtils;
import java.io.File;
import si.uni_lj.fri.xml2owl.map.service.types.*;
import si.uni_lj.fri.xml2owl.util.*;

/** Bridge of Application with data, reading from and writing to files. */
public class MapApplicationDataManager {
    
    /** Directory with the OWL ontology (owl.xml) to read and update. */
    private String ontologyDirectory;

    /** Directory of an XML source (xml.xml) and corresponding rules
        file (rules.xml). */
    private String sourceDirectory;

    /** Manages accesses to and from the data. */
    private DataManager dataManager;

    /** Constructor. Uses a FileManager implementation. */
    public MapApplicationDataManager(String ontologyDirectory, 
                                     String sourceDirectory) {
        this.ontologyDirectory = ontologyDirectory;
        this.sourceDirectory = sourceDirectory;
        dataManager = new FileManager(ontologyDirectory, ".xml");
    }

    /** Create a Request, based on xml files in ontologyDirectory and sourceDirectory. */ 
    public Request makeRequest() throws Xml2OwlDataException {
	Request request = new Request();
	request.setData(dataManager.read(sourceDirectory + "/xml"));
	request.setRules(dataManager.read(sourceDirectory + "/rules"));
	request.setOwl(dataManager.read("owl"));
	return request;
    }
	
    /** Process a Response, writing to owl.xml if successful. */  
    public void processResponse(Response response) throws Xml2OwlDataException {
	String owl = response.getOwl();
        dataManager.write("owl", owl, true);
    }
}
