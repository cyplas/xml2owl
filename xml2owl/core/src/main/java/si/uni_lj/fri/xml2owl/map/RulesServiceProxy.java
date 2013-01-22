package si.uni_lj.fri.xml2owl.map;

import si.uni_lj.fri.xml2owl.rules.service.*;
import si.uni_lj.fri.xml2owl.rules.service.types.*;

/** Interface to the RulesService. */
public class RulesServiceProxy {

    /** Validate the ruleset provided, using the RulesService. */
    public void validateRules(String rules) throws Xml2OwlRulesException {
        RulesService rulesService = new RulesService();
        ObjectFactory factory = new ObjectFactory();
        ValidateFullRequest request = factory.createValidateFullRequest();
        request.setRules(rules);
        ValidateFullResponse response = rulesService.validateFullRules(request);
    }

}
