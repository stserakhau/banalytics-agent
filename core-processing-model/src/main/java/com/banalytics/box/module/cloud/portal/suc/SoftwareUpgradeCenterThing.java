package com.banalytics.box.module.cloud.portal.suc;

import com.banalytics.box.api.integration.AbstractMessage;
import com.banalytics.box.api.integration.MessageHandler;
import com.banalytics.box.api.integration.suc.Module;
import com.banalytics.box.api.integration.suc.*;
import com.banalytics.box.api.integration.utils.CommonUtils;
import com.banalytics.box.module.AbstractThing;
import com.banalytics.box.module.BoxEngine;
import com.banalytics.box.module.Singleton;
import com.banalytics.box.module.cloud.portal.PortalIntegrationConfiguration;
import com.banalytics.box.module.cloud.portal.PortalIntegrationThing;
import com.banalytics.box.module.constants.SUCUpdateType;
import com.banalytics.box.service.SystemThreadsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.springframework.boot.info.BuildProperties;
import org.springframework.core.annotation.Order;
import org.springframework.web.socket.WebSocketSession;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static com.banalytics.box.TimeUtil.currentTimeInServerTz;
import static com.banalytics.box.api.integration.suc.EnvironmentModuleUpdateStatusEvent.of;
import static com.banalytics.box.module.Thing.StarUpOrder.DATA_EXCHANGE;

@Slf4j
@Order(DATA_EXCHANGE)
public class SoftwareUpgradeCenterThing extends AbstractThing<SoftwareUpgradeCenterConfiguration> implements MessageHandler<AbstractSUCIntegrationMessage>, Singleton {
    private static final String FILE_MODULES_INFO = "modules-info.properties";

    private static final String MAVEN_REPO_ROOT = "https://europe-central2-maven.pkg.dev/banalytics-production/maven-repo";

    @Override
    public String getTitle() {
        return getSelfClassName();
    }

    public SoftwareUpgradeCenterThing(BoxEngine engine) {
        super(engine);
    }

    private File packageFolder;
    private File configFolder;

    private File modulesFolder;

    private File modelsFolder;

    private File installedModulesInfoFile;
    private Properties installedModulesInfo = new Properties();

    private File modulesDownloadFolder;
    private File modulesUpgradeFolder;
    private File modulesBackupFolder;

    private boolean firstRun;
    private PortalIntegrationThing portalIntegrationThing;

    @Override
    protected void doInit() throws Exception {
        this.portalIntegrationThing = engine.getThing(PortalIntegrationConfiguration.THING_UUID);
    }

    @Override
    protected void doStart() throws Exception {
        this.portalIntegrationThing.subscribe(this);
        this.portalIntegrationThing.subscribeHandler(this);

        this.configFolder = engine.applicationConfigFolder();
        this.packageFolder = this.configFolder.getParentFile();
        this.modulesFolder = new File(packageFolder, "modules");
        if (!this.modulesFolder.exists()) {
            this.modulesFolder.mkdirs();
            log.info(">> Modules folder created: {}", modulesFolder);
        }
        this.installedModulesInfoFile = new File(this.modulesFolder, FILE_MODULES_INFO);
        if (this.installedModulesInfoFile.exists()) {
            log.info(">> Found modules info file: {}", installedModulesInfoFile);
            this.installedModulesInfo.load(new FileReader(this.installedModulesInfoFile));
            log.info(">> Modules folder loaded:\n{}", installedModulesInfo);
            this.firstRun = false;
        } else {
            log.info(">> First run detected!");
            this.firstRun = true;
        }

        this.modulesDownloadFolder = new File(this.packageFolder, "modules-download");
        if (!this.modulesDownloadFolder.exists()) {
            this.modulesDownloadFolder.mkdirs();
            log.info(">> Download folder created: {}", modulesDownloadFolder);
        }

        boolean emptyUpgradeFolder;
        this.modulesUpgradeFolder = new File(this.packageFolder, "modules-upgrade");
        if (!this.modulesUpgradeFolder.exists()) {
            this.modulesUpgradeFolder.mkdirs();
            log.info(">> Upgrade folder created: {}", modulesUpgradeFolder);
            emptyUpgradeFolder = true;
        } else {
            String[] files = modulesUpgradeFolder.list();
            emptyUpgradeFolder = files == null || files.length == 0;
        }

        boolean emptyBackupFolder;
        this.modulesBackupFolder = new File(this.packageFolder, "modules-backup");
        if (!this.modulesBackupFolder.exists()) {
            this.modulesBackupFolder.mkdirs();
            log.info(">> Backup folder created: {}", modulesBackupFolder);
            emptyBackupFolder = true;
        } else {
            String[] files = modulesBackupFolder.list();
            emptyBackupFolder = files == null || files.length == 0;
        }

        this.modelsFolder = new File(this.packageFolder, "models");
        if (!this.modelsFolder.exists()) {
            this.modelsFolder.mkdirs();
            log.info(">> Models folder created: {}", modelsFolder);
        }

        if (portalIntegrationThing.getEnvironmentUUID() == null) {
            log.warn("Environment didn't got UUID. Software upgrade cancelled.");
            return;
        }
        {//Start software upgrade case if first run after installation or configured update on start of the application
            boolean startUpgradeOnApplicationStart = firstRun || configuration.getUpdateType() == SUCUpdateType.ON_START_APPLICATION;
            if (startUpgradeOnApplicationStart) {
                log.info(">> Software upgrade started.");
                initiateSoftwareUpgrade();
                return;
            }
        }
        // after upgrading software
        //    old modules copied to backup
        //    upgrade modules copied to modules
        //    i.e. backup contains files
        if (!installedModulesInfo.isEmpty() && emptyUpgradeFolder && !emptyBackupFolder) {
            log.info(">> Software upgraded successfully.");
            sendDownloadStatusMessage(ModuleUpdateStatus.installed, null, null);
            //clean backup folder
            log.info("Cleaning backup folder");
            FileUtils.cleanDirectory(modulesBackupFolder);
        }
    }

    @Override
    protected void doStop() throws Exception {
    }

    private boolean softwareUpgradeInProgress = false;

    @Override
    public boolean isSupport(AbstractMessage message) {
        return message instanceof AbstractSUCIntegrationMessage;
    }

    public void destroy() {
        if (portalIntegrationThing != null) {
            portalIntegrationThing.unSubscribeHandler(this);
            portalIntegrationThing.unSubscribe(this);
            portalIntegrationThing = null;
        }
    }

    private void sendDownloadStatusMessage(ModuleUpdateStatus status, Module module, String content) {
        EnvironmentModuleUpdateStatusEvent message = of(
                portalIntegrationThing.getEnvironmentUUID(), module,
                status,
                content
        );

        try {
            portalIntegrationThing.sendMessage(message);
        } catch (Exception e) {
            log.error("Can't send message to portal.", e);
        }
    }

    @Override
    public AbstractSUCIntegrationMessage handleMessage(WebSocketSession session, AbstractMessage message) {
        try {
            if (message instanceof SynchronizeSoftwareEvent res) {
                log.info("Call software upgrade.");
                initiateSoftwareUpgrade();
            } else if (message instanceof GetEnvironmentModulesResponse res) {
                if (softwareUpgradeInProgress) {
                    log.info("Software Upgrade in progress. Response skipped:\n{}", res);
                    return null;
                }
                log.info("Software Upgrade started.");
                softwareUpgradeInProgress = true;
                List<Module> receivedModules = res.getModules();
                if (receivedModules.isEmpty()) {
                    final String text = "Portal received empty modules list to environment %s. Something wrong on portal side. Update cancelled. Tech support catch this message and work."
                            .formatted(portalIntegrationThing.getEnvironmentUUID());
                    log.warn(text);
                    sendDownloadStatusMessage(ModuleUpdateStatus.installation_cancelled, null, text);
                    softwareUpgradeInProgress = false;
                    return null;
                }
                Properties upgradeModuleInfo = new Properties();
                FileUtils.cleanDirectory(modulesUpgradeFolder);
                log.info("==============================");
                log.info("Starting software upgrade.");
                boolean allModulesDownloadedSuccessfuly = true;
                for (Module module : receivedModules) {
                    String moduleFileName = module.fileName();
                    log.info(">> Received module: {}", module);
                    Map.Entry<String, String> receivedModule = module.toEntry();

//                    String receivedModuleValue = receivedModule.getValue();
//                    String installedModuleValue = this.installedModulesInfo.getProperty(receivedModule.getKey());
//                    if (receivedModuleValue.equals(installedModuleValue)) {
                    log.info(">>>> Module already installed");
                    //copy to modules-upgrade
//                        Files.copy(
//                                new File(modulesFolder, moduleFileName).toPath(),
//                                new File(modulesUpgradeFolder, moduleFileName).toPath(),
//                                StandardCopyOption.REPLACE_EXISTING
//                        );
//                        log.info(">>>> Will be used current version.");
//                        upgradeModuleInfo.setProperty(receivedModule.getKey(), receivedModule.getValue());
//                    } else {
                    log.info(">>>> New module detected");
                    try {
                        sendDownloadStatusMessage(ModuleUpdateStatus.downloading, module, null);
                        {//download module to modules-upgrade
                            log.info(">>>>>> Downloading started");
                            URL moduleUrl = GetEnvironmentModulesResponse.getModuleUrl(MAVEN_REPO_ROOT, module);
                            File moduleFile = new File(this.modulesUpgradeFolder, moduleFileName);
                            DownloadUtil.copyUrlToFile(moduleUrl, moduleFile);
                            log.info(">>>>>> Downloaded");
                        }

                        {//download zip to temporary file and extract to modules-upgrade
                            File dependenciesFile = new File(modulesDownloadFolder, moduleFileName + "-bin.zip");
                            URL dependenciesZipUrl = GetEnvironmentModulesResponse.getModuleDependenciesUrl(MAVEN_REPO_ROOT, module);
                            URLConnection urlConnection = dependenciesZipUrl.openConnection();
                            long contentLength = urlConnection.getContentLength();
                            log.info(">>>> Check dependencies package. Content length: {}", contentLength);
                            if (contentLength > 0) {
                                boolean needDownload = !dependenciesFile.exists() || dependenciesFile.length() != contentLength;
                                if (needDownload) {
                                    log.info(">>>>>> Dependencies downloading started: {}", dependenciesZipUrl);
                                    try {
                                        DownloadUtil.copyUrlToFile(dependenciesZipUrl, dependenciesFile);
                                        log.info(">>>>>> Downloaded");
                                    } catch (IOException e) {
                                        log.info("Dependency not found: {}", e.getMessage());
                                    }
                                } else {
                                    log.info(">>>>>> Dependencies downloaded already: {}", dependenciesFile.getAbsolutePath());
                                }
                            }
                            if (dependenciesFile.exists()) {
                                log.info(">>>> Start dependencies extraction");
                                DownloadUtil.unzip(dependenciesFile, this.modulesUpgradeFolder);
                                log.info(">>>>>> Dependencies extracted");
                            } else {
                                log.info(">>>>>>>> Dependencies package not detected");
                            }
                        }
                        {//download zip to temporary file and extract to modules-upgrade
                            try {
                                log.info(">>>> Check models package");
                                URL modelsZipUrl = GetEnvironmentModulesResponse.getModuleModelUrl(MAVEN_REPO_ROOT, module);
                                URLConnection urlConnection = modelsZipUrl.openConnection();
                                File modelsFile = new File(modulesDownloadFolder, moduleFileName + "-model.zip");
                                boolean needDownload = !modelsFile.exists() || modelsFile.length() != urlConnection.getContentLength();
                                if (needDownload) {
                                    log.info(">>>>>> Models downloading started: {}", modelsZipUrl);
                                    DownloadUtil.copyUrlToFile(modelsZipUrl, modelsFile);
                                    log.info(">>>>>> Downloaded");
                                } else {
                                    log.info(">>>>>> Models downloaded already: {}", modelsFile.getAbsolutePath());
                                }
                                log.info(">>>> Start models extraction");
                                DownloadUtil.unzip(modelsFile, this.modelsFolder);
                                log.info(">>>>>> models extracted");
                            } catch (FileNotFoundException e) {
                                log.info(">>>>>>>> Models package not detected");
                            }
                        }

                        upgradeModuleInfo.setProperty(
                                receivedModule.getKey(),
                                receivedModule.getValue()
                        );
                        sendDownloadStatusMessage(ModuleUpdateStatus.downloaded, module, null);
                    } catch (Exception e) {
                        allModulesDownloadedSuccessfuly = false;
                        log.error("Module download error: " + module, e);
                        String errorContent = CommonUtils.DEFAULT_OBJECT_MAPPER.writeValueAsString(e.getStackTrace());
                        sendDownloadStatusMessage(ModuleUpdateStatus.download_error, module, errorContent);
                        break;
                    }
                }

                if (allModulesDownloadedSuccessfuly) {
                    try (FileWriter fw = new FileWriter(new File(modulesUpgradeFolder, FILE_MODULES_INFO))) {
                        upgradeModuleInfo.store(fw, "Upgrade executed: " + currentTimeInServerTz());
                        startUpgradeJob(packageFolder);
                    }
                } else {
                    softwareUpgradeInProgress = false;
                }
            }
        } catch (Exception e) {
            log.error("Failure to handle message: " + message, e);
            softwareUpgradeInProgress = false;
        }
        return null;
    }

    public void initiateSoftwareUpgrade() {
        if (softwareUpgradeInProgress) {
            log.info("Software upgrade in progress. New request skipped.");
            return;
        }
        log.warn("Starting software upgrade...");
        GetEnvironmentModulesRequest req = new GetEnvironmentModulesRequest();
        req.setEnvironmentUUID(portalIntegrationThing.getEnvironmentUUID());
        com.banalytics.box.api.integration.suc.Module module = new com.banalytics.box.api.integration.suc.Module();
        BuildProperties bp = engine.getBuildProperties();
        module.setGroupId(bp.getGroup());
        module.setArtifactId(bp.getArtifact());
        module.setVersion(bp.getVersion());
        req.setBanalyticsBoxCoreModule(module);
        portalIntegrationThing.sendMessage(req);
    }

    private static void startUpgradeJob(File packageFolder) throws Exception {
        Thread.sleep(3000);//wait 3 sec to send "downloaded" status message
        Runtime rt = Runtime.getRuntime();
        if (SystemUtils.IS_OS_WINDOWS) {
            log.info("Reboot initiated on software upgrade");
            rt.exec("upgrade-job-start.bat");
        } else if (SystemUtils.IS_OS_LINUX) {
            //todo it's docker case when upgrades checks on the start of the applications
            log.info("Reboot initiated on software upgrade");
            SystemThreadsService.reboot();
        } else if (SystemUtils.IS_OS_MAC) {
            rt.exec("upgrade-job-start.sh", null, packageFolder);
        }
    }
}
