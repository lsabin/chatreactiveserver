package com.smart.chat.api.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = "body")
public class MessageRequest {
    private String username;
    private String directMessageDestination;
    private String body;
}
