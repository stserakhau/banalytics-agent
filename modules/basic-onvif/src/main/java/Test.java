import com.banalytics.box.module.onvif.thing.OnvifConfiguration;
import com.banalytics.box.module.onvif.client.OnvifDevice;

public class Test {
    public static void main(String[] args) throws Exception {
        OnvifDevice d1 = new OnvifDevice("185.152.136.159", 10000,
                "admin", "iT12GFKpop"/*,
                OnvifConfiguration.TimeType.LOCAL_UTC*/
        );
//        OnvifDevice d2 = new OnvifDevice("127.0.0.1", 15080, "onvif", "conn3X3d", ONVIFConfiguration.TimeType.CAMERA_UTC);

        Object obj = d1.device().getNetworkProtocols();

        System.out.println(obj);
//        System.out.println(d2);
    }
}
