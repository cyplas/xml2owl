
package si.uni_lj.fri.xml2owl.map.service.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="data" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="owl" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="rules" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "data",
    "owl",
    "rules"
})
@XmlRootElement(name = "request")
public class Request {

    @XmlElement(required = true)
    protected String data;
    @XmlElement(required = true)
    protected String owl;
    @XmlElement(required = true)
    protected String rules;

    /**
     * Gets the value of the data property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getData() {
        return data;
    }

    /**
     * Sets the value of the data property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setData(String value) {
        this.data = value;
    }

    /**
     * Gets the value of the owl property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOwl() {
        return owl;
    }

    /**
     * Sets the value of the owl property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOwl(String value) {
        this.owl = value;
    }

    /**
     * Gets the value of the rules property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRules() {
        return rules;
    }

    /**
     * Sets the value of the rules property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRules(String value) {
        this.rules = value;
    }

}

