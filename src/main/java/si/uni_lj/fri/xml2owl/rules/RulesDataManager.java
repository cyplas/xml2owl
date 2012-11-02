package si.uni_lj.fri.xml2owl.rules;

import si.uni_lj.fri.xml2owl.util.*;

/** Class responsible for storing and accessing rule sets.  For now, uses simple
 * local files. */ 
class RulesDataManager {

    /** The FileManager used to store and access rulesets. */
    public static final DataManager dataManager = 
        //	new FileManager("data/rules", ".xml");
	new DatabaseManager("rules");

    /** Finds if there are rules associated with the name provided. */   
    public boolean findRuleset(String name) throws Xml2OwlDataException {
	return dataManager.exists(name);
    }

    /** Gets the rules associated with the name provided.  If no such ruleset
     * exists, return null. */   
    public String getRuleset(String name) throws Xml2OwlDataException {
	return dataManager.read(name);
    }

    /** Stores the rules given under the name provided. */
    public void storeRuleset(String name, String rules, boolean override) 
        throws Xml2OwlDataException {
	dataManager.write(name, rules, override);
    }

    /** Deletes the rules stored under the name provided. */ 
    public void deleteRuleset(String name) throws Xml2OwlDataException {
	dataManager.delete(name);
    }

}
