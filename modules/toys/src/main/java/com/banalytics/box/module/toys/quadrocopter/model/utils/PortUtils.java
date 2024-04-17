package com.banalytics.box.module.toys.quadrocopter.model.utils;

import com.fazecast.jSerialComm.SerialPort;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static com.banalytics.box.module.toys.quadrocopter.model.command.Command.MSP_SET_MOTOR;

@Slf4j
public class PortUtils {
//    private static final Map<Short, Long> awaitingResponseCommandTimeout = new HashMap<>();
//    public static AtomicInteger reqCounter = new AtomicInteger(0);
//    public static AtomicInteger resCounter = new AtomicInteger(0);

    private static final Object SEND_SYCH = new Object();

//    static AtomicInteger sendCounter = new AtomicInteger(0);
//    static AtomicInteger receiveCounter = new AtomicInteger(0);

    public static void receivedResponse(short code) {
//        synchronized (SEND_SYCH) {
//            long now = System.currentTimeMillis();
//            int val = receiveCounter.incrementAndGet();
//            log.info("{} - {}: =====> received code: {}", now, val, code);
//            awaitingResponseCommandTimeout.remove(code);
//        }
    }

    public static void send_message(SerialPort port, short code, byte[] data) {
        synchronized (SEND_SYCH) {
//            long now = System.currentTimeMillis();
//            int val = sendCounter.incrementAndGet();
//            Long timeout = awaitingResponseCommandTimeout.get(code);
//            if (timeout != null) {
//                if (now < timeout) {
//                    System.out.println("=====> double send: " + code);
//                    return;
//                }
//            }
//            log.info("{} - {}: =====> send code: {}", now, val, code);
//            int commandTimeout = switch (code) {
//                case MSP_SET_MOTOR -> 0;
//                default -> 50;
//            };
//            awaitingResponseCommandTimeout.put(code, now + commandTimeout);
//            reqCounter.incrementAndGet();

            byte[] bufferOut = code < 254 ? encode_message_v1(code, data) : encode_message_v2(code, data);

            port.writeBytes(bufferOut, bufferOut.length);
        }
    }

    private static byte[] encode_message_v1(short code, byte[] data) {
        int bufferSize = data.length + 6;
        byte[] buffer = new byte[bufferSize];
        buffer[0] = 36; // $
        buffer[1] = 77; // M
        buffer[2] = 60; // <
        buffer[3] = (byte) data.length;
        buffer[4] = (byte) code;

        byte checksum = (byte) (buffer[3] ^ buffer[4]);

        for (int i = 0; i < data.length; i++) {
            buffer[i + 5] = data[i];
            checksum ^= buffer[i + 5];
        }

        buffer[5 + data.length] = checksum;

        return buffer;
    }

    private static byte[] encode_message_v2(short code, byte[] data) {
        int bufferSize = data.length + 6;
        byte[] buffer = new byte[bufferSize];
        buffer[0] = 36; // $
        buffer[1] = 88; // X
        buffer[2] = 60; // <
        buffer[3] = 0;  // flag
        buffer[4] = (byte) (code & 0xFF);
        buffer[5] = (byte) (code >> 8);
        buffer[6] = (byte) (data.length & 0xFF);
        buffer[7] = (byte) ((data.length >> 8) & 0xFF);

        byte checksum = (byte) (buffer[3] ^ buffer[4]);

        for (int ii = 0; ii < data.length; ii++) {
            buffer[8 + ii] = data[ii];
        }

        buffer[bufferSize - 1] = crc8_dvb_s2_data(buffer, 3, bufferSize - 1);

        return buffer;
    }

    private static byte crc8_dvb_s2_data(byte[] data, int start, int end) {
        byte crc = 0;
        for (int ii = start; ii < end; ii++) {
            crc = crc8_dvb_s2(crc, data[ii]);
        }
        return crc;
    }

    private static byte crc8_dvb_s2(byte crc, byte ch) {
        crc ^= ch;
        for (int ii = 0; ii < 8; ii++) {
            if ((crc & 0x80) > 0) {
                crc = (byte) (((crc << 1) & 0xFF) ^ 0xD5);
            } else {
                crc = (byte) ((crc << 1) & 0xFF);
            }
        }
        return crc;
    }

    public static SplitResult split(byte[] buffer) {
        List<byte[]> splitRes = new ArrayList<>(5);

        byte[] appendix = EMPTY;
        int startPos;
        for (int i = 0; i < buffer.length; ) {
            if (buffer[i] == 36) {
                startPos = i;

                if (i + 3 >= buffer.length) {
                    appendix = new byte[buffer.length - i];
                    System.arraycopy(buffer, startPos, appendix, 0, appendix.length);
                    break;
                } else if (buffer[i + 1] == 77 && buffer[i + 2] == 62) {
                    int length = buffer[i + 3] + 6;
                    byte[] response = new byte[length];
                    if (i + length <= buffer.length) {
                        System.arraycopy(buffer, startPos, response, 0, response.length);
                    } else {
                        appendix = new byte[buffer.length - i];
                        System.arraycopy(buffer, startPos, appendix, 0, appendix.length);
                        break;
                    }
                    splitRes.add(response);
                    i += length;
                } else if (buffer[i + 1] == 88 && buffer[i + 2] == 62) {//todo draft implementation with bugs
                    int length = 9 + ((short) buffer[i + 7]) << 8 ^ (short) buffer[i + 6];//replace to byte buf probably error
                    byte[] response = new byte[length];
                    System.arraycopy(buffer, startPos, response, 0, response.length);
                    splitRes.add(response);
                    i += length;
                } else {
                    i++;
                }
            }
        }
        return new SplitResult(splitRes, appendix);
    }

    public record SplitResult(List<byte[]> parts, byte[] appendix) {
    }

    public final static byte[] EMPTY = new byte[0];
}
