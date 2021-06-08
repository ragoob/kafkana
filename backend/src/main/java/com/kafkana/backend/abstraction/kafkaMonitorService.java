package com.kafkana.backend.abstraction;

import com.kafkana.backend.models.*;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface kafkaMonitorService {
    clusterSummaryModel getClusterSummary(String clusterIp,boolean refresh);
    List<topicModel> getTopics(String clusterId,boolean showDefaultConfig,boolean refresh);
    Optional<topicModel> getTopic(String topic, String clusterId,boolean showDefaultConfig);
    List<consumerModel> getConsumers(Collection<topicModel> topicModels, String clusterId,boolean refresh);
    List<messageModel> getMessages(String topic,String clusterId,int size,long start, long end);
    List<messageModel> getLatestMessages(String topic,String clusterId,int size);
    List<messageModel> getMessages(String topic,String clusterIp,int size,long start);
    List<messageModel> getMessagesUntilTime(String topic,String clusterIp,int size,long end);
}
