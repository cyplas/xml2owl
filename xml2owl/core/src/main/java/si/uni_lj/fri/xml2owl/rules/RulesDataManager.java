package si.uni_lj.fri.xml2owl.rules;

import si.uni_lj.fri.xml2owl.util.*;

/** Class responsible for storing and accessing rule sets.  For now, uses simple
 * local files. */ 
class RulesDataManager {

    /** The FileManager used to store and access rulesets. */
    public static final DataManager dataManager = new FileManager();

    /** Find if there are rules associated with the name provided. */   
    public boolean findRuleset(String name) throws Xml2OwlDataException {
	return dataManager.exists(name);
    }

    /** Get the rules associated with the name provided.  If no such ruleset
     * exists, return null. */   
    public String getRuleset(String name) throws Xml2OwlDataException {
	return dataManager.read(name);
    }

    /** Store the rules given under the name provided. */
    public void storeRuleset(String name, String rules, boolean override) 
        throws Xml2OwlDataException {
	dataManager.write(name, rules, override);
    }

    /** Delete the rules stored under the name provided. */ 
    public void deleteRuleset(String name) throws Xml2OwlDataException {
	dataManager.delete(name);
    }

}
