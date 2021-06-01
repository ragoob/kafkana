package com.kafkana.backend.abstraction;

import com.kafkana.backend.models.*;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface kafkaMonitorService {
    clusterSummaryModel getClusterSummary(Collection<topicModel> topics);
    List<topicModel> getTopics(String clusterId);
    Optional<topicModel> getTopic(String topic, String clusterId);
    List<consumerModel> getConsumers(Collection<topicModel> topicModels, String clusterId);
    List<messageModel> getMessages(String topic, String clusterId, int size);
    List<messageModel> getMessages(String topic,String clusterId,int size,long start, long end);

}
