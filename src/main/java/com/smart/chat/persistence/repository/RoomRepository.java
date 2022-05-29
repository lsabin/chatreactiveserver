package com.smart.chat.persistence.repository;

import com.smart.chat.persistence.model.RoomDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface RoomRepository  extends ReactiveMongoRepository<RoomDocument, String> {
    Mono<RoomDocument> findByName(String name);
}
