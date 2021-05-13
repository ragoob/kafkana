package com.kafkana.backend.models;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
public class consumerModel implements Comparable<consumerModel> {

    private final String groupId;
    private final Map<String, consumerTopicModel> topics = new TreeMap<>();

    public consumerModel(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void addTopic(consumerTopicModel topic) {
        topics.put(topic.getTopic(), topic);
    }

    public consumerTopicModel getTopic(String topic) {
        return topics.get(topic);
    }

    public Collection<consumerTopicModel> getTopics() {
        return topics.values();
    }

    @Override
    public int compareTo(consumerModel that) {
        return this.groupId.compareTo(that.groupId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof consumerModel) {
            final var that = (consumerModel) o;
            return Objects.equals(groupId, that.groupId);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(groupId);
    }

}
