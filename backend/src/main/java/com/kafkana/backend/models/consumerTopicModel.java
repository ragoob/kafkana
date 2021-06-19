package com.kafkana.backend.models;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

public class consumerTopicModel implements Serializable {
    private final String topic;
    private final Map<Integer, consumerPartitionModel> offsets = new TreeMap<>();

    public consumerTopicModel(String topic) {
        this.topic = topic;
    }

    public String getTopic() {
        return topic;
    }

    public void addOffset(consumerPartitionModel offset) {
        offsets.put(offset.getPartitionId(), offset);
    }

    public long getLag() {
        return offsets.values().stream()
                .map(consumerPartitionModel::getLag)
                .filter(lag -> lag >= 0)
                .reduce(0L, Long::sum);
    }

    public Collection<consumerPartitionModel> getPartitions() {
        return offsets.values();
    }
}
