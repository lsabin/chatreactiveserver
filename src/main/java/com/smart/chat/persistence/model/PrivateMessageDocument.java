package com.smart.chat.persistence.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "chatprivatemessages")
@Getter
@Builder
@AllArgsConstructor
@ToString
public class PrivateMessageDocument {

    @Id
    private String id;

    @Indexed
    private String destinationUsername;

    @Indexed
    private Long timestamp;

    private String senderUsername;
    private String body;
}
