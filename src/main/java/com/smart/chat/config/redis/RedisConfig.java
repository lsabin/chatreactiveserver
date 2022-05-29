package com.smart.chat.config.redis;

import com.smart.chat.config.properties.PubSubConfigProperties;
import com.smart.chat.messaging.model.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.ReactiveRedisMessageListenerContainer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    private final PubSubConfigProperties configProperties;

    @Autowired
    public RedisConfig(PubSubConfigProperties configProperties) {
        this.configProperties = configProperties;
    }

    @Bean
    public ReactiveRedisTemplate<String, Message> reactiveRedisTemplate(ReactiveRedisConnectionFactory reactiveRedisConnectionFactory) {
        StringRedisSerializer keySerializer = new StringRedisSerializer();
        Jackson2JsonRedisSerializer<Message> valueSerializer = new Jackson2JsonRedisSerializer<>(Message.class);
        RedisSerializationContext.RedisSerializationContextBuilder<String, Message> builder =
                RedisSerializationContext.newSerializationContext(keySerializer);

        RedisSerializationContext<String, Message> context = builder.value(valueSerializer).build();

        return new ReactiveRedisTemplate<>(reactiveRedisConnectionFactory, context);
    }

    @Bean
    public ReactiveRedisMessageListenerContainer container(ReactiveRedisConnectionFactory connectionFactory) {
        return new ReactiveRedisMessageListenerContainer(connectionFactory);
    }

    @Bean
    public ChannelTopic messagesChannelTopic() {
        return new ChannelTopic(configProperties.getMessagesChannel());
    }

}
