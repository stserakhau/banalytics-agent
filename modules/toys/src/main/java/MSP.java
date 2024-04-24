public class MSP {

    public MWIData data = new MWIData();

    final byte[] MSP_HEADER = getMspHeap();

    public static final int MSP_IDENT = 100;
    public static final int MSP_STATUS = 101;
    public static final int MSP_RAW_IMU = 102;
    public static final int MSP_SERVO = 103;
    public static final int MSP_MOTOR = 104;
    public static final int MSP_RC = 105;
    public static final int MSP_RAW_GPS = 106;
    public static final int MSP_COMP_GPS = 107;
    public static final int MSP_ATTITUDE = 108;
    public static final int MSP_ALTITUDE = 109;
    public static final int MSP_ANALOG = 110;
    public static final int MSP_RC_TUNING = 111;
    public static final int MSP_PID = 112;
    public static final int MSP_BOX = 113;
    public static final int MSP_MISC = 114;
    public static final int MSP_MOTOR_PINS = 115;
    public static final int MSP_BOXNAMES = 116;
    public static final int MSP_PIDNAMES = 117;
    public static final int MSP_WP = 118;
    public static final int MSP_BOXIDS = 119;
    public static final int MSP_SERVO_CONF = 120;
    public static final int MSP_NAV_STATUS = 121;
    public static final int MSP_NAV_CONFIG = 122;
    public static final int MSP_SET_RAW_RC = 200;
    public static final int MSP_SET_RAW_GPS = 201;
    public static final int MSP_SET_PID = 202;
    public static final int MSP_SET_BOX = 203;
    public static final int MSP_SET_RC_TUNING = 204;
    public static final int MSP_ACC_CALIBRATION = 205;
    public static final int MSP_MAG_CALIBRATION = 206;
    public static final int MSP_SET_MISC = 207;
    public static final int MSP_RESET_CONF = 208;
    public static final int MSP_SET_WP = 209;
    public static final int MSP_SELECT_SETTING = 210;
    public static final int MSP_SET_HEAD = 211;
    public static final int MSP_SET_SERVO_CONF = 212;
    public static final int MSP_SET_MOTOR = 214;

    public static final int MSP_BIND = 240;

    public static final int MSP_EEPROM_WRITE = 250;

    public static final int MSP_DEBUGMSG = 253;
    public static final int MSP_DEBUG = 254;

    public static final int MSP_SET_SERIAL_BAUDRATE = 199;
    public static final int MSP_ENABLE_FRSKY = 198;

    public static final int IDLE = 0, HEADER_START = 1, HEADER_M = 2, HEADER_ARROW = 3, HEADER_SIZE = 4,
            HEADER_CMD = 5, HEADER_ERR = 6;

    // private static final boolean[] ActiveModes = null;

    public byte[] encode(int msp, Character[] payload) {
        byte[] r;
        int rsSize = 6, idx = MSP_HEADER.length;
        if (payload != null) {
            rsSize = rsSize + payload.length;
        }
        r = new byte[rsSize];

        System.arraycopy(MSP_HEADER, 0, r, 0, idx);

        byte checksum = 0;
        byte payLoadSize = (byte) ((payload != null ? (int) (payload.length) : 0) & 0xFF);
        r[idx++] = (payLoadSize);
        checksum ^= (payLoadSize & 0xFF);

        r[idx++] = ((byte) (msp & 0xFF));
        checksum ^= (msp & 0xFF);

        if (payload != null) {
            for (char c : payload) {
                r[idx++] = ((byte) (c & 0xFF));
                checksum ^= (c & 0xFF);
            }
        }
        r[idx++] = (checksum);
        return (r);
    }

    private byte[] getMspHeap() {

        byte[] r = new byte[3];
        r[0] = "$".getBytes()[0];
        r[1] = "M".getBytes()[0];
        r[2] = "<".getBytes()[0];
        return r;
    }

    public void evaluateCommand(byte cmd, int dataSize) {

        int i;
        int id = (int) (cmd & 0xFF);

        switch (id) {
            case MSP_IDENT:
                data.ident = true;
                data.version = read8();
                data.multiType = read8();
                data.MSPversion = read8(); // MSP version
                int multiCapability = read32();// capability
                if ((multiCapability & 1) > 0)
                    data.multi_Capability.RXBind = true;
                if ((multiCapability & 4) > 0)
                    data.multi_Capability.Motors = true;
                if ((multiCapability & 8) > 0)
                    data.multi_Capability.Flaps = true;

                if ((multiCapability & 16) > 0)
                    data.multi_Capability.Nav = true;

                if ((multiCapability & 0x80000000) > 0)
                    data.multi_Capability.ByMis = true;

                break;

            case MSP_STATUS:
                data.cycleTime = read16();
                data.i2cError = read16();
                int SensorPresent = read16();
                int mode = read32();
                data.confSetting = read8();

                if ((SensorPresent & 1) > 0)
                    data.AccPresent = 1;
                else
                    data.AccPresent = 0;

                if ((SensorPresent & 2) > 0)
                    data.BaroPresent = 1;
                else
                    data.BaroPresent = 0;

                if ((SensorPresent & 4) > 0)
                    data.MagPresent = 1;
                else
                    data.MagPresent = 0;

                if ((SensorPresent & 8) > 0)
                    data.GPSPresent = 1;
                else
                    data.GPSPresent = 0;

                if ((SensorPresent & 16) > 0)
                    data.SonarPresent = 1;
                else
                    data.SonarPresent = 0;

                if (data.BoxNames != null) {

                    for (i = 0; i < data.BoxNames.length; i++) {
                        if ((mode & (1 << i)) > 0)
                            data.ActiveModes[i] = true;
                        else
                            data.ActiveModes[i] = false;

                    }
                }

                break;
            case MSP_RAW_IMU:

                data.ax = read16();
                data.ay = read16();
                data.az = read16();

                data.gx = read16() / 8;
                data.gy = read16() / 8;
                data.gz = read16() / 8;

                data.magx = read16() / 3;
                data.magy = read16() / 3;
                data.magz = read16() / 3;
                break;

            case MSP_SERVO:
                // for (i = 0; i < 8; i++)
                // servo[i] = read16();
                break;
            case MSP_MOTOR:
                // for (i = 0; i < 8; i++)
                // mot[i] = read16();
                // if (multiType == SINGLECOPTER)
                // servo[7] = mot[0];
                // if (multiType == DUALCOPTER) {
                // servo[7] = mot[0];
                // servo[6] = mot[1];
                // }
                break;
            case MSP_RC:
                // rcRoll = read16();
                // rcPitch = read16();
                // rcYaw = read16();
                // rcThrottle = read16();
                // rcAUX1 = read16();
                // rcAUX2 = read16();
                // rcAUX3 = read16();
                // rcAUX4 = read16();
                break;
            case MSP_RAW_GPS:
                // GPS_fix = read8();
                // GPS_numSat = read8();
                // GPS_latitude = read32();
                // GPS_longitude = read32();
                // GPS_altitude = read16();
                // GPS_speed = read16();
                // GPS_ground_course = read16();
                break;
            case MSP_COMP_GPS:
                // GPS_distanceToHome = read16();
                // GPS_directionToHome = read16();
                // GPS_update = read8();
                break;
            case MSP_ATTITUDE:
                data.angx = read16() / 10;
                data.angy = read16() / 10;
                data.head = read16();
                break;
            case MSP_ALTITUDE:
                // alt = ((float) read32() / 100);
                // vario = read16();
                break;
            case MSP_ANALOG:
                // bytevbat = read8();
                // pMeterSum = read16();
                // rssi = read16();
                // amperage = read16();
                break;
            case MSP_RC_TUNING:
                // byteRC_RATE = read8();
                // byteRC_EXPO = read8();
                // byteRollPitchRate = read8();
                // byteYawRate = read8();
                // byteDynThrPID = read8();
                // byteThrottle_MID = read8();
                // byteThrottle_EXPO = read8();
                break;
            case MSP_ACC_CALIBRATION:
                break;
            case MSP_MAG_CALIBRATION:
                break;
            case MSP_PID:
                // for (i = 0; i < PIDITEMS; i++) {
                // byteP[i] = read8();
                // byteI[i] = read8();
                // byteD[i] = read8();
                // }
                break;
            case MSP_BOX:
                // for (i = 0; i < CHECKBOXITEMS; i++) {
                // activation[i] = read16();
                // for (int aa = 0; aa < 12; aa++) {
                // if ((activation[i] & (1 << aa)) > 0)
                // Checkbox[i][aa] = true;
                // else
                // Checkbox[i][aa] = false;
                // }
                // }

                break;
            case MSP_BOXNAMES:
                data.BoxNames = new String(inBuf, 0, dataSize).split(";");
                break;
            case MSP_PIDNAMES:
                // PIDNames = new String(inBuf, 0, dataSize).split(";");
                break;

            case MSP_SERVO_CONF:
                // // min:2 / max:2 / middle:2 / rate:1
                // for (i = 0; i < 8; i++) {
                // ServoConf[i].Min = read16();
                // ServoConf[i].Max = read16();
                // ServoConf[i].MidPoint = read16();
                // ServoConf[i].Rate = read8();
                // }
                break;
            case MSP_MISC:
                // intPowerTrigger = read16(); // a
                //
                // minthrottle = read16();// b
                // maxthrottle = read16();// c
                // mincommand = read16();// d
                // failsafe_throttle = read16();// e
                // ArmCount = read16();// f
                // LifeTime = read32();// g
                // mag_decliniation = read16() / 10f;// h
                //
                // vbatscale = read8();// i
                // vbatlevel_warn1 = (float) (read8() / 10.0f);// j
                // vbatlevel_warn2 = (float) (read8() / 10.0f);// k
                // vbatlevel_crit = (float) (read8() / 10.0f);// l
                // if (ArmCount < 1)
                // Log_Permanent_Hidden = true;
                break;

            case MSP_MOTOR_PINS:
                // for (i = 0; i < 8; i++) {
                // byteMP[i] = read8();
                // }
                break;
            case MSP_DEBUG:
                // debug1 = read16();
                // debug2 = read16();
                // debug3 = read16();
                // debug4 = read16();
                break;
            case MSP_DEBUGMSG:
                // while (dataSize-- > 0) {
                // char c = (char) read8();
                // if (c != 0) {
                // DebugMSG += c;
                // }
                // }
                break;
            case MSP_WP:
                // Waypoint WP = new Waypoint();
                // WP.Number = read8();
                // WP.Lat = read32();
                // WP.Lon = read32();
                // WP.Altitude = read32();
                // WP.Heading = read16();
                // WP.TimeToStay = read16();
                // WP.NavFlag = read8();
                //
                // Waypoints[WP.Number] = WP;
                //
                // Log.d("aaa", "MSP_WP (get) " + String.valueOf(WP.Number) + "  " +
                // String.valueOf(WP.Lat) + "x" + String.valueOf(WP.Lon) + " " +
                // String.valueOf(WP.Altitude) + " " + String.valueOf(WP.NavFlag));
                break;

            default:
                System.out.println("Error command - unknown replay " + String.valueOf(id));

        }
    }

    int c_state = IDLE;

    boolean err_rcvd = false;
    int offset = 0, dataSize = 0;
    byte checksum = 0;
    byte cmd;
    byte[] inBuf = new byte[256];
    int i = 0;
    int p = 0;

    // private DataSet dataSet = new DataSet();

    int read32() {
        return (inBuf[p++] & 0xff) + ((inBuf[p++] & 0xff) << 8) + ((inBuf[p++] & 0xff) << 16)
                + ((inBuf[p++] & 0xff) << 24);
    }

    int read16() {
        return (inBuf[p++] & 0xff) + ((inBuf[p++]) << 8);
    }

    int read8() {
        return inBuf[p++] & 0xff;
    }

    public void decode(byte[] data) {
        for (byte c : data) {
            switch (c_state) {
                case IDLE:
                    c_state = (c == '$') ? HEADER_START : IDLE;
                    break;
                case HEADER_START:
                    c_state = (c == 'M') ? HEADER_M : IDLE;
                    break;
                case HEADER_M:
                    if (c == '>') {
                        c_state = HEADER_ARROW;
                    } else if (c == '!') {
                        c_state = HEADER_ERR;
                    } else {
                        c_state = IDLE;
                    }
                    break;
                case HEADER_ARROW:
                case HEADER_ERR:
                    /* is this an error message? */
                    err_rcvd = (c_state == HEADER_ERR); /*
                     * now we are expecting the
                     * payload size
                     */
                    dataSize = (c & 0xFF);
                    /* reset index variables */
                    p = 0;
                    offset = 0;
                    checksum = 0;
                    checksum ^= (c & 0xFF);
                    /* the command is to follow */
                    c_state = HEADER_SIZE;
                    break;
                case HEADER_SIZE:
                    cmd = (byte) (c & 0xFF);
                    checksum ^= (c & 0xFF);
                    c_state = HEADER_CMD;
                    break;
                case HEADER_CMD:
                    if (offset < dataSize) {
                        checksum ^= (c & 0xFF);
                        inBuf[offset++] = (byte) (c & 0xFF);
                    } else {
                        /* compare calculated and transferred checksum */
                        if ((checksum & 0xFF) == (c & 0xFF)) {
                            if (err_rcvd) {
                                System.out.println("Multiwii protocol: Copter did not understand request type " + c);
                            } else {
                                /* we got a valid response packet, evaluate it */
                                evaluateCommand(cmd, (int) dataSize);

                            }
                        } else {
                            System.out.println("Multiwii protocol invalid checksum for command " + ((int) (cmd & 0xFF)) + ": "
                                    + (checksum & 0xFF) + " expected, got " + (int) (c & 0xFF));
                            System.out.println("Multiwii protocol <" + (cmd & 0xFF) + " " + (dataSize & 0xFF) + "> {");
                            System.out.println("Multiwii protocol } [" + c + "]");
                            System.out.println("Multiwii protocol " + new String(inBuf, 0, dataSize));
                        }
                        c_state = IDLE;
                    }
                    break;
                default:
                    break;
            }
        }
    }

}
