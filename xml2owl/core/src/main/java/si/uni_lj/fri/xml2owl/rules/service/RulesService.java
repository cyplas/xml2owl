package si.uni_lj.fri.xml2owl.rules.service;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.ws.Action;
import javax.xml.ws.BindingType;
import javax.xml.ws.FaultAction;
import javax.xml.ws.soap.SOAPBinding;

import si.uni_lj.fri.xml2owl.rules.service.types.*;
import si.uni_lj.fri.xml2owl.rules.RulesManager;

/** The XML2OWL ruleset validation service. */
@WebService(name = "rules", 
            targetNamespace = "http://www.fri.uni-lj.si/xml2owl/rules", 
            serviceName = "rules", 
            portName = "rulesPort", 
            wsdlLocation = "/WEB-INF/wsdl/xml2owlRules.wsdl")
@javax.jws.soap.SOAPBinding(style = javax.jws.soap.SOAPBinding.Style.DOCUMENT, 
                            parameterStyle = javax.jws.soap.SOAPBinding.ParameterStyle.BARE)
@BindingType(SOAPBinding.SOAP12HTTP_BINDING)
public class RulesService {

    private static final RulesManager rulesManager = new RulesManager();

    public RulesService() {
    }

    @javax.jws.soap.SOAPBinding(parameterStyle = javax.jws.soap.SOAPBinding.ParameterStyle.BARE)
    @Action(input = "http://www.fri.uni-lj.si/xml2owl/rules/addRules", 
            output = "http://www.fri.uni-lj.si/xml2owl/rules/rules/addRulesResponse", 
            fault = { @FaultAction(className = Xml2OwlRulesException.class, 
                                   value = "http://www.fri.uni-lj.si/xml2owl/rules/rules/addRules/Fault/xml2owlRulesException") })
    @WebMethod(action = "http://www.fri.uni-lj.si/xml2owl/rules/addRules")
    @WebResult(name = "addResponse", 
               targetNamespace = "http://www.fri.uni-lj.si/xml2owl/rules", 
               partName = "part")
    public AddResponse addRules(@WebParam(name = "addRequest", 
                                          partName = "part", 
                                          targetNamespace = "http://www.fri.uni-lj.si/xml2owl/rules")
        AddRequest part) throws Xml2OwlRulesException {
        return rulesManager.addRules(part);
    }

    @javax.jws.soap.SOAPBinding(parameterStyle = javax.jws.soap.SOAPBinding.ParameterStyle.BARE)
    @Action(input = "http://www.fri.uni-lj.si/xml2owl/rules/getRules", 
            output = "http://www.fri.uni-lj.si/xml2owl/rules/rules/getRulesResponse", 
            fault = { @FaultAction(className = Xml2OwlRulesException.class, 
                                   value = "http://www.fri.uni-lj.si/xml2owl/rules/rules/getRules/Fault/xml2owlRulesException") })
    @WebMethod(action = "http://www.fri.uni-lj.si/xml2owl/rules/getRules")
    @WebResult(name = "getResponse", 
               targetNamespace = "http://www.fri.uni-lj.si/xml2owl/rules", 
               partName = "part")
    public GetResponse getRules(@WebParam(name = "getRequest", 
                                          partName = "part", 
                                          targetNamespace = "http://www.fri.uni-lj.si/xml2owl/rules")
        GetRequest part) throws Xml2OwlRulesException {
        return rulesManager.getRules(part);
    }

    @javax.jws.soap.SOAPBinding(parameterStyle = javax.jws.soap.SOAPBinding.ParameterStyle.BARE)
    @Action(input = "http://www.fri.uni-lj.si/xml2owl/rules/deleteRules", 
            output = "http://www.fri.uni-lj.si/xml2owl/rules/rules/deleteRulesResponse", 
            fault = { @FaultAction(className = Xml2OwlRulesException.class, 
                                   value = "http://www.fri.uni-lj.si/xml2owl/rules/rules/deleteRules/Fault/xml2owlRulesException") })
    @WebMethod(action = "http://www.fri.uni-lj.si/xml2owl/rules/deleteRules")
    @WebResult(name = "deleteResponse", 
               targetNamespace = "http://www.fri.uni-lj.si/xml2owl/rules", 
               partName = "part")
    public DeleteResponse deleteRules(@WebParam(name = "deleteRequest", 
                                                partName = "part", 
                                                targetNamespace = "http://www.fri.uni-lj.si/xml2owl/rules")
        DeleteRequest part) throws Xml2OwlRulesException {
        return rulesManager.deleteRules(part);
    }

    @javax.jws.soap.SOAPBinding(parameterStyle = javax.jws.soap.SOAPBinding.ParameterStyle.BARE)
    @Action(input = "http://www.fri.uni-lj.si/xml2owl/rules/validateFullRules", 
            output = "http://www.fri.uni-lj.si/xml2owl/rules/rules/validateFullRulesResponse", 
            fault = { @FaultAction(className = Xml2OwlRulesException.class, 
                                   value = "http://www.fri.uni-lj.si/xml2owl/rules/rules/validateFullRules/Fault/xml2owlRulesException") })
    @WebMethod(action = "http://www.fri.uni-lj.si/xml2owl/rules/validateFullRules")
    @WebResult(name = "validateFullResponse", 
               targetNamespace = "http://www.fri.uni-lj.si/xml2owl/rules", 
               partName = "part")
    public ValidateFullResponse validateFullRules(@WebParam(name = "validateFullRequest", 
                                                            partName = "part", 
                                                            targetNamespace = "http://www.fri.uni-lj.si/xml2owl/rules")
        ValidateFullRequest part) throws Xml2OwlRulesException {
        return rulesManager.validateRulesFull(part);
    }

    @javax.jws.soap.SOAPBinding(parameterStyle = javax.jws.soap.SOAPBinding.ParameterStyle.BARE)
    @Action(input = "http://www.fri.uni-lj.si/xml2owl/rules/validateByNameRules", 
            output = "http://www.fri.uni-lj.si/xml2owl/rules/rules/validateByNameRulesResponse", 
            fault = { @FaultAction(className = Xml2OwlRulesException.class, 
                                   value = "http://www.fri.uni-lj.si/xml2owl/rules/rules/validateByNameRules/Fault/xml2owlRulesException") })
    @WebMethod(action = "http://www.fri.uni-lj.si/xml2owl/rules/validateByNameRules")
    @WebResult(name = "validateByNameResponse", 
               targetNamespace = "http://www.fri.uni-lj.si/xml2owl/rules", 
               partName = "part")
    public ValidateByNameResponse validateByNameRules(@WebParam(name = "validateByNameRequest", 
                                                                partName = "part", 
                                                                targetNamespace = "http://www.fri.uni-lj.si/xml2owl/rules")
        ValidateByNameRequest part) throws Xml2OwlRulesException {
        return rulesManager.validateRulesByName(part);
    }
}
