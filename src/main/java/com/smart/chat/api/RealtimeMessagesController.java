package com.smart.chat.api;

import com.smart.chat.messaging.model.Message;
import com.smart.chat.messaging.redis.RedisPubSubService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.annotation.ConnectMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;

import java.util.Objects;

@Controller
@Slf4j
public class RealtimeMessagesController {

    private final RedisPubSubService pubSubService;

    @Autowired
    public RealtimeMessagesController(RedisPubSubService pubSubService) {
        this.pubSubService = pubSubService;
    }

    @ConnectMapping
    public void onConnect(RSocketRequester requester) {
        log.info("Connected to RSocket");

        Objects.requireNonNull(requester.rsocket(), "RSocket connection should not be null")
                .onClose()
                .doOnError(error -> log.warn(requester.rsocketClient() + " Client closed"))
                .doFinally(consumer -> log.info(requester.rsocketClient() + " Client disconnected"))
                .subscribe();
    }

    @MessageMapping("roommessages")
    public Flux<Message> subscribeRoomMessages(String roomId) {
        log.info("Message received to subscribe to room={}", roomId);

        return pubSubService.subscribe().filter(message -> roomId.equals(message.getRoomId()));
    }

    @MessageMapping("chatmessages")
    public Flux<Message> subscribeMessages(String user) {
        log.info("Getting direct messages for user={}", user);
        Flux<Message> messages = pubSubService.subscribe();

        return messages.filter(message -> user.equals(message.getDirectMessageDestination()));
    }

}
