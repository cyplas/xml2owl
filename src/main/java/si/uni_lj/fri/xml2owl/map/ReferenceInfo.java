package si.uni_lj.fri.xml2owl.map;

import net.sf.saxon.s9api.XdmNode;

/** An association between an XML node and a name, involved in XPath queries and
 * expression evaluations. */ 
public class ReferenceInfo {

    public final static int NO_PARENT = -1;

    /** The XML node relative to which the name applies. */
    private XdmNode node;

    /** The name to be converted to an IRI for an OWL entity. */
    private String name;

    private int parentIndex;

    /** Constructor. */
    public ReferenceInfo(XdmNode node, String name, int parentIndex) {
	  this.node = node;
	  this.name = name;
          this.parentIndex = parentIndex;
    }

    public ReferenceInfo(XdmNode node, String name) {
	  this.node = node;
	  this.name = name;
          this.parentIndex = NO_PARENT;
    }

    /** Get the node in the association. */
    public XdmNode getNode() {
	return node;
    }

    /** Get the IRI in the association. */
    public String getName() {
	return name;
    }

    /** Get the name in the association. */
    public int getParentIndex() {
	return parentIndex;
    }

}
