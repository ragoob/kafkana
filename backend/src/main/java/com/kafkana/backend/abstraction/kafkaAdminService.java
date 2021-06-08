package com.kafkana.backend.abstraction;
import com.kafkana.backend.models.brokers;
import com.kafkana.backend.models.createTopicModel;

import java.util.ArrayList;
import java.util.HashMap;

public interface kafkaAdminService {
    void create(createTopicModel model,String clusterId);
    void delete(createTopicModel model,String clusterId);
    ArrayList<brokers> getConfig(String clusterId,boolean refresh);
    HashMap<String,String> getConfig(String clusterId, String id);
    ArrayList<brokers> getBrokers(String clusterId,boolean refresh);
    boolean IsHealth(String clusterIp);
    void  deleteConsumer(String clusterIp, String id);
}
