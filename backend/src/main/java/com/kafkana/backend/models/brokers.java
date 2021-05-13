package com.kafkana.backend.models;

import org.apache.kafka.clients.admin.Config;
import org.apache.kafka.clients.admin.ConfigEntry;
import org.apache.kafka.common.Node;

import java.util.HashMap;
import java.util.Map;

public class brokers {
    private  int id;
    private  String host;
    private  int port;
    private  String rack;
    private HashMap<String, String> config;
    private  boolean isController;

    public int getId() {
        return id;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getRack() {
        return rack;
    }


    public brokers(int id, String host , int port, String rack, HashMap<String,String> config, boolean isController) {
        this.id = id;
        this.host = host;
        this.port = port;
        this.rack = rack;
        this.config = config;
        this.isController = isController;
    }

    public brokers(int id, String host, int port) {
        this.id = id;
        this.host = host;
        this.port = port;
        this.setConfig(null);

    }

    public brokers(int id, String host, int port, String rack) {
        this.id = id;
        this.host = host;
        this.port = port;
        this.rack = rack;
        this.setConfig(null);

    }

    public HashMap<String,String> getConfig() {
        return config;
    }

    public void setConfig(HashMap<String,String> config) {
        this.config = config;
    }

    public boolean isController() {
        return isController;
    }
}
