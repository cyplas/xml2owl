package si.uni_lj.fri.xml2owl.map;

import java.io.File;

import org.apache.commons.io.FileUtils;

import si.uni_lj.fri.xml2owl.util.*;

/** Bridge of Application with data, reading from and writing to files. */
public class MapApplicationDataManager {
    
    /** Prefix path shared by owl, rules and data files. */
    private String commonPath;

    /** Location of OWL file, relative to commonPath. */
    private String owlFile;

    /** Location of rules file, relative to commonPath. */
    private String rulesFile;

    /** Location of XML data file, relative to commonPath. */
    private String dataFile;

    /** Manages accesses to and from the data. */
    private DataManager dataManager;

    /** Constructor. Uses a FileManager implementation. */
    public MapApplicationDataManager(String commonPath, String owlFile, String rulesFile, String dataFile) {
        this.commonPath = commonPath;
        this.owlFile = commonPath + "/" + owlFile;
        this.rulesFile = commonPath + "/" + rulesFile;
        this.dataFile = commonPath + "/" + dataFile;
        dataManager = new FileManager();
    }

    /** Create the request. */ 
    public MapRequest makeRequest() throws Xml2OwlDataException {
	MapRequest request = new MapRequest();
	request.setOwl(dataManager.read(owlFile));
	request.setRules(dataManager.read(rulesFile));
	request.setData(dataManager.read(dataFile));
	return request;
    }
	
    /** Process the response. */  
    public void processResponse(MapResponse response) throws Xml2OwlDataException {
	String owl = response.getOwl();
        dataManager.write(owlFile, owl, true);
    }
}
