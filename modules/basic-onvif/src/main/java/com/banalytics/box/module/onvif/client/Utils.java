package com.banalytics.box.module.onvif.client;

import javax.xml.ws.BindingProvider;
import org.apache.cxf.binding.soap.Soap12;
import org.apache.cxf.binding.soap.SoapBindingConfiguration;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.onvif.ver10.schema.PTZSpeed;
import org.onvif.ver10.schema.PTZVector;
import org.onvif.ver10.schema.Vector1D;
import org.onvif.ver10.schema.Vector2D;

import java.net.MalformedURLException;
import java.net.URL;

public class Utils {
    public static JaxWsProxyFactoryBean getServiceProxy(BindingProvider servicePort, String serviceAddr) {
        return getServiceProxy(servicePort, serviceAddr, null);
    }

    public static JaxWsProxyFactoryBean getServiceProxy(BindingProvider servicePort, String serviceAddr, SecurityHandler securityHandler) {

        JaxWsProxyFactoryBean proxyFactory = new JaxWsProxyFactoryBean();
        proxyFactory.getHandlers();

        if (serviceAddr != null) {
            proxyFactory.setAddress(serviceAddr);
        }
        proxyFactory.setServiceClass(servicePort.getClass());

        SoapBindingConfiguration config = new SoapBindingConfiguration();

        config.setVersion(Soap12.getInstance());
        proxyFactory.setBindingConfig(config);
        Client deviceClient = ClientProxy.getClient(servicePort);

//        if (verbose) {
        // these logging interceptors are depreciated, but should be fine for debugging/development
        // use.
//        proxyFactory.getOutInterceptors().add(new LoggingOutInterceptor());
//        proxyFactory.getInInterceptors().add(new LoggingInInterceptor());
//        proxyFactory.getInFaultInterceptors().add(new LoggingInInterceptor());
//        }

        HTTPConduit http = (HTTPConduit) deviceClient.getConduit();
        if (securityHandler != null) {
            proxyFactory.getHandlers().add(securityHandler);
        }
        HTTPClientPolicy httpClientPolicy = http.getClient();
        httpClientPolicy.setConnectionTimeout(36000);
        httpClientPolicy.setReceiveTimeout(32000);
        httpClientPolicy.setAllowChunking(false);

        return proxyFactory;
    }

    public static PTZVector buildPTZVector(float x, float y, float z) {
        PTZVector ptzVector = new PTZVector();

        {
            Vector2D panTilt = new Vector2D();
            panTilt.setX(x);
            panTilt.setY(y);
            ptzVector.setPanTilt(panTilt);

            {
                Vector1D zoom = new Vector1D();
                zoom.setX(z);
                ptzVector.setZoom(zoom);
            }
        }

        return ptzVector;
    }

    public static PTZSpeed buildPTZSpeed(float x, float y, float z) {
        PTZSpeed speed = new PTZSpeed();
        {
            if (x != 0 || y != 0) {
                Vector2D panTilt = new Vector2D();
                panTilt.setX(x);
                panTilt.setY(y);
                speed.setPanTilt(panTilt);
            }
            if (z != 0) {
                Vector1D zoom = new Vector1D();
                zoom.setX(z);
                speed.setZoom(zoom);
            }
        }
        return speed;
    }

    public static String getDefaultDeviceURL(String ip, int port) {
        return "http://" + ip + ":" + port + "/onvif/device_service";
    }

    public static String update(String url, String ip, int port) {
        try {
            URL u = new URL(url);
            return u.getProtocol() + "://" + ip + ":" + port + u.getPath();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
