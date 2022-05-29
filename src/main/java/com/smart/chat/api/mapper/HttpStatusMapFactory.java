package com.smart.chat.api.mapper;

import com.smart.chat.api.model.ChatError;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

import java.util.EnumMap;
import java.util.Map;

@Configuration
public class HttpStatusMapFactory {

    @Bean
    public Map<ChatError, HttpStatus> createErrorMap() {
        Map<ChatError, HttpStatus> map = new EnumMap<>(ChatError.class);
        map.put(ChatError.ROOM_ALREADY_EXISTS, HttpStatus.CONFLICT);
        map.put(ChatError.ROOM_NOT_FOUND, HttpStatus.NOT_FOUND);

        return map;
    }

}
