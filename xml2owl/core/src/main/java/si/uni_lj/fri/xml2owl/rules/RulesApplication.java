package si.uni_lj.fri.xml2owl.rules;

/** Application used for testing the rules service from the command-line. */
public class RulesApplication {

    /** Test out the different operations offered by the rules web service. */ 
    public static void run (String rulesFile) throws Exception {

	System.out.println
	    ("                      *** Xml2owl rules: begin output ***\n\n" );
	
	RulesService service = new RulesService();
	RulesApplicationDataManager manager = new RulesApplicationDataManager(rulesFile);

	ValidationRequest validationRequest = 
            manager.makeRequest();
	ValidationResponse validationResponse = 
            service.validateRules(validationRequest);
	manager.processResponse(validationResponse);

	System.out.println
	    ("\n\n                      *** Xml2owl rules:  end output  ***" );

    }

    /** Application entry point, which just calls run(). */
    public static void main (String[] args) throws Exception {
	run(args[0]);
    }

}
