package com.banalytics.box.web.sec;

import com.banalytics.box.service.SystemThreadsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/secured/system")
@Slf4j
public class SystemRest {

    @GetMapping("/restart")
    @ResponseStatus(HttpStatus.OK)
    public void restart() throws Exception {
        log.info("Reboot initiated via local console");
        SystemThreadsService.reboot();
    }
}