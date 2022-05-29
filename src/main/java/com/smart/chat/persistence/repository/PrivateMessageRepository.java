package com.smart.chat.persistence.repository;

import com.smart.chat.persistence.model.PrivateMessageDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface PrivateMessageRepository extends ReactiveMongoRepository<PrivateMessageDocument, String> {
    Flux<PrivateMessageDocument> findAllByDestinationUsernameOrderByTimestamp(String destinationUsername);
}
