package si.uni_lj.fri.xml2owl.map.test;

import si.uni_lj.fri.xml2owl.map.application.MapApplication;

public class MapTest {

    public void testMap() throws Exception {
        MapApplication application = new MapApplication();
        application.run("../../data/books","amazon");
    }

}
