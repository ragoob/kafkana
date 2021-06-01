package com.kafkana.backend.repositories;

import com.kafkana.backend.models.kafkaCluster;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

@Repository
@Component
public class kafkaClusterRepository  implements EnvironmentAware {
    private static   ArrayList<kafkaCluster> collections;
    private  String defaultBootStrapServer;
    public kafkaClusterRepository( ) {

        this.collections = new ArrayList<kafkaCluster>();
        this.SetDefault();
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

    private  void  SetDefault(){
       if(defaultBootStrapServer != null){
           final  var cluster = new kafkaCluster();
           cluster.setId("default-cluster");
           cluster.setBootStrapServers(defaultBootStrapServer);
           this.collections.add(cluster);
       }

    }

    @Override
    public void setEnvironment(Environment environment) {
        this.defaultBootStrapServer = environment.getProperty("DEFAULT_BOOTSTRAP_SERVER");
    }
}

