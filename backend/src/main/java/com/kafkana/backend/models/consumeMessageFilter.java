package com.kafkana.backend.models;

import java.util.Optional;

public class consumeMessageFilter {
    private  String topicName;
    private  String clusterId;
    private boolean fromBegaining;
    private  int startOffset;
    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }


    public boolean isFromBegaining() {
        return fromBegaining;
    }

    public void setFromBegaining(boolean fromBegaining) {
        this.fromBegaining = fromBegaining;
    }

    public int getStartOffset() {
        return startOffset;
    }

    public void setStartOffset(int startOffset) {
        this.startOffset = startOffset;
    }
}
