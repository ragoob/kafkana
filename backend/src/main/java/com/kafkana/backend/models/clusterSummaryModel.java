package com.kafkana.backend.models;

import java.io.Serializable;
import java.security.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class clusterSummaryModel implements Serializable {
    private int topicCount;
    private int partitionCount;
    private int underReplicatedCount;
    private double preferredReplicaPercent;
    private  int brokerCount;
    private Date  timeStamp;


    /**
     * Number of partitions each broker is the leader for
     */
    private Map<Integer, Integer> brokerLeaderPartitionCount = new HashMap<>();

    /**
     * Number of partitions each broker should be the leader for
     */
    private Map<Integer, Integer> brokerPreferredLeaderPartitionCount = new HashMap<>();

    public int getTopicCount() {
        return topicCount;
    }

    public void setTopicCount(int topicCount) {
        this.topicCount = topicCount;
    }

    public int getPartitionCount() {
        return partitionCount;
    }

    public void setPartitionCount(int partitionCount) {
        this.partitionCount = partitionCount;
    }

    public int getUnderReplicatedCount() {
        return underReplicatedCount;
    }

    public void setUnderReplicatedCount(int underReplicatedCount) {
        this.underReplicatedCount = underReplicatedCount;
    }

    public double getPreferredReplicaPercent() {
        return preferredReplicaPercent;
    }

    public void setPreferredReplicaPercent(double preferredReplicaPercent) {
        this.preferredReplicaPercent = preferredReplicaPercent;
    }

    public Map<Integer, Integer> getBrokerLeaderPartitionCount() {
        return brokerLeaderPartitionCount;
    }

    public Integer getBrokerLeaderPartitionCount(int brokerId) {
        return brokerLeaderPartitionCount.get(brokerId);
    }

    public double getBrokerLeaderPartitionRatio(int brokerId) {
        final var totalPartitionCount = getPartitionCount();
        if (totalPartitionCount != 0) {
            final var brokerPartitionCount = getBrokerLeaderPartitionCount(brokerId);
            return brokerPartitionCount != null ? (double) brokerPartitionCount / totalPartitionCount : 0;
        } else {
            return 0;
        }
    }

    public void addBrokerLeaderPartition(int brokerId) {
        addBrokerLeaderPartition(brokerId, 1);
    }

    public void addBrokerLeaderPartition(int brokerId, int partitionCount) {
        brokerLeaderPartitionCount.compute(brokerId, (k, v) -> v == null ? partitionCount : v + partitionCount);
    }

    public Map<Integer, Integer> getBrokerPreferredLeaderPartitionCount() {
        return brokerPreferredLeaderPartitionCount;
    }

    public void addBrokerPreferredLeaderPartition(int brokerId) {
        addBrokerPreferredLeaderPartition(brokerId, 1);
    }

    public void addBrokerPreferredLeaderPartition(int brokerId, int partitionCount) {
        brokerPreferredLeaderPartitionCount.compute(brokerId, (k, v) -> v == null ? partitionCount : v + partitionCount);
    }

    public Collection<Integer> getExpectedBrokerIds() {
        return brokerPreferredLeaderPartitionCount.keySet();
    }

    public int getBrokerCount() {
        return brokerCount;
    }

    public void setBrokerCount(int brokerCount) {
        this.brokerCount = brokerCount;
    }

    public Date  getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }
}
