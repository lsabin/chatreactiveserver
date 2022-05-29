package com.smart.chat.persistence.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "chatmessages")
@Getter
@Builder
@AllArgsConstructor
@ToString
public class MessageDocument {

    @Id
    private String id;

    @Indexed
    private String roomId;

    @Indexed
    private Long timestamp;

    private String senderUsername;
    private String body;
}
