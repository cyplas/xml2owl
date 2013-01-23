package si.uni_lj.fri.xml2owl.rules;

import si.uni_lj.fri.xml2owl.util.*;

/** Class responsible for storing and accessing rule sets.  For now, uses simple
 * local files. */ 
class RulesApplicationDataManager {

    private String rulesFile;

    /** Manages accesses to and from the data. */
    public DataManager dataManager;

    /** Constructor. Uses a FileManager implementation. */
    public RulesApplicationDataManager(String rulesFile) {
        this.rulesFile = rulesFile;
        dataManager = new FileManager();
    }

    /** Validate a provided ruleset. */   
    public ValidationRequest makeRequest() throws Xml2OwlDataException {
	ValidationRequest request = new ValidationRequest();
	request.setRules(dataManager.read(rulesFile));
	return request;
    }

    /** Report the result of validating a ruleset. */   
    public void processResponse(ValidationResponse response) {
	System.out.println("ValidationResponse: success=" 
                           + response.isSuccess());
    }

}
