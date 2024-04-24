public class MWIData {
    public boolean ident;
    public int version;
    public int multiType;
    public int MSPversion;
    public MultiCapability multi_Capability;
    public int cycleTime;
    public int i2cError;
    public int confSetting;
    public int AccPresent;
    public int BaroPresent;
    public int MagPresent;
    public int GPSPresent;
    public int SonarPresent;
    public String[] BoxNames;
    public boolean[] ActiveModes;
    public int ax;
    public int ay;
    public int az;
    public int gx;
    public int gy;
    public int gz;
    public int magx;
    public int magy;
    public int magz;
    public int angx;
    public int angy;
    public int head;

    public static class MultiCapability {

        public boolean RXBind;
        public boolean Motors;
        public boolean Flaps;
        public boolean Nav;
        public boolean ByMis;
    }
}
