package com.banalytics.box.module.events.jpa;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(
        name = "webrtc_connection_history",
        indexes = {
                @Index(name = "idx_webrtc_connection_history_dateTime", columnList = "date_time")
        }
)
public class WebRTCConnectionHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @Column(name = "id", nullable = false)
    Long id;

    @Column(name = "date_time", nullable = false)
    LocalDateTime dateTime;

    @Column(name = "account_id", nullable = false)
    Long accountId;

    @Column(name = "account_email", nullable = false)
    String accountEmail;
}
