
package org.onvif.ver10.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ImageSendingType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <pre>
 * &lt;simpleType name="ImageSendingType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="Embedded"/&gt;
 *     &lt;enumeration value="LocalStorage"/&gt;
 *     &lt;enumeration value="RemoteStorage"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ImageSendingType")
@XmlEnum
public enum ImageSendingType {

    @XmlEnumValue("Embedded")
    EMBEDDED("Embedded"),
    @XmlEnumValue("LocalStorage")
    LOCAL_STORAGE("LocalStorage"),
    @XmlEnumValue("RemoteStorage")
    REMOTE_STORAGE("RemoteStorage");
    private final String value;

    ImageSendingType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ImageSendingType fromValue(String v) {
        for (ImageSendingType c: ImageSendingType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
