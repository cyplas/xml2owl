package si.uni_lj.fri.xml2owl.crawl;

import java.io.*;

import org.webharvest.definition.ScraperConfiguration;
import org.webharvest.runtime.Scraper;
import org.webharvest.runtime.variables.Variable;

import si.uni_lj.fri.xml2owl.map.MapApplication;

/** A class used to crawl the web, gather relevant data and prepare XML input
 * files suitable for XML2OWL, based on a webharvest.xml configuration file. */ 
public class CrawlApplication {

    /** Application entry point, which just calls run(). */
    public static void main(String[] args) {
        run(args[0],args[1],args[2],args[3],args[4],Boolean.valueOf(args[5]),Boolean.valueOf(args[6]));
    }

    /** Download the datafiles, maybe generate the XML input for XML2OWL, and maybe run XML2OWL. */
    public static void run (String commonPath, String webharvestFile, String downloadDirectory, String owlFile, String rulesFile, boolean harvest, boolean map) {
        try {
            if (harvest) {
                delete(new File(commonPath + "/" + downloadDirectory));
                ScraperConfiguration config = 
                    new ScraperConfiguration(commonPath + "/" + webharvestFile);
                Scraper scraper = new Scraper(config, "data");
                scraper.setDebug(true);
                scraper.execute();
            }
            if (map) {
                MapApplication mapApplication = new MapApplication();
                mapApplication.run(commonPath, owlFile, rulesFile, "xml.xml"); // assume that datafile is xml.xml
            }
        }
        catch (FileNotFoundException e) {
            System.out.println("Webharvest configuration file not found.");
        }
        catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
        catch (Exception e) {
            System.out.println("XML2OWL or other error: " + e.getMessage());
        }
    }

    /** Clear and delete the downloads directory. */
    private static void delete(File file) throws IOException {
 
    	if (file.isDirectory()) {
            if( file.list().length==0) {
                file.delete();
                System.out.println("Directory is deleted : " 
                                   + file.getAbsolutePath());
            } else {
                String files[] = file.list();
                for (String temp : files) {
                    File fileDelete = new File(file, temp);
                    delete(fileDelete);
                }
                if (file.list().length==0) {
                    file.delete();
                    System.out.println("Directory is deleted : " 
                                       + file.getAbsolutePath());
                }
            }
    	} else {
            file.delete();
    	}
    }

} 
