package com.kafkana.backend.abstraction;

import com.kafkana.backend.models.*;
import org.apache.kafka.common.TopicPartition;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public interface kafkaMonitorService {
    clusterSummaryModel getClusterSummary(String clusterIp);
    List<topicModel> getTopics(String clusterId,boolean showDefaultConfig);
    Optional<topicModel> getTopic(String topic, String clusterId,boolean showDefaultConfig);
    List<consumerModel> getConsumers(String clusterId);
    List<messageModel> getMessages(String topic,String clusterId,long size,long start, long end,String sortDirection);
    List<messageModel> getMessages(String topic,String clusterId,long size,String sortDirection);
    List<messageModel> getMessages(String topic,String clusterIp,long size,long start,String sortingDirection);
    List<messageModel> getMessagesUntilTime(String topic,String clusterIp,long size,long end,String sortingDirection);
    Map<Integer,Long> getLastOffsetPerPartition(String topic, String clusterIp);
    Map<String,Map<String,Long>> GetLastOffsetPerPartition(String clusterIp);
}
