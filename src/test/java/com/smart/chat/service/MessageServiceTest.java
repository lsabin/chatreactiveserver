package com.smart.chat.service;

import com.smart.chat.api.errors.RoomNotFoundException;
import com.smart.chat.messaging.model.Message;
import com.smart.chat.messaging.redis.RedisPubSubService;
import com.smart.chat.persistence.model.MessageDocument;
import com.smart.chat.persistence.model.RoomDocument;
import com.smart.chat.persistence.repository.MessageRepository;
import com.smart.chat.persistence.repository.PrivateMessageRepository;
import com.smart.chat.persistence.repository.RoomRepository;
import com.smart.chat.service.model.RoomMessage;
import com.smart.chat.service.model.RoomMessageResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    private static final String ROOM_ID = "some-room-id";
    private static final String MESSAGE_BODY = "some-message-body";
    private static final String SENDER_USERNAME = "some-username";

    private MessageService messageService;
    @Mock
    private MessageRepository messageRepository;
    @Mock
    private PrivateMessageRepository privateMessageRepository;
    @Mock
    private RoomRepository roomRepository;
    @Mock
    private RedisPubSubService pubSubService;

    @Captor
    private ArgumentCaptor<MessageDocument> messageDocumentCaptor;

    @Captor
    private ArgumentCaptor<Message> messageCaptor;

    @BeforeEach
    void setUp() {
        messageService = new MessageService(messageRepository, privateMessageRepository, roomRepository, pubSubService);
    }

    @Test
    @DisplayName("When a message is inserted in a room, then message is saved in database")
    void savedMessage() {
        RoomMessage messageRequest = RoomMessage.builder()
                .roomId(ROOM_ID)
                .username(SENDER_USERNAME)
                .body(MESSAGE_BODY)
                .build();

        MessageDocument savedMessage = MessageDocument.builder()
                .roomId("room-id")
                .senderUsername("some-username")
                .body("some-body")
                .build();

        when(roomRepository.findById(ROOM_ID)).thenReturn(Mono.just(RoomDocument.builder().build()));
        when(messageRepository.save(any(MessageDocument.class))).thenReturn(Mono.just(MessageDocument.builder().build()));

        Mono<RoomMessageResult> roomResult = messageService.saveMessage(messageRequest);

        StepVerifier
                .create(roomResult)
                //.assertNext(room -> assertThat(room.getRoomId()).isEqualTo("room-id"))
                //.expectComplete()
                .thenCancel()
                .verify();

        verify(messageRepository).save(messageDocumentCaptor.capture());
        assertThat(messageDocumentCaptor.getValue().getRoomId()).isEqualTo(ROOM_ID);
    }

    @Test
    @DisplayName("When message is inserted in a room, then message is published to the queue")
    void publishedMessage() {
        RoomMessage messageRequest = RoomMessage.builder()
                .roomId(ROOM_ID)
                .username(SENDER_USERNAME)
                .body(MESSAGE_BODY)
                .build();

        when(roomRepository.findById(ROOM_ID)).thenReturn(Mono.just(RoomDocument.builder().build()));
        when(messageRepository.save(any(MessageDocument.class))).thenReturn(Mono.just(MessageDocument.builder().build()));

        Mono<RoomMessageResult> roomResult = messageService.saveMessage(messageRequest);

        StepVerifier
                .create(roomResult)
                .thenCancel()
                .verify();

        verify(pubSubService).publishMessage(messageCaptor.capture());
        assertThat(messageCaptor.getValue().getRoomId()).isEqualTo(ROOM_ID);
    }

    @Test
    @DisplayName("When message is inserted in a non existing room, then error is thrown")
    void errorThrownWhenRoomDoesNotExist() {
        RoomMessage messageRequest = RoomMessage.builder()
                .roomId(ROOM_ID)
                .username(SENDER_USERNAME)
                .body(MESSAGE_BODY)
                .build();

        when(roomRepository.findById(ROOM_ID)).thenReturn(Mono.empty());

        Mono<RoomMessageResult> roomResult = messageService.saveMessage(messageRequest);

        StepVerifier
                .create(roomResult)
                .expectError(RoomNotFoundException.class)
                .verify();
    }
}
