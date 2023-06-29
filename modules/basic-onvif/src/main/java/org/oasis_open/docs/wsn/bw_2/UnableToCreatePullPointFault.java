
package org.oasis_open.docs.wsn.bw_2;

import javax.xml.ws.WebFault;


/**
 * This class was generated by Apache CXF 3.5.5
 * Generated source version: 3.5.5
 */

@WebFault(name = "UnableToCreatePullPointFault", targetNamespace = "http://docs.oasis-open.org/wsn/b-2")
public class UnableToCreatePullPointFault extends Exception {

    private org.oasis_open.docs.wsn.b_2.UnableToCreatePullPointFaultType faultInfo;

    public UnableToCreatePullPointFault() {
        super();
    }

    public UnableToCreatePullPointFault(String message) {
        super(message);
    }

    public UnableToCreatePullPointFault(String message, java.lang.Throwable cause) {
        super(message, cause);
    }

    public UnableToCreatePullPointFault(String message, org.oasis_open.docs.wsn.b_2.UnableToCreatePullPointFaultType unableToCreatePullPointFault) {
        super(message);
        this.faultInfo = unableToCreatePullPointFault;
    }

    public UnableToCreatePullPointFault(String message, org.oasis_open.docs.wsn.b_2.UnableToCreatePullPointFaultType unableToCreatePullPointFault, java.lang.Throwable cause) {
        super(message, cause);
        this.faultInfo = unableToCreatePullPointFault;
    }

    public org.oasis_open.docs.wsn.b_2.UnableToCreatePullPointFaultType getFaultInfo() {
        return this.faultInfo;
    }
}
