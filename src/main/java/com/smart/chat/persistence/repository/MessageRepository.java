package com.smart.chat.persistence.repository;

import com.smart.chat.persistence.model.MessageDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface MessageRepository extends ReactiveMongoRepository<MessageDocument, String> {
    Flux<MessageDocument> findAllByRoomIdOrderByTimestamp(String roomId);
}
