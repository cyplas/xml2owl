package si.uni_lj.fri.xml2owl.test;

import si.uni_lj.fri.xml2owl.crawl.CrawlApplication;

public class CrawlTest {

    public void testCrawl() throws Exception {
        CrawlApplication application = new CrawlApplication();
        System.out.println("Running testCrawl() on amazon ...");
        application.run("../../data/books/bookdepository","webharvest.xml","downloads","../owl.xml","rules.xml",true,true);
    }

}
