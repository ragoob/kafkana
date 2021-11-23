package com.kafkana.backend.models;

public class consumerMemberModel {

    public consumerMemberModel(String id) {
        this.id = id;
    }

    public consumerMemberModel() {
    }

    private String id;
    private  String clientId;
    private  String host;
    private  String topic;
    private  int partition;
    private  long lastCommittedOffset;
    private  long endOffsets;
    private  long lag;
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }


    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public int getPartition() {
        return partition;
    }

    public void setPartition(int partition) {
        this.partition = partition;
    }

    public long getLastCommittedOffset() {
        return lastCommittedOffset;
    }

    public void setLastCommittedOffset(long lastcommittedOffset) {
        this.lastCommittedOffset = lastcommittedOffset;
    }

    public long getEndOffsets() {
        return endOffsets;
    }

    public void setEndOffsets(long endOffsets) {
        this.endOffsets = endOffsets;
    }

    public long getLag() {
        return lag;
    }

    public void setLag(long lag) {
        this.lag = lag;
    }
}
