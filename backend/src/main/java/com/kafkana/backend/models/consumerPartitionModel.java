package com.kafkana.backend.models;

import java.io.Serializable;

public class consumerPartitionModel implements Serializable {
    private final String groupId;
    private final String topic;
    private final int partitionId;
    private long offset;
    private long size;
    private long firstOffset;

    public consumerPartitionModel(String groupId, String topic, int partitionId) {
        this.groupId = groupId;
        this.topic = topic;
        this.partitionId = partitionId;
    }

    public String getTopic() {
        return topic;
    }

    public int getPartitionId() {
        return partitionId;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getFirstOffset() {
        return firstOffset;
    }

    public void setFirstOffset(long firstOffset) {
        this.firstOffset = firstOffset;
    }

    public long getLag() {
        if (size < 0 || firstOffset < 0) {
            return 0;
        } else if (offset < firstOffset) {
            return size - firstOffset;
        } else {
            return size - offset;
        }
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    @Override
    public String toString() {
        return consumerPartitionModel.class.getSimpleName() + " [groupId=" + groupId +
                ", topic=" + topic + ", partitionId=" + partitionId + ", offset=" + offset +
                ", size=" + size + ", firstOffset=" + firstOffset + "]";
    }
}
