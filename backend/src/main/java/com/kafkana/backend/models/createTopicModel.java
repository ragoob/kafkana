package com.kafkana.backend.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class createTopicModel {
    private  int partitions;
    private  String topicName;
    private  short replication;
    private HashMap<String,String > configurations;
    private List<String> topics;
    public createTopicModel(int partitions, String topicName, short replication, HashMap<String, String> configurations) {
        this.partitions = partitions;
        this.topicName = topicName;
        this.replication = replication;
        this.configurations = configurations;

    }

    public createTopicModel() {
    }


    public createTopicModel(String topicName) {
        this.topicName = topicName;

    }

    public createTopicModel(String topicName, int partitions) {
        this.partitions = partitions;
        this.topicName = topicName;
    }

    public int getPartitions() {
        return partitions;
    }

    public void setPartitions(int partitions) {
        this.partitions = partitions;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public short getReplication() {
        return replication;
    }

    public void setReplication(short replication) {
        this.replication = replication;
    }

    public HashMap<String, String> getConfigurations() {
        return configurations;
    }

    public void setConfigurations(HashMap<String, String> configurations) {
        this.configurations = configurations;
    }

    public List<String> getTopics() {
        return Arrays.stream(topicName.split(",")).toList();
    }
}
