package org.onvif.ver10.events.wsdl;

import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.Service;
import org.oasis_open.docs.wsn.bw_2.CreatePullPoint;
import org.oasis_open.docs.wsn.bw_2.NotificationConsumer;
import org.oasis_open.docs.wsn.bw_2.NotificationProducer;
import org.oasis_open.docs.wsn.bw_2.PausableSubscriptionManager;
import org.oasis_open.docs.wsn.bw_2.PullPoint;
import org.oasis_open.docs.wsn.bw_2.SubscriptionManager;

/**
 * This class was generated by Apache CXF 3.5.5
 * Generated source version: 3.5.5
 *
 */
@WebServiceClient(name = "EventService",
                  wsdlLocation = "null",
                  targetNamespace = "http://www.onvif.org/ver10/events/wsdl")
public class EventService extends Service {

    public final static URL WSDL_LOCATION;

    public final static QName SERVICE = new QName("http://www.onvif.org/ver10/events/wsdl", "EventService");
    public final static QName SubscriptionManagerPort = new QName("http://www.onvif.org/ver10/events/wsdl", "SubscriptionManagerPort");
    public final static QName PullPointSubscriptionPort = new QName("http://www.onvif.org/ver10/events/wsdl", "PullPointSubscriptionPort");
    public final static QName EventPort = new QName("http://www.onvif.org/ver10/events/wsdl", "EventPort");
    public final static QName NotificationProducerPort = new QName("http://www.onvif.org/ver10/events/wsdl", "NotificationProducerPort");
    public final static QName PullPointPort = new QName("http://www.onvif.org/ver10/events/wsdl", "PullPointPort");
    public final static QName PausableSubscriptionManagerPort = new QName("http://www.onvif.org/ver10/events/wsdl", "PausableSubscriptionManagerPort");
    public final static QName NotificationConsumerPort = new QName("http://www.onvif.org/ver10/events/wsdl", "NotificationConsumerPort");
    public final static QName CreatePullPointPort = new QName("http://www.onvif.org/ver10/events/wsdl", "CreatePullPointPort");
    static {
        WSDL_LOCATION = null;
    }

    public EventService(URL wsdlLocation) {
        super(wsdlLocation, SERVICE);
    }

    public EventService(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public EventService() {
        super(WSDL_LOCATION, SERVICE);
    }

    public EventService(WebServiceFeature ... features) {
        super(WSDL_LOCATION, SERVICE, features);
    }

    public EventService(URL wsdlLocation, WebServiceFeature ... features) {
        super(wsdlLocation, SERVICE, features);
    }

    public EventService(URL wsdlLocation, QName serviceName, WebServiceFeature ... features) {
        super(wsdlLocation, serviceName, features);
    }




    /**
     *
     * @return
     *     returns SubscriptionManager
     */
    @WebEndpoint(name = "SubscriptionManagerPort")
    public SubscriptionManager getSubscriptionManagerPort() {
        return super.getPort(SubscriptionManagerPort, SubscriptionManager.class);
    }

    /**
     *
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns SubscriptionManager
     */
    @WebEndpoint(name = "SubscriptionManagerPort")
    public SubscriptionManager getSubscriptionManagerPort(WebServiceFeature... features) {
        return super.getPort(SubscriptionManagerPort, SubscriptionManager.class, features);
    }


    /**
     *
     * @return
     *     returns PullPointSubscription
     */
    @WebEndpoint(name = "PullPointSubscriptionPort")
    public PullPointSubscription getPullPointSubscriptionPort() {
        return super.getPort(PullPointSubscriptionPort, PullPointSubscription.class);
    }

    /**
     *
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns PullPointSubscription
     */
    @WebEndpoint(name = "PullPointSubscriptionPort")
    public PullPointSubscription getPullPointSubscriptionPort(WebServiceFeature... features) {
        return super.getPort(PullPointSubscriptionPort, PullPointSubscription.class, features);
    }


    /**
     *
     * @return
     *     returns EventPortType
     */
    @WebEndpoint(name = "EventPort")
    public EventPortType getEventPort() {
        return super.getPort(EventPort, EventPortType.class);
    }

    /**
     *
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns EventPortType
     */
    @WebEndpoint(name = "EventPort")
    public EventPortType getEventPort(WebServiceFeature... features) {
        return super.getPort(EventPort, EventPortType.class, features);
    }


    /**
     *
     * @return
     *     returns NotificationProducer
     */
    @WebEndpoint(name = "NotificationProducerPort")
    public NotificationProducer getNotificationProducerPort() {
        return super.getPort(NotificationProducerPort, NotificationProducer.class);
    }

    /**
     *
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns NotificationProducer
     */
    @WebEndpoint(name = "NotificationProducerPort")
    public NotificationProducer getNotificationProducerPort(WebServiceFeature... features) {
        return super.getPort(NotificationProducerPort, NotificationProducer.class, features);
    }


    /**
     *
     * @return
     *     returns PullPoint
     */
    @WebEndpoint(name = "PullPointPort")
    public PullPoint getPullPointPort() {
        return super.getPort(PullPointPort, PullPoint.class);
    }

    /**
     *
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns PullPoint
     */
    @WebEndpoint(name = "PullPointPort")
    public PullPoint getPullPointPort(WebServiceFeature... features) {
        return super.getPort(PullPointPort, PullPoint.class, features);
    }


    /**
     *
     * @return
     *     returns PausableSubscriptionManager
     */
    @WebEndpoint(name = "PausableSubscriptionManagerPort")
    public PausableSubscriptionManager getPausableSubscriptionManagerPort() {
        return super.getPort(PausableSubscriptionManagerPort, PausableSubscriptionManager.class);
    }

    /**
     *
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns PausableSubscriptionManager
     */
    @WebEndpoint(name = "PausableSubscriptionManagerPort")
    public PausableSubscriptionManager getPausableSubscriptionManagerPort(WebServiceFeature... features) {
        return super.getPort(PausableSubscriptionManagerPort, PausableSubscriptionManager.class, features);
    }


    /**
     *
     * @return
     *     returns NotificationConsumer
     */
    @WebEndpoint(name = "NotificationConsumerPort")
    public NotificationConsumer getNotificationConsumerPort() {
        return super.getPort(NotificationConsumerPort, NotificationConsumer.class);
    }

    /**
     *
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns NotificationConsumer
     */
    @WebEndpoint(name = "NotificationConsumerPort")
    public NotificationConsumer getNotificationConsumerPort(WebServiceFeature... features) {
        return super.getPort(NotificationConsumerPort, NotificationConsumer.class, features);
    }


    /**
     *
     * @return
     *     returns CreatePullPoint
     */
    @WebEndpoint(name = "CreatePullPointPort")
    public CreatePullPoint getCreatePullPointPort() {
        return super.getPort(CreatePullPointPort, CreatePullPoint.class);
    }

    /**
     *
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns CreatePullPoint
     */
    @WebEndpoint(name = "CreatePullPointPort")
    public CreatePullPoint getCreatePullPointPort(WebServiceFeature... features) {
        return super.getPort(CreatePullPointPort, CreatePullPoint.class, features);
    }

}
