package si.uni_lj.fri.xml2owl.map;

import si.uni_lj.fri.xml2owl.util.*;

import java.net.*;
import java.util.*;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.*;
import org.semanticweb.owlapi.util.InferredOntologyGenerator;
import net.sf.saxon.s9api.*;
import com.clarkparsia.pellet.owlapiv3.PelletReasoner;

/** Maps rules to an OWLOntology, modifying it.  This class carries out the bulk
 * of the work for the TranslateD20 web service. */
public class Mapper {

    /** Indicates what kind of dependencies are applied to the current rule. */  
    private enum DependenciesType {
	NO_DEPENDENCY, // no dependencies
	    ONE_DEPENDENCY, // e.g., class->individual
	    TWO_DEPENDENCY_RING, // e.g., name->individual & individual->value
	    TWO_DEPENDENCY_TREE; // e.g., name->individual & name->value
    }

    /** Indicates the type of rule being mapped. */
    private enum RuleType {
	INDIVIDUAL, CLASS, OBJECT_PROPERTY, DATA_PROPERTY, 
	    SAME_INDIVIDUALS, DIFFERENT_INDIVIDUALS;
    }

    /** Counter used for IRI generation mechanism. */ 
    static private int counter = 0;

    /** Manager used to access and update the owlOntology. */ 
    private final OWLOntologyManager owlManager;

    /** Factory used to generate entities and axioms. */
    private final OWLDataFactory owlFactory;

    /** The OWL ontology being updated via the mappings. */
    private final OWLOntology owlOntology;

    /** The XML input data. */
    private final XdmNode xmlData;

    /** The parameters used for the mapping. */
    private final MapperParameters parameters;

    /** The XML tool used to process queries and expressions for the rules. */
    private final XPathEvaluator rulesEvaluator;

    /** The XML tool used to process queries and expressions for the data. */
    private final XPathEvaluator dataEvaluator;

    /** The set of references to individual mapping parts. */
     private final Map<String, List<ReferenceInfo>> references;

    /** Reasoner used to check consistency of OWL ontology. */ 
    private final PelletReasoner reasoner;

    /** The cumulative set of axioms added to the OWL ontology. */
    private Set<OWLAxiom> axiomsAdded;

     /** Constructor. */
     public Mapper(OWLOntologyManager owlManager, 
		   OWLOntology owlOntology, 
		   XdmNode xmlData, 
		   MapperParameters parameters,
		   XPathEvaluator rulesEvaluator,
                   XPathEvaluator dataEvaluator,
                   PelletReasoner reasoner) {
	 this.owlManager = owlManager;
	 this.owlOntology = owlOntology;
	 this.xmlData = xmlData;
	 this.parameters = parameters;
	 this.rulesEvaluator = rulesEvaluator;
	 this.dataEvaluator = dataEvaluator;
         this.reasoner = reasoner;
	 owlFactory = owlManager.getOWLDataFactory();
         axiomsAdded = new HashSet<OWLAxiom>();
	 references = new HashMap<String, List<ReferenceInfo>> ();
     }

     /** Return the (updated) OWL ontology. */
     public OWLOntology getOwl() {
	 return owlOntology;
     }

     /** Map the rule based on one of the supported mapping rule types. */
     public void mapRule (XdmNode rule) 
	 throws Xml2OwlMappingException, SaxonApiException {
	 String ruleName = rulesEvaluator.getName(rule);
	 System.out.println("Trying to map rule " + ruleName + " ...");
	 if (ruleName.equals("mapToOWLIndividual")) {
	     mapOnePartRule(rule, RuleType.INDIVIDUAL);
	 } else if (ruleName.equals("mapToOWLClassAssertion")) {
	     mapTwoPartRule(rule, RuleType.CLASS);
	 } else if (ruleName.equals("mapToOWLDataPropertyAssertion")) {
	     mapThreePartRule(rule, RuleType.DATA_PROPERTY);
	 } else if (ruleName.equals("mapToOWLObjectPropertyAssertion")) {
	     mapThreePartRule(rule, RuleType.OBJECT_PROPERTY);
	 } else if (ruleName.equals("mapToOWLSameAssertion")) {
	     if (hasTwoIndividualParts(rule)) {
		 mapTwoPartRule(rule, RuleType.SAME_INDIVIDUALS);
	     } else {
		 mapOnePartRule(rule, RuleType.SAME_INDIVIDUALS);
	     }
	 } else if (ruleName.equals("mapToOWLDifferentAssertion")) {
	     if (hasTwoIndividualParts(rule)) {
		 mapTwoPartRule(rule, RuleType.DIFFERENT_INDIVIDUALS);
	     } else {
		 mapOnePartRule(rule, RuleType.DIFFERENT_INDIVIDUALS);
	     }
         } else if (ruleName.equals("collectOWLIndividuals")) {
             collectRule(rule);
	 } else {
	     throw new Xml2OwlMappingException
		 ("Unsupported mapping rule " + ruleName + ".", false);
	 }
     }

    private void collectRule(XdmNode rule) throws SaxonApiException {
        String referenceName = rulesEvaluator.findString(rule, "@referenceName");
        List<ReferenceInfo> collectionList = new ArrayList<ReferenceInfo>();
        XdmSequenceIterator refNames = 
            rulesEvaluator.findIterator(rule, "referenceToIndividual/@refName");
        while (refNames.hasNext()) {
            String refName = refNames.next().getStringValue();
            List<ReferenceInfo> nextList = references.get(refName);
            collectionList.addAll(nextList);
        }
        references.put(referenceName,collectionList);
    }
            
    /** Maps a rule containing just one part.  This is either an individual
	mapping definition, or a one-part same/different identity assertion.*/   
    private void mapOnePartRule(XdmNode rule, RuleType type) 
	throws SaxonApiException, Xml2OwlMappingException {
	if (type == RuleType.INDIVIDUAL) {
            makePart(rule, xmlData, MapperPart.IN_INDIVIDUAL);
	} else { // one-part same/different individual assertion
	    List<ReferenceInfo> individuals = 
                makePart(rule, xmlData, MapperPart.ID_INDIVIDUAL1);
	    Iterator<ReferenceInfo> iterator1 = individuals.iterator(); 
	    Iterator<ReferenceInfo> iterator2 = individuals.iterator(); 
	    while (iterator1.hasNext()) {
		String individual1 = 
                    ((ReferenceInfo) iterator1.next()).getName();
		while (iterator2.hasNext()) {
		    String individual2 = 
                        ((ReferenceInfo) iterator2.next()).getName();
		    if (individual1 != individual2) {
			createIdentityAxiom(individual1, 
                                            individual2, 
                                            (type==RuleType.SAME_INDIVIDUALS));
		    }
		}
	    }
	}
     }

    /** Maps a rule containing two parts.  This is either a class assertion, or
	a two-part same/different identity assertion. */
    private void mapTwoPartRule(XdmNode rule, RuleType type) 
	throws SaxonApiException, Xml2OwlMappingException {
	List<MapperPart> ordering = determineOrdering(rule);
	DependenciesType dependenciesType = determineDependenciesType(rule);
	MapperPart part1 = ordering.get(0);
	 MapperPart part2 = ordering.get(1);
	 List<String> strings = Arrays.asList(null,null);
	 Iterator<ReferenceInfo> part1Pairs = 
             makePart(rule, xmlData, part1).iterator();
         int counter1 = 0; 
	 while (part1Pairs.hasNext()) {
	     ReferenceInfo pair1 = (ReferenceInfo) part1Pairs.next();
	     strings.set(0,pair1.getName());
	     Iterator<ReferenceInfo> part2Pairs;
	     if (dependenciesType == DependenciesType.NO_DEPENDENCY) {
                 part2Pairs = makePart(rule, xmlData, part2).iterator();
	     } else {
                 part2Pairs = makePart(rule, pair1.getNode(), part2).iterator();
	     }
	     while (part2Pairs.hasNext()) {
		 ReferenceInfo pair2 = (ReferenceInfo) part2Pairs.next();
                 strings.set(1,pair2.getName());
                 if (type == RuleType.CLASS) {
                     String individual = 
                         strings.get
                         (ordering.indexOf(MapperPart.CL_INDIVIDUAL));
                     String className = 
                         strings.get(ordering.indexOf(MapperPart.CL_CLASS));
                     createClassAxiom(individual, className);
                 } else {
                     String individual1 = 
                         strings.get
                         (ordering.indexOf(MapperPart.ID_INDIVIDUAL1));
                     String individual2 = 
                         strings.get
                         (ordering.indexOf(MapperPart.ID_INDIVIDUAL2));
                     createIdentityAxiom(individual1, 
                                         individual2, 
                                         (type==RuleType.SAME_INDIVIDUALS));
                 }
             }
             counter1++;
	 }
    }
	
    /** Maps a rule containing three parts.  These are properties, either data
	properties or object properties. */
    private void mapThreePartRule(XdmNode rule, RuleType type) 
        throws SaxonApiException, Xml2OwlMappingException {
	 List<MapperPart> ordering = determineOrdering(rule);
	 DependenciesType dependenciesType = determineDependenciesType(rule);
	 MapperPart part1 = ordering.get(0);
	 MapperPart part2 = ordering.get(1);
	 MapperPart part3 = ordering.get(2);
	 List<String> strings = Arrays.asList(null,null,null);
         String positiveAssertion = findValue(rule, "@type", "positive");
	 Iterator<ReferenceInfo> part1Pairs = 
             makePart(rule, xmlData, part1).iterator();
         int counter1 = 0;
	 while (part1Pairs.hasNext()) {
	     ReferenceInfo pair1 = (ReferenceInfo) part1Pairs.next();
	     strings.set(0,pair1.getName());
	     Iterator<ReferenceInfo> part2Pairs;
	     if (dependenciesType == DependenciesType.NO_DEPENDENCY) {
                 part2Pairs = makePart(rule, xmlData, part2).iterator();
	     } else {
                 part2Pairs = makePart(rule, pair1.getNode(), part2).iterator();
	     }
             int counter2 = 0;
	     while (part2Pairs.hasNext()) {
                 ReferenceInfo pair2 = (ReferenceInfo) part2Pairs.next();
                 strings.set(1,pair2.getName());
                 Iterator<ReferenceInfo> part3Pairs;
                 if (dependenciesType == DependenciesType.TWO_DEPENDENCY_RING) {
                     part3Pairs = makePart(rule, pair2.getNode(), part3).iterator();
                 } else if (dependenciesType == 
                            DependenciesType.TWO_DEPENDENCY_TREE) {
                     part3Pairs = makePart(rule, pair1.getNode(), part3).iterator();
                 } else {
                     part3Pairs = makePart(rule, xmlData, part3).iterator();
                 }
                 while (part3Pairs.hasNext()) {
                     ReferenceInfo pair3 = (ReferenceInfo) part3Pairs.next();
                     strings.set(2,pair3.getName());
                     if (type == RuleType.DATA_PROPERTY) {
                         String individual = 
                             strings.get
                             (ordering.indexOf(MapperPart.DP_INDIVIDUAL));
                         String propertyName = 
                             strings.get
                             (ordering.indexOf(MapperPart.DP_NAME));
                         String propertyValue = 
                             strings.get
                             (ordering.indexOf(MapperPart.DP_VALUE));
                         String propertyValueType= 
                             findValue(rule, 
                                       "propertyValue/expression/@type", 
                                       "xsd:string");
                         createDataPropertyAxiom(individual, 
                                                 propertyName, 
                                                 propertyValue, 
                                                 propertyValueType, 
                                                 positiveAssertion);
                     } else { // object property
                         String individual = 
                             strings.get
                             (ordering.indexOf(MapperPart.OP_INDIVIDUAL));
                         String propertyName = 
                             strings.get
                             (ordering.indexOf(MapperPart.OP_NAME));
                         String propertyValue = 
                             strings.get
                             (ordering.indexOf(MapperPart.OP_VALUE));
                         createObjectPropertyAxiom(individual, 
                                                   propertyName, 
                                                   propertyValue, 
                                                   positiveAssertion);
                     }
                 }
                 counter2++;
             }
             counter1++;
         }
     }

    /** Adds the individual to the OWL ontology. */
    private void createIndividual(String name) throws Xml2OwlMappingException {
	 System.out.println("creating individual definition: ");
	 System.out.println("  individual: " + name);
	 OWLNamedIndividual owlIndividual = 
             owlFactory.getOWLNamedIndividual(createIRI(name));
	 OWLAxiom axiom = 
	     owlFactory.getOWLDeclarationAxiom(owlIndividual);
         addAxiom(axiom);
    }

    /** Adds a class assertion to the OWL ontology, throwing an exception if the
	class does not yet exist. */  
    private void createClassAxiom(String individual, String className)
	throws Xml2OwlMappingException {
	 System.out.println("creating class assertion: ");
	 System.out.println("  individual: " + individual);
	 System.out.println("  class: " + className);
	 OWLNamedIndividual owlIndividual = 
             owlFactory.getOWLNamedIndividual(createIRI(individual));
	 OWLClass owlClass = owlFactory.getOWLClass(createIRI(className));
	 if (owlOntology.containsEntityInSignature(owlClass)) {
	     OWLAxiom axiom;
	     axiom = 
                 owlFactory.getOWLClassAssertionAxiom(owlClass,owlIndividual);
	     addAxiom(axiom);
	 } else {
	     throw new Xml2OwlMappingException 
		 ("OWL class does not yet exist in the ontology.", 
		  false);
	 }
    }

    /** Adds a data property assertion to the OWL ontology, throwing an
     * exception if the property does not yet exist. */
     private void createDataPropertyAxiom(String individual, 
					  String propertyName, 
					  String propertyValue, 
					  String propertyValueType,
					  String positiveAssertion) 
     throws Xml2OwlMappingException {
	 // TODO: check things with the propertyValueType 
	 System.out.println("creating data property: ");
	 System.out.println("  individual: " + individual);
	 System.out.println("  propertyName: " + propertyName);
	 System.out.println("  propertyValue: " + propertyValue);
	 System.out.println("  propertyValueType: " + propertyValueType);
	 System.out.println("  positiveAssertion: " + positiveAssertion); 
	 OWLNamedIndividual owlIndividual = 
             owlFactory.getOWLNamedIndividual(createIRI(individual));
	 OWLDataProperty owlProperty = 
             owlFactory.getOWLDataProperty(createIRI(propertyName));
	 boolean positive = positiveAssertion.equals("positive");
	 if (owlOntology.containsEntityInSignature(owlProperty)) {
	     Iterator<OWLDataRange> ranges = 
                 owlProperty.getRanges(owlOntology).iterator();
	     OWLAxiom axiom;
             OWLLiteral literal = null;
             while ((literal == null) && ranges.hasNext()) {
                 OWLDatatype datatype = ranges.next().asOWLDatatype();
                 if (datatype.isString() 
                     && propertyValueType.equals("xsd:string")) {
                     literal = owlFactory.getOWLLiteral(propertyValue);
                 } else if (datatype.isBoolean() 
                            && propertyValueType.equals("xsd:boolean")) {
                     literal = 
                         owlFactory.getOWLLiteral
                         (Boolean.valueOf(propertyValue));
                 } else if (datatype.isDouble() 
                            && propertyValueType.equals("xsd:double")) {
                     literal = 
                         owlFactory.getOWLLiteral
                         (Double.valueOf(propertyValue));
                 } else if (datatype.isFloat() 
                            && propertyValueType.equals("xsd:float")) {
                     literal = 
                         owlFactory.getOWLLiteral
                         (Float.valueOf(propertyValue));
                 } else if (datatype.isInteger() 
                            && propertyValueType.equals("xsd:integer")) {
                     literal = 
                         owlFactory.getOWLLiteral
                         (Integer.valueOf(propertyValue));
                 }
             }
             if (literal == null) { // specified range isn't among property data ranges
                 throw new Xml2OwlMappingException
                     ("OWL data property range datatype unsupported/mismatched.", 
                      true);
	     }
	     if (positive) {
		 axiom = owlFactory.getOWLDataPropertyAssertionAxiom
		     (owlProperty,owlIndividual,literal);
	     } else {
		 axiom = owlFactory.getOWLNegativeDataPropertyAssertionAxiom
		     (owlProperty,owlIndividual,literal);
	     }
	     addAxiom(axiom);
	 } else {
	     throw new Xml2OwlMappingException 
		 ("OWL data property does not yet exist in the ontology.", 
		  false);
	 }
     }

    /** Adds an object property assertion to the OWL ontology, throwing an
     * exception if the property does not yet exist. */
     private void createObjectPropertyAxiom(String individual, 
                                            String propertyName, 
                                            String object, 
                                            String positiveAssertion) 
     throws Xml2OwlMappingException {
	 System.out.println("creating object property: ");
	 System.out.println("  individual: " + individual);
	 System.out.println("  propertyName: " + propertyName);
	 System.out.println("  object: " + object);
	 System.out.println("  positiveAssertion: " + positiveAssertion); 
	 OWLNamedIndividual owlIndividual = 
             owlFactory.getOWLNamedIndividual(createIRI(individual));
	 OWLObjectProperty owlProperty = 
             owlFactory.getOWLObjectProperty(createIRI(propertyName));
	 OWLNamedIndividual owlObject = 
             owlFactory.getOWLNamedIndividual(createIRI(object));
	 boolean positive = positiveAssertion.equals("positive");
	 if (owlOntology.containsEntityInSignature(owlProperty)) {
	     OWLAxiom axiom;
	     if (positive) {
		 axiom = owlFactory.getOWLObjectPropertyAssertionAxiom
		     (owlProperty,owlIndividual,owlObject);
	     } else {
		 axiom = owlFactory.getOWLNegativeObjectPropertyAssertionAxiom
		     (owlProperty,owlIndividual,owlObject);
	     }
	     addAxiom(axiom);
	 } else {
	     throw new Xml2OwlMappingException 
		 ("OWL object property does not yet exist in the ontology.", 
		  false);
	 }
     }

    /** Adds a same/different individual assertion to the OWL ontology. */
    private void createIdentityAxiom(String individual1, 
                                     String individual2, 
                                     boolean areSame) 
    throws Xml2OwlMappingException {
	 System.out.println("creating identity assertion: ");
	 System.out.println("  individual1: " + individual1);
	 System.out.println("  individual2: " + individual2);
	 OWLNamedIndividual owlIndividual1 = 
             owlFactory.getOWLNamedIndividual(createIRI(individual1));	
	 OWLNamedIndividual owlIndividual2 = 
             owlFactory.getOWLNamedIndividual(createIRI(individual2));	
	 OWLAxiom axiom;
	 if (areSame) {
	    axiom = owlFactory.getOWLSameIndividualAxiom(owlIndividual1,
                                                         owlIndividual2);
	 } else {
	     axiom = owlFactory.getOWLDifferentIndividualsAxiom(owlIndividual1,
                                                                owlIndividual2);
	 }
	 addAxiom(axiom);
    }

    /** Returns a list of ReferenceInfos for the specified part in the rule,
     * relative to the root node provided. */
     private List<ReferenceInfo> makePart(XdmNode rule, 
                                          XdmNode root, 
                                          MapperPart part) 
	 throws SaxonApiException, Xml2OwlMappingException {
	 XdmNode partNode = 
             rulesEvaluator.findNode(rule, MapperPartTable.get(part, "name"));
	 if ((part == MapperPart.IN_INDIVIDUAL)
	     || (part == MapperPart.CL_INDIVIDUAL) 
	     || (part == MapperPart.DP_INDIVIDUAL) 
	     || (part == MapperPart.OP_INDIVIDUAL) 
	     || (part == MapperPart.OP_VALUE) 
	     || (part == MapperPart.ID_INDIVIDUAL1) 
	     || (part == MapperPart.ID_INDIVIDUAL2)) {
	     return makeIndividuals(partNode, root, part);
	 } else {
	     return makeAssertionPart(partNode, root, part);
	 }
     }

    /** Returns a list of ReferenceInfos for the specified non-individual part in
     * the rule, relative to the root node provided. */
    private List<ReferenceInfo> makeAssertionPart(XdmNode partNode, 
                                                 XdmNode root, 
                                                 MapperPart part) 
	throws SaxonApiException, Xml2OwlMappingException {
	List<ReferenceInfo> list = new ArrayList<ReferenceInfo>();
        String query = rulesEvaluator.findString(partNode, "node");
	String expression = rulesEvaluator.findString(partNode, "expression");
	String prefixIRI = findPrefixIRI(partNode, part);
	if (query == null) { // static
	    String name = prefixIRI + dataEvaluator.findString(null, expression);
	    list.add(new ReferenceInfo(null,name)); 
	} else { // dynamic
	    XdmSequenceIterator nodes = dataEvaluator.findIterator(root, query);
	    while (nodes.hasNext()) {
		XdmNode node = (XdmNode) nodes.next();
		String name = prefixIRI + 
                    dataEvaluator.findString(node, expression);
		list.add(new ReferenceInfo(node,name));
	    }
	}
	return list;
    }

    /** Returns a list of ReferenceInfos for the specified individual part in the
     * rule, relative to the root node provided. */
    private List<ReferenceInfo> makeIndividuals(XdmNode individualNode, 
                                                XdmNode root, 
                                                MapperPart part) 
	throws SaxonApiException, Xml2OwlMappingException {
	String refName = rulesEvaluator.findString(individualNode, "@refName");
	if (refName != null) {
            return references.get(refName);
	} else {
	    List<ReferenceInfo> list = new ArrayList<ReferenceInfo>();
	    String query = rulesEvaluator.findString(individualNode, "node");
	    String expression = rulesEvaluator.findString(individualNode, 
                                                          "expression");
	    String prefixIRI = findPrefixIRI(individualNode, part);
	    String mappingType = rulesEvaluator.findString(individualNode, "@type");
	    if (query == null) {
		String name = prefixIRI + 
                    dataEvaluator.findString(null, expression);
		if (!owlOntology.containsEntityInSignature(createIRI(name))) {
		    if (mappingType.equals("unknown")) {
                        System.out.println("createIndividual 1: " + name);
			createIndividual(name);
		    } else {
			throw new Xml2OwlMappingException
			    ("The supposedly existing OWL individual does not"
			     + " yet exist.", 
			     true);
		    }
		}
		list.add(new ReferenceInfo(null,name));
	    } else {
                XdmSequenceIterator nodes = dataEvaluator.findIterator(root, query);
                while (nodes.hasNext()) {
                    XdmNode node = (XdmNode) nodes.next();
                    String name;
                    if (expression == null) {
                        name = prefixIRI + 
                            dataEvaluator.getName(node) + "___" + counter; 
                        while (owlOntology.containsEntityInSignature
                               (createIRI(name))) {
                            counter++;
                            name = prefixIRI + 
                                dataEvaluator.getName(node) + "___" + counter; 
                        }
                    } else {
                        name = prefixIRI + 
                            dataEvaluator.findString(node, expression);
                    }
                    // check that individual doesn't exist yet
                    if (!owlOntology.containsEntityInSignature(createIRI(name))) {
                        System.out.println("createIndividual 2: " + name);
                        createIndividual(name);
                    } else {
                        if (mappingType.equals("new")) {
                            throw new Xml2OwlMappingException
                                ("The supposedly new OWL individual already"
                                 + " exists.", 
                                 true);
                        }
                    }
                    list.add(new ReferenceInfo(node,name)); // TODO: only spot where need to do something
                } 
            }
            String referenceName = rulesEvaluator.findString(individualNode,"@referenceName");
            if (referenceName != null) {
                List<ReferenceInfo> existingList = references.get(referenceName);
                if (existingList == null) {
                    references.put(referenceName,list); 
                } else {
                    existingList.addAll(list);
                    references.put(referenceName,existingList); // todo: redundant?
                }
            }
	    return list;
	}
    }

    /** Determine an order in which to process the mapping parts of the rule
     * which is consistent with its dependencies (if any). */
     private List<MapperPart> determineOrdering(XdmNode rule) 
         throws SaxonApiException {
	 XdmSequenceIterator dependencyNodes = 
	     rulesEvaluator.findIterator(rule, "dependency");
	 XdmNode dependency1 = null, dependency2 = null;
	 if (dependencyNodes.hasNext()) {
	     dependency1 = (XdmNode) dependencyNodes.next();
	     if (dependencyNodes.hasNext()) {
		 dependency2 = (XdmNode) dependencyNodes.next();
	     }
	 }
	 String ruleName = rulesEvaluator.getName(rule);
	 if (ruleName.equals("mapToOWLDataPropertyAssertion")
	     || ruleName.equals("mapToOWLObjectPropertyAssertion")) {
	     MapperPart individualPart, namePart, valuePart;
	     if (ruleName.equals("mapToOWLDataPropertyAssertion")) {	     
		 individualPart = MapperPart.DP_INDIVIDUAL;
		 namePart = MapperPart.DP_NAME;
		 valuePart = MapperPart.DP_VALUE;
	     } else {
		 individualPart = MapperPart.OP_INDIVIDUAL;
		 namePart = MapperPart.OP_NAME;
		 valuePart = MapperPart.OP_VALUE;
	     }
	     if (dependency1 != null) {
		 String independent = 
                     rulesEvaluator.findString(dependency1, "@independent");
		 String dependent = 
                     rulesEvaluator.findString(dependency1, "@dependent");
		 if (dependency2 != null) {
		     String dependentTemp = 
                         rulesEvaluator.findString(dependency2, "@dependent");
		     if (dependentTemp.equals(independent)) { // use dependency2
			 dependent = dependentTemp;
			 independent = 
                             rulesEvaluator.findString(dependency2, "@independent");
		     }
		 }
		 if (independent.equals("individual") 
                     && dependent.equals("propertyName")) {
		     return Arrays.asList(individualPart, namePart, valuePart);
		 } else if (independent.equals("individual") 
                            && dependent.equals("propertyValue")) {
		     return Arrays.asList(individualPart, valuePart, namePart);
		 } else if (independent.equals("propertyName") 
                            && dependent.equals("individual")) {
		     return Arrays.asList(namePart, individualPart, valuePart);
		 } else if (independent.equals("propertyName") 
                            && dependent.equals("propertyValue")) {
		     return Arrays.asList(namePart, valuePart, individualPart);
		 } else if (independent.equals("propertyValue") 
                            && dependent.equals("individual")) {
		     return Arrays.asList(valuePart, individualPart, namePart);
		 } else if (independent.equals("propertyValue") 
                            && dependent.equals("propertyName")) {
		     return Arrays.asList(valuePart, namePart, individualPart);
		 }
	     }
	     return Arrays.asList(valuePart, namePart, individualPart);
	 } else if (ruleName.equals("mapToOWLClassAssertion")) {
	     if (dependency1 != null) {
		 String independent = 
                     rulesEvaluator.findString(dependency1, "@independent");
		 String dependent = 
                     rulesEvaluator.findString(dependency1, "@dependent");
		 if (independent.equals("class")) {
		     return Arrays.asList(MapperPart.CL_CLASS, 
                                          MapperPart.CL_INDIVIDUAL);
		 }
	     } 
	     return Arrays.asList(MapperPart.CL_INDIVIDUAL, 
                                  MapperPart.CL_CLASS);
	 } else if (ruleName.equals("mapToOWLSameAssertion")
		    || ruleName.equals("mapToOWLDifferentAssertion")) {
	     if (dependency1 != null) {
		 String independent = 
                     rulesEvaluator.findString(dependency1, "@independent");
		 String dependent = 
                     rulesEvaluator.findString(dependency1, "@dependent");
		 if (independent.equals("individual2")) {
		     return Arrays.asList(MapperPart.ID_INDIVIDUAL2, 
                                          MapperPart.ID_INDIVIDUAL1);
		 }
	     } 
	     return Arrays.asList(MapperPart.ID_INDIVIDUAL1, 
                                  MapperPart.ID_INDIVIDUAL2);
	 } else {
	     return Arrays.asList();
	 }
     }

    /** Determines the rule's dependency structure. */  
     private DependenciesType determineDependenciesType (XdmNode rule) 
         throws SaxonApiException {
	 XdmSequenceIterator iterator = 
             rulesEvaluator.findIterator(rule, "dependency");
	 if (!iterator.hasNext()) {
	     return DependenciesType.NO_DEPENDENCY;
	 } else {
	     XdmNode dependency1 = (XdmNode) iterator.next();
	     if (!iterator.hasNext()) {
		 return DependenciesType.ONE_DEPENDENCY;
	     } else {
		 XdmNode dependency2 = (XdmNode) iterator.next();
		 String independent1 = rulesEvaluator.findString(dependency1, 
                                                            "@independent");
		 String independent2 = rulesEvaluator.findString(dependency2, 
                                                            "@independent");
		 if (independent1.equals(independent2)) {
		     return DependenciesType.TWO_DEPENDENCY_TREE;
		 } else {
		     return DependenciesType.TWO_DEPENDENCY_RING;
		 }
	     }
	 }
     }

    /** Checks if the (same/different individual assertion) rule contains two
	parts. */ 
    private boolean hasTwoIndividualParts(XdmNode rule) 
        throws SaxonApiException {
	XdmNode individual2 = 
	    rulesEvaluator.findNode(rule, "individual2|referenceToIndividual2");
	return (individual2 != null);
    }

    /** Evaluates the xpath expression relative to the rule provided, returning
	the defaultValue in the case of a null result. */
    private String findValue(XdmNode rule, String xpath, String defaultValue) 
	throws SaxonApiException {
	 String result = rulesEvaluator.findString(rule, xpath);
	 if (result == null) {
	     return defaultValue;
	 } else {
	     return result;
	 }
     }

    /** Determines the prefixIRI of the part, checking first in the part itself
	and then falling back on the global default. */
     private String findPrefixIRI(XdmNode partNode, MapperPart part) 
         throws SaxonApiException {
	 String prefixIRI;
	 if (part == MapperPart.DP_VALUE) { // not an OWL entity
	     prefixIRI = "";
	 } else {
	     prefixIRI = rulesEvaluator.findString(partNode, "prefixIRI");
	     if (prefixIRI == null) {
		 prefixIRI = parameters.getPrefixIRI();
	     }
	 }
	 return prefixIRI;
     }

    /** Check whether the state of owlOntology is self-consistent. */
    private boolean isOntologyConsistent() {
        return reasoner.isConsistent();
        // boolean consistent = reasoner.isConsistent();
        // reasoner.getKB().realize();
        // reasoner.getKB().printClassTree();
        // InferredOntologyGenerator generator = new InferredOntologyGenerator(reasoner);
        // generator.fillOntology(owlManager, owlOntology);
        // System.out.println("consistent: " + consistent);
        // return consistent;
    }

    /** Create an IRI based on the String given, first removing any spaces */ 
    private IRI createIRI (String string) throws Xml2OwlMappingException {
        String cleanedString = string.replaceAll(" ",""); // crude space removal
        URI uri = null;
        try {
            uri = new URI(cleanedString);
        }
        catch (URISyntaxException e) {
            throw new Xml2OwlMappingException
                ("Invalid IRI (well, at least invalid URI).", true);
        }
        return IRI.create(uri); 
    }

    /** Return the current set of added axioms. */
    public Set<OWLAxiom> getAxiomsAdded() {
        return axiomsAdded;
    }

    /** Add an axiom to the OWL ontology. */
    private void addAxiom(OWLAxiom axiom) throws Xml2OwlMappingException {
        owlManager.addAxiom(owlOntology, axiom);
        if (!isOntologyConsistent()) {
            owlManager.removeAxiom(owlOntology,axiom);
            throw new Xml2OwlMappingException("Ontology conflict in axiom creation.", true);
        }
        axiomsAdded.add(axiom);
    }

}



