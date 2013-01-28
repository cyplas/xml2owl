package si.uni_lj.fri.xml2owl.map;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.semanticweb.owlapi.model.*;

/** Utility class which converts between String and OWLOntology. */
public class OwlStringConvertor {

    /** An OWL manager for String<->OWLOntology conversions. */
    OWLOntologyManager owlManager;

    /** Constructor. */
    public OwlStringConvertor(OWLOntologyManager owlManager) {
	this.owlManager = owlManager;
    }

    /** Convert an OWLOntology to a String. */
    public String owlToString(OWLOntology owl) throws OWLException {
	ByteArrayOutputStream stream = new ByteArrayOutputStream();
	owlManager.saveOntology(owl, stream);
	return stream.toString();
    }

    /** Convert a String to an OWLOntology. */
    public OWLOntology stringToOwl(String string) throws OWLException {
	ByteArrayInputStream stream = 
	    new ByteArrayInputStream(string.getBytes());
	return owlManager.loadOntologyFromOntologyDocument(stream);
    }

}
