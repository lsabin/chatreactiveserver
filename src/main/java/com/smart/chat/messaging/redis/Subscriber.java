package com.smart.chat.messaging.redis;

import com.smart.chat.messaging.model.Message;
import org.springframework.data.redis.connection.ReactiveSubscription;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;


// TODO: delete
@Service
public class Subscriber {

    private final ReactiveRedisTemplate<String, Message> reactiveTemplate;

    public Subscriber(ReactiveRedisTemplate<String, Message> reactiveTemplate) {
        this.reactiveTemplate = reactiveTemplate;
    }

    //@PostConstruct
    private void init() {
        this.reactiveTemplate.listenTo(ChannelTopic.of("messages"))
                .map(ReactiveSubscription.Message::getMessage)
                .subscribe(message -> System.out.println("RECEIVED but secondary: "  + message));


    }
}
