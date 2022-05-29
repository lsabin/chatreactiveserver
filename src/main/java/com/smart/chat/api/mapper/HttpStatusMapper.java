package com.smart.chat.api.mapper;

import com.smart.chat.api.model.ChatError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class HttpStatusMapper {
    private final Map<ChatError, HttpStatus> errorMap;

    @Autowired
    public HttpStatusMapper(Map<ChatError, HttpStatus> errorMap) {
        this.errorMap = errorMap;
    }

    public HttpStatus mapErrorToStatus(ChatError error) {
        return errorMap.getOrDefault(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
