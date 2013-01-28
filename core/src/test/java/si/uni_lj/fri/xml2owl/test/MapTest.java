package si.uni_lj.fri.xml2owl.test;

import si.uni_lj.fri.xml2owl.map.MapApplication;

public class MapTest {

    final private MapApplication application = new MapApplication();

    public void testBooks() throws Exception {
        // testOnBookSource("amazon");
        // testOnBookSource("bookdepository");
        // testOnBookSource("doria");
        testOnBookSource("mimovrste");
    }

    private void testOnBookSource(String source) throws Exception {
        System.out.println("Running mapApplication.testBooks(" + source + ") ...");
        application.run("src/test/resources/data/books", "owl.xml", source + "/rules.xml", source + "/xml.xml");
    }

}
