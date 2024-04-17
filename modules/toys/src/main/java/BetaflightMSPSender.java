import java.io.OutputStream;
import com.fazecast.jSerialComm.SerialPort;

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
            SerialPort serialPort = SerialPort.getCommPort("COM5");
            // Устанавливаем параметры порта
//            serialPort.setBaudRate(9600);
//            serialPort.setNumDataBits(8);
//            serialPort.setNumStopBits(1);
//            serialPort.setParity(SerialPort.NO_PARITY);
//            serialPort.setFlowControl(SerialPort.FLOW_CONTROL_DISABLED);
//            serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);

            if(!serialPort.openPort()){
                throw new RuntimeException(
                        "Can't open port.\n Error code: %s\nError location %s".formatted(
                                serialPort.getLastErrorCode(),
                                serialPort.getLastErrorLocation()
                        )
                );
            }

            // Получаем выходной поток для отправки данных
            OutputStream outputStream = serialPort.getOutputStream();

            // Отправляем команду MSP_SET_RAW_RC, чтобы установить значения каналов управления
            sendMSPSetRawRC(outputStream, 1500, 1500, 1010, 1500, 1000, 1000, 1000, 1000);

            // Закрываем выходной поток и порт
            outputStream.close();
            serialPort.closePort();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Метод для отправки команды MSP_SET_RAW_RC msp\msp.c <- firmware commands
    private static void sendMSPSetRawRC(OutputStream outputStream, int roll, int pitch, int throttle, int yaw, int aux1, int aux2, int aux3, int aux4) throws Exception {
        int checksum = 0;
        byte[] data = new byte[22];
        data[0] = (byte) '$';
        data[1] = 'M';
        data[2] = '<';
        data[3] = 8; // Длина пакета
        data[4] = (byte) 200; // Команда MSP_SET_RAW_RC
        data[5] = (byte) (roll & 0xFF);
        data[6] = (byte) ((roll >> 8) & 0xFF);
        data[7] = (byte) (pitch & 0xFF);
        data[8] = (byte) ((pitch >> 8) & 0xFF);
        data[9] = (byte) (throttle & 0xFF);
        data[10] = (byte) ((throttle >> 8) & 0xFF);
        data[11] = (byte) (yaw & 0xFF);
        data[12] = (byte) ((yaw >> 8) & 0xFF);
        data[13] = (byte) (aux1 & 0xFF);
        data[14] = (byte) ((aux1 >> 8) & 0xFF);
        data[15] = (byte) (aux2 & 0xFF);
        data[16] = (byte) ((aux2 >> 8) & 0xFF);
        data[17] = (byte) (aux3 & 0xFF);
        data[18] = (byte) ((aux3 >> 8) & 0xFF);
        data[19] = (byte) (aux4 & 0xFF);
        data[20] = (byte) ((aux4 >> 8) & 0xFF);
        for (int i = 3; i < 21; i++) {
            checksum ^= data[i];
        }
        data[21] = (byte) (checksum & 0xFF);
        outputStream.write(data);
    }
}