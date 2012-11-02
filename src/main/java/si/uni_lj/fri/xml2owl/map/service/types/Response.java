
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
 *         &lt;element name="owl" type="{http://www.w3.org/2001/XMLSchema}string"/>
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
    "owl"
})
@XmlRootElement(name = "response")
public class Response {

    @XmlElement(required = true)
    protected String owl;

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

}
