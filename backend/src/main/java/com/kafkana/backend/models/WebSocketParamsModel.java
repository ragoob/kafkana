package com.kafkana.backend.models;

import java.io.Serializable;

public class WebSocketParamsModel implements Serializable {
    public  WebSocketParamsModel(){

    }
    private  String clusterIp;
    private  boolean refresh;

    public String getClusterIp() {
        return clusterIp;
    }

    public void setClusterIp(String clusterId) {
        this.clusterIp = clusterId;
    }

    public boolean isRefresh() {
        return refresh;
    }

    public void setRefresh(boolean refresh) {
        this.refresh = refresh;
    }
}
