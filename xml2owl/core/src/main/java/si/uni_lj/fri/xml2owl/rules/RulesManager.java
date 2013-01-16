package si.uni_lj.fri.xml2owl.rules;

import si.uni_lj.fri.xml2owl.rules.service.types.*;
import si.uni_lj.fri.xml2owl.rules.service.*;
import si.uni_lj.fri.xml2owl.util.*;

import net.sf.saxon.s9api.SaxonApiException;

/** The bridge between the RulesService and the RulesValidator. */
public class RulesManager {

    /** Manager used to handle the storing and retrieving of rulesets. */
    private final RulesDataManager dataManager;

    /** Constructor. */
    public RulesManager() {
	dataManager = new RulesDataManager();
    }
    
    /** Adds a ruleset, unless it does not validate. */
    public AddResponse addRules(AddRequest request) 
        throws Xml2OwlRulesException {
	try {
	    String ruleset = request.getRules();
	    validateRuleset(ruleset);
	    dataManager.storeRuleset(request.getName(), 
                                     ruleset, 
                                     request.isOverride());
	}
	catch (Exception e) {
	    handleException(e);
	}
	AddResponse response = new AddResponse();
	response.setSuccess(true);
	return response;
    }

    /** Deletes a stored ruleset, identified by its name. */
    public DeleteResponse deleteRules(DeleteRequest request) 
	throws Xml2OwlRulesException {
	String name = request.getName();
	try {
	    dataManager.deleteRuleset(name);
	}
	catch (Exception e) {
	    handleException(e);
	}
	DeleteResponse response = new DeleteResponse();
	response.setSuccess(true);
	return response;
    }

    /** Gets a stored ruleset, identified by its name. */
    public GetResponse getRules(GetRequest request) 
	throws Xml2OwlRulesException {
	String ruleset = "";
	try {
	    ruleset = dataManager.getRuleset(request.getName());
	}
	catch (Exception e) {
	    handleException(e);
	}
	GetResponse response = new GetResponse();
	response.setRules(ruleset);
	return response;
    }

    /** Validates a stored ruleset, identified by its name. */
    public ValidateByNameResponse validateRulesByName
        (ValidateByNameRequest request) 
	throws Xml2OwlRulesException {
	String name = request.getName();
	try {
            String ruleset = dataManager.getRuleset(name);
            if (ruleset == null) {
                throw new Xml2OwlRuleValidationException("No ruleset found named " + name);
            } else {
                validateRuleset(dataManager.getRuleset(name));
            }
	}
	catch (Exception e) {
	    handleException(e);
	}
	ValidateByNameResponse response = new ValidateByNameResponse();
	response.setSuccess(true);
	return response;
    }

    /** Validates the ruleset provided. */
    public ValidateFullResponse validateRulesFull(ValidateFullRequest request) 
	throws Xml2OwlRulesException {
	ValidateFullResponse response = new ValidateFullResponse();
	try {
	    validateRuleset(request.getRules());
	}
	catch (Exception e) {
	    handleException(e);
	}
	response.setSuccess(true);
        return response;
    }

    /** Validates a ruleset. */
    public void validateRuleset(String ruleset) 
        throws Xml2OwlRuleValidationException {
	RulesValidator validator = new RulesValidator();
        validator.validate(ruleset);
    }

    /** Handles possible exceptions thrown during validation. */ 
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
	RulesFault faultInfo = new RulesFault();
	faultInfo.setMessage(message);
        System.out.println(message);
	throw new Xml2OwlRulesException(message,faultInfo);
    }

}
