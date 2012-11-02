package si.uni_lj.fri.xml2owl.util;

import java.io.ByteArrayInputStream;
import net.sf.saxon.s9api.*;
import javax.xml.transform.stream.StreamSource;

/** Utility class which converts between String and XdmNode. */
public class XmlStringConvertor {

    /** An XML processor for String<->XdmNode conversions. */
    Processor xmlProcessor;

    /** Constructor. */
    public XmlStringConvertor(Processor xmlProcessor) {
	this.xmlProcessor = xmlProcessor;
    }

    /** Convert an XdmNode to a String. */ 
    public String nodeToString(XdmNode node) {
	return node.toString();
    }

    /** Convert a String to an XdmNode. */
    public XdmNode stringToNode(String string) throws SaxonApiException {
	// Make the String a full XML file
	string = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + string;
	ByteArrayInputStream stream = 
	    new ByteArrayInputStream(string.getBytes());
	XdmNode document = 
	    xmlProcessor.newDocumentBuilder().
	    build(new StreamSource(stream));
        return (XdmNode) document.axisIterator(Axis.CHILD).next();
    }
        
}
