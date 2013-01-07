package si.uni_lj.fri.xml2owl.test;

import si.uni_lj.fri.xml2owl.map.application.MapApplication;

public class MapTest {

    final private MapApplication application = new MapApplication();

    public void testMap() throws Exception {
        testMapOnBookSource("amazon");
        testMapOnBookSource("bookdepository");
        testMapOnBookSource("doria");
        testMapOnBookSource("mimovrste");
    }

    private void testMapOnBookSource(String source) throws Exception {
        System.out.println("Running testMap() on " + source + " ...");
        application.run("../../data/books", source);
    }

}
