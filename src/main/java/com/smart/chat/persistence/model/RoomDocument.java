package com.smart.chat.persistence.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "chatrooms")
@Getter
@Builder
@AllArgsConstructor
@ToString
public class RoomDocument {

    @Id
    private String id;

    @Indexed
    private String name;
}
