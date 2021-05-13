package com.kafkana.backend.models;

public class kafkaCluster {
    private String id;
    private  String bootStrapServers;

    public kafkaCluster(String id, String bootStrapServers) {
        this.id = id;
        this.bootStrapServers = bootStrapServers;
    }

    public kafkaCluster() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBootStrapServers() {
        return bootStrapServers;
    }

    public void setBootStrapServers(String bootStrapServers) {
        this.bootStrapServers = bootStrapServers;
    }
}
