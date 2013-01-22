package si.uni_lj.fri.xml2owl.rules;

import net.sf.saxon.s9api.*;
import javax.xml.transform.stream.StreamSource;
import java.util.*;
import java.io.*;
import javax.xml.validation.*;
import org.xml.sax.SAXException;
import si.uni_lj.fri.xml2owl.util.XmlStringConvertor;
import si.uni_lj.fri.xml2owl.util.XPathEvaluator;

/** Class used to validate rulesets against rules.xsd, and carry out further
 * checks on their validity. */
public class RulesValidator {

    /** Indicates whether a rule is static or dynamic. */
    private enum ReferenceType {
	STATIC, DYNAMIC;
    }

    /** Location of XML rules schema relative to resources directory. */
    private final String schemaLocation = "/xsd/rules.xsd";

    /** The supported expression languages. */
    public static final List<String> supportedExpressionLanguages =
        Arrays.asList("urn:fri-x2o:sublang:xpath2.0");

    /** The supported query languages. */
    private static final List<String> supportedQueryLanguages =
        Arrays.asList("urn:fri-x2o:sublang:xpath2.0");

    /** Supported data property value datatypes, other than xsd:string. */
    private static final List<String> nonStringTypes = 
        new ArrayList<String>
        (Arrays.asList
         ("xsd:boolean", "xsd:integer", "xsd:double", "xsd:float"));

    /** Converts between Strings and XdmNodes. */
    private XmlStringConvertor convertor;

    /** Used to evaluate XPath expressions. */
    private XPathEvaluator evaluator;

    /** Validates the XML input against the XML schema at schemaLocation. */
    private Validator schemaValidator;

    /** The map of reference names and their corresponding types. */
    private Map<String, ReferenceType> references;

    /** The XML processor. */ 
    private static final Processor processor = new Processor(false);

    /** Constructor. */
    public RulesValidator() {
	convertor = new XmlStringConvertor(processor);
	evaluator = new XPathEvaluator(processor, "http://www.fri.uni-lj.si/xml2owl");
	references = new HashMap<String, ReferenceType>();
        InputStream stream = getClass().getResourceAsStream(schemaLocation);
        StreamSource schemaSource = new StreamSource(stream);
	try {
	    SchemaFactory factory = 
		SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
	    Schema schema = factory.newSchema(schemaSource);
	    schemaValidator = schema.newValidator();
	}
        catch (SAXException e) { // this shouldn't happen!
	    System.out.println("[XML2OWL] Couldn't load rules.xsd:" + e.getMessage());
	}
    }

    /** The main validation function, which calls a series of specific
     * validation functions. */   
    public void validate(String xml) throws Xml2OwlRuleValidationException { 
        System.out.println("[XML2OWL] Beginning ruleset validation ...");
        validateVersusSchema(xml);
        XdmNode rules = convertToNode(xml);
	try {
            verifySupportedLanguages(rules);
	    verifyXPathValidity(rules);
	    verifyExpressionTypes(rules);
	    buildAndVerifyReferences(rules);
	    verifyReferenceUses(rules);
	    verifyDynamicRules(rules);
	    verifyDynamicPrefixIRIs(rules);
	    verifyDependencies(rules);
            System.out.println("[XML2OWL] Ruleset validation successfully completed.");
	}
	catch (SaxonApiException e) {
	    throw new Xml2OwlRuleValidationException
                ("Saxon exception during ruleset validation: " 
                 + e.getMessage());
	} 
    }

    /** Validate the XML input against the XML schema. */
    private void validateVersusSchema(String xml) 
        throws Xml2OwlRuleValidationException {
        try {
	    ByteArrayInputStream stream = 
		new ByteArrayInputStream(xml.getBytes());
	    StreamSource source = new StreamSource(stream);
	    schemaValidator.validate(source);
	}
	catch (SAXException e) {
	    throw new Xml2OwlRuleValidationException
                ("Ruleset does not match rule schema: " + e.getMessage());
	}
	catch (IOException e) {
	    throw new Xml2OwlRuleValidationException
                ("IO exception in validating ruleset against schema: " 
                 + e.getMessage());
	}
    }

    /** Verify that that expression and query languages provided are
     * supported. */
    private void verifySupportedLanguages(XdmNode rules) 
        throws SaxonApiException, Xml2OwlRuleValidationException {
        System.out.println("[XML2OWL] Verifying supported languages ...");
	String expressionLanguage = 
            evaluator.findString(rules, "@expressionLanguage");
	if (!supportedExpressionLanguages.contains(expressionLanguage)) {
	    throw new Xml2OwlRuleValidationException
                ("Unsupported expression language: " + expressionLanguage);
	} 
	String queryLanguage = evaluator.findString(rules, "@queryLanguage");
	if (!supportedQueryLanguages.contains(queryLanguage)) {
	    throw new Xml2OwlRuleValidationException
                ("Unsupported query language: " + queryLanguage);
	} 
    }

    /** Verify that XPath syntax is valid. */  
    private void verifyXPathValidity(XdmNode rules) 
        throws SaxonApiException, Xml2OwlRuleValidationException {
        System.out.println("[XML2OWL] Verifying XPath validity ...");
        XPathCompiler compiler = processor.newXPathCompiler();
        compiler.declareNamespace
            ("fn", "http://www.w3.org/2005/xpath-functions");
        XdmSequenceIterator namespaceIterator = evaluator.findIterator
            (rules, "/ontologyMappingElements/namespaces/namespace");
        while (namespaceIterator.hasNext()) {
            XdmNode node = (XdmNode) namespaceIterator.next();
            String prefix = evaluator.findString(node, "@prefix");
            String name = evaluator.findString(node, "@name");
            compiler.declareNamespace(prefix,name);
        }
	XdmSequenceIterator expressionIterator = evaluator.findIterator
	    (rules, "//(query|expression)"); // TODO: maybe check prefixIRI as well
        while (expressionIterator.hasNext()) {
            XdmNode node = (XdmNode) expressionIterator.next();
            String content = evaluator.findString(node, ".");
            compiler.compile(content); // throws exception if no good
        }
    }

    /** Check that all elements of type tExpression are either null or
     * "xsd:string", except for the propertyValue expression element. */
    private void verifyExpressionTypes(XdmNode rules) 
        throws SaxonApiException, Xml2OwlRuleValidationException {
        System.out.println("[XML2OWL] Verifying expression types ...");
	XdmSequenceIterator expressionIterator = evaluator.findIterator
	    (rules, "//(prefixIRI|expression)"); // TODO: check that prefixIRI handled ok
        while (expressionIterator.hasNext()) {
            XdmNode node = (XdmNode) expressionIterator.next();
            String nodeName = evaluator.getName(node);
            String type = evaluator.findString(node, "@type");
            if ((type != null) && !type.equals("xsd:string")) {
                if (nonStringTypes.contains(type)) {
                    XdmNode parentNode = evaluator.findNode(node, "..");
                    String parentName = evaluator.getName(parentNode);
                    if (!parentName.equals("propertyValue")
                        || !nodeName.equals("expression")) {
                        throw new Xml2OwlRuleValidationException
                            ("Expression type must be xsd:string, but is " + type + ".");
                    }
                } else {
                    throw new Xml2OwlRuleValidationException
                        ("Unsupported data property value type " + type + ".");
                }
            }
        }
    }

    /** Build up the set of references, and check enroute that there are no duplicates. */   
    private void buildAndVerifyReferences(XdmNode rules) 
        throws SaxonApiException, Xml2OwlRuleValidationException {
        System.out.println("[XML2OWL] Building and verifying references ...");
	XdmSequenceIterator nameIterator = evaluator.findIterator
	    (rules, "//*[@referenceName!='']");
        List<XdmNode> collectionReferenceNodes = new ArrayList<XdmNode>();
	while (nameIterator.hasNext()) {
	    XdmNode node = (XdmNode) nameIterator.next();
            String next = evaluator.findString(node,"@referenceName");
            if (references.containsKey(next)) {
                throw new Xml2OwlRuleValidationException
                    ("Ruleset has multiple individual mapping definitions named " 
                     + next + ".");
            } else {
                if (node.getNodeName().toString().equals("collectOWLIndividuals")) {
                    collectionReferenceNodes.add(node);
                } else {
                    if (evaluator.findNode(node, "query") == null) {
                        references.put(next, ReferenceType.STATIC);
                    } else {
                        references.put(next, ReferenceType.DYNAMIC);
                    }
                }
	    }
	}
        Iterator collectionIterator = collectionReferenceNodes.iterator();
        while (collectionIterator.hasNext()) {
            XdmNode collectionNode = (XdmNode) collectionIterator.next();
            String collectionName = evaluator.findString(collectionNode,"@referenceName");
            XdmSequenceIterator componentIterator = evaluator.findIterator
                (collectionNode, "referenceToIndividual");
            // Treat collection as dynamic if it has any dynamic components.
            boolean dynamicCollection = false;
            while (componentIterator.hasNext()) {
                XdmNode componentNode = (XdmNode) componentIterator.next();
                String componentName = evaluator.findString(componentNode, "@refName");
                if (references.get(componentName) == ReferenceType.DYNAMIC) {
                    dynamicCollection = true;
                    break;
                }
            }
            if (dynamicCollection) {
                references.put(collectionName, ReferenceType.DYNAMIC);
            } else {
                references.put(collectionName, ReferenceType.STATIC);
            }
        }
    }
	
    /** Check that all used references are defined, and that all defined
     * references are used. */
    private void verifyReferenceUses(XdmNode rules) 
        throws SaxonApiException, Xml2OwlRuleValidationException {
        System.out.println("[XML2OWL] Verifying reference uses ...");
	XdmSequenceIterator nameIterator = evaluator.findIterator
	    (rules, 
             "*/(referenceToIndividual | referenceToDomainIndividual | referenceToRangeIndividual | referenceToIndividual1 | referenceToIndividual2)");
	Set<String> uses = new HashSet<String>();  
        // check that all used references are defined
	while (nameIterator.hasNext()) { 
	    XdmNode node = (XdmNode) nameIterator.next();
	    String next = evaluator.findString(node, "@refName");
	    if (!references.containsKey(next)) {
		throw new Xml2OwlRuleValidationException
                    ("Individual reference named " + next + " is undefined.");
	    } else {
		uses.add(next);
	    }
	}
	Iterator definitionsIterator = references.keySet().iterator();
        // check that all defined references are used
	while (definitionsIterator.hasNext()) {
	    String next = (String) definitionsIterator.next();
	    if (!uses.contains(next)) {
		throw new Xml2OwlRuleValidationException
                    ("Individual reference named " + next + " is never used.");
	    }
	}
    }

    /** Verify that there is at least one dynamic part to every assertion
     * mapping rule. */
    private void verifyDynamicRules(XdmNode rules) 
        throws SaxonApiException, Xml2OwlRuleValidationException {
        System.out.println("[XML2OWL] Verifying dynamicity ...");
	XdmSequenceIterator iterator = evaluator.findIterator
	    (rules, 
             "mapToOWLClassAssertion | mapToOWLDataPropertyAssertion | mapToOWLObjectPropertyAssertion | mapToOWLSameAssertion | mapToOWLDifferentAssertion");
	while (iterator.hasNext()) {
	    XdmNode assertionNode = (XdmNode) iterator.next();
	    if (!isRuleDynamic(assertionNode)) {
		throw new Xml2OwlRuleValidationException
                    ("No dynamic parts in " 
                     + evaluator.getName(assertionNode) + ".");
	    }
	}
    }
			
    /** Verify that any part-level dynamic prefixIRIs are in a dynamic
     * part. */
    private void verifyDynamicPrefixIRIs(XdmNode rules) 
        throws SaxonApiException, Xml2OwlRuleValidationException {
        System.out.println("[XML2OWL] Verifying dynamic prefixIRIs ...");
	XdmSequenceIterator iterator = evaluator.findIterator
	    (rules, "//prefixIRI/..");
	while (iterator.hasNext()) {
	    XdmNode node = (XdmNode) iterator.next();
            String name = evaluator.getName(node);
            if (!name.equals("ontologyMappingElements")
                && !isPartDynamic(node,".")
                && Boolean.valueOf(evaluator.findString(node,"prefixIRI/@dynamic"))) {
                throw new Xml2OwlRuleValidationException
                    ("Dynamic prefixIRI in static part " + name + ".");
            }
        }
    } 

    /** Check if an individual rule has at least one dynamic part. */
    private boolean isRuleDynamic(XdmNode rule) throws SaxonApiException {
	String ruleName = evaluator.getName(rule);
	if (ruleName.equals("mapToOWLClassAssertion")) {
	    return ((isPartDynamic(rule, "individual"))
		    || (isPartDynamic(rule, "class")) 
		    || (isReferenceDynamic(rule, "referenceToIndividual")));
	} else if (ruleName.equals("mapToOWLDataPropertyAssertion")) {
	    return ((isPartDynamic(rule, "individual"))
		    || (isReferenceDynamic(rule, "referenceToIndividual"))
		    || (isPartDynamic(rule, "propertyName"))
		    || (isPartDynamic(rule, "propertyValue")));
	} else if (ruleName.equals("mapToOWLObjectPropertyAssertion")) {
	    return ((isPartDynamic(rule, "domainIndividual"))
		    || (isReferenceDynamic(rule, "referenceToDomainIndividual"))
		    || (isPartDynamic(rule, "propertyName"))
		    || (isPartDynamic(rule, "rangeIndividual"))
		    || (isReferenceDynamic(rule, 
                                           "referenceToRangeIndividual")));
	} else if (ruleName.equals("mapToOWLSameAssertion")
                   || ruleName.equals("mapToOWLDifferentAssertion")) {
	    return ((isPartDynamic(rule, "individual1"))
		    || (isReferenceDynamic(rule, "referenceToIndividual1"))
		    || (isPartDynamic(rule, "individual2"))
		    || (isReferenceDynamic(rule, "referenceToIndividual2")));
	} else { 
	    return true;
	}
    }

    /** Check whether the part specified in the rule is dynamic. */
    private boolean isPartDynamic(XdmNode rule, String part) 
        throws SaxonApiException {
	return (evaluator.findNode(rule, part+"/query") != null);
    }

    /** Check whether the specified reference part in rule is dynamic. */  
    private boolean isReferenceDynamic(XdmNode rule, String part) 
        throws SaxonApiException {
	String reference = evaluator.findString(rule, part+"/@refName");
	return ((reference != null) 
                && (references.get(reference) == ReferenceType.DYNAMIC));
    }
	
    /** Verify each dependency as well as the relations between dependencies. */
    private void verifyDependencies(XdmNode rules) 
	throws SaxonApiException, Xml2OwlRuleValidationException {
        System.out.println("[XML2OWL] Verifying dependencies ...");
	XdmSequenceIterator ruleIterator = evaluator.findIterator
	    (rules, 
             "mapToOWLClassAssertion | mapToOWLDataPropertyAssertion | mapToOWLObjectPropertyAssertion | mapToOWLSameAssertion | mapToOWLDifferentAssertion");
	while (ruleIterator.hasNext()) {
	    XdmNode assertion = (XdmNode) ruleIterator.next();
	    XdmSequenceIterator dependencyIterator = evaluator.findIterator
		(assertion, "dependency");
	    XdmNode dependency1 = null, dependency2 = null;
	    if (dependencyIterator.hasNext()) {
		dependency1 = (XdmNode) dependencyIterator.next();
		verifyDependency(assertion, dependency1);
		if (dependencyIterator.hasNext()) {
		    dependency2 = (XdmNode) dependencyIterator.next();
		    verifyDependency(assertion, dependency2);
		}
	    }
	    if (dependency2 != null) { // 2 dependencies, so property
		String dependency1From = 
                    evaluator.findString(dependency1, "@independent");
		String dependency1To = 
                    evaluator.findString(dependency1, "@dependent");
		String dependency2From = 
                    evaluator.findString(dependency2, "@independent");
		String dependency2To = 
                    evaluator.findString(dependency2, "@dependent");
                // reject a->b->a cycles
		if ((dependency1From.equals(dependency2To))
		    && (dependency2From.equals(dependency1To))) { 
		    throw new Xml2OwlRuleValidationException
                        (evaluator.getName(assertion) 
                         + " has dependency cycle.");
		}
	    }
	}
    }

    /** Validate various potential problems relating to an individual
     * dependency. */
    private void verifyDependency(XdmNode assertion, XdmNode dependency) 
	throws SaxonApiException, Xml2OwlRuleValidationException {
	String assertionName = evaluator.getName(assertion);
	String from = evaluator.findString(dependency, "@independent");
	String to = evaluator.findString(dependency, "@dependent");

	// Reject simple cycle a->a.
	if (from.equals(to)) { 
	    throw new Xml2OwlRuleValidationException
		("Dependency for " + assertionName
		 + " has same independent and dependent part.");
	}

        // Reject individual references in dependent parts of the dependency.
	String reference = null;
	if (to.equals("individual")) {
	    if (assertionName.equals("mapToOWLClassAssertion")
		|| assertionName.equals("mapToOWLDataPropertyAssertion")) {
		reference = 
                    evaluator.findString(assertion, "referenceToIndividual");
	    } else if (assertionName.equals
                       ("mapToOWLObjectPropertyAssertion")) {
		reference = 
                    evaluator.findString(assertion, 
                                         "referenceToDomainIndividual");
	    }
	} else if (to.equals("propertyValue")) {
	    if (assertionName.equals("mapToOWLObjectPropertyAssertion")) {
		reference = 
                    evaluator.findString(assertion, 
                                         "referenceToRangeIndividual");
	    }
	} else if (to.equals("individual1")) {
	    reference = 
                evaluator.findString(assertion, "referenceToIndividual1");
	} else if (to.equals("individual2")) {
	    reference = 
                evaluator.findString(assertion, "referenceToIndividual2");
	}
	if (reference != null) {
	    throw new Xml2OwlRuleValidationException
		("Dependency for " + assertionName
		 + " has an individual reference as the dependent part.");
	}

	// Reject dependencies with static parts.  
	boolean allDynamic = true;
	if (assertionName.equals("mapToOWLClassAssertion")) {
	    allDynamic = (isPartDynamic(assertion,"class")
		  && (isPartDynamic(assertion,"individual")
		      || isReferenceDynamic(assertion,
                                            "referenceToIndividual")));
	} else if (assertionName.equals("mapToOWLSameAssertion")
                   || assertionName.equals("mapToOWLDifferentAssertion")) {
	    allDynamic = (isPartDynamic(assertion,"individual1")
		  || isReferenceDynamic(assertion,"referenceToIndividual1"))
		&& (isPartDynamic(assertion,"individual2")
		    || isReferenceDynamic(assertion,"referenceToIndividual2"));
	} else { // Property assertions have 3 parts, but check only 2 key ones.
	    Set<String> parts = new HashSet<String>();
	    parts.add(from);
	    parts.add(to);
	    boolean individualDynamic = true, 
                nameDynamic = true, 
                valueDynamic = true;
	    if (parts.contains("individual")) {
		if (assertionName.equals("mapToOWLDataPropertyAssertion")) {
		    individualDynamic = isPartDynamic(assertion,"individual")
			|| isReferenceDynamic(assertion,
                                              "referenceToIndividual");
		} else if (assertionName.equals
                           ("mapToOWLObjectPropertyAssertion")) {
		    individualDynamic = isPartDynamic(assertion,
                                                      "domainIndividual")
			|| isReferenceDynamic(assertion,
                                              "referenceToDomainIndividual");
		}		    
	    }
	    if (parts.contains("propertyName")) { // same for both types
		nameDynamic = isPartDynamic(assertion,"propertyName");
	    }
	    if (parts.contains("propertyValue")) {
		if (assertionName.equals("mapToOWLDataPropertyAssertion")) {
		    valueDynamic = isPartDynamic(assertion,"propertyValue");
		} else if (assertionName.equals
                           ("mapToOWLObjectPropertyAssertion")) {
		    valueDynamic = isPartDynamic(assertion,"rangeIndividual")
			|| isReferenceDynamic(assertion,
                                              "referenceToRangeIndividual");
		}
	    }
	    allDynamic = individualDynamic && nameDynamic && valueDynamic;
	}
	if (!allDynamic) {
	    throw new Xml2OwlRuleValidationException("Dependency for " 
                                                     + assertionName 
                                                     + " has a static part.");
	} 
    }

    /** Convert the XML String to an XdmNode. */
    private XdmNode convertToNode(String xml) 
        throws Xml2OwlRuleValidationException {
	XdmNode rules = null;
	try {
  	    rules = convertor.stringToNode(xml);
	}
	catch (SaxonApiException e) {
	    throw new Xml2OwlRuleValidationException
                ("Saxon exception while parsing ruleset: " + e.getMessage());
	}
	return rules;
    }  
	
}
