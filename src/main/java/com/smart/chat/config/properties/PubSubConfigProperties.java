package com.smart.chat.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Configuration
@ConfigurationProperties(value = "messages")
@Validated
public class PubSubConfigProperties {

    private String messagesChannel = "messages";

    public String getMessagesChannel() {
        return messagesChannel;
    }

    public void setMessagesChannel(String messagesChannel) {
        this.messagesChannel = messagesChannel;
    }
}
