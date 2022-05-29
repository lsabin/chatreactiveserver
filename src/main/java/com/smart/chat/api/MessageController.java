package com.smart.chat.api;

import com.smart.chat.api.model.request.MessageRequest;
import com.smart.chat.api.model.response.PrivateMessageResource;
import com.smart.chat.api.model.response.RoomMessageResource;
import com.smart.chat.messaging.model.Message;
import com.smart.chat.service.MessageService;
import com.smart.chat.service.model.PrivateMessage;
import com.smart.chat.service.model.PrivateMessageResult;
import com.smart.chat.service.model.RoomMessage;
import com.smart.chat.service.model.RoomMessageResult;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(path = "/room")
@Slf4j
public class MessageController {

    private final MessageService messageService;

    @Autowired
    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping(path="{roomId}/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Publisher<Message> readMessages(@PathVariable String roomId) {
        log.info("Subscribed to messages in room={}", roomId);
        return messageService.subscribeRoomMessages(roomId);
    }

    @GetMapping(path = "{roomId}/messages", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Flux<RoomMessageResource>> fix(@PathVariable String roomId) {
        log.info("Reading messages in room={}", roomId);
        Flux<RoomMessageResult> messagesResult =  messageService.readRoomMessages(roomId);

        return ResponseEntity.ok(messagesResult.map(this::buildRoomMessageResource));
    }

    @PostMapping(path="{roomId}/messages", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Object>> sendMessage(@PathVariable String roomId, @RequestBody MessageRequest messageRequest) {
        log.info("Received new message: {}", messageRequest);

        Mono<RoomMessageResult> messageResult = messageService.saveMessage(RoomMessage.builder()
                .roomId(roomId)
                .body(messageRequest.getBody())
                .username(messageRequest.getUsername()).build());

        return messageResult.map(this::createRoomMessageResponse);
    }

    @PostMapping(path="{username}/privatemessages", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<PrivateMessageResource> sendPrivateMessage(@PathVariable String username, @RequestBody MessageRequest messageRequest) {
        log.info("Received new private message: {}", messageRequest);
        Mono<PrivateMessageResult> saved = messageService.savePrivateMessage(PrivateMessage.builder()
                .destinationUsername(username)
                .body(messageRequest.getBody())
                .username(messageRequest.getUsername()).build());

        return saved.map(newMessage -> PrivateMessageResource.builder()
                .username(newMessage.getSenderUsername())
                .destinationUsername(newMessage.getDestinationUsername())
                .timestamp(newMessage.getTimestamp()).build());
    }

    private ResponseEntity<Object> createRoomMessageResponse(RoomMessageResult messageResult) {
        return new ResponseEntity<>(buildRoomMessageResource(messageResult), HttpStatus.CREATED);
    }

    private RoomMessageResource buildRoomMessageResource(RoomMessageResult roomMessage) {
        return RoomMessageResource.builder()
                .roomId(roomMessage.getRoomId())
                .body(roomMessage.getBody())
                .timestamp(roomMessage.getTimestamp())
                .username(roomMessage.getSenderUsername()).build();
    }

}
