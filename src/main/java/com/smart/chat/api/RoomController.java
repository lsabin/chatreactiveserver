package com.smart.chat.api;

import com.smart.chat.api.mapper.HttpStatusMapper;
import com.smart.chat.api.model.request.RoomRequest;
import com.smart.chat.api.model.response.RoomResource;
import com.smart.chat.service.RoomService;
import com.smart.chat.service.model.SaveRoomResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(path = "/rooms")
public class RoomController {

    private final RoomService roomService;
    private final HttpStatusMapper httpStatusMapper;
    private final RequestValidator requestValidator;

    @Autowired
    public RoomController(RoomService roomService, HttpStatusMapper httpStatusMapper, RequestValidator requestValidator) {
        this.roomService = roomService;
        this.httpStatusMapper = httpStatusMapper;
        this.requestValidator = requestValidator;
    }

    @GetMapping
    public Flux<RoomResource> readRooms() {
        return roomService.readRooms().map(room -> RoomResource.builder()
                .roomId(room.getId())
                .name(room.getName())
                .build());
    }

    @PostMapping
    public Mono<ResponseEntity<Object>> saveRoom(@RequestBody RoomRequest roomRequest) {
        if (requestValidator.isInvalid(roomRequest)) {
            return Mono.just(new ResponseEntity<>(HttpStatus.BAD_REQUEST));
        }

        Mono<SaveRoomResult> roomResource = roomService.insertRoom(roomRequest.getRoomName());

        return roomResource.map(this::createResponse);
    }

    private ResponseEntity<Object> createResponse(SaveRoomResult saveRoomResult) {
        if (saveRoomResult.getError() != null) {
            return new ResponseEntity<>(saveRoomResult.getError().getDescription(),
                    httpStatusMapper.mapErrorToStatus(saveRoomResult.getError()));
        } else {
            return new ResponseEntity<>(RoomResource.builder()
                    .roomId(saveRoomResult.getRoomId())
                    .name(saveRoomResult.getName()).build(),
                    HttpStatus.CREATED);
        }
    }

}
