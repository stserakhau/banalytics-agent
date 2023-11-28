package com.banalytics.box.module.webrtc.client.channel;

import com.banalytics.box.api.integration.utils.CommonUtils;
import com.banalytics.box.api.integration.webrtc.channel.AbstractChannelMessage;
import com.banalytics.box.api.integration.webrtc.channel.ChannelMessage;
import com.banalytics.box.api.integration.webrtc.channel.environment.I18NReq;
import com.banalytics.box.api.integration.webrtc.channel.environment.I18NRes;
import com.banalytics.box.module.AbstractListOfTask;
import com.banalytics.box.module.BoxEngine;
import com.banalytics.box.module.ITask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import static com.banalytics.box.module.ConverterTypes.TYPE_MAP_STR_MAP_STR_STR;

@Slf4j
@RequiredArgsConstructor
public class I18NReqHandler implements ChannelRequestHandler {
    final BoxEngine engine;

    Map<String, Map<String, String>> i18n;

    @Override
    public ChannelMessage handle(ChannelMessage request) throws Exception {
        if (request instanceof I18NReq req) {
            I18NRes res = new I18NRes();
            res.setRequestId(req.getRequestId());

            {
                Map<UUID, Map<String, String>> titlesMap = new HashMap<>(50);
                engine.findThings().forEach(t -> {
                    titlesMap.put(
                            t.getUuid(),
                            Map.of(
                                    "className", t.getSelfClassName(),
                                    "title", t.getTitle()
                            )
                    );
                });

                engine.instances().forEach(i -> {
                    processTasksTree(i, titlesMap);
                });
                res.setUuidClassTitleMap(titlesMap);
            }
            if (this.i18n == null) {//load i18n resources
                this.i18n = new HashMap<>();

                Reflections reflections = new Reflections("i18n", new ResourcesScanner());
                Set<String> resourceList = reflections.getResources(Pattern.compile(".*\\.json"));
                for (String i18nResource : resourceList) {
                    Map<String, Map<String, String>> i18n = CommonUtils.DEFAULT_OBJECT_MAPPER.readValue(
                            getClass().getResourceAsStream("/" + i18nResource),
                            TYPE_MAP_STR_MAP_STR_STR
                    );
                    {//merge to general map
                        for (Map.Entry<String, Map<String, String>> e1 : i18n.entrySet()) {
                            if (this.i18n.containsKey(e1.getKey())) {
                                Map<String, String> map = this.i18n.get(e1.getKey());
                                map.putAll(e1.getValue());
                            } else {
                                this.i18n.put(e1.getKey(), e1.getValue());
                            }
                        }
                    }
                }
            }
            res.setI18n(this.i18n);

            return res;
        }
        return null;
    }

    private void processTasksTree(ITask<?> task, Map<UUID, Map<String, String>> titlesMap) {
        titlesMap.put(task.getUuid(), Map.of(
                "className", task.getSelfClassName(),
                "title", task.getTitle()
        ));

        if (task instanceof AbstractListOfTask<?> parent) {
            parent.getSubTasks().forEach(t -> {
                processTasksTree(t, titlesMap);
            });
        }
    }
}
