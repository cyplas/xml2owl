package si.uni_lj.fri.xml2owl.map;

/** Set of parameters which modulate the mappings carried out by
 * Mapper.  These parameters are obtained through the rules provided
 * to the service. */
public class MapperParameters {

    /** Query language for finding XML nodes. */
    private String queryLanguage;

    /** Expression language for making expressions involving XML data. */
    private String expressionLanguage;

    /** Whether to overwrite pre-existing OWL entities. */
    private boolean override;

    /** Global default prefix IRI for OWL entities. */ 
    private String prefixIRI;
    
    public String getQueryLanguage() {
	return queryLanguage;
    }

    public void setQueryLanguage(String queryLanguage) {
	this.queryLanguage = queryLanguage;
    }

    public String getExpressionLanguage() {
	return expressionLanguage;
    }

    public void setExpressionLanguage(String expressionLanguage) {
	this.expressionLanguage = expressionLanguage;
    }

    public boolean getOverride() {
	return override;
    }

    public void setOverride(boolean override) {
	this.override = override;
    }

    public String getPrefixIRI() {
	return prefixIRI;
    }

    public void setPrefixIRI(String prefixIRI) {
	this.prefixIRI = prefixIRI;
    }

}

