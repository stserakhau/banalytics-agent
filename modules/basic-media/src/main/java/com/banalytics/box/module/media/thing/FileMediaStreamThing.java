package com.banalytics.box.module.media.thing;

import com.banalytics.box.module.AbstractThing;
import com.banalytics.box.module.BoxEngine;
import com.banalytics.box.module.Thing;
import com.banalytics.box.module.constants.MediaFormat;
import com.banalytics.box.module.standard.UrlMediaStream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.core.annotation.Order;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Order(Thing.StarUpOrder.BUSINESS)
public class FileMediaStreamThing extends AbstractThing<FileMediaStreamThingConfiguration> implements UrlMediaStream {
    public FileMediaStreamThing(BoxEngine engine) {
        super(engine);
    }

    @Override
    public String getTitle() {
        return configuration.getSourceUri();
    }

    @Override
    public String getUrl() {
        try {
            URI uri = new URI(configuration.getSourceUri());
            return uri.toURL().toString();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public MediaFormat getStreamFormat() {
        String uri = configuration.getSourceUri();
        String extension = FilenameUtils.getExtension(uri);
        return MediaFormat.valueOf(extension);
    }


    @Override
    public Set<String> generalPermissions() {
        Set<String> p = new HashSet<>(super.generalPermissions());
        p.add(PERMISSION_VIDEO);
        return p;
    }

    @Override
    protected void doInit() throws Exception {
    }

    @Override
    protected void doStart() throws Exception {
    }

    @Override
    protected void doStop() throws Exception {
    }
}
