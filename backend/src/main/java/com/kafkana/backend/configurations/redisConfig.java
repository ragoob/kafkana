package com.kafkana.backend.configurations;
import com.kafkana.backend.models.clusterSummaryModel;
import com.kafkana.backend.models.consumerModel;
import com.kafkana.backend.models.topicModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class redisConfig {
    @Value("${redis.host}")
    private String redisHost;
    @Value("${redis.port}")
    private  int redisPort;

    @Bean
    JedisConnectionFactory jedisConnectionFactory(){
        JedisConnectionFactory jedisConFactory
                = new JedisConnectionFactory();
        jedisConFactory.setHostName(redisHost);
        jedisConFactory.setPort(redisPort);
        return jedisConFactory;
    }

    @Bean
    @ConditionalOnProperty(
            value="cache.allowed",
            havingValue = "true",
            matchIfMissing = true)
    RedisTemplate<String, topicModel> topicsCache(){
        RedisTemplate<String, topicModel> template = new RedisTemplate<>();
        template.setConnectionFactory(jedisConnectionFactory());
        return  template;
    }

    @Bean
    @ConditionalOnProperty(
            value="cache.allowed",
            havingValue = "true",
            matchIfMissing = true)
    RedisTemplate<String, clusterSummaryModel> summaryCache(){
        RedisTemplate<String, clusterSummaryModel> template = new RedisTemplate<>();
        template.setConnectionFactory(jedisConnectionFactory());
        return  template;
    }

    @Bean
    @ConditionalOnProperty(
            value="cache.allowed",
            havingValue = "true",
            matchIfMissing = true)
    RedisTemplate<String, consumerModel> consumerCache(){
        RedisTemplate<String, consumerModel> template = new RedisTemplate<>();
        template.setConnectionFactory(jedisConnectionFactory());
        return  template;
    }
}
