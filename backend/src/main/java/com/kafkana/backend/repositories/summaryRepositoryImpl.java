package com.kafkana.backend.repositories;

import com.kafkana.backend.abstraction.summaryRepository;
import com.kafkana.backend.models.clusterSummaryModel;
import com.kafkana.backend.models.topicModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(
        value="cache.allowed",
        havingValue = "true",
        matchIfMissing = true
)
public class summaryRepositoryImpl implements summaryRepository {

    private RedisTemplate<String, clusterSummaryModel> redisTemplate;
    private HashOperations hashOperations;

    public  summaryRepositoryImpl(RedisTemplate<String,clusterSummaryModel> redisTemplate){
        this.redisTemplate= redisTemplate;
        this.hashOperations =  redisTemplate.opsForHash();;
    }

    @Override
    public void save(clusterSummaryModel summary,String clusterId) {
      this.hashOperations.put("CLUSTER_SUMMARY-" + clusterId,"CLUSTER_SUMMARY-" + clusterId,summary);
    }

    @Override
    public clusterSummaryModel find(String clusterId) {
        return (clusterSummaryModel) this.hashOperations.get("CLUSTER_SUMMARY-" + clusterId,"CLUSTER_SUMMARY-" + clusterId);

    }

    @Override
    public void delete(String clusterId) {
        this.hashOperations.delete("CLUSTER_SUMMARY-" + clusterId);
    }
}
