
package org.onvif.ver10.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for AFModes.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <pre>
 * &lt;simpleType name="AFModes"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="OnceAfterMove"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "AFModes")
@XmlEnum
public enum AFModes {


    /**
     * Focus of a moving camera is updated only once after stopping a pan, tilt or zoom movement.
     * 
     */
    @XmlEnumValue("OnceAfterMove")
    ONCE_AFTER_MOVE("OnceAfterMove");
    private final String value;

    AFModes(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static AFModes fromValue(String v) {
        for (AFModes c: AFModes.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
