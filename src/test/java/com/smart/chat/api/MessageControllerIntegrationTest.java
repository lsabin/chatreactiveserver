package com.smart.chat.api;

import com.smart.chat.api.model.request.MessageRequest;
import com.smart.chat.api.model.request.RoomRequest;
import com.smart.chat.api.model.response.RoomResource;
import com.smart.chat.messaging.model.Message;
import com.smart.chat.persistence.model.RoomDocument;
import com.smart.chat.persistence.repository.RoomRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MessageControllerIntegrationTest {

    private static final String MESSAGE_BODY = "some-message-body";
    private static final String SENDER_USERNAME = "some-username";

    private String ROOM_ID;

    @Autowired
    private WebTestClient webClient;

    @Autowired
    private RoomRepository roomRepository;

    @BeforeEach
    void setUp() {
        Mono<RoomDocument> createdRoom = roomRepository.save(RoomDocument.builder().name("room-name").build());
        ROOM_ID = createdRoom.block().getId();
    }

    @AfterEach
    void deleteData() {
        roomRepository.deleteAll().block();
    }

    @Test
    void roomMessagesList() {
        RoomRequest roomRequest = new RoomRequest("room-name");

        MessageRequest messageRequest = new MessageRequest();
        messageRequest.setUsername(SENDER_USERNAME);
        messageRequest.setBody(MESSAGE_BODY);

//        RoomResource createdRoom = webClient.post().uri("/rooms")
//                .accept(MediaType.APPLICATION_JSON)
//                .contentType(MediaType.APPLICATION_JSON)
//                .body(Mono.just(roomRequest), RoomRequest.class)
//                .exchange()
//                .expectStatus().isCreated()
//                .returnResult(RoomResource.class)
//                .getResponseBody().blockFirst();
//
//        String createdRoomId = createdRoom.getRoomId();

//        Mono<RoomDocument> createdRoom = roomRepository.save(RoomDocument.builder().name("room-name").build());
//        String createdRoomId = createdRoom.block().getId();

        webClient.post().uri("/room/{roomId}/messages", ROOM_ID)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(messageRequest), MessageRequest.class)
                .exchange()
                .expectStatus().isCreated();

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
    void subscribeToRoomMessages() throws Exception {
        MessageRequest messageRequest = new MessageRequest();
        messageRequest.setUsername(SENDER_USERNAME);
        messageRequest.setBody(MESSAGE_BODY);

        Flux<Message> messages = webClient.get().uri("/room/{roomId}/subscribe", ROOM_ID)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .returnResult(Message.class)
                .getResponseBody().delaySubscription(Duration.ofSeconds(1));

        webClient.post().uri("/room/{roomId}/messages", ROOM_ID)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(messageRequest), MessageRequest.class)
                .exchange()
                .expectStatus().isCreated();

        StepVerifier
                .create(messages)
                .assertNext(message -> assertThat(message.getBody()).isEqualTo(MESSAGE_BODY))
                .expectComplete()
                .verify(Duration.ofSeconds(1));
    }

}
