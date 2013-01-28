package si.uni_lj.fri.xml2owl.rules;

/** Response class for rule validation. */
public class ValidationResponse {

    /** Indicates whether the last validation was successful. */
    protected boolean success;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean value) {
        this.success = value;
    }

}
