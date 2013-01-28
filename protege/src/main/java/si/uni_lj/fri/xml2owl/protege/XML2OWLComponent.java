package si.uni_lj.fri.xml2owl.protege;

import si.uni_lj.fri.xml2owl.map.*;
import si.uni_lj.fri.xml2owl.rules.RulesValidator;
import si.uni_lj.fri.xml2owl.rules.Xml2OwlRuleValidationException;
import si.uni_lj.fri.xml2owl.util.XmlStringConvertor;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.XdmNode;

import javax.swing.*;
import java.awt.*;

//import org.apache.log4j.Logger;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

/** Top-level component for the XML2OWL-specific parts of the XML2OWL plugin
 * tab. */
public class XML2OWLComponent extends AbstractOWLViewComponent {

    /** The panel containing the control buttons. */
    private XML2OWLControlButtonPanel controlPanel;

    /** The component with the buttons and text for the XML data. */ 
    private XML2OWLTextAreaComponent dataComponent; 

    /** The component with the buttons and text for the XML2OWL rules. */
    private XML2OWLTextAreaComponent rulesComponent;

    /** The manager used to carry out the mappings. */ 
    private MapperManager mapperManager; 

    /** The OWL model manager. */ 
    private OWLModelManager modelManager;

    /** The OWL ontology manager. */
    private OWLOntologyManager owlManager;

    /** A convertor for converting Strings to XdmNodes. */
    private XmlStringConvertor xmlConvertor;

    /** Function called when the view is dismissed. */
    @Override
    protected void disposeOWLView() {
    }

    /** Initialisation function which sets up the managers and components. */
    @Override
    protected void initialiseOWLView() throws Exception {

        controlPanel = new XML2OWLControlButtonPanel(this);
        rulesComponent = new XML2OWLTextAreaComponent(this,"Rules");
        dataComponent = new XML2OWLTextAreaComponent(this,"Data");
        JSplitPane bodyPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,rulesComponent,dataComponent);
        bodyPane.setResizeWeight(0.5);

        setLayout(new BorderLayout());
        add(controlPanel,BorderLayout.NORTH);
        add(bodyPane,BorderLayout.CENTER);

        Processor xmlProcessor = new Processor(false);
        modelManager = getOWLModelManager();
        owlManager = modelManager.getOWLOntologyManager();
        xmlConvertor = new XmlStringConvertor(xmlProcessor);
        mapperManager = new MapperManager(xmlProcessor);
    }

    /** Carry out the mapping and update the active OWL ontology based on the
        current contents of the data and rules text components. */
    public void mapRules() { 
        try {
            OWLOntology owl = modelManager.getActiveOntology();
            XdmNode xml = xmlConvertor.stringToNode(dataComponent.getValue());
            XdmNode rules = xmlConvertor.stringToNode(rulesComponent.getValue());
            mapperManager.map(owlManager, rules, owl, xml);
            modelManager.refreshRenderer();
        } 
        catch (Exception e) {
            showException("XML2OWL mapping exception",e.getMessage());
        }
    }
    
    /** Undo the last set of mapping changes. */ 
    public void undoOwl() { 
        OWLOntology owl = modelManager.getActiveOntology();
        mapperManager.unmap(owlManager, owl);
        modelManager.refreshRenderer();
    }

    /** Validate the ruleset currently given in the rules component. */
    public void validateRules() {
        RulesValidator validator = new RulesValidator();
        try {
            validator.validate(rulesComponent.getValue());
            JOptionPane.showMessageDialog(this,"Validation successful!","XML2OWL rules validation",JOptionPane.WARNING_MESSAGE);
        } 
        catch (Xml2OwlRuleValidationException e) {
            showException("XML2OWL rule validation exception",e.getMessage());
        }
    }

    /** Display an exception in mapping or rule validation in a new frame. */
    protected void showException(String title, String message) {
        JFrame exceptionFrame = new JFrame(title);
        JTextArea messageTextArea = new JTextArea(message,12,40);
        messageTextArea.setEditable(false);
        messageTextArea.setLineWrap(true);
        messageTextArea.setWrapStyleWord(true);
        JScrollPane messageScrollPane = new JScrollPane(messageTextArea);
        exceptionFrame.getContentPane().add(messageScrollPane, BorderLayout.CENTER);            
        exceptionFrame.pack();
        exceptionFrame.setLocationRelativeTo(this);
        exceptionFrame.setVisible(true);
    }

}
