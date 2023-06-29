package com.banalytics.box.module.events.jpa;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "token")
public class TokenEntity {
    @Id
    @EqualsAndHashCode.Include
    @Column(name = "token", length = 40, nullable = false)
    String token;

    @Column(name = "expiration_time")
    LocalDateTime expirationTime;

    @Column(name = "object_ref", length = 255)
    String objectReference;

}
