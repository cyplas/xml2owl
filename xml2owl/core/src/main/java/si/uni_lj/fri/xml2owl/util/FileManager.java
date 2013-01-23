package si.uni_lj.fri.xml2owl.util;

import org.apache.commons.io.FileUtils;
import java.io.File;
import java.io.*;
import java.sql.*;

/** An implementation of DataManager which uses the file system for storage.  */  
public class FileManager implements DataManager {

    /** Check if named file exists. */
    public boolean exists(String name) throws Xml2OwlDataException {
	System.out.println("[XML2OWL] Checking for file: " + name + " ...");
	File file = new File(name);
        try {
            return file.exists();
        } 
        catch (Exception e) {
            throw new Xml2OwlDataException("Couldn't verify existence of data named " + name);
        }
    }

    /** Returns contents of named file. */
    public String read(String name) throws Xml2OwlDataException {
	System.out.println("[XML2OWL] Reading file: " + name + " ...");
	File file = new File(name);
        if (!file.exists()) {
            return null;
        } else {
            try {
                return FileUtils.readFileToString(file);
            }
            catch (Exception e) {
                throw new Xml2OwlDataException("Couldn't read data named " + name);
            }
        }
    }

    /** Write the contents to the named file. */
    public boolean write(String name, String contents, boolean overwrite) 
        throws Xml2OwlDataException {
        boolean success = false;
	System.out.println("[XML2OWL] Writing file: " + name + " ...");
        File file = new File(name);
	if (file.exists() && !overwrite) {
            return false;
        } else {
	    try {
		FileUtils.writeStringToFile(file, contents);
                return true;
	    }
	    catch (Exception e) {
                throw new Xml2OwlDataException("Couldn't write data named " + name);
            }
	}
    }

    /** Delete named file. */
    public boolean delete(String name) throws Xml2OwlDataException {
	System.out.println("[XML2OWL] Deleting file: " + name + " ...");
        File file = new File(name);
        try { 
            return file.delete();
        }
        catch (Exception e) {
            throw new Xml2OwlDataException("Couldn't delete data named " + name);
        }
    }

}
