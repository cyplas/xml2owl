package si.uni_lj.fri.xml2owl.rules.application;

import si.uni_lj.fri.xml2owl.rules.service.RulesService;
import si.uni_lj.fri.xml2owl.rules.service.types.*;

/** Application used for testing the rules service from the command-line. */
public class RulesApplication {

    /** Test out the different operations offered by the rules web service. */ 
    public static void run () throws Exception {

	System.out.println
	    ("                      *** Xml2owl rules: begin output ***\n\n" );
	
	RulesService service = new RulesService();
	RulesApplicationDataManager manager = new RulesApplicationDataManager();

	GetRequest getRequest = manager.makeGetRequest();
	GetResponse getResponse = service.getRules(getRequest);
	manager.processGetResponse(getResponse);

	AddRequest addRequest = manager.makeAddRequest();
	AddResponse addResponse = service.addRules(addRequest);
	manager.processAddResponse(addResponse);

	DeleteRequest deleteRequest = manager.makeDeleteRequest();
	DeleteResponse deleteResponse = service.deleteRules(deleteRequest);
	manager.processDeleteResponse(deleteResponse);

	ValidateByNameRequest validateByNameRequest = 
            manager.makeValidateByNameRequest();
	ValidateByNameResponse validateByNameResponse = 
            service.validateByNameRules(validateByNameRequest);
	manager.processValidateByNameResponse(validateByNameResponse);

	ValidateFullRequest validateFullRequest = 
            manager.makeValidateFullRequest();
	ValidateFullResponse validateFullResponse = 
            service.validateFullRules(validateFullRequest);
	manager.processValidateFullResponse(validateFullResponse);

	System.out.println
	    ("\n\n                      *** Xml2owl rules:  end output  ***" );

    }

    /** Application entry point, which just calls run(). */
    public static void main (String[] args) throws Exception {
	run();
    }

}
