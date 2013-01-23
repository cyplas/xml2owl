package si.uni_lj.fri.xml2owl.map;

import si.uni_lj.fri.xml2owl.rules.*;

/** Interface to the RulesService. */
public class RulesServiceProxy {

    /** Validate the ruleset provided, using the RulesService. */
    public void validateRules(String rules) throws Xml2OwlRulesException {
        RulesService rulesService = new RulesService();
        ValidationRequest request = new ValidationRequest();
        request.setRules(rules);
        ValidationResponse response = rulesService.validateRules(request);
    }

}
