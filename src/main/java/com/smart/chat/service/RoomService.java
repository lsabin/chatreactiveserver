package com.smart.chat.service;

import com.smart.chat.api.model.ChatError;
import com.smart.chat.persistence.model.RoomDocument;
import com.smart.chat.persistence.repository.RoomRepository;
import com.smart.chat.service.model.SaveRoomResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class RoomService {

    private final RoomRepository roomRepository;

    @Autowired
    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    public Mono<SaveRoomResult> insertRoom(String roomName) {
        Mono<RoomDocument> existingRoom = roomRepository.findByName(roomName);

        return existingRoom
                .map(existing -> SaveRoomResult.builder().error(ChatError.ROOM_ALREADY_EXISTS).build())
                .switchIfEmpty(Mono.defer(() ->
                    roomRepository.insert(RoomDocument.builder().name(roomName).build())
                                    .map(saved -> SaveRoomResult.builder().roomId(saved.getId()).name(saved.getName()).build())
                ));
    }

    public Flux<RoomDocument> readRooms() {
        return roomRepository.findAll();
    }


}
