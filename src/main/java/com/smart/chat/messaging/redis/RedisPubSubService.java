package com.smart.chat.messaging.redis;

import com.smart.chat.config.properties.PubSubConfigProperties;
import com.smart.chat.messaging.model.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.ReactiveSubscription;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.ReactiveRedisMessageListenerContainer;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Collections;

@Service
@Slf4j
public class RedisPubSubService {
    private final ReactiveRedisTemplate<String, Message> reactiveTemplate;
    private final ReactiveRedisMessageListenerContainer redisMessageListenerContainer;

    private final ChannelTopic channelTopic;

    @Autowired
    public RedisPubSubService(ReactiveRedisTemplate<String, Message> reactiveTemplate, ReactiveRedisMessageListenerContainer redisMessageListenerContainer, PubSubConfigProperties configProperties, ChannelTopic channelTopic) {
        this.reactiveTemplate = reactiveTemplate;
        this.redisMessageListenerContainer = redisMessageListenerContainer;
        this.channelTopic = channelTopic;
    }

    public Mono<Void> publishMessage(Message message) {
        message.setTimestamp(Instant.now().toEpochMilli());
        reactiveTemplate
                .convertAndSend(channelTopic.getTopic(), message)
                .subscribe();

        return Mono.empty();
    }

    public Flux<Message> subscribe() {
        return redisMessageListenerContainer
                .receive(Collections.singletonList(channelTopic),
                        reactiveTemplate.getSerializationContext().getKeySerializationPair(),
                        reactiveTemplate.getSerializationContext().getValueSerializationPair())
                .map(ReactiveSubscription.Message::getMessage);
    }
}
