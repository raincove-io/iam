package io.github.erfangc.iam;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class RedisConfiguration {
    private final RedisProperties properties;

    public RedisConfiguration(RedisProperties properties) {
        this.properties = properties;
    }

    @Bean
    public RedisClient redisClient() {
        final RedisURI redisURI = new RedisURI(properties.getHost(), properties.getPort(), Duration.ofSeconds(60));
        return RedisClient.create(redisURI);
    }
}
