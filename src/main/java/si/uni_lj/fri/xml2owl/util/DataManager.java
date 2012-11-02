package si.uni_lj.fri.xml2owl.util;

/** A utility class which handles storage and retrieval of persistent data. */
public interface DataManager {

    /** Check if named data exists. If can't check, throw exception. */
    boolean exists(String name) throws Xml2OwlDataException;

    /** Return the contents under name.  If there are no contents, return null.
     * If can't access storage, throw exception. */
    String read(String name) throws Xml2OwlDataException;

    /** Write the contents to name, and return true, unless they already exist and
        shouldn't overwrite, in which case return false.  Throw exception if a
        write attempt fails. */
    boolean write(String name, String contents, boolean overwrite) throws Xml2OwlDataException;

    /** Delete the contents under name.  Return true if successfully deleted,
     * false if there's nothing to delete, and throw exception if a delete
     * attempt fails. */
    boolean delete(String name) throws Xml2OwlDataException;

}
