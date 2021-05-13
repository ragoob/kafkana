package com.kafkana.backend.repositories;

import com.kafkana.backend.models.kafkaCluster;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

@Repository
public class kafkaClusterRepository {
    private static   ArrayList<kafkaCluster> collections;

    public kafkaClusterRepository( ) {
        this.collections = new ArrayList<kafkaCluster>();
    }

    public  void  add(kafkaCluster cluster){
        this.collections.add(cluster);
    }

    public  void  remove(String id){
        int index = this.collections.indexOf(getById(id).get());
        this.collections.remove(index);

    }

    public  ArrayList<kafkaCluster> getAll(){
        return  this.collections;
    }

    public Optional<kafkaCluster> getById(String id){

        return  this.collections.stream().filter(c-> c.getId().equals(id)).findFirst();
    }
}

