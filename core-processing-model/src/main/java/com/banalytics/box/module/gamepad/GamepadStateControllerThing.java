package com.banalytics.box.module.gamepad;

import com.banalytics.box.api.integration.webrtc.channel.environment.ThingApiCallReq;
import com.banalytics.box.api.integration.webrtc.channel.events.AbstractEvent;
import com.banalytics.box.api.integration.webrtc.channel.events.measurement.gamepad.GPAxisChangeEvent;
import com.banalytics.box.api.integration.webrtc.channel.events.measurement.gamepad.GPButtonChangeEvent;
import com.banalytics.box.api.integration.webrtc.channel.events.measurement.gamepad.GamePadStateChangedEvent;
import com.banalytics.box.api.integration.webrtc.channel.events.measurement.gamepad.GamePadStateChangedEvent.GamepadButton;
import com.banalytics.box.module.AbstractThing;
import com.banalytics.box.module.BoxEngine;
import com.banalytics.box.module.Thing;
import com.banalytics.box.module.standard.EventConsumer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.core.annotation.Order;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.banalytics.box.module.State.RUN;

@Slf4j
@Order(Thing.StarUpOrder.INTEGRATION)
public class GamepadStateControllerThing extends AbstractThing<GamepadStateControllerConfiguration> implements EventConsumer {
    public GamepadStateControllerThing(BoxEngine engine) {
        super(engine);
    }

    Map<Integer, Pair<double[], GamepadButton[]>> gamepadStates = new ConcurrentHashMap();

    @Override
    public void consume(Recipient target, AbstractEvent abstractEvent) {
        if (!(abstractEvent instanceof GamePadStateChangedEvent)) {
            return;
        }
        GamePadStateChangedEvent gpe = (GamePadStateChangedEvent) abstractEvent;
        log.info("Consumed: {}", gpe);
        Pair<double[], GamepadButton[]> gamepadState = gamepadStates.get(gpe.gamepadIndex);
        if (gamepadState == null) {
            gamepadStates.put(
                    gpe.gamepadIndex,
                    Pair.of(gpe.getAxes(), gpe.buttons)
            );
        } else {
            double[] axisStates = gamepadState.getLeft();
            for (int i = 0; i < gpe.axes.length; i++) {
                double newVal = gpe.axes[i];
                double stateVal = axisStates[i];
                if (Math.abs(newVal - stateVal) > configuration.axisThreshold) {
                    GPAxisChangeEvent e = new GPAxisChangeEvent(i, newVal);
                    e.setEnvironmentUuid(gpe.getEnvironmentUuid());
                    e.setNodeType(gpe.getNodeType());
                    e.setNodeUuid(gpe.getNodeUuid());
                    log.info("Axis Fired: {}", e);
                    engine.fireEvent(e);
                }
                axisStates[i] = newVal;
            }
            GamepadButton[] buttonStates = gamepadState.getRight();
            for (int i = 0; i < gpe.buttons.length; i++) {
                GamepadButton newVal = gpe.buttons[i];
                GamepadButton stateVal = buttonStates[i];
                if (
                        Math.abs(newVal.value - stateVal.value) > configuration.axisThreshold
                                || newVal.pressed != stateVal.pressed
                                || newVal.touched != stateVal.touched
                ) {
                    GPButtonChangeEvent e = new GPButtonChangeEvent(
                            gpe.gamepadIndex,
                            newVal.index,
                            newVal.pressed,
                            newVal.touched,
                            newVal.value
                    );
                    e.setEnvironmentUuid(gpe.getEnvironmentUuid());
                    e.setNodeType(gpe.getNodeType());
                    e.setNodeUuid(gpe.getNodeUuid());
                    log.info("Button Fired: {}", e);
                    engine.fireEvent(e);
                    buttonStates[i] = newVal;
                }
            }
        }
    }

    @Override
    public Set<String> accountNames(Set<String> accountIds) {
        return Set.of();
    }

    @Override
    protected void doInit() throws Exception {
    }

    @Override
    protected void doStart() throws Exception {
        gamepadStates.clear();
    }

    @Override
    protected void doStop() throws Exception {
        gamepadStates.clear();
    }

    @Override
    public Object uniqueness() {
        return "gamepad_controller";
    }

    @Override
    public String getTitle() {
        return getSelfClassName();
    }

//    @Override
//    public Set<String> apiMethodsPermissions() {
//        return Set.of(PERMISSION_READ, "scan*");
//    }

    @Override
    public Object call(Map<String, Object> params) throws Exception {
        if (getState() != RUN) {
            throw new Exception("error.thing.notInitialized");
        }
        String method = (String) params.get(ThingApiCallReq.PARAM_METHOD);

//        switch (method) {
//            case "readAvailableSubnets" -> {
//                return deviceDiscoveryService.availableSubnets();
//            }
//            case "scanSubnet" -> {
//                String addr = (String) params.get(PARAM_ADDRESS);
//                String mask = (String) params.get(PARAM_MASK);
//                deviceDiscoveryService.scanSubnets(addr, mask, configuration.pingTimeout);
//                return null;
//            }
//            case "readScanSubnetResult" -> {
//                return deviceDiscoveryService.listDiscoveredDevices();
//            }
//        }

        throw new Exception("Invalid method: " + method);
    }
}
