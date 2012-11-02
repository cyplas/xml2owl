package si.uni_lj.fri.xml2owl.test;

import si.uni_lj.fri.xml2owl.parse.WebHarvestTest;
import si.uni_lj.fri.xml2owl.map.application.MapApplication;

/** A top-level testing application which combines WebHarvestTest (to extract
    relevant XML data from the web) and XML2OWL (to use that data for carrying
    out mappings to OWL). */ 
public class TestApplication {

    /** Application entry point, which just calls run(). */
    public static void main (String[] args) throws Exception {
	run(args[0],args[1]);
    }

    /** Run XML2OWL on the specified source, perhaps first updating the HTML data. */ 
    public static void run (String source, String redownload) throws Exception {
     
        if (Boolean.parseBoolean(redownload)) {
            WebHarvestTest webHarvestTest = new WebHarvestTest();
            webHarvestTest.run(source);
        }

        MapApplication mapApplication = new MapApplication();
        mapApplication.run("test/harvest/books", source);
    }

}
