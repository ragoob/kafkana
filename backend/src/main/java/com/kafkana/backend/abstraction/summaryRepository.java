package com.kafkana.backend.abstraction;

import com.kafkana.backend.models.clusterSummaryModel;
import com.kafkana.backend.models.topicModel;

import java.util.List;

public interface summaryRepository {
    void save(clusterSummaryModel summary,String clusterId);
    clusterSummaryModel find(String clusterId);
    void delete(String clusterId);
}
