package com.kafkana.backend.abstraction;
import com.kafkana.backend.models.topicModel;

import java.util.List;
import java.util.Map;


public interface  topicsRepository {
    void save(topicModel topic,String clusterId);
    void save(List<topicModel> topics ,String clusterId);
    List<topicModel> findAll(String clusterId);
    void delete(String topicName ,String clusterId);
}
