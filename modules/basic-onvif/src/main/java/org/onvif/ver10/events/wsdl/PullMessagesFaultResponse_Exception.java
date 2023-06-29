
package org.onvif.ver10.events.wsdl;

import javax.xml.ws.WebFault;


/**
 * This class was generated by Apache CXF 3.5.5
 * Generated source version: 3.5.5
 */

@WebFault(name = "PullMessagesFaultResponse", targetNamespace = "http://www.onvif.org/ver10/events/wsdl")
public class PullMessagesFaultResponse_Exception extends Exception {

    private org.onvif.ver10.events.wsdl.PullMessagesFaultResponse faultInfo;

    public PullMessagesFaultResponse_Exception() {
        super();
    }

    public PullMessagesFaultResponse_Exception(String message) {
        super(message);
    }

    public PullMessagesFaultResponse_Exception(String message, java.lang.Throwable cause) {
        super(message, cause);
    }

    public PullMessagesFaultResponse_Exception(String message, org.onvif.ver10.events.wsdl.PullMessagesFaultResponse pullMessagesFaultResponse) {
        super(message);
        this.faultInfo = pullMessagesFaultResponse;
    }

    public PullMessagesFaultResponse_Exception(String message, org.onvif.ver10.events.wsdl.PullMessagesFaultResponse pullMessagesFaultResponse, java.lang.Throwable cause) {
        super(message, cause);
        this.faultInfo = pullMessagesFaultResponse;
    }

    public org.onvif.ver10.events.wsdl.PullMessagesFaultResponse getFaultInfo() {
        return this.faultInfo;
    }
}
