package si.uni_lj.fri.xml2owl.test;

import si.uni_lj.fri.xml2owl.rules.RulesApplication;

public class RulesTest {

    final private RulesApplication application = new RulesApplication();

    public void testBooks() throws Exception {
        testOnBookSource("mimovrste");
    }

    private void testOnBookSource(String source) throws Exception {
        System.out.println("Running rulesApplication.testBooks(" + source + ") ...");
        application.run("src/test/resources/data/books/" + source + "/rules.xml");
    }

}
