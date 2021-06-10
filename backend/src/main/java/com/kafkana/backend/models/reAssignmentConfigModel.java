package com.kafkana.backend.models;

import java.util.ArrayList;

public class reAssignmentConfigModel {
    private ArrayList<partitionsConfigModel> partitions;

    public ArrayList<partitionsConfigModel> getPartitions() {
        return partitions;
    }

    public void setPartitions(ArrayList<partitionsConfigModel> partitions) {
        this.partitions = partitions;
    }
}
