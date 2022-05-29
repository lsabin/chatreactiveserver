package com.smart.chat.persistence;

import com.smart.chat.persistence.model.RoomDocument;
import com.smart.chat.persistence.repository.RoomRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
public class RoomRepositoryTest {

    private static final String ROOM_ID_1 = "room-id-1";
    private static final String ROOM_ID_2 = "room-id-2";
    private static final String ROOM_NAME_1 = "room-name-1";
    private static final String ROOM_NAME_2 = "room-name-2";
    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private ReactiveMongoTemplate mongoTemplate;

    @BeforeEach
    void setUp() {
        mongoTemplate.save(RoomDocument.builder().id(ROOM_ID_1).name(ROOM_NAME_1).build()).block();
        mongoTemplate.save(RoomDocument.builder().id(ROOM_ID_2).name(ROOM_NAME_2).build()).block();
    }

    @Test
    @DisplayName("When rooms exist, then find result has the expected rooms")
    public void findAllExistingRooms() {
        Flux<RoomDocument> rooms = roomRepository.findAll();

        StepVerifier
                .create(rooms)
                .assertNext(document -> assertThat(document.getName()).isEqualTo(ROOM_NAME_1))
                .assertNext(document -> assertThat(document.getName()).isEqualTo(ROOM_NAME_2))
                .expectComplete()
                .verify();

        assertThat(rooms.toIterable()).extracting(RoomDocument::getName).containsExactlyInAnyOrder(ROOM_NAME_1, ROOM_NAME_2);
    }

    @Test
    @DisplayName("When room exists, then result is returned")
    void whenRoomExistsThenResultIsReturned() {
        Mono<RoomDocument> existingDocument = roomRepository.findByName(ROOM_NAME_1);

        StepVerifier
                .create(existingDocument)
                .assertNext(document -> assertThat(document.getId()).isEqualTo(ROOM_ID_1))
                .expectComplete()
                .verify();
    }


    @Test
    @DisplayName("When room does not exist, then no result is returned")
    void whenRoomDoesNotExistThenResultIsReturned() {
        Mono<RoomDocument> nonExistingRoom = roomRepository.findByName("non-existent-room-id");

        StepVerifier.create(nonExistingRoom)
                .expectNextCount(0)
                .verifyComplete();
    }

    @AfterEach
    void cleanUpDatabase() {
       mongoTemplate.getMongoDatabase().block().drop();
    }

}
