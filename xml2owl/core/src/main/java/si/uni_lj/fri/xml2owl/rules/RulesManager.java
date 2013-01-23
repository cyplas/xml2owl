package si.uni_lj.fri.xml2owl.rules;

import si.uni_lj.fri.xml2owl.util.*;

import net.sf.saxon.s9api.SaxonApiException;

/** The bridge between the RulesService and the RulesValidator. */
public class RulesManager {

    /** Validates the ruleset provided. */
    public ValidationResponse validateRules(ValidationRequest request) 
	throws Xml2OwlRulesException {
	ValidationResponse response = new ValidationResponse();
	try {
            RulesValidator validator = new RulesValidator();
            validator.validate(request.getRules());
	}
	catch (Exception e) {
	    handleException(e);
	}
	response.setSuccess(true);
        return response;
    }

    /** Handle possible exceptions thrown during validation. */ 
    private void handleException(Exception exception) 
        throws Xml2OwlRulesException {
	String prefix = "[XML2OWL] ";
	if (exception instanceof Xml2OwlRuleValidationException) {
	    prefix += "Rule validation exception: ";
	} else if (exception instanceof Xml2OwlDataException) {
	    prefix += "Data exception: ";
	} else if (exception instanceof SaxonApiException) {
	    prefix += "Saxon exception: ";
	} else {
	    prefix += "Unrecognised blah exception: ";
	}
	String message = prefix + exception.getMessage() + "\n";
        System.out.println(message);
	throw new Xml2OwlRulesException(message);
    }

}
