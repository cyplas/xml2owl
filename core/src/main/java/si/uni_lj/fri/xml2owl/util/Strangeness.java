package si.uni_lj.fri.xml2owl.util;

import java.io.ByteArrayInputStream;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.s9api.*;

public class Strangeness {

    public static void main (String[] args) throws Exception {
	Processor processor = new Processor(false);
	String input =
	    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
	    + "<a\n"
	    // + "   xmlns=\"http://www.fri.uni-lj.si/xml2owl\"\n"
	    + "   targetNamespace=\"http://www.fri.uni-lj.si/xml2owl\"\n"
	    + "   xmlns:tns=\"http://www.fri.uni-lj.si/xml2owl\"\n" 
	    + ">\n" 
	    + "  <b>plutch!</b>\n"
	    + "</a>\n";
	ByteArrayInputStream stream = new ByteArrayInputStream(input.getBytes());
	XdmNode document = processor.newDocumentBuilder().build(new StreamSource(stream));
	XdmNode root = (XdmNode) document.axisIterator(Axis.CHILD).next();
	XPathCompiler compiler = processor.newXPathCompiler();
	compiler.declareNamespace("tns", "http://www.fri.uni-lj.si/xml2owl");
	XPathSelector selector = compiler.compile("b").load();
	selector.setContextItem(root);
	XdmValue value = selector.evaluate();
	XdmNode node = (XdmNode) (value.itemAt(0));
	String name = node.getNodeName().toString();
	System.out.println("The child node's name is: " + name);
    }
    
}
