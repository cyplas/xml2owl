package si.uni_lj.fri.xml2owl.map;

/** Exception thrown for XML2OWL mapping errors. */
public class Xml2OwlMappingException extends Exception {

    /** A flag indicating whether the error is lethal. */
    private boolean lethal;

    /** Constructor. */
    public Xml2OwlMappingException(String message, boolean lethal) {
	super(message);
	this.lethal = lethal;
    }

    /** Returns whether the exception is lethal. */
    public boolean isLethal() {
	return lethal;
    }

}
