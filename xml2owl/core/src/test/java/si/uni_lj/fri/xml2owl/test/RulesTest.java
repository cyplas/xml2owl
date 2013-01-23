package si.uni_lj.fri.xml2owl.test;

import si.uni_lj.fri.xml2owl.rules.RulesApplication;

public class RulesTest {

    final private RulesApplication application = new RulesApplication();

    public void testRules() throws Exception {
        testRulesOnBookSource("mimovrste");
    }

    private void testRulesOnBookSource(String source) throws Exception {
        System.out.println("Running testMap() on " + source + " ...");
        application.run("../../data/books/" + source + "/rules.xml");
    }

}
