package com.banalytics.box.module.onvif.client;

import com.banalytics.box.module.onvif.thing.OnvifConfiguration;
import javax.xml.soap.*;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.MessageDigestAlgorithms;
import org.apache.commons.lang3.RandomUtils;
import org.onvif.ver10.device.wsdl.Device;
import org.onvif.ver10.schema.DateTime;
import org.onvif.ver10.schema.Time;

import javax.xml.namespace.QName;
import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

import static com.banalytics.box.module.onvif.client.WSS4JConstants.*;

public class SecurityHandler implements SOAPHandler<SOAPMessageContext> {

    private final String username;
    private final String password;
    private Device device;
//    private final OnvifConfiguration.TimeType timeType;

    public SecurityHandler(String username, String password, /*OnvifConfiguration.TimeType timeType,*/ Device device) {
        this.username = username;
        this.password = password;
//        this.timeType = timeType;
        this.device = device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    @Override
    public boolean handleMessage(final SOAPMessageContext msgCtx) {
        // Indicator telling us which direction this message is going in
        final Boolean outInd = (Boolean) msgCtx.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

        // Handler must only add security headers to outbound messages
        if (outInd) {
            try {
                // Create the xml
                SOAPMessage soapMessage = msgCtx.getMessage();
                SOAPEnvelope envelope = soapMessage.getSOAPPart().getEnvelope();
                SOAPHeader header = envelope.getHeader();
                if (header == null) header = envelope.addHeader();

                SOAPPart sp = soapMessage.getSOAPPart();
                SOAPEnvelope se = sp.getEnvelope();
                se.addNamespaceDeclaration(WSSE_PREFIX, WSSE_NS);
                se.addNamespaceDeclaration(WSU_PREFIX, WSU_NS);

                SOAPElement securityElem = header.addChildElement(WSSE_LN, WSSE_PREFIX);
                // securityElem.setAttribute("SOAP-ENV:mustUnderstand", "1");

                SOAPElement usernameTokenElem =
                        securityElem.addChildElement(USERNAME_TOKEN_LN, WSSE_PREFIX);

                SOAPElement usernameElem = usernameTokenElem.addChildElement(USERNAME_LN, WSSE_PREFIX);
                usernameElem.setTextContent(username);

                Token token = getToken();

                SOAPElement passwordElem = usernameTokenElem.addChildElement(PASSWORD_LN, WSSE_PREFIX);
                passwordElem.setAttribute(PASSWORD_TYPE_ATTR, PASSWORD_DIGEST);
                passwordElem.setTextContent(token.passwordDigest);

                SOAPElement nonceElem = usernameTokenElem.addChildElement(NONCE_LN, WSSE_PREFIX);
                nonceElem.setAttribute("EncodingType", BASE64_ENCODING);
                nonceElem.setTextContent(token.nonce);

                SOAPElement createdElem = usernameTokenElem.addChildElement(CREATED_LN, WSU_PREFIX);
                createdElem.setTextContent(token.created);
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        }
        return true;
    }

    public String getUTCTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-d'T'HH:mm:ss'Z'");
        sdf.setTimeZone(new SimpleTimeZone(SimpleTimeZone.UTC_TIME, "UTC"));
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        return sdf.format(cal.getTime());
    }

    private Token getToken() {
        try {
            byte[] nonce = RandomUtils.nextBytes(20);
            String timestamp = getUTCTime();
            /*String timestamp = timeType == OnvifConfiguration.TimeType.LOCAL_UTC ? getUTCTime() : (
                    timeType == OnvifConfiguration.TimeType.CAMERA_UTC_WITH_MILLIS ?
                            toUTCDateTimeWithMillis(device.getSystemDateAndTime().getUTCDateTime())
                            : toUTCDateTime(device.getSystemDateAndTime().getUTCDateTime())
            );*/
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(nonce);
            baos.write(timestamp.getBytes());
            baos.write(password.getBytes());
            baos.close();

            MessageDigest SHA1 = MessageDigest.getInstance(MessageDigestAlgorithms.SHA_1);
            SHA1.reset();
            SHA1.update(baos.toByteArray());

            return new Token(
                    Base64.encodeBase64String(nonce),
                    timestamp,
                    Base64.encodeBase64String(SHA1.digest())
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static class Token {
        final String nonce;
        final String created;
        final String passwordDigest;

        public Token(String nonce, String created, String password) {
            this.nonce = nonce;
            this.created = created;
            this.passwordDigest = password;
        }

        @Override
        public String toString() {
            return "Token{" +
                    "nonce='" + nonce + '\'' +
                    ", created='" + created + '\'' +
                    ", password='" + passwordDigest + '\'' +
                    '}';
        }
    }


    @Override
    public boolean handleFault(SOAPMessageContext context) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void close(MessageContext messageContext) {
        // TODO Auto-generated method stub
    }

    @Override
    public Set<QName> getHeaders() {
        // TODO Auto-generated method stub
        return null;
    }

    private String toUTCDateTime(DateTime dateTime) {
        org.onvif.ver10.schema.Date date = dateTime.getDate();
        Time time = dateTime.getTime();
        LocalDateTime ldt = LocalDateTime.of(
                date.getYear(),
                date.getMonth(),
                date.getDay(),
                time.getHour(),
                time.getMinute(),
                time.getSecond()
        );
        return ldt + " UTC";
    }

    private String toUTCDateTimeWithMillis(DateTime dateTime) {
        //yyyy-MM-dd'T'HH:mm:ss.SSS zzz
        org.onvif.ver10.schema.Date date = dateTime.getDate();
        Time time = dateTime.getTime();
        LocalDateTime ldt = LocalDateTime.of(
                date.getYear(),
                date.getMonth(),
                date.getDay(),
                time.getHour(),
                time.getMinute(),
                time.getSecond()
        );
        return ldt + ".000 UTC";
    }
}
