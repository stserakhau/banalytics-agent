package com.banalytics.box.module.events.jpa;

import com.banalytics.box.api.integration.AbstractMessage;
import com.banalytics.box.api.integration.MessageType;
import com.banalytics.box.api.integration.webrtc.channel.events.AbstractEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(
        name = "event_store",
        indexes = {
                @Index(name = "idx_event_node_uuid_dateTime_messageType", columnList = "node_uuid, date_time, message_type")
        }
)
@TypeDefs({
        @TypeDef(name = "AbstractMessage", typeClass = AbstractMessageJsonUserType.class)
})
public class EventStore {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @Column(name = "id", nullable = false)
    Long id;

    @Column(name = "node_uuid", nullable = true)
    UUID nodeUuid;

    @Column(name = "date_time", nullable = false)
    LocalDateTime dateTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false)
    MessageType messageType;

    @Type(type = "AbstractMessage")
    @Column(name = "event", nullable = false)
    AbstractMessage event;
}
