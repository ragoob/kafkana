package com.kafkana.backend.models;

import java.util.List;

public class partitionInfo {
   private int partition;
   private brokers leader;
    private List<brokers> replicas;
    private List<brokers> isr;

    public partitionInfo(int partition, brokers leader, List<brokers> replicas, List<brokers> isr) {
        this.partition = partition;
        this.leader = leader;
        this.replicas = replicas;
        this.isr = isr;
    }

    public int getPartition() {
        return partition;
    }

    public brokers getLeader() {
        return leader;
    }

    public List<brokers> getReplicas() {
        return replicas;
    }

    public List<brokers> getIsr() {
        return isr;
    }
}
