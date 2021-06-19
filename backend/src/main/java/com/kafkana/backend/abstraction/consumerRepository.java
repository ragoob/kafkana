package com.kafkana.backend.abstraction;
import com.kafkana.backend.models.consumerModel;

import java.util.List;

public interface consumerRepository {
    void save(consumerModel consumer,String clusterId);
    void save(List<consumerModel> consumers,String clusterId);
    List<consumerModel> findAll(String clusterId);
    void delete(String groupId,String clusterId);
}
