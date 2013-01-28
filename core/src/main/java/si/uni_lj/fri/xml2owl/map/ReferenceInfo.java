package si.uni_lj.fri.xml2owl.map;

import net.sf.saxon.s9api.XdmNode;

/** An association between an XML node and a name, involved in XPath queries and
 * expression evaluations. */ 
public class ReferenceInfo {

    /** The XML node relative to which the name applies. */
    private XdmNode node;

    /** The name to be converted to an IRI for an OWL entity. */
    private String name;

    /** Constructor. */
    public ReferenceInfo(XdmNode node, String name) {
	  this.node = node;
	  this.name = name;
    }

    /** Get the node in the association. */
    public XdmNode getNode() {
	return node;
    }

    /** Get the IRI in the association. */
    public String getName() {
	return name;
    }

}
