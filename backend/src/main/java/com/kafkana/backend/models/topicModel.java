package com.kafkana.backend.models;

import java.util.*;
import java.util.stream.Collectors;

public class topicModel  implements Comparable<topicModel>{
    private final String name;

    private Map<Integer, topicPartitionModel> partitions = new TreeMap<>();

    private Map<String, String> config = Collections.emptyMap();

    public topicModel(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Map<String, String> getConfig() {
        return config;
    }

    public void setConfig(Map<String, String> config) {
        this.config = config;
    }

    public Map<Integer, topicPartitionModel> getPartitionMap() {
        return Collections.unmodifiableMap(partitions);
    }

    public Collection<topicPartitionModel> getPartitions() {
        return Collections.unmodifiableCollection(partitions.values());
    }

    public void setPartitions(Map<Integer, topicPartitionModel> partitions) {
        this.partitions = partitions;
    }

    public Optional<topicPartitionModel> getPartition(int partitionId) {
        return Optional.ofNullable(partitions.get(partitionId));
    }

    public Collection<topicPartitionModel> getLeaderPartitions(int brokerId) {
        return partitions.values().stream()
                .filter(tp -> tp.getLeader() != null && tp.getLeader().getId() == brokerId)
                .collect(Collectors.toList());
    }

    public Collection<topicPartitionModel> getUnderReplicatedPartitions() {
        return partitions.values().stream()
                .filter(topicPartitionModel::isUnderReplicated)
                .collect(Collectors.toList());
    }

    /**
     * Returns the total number of messages published to the topic, ever
     * @return
     */
    public long getTotalSize() {
        return partitions.values().stream()
                .map(topicPartitionModel::getSize)
                .reduce(0L, Long::sum);
    }

    /**
     * Returns the total number of messages available to consume from the topic.
     * @return
     */
    public long getAvailableSize() {
        return partitions.values().stream()
                .map(p -> p.getSize())
                .reduce(0L, Long::sum);
    }

    public double getPreferredReplicaPercent() {
        if (partitions.isEmpty()) {
            return 0;
        } else {
            final var preferredLeaderCount = partitions.values().stream()
                    .filter(topicPartitionModel::isLeaderPreferred)
                    .count();
            return ((double) preferredLeaderCount) / ((double) partitions.size());
        }
    }

    @Override
    public int compareTo(topicModel that) {
        return this.name.compareTo(that.name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof topicModel) {
            final var that = (topicModel) o;
            return Objects.equals(name, that.name);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

    @Override
    public String toString() {
        return topicModel.class.getSimpleName() + " [name=" + name +", partitions=" + partitions + "]";
    }
}
