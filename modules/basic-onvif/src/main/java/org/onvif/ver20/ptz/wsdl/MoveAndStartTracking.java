
package org.onvif.ver20.ptz.wsdl;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.cxf.xjc.runtime.JAXBToStringStyle;
import org.onvif.ver10.schema.GeoLocation;
import org.onvif.ver10.schema.PTZSpeed;
import org.onvif.ver10.schema.PTZVector;
import org.w3c.dom.Element;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="ProfileToken" type="{http://www.onvif.org/ver10/schema}ReferenceToken"/&gt;
 *         &lt;element name="PresetToken" type="{http://www.onvif.org/ver10/schema}ReferenceToken" minOccurs="0"/&gt;
 *         &lt;element name="GeoLocation" type="{http://www.onvif.org/ver10/schema}GeoLocation" minOccurs="0"/&gt;
 *         &lt;element name="TargetPosition" type="{http://www.onvif.org/ver10/schema}PTZVector" minOccurs="0"/&gt;
 *         &lt;element name="Speed" type="{http://www.onvif.org/ver10/schema}PTZSpeed" minOccurs="0"/&gt;
 *         &lt;element name="ObjectID" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/&gt;
 *         &lt;any processContents='lax' maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "profileToken",
    "presetToken",
    "geoLocation",
    "targetPosition",
    "speed",
    "objectID",
    "any"
})
@XmlRootElement(name = "MoveAndStartTracking")
public class MoveAndStartTracking {

    @XmlElement(name = "ProfileToken", required = true)
    protected String profileToken;
    @XmlElement(name = "PresetToken")
    protected String presetToken;
    @XmlElement(name = "GeoLocation")
    protected GeoLocation geoLocation;
    @XmlElement(name = "TargetPosition")
    protected PTZVector targetPosition;
    @XmlElement(name = "Speed")
    protected PTZSpeed speed;
    @XmlElement(name = "ObjectID")
    protected BigInteger objectID;
    @XmlAnyElement(lax = true)
    protected List<Object> any;

    /**
     * Gets the value of the profileToken property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProfileToken() {
        return profileToken;
    }

    /**
     * Sets the value of the profileToken property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProfileToken(String value) {
        this.profileToken = value;
    }

    /**
     * Gets the value of the presetToken property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPresetToken() {
        return presetToken;
    }

    /**
     * Sets the value of the presetToken property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPresetToken(String value) {
        this.presetToken = value;
    }

    /**
     * Gets the value of the geoLocation property.
     * 
     * @return
     *     possible object is
     *     {@link GeoLocation }
     *     
     */
    public GeoLocation getGeoLocation() {
        return geoLocation;
    }

    /**
     * Sets the value of the geoLocation property.
     * 
     * @param value
     *     allowed object is
     *     {@link GeoLocation }
     *     
     */
    public void setGeoLocation(GeoLocation value) {
        this.geoLocation = value;
    }

    /**
     * Gets the value of the targetPosition property.
     * 
     * @return
     *     possible object is
     *     {@link PTZVector }
     *     
     */
    public PTZVector getTargetPosition() {
        return targetPosition;
    }

    /**
     * Sets the value of the targetPosition property.
     * 
     * @param value
     *     allowed object is
     *     {@link PTZVector }
     *     
     */
    public void setTargetPosition(PTZVector value) {
        this.targetPosition = value;
    }

    /**
     * Gets the value of the speed property.
     * 
     * @return
     *     possible object is
     *     {@link PTZSpeed }
     *     
     */
    public PTZSpeed getSpeed() {
        return speed;
    }

    /**
     * Sets the value of the speed property.
     * 
     * @param value
     *     allowed object is
     *     {@link PTZSpeed }
     *     
     */
    public void setSpeed(PTZSpeed value) {
        this.speed = value;
    }

    /**
     * Gets the value of the objectID property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getObjectID() {
        return objectID;
    }

    /**
     * Sets the value of the objectID property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setObjectID(BigInteger value) {
        this.objectID = value;
    }

    /**
     * Gets the value of the any property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the any property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAny().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Object }
     * {@link Element }
     * 
     * 
     */
    public List<Object> getAny() {
        if (any == null) {
            any = new ArrayList<Object>();
        }
        return this.any;
    }

    /**
     * Generates a String representation of the contents of this type.
     * This is an extension method, produced by the 'ts' xjc plugin
     * 
     */
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, JAXBToStringStyle.DEFAULT_STYLE);
    }

}
