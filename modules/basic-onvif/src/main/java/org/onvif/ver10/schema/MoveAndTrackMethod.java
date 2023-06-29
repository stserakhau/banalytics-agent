
package org.onvif.ver10.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for MoveAndTrackMethod.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <pre>
 * &lt;simpleType name="MoveAndTrackMethod"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="PresetToken"/&gt;
 *     &lt;enumeration value="GeoLocation"/&gt;
 *     &lt;enumeration value="PTZVector"/&gt;
 *     &lt;enumeration value="ObjectID"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "MoveAndTrackMethod")
@XmlEnum
public enum MoveAndTrackMethod {

    @XmlEnumValue("PresetToken")
    PRESET_TOKEN("PresetToken"),
    @XmlEnumValue("GeoLocation")
    GEO_LOCATION("GeoLocation"),
    @XmlEnumValue("PTZVector")
    PTZ_VECTOR("PTZVector"),
    @XmlEnumValue("ObjectID")
    OBJECT_ID("ObjectID");
    private final String value;

    MoveAndTrackMethod(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static MoveAndTrackMethod fromValue(String v) {
        for (MoveAndTrackMethod c: MoveAndTrackMethod.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
