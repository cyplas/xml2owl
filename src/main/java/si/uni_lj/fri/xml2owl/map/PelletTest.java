package si.uni_lj.fri.xml2owl.map;

import java.io.*;

import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.util.InferredOntologyGenerator;
import org.semanticweb.owlapi.vocab.PrefixOWLOntologyFormat;
import org.semanticweb.owlapi.io.OWLObjectRenderer;
import uk.ac.manchester.cs.owlapi.dlsyntax.DLSyntaxObjectRenderer;

import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;

public class PelletTest {
    
    public static void main (String[] args) throws Exception {

        // load ontology
        OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();    
        OWLOntology ontology = ontologyManager.loadOntologyFromOntologyDocument(new File("owl_input.xml"));

        // engage reasoner 
        PelletReasoner reasoner = PelletReasonerFactory.getInstance().createNonBufferingReasoner(ontology);
        ontologyManager.addOntologyChangeListener(reasoner);
        reasoner.prepareReasoner();
        reasoner.getKB().realize();
        InferredOntologyGenerator generator = new InferredOntologyGenerator(reasoner);
        generator.fillOntology(ontologyManager, ontology);

        // save ontology
        OutputStream output = new FileOutputStream("owl_output.xml");
        ontologyManager.saveOntology(ontology,output);

        // print list of SWRL rules found
        PrefixOWLOntologyFormat pm = ontologyManager.getOntologyFormat(ontology).asPrefixOWLOntologyFormat(); 
        OWLObjectRenderer renderer = new DLSyntaxObjectRenderer(); 
        for (SWRLRule rule : ontology.getAxioms(AxiomType.SWRL_RULE)) { 
            System.out.println(renderer.render(rule)); 
        }
    }

}


    