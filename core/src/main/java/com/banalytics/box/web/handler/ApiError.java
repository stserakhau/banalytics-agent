package com.banalytics.box.web.handler;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;

import static com.banalytics.box.TimeUtil.currentTimeInServerTz;

@Setter
@Getter
public class ApiError {
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
    private LocalDateTime timestamp;
    private String message;
    private String stacktrace;

    public ApiError(Throwable t) {
        this.timestamp = currentTimeInServerTz();
        this.message = t.getMessage();

        ByteArrayOutputStream baos = new ByteArrayOutputStream(10000);
        PrintStream ps = new PrintStream(baos);
        t.printStackTrace(ps);
        this.stacktrace = baos.toString();
    }


}
