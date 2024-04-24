import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Random;

import com.banalytics.box.module.toys.quadrocopter.model.utils.PortUtils;
import com.fazecast.jSerialComm.SerialPort;

import static com.banalytics.box.module.toys.quadrocopter.model.utils.PortUtils.send_message;

//https://elektroweb.pl/konwerteryekspandery/631-konwerter-usb-uart-rs232-sterownik-pl2303hx-przewod-1m--5904162804610.html
//KONWERTER USB-UART RS232 STEROWNIK PL2303HX 1M
/*
Konwerter USB-UART na sterowniku PL2303HX na 1 metrowym przewodzie zakończonym żeńskimi wtyczkami przystosowanymi do typowych goldpinów. Wygodny w użyciu, cała elektronika została schowana we wtyczce USB.

Sprawnie emuluje port UART RS232.

Przewód Czerwony <-> 5V
Przewód Czarny <-> GND
Przewód Biały <-> Tx
Przewód Zielony <-> Rx
Specyfikacja:
napięcie zasilania 5V port USB
sterownik PL2303HX
konwerter USB - TTL UART
długość przewodu 1m
 */
public class BetaflightMSPSender {
    // Указываем COM порт, через который мы будем отправлять данные
    public static void main(String[] args) {
        try {
            // Открываем порт
            final SerialPort serialPort = SerialPort.getCommPort("COM6");
            // Устанавливаем параметры порта
            serialPort.setBaudRate(115200);
            serialPort.setNumDataBits(8);
            serialPort.setNumStopBits(1);
            serialPort.setParity(SerialPort.NO_PARITY);
            serialPort.setFlowControl(SerialPort.FLOW_CONTROL_DISABLED);
//            serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);

            if (!serialPort.openPort()) {
                throw new RuntimeException(
                        "Can't open port.\n Error code: %s\nError location %s".formatted(
                                serialPort.getLastErrorCode(),
                                serialPort.getLastErrorLocation()
                        )
                );
            }

            // Получаем выходной поток для отправки данных
            InputStream inputStream = serialPort.getInputStream();

//            Thread telemetryRead = new Thread(() -> {
//                try {
//                    while (serialPort.isOpen()) {
//                        int available = inputStream.available();
//                        if (available > 0) {
//                            int data = inputStream.read();
////                            System.out.println(data);
//                        } else {
//                            Thread.sleep(50);
//                        }
//                    }
//                } catch (Exception e) {
//                    throw new RuntimeException(e);
//                }
//            });
//            telemetryRead.start();

            sendMSPSetRawRC(serialPort, (short) 1500, (short) 1500, (short) 1000, (short) 1500, (short) 900, (short) 900, (short) 900, (short) 900, (short) 900, (short) 900, (short) 900, (short) 900, (short) 900, (short) 900, (short) 900, (short) 900, (short) 900, (short) 900);
            Thread.sleep(2000);
//            Thread receiver = new Thread(() -> {
//                while (true) {
//                    sendRSSI(serialPort, (byte) 80);
//                    try {
//                        Thread.sleep(50);
//                    } catch (InterruptedException e) {
//                        throw new RuntimeException(e);
//                    }
//                }
//            });
//            receiver.start();

            Thread joystick = new Thread(() -> {
                try {
                    for (int i = 0; i < 50; i++) {
                        sendMSPSetRawRC(serialPort, (short) 1500, (short) 1500, (short) 900, (short) 1500, (short) 900, (short) 900, (short) 900, (short) 900, (short) 900, (short) 900, (short) 900, (short) 900, (short) 900, (short) 900, (short) 900, (short) 900, (short) 900, (short) 900);
                        Thread.sleep(50);
                    }
                    for (int i = 0; i < 50; i++) {
                        sendMSPSetRawRC(serialPort, (short) 1500, (short) 1500, (short) 900, (short) 1500, (short) 2000, (short) 900, (short) 900, (short) 900, (short) 900, (short) 900, (short) 900, (short) 900, (short) 900, (short) 900, (short) 900, (short) 900, (short) 900, (short) 900);
                        Thread.sleep(50);
                    }
                    System.out.println("============= Launch quadro");
                    while (true) {
                        sendMSPSetRawRC(serialPort, (short) 1500, (short) 1500, (short) 1000, (short) 1500, (short) 2000, (short) 900, (short) 900, (short) 900, (short) 900, (short) 900, (short) 900, (short) 900, (short) 900, (short) 900, (short) 900, (short) 900, (short) 900, (short) 900);
                        Thread.sleep(100);
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
            joystick.start();


//            sendMotor(serialPort, (short) 1010, (short) 0, (short) 0, (short) 0);
//            Thread.sleep(1000);
//            sendMotor(serialPort, (short) 1000, (short) 0, (short) 0, (short) 0);
//            Thread.sleep(1000);

//            requestModeRanges(serialPort);
//            requestArmConfig(serialPort);


            Thread.sleep(10000);
            sendMSPSetRawRC(serialPort, (short) 1500, (short) 1500, (short) 900, (short) 1500, (short) 900, (short) 900, (short) 900, (short) 900, (short) 900, (short) 900, (short) 900, (short) 900, (short) 900, (short) 900, (short) 900, (short) 900, (short) 900, (short) 900);
            joystick.interrupt();
//            receiver.interrupt();
//            telemetryRead.interrupt();
            inputStream.close();
            serialPort.closePort();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


   /* private static void sendMotor(SerialPort port, short e1, short e2, short e3, short e4) {
        short MSP_SET_MOTOR = 214;

        byte[] data = new byte[8];
        ByteBuffer bb = ByteBuffer.wrap(data);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.putShort(e1);
        bb.putShort(e2);
        bb.putShort(e3);
        bb.putShort(e4);

        send_message(port, MSP_SET_MOTOR, data);
    }

    private static void sendRSSI(SerialPort port, byte value) {
        byte[] data = new byte[7];
        data[0] = '$';
        data[1] = 0x08;
        data[2] = 0x02;
        data[3] = 0x00;
        data[4] = value;
        byte checksum = 0;
        for (int i = 1; i < 5; i++) { // Start from index 1 to exclude start byte
            checksum ^= data[i];
        }
        data[5] = checksum;

        port.writeBytes(data, data.length);
    }

    private static void readRSSI(SerialPort port) {
        short MSP_RSSI_CONFIG = 50;

        send_message(port, MSP_RSSI_CONFIG, new byte[]{127});
    }

    private static void requestArmConfig(SerialPort port) {
        short MSP_ARMING_CONFIG = 61;

        send_message(port, MSP_ARMING_CONFIG, new byte[0]);
    }

    private static void requestModeRanges(SerialPort port) {
        short MSP_MODE_RANGES = 34;

        send_message(port, MSP_MODE_RANGES, new byte[0]);
    }

    private static void setModeRange(SerialPort port, short mode) {
        short MSP_SET_MODE = 62;

        byte[] data = new byte[2];
        ByteBuffer bb = ByteBuffer.wrap(data);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.putShort(mode);

        send_message(port, MSP_SET_MODE, data);
    }*/

    private static void sendMSPSetRawRC(SerialPort port,
                                        short roll, short pitch, short throttle, short yaw,
                                        short aux1, short aux2, short aux3, short aux4, short aux5,
                                        short aux6, short aux7, short aux8, short aux9, short aux10,
                                        short aux11, short aux12, short aux13, short aux14
    ) {
        final byte MSP_SET_RAW_RC = (byte) 200;

        byte[] data = new byte[36];
        ByteBuffer bb = ByteBuffer.wrap(data);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.putShort(roll);
        bb.putShort(pitch);
        bb.putShort(throttle);
        bb.putShort(yaw);
        bb.putShort(aux1);
        bb.putShort(aux2);
        bb.putShort(aux3);
        bb.putShort(aux4);
        bb.putShort(aux5);
        bb.putShort(aux6);
        bb.putShort(aux7);
        bb.putShort(aux8);
        bb.putShort(aux9);
        bb.putShort(aux10);
        bb.putShort(aux11);
        bb.putShort(aux12);
        bb.putShort(aux13);
        bb.putShort(aux14);

        System.out.println(Arrays.toString(data));

        PortUtils.send_message(port, MSP_SET_RAW_RC, data);
    }
}