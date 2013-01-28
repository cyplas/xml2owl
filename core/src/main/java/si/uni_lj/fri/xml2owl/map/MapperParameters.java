package si.uni_lj.fri.xml2owl.map;

/** Set of parameters which modulate the mappings carried out by
 * Mapper. */
public class MapperParameters {

    /** Query language for finding XML nodes. */
    private String queryLanguage;

    /** Expression language for making expressions involving XML data. */
    private String expressionLanguage;

    /** Whether to abort for all exceptions. */
    private boolean strict;

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

    public boolean getStrict() {
	return strict;
    }

    public void setStrict(boolean strict) {
	this.strict = strict;
    }


}

