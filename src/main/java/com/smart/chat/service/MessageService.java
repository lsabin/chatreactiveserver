package com.smart.chat.service;

import com.smart.chat.api.errors.RoomNotFoundException;
import com.smart.chat.api.model.ChatError;
import com.smart.chat.messaging.model.Message;
import com.smart.chat.messaging.redis.RedisPubSubService;
import com.smart.chat.persistence.model.MessageDocument;
import com.smart.chat.persistence.model.PrivateMessageDocument;
import com.smart.chat.persistence.model.RoomDocument;
import com.smart.chat.persistence.repository.MessageRepository;
import com.smart.chat.persistence.repository.PrivateMessageRepository;
import com.smart.chat.persistence.repository.RoomRepository;
import com.smart.chat.service.model.PrivateMessage;
import com.smart.chat.service.model.PrivateMessageResult;
import com.smart.chat.service.model.RoomMessage;
import com.smart.chat.service.model.RoomMessageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final PrivateMessageRepository privateMessageRepository;
    private final RoomRepository roomRepository;
    private final RedisPubSubService pubSubService;

    @Autowired
    public MessageService(MessageRepository messageRepository, PrivateMessageRepository privateMessageRepository, RoomRepository roomRepository, RedisPubSubService pubSubService) {
        this.messageRepository = messageRepository;
        this.privateMessageRepository = privateMessageRepository;
        this.roomRepository = roomRepository;
        this.pubSubService = pubSubService;
    }

    public Flux<Message> subscribeRoomMessages(String roomId) {
        Mono<RoomDocument> existingRoom = roomRepository.findById(roomId);

        return existingRoom
                .flatMapMany(room -> pubSubService.subscribe().filter(message -> roomId.equals(message.getRoomId())))
                .switchIfEmpty(Mono.error(new RoomNotFoundException(ChatError.ROOM_NOT_FOUND.getDescription())));
    }

    public Mono<RoomMessageResult> saveMessage(RoomMessage roomMessage) {
        Mono<RoomDocument> existingRoom = roomRepository.findById(roomMessage.getRoomId());

        return existingRoom
                .flatMap(existing -> sendAndSave(roomMessage))
                .switchIfEmpty(Mono.error(new RoomNotFoundException(ChatError.ROOM_NOT_FOUND.getDescription())));
    }

    private Mono<RoomMessageResult> sendAndSave(RoomMessage roomMessage) {
        pubSubService.publishMessage(Message.builder()
                .roomId(roomMessage.getRoomId())
                .username(roomMessage.getUsername())
                .body(roomMessage.getBody()).build());

        return messageRepository.save(MessageDocument.builder()
                .body(roomMessage.getBody())
                .roomId(roomMessage.getRoomId())
                .senderUsername(roomMessage.getUsername())
                .timestamp(Instant.now().toEpochMilli()).build())
                .map(this::buildRoomMessageResult);
    }

    public Mono<PrivateMessageResult> savePrivateMessage(PrivateMessage privateMessage) {
        pubSubService.publishMessage(Message.builder()
                .username(privateMessage.getUsername())
                .directMessageDestination(privateMessage.getDestinationUsername())
                .body(privateMessage.getBody()).build());

        return privateMessageRepository
                .save(buildPrivateMessageDocument(privateMessage))
                .map(this::buildPrivateMessageResult);
    }

    public Flux<RoomMessageResult> readRoomMessages(String roomId) {
        Mono<RoomDocument> existingRoom = roomRepository.findById(roomId);

        return existingRoom
                .flatMapMany(existing -> buildMessages(messageRepository.findAllByRoomIdOrderByTimestamp(roomId)))
                .switchIfEmpty(Mono.error(new RoomNotFoundException(ChatError.ROOM_NOT_FOUND.getDescription())));
    }

    private Flux<RoomMessageResult> buildMessages(Flux<MessageDocument> messages) {
        return messages.map(this::buildRoomMessageResult);
    }

    private RoomMessageResult buildRoomMessageResult(MessageDocument messageDocument) {
        return RoomMessageResult.builder()
                .roomId(messageDocument.getRoomId())
                .senderUsername(messageDocument.getSenderUsername())
                .timestamp(messageDocument.getTimestamp())
                .body(messageDocument.getBody())
                .build();
    }

    private PrivateMessageResult buildPrivateMessageResult(PrivateMessageDocument privateMessage) {
        return PrivateMessageResult.builder()
                .senderUsername(privateMessage.getSenderUsername())
                .destinationUsername(privateMessage.getDestinationUsername())
                .timestamp(privateMessage.getTimestamp()).build();
    }

    private PrivateMessageDocument buildPrivateMessageDocument(PrivateMessage privateMessage) {
        return PrivateMessageDocument.builder()
                .body(privateMessage.getBody())
                .destinationUsername(privateMessage.getDestinationUsername())
                .senderUsername(privateMessage.getUsername())
                .timestamp(Instant.now().toEpochMilli())
                .build();
    }
}
