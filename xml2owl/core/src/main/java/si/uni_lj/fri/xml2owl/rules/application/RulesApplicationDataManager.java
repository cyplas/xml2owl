package si.uni_lj.fri.xml2owl.rules.application;

import si.uni_lj.fri.xml2owl.util.*;
import si.uni_lj.fri.xml2owl.rules.service.types.*;

/** Class responsible for storing and accessing rule sets.  For now, uses simple
 * local files. */ 
class RulesApplicationDataManager {

    /** The FileManager used to get the input for some of the operations. */ 
    public static final DataManager dataManager = 
	new FileManager("test/rules", ".xml");

    /** Add a ruleset. */   
    public AddRequest makeAddRequest() throws Xml2OwlDataException {
	AddRequest request = new AddRequest();
	request.setName("add");
	request.setRules(dataManager.read("add"));
	request.setOverride(true);
	return request;
    }

    /** Delete a ruleset. */   
    public DeleteRequest makeDeleteRequest() {
	DeleteRequest request = new DeleteRequest();
	request.setName("delete");
	return request;
    }

    /** Get a ruleset. */   
    public GetRequest makeGetRequest() {
	GetRequest request = new GetRequest();
	request.setName("get");
	return request;
    }

    /** Validate a stored ruleset. */   
    public ValidateByNameRequest makeValidateByNameRequest() {
	ValidateByNameRequest request = new ValidateByNameRequest();
	request.setName("validateByName");
	return request;
    }

    /** Validate a provided ruleset. */   
    public ValidateFullRequest makeValidateFullRequest() 
        throws Xml2OwlDataException {
	ValidateFullRequest request = new ValidateFullRequest();
	request.setRules(dataManager.read("validateFull"));
	return request;
    }

    /** Report the result of adding a ruleset. */   
    public void processAddResponse(AddResponse response) {
	System.out.println("AddResponse: success=" + response.isSuccess());
    }

    /** Report the result of deleting a ruleset. */   
    public void processDeleteResponse(DeleteResponse response) {
	System.out.println("DeleteResponse: success=" + response.isSuccess());
    }

    /** Report the result of getting a ruleset. */   
    public void processGetResponse(GetResponse response) {
	System.out.println("GetResponse: rules=" + response.getRules());
    }

    /** Report the result of validating a stored ruleset. */   
    public void processValidateByNameResponse(ValidateByNameResponse response) {
	System.out.println("ValidateByNameResponse: success=" + 
                           response.isSuccess());
    }

    /** Report the result of validating a provided ruleset. */   
    public void processValidateFullResponse(ValidateFullResponse response) {
	System.out.println("ValidateFullResponse: success=" 
                           + response.isSuccess());
    }

}
