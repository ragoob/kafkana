package com.kafkana.backend.repositories;

import com.kafkana.backend.abstraction.topicsRepository;
import com.kafkana.backend.models.topicModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
@ConditionalOnProperty(
        value="cache.allowed",
        havingValue = "true",
        matchIfMissing = true
)
public class topicsRepositoryImpl implements topicsRepository {
    private RedisTemplate<String,topicModel> redisTemplate;
    private HashOperations hashOperations;

    public  topicsRepositoryImpl(RedisTemplate<String,topicModel> redisTemplate){
        this.redisTemplate= redisTemplate;
        this.hashOperations =  redisTemplate.opsForHash();;
    }


    @Override
    public void save(topicModel topic ,String clusterId) {
        hashOperations.put("TOPICS-" + clusterId,topic.getName(),topic);
    }

    @Override
    public void save(List<topicModel> topics ,String clusterId) {
        topics.forEach(topicModel-> save(topicModel,clusterId));
    }

    @Override
    public List<topicModel> findAll(String clusterId) {
       return new ArrayList<>(hashOperations.values("TOPICS-" + clusterId));
    }

    @Override
    public void delete(String topicName ,String clusterId) {
        hashOperations.delete("TOPICS-" + clusterId);
    }
}
