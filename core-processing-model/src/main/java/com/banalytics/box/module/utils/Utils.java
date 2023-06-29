package com.banalytics.box.module.utils;

import com.banalytics.box.api.integration.webrtc.channel.NodeDescriptor;
import com.banalytics.box.module.*;
import lombok.extern.slf4j.Slf4j;

import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Slf4j
public class Utils {

    public static String md5Hash(String value) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(value.getBytes());
        byte[] digest = md.digest();
        return  DatatypeConverter.printHexBinary(digest).toUpperCase();
    }

    public static NodeDescriptor.NodeType nodeType(Class cls) {
        if (IAction.class.isAssignableFrom(cls)) {
            return NodeDescriptor.NodeType.ACTION;
        }
        if (ITask.class.isAssignableFrom(cls)) {
            return NodeDescriptor.NodeType.TASK;
        }
        if (Thing.class.isAssignableFrom(cls)) {
            return NodeDescriptor.NodeType.THING;
        }
        throw new RuntimeException("Unknown class type: " + cls);
    }

    public static boolean isSupportsSubtasks(Class cls) {
        if (cls.getName().toLowerCase().contains("motiondetection") || cls.getName().toLowerCase().contains("sounddetection")) {
            return false;
        }
        return AbstractListOfTask.class.isAssignableFrom(cls);
    }

    public static boolean isSupportsMediaStream(Class cls) {
        if (MediaCaptureCallbackSupport.class.isAssignableFrom(cls)) {
            if (cls.getName().contains("VideoRecording")) {
                return false;
            }
            return true;
        }
        return false;
    }
}
