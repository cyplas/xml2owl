package si.uni_lj.fri.xml2owl.crawl.app;

import org.webharvest.definition.ScraperConfiguration;
import org.webharvest.runtime.Scraper;
import org.webharvest.runtime.variables.Variable;
import java.io.*;

/** A class used to crawl the web, gather relevant data and prepare XML input
 * files suitable for XML2OWL, based on a webharvest.xml configuration file. */ 
public class WebHarvestApplication {

    /** Application entry point, which just calls run(). */
    public static void main(String[] args) {
        run(args[0]);
    }

    /** Download the datafiles and generate the XML input for XML2OWL. */
    public static void run (String source) {
        try {
            String directory = "src/main/resources/test/harvest/books/"  + source + "/";
            delete(new File(directory + "downloads"));
            ScraperConfiguration config = 
                new ScraperConfiguration(directory + "webharvest.xml");
            Scraper scraper = new Scraper(config, "data");
            scraper.setDebug(true);
            scraper.execute();
        }
        catch (FileNotFoundException e) {
            System.out.println("Webharvest configuration file not found ...");
        }
        catch (IOException e) {
            System.out.println("IOException took place ...");
        }
    }

    /** Clear and delete the downloads directory. */
    private static void delete(File file) 
    	throws IOException{
 
    	if(file.isDirectory()){
 
            //directory is empty, then delete it
            if(file.list().length==0){
 
                file.delete();
                System.out.println("Directory is deleted : " 
                                   + file.getAbsolutePath());
 
            }else{
 
                //list all the directory contents
                String files[] = file.list();
 
                for (String temp : files) {
                    //construct the file structure
                    File fileDelete = new File(file, temp);
 
                    //recursive delete
                    delete(fileDelete);
                }
 
                //check the directory again, if empty then delete it
                if(file.list().length==0){
                    file.delete();
                    System.out.println("Directory is deleted : " 
                                       + file.getAbsolutePath());
                }
            }
 
    	}else{
            //if file, then delete it
            file.delete();
    	}
    }

} 
