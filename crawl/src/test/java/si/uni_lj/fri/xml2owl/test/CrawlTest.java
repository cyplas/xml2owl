package si.uni_lj.fri.xml2owl.test;

import si.uni_lj.fri.xml2owl.crawl.CrawlApplication;

public class CrawlTest {

    final private CrawlApplication application = new CrawlApplication();

    public void testBooks() throws Exception {
        testOnBookSource("bookdepository");
    }

    private void testOnBookSource(String source) throws Exception {
        System.out.println("Running crawlApplication.testBooks(" + source + ") ...");
        application.run("src/test/resources/data/books/" + source, "webharvest.xml", "downloads", "../owl.xml","rules.xml",true,true);
    }

}
