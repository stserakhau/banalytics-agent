package com.banalytics.box;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SystemUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;
import java.util.TimeZone;

@Slf4j
@SpringBootApplication(scanBasePackages = "com.banalytics.box")
public class BanalyticsBoxApplication {

    @PostConstruct
    void setUTCTimezone() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(BanalyticsBoxApplication.class);

        if (SystemUtils.IS_OS_LINUX) {
            log.info("Linux environment detected");
            application.setAdditionalProfiles("linux");
        } else if(SystemUtils.IS_OS_WINDOWS) {
            log.info("Windows environment detected");
            application.setAdditionalProfiles("windows");
        }

//        application.addListeners(new ApplicationPidFileWriter("banalytics-box.pid"));
        application.run(args);
    }

}
