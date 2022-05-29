package com.smart.chat.service;

import com.smart.chat.api.model.ChatError;
import com.smart.chat.persistence.model.RoomDocument;
import com.smart.chat.persistence.repository.RoomRepository;
import com.smart.chat.service.model.SaveRoomResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    private static final String ROOM_NAME = "some-room-name";
    private static final String ROOM_ID = "some-room-id";

    @Mock
    private RoomRepository roomRepository;

    @Captor
    private ArgumentCaptor<RoomDocument> roomDocumentCaptor;

    private RoomService roomService;

    @BeforeEach
    void setUp() {
        roomService = new RoomService(roomRepository);
    }

    @Test
    void whenRoomExistsThenErrorIsReturned() {
        when(roomRepository.findByName(ROOM_NAME)).thenReturn(Mono.just(RoomDocument.builder().build()));

        Mono<SaveRoomResult> saveRoomResult = roomService.insertRoom(ROOM_NAME);

        assertThat(saveRoomResult.block().getRoomId()).isNull();
        assertThat(saveRoomResult.block().getName()).isNull();
        assertThat(saveRoomResult.block().getError()).isEqualTo(ChatError.ROOM_ALREADY_EXISTS);
    }

    @Test
    void whenRoomDoesNotExistThenReturnedRoomHasTheExpectedAttributes() {
        when(roomRepository.findByName(ROOM_NAME)).thenReturn(Mono.empty());
        when(roomRepository.insert(any(RoomDocument.class)))
                .thenReturn(Mono.just(RoomDocument.builder().id(ROOM_ID).name(ROOM_NAME).build()));

        Mono<SaveRoomResult> saveRoomResult = roomService.insertRoom(ROOM_NAME);

        assertThat(saveRoomResult.block().getRoomId()).isEqualTo(ROOM_ID);
        assertThat(saveRoomResult.block().getName()).isEqualTo(ROOM_NAME);
    }
}
