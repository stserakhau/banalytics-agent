package com.banalytics.box.api.integration.suc;

import com.banalytics.box.api.integration.MessageType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

@Getter
@Setter
@ToString(callSuper = true)
public class GetEnvironmentModulesResponse extends AbstractSUCIntegrationMessage {
    List<Module> modules;

    public GetEnvironmentModulesResponse() {
        super(MessageType.GET_ENV_MOD_RES);
    }

    //https://europe-central2-maven.pkg.dev/banalytics-production/maven-repo/
// com/banalytics/box/core/0.0.1-SNAPSHOT-windows-x86_64/core-0.0.1-SNAPSHOT-windows-x86_64.jar
    public static URL getModuleUrl(String rootUrl, Module module) throws MalformedURLException {
        String path = "%s/%s/%s/%s/%s-%s.jar".formatted(
                rootUrl,
                module.getGroupId().replaceAll("\\.", "/"),
                module.getArtifactId(),
                module.getVersion(),
                module.getArtifactId(),
                module.getVersion()
        );
        return new URL(path);
    }

    public static URL getModuleDependenciesUrl(String rootUrl, Module module) throws MalformedURLException {
        String path = "%s/%s/%s/%s/%s-%s-bin.zip".formatted(
                rootUrl,
                module.getGroupId().replaceAll("\\.", "/"),
                module.getArtifactId(),
                module.getVersion(),
                module.getArtifactId(),
                module.getVersion()
        );
        return new URL(path);
    }

    public static URL getModuleModelUrl(String rootUrl, Module module) throws MalformedURLException {
        String path = "%s/%s/%s/%s/%s-%s-model.zip".formatted(
                rootUrl,
                module.getGroupId().replaceAll("\\.", "/"),
                module.getArtifactId(),
                module.getVersion(),
                module.getArtifactId(),
                module.getVersion()
        );
        return new URL(path);
    }
}
