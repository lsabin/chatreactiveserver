package com.smart.chat.api;

import com.smart.chat.api.errors.RoomNotFoundException;
import com.smart.chat.api.mapper.HttpStatusMapper;
import com.smart.chat.api.model.request.MessageRequest;
import com.smart.chat.messaging.model.Message;
import com.smart.chat.service.MessageService;
import com.smart.chat.service.model.RoomMessage;
import com.smart.chat.service.model.RoomMessageResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = MessageController.class, excludeAutoConfiguration = ReactiveSecurityAutoConfiguration.class)
class MessageControllerTest {

    private static final String ROOM_ID = "some-room-id";
    private static final String MESSAGE_BODY = "some-message-body";
    private static final String SENDER_USERNAME = "some-username";

    @Autowired
    private WebTestClient webClient;

    @MockBean
    private MessageService messageService;

    @MockBean
    private HttpStatusMapper httpStatusMapper;

    @Test
    void roomMessagesList() {
        when(messageService.readRoomMessages(ROOM_ID))
                .thenReturn(Flux.just(RoomMessageResult.builder()
                        .roomId(ROOM_ID)
                        .senderUsername(SENDER_USERNAME)
                        .body(MESSAGE_BODY)
                        .build()));


        webClient.get().uri("/room/{roomId}/messages", ROOM_ID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$").isArray()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$[0].roomId").isEqualTo(ROOM_ID)
                .jsonPath("$[0].username").isEqualTo(SENDER_USERNAME)
                .jsonPath("$[0].body").isEqualTo(MESSAGE_BODY);
    }

    @Test
    void roomNotFoundMessagesList() {
        when(messageService.readRoomMessages(ROOM_ID)).thenThrow(RoomNotFoundException.class);

        webClient.get().uri("/room/{roomId}/messages", ROOM_ID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void postMessageToRoom() {
        MessageRequest messageRequest = new MessageRequest();
        messageRequest.setUsername(SENDER_USERNAME);
        messageRequest.setBody(MESSAGE_BODY);

        when(messageService.saveMessage(any(RoomMessage.class)))
                .thenReturn(Mono.just(RoomMessageResult.builder()
                        .roomId(ROOM_ID)
                        .senderUsername(SENDER_USERNAME)
                        .body(MESSAGE_BODY)
                        .build()));

        webClient.post().uri("/room/{roomId}/messages", ROOM_ID)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(messageRequest), MessageRequest.class)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.roomId").isEqualTo(ROOM_ID)
                .jsonPath("$.username").isEqualTo(SENDER_USERNAME)
                .jsonPath("$.body").isEqualTo(MESSAGE_BODY);
    }

    @Test
    void subscribeToMessages() {
        when(messageService.subscribeRoomMessages(ROOM_ID))
                .thenReturn(Flux.just(Message.builder()
                        .roomId(ROOM_ID)
                        .username(SENDER_USERNAME)
                        .body(MESSAGE_BODY)
                        .build()));

        Flux<Message> messages = webClient.get().uri("/room/{roomId}/subscribe", ROOM_ID)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .returnResult(Message.class)
                .getResponseBody();

        StepVerifier
                .create(messages)
                .assertNext(message -> assertThat(message.getRoomId()).isEqualTo(ROOM_ID))
                .expectComplete()
                .verify();
    }
}
