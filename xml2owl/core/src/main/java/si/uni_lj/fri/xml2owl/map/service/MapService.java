package si.uni_lj.fri.xml2owl.map.service;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.ws.Action;
import javax.xml.ws.BindingType;
import javax.xml.ws.FaultAction;
import javax.xml.ws.soap.SOAPBinding;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.SaxonApiException;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLException;

import si.uni_lj.fri.xml2owl.map.service.types.*;
import si.uni_lj.fri.xml2owl.map.*;
import si.uni_lj.fri.xml2owl.util.*;
import si.uni_lj.fri.xml2owl.rules.service.Xml2OwlRulesException;

/** The XMl2OWL mapping service. */
@WebService(name = "map", targetNamespace = "http://www.fri.uni-lj.si/xml2owl", serviceName = "map", portName = "mapPort", wsdlLocation = "/WEB-INF/wsdl/xml2owlMap.wsdl")
@javax.jws.soap.SOAPBinding(style = javax.jws.soap.SOAPBinding.Style.DOCUMENT, parameterStyle = javax.jws.soap.SOAPBinding.ParameterStyle.BARE)
@BindingType(SOAPBinding.SOAP12HTTP_BINDING)
public class MapService {
    public MapService() {
    }

    /** Validates the input ruleset (using the RulesService), and if successful,
     * uses it together with the input XML to update the OWL provided. */
    @javax.jws.soap.SOAPBinding(parameterStyle = javax.jws.soap.SOAPBinding.ParameterStyle.BARE)
    @Action(input = "http://www.fri.uni-lj.si/xml2owl/request", output = "http://www.fri.uni-lj.si/xml2owl/map/mapResponse", fault = { @FaultAction(className =
                        Xml2OwlRulesException.class, value = "http://www.fri.uni-lj.si/xml2owl/map/map/Fault/xml2owlRulesException"), @FaultAction(className =
                        Xml2OwlMapException.class, value = "http://www.fri.uni-lj.si/xml2owl/map/map/Fault/xml2owlMapException") })
    @WebMethod(action = "http://www.fri.uni-lj.si/xml2owl/request")
    @WebResult(name = "response", targetNamespace = "http://www.fri.uni-lj.si/xml2owl", partName = "part")
    public Response map(@WebParam(name = "request", partName = "part", targetNamespace = "http://www.fri.uni-lj.si/xml2owl")
                        Request request) throws Xml2OwlRulesException, Xml2OwlMapException, SaxonApiException, OWLException {

        RulesServiceProxy rulesServiceProxy = new RulesServiceProxy();
        rulesServiceProxy.validateRules(request.getRules());

	Processor xmlProcessor = new Processor(false);
        OWLOntologyManager owlManager = OWLManager.createOWLOntologyManager();
        XmlStringConvertor xmlConvertor = new XmlStringConvertor(xmlProcessor);
        OwlStringConvertor owlConvertor = new OwlStringConvertor(owlManager);
        MapperManager mapManager = new MapperManager(xmlProcessor);
        XdmNode xml = xmlConvertor.stringToNode(request.getData());
        OWLOntology oldOwl = owlConvertor.stringToOwl(request.getOwl());
        XdmNode rules = xmlConvertor.stringToNode(request.getRules());
        OWLOntology newOwl = mapManager.map(owlManager,rules,oldOwl,xml);
        Response response = new Response();
        response.setOwl(owlConvertor.owlToString(newOwl));
        return response;
    }
}
