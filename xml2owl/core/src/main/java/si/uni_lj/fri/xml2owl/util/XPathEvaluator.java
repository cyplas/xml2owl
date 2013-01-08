package si.uni_lj.fri.xml2owl.util;

import net.sf.saxon.s9api.*;

// TODO: rename this class perhaps: such a class exists in Saxon!
/** Utility class which processes XPath expressions and provides results in
 * various formats. */
public class XPathEvaluator {

    /** Compiler used to process XPath queries and expressions. */
    private final XPathCompiler compiler;

    /** Constructor. */
    public XPathEvaluator(Processor processor, String defaultNamespace) {
	compiler = processor.newXPathCompiler();
        addNamespace("fn", "http://www.w3.org/2005/xpath-functions");
	addNamespace("", defaultNamespace);
    }

    public void addNamespace(String prefix, String name) {
        compiler.declareNamespace(prefix,name);
    }

    /** Get the first XML node resulting from evaluating the XPath expression
     * relative to the node provided. */
    public XdmNode findNode (XdmNode node, String xpath) 
	throws SaxonApiException {
	XdmValue value = processXPath(node, xpath);
	if (value.size() > 0) {
	    XdmItem item = value.itemAt(0);
	    return (XdmNode) item;
	} else {
	    return null;
	}
    }

    /** Get the string resulting from evaluating the XPath expression relative
     * to the node provided. */
    public String findString (XdmNode node, String xpath) 
	throws SaxonApiException {
	XdmValue value = processXPath(node, xpath);
	if (value.size() > 0) {
	    XdmItem item = value.itemAt(0);
	    return item.getStringValue();
	} else {
	    return null;
	}
    }

    /** Evaluate the XPath expression relative to the node provided, and return
     * an iterator for the resulting set of values. */
    public XdmSequenceIterator findIterator(XdmNode node, 
					    String xpath) 
	throws SaxonApiException { 
	return processXPath(node, xpath).iterator();
    }

    /** Return the name of the XML node. */
    public String getName (XdmNode node) {
	return node.getNodeName().toString();
    }

    /** Utility function processing the XPath expression relative to the node
     * provided, and returning the resulting value.  If the node is null, then
     * the expression is evaluated relative to no context, so should be
     * static. Throws a SaxonApiException if there is an XPath processing
     * error.  */  
    private XdmValue processXPath (XdmNode node, String xpath) 
	throws SaxonApiException {
	if (node != null) {
	    XPathSelector selector = compiler.compile(xpath).load();
	    selector.setContextItem(node);
	    return selector.evaluate();
	} else {
	    // with dummy 0 value
            return compiler.evaluate(xpath, new XdmAtomicValue(0)); 
	}
    }

}
