package com.kafkana.backend.repositories;
import com.kafkana.backend.abstraction.consumerRepository;
import com.kafkana.backend.models.consumerModel;
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
public class consumerRepositoryImpl implements consumerRepository {
    private RedisTemplate<String, consumerModel> redisTemplate;
    private HashOperations hashOperations;

    public  consumerRepositoryImpl(RedisTemplate<String,consumerModel> redisTemplate){
        this.redisTemplate= redisTemplate;
        this.hashOperations =  redisTemplate.opsForHash();;
    }

    @Override
    public void save(consumerModel consumer ,String clusterId) {
        this.hashOperations.put("CONSUMER-" + clusterId,consumer.getGroupId(),consumer);
    }

    @Override
    public void save(List<consumerModel> consumers ,String clusterId) {
        consumers.forEach(c-> save(c, clusterId));
    }

    @Override
    public List<consumerModel> findAll(String clusterId) {
        return new ArrayList<>(hashOperations.values("CONSUMER-" + clusterId));
    }

    @Override
    public void delete(String groupId,String clusterId) {
      this.hashOperations.delete(groupId);
    }
}
