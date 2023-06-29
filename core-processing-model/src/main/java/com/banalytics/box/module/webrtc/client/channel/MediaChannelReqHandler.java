package com.banalytics.box.module.webrtc.client.channel;

import com.banalytics.box.api.integration.webrtc.channel.AbstractChannelMessage;
import com.banalytics.box.api.integration.webrtc.channel.environment.MediaChannelCreateReq;
import com.banalytics.box.api.integration.webrtc.channel.environment.MediaChannelCreateRes;
import com.banalytics.box.module.BoxEngine;
import com.banalytics.box.module.ITask;
import com.banalytics.box.module.MediaCaptureCallbackSupport;
import com.banalytics.box.module.State;
import com.banalytics.box.module.webrtc.client.RTCClient;
import com.banalytics.box.module.webrtc.client.channel.observer.MediaChannelObserver;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
public class MediaChannelReqHandler implements ChannelRequestHandler {
    final BoxEngine engine;
    final RTCClient rtcClient;

    public MediaChannelReqHandler(BoxEngine engine, RTCClient rtcClient) {
        this.engine = engine;
        this.rtcClient = rtcClient;
    }

    @Override
    public AbstractChannelMessage handle(AbstractChannelMessage request) throws Exception {
        if (request instanceof MediaChannelCreateReq req) {
            log.debug("Request to Create Media Channel:\n{}", req.toJson());
            UUID taskUuid = req.getTaskUuid();

            MediaChannelCreateRes res = new MediaChannelCreateRes();
            res.setRequestId(request.getRequestId());

            ITask<?> task = engine.findTask(taskUuid);
            if (task == null) {
                return res;
            }
            res.setStreamId(req.getStreamId());
            if (task.getState() == State.RUN && task instanceof MediaCaptureCallbackSupport mediaCaptureCallbackSupport) {
                rtcClient.mediaChannelObserver.requestStream(new MediaChannelObserver.StreamDescriptor(
                        req.getTaskUuid(), req.getStreamId(),
                        req.getRequestedWidth(), req.getRequestedHeight(),
                        req.isRequestedAudio(),
                        mediaCaptureCallbackSupport
                ));

            }
            return res;
        }
        return null;
    }
}
