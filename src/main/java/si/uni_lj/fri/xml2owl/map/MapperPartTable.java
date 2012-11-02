package si.uni_lj.fri.xml2owl.map;

import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

/** A table containing the mappings between MapperPartTable and the names of
 * their nodes and dependency types. */ 
public class MapperPartTable {
    
    /** The map used to store the associations. */
    private static final Map<MapperPart,Map<String,String>> parts;
    static {
	Map<MapperPart,Map<String,String>> partMap = 
            new HashMap<MapperPart, Map<String,String>>();

	Map<String,String> individualParts = new HashMap<String,String>();
	partMap.put(MapperPart.IN_INDIVIDUAL, individualParts);
	individualParts.put("name", ".");

	Map<String,String> classIndividualParts = new HashMap<String,String>();
	partMap.put(MapperPart.CL_INDIVIDUAL, classIndividualParts);
	classIndividualParts.put("name", "individual|referenceToIndividual");
	classIndividualParts.put("dependency", "individual");

	Map<String,String> classClassParts = new HashMap<String,String>();
	partMap.put(MapperPart.CL_CLASS, classClassParts);
	classClassParts.put("name", "class");
	classClassParts.put("dependency", "class");

	Map<String,String> dpIndividualParts = new HashMap<String,String>();
	partMap.put(MapperPart.DP_INDIVIDUAL, dpIndividualParts);
	dpIndividualParts.put("name", "individual|referenceToIndividual");
	dpIndividualParts.put("dependency", "individual");

	Map<String,String> dpNameParts = new HashMap<String,String>();
	partMap.put(MapperPart.DP_NAME, dpNameParts);
	dpNameParts.put("name", "propertyName");
	dpNameParts.put("dependency", "propertyName");

	Map<String,String> dpValueParts = new HashMap<String,String>();
	partMap.put(MapperPart.DP_VALUE, dpValueParts);
	dpValueParts.put("name", "propertyValue");
	dpValueParts.put("dependency", "propertyValue");

	Map<String,String> opIndividualParts = new HashMap<String,String>();
	partMap.put(MapperPart.OP_INDIVIDUAL, opIndividualParts);
	opIndividualParts.put("name", 
                              "domainIndividual|referenceToDomainIndividual");
	opIndividualParts.put("dependency", "individual");

	Map<String,String> opNameParts = new HashMap<String,String>();
	partMap.put(MapperPart.OP_NAME, opNameParts);
	opNameParts.put("name", "propertyName");
	opNameParts.put("dependency", "propertyName");

	Map<String,String> opValueParts = new HashMap<String,String>();
	partMap.put(MapperPart.OP_VALUE, opValueParts);
	opValueParts.put("name", "rangeIndividual|referenceToRangeIndividual");
	opValueParts.put("dependency", "propertyValue");

	Map<String,String> identityIndividual1Parts = 
            new HashMap<String,String>();
	partMap.put(MapperPart.ID_INDIVIDUAL1, identityIndividual1Parts);
	identityIndividual1Parts.put("name", 
                                     "individual1|referenceToIndividual1");
	identityIndividual1Parts.put("dependency", "individual1");

	Map<String,String> identityIndividual2Parts = 
            new HashMap<String,String>();
	partMap.put(MapperPart.ID_INDIVIDUAL2, identityIndividual2Parts);
	identityIndividual2Parts.put("name", 
                                     "individual2|referenceToIndividual2");
	identityIndividual2Parts.put("dependency", "individual2");

        parts = Collections.unmodifiableMap(partMap);
    }

    /** Get the XPath location of a mapping part's name or dependency, relative
     * to the rule node. */
    public static String get(MapperPart part, String element) {
	return parts.get(part).get(element);
    }

}
