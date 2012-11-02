package si.uni_lj.fri.xml2owl.util;

import java.io.*;
import java.sql.*;

/** An implementation of DataManager which uses an Oracle database for storage.  */  
public class DatabaseManager implements DataManager {

    /** The directory where the files should be stored. */
    private final String folder;

    /** Constructor. */
    public DatabaseManager(String folder) {
	this.folder = folder;
    }

    /** Check if named data exists. */
    public boolean exists(String name) throws Xml2OwlDataException {
        try {
            Connection connection = getConnection();
            PreparedStatement statement = 
                connection.prepareStatement("select null from xml2owl_data where folder = ? and name = ?");
            statement.setString(1,folder);
            statement.setString(2,name);
            ResultSet results = statement.executeQuery();
            boolean found = results.next();
            statement.close();
            connection.close();
            return found;
        } 
        catch(Exception e) {
            throw new Xml2OwlDataException("Couldn't verify existence of data named " + name);
        }
    }

    /** Return data for name. */
    public String read(String name) throws Xml2OwlDataException {
        try {
            Connection connection = getConnection();
            PreparedStatement statement = 
                connection.prepareStatement("select data from xml2owl_data where folder = ? and name = ?");
            statement.setString(1,folder);
            statement.setString(2,name);
            ResultSet results = statement.executeQuery();
            String data;
            if (results.next()) {
                data = results.getString(name);
            } else {
                data = null;
            }
            statement.close();
            connection.close();
            return data;
        } 
        catch(Exception e) {
                throw new Xml2OwlDataException("Couldn't read data named " + name);
        }
    }

    /** Write data for name. */
    public boolean write(String name, String contents, boolean overwrite) throws Xml2OwlDataException {
        try {
            Connection connection = getConnection();
            PreparedStatement checkStatement = 
                connection.prepareStatement("select null from xml2owl_data where folder = ? and name = ?");
            checkStatement.setString(1,folder);            
            checkStatement.setString(2,name);            
            ResultSet checkResult = checkStatement.executeQuery();
            boolean found = checkResult.next();
            boolean success;
            checkStatement.close();
            if (found) {
                if (!overwrite) {
                    success = false;
                } else {
                    PreparedStatement statement = 
                        connection.prepareStatement("update xml2owl_data set data = ? where folder = ? and name = ?");
                    statement.setString(1,contents);
                    statement.setString(2,folder);
                    statement.setString(3,name);
                    statement.execute();
                    statement.close();
                    success= true;
                }
            } else {
                PreparedStatement statement = 
                    connection.prepareStatement("insert into xml2owl_data (folder, name, data) values (?,?,?)");
                statement.setString(1,folder);
                statement.setString(2,name);
                statement.setString(3,contents);
                statement.execute();
                statement.close();
                success = true;
            }
            connection.close();
            return success;
        } 
        catch(Exception e) {
            throw new Xml2OwlDataException("Couldn't write data named " + name);
        }
    }

    /** Delete contents for name. */
    public boolean delete(String name) throws Xml2OwlDataException {
        try {
            Connection connection = getConnection();
            PreparedStatement checkStatement = 
                connection.prepareStatement("select null from xml2owl_data where folder = ? and name = ?");
            checkStatement.setString(1,folder);            
            checkStatement.setString(2,name);            
            ResultSet checkResult = checkStatement.executeQuery();
            boolean found = checkResult.next();
            if (!found) {
                return false;
            } else {
                PreparedStatement statement = 
                    connection.prepareStatement("delete from xml2owl_data where folder = ? and name = ?");
                statement.setString(1,folder);
                statement.setString(2,name);
                statement.execute();
                statement.close();
                connection.close();
                return true;
            }
        } 
        catch(Exception e) {
            throw new Xml2OwlDataException("Couldn't delete data named " + name);
        }
    }

    /** Get Oracle database connection. */
    private Connection getConnection() throws SQLException {
        String address = "jdbc:oracle:thin:@cypdb.fri1.uni-lj.si:1521:XE";
        String username = "cyp";
        String password = "kronos";
        return DriverManager.getConnection(address, username, password);
    }

}
