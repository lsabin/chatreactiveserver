package com.smart.chat.api.model.response;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
public class PrivateMessageResource {
    private final String username;
    private final String destinationUsername;
    private final Long timestamp;
}
