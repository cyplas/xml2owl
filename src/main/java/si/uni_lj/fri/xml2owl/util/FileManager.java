package si.uni_lj.fri.xml2owl.util;

import org.apache.commons.io.FileUtils;
import java.io.File;
import java.io.*;
import java.sql.*;

/** An implementation of DataManager which uses the file system for storage.  */  
public class FileManager implements DataManager {

    /** The base directory. */
    public final static String path = "src/main/resources/";

    /** The directory where the files should be stored relative to path. */
    public final String directory;

    /** The suffix (e.g., ".xml") to attach onto the names provided. */
    public final String suffix;

    /** Constructor. */
    public FileManager(String directory, String suffix) {
	this.directory = directory;
	this.suffix = suffix;
    }

    /** Check if named file exists. */
    public boolean exists(String name) throws Xml2OwlDataException {
	String fullName = makeName(name);
	File file = new File(fullName);
        try {
            return file.exists();
        } 
        catch (Exception e) {
            throw new Xml2OwlDataException("Couldn't verify existence of data named " + name);
        }
    }

    /** Returns contents of named file. */
    public String read(String name) throws Xml2OwlDataException {
	String fullName = makeName(name);
        File file = new File(fullName);
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
	String fullName = makeName(name);
        File file = new File(fullName);
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
	String fullName = makeName(name);
        File file = new File(fullName);
        try { 
            return file.delete();
        }
        catch (Exception e) {
            throw new Xml2OwlDataException("Couldn't delete data named " + name);
        }
    }

    /** Produce the full filename from the name provided. */
    private String makeName(String name) {
	System.out.println(path + directory + "/" + name + suffix);
	return (path + directory + "/" + name + suffix);
    }

}
