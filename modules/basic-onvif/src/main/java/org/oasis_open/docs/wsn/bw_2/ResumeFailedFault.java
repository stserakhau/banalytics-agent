
package org.oasis_open.docs.wsn.bw_2;

import javax.xml.ws.WebFault;


/**
 * This class was generated by Apache CXF 3.5.5
 * Generated source version: 3.5.5
 */

@WebFault(name = "ResumeFailedFault", targetNamespace = "http://docs.oasis-open.org/wsn/b-2")
public class ResumeFailedFault extends Exception {

    private org.oasis_open.docs.wsn.b_2.ResumeFailedFaultType faultInfo;

    public ResumeFailedFault() {
        super();
    }

    public ResumeFailedFault(String message) {
        super(message);
    }

    public ResumeFailedFault(String message, java.lang.Throwable cause) {
        super(message, cause);
    }

    public ResumeFailedFault(String message, org.oasis_open.docs.wsn.b_2.ResumeFailedFaultType resumeFailedFault) {
        super(message);
        this.faultInfo = resumeFailedFault;
    }

    public ResumeFailedFault(String message, org.oasis_open.docs.wsn.b_2.ResumeFailedFaultType resumeFailedFault, java.lang.Throwable cause) {
        super(message, cause);
        this.faultInfo = resumeFailedFault;
    }

    public org.oasis_open.docs.wsn.b_2.ResumeFailedFaultType getFaultInfo() {
        return this.faultInfo;
    }
}
