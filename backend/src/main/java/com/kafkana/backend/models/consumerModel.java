package com.kafkana.backend.models;
import java.io.Serializable;
import java.util.*;

public class consumerModel implements Comparable<consumerModel>, Serializable {

    private final String groupId;
    private  final   brokers broker;
    private List<consumerMemberModel> members;

    public consumerModel(String groupId, com.kafkana.backend.models.brokers broker) {
        this.groupId = groupId;
        this.broker = broker;
    }

    public String getGroupId() {
        return groupId;
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

    public List<consumerMemberModel> getMembers() {
        return members;
    }

    public void setMembers(List<consumerMemberModel> members) {
        this.members = members;
    }

    public com.kafkana.backend.models.brokers getBroker() {
        return broker;
    }
}
