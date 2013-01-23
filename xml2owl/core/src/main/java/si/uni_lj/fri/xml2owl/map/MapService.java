package si.uni_lj.fri.xml2owl.map;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.SaxonApiException;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLException;

import si.uni_lj.fri.xml2owl.util.*;
import si.uni_lj.fri.xml2owl.rules.*;

/** The XML2OWL mapping service. */
public class MapService {

    /** Validates the input ruleset, and if successful, uses it
     * together with the input XML to update the OWL provided. */
    public MapResponse map(MapRequest request) throws Xml2OwlRulesException, Xml2OwlMapException, SaxonApiException, OWLException {

        // Rules validation, using RulesService. 
        RulesService rulesService = new RulesService();
        ValidationRequest validationRequest = new ValidationRequest();
        validationRequest.setRules(request.getRules());
        ValidationResponse validationResponse = rulesService.validateRules(validationRequest);

        // Mapping.
	Processor xmlProcessor = new Processor(false);
        OWLOntologyManager owlManager = OWLManager.createOWLOntologyManager();
        XmlStringConvertor xmlConvertor = new XmlStringConvertor(xmlProcessor);
        OwlStringConvertor owlConvertor = new OwlStringConvertor(owlManager);
        MapperManager mapManager = new MapperManager(xmlProcessor);
        XdmNode xml = xmlConvertor.stringToNode(request.getData());
        OWLOntology oldOwl = owlConvertor.stringToOwl(request.getOwl());
        XdmNode rules = xmlConvertor.stringToNode(request.getRules());
        OWLOntology newOwl = mapManager.map(owlManager,rules,oldOwl,xml);
        MapResponse response = new MapResponse();
        response.setOwl(owlConvertor.owlToString(newOwl));
        return response;
    }
}
