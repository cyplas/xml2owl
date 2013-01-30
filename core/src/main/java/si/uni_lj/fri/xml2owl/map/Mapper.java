package si.uni_lj.fri.xml2owl.map;

import java.net.*;
import java.util.*;

import net.sf.saxon.s9api.*;
import org.semanticweb.owlapi.model.*;
import com.clarkparsia.pellet.owlapiv3.PelletReasoner;

import si.uni_lj.fri.xml2owl.util.*;

/** Maps rules to an OWLOntology, modifying and updating it. */
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
    static private int nameCounter = 0;

    /** Manager used to access and update the owlOntology. */ 
    private final OWLOntologyManager owlManager;

    /** Factory used to generate entities and axioms. */
    private final OWLDataFactory owlFactory;

    /** The OWL ontology being updated via the mappings. */
    private final OWLOntology owlOntology;

    /** The XML input data. */
    private final XdmNode xmlData;

    /** Whether to abort strictly for every exception. */
    private final boolean strict;

    /** The XML tool used to process queries and expressions for the rules. */
    private final XPathEvaluator rulesEvaluator;

    /** The XML tool used to process queries and expressions for the data. */
    private final XPathEvaluator dataEvaluator;

    /** The set of references to individual mapping parts. */
     private final Map<String, List<ReferenceInfo>> references;

    /** Reasoner used to check consistency of the OWL ontology. */ 
    private final PelletReasoner reasoner;

    /** The cumulative set of axioms added to the OWL ontology. */
    private Set<OWLAxiom> axiomsAdded;

    /** Default prefixIRI. */
    private String globalPrefixIRI = null;

     /** Constructor. */
     public Mapper(OWLOntologyManager owlManager, 
		   OWLOntology owlOntology, 
		   XdmNode xmlData, 
		   MapperParameters parameters,
		   XPathEvaluator rulesEvaluator,
                   XPathEvaluator dataEvaluator,
                   PelletReasoner reasoner,
                   List<String> referenceNames) {
	 this.owlManager = owlManager;
	 this.owlOntology = owlOntology;
	 this.xmlData = xmlData;
	 this.strict = parameters.getStrict();
	 this.rulesEvaluator = rulesEvaluator;
	 this.dataEvaluator = dataEvaluator;
         this.reasoner = reasoner;

         references = new HashMap<String, List<ReferenceInfo>> ();
         Iterator<String> referenceIterator = referenceNames.iterator();
         while (referenceIterator.hasNext()) {
             references.put((String) referenceIterator.next(), new ArrayList<ReferenceInfo>());
         }

	 owlFactory = owlManager.getOWLDataFactory();
         axiomsAdded = new HashSet<OWLAxiom>();
     }

     /** Return the OWL ontology. */
     public OWLOntology getOwl() {
	 return owlOntology;
     }

     /** Map the rule based on one of the supported mapping rule types. */
     public void mapRule (XdmNode rule) 
	 throws Xml2OwlMappingException, SaxonApiException {
	 String ruleName = rulesEvaluator.getName(rule);
         if (ruleName.equals("prefixIRI")) {
             globalPrefixIRI = calculatePrefixIRI(xmlData, rule);
             System.out.println("[XML2OWL] Determined default prefix IRI: " + globalPrefixIRI + ".");
         } else if (ruleName.equals("collectOWLIndividuals")) {
             collectRule(rule);
         } else {
             System.out.println("[XML2OWL] Mapping rule " + ruleName + " ...");
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
             } else {  // should already be caught by validator
                 throw new Xml2OwlMappingException
                     ("Unsupported mapping rule " + ruleName + ".", true);
             }
         }
     }

    /** Map a rule gathering individual references into a collection. */
    private void collectRule(XdmNode rule) throws SaxonApiException {
        String referenceName = rulesEvaluator.findString(rule, "@referenceName");
        System.out.println("[XML2OWL] Building collection reference: " + referenceName + " ...");
        List<ReferenceInfo> collectionList = new ArrayList<ReferenceInfo>();
        XdmSequenceIterator refNames = 
            rulesEvaluator.findIterator(rule, "referenceToIndividual/@refName");
        while (refNames.hasNext()) {
            String refName = refNames.next().getStringValue();
            System.out.println("[XML2OWL]   Including reference: " + refName + " ...");
            List<ReferenceInfo> nextList = references.get(refName);
            collectionList.addAll(nextList);
        }
        references.put(referenceName,collectionList);
        System.out.println("[XML2OWL] Collection reference built.");
    }
            
    /** Map a rule containing just one part.  This is either an individual
	mapping definition, or a one-part same/different identity assertion.*/   
    private void mapOnePartRule(XdmNode rule, RuleType type) 
	throws SaxonApiException, Xml2OwlMappingException {
	if (type == RuleType.INDIVIDUAL) {
            makePart(rule, xmlData, MapperPart.IN_INDIVIDUAL);
	} else { // one-part same/different individual assertion
	    List<ReferenceInfo> individuals = 
                makePart(rule, xmlData, MapperPart.ID_INDIVIDUAL1);
	    Iterator<ReferenceInfo> iterator1 = individuals.iterator(); 
	    while (iterator1.hasNext()) {
		String individual1 = 
                    ((ReferenceInfo) iterator1.next()).getName();
                iterator1.remove();
                Iterator<ReferenceInfo> iterator2 = individuals.iterator(); 
		while (iterator2.hasNext()) {
		    String individual2 = 
                        ((ReferenceInfo) iterator2.next()).getName();
                    createIdentityAxiom(individual1, 
                                        individual2, 
                                        (type==RuleType.SAME_INDIVIDUALS));
		}
	    }
	}
     }

    /** Map a rule containing two parts.  This is either a class assertion, or
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
                 // Skip to next pair if part 1 is static.
                 if (pair1.getNode() == null) { 
                     continue; 
                 } else {
                     part2Pairs = makePart(rule, pair1.getNode(), part2).iterator();
                 }
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
	
    /** Map a rule containing three parts.  This is either a data
	property or an object property. */
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
                 // Skip to next pair if part 1 is static.
                 if (pair1.getNode() == null) { 
                     continue; 
                 } else {
                     part2Pairs = makePart(rule, pair1.getNode(), part2).iterator();
                 }
	     }
             int counter2 = 0;
	     while (part2Pairs.hasNext()) {
                 ReferenceInfo pair2 = (ReferenceInfo) part2Pairs.next();
                 strings.set(1,pair2.getName());
                 Iterator<ReferenceInfo> part3Pairs;
                 if (dependenciesType == DependenciesType.TWO_DEPENDENCY_RING) {
                     // Skip to next pair if part 2 is static.
                     if (pair2.getNode() == null) { 
                         continue; 
                     } else {
                         part3Pairs = makePart(rule, pair2.getNode(), part3).iterator();
                     }
                 } else if (dependenciesType == 
                            DependenciesType.TWO_DEPENDENCY_TREE) {
                     // Skip to next pair if part 1 is static.
                     if (pair2.getNode() == null) { 
                         continue; 
                     } else {
                         part3Pairs = makePart(rule, pair1.getNode(), part3).iterator();
                     }
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

    /** Add the individual to the OWL ontology. */
    private void createIndividual(String name) throws Xml2OwlMappingException {
	 System.out.println("[XML2OWL]     Creating individual ...");
	 System.out.println("[XML2OWL]       individual: " + name);
	 OWLNamedIndividual owlIndividual = 
             owlFactory.getOWLNamedIndividual(createIRI(name));
	 OWLAxiom axiom = 
	     owlFactory.getOWLDeclarationAxiom(owlIndividual);
         addAxiom(axiom);
	 System.out.println("[XML2OWL]   Individual successfully created.");
    }

    /** Add a class assertion to the OWL ontology, throwing an
	exception if the class does not yet exist. */  
    private void createClassAxiom(String individual, String className)
	throws Xml2OwlMappingException {
	 System.out.println("[XML2OWL]   Creating class assertion ...");
	 System.out.println("[XML2OWL]     individual: " + individual);
	 System.out.println("[XML2OWL]     class: " + className);
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
		 ("OWL class " + className + " does not yet exist in the ontology.", 
		  strict);
	 }
	 System.out.println("[XML2OWL]   Class assertion successfully created.");
    }

    /** Add a data property assertion to the OWL ontology, throwing an
     * exception if the property does not yet exist. */
     private void createDataPropertyAxiom(String individual, 
					  String propertyName, 
					  String propertyValue, 
					  String propertyValueType,
					  String positiveAssertion) 
     throws Xml2OwlMappingException {
	 System.out.println("[XML2OWL]   Creating data property assertion ...");
	 System.out.println("[XML2OWL]     individual: " + individual);
	 System.out.println("[XML2OWL]     propertyName: " + propertyName);
	 System.out.println("[XML2OWL]     propertyValue: " + propertyValue);
	 System.out.println("[XML2OWL]     propertyValueType: " + propertyValueType);
	 System.out.println("[XML2OWL]     positiveAssertion: " + positiveAssertion); 
	 OWLNamedIndividual owlIndividual = 
             owlFactory.getOWLNamedIndividual(createIRI(individual));
	 OWLDataProperty owlProperty = 
             owlFactory.getOWLDataProperty(createIRI(propertyName));
	 boolean positive = positiveAssertion.equals("positive");
	 if (owlOntology.containsEntityInSignature(owlProperty)) {
	     Iterator<OWLDataRange> ranges = 
                 owlProperty.getRanges(owlOntology).iterator();
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
                 } else {
                     throw new Xml2OwlMappingException
                         ("OWL data property value " + propertyValue 
                          + " does not match expected type " + propertyValueType + ".",
                         strict);
                 }
             }
	     OWLAxiom axiom;
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
		 ("OWL data property " + propertyName + " does not yet exist in the ontology.", 
		  strict);
	 }
	 System.out.println("[XML2OWL]   Data property assertion successfully created.");
     }

    /** Add an object property assertion to the OWL ontology, throwing
     * an exception if the property does not yet exist. */
     private void createObjectPropertyAxiom(String individual, 
                                            String propertyName, 
                                            String object, 
                                            String positiveAssertion) 
     throws Xml2OwlMappingException {
	 System.out.println("[XML2OWL]   Creating object property assertion ...");
	 System.out.println("[XML2OWL]     individual: " + individual);
	 System.out.println("[XML2OWL]     propertyName: " + propertyName);
	 System.out.println("[XML2OWL]     object: " + object);
	 System.out.println("[XML2OWL]     positiveAssertion: " + positiveAssertion); 
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
		 ("OWL object property " + propertyName + " does not yet exist in the ontology.", 
		  strict);
	 }
	 System.out.println("[XML2OWL]   Object property assertion successfully created.");
     }

    /** Add a same/different individual assertion to the OWL ontology. */
    private void createIdentityAxiom(String individual1, 
                                     String individual2, 
                                     boolean areSame) 
    throws Xml2OwlMappingException {
        if (areSame) {
            System.out.println("[XML2OWL]   Creating same individual assertion ...");
        } else {
            System.out.println("[XML2OWL]   Creating different individual assertion ...");
        }
	 System.out.println("[XML2OWL]     individual1: " + individual1);
	 System.out.println("[XML2OWL]     individual2: " + individual2);
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
	 System.out.println("[XML2OWL]   Identity assertion successfully created.");
    }

    /** Return the ReferenceInfos for the specified part in the rule,
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
             List<ReferenceInfo> individuals = makeIndividuals(partNode, root, part);
	     return individuals;
	 } else {
	     return makeAssertionPart(partNode, root, part);
	 }
     }

    /** Return the ReferenceInfos for the specified non-individual
     * part in the rule, relative to the root node provided. */
    private List<ReferenceInfo> makeAssertionPart(XdmNode partNode, 
                                                 XdmNode root, 
                                                 MapperPart part) 
	throws SaxonApiException, Xml2OwlMappingException {
	List<ReferenceInfo> list = new ArrayList<ReferenceInfo>();
        String query = rulesEvaluator.findString(partNode, "query");
	String expression = rulesEvaluator.findString(partNode, "expression");
        XdmNode prefixIRINode = rulesEvaluator.findNode(partNode, "prefixIRI"); 
	if (query == null) { // static
            String prefixIRI = findPrefixIRI(null, prefixIRINode, part);
	    String name = prefixIRI + dataEvaluator.findString(null, expression);
	    list.add(new ReferenceInfo(null,name)); 
	} else { // dynamic
	    XdmSequenceIterator nodes = dataEvaluator.findIterator(root, query);
	    while (nodes.hasNext()) {
		XdmNode node = (XdmNode) nodes.next();
                String prefixIRI = findPrefixIRI(node, prefixIRINode, part);
		String name = prefixIRI + 
                    dataEvaluator.findString(node, expression);
		list.add(new ReferenceInfo(node,name));
	    }
	}
	return list;
    }

    /** Return the ReferenceInfos for the specified individual part in
     * the rule, relative to the root node provided. */
    private List<ReferenceInfo> makeIndividuals(XdmNode individualNode, 
                                                XdmNode root, 
                                                MapperPart part) 
	throws SaxonApiException, Xml2OwlMappingException {
	String refName = rulesEvaluator.findString(individualNode, "@refName");
	if (refName != null) {
            return references.get(refName);
	} else {
	    List<ReferenceInfo> list = new ArrayList<ReferenceInfo>();
	    String query = rulesEvaluator.findString(individualNode, "query");
	    String expression = rulesEvaluator.findString(individualNode, 
                                                          "expression");
            XdmNode prefixIRINode = rulesEvaluator.findNode(individualNode,
                                                            "prefixIRI");
	    String mappingType = findValue(individualNode, "@type", "unknown");
	    if (query == null) {
                String prefixIRI = findPrefixIRI(null, prefixIRINode, part);
		String name = prefixIRI + 
                    dataEvaluator.findString(null, expression);
                handleIndividualType(name, mappingType);
                list.add(new ReferenceInfo(null,name));
	    } else {
                XdmSequenceIterator nodes = dataEvaluator.findIterator(root, query);
                while (nodes.hasNext()) {
                    XdmNode node = (XdmNode) nodes.next();
                    String prefixIRI = findPrefixIRI(node,prefixIRINode, part);
                    String name;
                    if (expression == null) {
                        name = prefixIRI + 
                            dataEvaluator.getName(node) + "___" + nameCounter; 
                        while (owlOntology.containsEntityInSignature
                               (createIRI(name))) {
                            nameCounter++;
                            name = prefixIRI + 
                                dataEvaluator.getName(node) + "___" + nameCounter; 
                        }
                    } else {
                        name = prefixIRI + 
                            dataEvaluator.findString(node, expression);
                    }
                    handleIndividualType(name,mappingType);
                    list.add(new ReferenceInfo(node,name));
                } 
            }
            String referenceName = rulesEvaluator.findString(individualNode,"@referenceName");
            if (referenceName != null) {
                List<ReferenceInfo> existingReferences = references.get(referenceName);
                if (existingReferences == null) {
                    references.put(referenceName,list); 
                } else {
                    existingReferences.addAll(list);
                }
            }
	    return list;
	}
    }

    /** Depending on the individual mapping type and on whether the
    individual already exists in the ontology, either create the
    individual, throws an exception, or do nothing. */
    private void handleIndividualType(String name, String type) throws Xml2OwlMappingException {
        if (owlOntology.containsEntityInSignature(createIRI(name))) {
            if (type.equals("new")) {
                throw new Xml2OwlMappingException
                    ("The supposedly new OWL individual " + name 
                     + " already exists.", 
                     true);
            } 
        } else {
            if (type.equals("existing")) {
                throw new Xml2OwlMappingException
                    ("The supposedly existing OWL individual " + name 
                     + " does not yet exist.", 
                     true);
            } else {
                createIndividual(name);
            }
        }
    }

    /** Determine the order in which to process the mapping parts of the rule
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

    /** Determine the rule's dependency structure. */  
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

    /** Check if the (same/different individual assertion) rule
	contains two parts. */ 
    private boolean hasTwoIndividualParts(XdmNode rule) 
        throws SaxonApiException {
	XdmNode individual2 = 
	    rulesEvaluator.findNode(rule, "individual2|referenceToIndividual2");
	return (individual2 != null);
    }

    /** Evaluate the XPath expression relative to the rule provided, returning
	a defaultValue provided in case of a null result. */
    private String findValue(XdmNode rule, String xpath, String defaultValue) 
	throws SaxonApiException {
	 String result = rulesEvaluator.findString(rule, xpath);
	 if (result == null) {
	     return defaultValue;
	 } else {
	     return result;
	 }
     }

    /** Calculate the prefixIRI for a prefixIRI node, possibly
        dynamically relative to a particular data node. */
    public String calculatePrefixIRI(XdmNode relativeNode, XdmNode prefixIRINode) 
        throws SaxonApiException {
        if (prefixIRINode == null) {
            return null;
        } else {
            String expression = rulesEvaluator.findString(prefixIRINode, ".");
            if (expression == null) {
                return null;
            } else {
                boolean dynamic = Boolean.valueOf(findValue(prefixIRINode,"@dynamic","false"));
                if (dynamic) {
                    return dataEvaluator.findString(relativeNode, expression);
                } else {
                    return dataEvaluator.findString(null,expression);
                }
            }
        }
    }

    /** Determine the prefixIRI of the mapping part, checking first at
         the part level, and then globally. */
    private String findPrefixIRI(XdmNode relativeNode, XdmNode prefixIRINode, MapperPart part) 
        throws SaxonApiException {
        if (part == MapperPart.DP_VALUE) { // not an OWL entity
            return "";
        } else {
            String localPrefixIRI = calculatePrefixIRI(relativeNode, prefixIRINode);
            if (localPrefixIRI != null) {
                return localPrefixIRI;
            } else if (globalPrefixIRI != null) {
                return globalPrefixIRI;
            } else {
                return "";
            }
        }
    } 

    /** Check whether the state of owlOntology is self-consistent. */
    private boolean isOntologyConsistent() {
        return reasoner.isConsistent();
    }

    /** Create an IRI based on the String given, first removing any spaces. */ 
    private IRI createIRI (String string) throws Xml2OwlMappingException {
        String cleanedString = string.replaceAll(" ",""); // crude space removal
        URI uri = null;
        try {
            uri = new URI(cleanedString);
        }
        catch (URISyntaxException e) {
            throw new Xml2OwlMappingException
                ("Invalid URI: " + cleanedString + ".", true);
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
            throw new Xml2OwlMappingException("Ontology conflict in axiom creation.", strict);
        } else {
            axiomsAdded.add(axiom);
        }
    }

}
