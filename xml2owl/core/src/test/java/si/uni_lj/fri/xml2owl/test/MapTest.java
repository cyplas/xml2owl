package si.uni_lj.fri.xml2owl.test;

import si.uni_lj.fri.xml2owl.map.application.MapApplication;

public class MapTest {

    public void testMap() throws Exception {
        MapApplication application = new MapApplication();
        System.out.println("Running testMap() on amazon ...");
        application.run("../../data/books","amazon");
        System.out.println("Running testMap() on bookdepository ...");
        application.run("../../data/books","bookdepository");
    }

}
