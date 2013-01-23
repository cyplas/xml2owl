package si.uni_lj.fri.xml2owl.rules;

import si.uni_lj.fri.xml2owl.rules.RulesManager;

/** The XML2OWL ruleset validation service. */
public class RulesService {

    /** Manager for rule validation. */ 
    private static final RulesManager rulesManager = new RulesManager();

    /** Validate ruleset provided. */
    public ValidationResponse validateRules(ValidationRequest part) throws Xml2OwlRulesException {
        return rulesManager.validateRules(part);
    }

}
