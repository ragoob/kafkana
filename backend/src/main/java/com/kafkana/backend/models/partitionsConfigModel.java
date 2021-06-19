package com.kafkana.backend.models;

import java.io.Serializable;
import java.util.ArrayList;

public class partitionsConfigModel  implements Serializable {
    private  String topic;
    private  int partition;
    private ArrayList<Integer>  replicas;

    public  partitionsConfigModel(String topic,int partition,ArrayList<Integer>  replicas){
      this.setTopic(topic);
      this.setPartition(partition);
      this.setReplicas(replicas);
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

    public ArrayList<Integer> getReplicas() {
        return replicas;
    }

    public void setReplicas(ArrayList<Integer> replicas) {
        this.replicas = replicas;
    }


}
