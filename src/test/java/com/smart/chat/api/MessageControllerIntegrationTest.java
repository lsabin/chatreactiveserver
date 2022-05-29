package com.smart.chat.api;

import com.smart.chat.api.model.request.MessageRequest;
import com.smart.chat.api.model.request.RoomRequest;
import com.smart.chat.config.properties.PubSubConfigProperties;
import com.smart.chat.messaging.model.Message;
import com.smart.chat.persistence.model.RoomDocument;
import com.smart.chat.persistence.repository.RoomRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.ReactiveSubscription;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MessageControllerIntegrationTest {

    private static final String MESSAGE_BODY = "some-message-body";
    private static final String SENDER_USERNAME = "some-username";

    private String ROOM_ID;

    @Autowired
    private WebTestClient webClient;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private ReactiveRedisTemplate<String, Message> reactiveTemplate;

    @Autowired
    private PubSubConfigProperties configProperties;

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

        TestMessagesSubscriber messagesSubscriber = new TestMessagesSubscriber(reactiveTemplate, configProperties);
        messagesSubscriber.init();

        webClient.post().uri("/room/{roomId}/messages", ROOM_ID)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(messageRequest).delayElement(Duration.ofSeconds(1)), MessageRequest.class)
                .exchange()
                .expectStatus().isCreated();

        List<Message> receivedMessages = messagesSubscriber.getReceivedMessages();

        assertThat(receivedMessages).extracting(Message::getRoomId).containsExactly(ROOM_ID);
        assertThat(receivedMessages).extracting(Message::getUsername).containsExactly(SENDER_USERNAME);
        assertThat(receivedMessages).extracting(Message::getBody).containsExactly(MESSAGE_BODY);
    }

    private class TestMessagesSubscriber {

        private final ReactiveRedisTemplate<String, Message> reactiveTemplate;
        private final PubSubConfigProperties pubSubConfigProperties;
        private final List<Message> receivedMessages = new ArrayList<>();

        public TestMessagesSubscriber(ReactiveRedisTemplate<String, Message> reactiveTemplate, PubSubConfigProperties pubSubConfigProperties) {
            this.reactiveTemplate = reactiveTemplate;
            this.pubSubConfigProperties = pubSubConfigProperties;
        }

        public void init() {
            this.reactiveTemplate.listenTo(ChannelTopic.of(pubSubConfigProperties.getMessagesChannel()))
                    .map(ReactiveSubscription.Message::getMessage)
                    .subscribe(receivedMessages::add);
        }

        public List<Message> getReceivedMessages() {
            return receivedMessages;
        }
    }

}
